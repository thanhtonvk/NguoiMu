#include "face_emb.h"

#include <string.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include "aligner.h"
#include "cpu.h"

FaceEmb::FaceEmb() {
    aligner = Aligner();
}

int FaceEmb::load() {
    modelEmb.clear();

    ncnn::set_cpu_powersave(2);
    ncnn::set_omp_num_threads(ncnn::get_big_cpu_count());

    modelEmb.opt = ncnn::Option();


    modelEmb.opt.num_threads = ncnn::get_big_cpu_count();

    char parampath[256];
    char modelpath[256];
    sprintf(parampath, "w600k_mbf.param");
    sprintf(modelpath, "w600k_mbf.bin");

    modelEmb.load_param(parampath);
    modelEmb.load_model(modelpath);

    return 0;
}

int FaceEmb::getEmbeding(cv::Mat src, cv::Point2f landmark[5], std::vector<float> &result) {


    cv::Mat faceAligned;
    std::vector<cv::Point2f> landmarks;
    for (int i = 0; i < 5; i++) {
        cv::Point2f p1 = cv::Point(landmark[i].x, landmark[i].y);
        landmarks.push_back(p1);
    }

    aligner.AlignFace(src, landmarks, &faceAligned);
    ncnn::Mat in_net = ncnn::Mat::from_pixels_resize(faceAligned.clone().data,
                                                     ncnn::Mat::PIXEL_BGR2RGB, faceAligned.cols,
                                                     faceAligned.rows,
                                                     112, 112);
    float norm[3] = {1 / 127.5f, 1 / 127.5f, 1 / 127.5f};
    float mean[3] = {127.5f, 127.5f, 127.5f};
    in_net.substract_mean_normalize(mean, norm);
    ncnn::Extractor extractor = modelEmb.create_extractor();
    extractor.set_light_mode(true);
    extractor.set_num_threads(4);
    extractor.input("input.1", in_net);
    ncnn::Mat outBlob;
    extractor.extract("516", outBlob);
    for (int i = 0; i < outBlob.w; i++) {
        float test = outBlob.row(0)[i];

        result.push_back(test);
    }
    return 0;
}

