//
// Created by huangxy on 2018/1/17.
//

#include <jni.h>
#include "YuvRenderer.h"

extern "C" {
JNIEXPORT jlong JNICALL
Java_com_ligo_medialib_YUVRenderer_createYuvRenderer(JNIEnv *env, jobject instance) {
    return reinterpret_cast<jlong>(new YuvRenderer());
}
JNIEXPORT void JNICALL
Java_com_ligo_medialib_YUVRenderer_nativeRelease(JNIEnv *env, jobject instance, jlong id) {
    YuvRenderer *renderer = reinterpret_cast<YuvRenderer *>(id);
    if (renderer) {
        delete renderer;
    }
}
JNIEXPORT void JNICALL
Java_com_ligo_medialib_YUVRenderer_initGles(JNIEnv *env, jobject instance, jlong id) {
    YuvRenderer *yuvRenderer = reinterpret_cast<YuvRenderer *>(id);
    if (yuvRenderer) {
        yuvRenderer->initGles();
    }
}
JNIEXPORT void JNICALL
Java_com_ligo_medialib_YUVRenderer_changeESLayout(JNIEnv *env, jobject instance, jlong id,
                                                  jint width, jint height) {
    YuvRenderer *renderer = reinterpret_cast<YuvRenderer *>(id);
    if (renderer) {
        renderer->changeESLayout(width, height);
    }

}
JNIEXPORT jint JNICALL
Java_com_ligo_medialib_YUVRenderer_nativeDrawYuv(JNIEnv *env, jobject instance, jlong id,
                                                 jbyteArray data_, jint size, jint width,
                                                 jint height) {
    jint ret = -1;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    YuvRenderer *renderer = reinterpret_cast<YuvRenderer *>(id);
    if (renderer) {
        ret = renderer->drawYuv((char *) data, size, width, height);
    }
    env->ReleaseByteArrayElements(data_, data, 0);
    return ret;
}
}