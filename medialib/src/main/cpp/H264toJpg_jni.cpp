//
// Created by huangxy on 2018/7/11.
//

#include <jni.h>
#include "H264toJpg.h"

extern "C" {
JNIEXPORT jint JNICALL
Java_com_ligo_medialib_H264toJpg_nativeInitDecorder(JNIEnv *env, jclass type) {
    return initDecoder();
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_H264toJpg_nativeDecodeFrame(JNIEnv *env, jclass type, jbyteArray data_,
                                                   jint len, jstring out_path_) {
    int ret;
    jbyte *data = env->GetByteArrayElements(data_, NULL);
    const char *out_path = env->GetStringUTFChars(out_path_, 0);
    ret = decodeFrame(reinterpret_cast<char *>(data), len, const_cast<char *>(out_path));
    env->ReleaseByteArrayElements(data_, data, 0);
    env->ReleaseStringUTFChars(out_path_, out_path);
    return ret;
}
JNIEXPORT void JNICALL
Java_com_ligo_medialib_H264toJpg_nativeDeInitDecoder(JNIEnv *env, jclass type) {
    deInitDecoder();
}
}
