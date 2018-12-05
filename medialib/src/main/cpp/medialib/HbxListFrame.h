#pragma once
#ifndef  _HBX_LIST_FRAME_H_
#define _HBX_LIST_FRAME_H_
#include "public.h"
#include "HbxFrame.h"
#include "HbxBaseFile.h"

class CHbxListFrame
{
public:
	CHbxListFrame();
	~CHbxListFrame();
protected:
	CHbxMutex          m_Mutex;
	struct list_head   m_Video;
	struct list_head   m_Audio;
	struct list_head   m_YuvList;

	int                m_MaxYuvCache;
	int                m_VideoListSize;
	int                m_YuvListSize;
	//
	int                m_nReadStatus;
	int                m_nCurFileType;

public:
	void              Push(CHbxFrame* hbxFrame);
	CHbxFrame*        YuvPop();
	CHbxFrame*        VideoPop();
	CHbxFrame*        AudioPop();
	int               VideoSize();
	int               YuvSize();
	void              Clean();
	//
	bool              NeedHttpCache();
	void              ReadThreadStatus(int status) { m_nReadStatus = status; };
	void              MedieType(int type) { m_nCurFileType = type; };
	//
	void              SetMaxYuvCache(int count);
	int               GetMaxYuvCache();
	int               GetFileType(){return m_nCurFileType;};
public:
	static void       ResetID(int id);
};

#endif // ! _HBX_LIST_FRAME_H_

