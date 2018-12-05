#include "HbxBaseTasksCheduler.h"


CHbxBaseTasksCheduler::CHbxBaseTasksCheduler() {
    m_VideoThread = new CHbxVideoThread();
    m_AudioThread = new CHbxAudioThread();
}

CHbxBaseTasksCheduler::~CHbxBaseTasksCheduler() {
    ExitThread();
    if (m_VideoThread)
        delete m_VideoThread;

    if (m_AudioThread)
        delete m_AudioThread;
}


void CHbxBaseTasksCheduler::CreatThread() {
    m_nCurrent = 0;
    m_VideoThread->CreatThread();
    m_AudioThread->CreatThread();
    CreatNewThread(NULL, (void *) this);
}

void CHbxBaseTasksCheduler::ExitThread() {
    CHbxThread::ExitThread();
    m_VideoThread->ExitThread();
    m_AudioThread->ExitThread();
}

void CHbxBaseTasksCheduler::Pause() {
    if (m_ThrStatus != _STATUS_THREAD_PAUSE_ && m_ThrStatus != _STATUS_THREAD_EXIT_) {
        m_ThrStatus = _STATUS_THREAD_PAUSE_;
        if (m_VideoThread)
            m_VideoThread->Status(_STATUS_THREAD_PAUSE_);
        if (m_AudioThread)
            m_AudioThread->Status(_STATUS_THREAD_PAUSE_);
    }
}

void CHbxBaseTasksCheduler::Play() {
    if (m_ThrStatus == _STATUS_THREAD_PAUSE_) {
        if (m_VideoThread)
            m_VideoThread->Status(_STATUS_THREAD_RUN_);
        if (m_AudioThread)
            m_AudioThread->Status(_STATUS_THREAD_RUN_);
        m_ThrStatus = _STATUS_THREAD_RUN_;
    }
}


void CHbxBaseTasksCheduler::Stop() {
    ExitThread();
}

void CHbxBaseTasksCheduler::DispenseFrame(CHbxFrame *frame) {
    if (!frame) return;

    if (frame->m_nIndex == 0) {
        m_VideoThread->SetInputFrame(frame);
    } else if (frame->m_nIndex == 3) {
        m_VideoThread->SetInputFrame(frame);
    } else {
        m_AudioThread->SetInputFrame(frame);
    }
}

