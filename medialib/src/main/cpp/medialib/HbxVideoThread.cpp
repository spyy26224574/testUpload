
#include "HbxVideoThread.h"

unsigned char *g_Yuvbuffer = NULL;

VideoCallBack CHbxVideoThread::m_vCallBack = NULL;

CHbxVideoThread::CHbxVideoThread() {
    for (int i = 0; i < _CACHE_COUNT_; i++)
        m_hbxFrame[i] = NULL;
    m_nCount = 0;

    m_nCacheAllowMax = 30;
}

CHbxVideoThread::~CHbxVideoThread() {
    ExitThread();
}

void CHbxVideoThread::CreatThread() {
    CreatNewThread(NULL, (void *) this);
}

void CHbxVideoThread::SetInputFrame(CHbxFrame *frame) {
    m_FrameMutex.Lock();
    if (m_nCount < _CACHE_COUNT_) {
        m_hbxFrame[m_nCount] = frame;
        m_nCount++;

#ifdef _ANDROID_
        pthread_cond_signal(&m_Cond);
#endif

#ifdef _IOS_
        pthread_cond_signal(&m_Cond);
#endif
    } else {
        //cache is full
        if (frame)
            delete frame;
    }
    m_FrameMutex.UnLock();
}

void CHbxVideoThread::Clean() {
    m_FrameMutex.Lock();
    for (int i = 0; i < _CACHE_COUNT_; i++) {
        if (m_hbxFrame[i]) {
            delete m_hbxFrame[i];
        }
        m_hbxFrame[i] = NULL;
    }
    m_FrameMutex.UnLock();
}

CHbxFrame *CHbxVideoThread::GetFrame() {
    CHbxFrame *pFrame = NULL;
    int i = 0;
    if (m_nCount <= 0)
        return pFrame;

    m_FrameMutex.Lock();
    pFrame = m_hbxFrame[0];
    do {
        m_hbxFrame[i] = m_hbxFrame[i + 1];
        i++;
    } while (i < (m_nCount - 1));
    m_nCount--;
    m_hbxFrame[m_nCount] = NULL;
    m_FrameMutex.UnLock();
    return pFrame;
}

void CHbxVideoThread::SetAllowCacheMax(int max) {
    m_nCacheAllowMax = max;
}

void CHbxVideoThread::ExitThread() {
    HBXLOG("CHbxVideoThread::ExitThread 000");
#ifndef _WIN_
    pthread_cond_signal(&m_Cond);
#endif
    CHbxThread::ExitThread();
    if (g_Yuvbuffer) {
        HBXLOG("CHbxVideoThread::ExitThread  delete g_Yuvbuffer");
        delete g_Yuvbuffer;
    }
    g_Yuvbuffer = NULL;
    HBXLOG("CHbxVideoThread::ExitThread 111");
    Clean();
}

void CHbxVideoThread::PlayVideo() {
    m_ThrStatus = _STATUS_THREAD_RUN_;
    HBXLOG("CHbxVideoThread:tid =%d \r\n", gettid());
    while (m_ThrStatus != _STATUS_THREAD_EXIT_) {
        if (m_ThrStatus != _STATUS_THREAD_PAUSE_) {
            CHbxFrame *frame = GetFrame();
            try {
                if (frame) {
                    if (frame->m_avFrame) {
                        if (!g_Yuvbuffer) {
                            g_Yuvbuffer = new unsigned char[frame->m_avFrame->width *
                                                            frame->m_avFrame->height * 3 / 2];
                        }
                        frame->FrameToYuv(g_Yuvbuffer);
                        if (m_vCallBack) {
                            m_vCallBack(frame->m_avFrame->width, frame->m_avFrame->height,
                                        g_Yuvbuffer);
                        }
                    }
                    if (frame->m_pkt) {
                        if (m_vCallBack) {
                            m_vCallBack(frame->m_pkt->size, 0, frame->m_pkt->data);
                            HBXLOG("CHbxVideoThread::PlayVideo 硬解码 size = %d", frame->m_pkt->size);
                        }
                    }
                    delete frame;
                } else {
#ifdef _WIN_
                    msleep(_DELAY_TIME_ / 2);
#endif
#ifdef _ANDROID_
                    struct timeval now;
                    struct timespec outtime;
                    gettimeofday(&now, NULL);
                    outtime.tv_sec = now.tv_sec + 5;
                    outtime.tv_nsec = now.tv_usec * 1000;
                    int err = pthread_cond_timedwait(&m_Cond, &(m_ThreadMutex.m_Mutex),
                                                     (const struct timespec *) &outtime);
                    if (err < 0)
                        HBXLOG("video err = %d ", err);
#endif
                }
            }
            catch (exception &e) {
                CHbxInteractive api;
                api.UpdateMediaInfo("error:play video \r\n", _INFO_ERROR_TAG_);
                HBXLOG("video play error \r\n");
            }
        } else
            msleep(_DELAY_TIME_);
    }
    HBXLOG("CHbxVideoThread::exit........\r\n");
}

void CHbxVideoThread::Run() {
    m_ThreadMutex.Lock();
    PlayVideo();
    m_ThreadMutex.UnLock();
}