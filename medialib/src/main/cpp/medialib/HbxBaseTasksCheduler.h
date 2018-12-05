#pragma once
#ifndef  _HBX_BASETASK_H_
#define  _HBX_BASETASK_H_

#include "HbxThread.h"
#include "HbxVideoThread.h"
#include "HbxListFrame.h"
#include "HbxAudioThread.h"

typedef void(*TaskCallBack)(CHbxFrame *);

class CHbxBaseTasksCheduler : public CHbxThread
{
public:
	CHbxBaseTasksCheduler();
	~CHbxBaseTasksCheduler();
public:
	int                m_nCurrent;

public:
	CHbxAudioThread   *m_AudioThread;
	CHbxVideoThread   *m_VideoThread;
	CHbxListFrame     *m_ListFrame;
	//
	int               Current() {return m_nCurrent;};
	void              DispenseFrame(CHbxFrame *frame);
	void              Pause();
	void              Play();
	void              Stop();
	void              TasksCheduler();
    void              TasksRealCheduler();
public: 
	virtual  void CreatThread();
	virtual  void ExitThread();
	virtual  void Run();
};
#endif