void CHbxBaseTasksCheduler::TasksCheduler() {
    CHbxFrame *hbxFrame = NULL;
    CHbxFrame *hbxAuFrame = NULL;
    //
    long long nCurrentPts = 0;//当前播放启动的相对时间
    long long nStartPlayPts = CHbxThread::Clock();//当前播放启动的起始时间
    long long nFistFrameDts = 0;//解码出来的起始时间
    long long nCurrentFrameDts = 0; //当前解码出来帧的时间

    long long nAFirstFrameDts = 0;//解码出来的起始时间
    long long nAStartPlayPts = CHbxThread::Clock();//当前播放启动的起始时间
    int nStatus = 0;
    HBXLOG("TasksCheduler:tid =%d \r\n", gettid());
    while (m_ThrStatus != _STATUS_THREAD_EXIT_) {
        //是否处理完成，如果处理完成就去取下一帧
        if (m_ThrStatus != _STATUS_THREAD_PAUSE_ && (nStatus == _NO_CACHE_)) {
            if (!hbxAuFrame)
                hbxAuFrame = m_ListFrame->AudioPop();

            if (!hbxFrame)
                hbxFrame = m_ListFrame->YuvPop();

            if (hbxAuFrame) {//音频处理
                if (hbxAuFrame->m_nID == 0)// first frame
                {
                    nAStartPlayPts = CHbxThread::Clock();
                    nAFirstFrameDts = hbxAuFrame->m_nPts;
                }
                //当前调节器的时间
                nCurrentPts = CHbxThread::Clock() - nAStartPlayPts;
                //
                nCurrentFrameDts = hbxAuFrame->m_nPts - nAFirstFrameDts;
                //
                if ((nCurrentPts + _DELAY_TIME_) >= nCurrentFrameDts) {
                    DispenseFrame(hbxAuFrame);

                    hbxAuFrame = NULL;
                }
                //调整pts 与 dts 时间差
                //显示与解码相差太大，容易出现就是在暂停情况下，线程继续运行就会出现，seek 后退的时候
                if (nCurrentPts >= (nCurrentFrameDts + _DELAY_DTS_TIME_)) {
                    nAStartPlayPts += nCurrentPts - nCurrentFrameDts;
                }
                    //显示与解码相差太大，seek 前进的时候容易出现
                else if (nCurrentPts <= (nCurrentFrameDts - _DELAY_DTS_TIME_)) {
                    nAStartPlayPts -= nCurrentFrameDts - nCurrentPts;
                }
            }
            if (hbxFrame)//视频处理
            {
                if (hbxFrame->m_nID == 0)// first frame
                {
                    nStartPlayPts = CHbxThread::Clock();
                    nFistFrameDts = hbxFrame->m_nPts;
                }
                //当前调节器的时间
                nCurrentPts = CHbxThread::Clock() - nStartPlayPts;
                //
                nCurrentFrameDts = hbxFrame->m_nPts - nFistFrameDts;
                //
                if ((nCurrentPts + _DELAY_TIME_) >= nCurrentFrameDts) {
                    //processs 数据
                    m_nCurrent = hbxFrame->m_nPts - m_MediaInfo->first_pts;
                    if (m_nCurrent < 0)
                        m_nCurrent = hbxFrame->m_nPts;

                    DispenseFrame(hbxFrame);
                    hbxFrame = NULL;
                }
                //调整pts 与 dts 时间差
                //显示与解码相差太大，容易出现就是在暂停情况下，线程继续运行就会出现，seek 后退的时候
                if (nCurrentPts >= (nCurrentFrameDts + _DELAY_DTS_TIME_)) {
                    nStartPlayPts += nCurrentPts - nCurrentFrameDts;
                }

                    //显示与解码相差太大，seek 前进的时候容易出现
                else if (nCurrentPts <= (nCurrentFrameDts - _DELAY_DTS_TIME_)) {
                    nStartPlayPts -= nCurrentFrameDts - nCurrentPts;
                }
                //丢帧
                // 1.0 数据帧出问题
                // 2.0 缓存帧过多，这个情况放在，处理线程里面自己处理
            } else {
                //1 net speed is low: http or rtsp
                if (m_ListFrame->NeedHttpCache()) {
                    //
                    nStatus = _NET_TYPE_CACHE_;
                    HBXLOG("net cache .................. \r\n");
                }
                    //2 ffmpeg is low
                else if (m_ListFrame->VideoSize() > 60 && m_ListFrame->VideoSize() < 120) {
                    //
                    m_ListFrame->SetMaxYuvCache(_MID_BUFFER_YUV_);
                    nStatus = _YUV_TYPE_CACHE_;
                    HBXLOG("yuv cache0   ................. \r\n");
                }

                    //2 ffmpeg is low
                else if (m_ListFrame->VideoSize() >= 120) {
                    //
                    m_ListFrame->SetMaxYuvCache(_MAX_BUFFER_YUV_);
                    nStatus = _YUV_TYPE_CACHE_;
                    HBXLOG("yuv cache1   ................. \r\n");
                }
            }
        } else {
            if (nStatus == 1) {
                if (!m_ListFrame->NeedHttpCache()) {
                    nStatus = _NO_CACHE_;
                    HBXLOG("net cache over.................. \r\n");
                }
            } else if (nStatus == 2) {
                if ((m_ListFrame->YuvSize() >= m_ListFrame->GetMaxYuvCache()) ||
                    (m_ListFrame->VideoSize() <= _LOW_BUFFER_YUV_)) {
                    nStatus = _NO_CACHE_;
                    HBXLOG("yuv cache   over.............%d.... \r\n", m_ListFrame->YuvSize());
                }
            }
            //
        }

        //事件处理
        msleep(_DELAY_TIME_);
#if 0
#ifdef _WIN_
        msleep(_DELAY_TIME_);
#else
        struct timeval now;
        struct timespec outtime;
        gettimeofday(&now, NULL);
        outtime.tv_sec = now.tv_sec;
        outtime.tv_nsec = now.tv_usec * 1000 + _DELAY_TIME_*1000 * 1000;
        int err = pthread_cond_timedwait(&m_Cond, &(m_ThreadMutex.m_Mutex),
                                         (const struct timespec *) &outtime);
        if (err < 0)
            HBXLOG("video err = %d ", err);
#endif
#endif
        //结束
    }
    if (hbxFrame) {
        delete hbxFrame;
    }
    if (hbxAuFrame) {
        delete hbxAuFrame;
    }
    m_ListFrame->SetMaxYuvCache(_LOW_BUFFER_YUV_);
    HBXLOG("CHbxBaseTasksCheduler exit \r\n");
}

void CHbxBaseTasksCheduler::TasksRealCheduler() {
    CHbxFrame *hbxFrame = NULL;
    CHbxFrame *hbxAuFrame = NULL;
    //
    while (m_ThrStatus != _STATUS_THREAD_EXIT_) {
        //是否处理完成，如果处理完成就去取下一帧
        if (m_ThrStatus != _STATUS_THREAD_PAUSE_) {
            if (!hbxAuFrame)
                hbxAuFrame = m_ListFrame->AudioPop();

            if (!hbxFrame)
                hbxFrame = m_ListFrame->YuvPop();

            if (hbxAuFrame) {//音频处理
                DispenseFrame(hbxAuFrame);
                hbxAuFrame = NULL;
            }
            if (hbxFrame)//视频处理
            {
                DispenseFrame(hbxFrame);
                hbxFrame = NULL;
            }
        }

        //事件处理
        msleep(_DELAY_TIME_);
        //结束
    }
    if (hbxFrame) {
        delete hbxFrame;
    }
    if (hbxAuFrame) {
        delete hbxAuFrame;
    }
    HBXLOG("CHbxBaseTasksCheduler exit \r\n");
}

void CHbxBaseTasksCheduler::Run() {
    m_ThreadMutex.Lock();
    if (m_ListFrame->GetFileType() == 2)
        TasksRealCheduler();
    else
        TasksCheduler();
    m_ThreadMutex.UnLock();
    HBXLOG("CHbxBaseTasksCheduler exit  00\r\n");
}
