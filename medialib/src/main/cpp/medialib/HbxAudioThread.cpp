
#include "HbxAudioThread.h"
#define _DELAY_TIME_   5
#define _DELAY_DTS_TIME_   500

AudioCallBack CHbxAudioThread::m_aCallBack = NULL;

CHbxAudioThread::CHbxAudioThread(){
	for (int i = 0; i < _AUDIO_CACHE_COUNT_; i++)
		m_hbxFrame[i] = NULL;
	m_nCount = 0;

	m_nCacheAllowMax = _AUDIO_CACHE_COUNT_ / 2 ;
}

CHbxAudioThread::~CHbxAudioThread()
{
	ExitThread();
}

void   CHbxAudioThread::SetInputFrame(CHbxFrame *frame)
{
	m_FrameMutex.Lock();
	if (m_nCount < _AUDIO_CACHE_COUNT_) {
		m_hbxFrame[m_nCount] = frame;
		m_nCount++;
		#ifndef _WIN_
		pthread_cond_signal(&m_Cond);
		#endif
	}
	//cache is full
	else {
		if(frame)
			delete frame;
	}
	m_FrameMutex.UnLock();
}


CHbxFrame*   CHbxAudioThread::GetFrame()
{
	CHbxFrame* pFrame = NULL;
	int i = 0;
	if (m_nCount <= 0)
		return pFrame;

	m_FrameMutex.Lock();
	pFrame = m_hbxFrame[0];
	do {
		m_hbxFrame[i] = m_hbxFrame[i + 1];
		i++;
	} while (i < (_AUDIO_CACHE_COUNT_ -1));
	m_nCount--;
	m_hbxFrame[m_nCount] = NULL;
	m_FrameMutex.UnLock();
	return pFrame;
}

void  CHbxAudioThread::SetAllowCacheMax(int max)
{
	m_nCacheAllowMax = max;
}

void CHbxAudioThread::ExitThread()
{
#ifndef _WIN_
    pthread_cond_signal(&m_Cond);
#endif	
	CHbxThread::ExitThread();
	Clean();
}

void   CHbxAudioThread::Clean()
{
	m_FrameMutex.Lock();
	for (int i = 0; i < _AUDIO_CACHE_COUNT_; i++) {
		if (m_hbxFrame[i]) {
			delete m_hbxFrame[i];
		}
		m_hbxFrame[i] = NULL;
	}
	m_FrameMutex.UnLock();
}

void CHbxAudioThread::CreatThread()
{
	CreatNewThread(NULL, (void *)this);
}

void  CHbxAudioThread::PlayAudio()
{
	CHbxFrame*         hbxFrame = NULL;
	m_ThrStatus     = _STATUS_THREAD_RUN_;
    HBXLOG("CHbxAudioThread:tid =%d \r\n",gettid());
	while(m_ThrStatus != _STATUS_THREAD_EXIT_)
	{
		if(m_ThrStatus != _STATUS_THREAD_PAUSE_) {
			if (!hbxFrame)
				hbxFrame = GetFrame();

			if (hbxFrame) {
				if (m_aCallBack && hbxFrame->m_avFrame) {
					try {
						m_aCallBack(hbxFrame->m_avFrame);
					}
					catch (exception &e) {
						CHbxInteractive api;
						api.UpdateMediaInfo("error:play audio\r\n", _INFO_ERROR_TAG_);
						HBXLOG("error:play audio\r\n");
					}
				}
				delete hbxFrame;
				hbxFrame = NULL;
			}
			else{
			#ifdef _WIN_
			msleep(_DELAY_TIME_/2);
			#else
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
		else
			msleep(_DELAY_DTS_TIME_ / 2 );
	}
}

void CHbxAudioThread::Run()
{
	m_ThreadMutex.Lock();
	PlayAudio();
    HBXLOG("CHbxAudioThread:tid =%d exit \r\n",gettid());
	m_ThreadMutex.UnLock();
}
