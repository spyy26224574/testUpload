
#include "HbxVideoDecodec.h"

CHbxVideoDecodec::CHbxVideoDecodec()
{
	m_ListFrame = NULL;
	m_bSoftDecodec = 1;
}

CHbxVideoDecodec::~CHbxVideoDecodec()
{
}

void CHbxVideoDecodec::CreatThread()
{
	CreatNewThread(NULL, (void *)this);
}

void CHbxVideoDecodec::Run()
{
	m_ThreadMutex.Lock();
	OnVideoDecodec();
	m_ThreadMutex.UnLock();
}

CHbxFrame* CHbxVideoDecodec::GetFrame()
{ 
	CHbxFrame* frame = NULL;
	frame = m_ListFrame->VideoPop();
	return frame;
}

int CHbxVideoDecodec::OnVideoDecodec()
{
	CHbxFrame *frame = NULL;
	HBXLOG("CHbxVideoDecodec:tid =%d \r\n",gettid());
	while(m_ThrStatus != _STATUS_THREAD_EXIT_)
	{
		frame = GetFrame();
		if (m_pMediaFile && frame) {
			//
			if (frame->m_nIndex == _VIDEO_INDEX_STREAM_) {
				//
				if(m_bSoftDecodec)
					m_pMediaFile->SoftVideoDecodec(frame);
				//
				frame->m_nIndex = _YUV_INDEX_STREAM_;
			}
			//
			if(frame->m_nIndex == _YUV_INDEX_STREAM_)
			m_ListFrame->Push(frame);
		}
		else
			msleep(50);
	}
    HBXLOG("CHbxVideoDecodec:tid =%d exit \r\n",gettid());
	return 0;
}