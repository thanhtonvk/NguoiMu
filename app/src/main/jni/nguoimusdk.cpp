// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#include <android/asset_manager_jni.h>
#include <android/native_window_jni.h>
#include <android/native_window.h>

#include <android/log.h>

#include <jni.h>

#include <string>
#include <vector>

#include <platform.h>
#include <benchmark.h>

#include "yolo.h"

#include "ndkcamera.h"
#include "scrfd.h"
#include "face_emb.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <iostream>
#include <android/bitmap.h>
#include <opencv2/opencv.hpp>

using namespace cv;

#if __ARM_NEON
#include <arm_neon.h>
#endif // __ARM_NEON

static int draw_unsupported(cv::Mat &rgb) {
    const char text[] = "unsupported";

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 1.0, 1, &baseLine);

    int y = (rgb.rows - label_size.height) / 2;
    int x = (rgb.cols - label_size.width) / 2;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y),
                                cv::Size(label_size.width, label_size.height + baseLine)),
                  cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 1.0, cv::Scalar(0, 0, 0));

    return 0;
}

static int draw_fps(cv::Mat &rgb) {
    // resolve moving average
    float avg_fps = 0.f;
    {
        static double t0 = 0.f;
        static float fps_history[10] = {0.f};

        double t1 = ncnn::get_current_time();
        if (t0 == 0.f) {
            t0 = t1;
            return 0;
        }

        float fps = 1000.f / (t1 - t0);
        t0 = t1;

        for (int i = 9; i >= 1; i--) {
            fps_history[i] = fps_history[i - 1];
        }
        fps_history[0] = fps;

        if (fps_history[9] == 0.f) {
            return 0;
        }

        for (int i = 0; i < 10; i++) {
            avg_fps += fps_history[i];
        }
        avg_fps /= 10.f;
    }

    char text[32];
    sprintf(text, "FPS=%.2f", avg_fps);

    int baseLine = 0;
    cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

    int y = 0;
    int x = rgb.cols - label_size.width;

    cv::rectangle(rgb, cv::Rect(cv::Point(x, y),
                                cv::Size(label_size.width, label_size.height + baseLine)),
                  cv::Scalar(255, 255, 255), -1);

    cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));

    return 0;
}

static Yolo *g_yolo = 0;
static SCRFD *g_scrfd = 0;
static FaceEmb *g_faceEmb = 0;

static ncnn::Mutex lock;


static std::vector<Object> objects;
static std::vector<FaceObject> faceObjects;
static std::vector<float> embedding;
static cv::Mat faceAligned;

class MyNdkCamera : public NdkCameraWindow {
public:
    virtual void on_image_render(cv::Mat &rgb) const;
};

void MyNdkCamera::on_image_render(cv::Mat &rgb) const {
    // nanodet
    {
        ncnn::MutexLockGuard g(lock);

        if (g_yolo) {
            g_scrfd->detect(rgb, faceObjects);

            if (faceObjects.size() > 0) {
                __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "pass1");
                g_faceEmb->getEmbeding(rgb, faceObjects[0].landmark, embedding, faceAligned);
            }
            g_yolo->detect(rgb, objects);

            g_yolo->draw(rgb, objects);
            g_scrfd->draw(rgb, faceObjects);


        } else {
            draw_unsupported(rgb);
        }
    }

    draw_fps(rgb);
}

static MyNdkCamera *g_camera = 0;

extern "C" {

JNIEXPORT jint
JNI_OnLoad(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnLoad");

    g_camera = new MyNdkCamera;

    return JNI_VERSION_1_4;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "JNI_OnUnload");

    {
        ncnn::MutexLockGuard g(lock);

        delete g_yolo;
        g_yolo = 0;

        delete g_scrfd;
        g_scrfd = 0;

        delete g_faceEmb;
        g_faceEmb = 0;
    }

    delete g_camera;
    g_camera = 0;
}

extern "C" jboolean
Java_com_tondz_nguoimu_NguoiMuSDK_loadModel(JNIEnv *env, jobject thiz, jobject assetManager,
                                            jint modelid, jint cpugpu) {
    if (modelid < 0 || modelid > 6 || cpugpu < 0 || cpugpu > 1) {
        return JNI_FALSE;
    }

    AAssetManager *mgr = AAssetManager_fromJava(env, assetManager);

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    const char *modeltypes[] =
            {
                    "n",
                    "s",
            };

    const int target_sizes[] =
            {
                    320,
                    320,
            };

    const float mean_vals[][3] =
            {
                    {103.53f, 116.28f, 123.675f},
                    {103.53f, 116.28f, 123.675f},
            };

    const float norm_vals[][3] =
            {
                    {1 / 255.f, 1 / 255.f, 1 / 255.f},
                    {1 / 255.f, 1 / 255.f, 1 / 255.f},
            };

    const char *modeltype = modeltypes[(int) modelid];
    int target_size = target_sizes[(int) modelid];
    bool use_gpu = (int) cpugpu == 1;

    // reload
    {
        ncnn::MutexLockGuard g(lock);

        if (use_gpu && ncnn::get_gpu_count() == 0) {
            // no gpu
            delete g_yolo;
            g_yolo = 0;
            delete g_scrfd;
            g_scrfd = 0;
            delete g_faceEmb;
            g_faceEmb = 0;
        } else {
            if (!g_yolo)
                g_yolo = new Yolo;
            g_yolo->load(mgr, modeltype, target_size, mean_vals[(int) modelid],
                         norm_vals[(int) modelid], false);

            if (!g_scrfd)
                g_scrfd = new SCRFD;
            g_scrfd->load(mgr, modeltype, false);

            if (!g_faceEmb)
                g_faceEmb = new FaceEmb;
            g_faceEmb->load(mgr);
        }
    }

    return JNI_TRUE;
}

