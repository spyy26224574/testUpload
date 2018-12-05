#pragma once
#include "public.h"

#ifndef _HBX_FRAME_H_
#define _HBX_FRAME_H_
class CHbxFrame
{
public:
	struct list_head m_framelist;

public:
	CHbxFrame();
	~CHbxFrame();

	void              FrameToYuv(unsigned char *pBuffer);
public:
	AVFrame           *m_avFrame;
	AVPacket          *m_pkt;

	unsigned int      m_nPts;
	int               m_nIndex;
	void              FreeFrame();
	void              FreePkt();
	int               m_nID;
};

#endif