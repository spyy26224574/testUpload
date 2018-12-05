#include "HbxReadThread.h"


CHbxReadThread::CHbxReadThread()
{
	m_pMediaFile = NULL;
	m_bHaveCmd = false;
}

CHbxReadThread::~CHbxReadThread()
{
	ExitThread();
}

void CHbxReadThread::CreatThread()
{
	m_nReadStatus = _STATUS_READ_PLAY_;
	CreatNewThread(NULL, (void *)this);
}

void CHbxReadThread::Run()
{
	m_ThreadMutex.Lock();
	HBXLOG("CHbxReadThread:tid =%d \r\n",gettid());
	OnReadFrame();
	m_ThreadMutex.UnLock();
}

void  CHbxReadThread::Seek(int second)
{
	if (m_nReadStatus != _STATUS_READ_STOP_) {
		m_nReadStatus = _STATUS_READ_PAUSE_;
		msleep(_DELAY_TIME_ * 2);
	}
	m_ListFrame->Clean();
	m_pMediaFile->Seek(second);
	m_nReadStatus = _STATUS_READ_PLAY_;
}


int CHbxReadThread::OnReadFrame()
{
	int nRet = 0;
	CHbxFrame* hbxFrame = NULL;
	if (!m_pMediaFile) return 0;
	bool               bStop = false;
	m_nReadStatus = _STATUS_READ_PLAY_;
	while (m_ThrStatus != _STATUS_THREAD_EXIT_) {
		//是否缓存满了
		if((m_nReadStatus == _STATUS_READ_PLAY_) && (m_ListFrame->VideoSize() < _MAX_BUFFER_FRAME_))
		{
			//缓存帧数据
			hbxFrame = new CHbxFrame();
			nRet = m_pMediaFile->ReadFrame(hbxFrame);

			if ((nRet == -1)) {
				delete hbxFrame;
				hbxFrame = NULL;
				msleep(_DELAY_TIME_);
				continue;
			}
			if (nRet == -2) {
				delete hbxFrame;
				hbxFrame = NULL;
				m_nReadStatus = _STATUS_READ_STOP_;
				//
				msleep(_DELAY_TIME_);
				continue;
			}
			//
			//视频
			if (hbxFrame) {
			    bStop = false;
			    m_ListFrame->Push(hbxFrame);
				hbxFrame = NULL;
				m_ListFrame->ReadThreadStatus(_STATUS_READ_PLAY_);
				continue;
			}
		}
		//
		if ((m_nReadStatus == _STATUS_READ_STOP_))
		{
		    m_ListFrame->ReadThreadStatus(_STATUS_READ_STOP_);
			if ((m_ListFrame->VideoSize() == 0) && (m_ListFrame->YuvSize() == 0) && !bStop)
			{
				CHbxInteractive api;
				api.UpdateMediaInfo("stop", 0);
				bStop = true;
			}
			msleep(_DELAY_DTS_TIME_);
//			HBXLOG("_STATUS_READ_STOP_ h264 .......\r\n");
		}
		msleep(_DELAY_TIME_);
	}
	m_pMediaFile->m_nStatus = _STATUS_READ_STOP_;
	HBXLOG("decode thread exit \r\n");
	return 0;
}
