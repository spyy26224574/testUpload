//
// Created by huangxy on 2018/9/4.
//

#include <jni.h>

#include "CHbxLsxFishEye.h"

extern "C" {
JNIEXPORT jfloatArray JNICALL
Java_com_ligo_medialib_opengl_ShapeManagerJni_GetVertext(JNIEnv *env, jclass type_, jint width,
                                                         jint height,
                                                         jfloat centerX, jfloat centerY,
                                                         jfloat radius,
                                                         jint type) {
    CHbxLsxFishEye *cHbxLsxFishEye = new CHbxLsxFishEye();
    jfloatArray result = NULL;
    cHbxLsxFishEye->MakeVertex(centerX, centerY, radius, width, height, type);
    int nLength = cHbxLsxFishEye->m_nVerticesCount * 5;
    if (nLength > 0 && cHbxLsxFishEye->m_pDstVertices) {
        result = env->NewFloatArray(nLength);
        env->SetFloatArrayRegion(result, 0, nLength, cHbxLsxFishEye->m_pDstVertices);
    }
    return result;
}
}