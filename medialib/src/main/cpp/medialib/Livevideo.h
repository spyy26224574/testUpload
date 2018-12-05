//
// Created by admin on 2016/11/14.
//

#ifndef FFMPEGVIDEO_LIVEVIDEO_H
#define FFMPEGVIDEO_LIVEVIDEO_H
extern "C" {
#include <jni.h>
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
#include "include/libavutil/imgutils.h"
#include "include/libavutil/opt.h"
#include "include/libavutil/time.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <unistd.h>
#include <pthread.h>
#include "log.h"
}

#include <thread>
#include "log.h"

class Livevideo {
public:
    void startLive(const char* push_url);

private:
    void pushThread(const char * push_url);
};


#endif //FFMPEGVIDEO_LIVEVIDEO_H
