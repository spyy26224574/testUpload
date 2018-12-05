
#include "public.h"
#include <list>
#include "HbxAudioPlay.h"

#ifdef _WIN_
#include <Mmsystem.h>
#include <afxmt.h>

#ifndef _HBX_WAVE_PLAY_H_
#define _HBX_WAVE_PLAY_H_

#define _SAMPLE_RATE_  48000
#define _CHANNAL_NUM_  2
#pragma comment(lib, "winmm.lib")


class CHbxWavePlay : public CHbxAudioPlay
{
public:
	CHbxWavePlay(void);
	~CHbxWavePlay(void);
protected:
	int Init(DWORD nSampleRate, enum AVSampleFormat nSampleFormat, WORD nChannels);
	int Close();
	int AddFrame(AVCodecContext *audio_dec_ctx, AVFrame *pFrame);
public:
	virtual void Start(CHbxBaseFile* mediafile);
	virtual void Stop();
	virtual int  AddFrame(AVFrame *pFrame);

private:
	HWAVEOUT m_hWaveOut; /* device handle */
	WAVEFORMATEX m_wfx;
	BOOL m_bConvertFormat;
	BOOL m_bStop;

	unsigned long m_nBufferNumber;


	static  void CALLBACK CallBackwaveOut(
		HWAVEOUT hwo,
		UINT uMsg,
		DWORD_PTR dwInstance,
		DWORD_PTR dwParam1,
		DWORD_PTR dwParam2
	);
	int StartThread();
	void StopThread();
	BOOL m_bThread;
	DWORD m_ThreadID;
	HANDLE m_hThread;


	static DWORD CALLBACK CHbxWavePlay::ThreadProc(LPVOID lpParameter);

	void OnwaveOutProc(
		HWAVEOUT hwo,
		UINT uMsg,
		WAVEHDR * pHdr
	);
	void writeAudioBlock(HWAVEOUT hWaveOut,unsigned char* block, DWORD size);
	int CloseWaveOut();
};
#endif
#endif 