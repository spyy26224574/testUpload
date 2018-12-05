//
// Created by admin on 2016/11/9.
//

#ifndef FFMPEGVIDEO_VIDEOTRANSLATE_H
#define FFMPEGVIDEO_VIDEOTRANSLATE_H

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
typedef void (*funcProgressListner)(int,int,char *);
class VideoTranslate {
public:
    void translate(const char *in_url, const char *out_url,int bitrate,int width,int height);

    int getProgress();

    int getCurrentState();

    void setProgressListner(funcProgressListner progress);

private:
    funcProgressListner onprogress;
    void translateThread(const char *in_url, const char *out_url,int bitrate,int width,int height);

    /**
     * 回调消息函数
     */
    void onProgressUpdate(int state,int progress,char * info);

    int progress;
    /**
     * 0为无状态，1为开始转码，2为正在转码，3为转换结束
     */
    int currentState=0;
};


#endif //FFMPEGVIDEO_VIDEOTRANSLATE_H
