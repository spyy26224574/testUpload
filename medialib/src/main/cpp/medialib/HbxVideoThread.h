#pragma once
#include "HbxThread.h"
#include "HbxFrame.h"

#define  _CACHE_COUNT_ 60

typedef void(*VideoCallBack)(int, int, unsigned char *);

class CHbxVideoThread :
	public CHbxThread
{
public:
	CHbxVideoThread();
	~CHbxVideoThread();

protected:
	CHbxFrame *m_hbxFrame[_CACHE_COUNT_];
	int        m_nCount;
	int        m_nCacheAllowMax;

public:
	virtual void  CreatThread();
	virtual  void ExitThread();
	virtual  void Run();

	void          SetInputFrame(CHbxFrame *frame);
	void          SetAllowCacheMax(int max);
	CHbxFrame*    GetFrame();
	CHbxMutex     m_FrameMutex;
	void          Clean();

	void          PlayVideo();
public:
	static VideoCallBack m_vCallBack;
};

