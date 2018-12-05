
#include "HbxAudioPlay.h"
#ifndef _HBX_ANDROID_AUDIO_PLAY_H
#define _HBX_ANDROID_AUDIO_PLAY_H
#ifdef _ANDROID_
class CHbxAndroidAudioPlay :
	public CHbxAudioPlay
{
public:
	CHbxAndroidAudioPlay();
	~CHbxAndroidAudioPlay();

protected:
	bool m_bConvertFormat;
    unsigned  char *m_pAudioBuf;
    int              m_nBufferLength;
	unsigned long long m_nBufferNumber;
public:
	virtual void Start(CHbxBaseFile* mediafile) ;
	virtual void Stop() ;
	virtual int AddFrame(AVFrame *pFrame);
};
#endif
#endif
