//
// Created by admin on 2016/10/31.
//


#include "videoplayer.h"
#include "HbxMediaPlay.h"
#include "sunInfoPaser.h"


char *g_url = NULL;
CHbxMediaPlay *g_HbxMediaPlay = NULL;
bool gSoftDecodec = true;
CHbxMutex gMutex;

unsigned char *spsInfo = NULL;
int nSpssize = 0;

int gDecodecType = 0;
jobject mListerner = NULL;
JavaVM *jvm = NULL;
jclass listnerclass;
jmethodID mid, update264Frame, updatevideoframe, updateaudioframe;//, updatevideoInfo;
jbyte *frameBuffer;

void notifyMediaInfo(int state, char *message) {
    if (!message || !jvm)
        return;
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            jstring jmsg = _env->NewStringUTF(message);
            _env->CallVoidMethod(mListerner, mid, state, jmsg);
        }
        jvm->DetachCurrentThread();
    }
}

void updateVideoframe(int width, int height, int size, unsigned char *data, int type) {
    JNIEnv *_env = NULL;
    if (size <= 0 || !data || !jvm)
        return;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
//        if (_env != NULL && mListerner != NULL) {
//            _env->CallVoidMethod(mListerner, updatevideoInfo, width, height);
//        }
        if (_env != NULL && mListerner != NULL && frameBuffer != NULL) {
//            jbyteArray jbyteArray = _env->NewByteArray(size);
//            _env->SetByteArrayRegion(jbyteArray, 0, size, (jbyte *) data);
            if (g_HbxMediaPlay && (type == 1)) {
                jbyteArray h264Frame = _env->NewByteArray(size);
                _env->SetByteArrayRegion(h264Frame, 0, size, (jbyte *) data);
                _env->CallVoidMethod(mListerner, update264Frame, h264Frame, size, width, height,
                                     type);
            } else {
                memcpy(frameBuffer, data, size);
                _env->CallVoidMethod(mListerner, updatevideoframe, size, width, height, type);
            }

        }
        jvm->DetachCurrentThread();
    }
}

void updateAudioframe(int size, unsigned char *data) {
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            jbyteArray jbyteArray = _env->NewByteArray(size);
            _env->SetByteArrayRegion(jbyteArray, 0, size, (jbyte *) data);
            _env->CallVoidMethod(mListerner, updateaudioframe, jbyteArray, size);
        }
        jvm->DetachCurrentThread();
    }
}

void VCallBack(int width, int height, unsigned char *data) {
    int len = 0;
    int keyFrame;
    int type = 0;
    if (height != 0) {
        len = width * height * 3 / 2;
        type = 0;
    } else {
        len = width;
        data[0] = 0x00;
        data[1] = 0x00;
        data[2] = 0x00;
        data[3] = 0x01;
        width = g_HbxMediaPlay->m_MediaInfo.nWidth;
        height = g_HbxMediaPlay->m_MediaInfo.nHeight;
        keyFrame = data[4] & 0x1f;
        type = 1;
        if (keyFrame == 0x05) {
            updateVideoframe(width, height, g_HbxMediaPlay->m_MediaInfo.spsppslength,
                             g_HbxMediaPlay->m_MediaInfo.sps, type);
//            HBXLOG("keyFrame = 0x%x spsppslength =%d\r\n", keyFrame,
//                   g_HbxMediaPlay->m_MediaInfo.spsppslength);
        }
    }
//    HBXLOG("keyFrame = 0x%x 0x%x 0x%x 0x%x 0x%x 0x%x 0x%x 0x%x\r\n", data[0], data[1], data[2], data[3],data[4], data[5], data[6], data[7]);
    updateVideoframe(width, height, len, data, type);
}

void ACallBack(int size, unsigned char *data) {
    updateAudioframe(size, data);
}

static void Stop() {
    if (g_HbxMediaPlay) {
        g_HbxMediaPlay->Stop();
    }
}

static int Play(int type) {
    int nRet = -1;
    if (g_HbxMediaPlay) {
        HBXLOG("gSoftDecodec = %d  kevin ........\r\n", type);
        nRet = g_HbxMediaPlay->Open(g_url, type);
    }
    return nRet;
}

static void SetFileUrl(const char *url) {
    if (g_url)
        delete g_url;
    g_url = new char[strlen(url) + 1];

    memset(g_url, 0, strlen(url) + 1);
    memcpy(g_url, url, strlen(url));
}