// public native boolean openCamera(int facing);

extern "C" jboolean
Java_com_tondz_nguoimu_NguoiMuSDK_openCamera(JNIEnv *env, jobject thiz, jint facing) {
    if (facing < 0 || facing > 1)
        return JNI_FALSE;

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "openCamera %d", facing);

    g_camera->open((int) facing);

    return JNI_TRUE;
}

extern "C" jboolean
Java_com_tondz_nguoimu_NguoiMuSDK_closeCamera(JNIEnv *env, jobject thiz) {
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "closeCamera");

    g_camera->close();

    return JNI_TRUE;
}

extern "C" jboolean
Java_com_tondz_nguoimu_NguoiMuSDK_setOutputWindow(JNIEnv *env, jobject thiz, jobject surface) {
    ANativeWindow *win = ANativeWindow_fromSurface(env, surface);

    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "setOutputWindow %p", win);

    g_camera->set_window(win);

    return JNI_TRUE;
}

}
extern "C" jobject
Java_com_tondz_nguoimu_NguoiMuSDK_getListResult(JNIEnv *env, jobject thiz) {
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject arrayList = env->NewObject(arrayListClass, arrayListConstructor);

    // Get the add method of ArrayList
    jmethodID arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    for (const Object &obj: objects) {
        std::ostringstream oss;
        oss << obj.label << " " << obj.rect.x << " " << obj.rect.y << " "
            << obj.rect.width << " " << obj.rect.height;
        std::string objName = oss.str();
        jstring javaString = env->NewStringUTF(objName.c_str());  // Convert to jstring
        env->CallBooleanMethod(arrayList, arrayListAdd, javaString);
        env->DeleteLocalRef(javaString);  // Clean up local reference
    }
    return arrayList;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_tondz_nguoimu_NguoiMuSDK_getEmbedding(JNIEnv *env, jobject thiz) {
    if (embedding.size() > 0) {
        std::ostringstream oss;

        // Convert each element to string and add it to the stream
        for (size_t i = 0; i < embedding.size(); ++i) {
            if (i != 0) {
                oss << ",";  // Add a separator between elements
            }
            oss << embedding[i];
        }

        // Convert the stream to a string
        std::string embeddingStr = oss.str();
        embedding.clear();
        return env->NewStringUTF(embeddingStr.c_str());
    }
    return env->NewStringUTF("");

}


jobject mat_to_bitmap(JNIEnv *env, Mat &src, bool needPremultiplyAlpha) {
    jclass java_bitmap_class = env->FindClass("android/graphics/Bitmap");
    jclass bmpCfgCls = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID bmpClsValueOfMid = env->GetStaticMethodID(bmpCfgCls, "valueOf",
                                                        "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject jBmpCfg = env->CallStaticObjectMethod(bmpCfgCls, bmpClsValueOfMid,
                                                  env->NewStringUTF("ARGB_8888"));

    jmethodID mid = env->GetStaticMethodID(java_bitmap_class,
                                           "createBitmap",
                                           "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");

    jobject bitmap = env->CallStaticObjectMethod(java_bitmap_class,
                                                 mid, src.cols, src.rows,
                                                 jBmpCfg);

    AndroidBitmapInfo info;
    void *pixels = nullptr;


    // Validate
    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) {
        std::runtime_error("Failed to get Bitmap info.");
    }
    if (src.type() != CV_8UC1 && src.type() != CV_8UC3 && src.type() != CV_8UC4) {
        std::runtime_error("Unsupported cv::Mat type.");
    }
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) {
        std::runtime_error("Failed to lock Bitmap pixels.");
    }
    if (!pixels) {
        std::runtime_error("Bitmap pixels are null.");
    }

    // Convert cv::Mat to the Bitmap format
    if (info.format == ANDROID_BITMAP_FORMAT_RGBA_8888) {
        Mat tmp(info.height, info.width, CV_8UC4, pixels);
        if (src.type() == CV_8UC1) {
            cvtColor(src, tmp, COLOR_GRAY2RGBA);
        } else if (src.type() == CV_8UC3) {
            cvtColor(src, tmp, COLOR_RGB2RGBA);
        } else if (src.type() == CV_8UC4) {
            if (needPremultiplyAlpha) {
                cvtColor(src, tmp, COLOR_RGBA2mRGBA);
            } else {
                src.copyTo(tmp);
            }
        }
    } else if (info.format == ANDROID_BITMAP_FORMAT_RGB_565) {
        Mat tmp(info.height, info.width, CV_8UC2, pixels);
        if (src.type() == CV_8UC1) {
            cvtColor(src, tmp, COLOR_GRAY2BGR565);
        } else if (src.type() == CV_8UC3) {
            cvtColor(src, tmp, COLOR_RGB2BGR565);
        } else if (src.type() == CV_8UC4) {
            cvtColor(src, tmp, COLOR_RGBA2BGR565);
        }
    }

    AndroidBitmap_unlockPixels(env, bitmap);
    return bitmap;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_com_tondz_nguoimu_NguoiMuSDK_getFaceAlign(JNIEnv *env, jobject thiz) {
    jobject bitmap = mat_to_bitmap(env, faceAligned, false);
    return bitmap;
}