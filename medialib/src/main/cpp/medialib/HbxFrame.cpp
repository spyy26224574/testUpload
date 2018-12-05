
#include "HbxFrame.h"


CHbxFrame::CHbxFrame()
{
	m_nPts = 0;
	m_nIndex = -1;
	m_avFrame = NULL;
	m_pkt = NULL;
	m_nID = -1;
	m_framelist.next = NULL;
}


CHbxFrame::~CHbxFrame()
{
	m_framelist.next = NULL;
	FreeFrame();
	FreePkt();
}

void CHbxFrame::FreeFrame()
{
	if (m_avFrame) {
		av_frame_free(&m_avFrame);
	}
	m_avFrame = NULL;
}

void CHbxFrame::FreePkt()
{
	if (m_pkt) {
		av_packet_unref(m_pkt);
		av_packet_free(&m_pkt);
		m_pkt = NULL;
	}
}
void CHbxFrame::FrameToYuv(unsigned char *pBuffer) {
	
	if (!m_avFrame || !pBuffer)
		return ;

	int len = m_avFrame->height * m_avFrame->width * 3 / 2;
	int height = m_avFrame->height;
	int width = m_avFrame->width;

	int a = 0, i;
	for (i = 0; i<height; i++)
	{
		memcpy(pBuffer + a, m_avFrame->data[0] + i * m_avFrame->linesize[0], width);
		a += width;
	}
	for (i = 0; i<height / 2; i++)
	{
		memcpy(pBuffer + a, m_avFrame->data[1] + i * m_avFrame->linesize[1], width / 2);
		a += width / 2;
	}
	for (i = 0; i<height / 2; i++)
	{
		memcpy(pBuffer + a, m_avFrame->data[2] + i * m_avFrame->linesize[2], width / 2);
		a += width / 2;
	}
}
