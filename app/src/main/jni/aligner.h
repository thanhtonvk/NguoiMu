#ifndef NGUOIMU_ALIGNER_H
#define NGUOIMU_ALIGNER_H

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

class Aligner {
public:
    Aligner();

    ~Aligner();

    int AlignFace(const cv::Mat &img_src,
                  const std::vector<cv::Point2f> &keypoints, cv::Mat *face_aligned);

private:
    class Impl;

    Impl *impl_;
};

#endif