extern "C" {
JNIEXPORT jintArray JNICALL
Java_com_ligo_medialib_MediaPlayLib_getMediaWH(JNIEnv *env, jobject instance, jstring path_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    g_HbxMediaPlay->OpenFile(path);
    int w = g_HbxMediaPlay->m_MediaInfo.nWidth;
    int h = g_HbxMediaPlay->m_MediaInfo.nHeight;
    int wh[2] = {w, h};
    jintArray pArray = env->NewIntArray(2);
    env->SetIntArrayRegion(pArray, 0, 2, wh);
    env->ReleaseStringUTFChars(path_, path);
    return pArray;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeSetFrameBuffer(JNIEnv *env, jobject instance,
                                                         jbyteArray frameBuffer_) {
    if (frameBuffer) {
        frameBuffer = NULL;
    }
    frameBuffer = env->GetByteArrayElements(frameBuffer_, NULL);
}
JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeDuration(JNIEnv *env, jobject instance) {
    int nRet = -1;
    gMutex.Lock();
    if (g_HbxMediaPlay && g_HbxMediaPlay->Status()) {
        nRet = g_HbxMediaPlay->Duration();
    }
    gMutex.UnLock();
    return nRet;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeSeek(JNIEnv *env, jobject instance, jint second) {

    gMutex.Lock();
    if (g_HbxMediaPlay) {
        g_HbxMediaPlay->Seek(second);
    }
    gMutex.UnLock();
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeCurrent(JNIEnv *env, jobject instance) {

    int nRet = -1;
    gMutex.Lock();
    if (g_HbxMediaPlay) {
        nRet = g_HbxMediaPlay->Current();
    }
    gMutex.UnLock();
    return nRet;
}

JNIEXPORT jbyteArray JNICALL
Java_com_ligo_medialib_MediaPlayLib_playRtsp(JNIEnv *env, jobject instance, jstring url_,
                                             jobject surface) {

    const char *url = env->GetStringUTFChars(url_, 0);
    int nRet = 0;
    gMutex.Lock();
    HBXLOG("playRtsp ......\r\n");
    SetFileUrl(url);
    nRet = Play(0);
    if (!nRet) {
        jbyteArray mediainfo = env->NewByteArray(sizeof(g_HbxMediaPlay->m_MediaInfo));
        env->SetByteArrayRegion(mediainfo, 0, sizeof(g_HbxMediaPlay->m_MediaInfo),
                                (jbyte *) &(g_HbxMediaPlay->m_MediaInfo));
        gMutex.UnLock();
        return mediainfo;
    }
    gMutex.UnLock();
    return NULL;
}


JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeStopPlay(JNIEnv *env, jobject instance) {
    HBXLOG("Stop .....start.\r\n");
    gMutex.Lock();
    Stop();
    gMutex.UnLock();
    HBXLOG("Stop .....end.\r\n");
}


JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativePause(JNIEnv *env, jobject instance) {
    HBXLOG("nativePause ......\r\n");
    gMutex.Lock();
    if (g_HbxMediaPlay)
        g_HbxMediaPlay->Pause();
    gMutex.UnLock();
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeResume(JNIEnv *env, jobject instance) {
    HBXLOG("nativeResume ......\r\n");
    gMutex.Lock();
    if (g_HbxMediaPlay)
        g_HbxMediaPlay->Play();
    gMutex.UnLock();
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeSetListener(JNIEnv *env, jobject instance,
                                                      jobject listener) {
    mListerner = env->NewGlobalRef(listener);
    env->GetJavaVM(&jvm);

    listnerclass = env->GetObjectClass(listener);
    mid = env->GetMethodID(listnerclass, "onInfoUpdate",
                           "(ILjava/lang/String;)V");
    updatevideoframe = env->GetMethodID(listnerclass, "onUpdateFrame",
                                        "(IIII)V");
    updateaudioframe = env->GetMethodID(listnerclass, "onUpdateAudioFrame",
                                        "([BI)V");
//    updatevideoInfo = env->GetMethodID(listnerclass, "onUpdateVideoInfo", "(II)V");
    update264Frame = env->GetMethodID(listnerclass, "update264Frame", "([BIIII)V");
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_replay(JNIEnv *env, jobject instance, jstring url_) {
    HBXLOG("replay kevin ........\r\n");
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_setupSurface(JNIEnv *env, jobject instance, jobject surface,
                                                 jint width, jint height) {
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_setUrl(JNIEnv *env, jobject instance, jstring url_) {
    const char *url = env->GetStringUTFChars(url_, 0);

    gMutex.Lock();
    SetFileUrl(url);
    HBXLOG("setUrl :%s......\r\n", url);
    gMutex.UnLock();
}

JNIEXPORT jbyteArray JNICALL
Java_com_ligo_medialib_MediaPlayLib_nativeStartPlay(JNIEnv *env, jobject instance, jint type) {
    HBXLOG("StartPlay ...type=%d.....\r\n", type);
    int nRet = 0;
    gMutex.Lock();
    nRet = Play(type);
    if (!nRet) {
        jbyteArray mediainfo = env->NewByteArray(sizeof(g_HbxMediaPlay->m_MediaInfo));
        env->SetByteArrayRegion(mediainfo, 0, sizeof(g_HbxMediaPlay->m_MediaInfo),
                                (jbyte *) &(g_HbxMediaPlay->m_MediaInfo));
        gMutex.UnLock();
        return mediainfo;
    }
    gMutex.UnLock();
    return NULL;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_init(JNIEnv *env, jobject instance) {
    HBXLOG("init  \r\n");
    if (!g_HbxMediaPlay) {
        g_HbxMediaPlay = new CHbxMediaPlay();
        env->GetJavaVM(&jvm);
        av_jni_set_java_vm(jvm, NULL);
        CHbxVideoThread::m_vCallBack = VCallBack;
        CHbxAudioPlay::m_aCallBack = ACallBack;
        CHbxInteractive::m_cbUpdateMediaInfo = notifyMediaInfo;
    }
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_getCurrentState(JNIEnv *env, jobject instance) {
//    HBXLOG("getCurrentState  \r\n");
    return g_HbxMediaPlay ? g_HbxMediaPlay->Status() : 0;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_startCut(JNIEnv *env, jobject instance, jstring localname_) {
//    HBXLOG("startCut  \r\n");
    const char *localname = env->GetStringUTFChars(localname_, 0);
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_stopCut(JNIEnv *env, jobject instance) {
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_startLive(JNIEnv *env, jobject instance, jstring pushurl_) {
    const char *pushurl = env->GetStringUTFChars(pushurl_, 0);
    env->ReleaseStringUTFChars(pushurl_, pushurl);
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_setIsLive(JNIEnv *env, jobject instance, jboolean isLive) {

}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_release(JNIEnv *env, jobject instance) {
    HBXLOG("Java_com_ligo_medialib_MediaPlayLib_release  \r\n");
    if (g_HbxMediaPlay) {
        delete g_HbxMediaPlay;
        g_HbxMediaPlay = NULL;
    }
    if (mListerner) {
        env->DeleteGlobalRef(mListerner);
        mListerner = NULL;
    }
    if (frameBuffer) {
        frameBuffer = NULL;
    }
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_startScreenshot(JNIEnv *env, jobject instance,
                                                    jstring localname) {
    return 0;
}

JNIEXPORT void JNICALL
Java_com_ligo_medialib_MediaPlayLib_ChangeDecodec(JNIEnv *env, jobject instance, jint type) {
    HBXLOG("ChangeDecodec  \r\n");
    gMutex.Lock();
    if (g_HbxMediaPlay) {
        g_HbxMediaPlay->ChangeDecodec(type);
//        if (type == 0) {
//            updateVideoframe(g_HbxMediaPlay->m_MediaInfo.nWidth,
//                             g_HbxMediaPlay->m_MediaInfo.nHeight,
//                             g_HbxMediaPlay->m_MediaInfo.spsppslength,
//                             g_HbxMediaPlay->m_MediaInfo.sps, 1);
//        }
    }
    gMutex.UnLock();
}
JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_GetDecodec(JNIEnv *env, jobject instance) {
    if (g_HbxMediaPlay) {
        return g_HbxMediaPlay->GetDecodec();
    }
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_sunGetInfoType(JNIEnv *env, jobject instance,
                                                   jstring localname_) {
    const char *localname = env->GetStringUTFChars(localname_, 0);
    int nRet = 0;
    // TODO
    nRet = sunGetInfoType(localname);
    env->ReleaseStringUTFChars(localname_, localname);
    return nRet;
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_sunSetInfoType(JNIEnv *env, jobject instance,
                                                   jstring localname_, jint type) {
    const char *localname = env->GetStringUTFChars(localname_, 0);
    sunSetInfoType(localname, type);
    env->ReleaseStringUTFChars(localname_, localname);
    return 0;
}

JNIEXPORT jint JNICALL
Java_com_ligo_medialib_MediaPlayLib_Cache(JNIEnv *env, jobject instance) {

    if (g_HbxMediaPlay)
        return g_HbxMediaPlay->Cache();
    return 0;
}
}