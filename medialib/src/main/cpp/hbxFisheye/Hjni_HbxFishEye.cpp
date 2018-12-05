
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include "Hjni_HbxFishEye.h"
#include "utilbase.h"
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "HbxFishEyeManager.h"

CHbxFishEyeManager *fishEyeManager = NULL;

extern "C" {
//
JNIEXPORT void JNICALL Java_com_Hjni_HbxFishEye_Init(JNIEnv *env, jobject obj, jstring dirpath) {
    const char *dir = env->GetStringUTFChars(dirpath, 0);
    LOGE("dirpath = %s", dir);
    fishEyeManager = CHbxFishEyeManager::GetInstance();
    fishEyeManager->Init(dir);
    env->ReleaseStringUTFChars(dirpath, dir);
}

JNIEXPORT void JNICALL Java_com_Hjni_HbxFishEye_Exit(JNIEnv *env, jobject obj) {
    LOGE("Java_Hjni_HbxFishEye_Exit ........... \r\n");
}


JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_UpdateVertex(JNIEnv *env, jclass type) {

    // TODO
    if (fishEyeManager)
        fishEyeManager->UpdateVertices();

}
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_ClearVertex(JNIEnv *env, jclass type) {

    if (fishEyeManager)
        fishEyeManager->ClearVertices();
}


JNIEXPORT jfloatArray JNICALL
Java_com_Hjni_HbxFishEye_GetVertext(JNIEnv *env, jclass type_, jint id, jint width, jint height,
                                    jint type) {
    jfloatArray result;
    int nlength = 0;
    float *arr = NULL;
    if (fishEyeManager) {
        fishEyeManager->GetVertices(id, width, height, type);

        nlength = fishEyeManager->m_nfOutputVerticesCount * 5;
        arr = fishEyeManager->m_pfOutputVertices;

        if (nlength > 0 && arr) {
            result = env->NewFloatArray(nlength);
            env->SetFloatArrayRegion(result, 0, nlength, arr);
            //
            fishEyeManager->m_nfOutputVerticesCount = 0;
            DELETE_BUFFER(fishEyeManager->m_pfOutputVertices);
            arr = NULL;
            return result;
        }
    }
    return NULL;
}
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_SaveId2File(JNIEnv *env, jclass type_, jstring fileName_, jint type,
                                     jint id) {
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    if (fishEyeManager) {
        fishEyeManager->MakeFishEyeFile((char *) fileName, type, id);
    }
    env->ReleaseStringUTFChars(fileName_, fileName);
}
JNIEXPORT jintArray JNICALL
Java_com_Hjni_HbxFishEye_GetId(JNIEnv *env, jclass type, jstring fileName_) {
    const char *fileName = env->GetStringUTFChars(fileName_, 0);
    jintArray result;
    int nType[2];
    if (fishEyeManager) {
        fishEyeManager->MediaFileType((char *) fileName, nType, &nType[1]);
        result = env->NewIntArray(2);
        env->SetIntArrayRegion(result, 0, 2, nType);
    }
    env->ReleaseStringUTFChars(fileName_, fileName);
    return result;
}
JNIEXPORT jint JNICALL
Java_com_Hjni_HbxFishEye_GetCalibrationId(JNIEnv *env, jclass type) {
    int result = 0;
    if (fishEyeManager) {
        result = fishEyeManager->GetCalibrationSn();
    }
    return result;
}

JNIEXPORT jfloatArray JNICALL
Java_com_Hjni_HbxFishEye_GetAngel(JNIEnv *env, jclass type) {
    jfloatArray result = NULL;
    float nAngel[2];
    if (fishEyeManager) {
        if (fishEyeManager->m_CurParame) {
            nAngel[0] = fishEyeManager->m_CurParame->m_fStartAngle;
            nAngel[1] = fishEyeManager->m_CurParame->m_fEndAngle;
        }
        result = env->NewFloatArray(2);
        env->SetFloatArrayRegion(result, 0, 2, nAngel);
    }
    return result;
}
JNIEXPORT void JNICALL
Java_com_Hjni_HbxFishEye_Save(JNIEnv *env, jobject obj, jfloatArray vetex, jstring file) {
    LOGE("Java_Hjni_HbxFishEye_Save ........... \r\n");
    jint length = 0;
    unsigned char buffer[4];
    jfloat *arr = env->GetFloatArrayElements(vetex, NULL);
    length = env->GetArrayLength(vetex);

//	JNI_UTFString jni_arg0(env, file);

//	const char* filepath = jni_arg0.c_str();
    const char *filepath = env->GetStringUTFChars(file, NULL);

    FILE *fp = fopen(filepath, "wb+");
    //
    if (fp) {
        buffer[3] = length & 0xff;
        buffer[2] = (length >> 8) & 0xff;
        buffer[1] = (length >> 16) & 0xff;
        buffer[0] = (length >> 24) & 0xff;
        fwrite(buffer, 4, 1, fp);
        //
        fwrite(arr, sizeof(jfloat), length, fp);
        //
        fclose(fp);
        fp = NULL;
    } else {
        LOGE("Save: read file %s error.... \r\n", filepath);
    }
//	env->ReleaseFloatArrayElements(vetex, arr, 0);
}
#if 0
JNIEXPORT jint JNICALL Java_Hjni_HbxFishEye_VertexAmountByType(JNIEnv *env, jobject obj, jstring file)
{
    int nRet = 0;
    FILE *fp = NULL;
    unsigned char buffer[4];
    //
    JNI_UTFString file(env, file);

    char* filepath = file.c_str;
    fp = fopen(filepath,"r");
    if (fp)
    {
        fread(buffer,4,1,fp);
        nRet =(buffer[0]<<24) & 0xff000000;
        nRet += ((buffer[1] << 16) & 0xff0000);
        nRet += ((buffer[2] << 8) & 0xff00);
        nRet += buffer[3]  & 0xff;
        fclose(fp);
        LOGE("Java_Hjni_HbxFishEye_VertexAmountByType .......nRet=%d.... \r\n", nRet);
    }
    else
    {
        LOGE("VertexAmountByType: read file %s error.... \r\n", filepath);
    }
    return nRet;

}
#endif

JNIEXPORT jfloatArray  JNICALL
Java_com_Hjni_HbxFishEye_VertexfByFile(JNIEnv *env, jobject obj, jstring file,
                                       jobject assetManager) {
    LOGE("Java_Hjni_HbxFishEye_VertexfByType ........... \r\n");
    int nlenght = 0;
    unsigned char buffer[4];
    //
    const char *filename = env->GetStringUTFChars(file, NULL);
    //
    jfloat *arr = NULL;
    AAssetManager *pAssetManager = AAssetManager_fromJava(env, assetManager);
    if (pAssetManager == NULL) {
        LOGI(" %s", "AAssetManager==NULL");
    }
    AAsset *pAsset = AAssetManager_open(pAssetManager, filename, AASSET_MODE_STREAMING);
    AAsset_read(pAsset, buffer, 4);
    nlenght = buffer[0];
    nlenght = nlenght << 8;
    nlenght += buffer[1];
    nlenght = nlenght << 8;
    nlenght += buffer[2];
    nlenght = nlenght << 8;
    nlenght += buffer[3] & 0xff;
    arr = new jfloat[nlenght];
    LOGE("arr =%x  ......nRet=%d..... \r\n", arr, nlenght);
    int len = 0;
    int readed = 0;
    while ((len = AAsset_read(pAsset, arr + readed, nlenght * sizeof(jfloat)))) {
        readed += len;
        LOGE("readed = %d,len = %d", readed, len);
    }
    LOGE("arr = %x", arr);
    jfloatArray result;
    result = env->NewFloatArray(nlenght);
    env->SetFloatArrayRegion(result, 0, nlenght, arr);
//    fp = NULL;
    delete arr;
    return result;
}
}
