//
// Created by thanh on 3/15/2025.
//

#include "chu_cai.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "cpu.h"
#include<iostream>

static inline float intersection_area(const Object &a, const Object &b) {
    if (a.rect.x > b.rect.x + b.rect.width || a.rect.x + a.rect.width < b.rect.x ||
        a.rect.y > b.rect.width + b.rect.height || a.rect.y + a.rect.height < b.rect.y) {
        // no intersection
        return 0.f;
    }

    float inter_width = std::min(a.rect.x + a.rect.width, b.rect.x + b.rect.width) -
                        std::max(a.rect.x, b.rect.x);
    float inter_height = std::min(a.rect.y + a.rect.height, b.rect.y + b.rect.height) -
                         std::max(a.rect.y, b.rect.y);

    return inter_width * inter_height;
}

static void qsort_descent_inplace(std::vector<Object> &faceobjects, int left, int right) {
    int i = left;
    int j = right;
    float p = faceobjects[(left + right) / 2].prob;

    while (i <= j) {
        while (faceobjects[i].prob > p)
            i++;

        while (faceobjects[j].prob < p)
            j--;

        if (i <= j) {
            // swap
            std::swap(faceobjects[i], faceobjects[j]);

            i++;
            j--;
        }
    }

#pragma omp parallel sections
    {
#pragma omp section
        {
            if (left < j) qsort_descent_inplace(faceobjects, left, j);
        }
#pragma omp section
        {
            if (i < right) qsort_descent_inplace(faceobjects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector<Object> &faceobjects) {
    if (faceobjects.empty())
        return;

    qsort_descent_inplace(faceobjects, 0, faceobjects.size() - 1);
}

static void nms_sorted_bboxes(const std::vector<Object> &faceobjects, std::vector<int> &picked,
                              float nms_threshold) {
    picked.clear();

    const int n = faceobjects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++) {
        areas[i] = faceobjects[i].rect.width * faceobjects[i].rect.height;
    }

    for (int i = 0; i < n; i++) {
        const Object &a = faceobjects[i];

        int keep = 1;
        for (int j = 0; j < (int) picked.size(); j++) {
            const Object &b = faceobjects[picked[j]];

            // intersection over union
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[picked[j]] - inter_area;
            // float IoU = inter_area / union_area
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.push_back(i);
    }
}

static inline float sigmoid(float x) {
    return static_cast<float>(1.f / (1.f + exp(-x)));
}


static void
generate_proposals(const ncnn::Mat &feat_blob, float prob_threshold, std::vector<Object> &objects) {
    float *data = (float *) feat_blob.data;
    for (int i = 0; i < feat_blob.h; i++) {
        int class_index = -1;
        float confidence = 0;

        for (int j = 4; j < feat_blob.w; j++) {
            if (data[i * feat_blob.w + j] >= prob_threshold) {
                class_index = j - 4;
                confidence = data[i * feat_blob.w + j];
                float x0 = data[i * feat_blob.w] - data[i * feat_blob.w + 2] * 0.5f;
                float y0 = data[i * feat_blob.w + 1] - data[i * feat_blob.w + 3] * 0.5f;
                float x1 = data[i * feat_blob.w] + data[i * feat_blob.w + 2] * 0.5f;
                float y1 = data[i * feat_blob.w + 1] + data[i * feat_blob.w + 3] * 0.5f;

                Object obj;
                obj.rect.x = x0;
                obj.rect.y = y0;
                obj.rect.width = x1 - x0;
                obj.rect.height = y1 - y0;
                obj.label = class_index;
                obj.prob = confidence;
                objects.push_back(obj);
            }
        }
    }
}

static ncnn::Mat transpose(const ncnn::Mat &in) {
    ncnn::Mat out(in.h, in.w, in.c);
    for (int q = 0; q < in.c; q++) {
        const float *ptr = in.channel(q);
        float *outptr = out.channel(q);
        for (int y = 0; y < in.h; y++) {
            for (int x = 0; x < in.w; x++) {
                outptr[x * in.h + y] = ptr[y * in.w + x];
            }
        }
    }

    return out;
}

chu_cai::chu_cai() {
    blob_pool_allocator.set_size_compare_ratio(0.f);
    workspace_pool_allocator.set_size_compare_ratio(0.f);
}

int chu_cai::load(AAssetManager *mgr, int _target_size, const float *_norm_vals, bool use_gpu) {
    yolo.clear();
    blob_pool_allocator.clear();
    workspace_pool_allocator.clear();

    ncnn::set_cpu_powersave(2);
    ncnn::set_omp_num_threads(ncnn::get_big_cpu_count());

    yolo.opt = ncnn::Option();

//#if NCNN_VULKAN
//	yolo.opt.use_vulkan_compute = use_gpu;
//#endif

    yolo.opt.num_threads = ncnn::get_big_cpu_count();
    yolo.opt.blob_allocator = &blob_pool_allocator;
    yolo.opt.workspace_allocator = &workspace_pool_allocator;
    yolo.opt.use_vulkan_compute = false;

    char parampath[256];
    char modelpath[256];
    sprintf(parampath, "chu_cai.param");
    sprintf(modelpath, "chu_cai.bin");

    yolo.load_param(mgr, parampath);
    yolo.load_model(mgr, modelpath);

    target_size = _target_size;
    norm_vals[0] = _norm_vals[0];
    norm_vals[1] = _norm_vals[1];
    norm_vals[2] = _norm_vals[2];

    return 0;
}

int chu_cai::detect(const cv::Mat &rgb, std::vector<Object> &objects, float prob_threshold,
                     float nms_threshold) {
    int width = rgb.cols;
    int height = rgb.rows;

    int w = width;
    int h = height;
    float scale = 1.f;
    if (w > h) {
        scale = (float) target_size / w;
        w = target_size;
        h = h * scale;
    } else {
        scale = (float) target_size / h;
        h = target_size;
        w = w * scale;
    }

    ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgb.data, ncnn::Mat::PIXEL_RGB, width, height,
                                                 w, h);

    int dw = target_size - w;
    int dh = target_size - h;
    dw = dw / 2;
    dh = dh / 2;

    int top = static_cast<int>(std::round(dh - 0.1));
    int bottom = static_cast<int>(std::round(dh + 0.1));
    int left = static_cast<int>(std::round(dw - 0.1));
    int right = static_cast<int>(std::round(dw + 0.1));


    ncnn::Mat in_pad;
    ncnn::copy_make_border(in, in_pad, top, bottom, left, right, ncnn::BORDER_CONSTANT, 114.f);

    const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
    in_pad.substract_mean_normalize(0, norm_vals);

    ncnn::Extractor ex = yolo.create_extractor();

    ex.input("in0", in_pad);

    {
        ncnn::Mat out;
        ex.extract("out0", out);

        ncnn::Mat out_t = transpose(out);
        generate_proposals(out_t, prob_threshold, objects);

    }
    qsort_descent_inplace(objects);

    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(objects, picked, nms_threshold);

    int count = picked.size();

    std::vector<Object> newobjects;
    newobjects.resize(count);
    for (int i = 0; i < count; i++) {
        newobjects[i] = objects[picked[i]];
        // adjust offset to original unpadded
        float x0 = (newobjects[i].rect.x - dw) / scale;
        float y0 = (newobjects[i].rect.y - dh) / scale;
        float x1 = (newobjects[i].rect.x + newobjects[i].rect.width - dw) / scale;
        float y1 = (newobjects[i].rect.y + newobjects[i].rect.height - dh) / scale;

        //clip
        x0 = std::max(std::min(x0, (float) (width - 1)), 0.f);
        y0 = std::max(std::min(y0, (float) (height - 1)), 0.f);
        x1 = std::max(std::min(x1, (float) (width - 1)), 0.f);
        y1 = std::max(std::min(y1, (float) (height - 1)), 0.f);

        newobjects[i].rect.x = x0;
        newobjects[i].rect.y = y0;
        newobjects[i].rect.width = x1 - x0;
        newobjects[i].rect.height = y1 - y0;
    }

    objects = newobjects;
    auto max_prob_obj = std::max_element(objects.begin(), objects.end(),
                                         [](const Object &a, const Object &b) {
                                             return a.prob < b.prob;
                                         });
    if (max_prob_obj != objects.end()) {
        objects = {*max_prob_obj};
    }

    return 0;

}

int chu_cai::draw(cv::Mat &rgb, const std::vector<Object> &objects) {
    static const char *class_names[] = {
                                        "a",
                                        "a",
                                        "a",
                                        "b",
                                        "c",
                                        "d",
                                        "d",
                                        "e",
                                        "e",
                                        "g",
                                        "h",
                                        "i",
                                        "k",
                                        "l",
                                        "m",
                                        "n",
                                        "o",
                                        "o",
                                        "o",
                                        "p",
                                        "q",
                                        "r",
                                        "s",
                                        "t",
                                        "u",
                                        "u",
                                        "v",
                                        "x",
                                        "y",
                                        "dau cham",
                                        "dau hoi",
                                        "dau huyen",
                                        "dau nga",
                                        "dau sac"};
    static const unsigned char colors[19][3] = {
            {54,  67,  244},
            {99,  30,  233},
            {176, 39,  156},
            {183, 58,  103},
            {181, 81,  63},
            {243, 150, 33},
            {244, 169, 3},
            {212, 188, 0},
            {136, 150, 0},
            {80,  175, 76},
            {74,  195, 139},
            {57,  220, 205},
            {59,  235, 255},
            {7,   193, 255},
            {0,   152, 255},
            {34,  87,  255},
            {72,  85,  121},
            {158, 158, 158},
            {139, 125, 96}
    };

    int color_index = 0;
    std::cout << objects.size() << std::endl;

    for (size_t i = 0; i < objects.size(); i++) {
        const Object &obj = objects[i];
//        __android_log_print(ANDROID_LOG_DEBUG, "LOGFACE", "len embedding %s",
//                            class_names[obj.label]);
//        __android_log_print(ANDROID_LOG_DEBUG, "LOGFACE", "len embedding %f", obj.prob);
        const unsigned char *color = colors[color_index % 19];
        color_index++;

        cv::Scalar cc(color[0], color[1], color[2]);

        cv::rectangle(rgb, obj.rect, cc, 2);

        char text[256];
        sprintf(text, "%s", class_names[obj.label]);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1,
                                              &baseLine);

        int x = obj.rect.x;
        int y = obj.rect.y - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > rgb.cols)
            x = rgb.cols - label_size.width;

        cv::rectangle(rgb, cv::Rect(cv::Point(x, y),
                                    cv::Size(label_size.width, label_size.height + baseLine)),
                      cc,
                      -1);

        cv::Scalar textcc = (color[0] + color[1] + color[2] >= 381) ? cv::Scalar(0, 0, 0)
                                                                    : cv::Scalar(255, 255, 255);

        cv::putText(rgb, text, cv::Point(x, y + label_size.height), cv::FONT_HERSHEY_SIMPLEX,
                    0.5,
                    textcc, 1);
    }


    return 0;
}