//
// Created by admin on 2016/11/5.
//

#ifndef _HBX_DECODEC_H_
#define _HBX_DECODEC_H_

#include "public.h"
#include "HbxFrame.h"
#include "HbxBaseFile.h"
#include "HbxThread.h"
#include "HbxListFrame.h"


class CHbxReadThread : public CHbxThread
{
public:
	CHbxReadThread();
	~CHbxReadThread();
protected:
	bool          m_bHaveCmd;
	bool          m_bWaiting;

public:
	CHbxListFrame *m_ListFrame;
protected:
	int            m_nReadStatus;
public:
	virtual  void  CreatThread();
	virtual  void  Run();
	CHbxBaseFile  *m_pMediaFile;
	CHbxMutex      m_FrameMutex;
	void           Seek(int second);
	int            OnReadFrame();
	int            GetDecodecStatus() { return m_nReadStatus; };
};

#endif //FFMPEGPLAYER_FFMPEGPLAYER_H
