//
// Created by admin on 2016/11/9.
//
#include <jni.h>
#include "VideoCut.h"
#include "VideoTranslate.h"
VideoCut *videoCut;
VideoTranslate *translate;
extern "C" {
void onCallback(int state, int progress, char *info){
    LOGE("progress = %d,info=%s",progress,info);
}
JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoUtilLib_cutVideo(JNIEnv *env, jobject instance, jstring in_url_,
                                             jstring out_url_, jint start, jint end) {
    const char *in_url = env->GetStringUTFChars(in_url_, 0);
    const char *out_url = env->GetStringUTFChars(out_url_, 0);

    if(!videoCut){
        videoCut=new VideoCut();
        videoCut->setCallback(&onCallback);
    }
    videoCut->cutVideo(in_url,out_url,start,end);
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoUtilLib_release(JNIEnv *env, jobject instance) {
    if(videoCut){
        delete videoCut;
    }
    if(translate){
        delete translate;
    }
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoUtilLib_transcode(JNIEnv *env, jobject instance, jstring in_url_,
                                              jstring out_url_, jint bitrate, jint width,
                                              jint height) {
    const char *in_url = env->GetStringUTFChars(in_url_, 0);
    const char *out_url = env->GetStringUTFChars(out_url_, 0);

    if(!translate){
        translate=new VideoTranslate();
    }
    translate->translate(in_url,out_url,bitrate,width,height);
}
}