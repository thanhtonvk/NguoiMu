#pragma once

#include <opencv2/core/core.hpp>

#include <net.h>
#include "yolo.h"


class yolov9
{
public:
    yolov9();

    int load(AAssetManager* mgr,int target_size, const float* norm_vals, bool use_gpu = false);

    int detect(const cv::Mat& rgb, std::vector<Object>& objects, float prob_threshold = 0.3f, float nms_threshold = 0.2f);

    int draw(cv::Mat& rgb, const std::vector<Object>& objects);

private:

    ncnn::Net yolo;

    int target_size;
    float norm_vals[3];
    ncnn::UnlockedPoolAllocator blob_pool_allocator;
    ncnn::PoolAllocator workspace_pool_allocator;
};
