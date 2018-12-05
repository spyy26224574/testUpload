#pragma once
#include "public.h"
#include "HbxBaseFile.h"
#ifndef _HBX_AUDIO_PLAY_
#define _HBX_AUDIO_PLAY_

typedef void(*ACALLBACK)(int , unsigned char *);

class CHbxAudioPlay
{
public:
	CHbxAudioPlay();
	~CHbxAudioPlay();
protected:
	CHbxBaseFile *m_pMediaFile;
	double  m_nVol;
	bool    m_bMute;

	int     m_nOutChannels;
	int     m_nOutSamplesPerSec;

	void    RaiseVolume(char* buf, int size, uint32_t uRepeat, double vol);
	int     AVFrameToAudio(AVCodecContext *audio_dec_ctx, AVFrame* pAudioDecodeFrame, unsigned char *buffer, int &len);

public:
	virtual void Start(CHbxBaseFile* mediafile);
	virtual void Stop()=0;
	void SetVolume(double vol) { m_nVol = vol / 16.0; };
	void SetMute() { m_bMute = !m_bMute; };
	virtual int AddFrame(AVFrame *pFrame)=0;
public:
    static ACALLBACK m_aCallBack;
};
#endif 
