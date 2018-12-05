//
// Created by admin on 2018-01-04.
//

#include <jni.h>
#include "FfmpegPlayer.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_ligo_medialib_VideoDecoder_createDecoder(JNIEnv *env, jobject instance,
                                                  jint videoType, jint width, jint height) {
    FfmpegPlayer *player = new FfmpegPlayer();
    int ret = player->createDecoder(videoType, width, height);
    if (ret == 0) {
        return reinterpret_cast<jlong>(player);
    }
    return -1;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_nativeSetSurface(JNIEnv *env, jobject instance,
                                                     jlong id, jobject surface, jint width,
                                                     jint height) {
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->setupSurface(env, surface, width, height);
    }
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_nativeStart(JNIEnv *env, jobject instance, jlong id) {

    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->start();
    }
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_nativeDecodeVideo(JNIEnv *env, jobject instance,
                                                      jlong id, jbyteArray data_,
                                                      jint len) {
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->decodeFrame((char *) data, len);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_nativeRelease(JNIEnv *env, jobject instance, jlong id) {

    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->stop();
        delete player;
    }
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_initGles(JNIEnv *env, jobject instance, jlong id,
                                             jint width, jint height) {
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->initGles(width, height);
    }

}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_VideoDecoder_changeESLayout(JNIEnv *env, jobject instance, jlong id,
                                                   jint width, jint height) {
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        player->changeESLayout(width, height);
    }

}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_VideoDecoder_drawESFrame(JNIEnv *env, jobject instance, jlong id) {
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        return player->drawESFrame();
    }
    return -1;
}
JNIEXPORT jint JNICALL
Java_com_ligo_medialib_VideoDecoder_nativeDrawYuv(JNIEnv *env, jobject instance, jlong id,
                                                  jbyteArray data_, jint size) {
    jint ret = -1;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    FfmpegPlayer *player = reinterpret_cast<FfmpegPlayer *>(id);
    if (player) {
        ret = player->drawYuv((char *) data, size);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}
}
