
#include <jni.h>

#ifndef _HBX_SCAN_
#define _HBX_SCAN_

#ifdef __cplusplus
extern "C" {
#endif
JNIEXPORT  void      JNICALL Java_com_Hjni_HbxFishEye_Init(JNIEnv *env, jobject obj, jstring path);
JNIEXPORT  void      JNICALL Java_com_Hjni_HbxFishEye_Exit(JNIEnv *, jobject);
JNIEXPORT  jfloatArray  JNICALL
Java_com_Hjni_HbxFishEye_VertexfByFile(JNIEnv *, jobject, jstring, jobject);
JNIEXPORT  void      JNICALL Java_com_Hjni_HbxFishEye_Save(JNIEnv *, jobject, jfloatArray, jstring);
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_UpdateVertex(JNIEnv *env, jclass type);
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_ClearVertex(JNIEnv *env, jclass type);

JNIEXPORT jfloatArray JNICALL
Java_com_Hjni_HbxFishEye_GetVertext(JNIEnv *env, jclass type_, jint id, jint width, jint height,
                                    jint type);
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_SaveId2File(JNIEnv *env, jclass type_, jstring fileName_, jint type,
                                     jint id);
JNIEXPORT jintArray JNICALL
Java_com_Hjni_HbxFishEye_GetId(JNIEnv *env, jclass type, jstring fileName_);
JNIEXPORT jfloatArray JNICALL
Java_com_Hjni_HbxFishEye_GetAngel(JNIEnv *env, jclass type);
JNIEXPORT jint JNICALL
Java_com_Hjni_HbxFishEye_GetCalibrationId(JNIEnv *env, jclass type);
#ifdef __cplusplus
}
#endif
#endif
