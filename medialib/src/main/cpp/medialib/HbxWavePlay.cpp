

#include "HbxWavePlay.h"
#ifdef _WIN_
CHbxWavePlay::CHbxWavePlay(void)
{
	m_bConvertFormat = FALSE;
	m_hWaveOut = NULL;
	m_nBufferNumber = 0;
	m_bThread = FALSE;
	m_ThreadID = 0;
	m_hThread = NULL;
	m_nVol = 1.0;
	m_bMute = FALSE;
}

CHbxWavePlay::~CHbxWavePlay(void)
{
	Close();
}
void CHbxWavePlay::Start(CHbxBaseFile* mediafile) { 
	CHbxAudioPlay::Start(mediafile);
	if (!m_pMediaFile)
		return;
	Init(m_pMediaFile->AudioContext()->sample_rate, m_pMediaFile->AudioContext()->sample_fmt, m_pMediaFile->AudioContext()->channels);
}

void CHbxWavePlay::Stop()
{
	Close();
}

int CHbxWavePlay::AddFrame(AVFrame *pFrame)
{
	if (!m_pMediaFile)
		return 0;
	return AddFrame(m_pMediaFile->AudioContext(), pFrame);
}

int CHbxWavePlay::Init(DWORD nSampleRate, enum AVSampleFormat nSampleFormat, WORD nChannels)
{
	m_wfx = { 0 };              /* look this up in your documentation */
	MMRESULT result = MMSYSERR_NOERROR;  /* for waveOut return values */

	if (AV_SAMPLE_FMT_NB < nSampleFormat || 0 > nSampleFormat)
		return -1;
	
	m_bConvertFormat = TRUE;//需要转换格式

	m_wfx.nSamplesPerSec = nSampleRate ; /* 采样率 */
	m_wfx.wBitsPerSample = 16;
	//采样大小，ffmpeg叫采样格式,enum AVSampleFormat常量,因为windows waveout api只支持8或16
	m_wfx.nChannels = nChannels; /*通道数*/
	m_wfx.cbSize = 0; /* size of _extra_ info */
	m_wfx.wFormatTag = WAVE_FORMAT_PCM;
	m_wfx.nBlockAlign = (m_wfx.wBitsPerSample* m_wfx.nChannels) >> 3;
	m_wfx.nAvgBytesPerSec = m_wfx.nBlockAlign * m_wfx.nSamplesPerSec;

	m_bStop = FALSE;
	StartThread();

	//检查格式是否被支持
	result = waveOutOpen(&m_hWaveOut, WAVE_MAPPER, &m_wfx,
		(DWORD_PTR)m_ThreadID, (DWORD_PTR)this, CALLBACK_THREAD);
		
	if (WAVERR_BADFORMAT == result)
	{
		//设置默认格式
		m_wfx.nSamplesPerSec = _SAMPLE_RATE_; /* 采样率 */
		m_wfx.wBitsPerSample = 16;//采样大小，ffmpeg叫采样格式,enum AVSampleFormat常量,因为windows waveout api只支持8或16
		m_wfx.nChannels = _CHANNAL_NUM_; /*通道数*/
		m_wfx.cbSize = 0; /* size of _extra_ info */
		m_wfx.wFormatTag = WAVE_FORMAT_PCM;
		m_wfx.nBlockAlign = (m_wfx.wBitsPerSample* m_wfx.nChannels) >> 3;
		m_wfx.nAvgBytesPerSec = m_wfx.nBlockAlign * m_wfx.nSamplesPerSec;
		//设置线程处理响应事件
		result = waveOutOpen(&m_hWaveOut, WAVE_MAPPER, &m_wfx,
			(DWORD_PTR)m_ThreadID, (DWORD_PTR)this, CALLBACK_THREAD);
	}

	if (result != MMSYSERR_NOERROR)
		return result;

	return result;
}

int CHbxWavePlay::CloseWaveOut()
{
	if (m_hWaveOut)
	{
		waveOutClose(m_hWaveOut);
		m_hWaveOut = NULL;
	}
	return 0;
}


int CHbxWavePlay::Close()
{
	m_bStop = TRUE;
	StopThread();
	CloseWaveOut();
	return 0;
}

void RaiseVolume(char* buf, int size, UINT32 uRepeat, double vol)
{
	if (!size)
	{
		return;
	}
	for (int i = 0; i < size; i += 2)
	{
		short wData;
		wData = buf[i + 1] & 0xff;
		wData <<= 8;
		wData += buf[i]&0xff;
		long dwData = wData;
		for (int j = 0; j < uRepeat; j++)
		{
			dwData = dwData * vol;
			if (dwData < -0x8000)
			{
				dwData = -0x8000;
			}
			else if (dwData > 0x7FFF)
			{
				dwData = 0x7FFF;
			}
		}

		wData = dwData & 0xfffff;
		buf[i] = wData & 0xff;
		buf[i + 1] = (wData & 0xff00) >> 8;
	}
}

