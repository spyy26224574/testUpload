//
// Created by admin on 2016/11/9.
//

#ifndef FFMPEGVIDEO_VIDEOCUT_H
#define FFMPEGVIDEO_VIDEOCUT_H
extern "C" {
#include <jni.h>
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
#include "include/libavutil/imgutils.h"
#include "include/libavutil/opt.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <unistd.h>
#include <pthread.h>
#include "log.h"
}

#include <thread>
#include "log.h"

typedef void (*funCallback)(int,int, char *);
class VideoCut {

public:
    void cutVideo(const char *in_url, const char *out_url,int startTime,int endTime);

    void setCallback(funCallback callback);

    int getCurrentState();
    int getProgress();
private:
    void onCallback(int state,int progress,char *info);
    void cutThread(const char *in_url, const char *out_url,int startTime,int endTime);
//    const char *in_url;
//    const char *out_url;
//    int startTime;
//    int endTime;
    funCallback callback;
    /**
     * 当前状态，0为默认状态，1为开始剪切，2为正在剪切，3剪切完成
     */
    int currentState=0;
    /**
     * 进度
     */
    int progress=0;
};


#endif //FFMPEGVIDEO_VIDEOCUT_H
