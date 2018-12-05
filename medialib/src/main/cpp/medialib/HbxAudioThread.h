#pragma 
#ifndef _AUDIO_THREAD_H_
#define _AUDIO_THREAD_H_
#include "public.h"
#include "HbxThread.h"
#include "HbxFrame.h"

#define  _AUDIO_CACHE_COUNT_ 60

typedef void(*AudioCallBack)(AVFrame *);

class CHbxAudioThread :
	public CHbxThread
{
public:
	CHbxAudioThread();
	~CHbxAudioThread();

protected:
	CHbxFrame *m_hbxFrame[_AUDIO_CACHE_COUNT_];
	int        m_nCount;
	int        m_nCacheAllowMax;
public:
	virtual void  CreatThread();
	virtual void  ExitThread();

	void          SetInputFrame(CHbxFrame *frame);
	void          SetAllowCacheMax(int max);

	CHbxFrame*    GetFrame();
	CHbxMutex     m_FrameMutex;
	void          Clean();
	virtual  void Run();
	void          PlayAudio();
public:
	static AudioCallBack m_aCallBack;
};
#endif