int CHbxWavePlay::AddFrame(AVCodecContext *audio_dec_ctx,AVFrame *pFrame)
{
	int nRet = 0;
	if (NULL == pFrame || m_bMute || m_bStop)
		return 0;

	//检查是否超出缓存
	if (m_nBufferNumber >= 5)
		return -100;

	int nLen = pFrame->linesize[0] * pFrame->channels << 1;
	unsigned char *pBuf = new unsigned char[nLen];
	memset(pBuf,0,nLen);
	if (m_bConvertFormat)
	{
		//转换音频格式
		AVFrameToAudio(audio_dec_ctx,pFrame,pBuf, nLen);
	//	RaiseVolume(pBuf,nLen,1, m_nVol);
	}
	else
	{
		nLen = pFrame->linesize[0] * pFrame->channels;
		memcpy(pBuf, pFrame->data, nLen);
	}

	writeAudioBlock(m_hWaveOut, pBuf, nLen);
	InterlockedIncrement((volatile LONG*)&m_nBufferNumber);
	return nRet;
}
void CHbxWavePlay::writeAudioBlock(HWAVEOUT hWaveOut,unsigned char* block, DWORD size)
{
	WAVEHDR* pHeader = new WAVEHDR;
	MMRESULT result;

	//用音频数据初始化 WAVEHDR 结构
	memset(pHeader, 0, sizeof(WAVEHDR));
	pHeader->dwBufferLength = size;
	pHeader->lpData = (LPSTR)block;
	

	//为播放准备数据块
	result = waveOutPrepareHeader(hWaveOut, pHeader, sizeof(WAVEHDR));
	if (result != MMSYSERR_NOERROR)
	{
		TRACE("waveOutPrepareHeader fail:%d\n", result);
		return;
	}
	//写入数据块到设备.一般情况下waveOutWrite立即返回。除了设备使用同步的情况。
	result = waveOutWrite(hWaveOut, pHeader, sizeof(WAVEHDR));
	if (result != MMSYSERR_NOERROR)
	{	
		TRACE("waveOutPrepareHeader fail:%d\n", result);
		return;
	}
}

void CALLBACK CHbxWavePlay::CallBackwaveOut(
	HWAVEOUT hwo,
	UINT uMsg,
	DWORD_PTR dwInstance,
	DWORD_PTR dwParam1,
	DWORD_PTR dwParam2
)
{
	int nRet = 0;

	switch (uMsg)
	{
	case WOM_DONE:
	case WOM_CLOSE:
	{
		WAVEHDR* pWaveHead = (WAVEHDR*)dwParam2;
		//ASSERT(pWaveHead);
		if (NULL == pWaveHead)
		{
			TRACE(_T("(WAVEHDR*)dwParam2 is null\n"));
			return;
		}
		CHbxWavePlay* pThis = (CHbxWavePlay*)dwInstance;
		pThis->OnwaveOutProc(hwo, uMsg, pWaveHead);
		break;
	}
	default:
		break;
	};

	return;
}

int CHbxWavePlay::StartThread()
{
	if (m_bThread)
		return 0;

	m_hThread = CreateThread(0, 0, ThreadProc, this, 0, &m_ThreadID);

	if (!m_hThread)
		return -1;

	m_bThread = TRUE;
	return 0;
}

void CHbxWavePlay::StopThread()
{
	if (!m_bThread)
	{
		return;
	}

	if (m_hThread)
	{
		int t = 500;
		DWORD ExitCode;
		BOOL bEnd = FALSE;
		while (m_nBufferNumber)
			Sleep(10);

		PostThreadMessage(m_ThreadID, WM_QUIT, 0, 0);
		while (t)
		{
			GetExitCodeThread(m_hThread, &ExitCode);
			if (ExitCode != STILL_ACTIVE)
			{
				bEnd = TRUE;
				break;
			}
			else
				Sleep(10);
			t--;
		}
		if (!bEnd)
		{
			TerminateThread(m_hThread, 0);
		}

		m_hThread = 0;
	}
	m_bThread = FALSE;
}

DWORD CALLBACK CHbxWavePlay::ThreadProc(LPVOID lpParameter)
{
	CHbxWavePlay *pThis;
	pThis = (CHbxWavePlay *)lpParameter;

	MSG msg;
	while (GetMessage(&msg, 0, 0, 0))
	{
		switch (msg.message)
		{
		case WOM_OPEN:
			break;
		case WOM_CLOSE:
		case WOM_DONE:
			WAVEHDR* pWaveHead = (WAVEHDR*)msg.lParam;
            pThis->OnwaveOutProc(pThis->m_hWaveOut, msg.message, pWaveHead);
			break;
		}
	}
	TRACE(_T("thread[%d] exit\n"), GetCurrentThreadId());
	return msg.wParam;
}

void CHbxWavePlay::OnwaveOutProc(
	HWAVEOUT hwo,
	UINT uMsg,
	WAVEHDR * pHdr
)
{
	switch (uMsg)
	{
	case WOM_DONE:
	case WOM_CLOSE:
	{
		//释放内存资源
		waveOutUnprepareHeader(hwo, pHdr, sizeof(WAVEHDR));
		if (pHdr) {
			if (pHdr->lpData)
				delete[] pHdr->lpData;
			pHdr->lpData = NULL;
			if (pHdr)
				delete pHdr;
		}
		//减小内存计数
		InterlockedDecrement((volatile LONG*)&m_nBufferNumber);
		break;
	}
	default:
		break;
	}
}
#endif 