#include "HbxAndroidAudioPlay.h"
#ifdef _ANDROID_

CHbxAndroidAudioPlay::CHbxAndroidAudioPlay()
{
    m_pAudioBuf = NULL;
    m_nBufferLength = 0;
}

CHbxAndroidAudioPlay::~CHbxAndroidAudioPlay()
{
    if(m_pAudioBuf)
        delete m_pAudioBuf;
}

void CHbxAndroidAudioPlay::Start(CHbxBaseFile* mediafile)
{
	CHbxAudioPlay::Start(mediafile);
	m_nBufferNumber = 0;
	m_bConvertFormat = true;//需要转换格式
}

void CHbxAndroidAudioPlay::Stop()
{
}

int CHbxAndroidAudioPlay::AddFrame(AVFrame *pFrame)
{
	int nRet = 0;
    //HBXLOG("CHbxAudioPlay::pFrame %x m_bMute %x m_bStop =%d \r\n",pFrame,m_bMute,m_bStop);
	if (NULL == pFrame)
		return 0;

	//检查是否超出缓存
	int nLen = pFrame->linesize[0] * pFrame->channels << 1;
    try
	{
		if (!m_pAudioBuf) {
			m_pAudioBuf = new unsigned char[nLen + 1024];
			m_nBufferLength = nLen + 1024;
		}
		//
		if (nLen > m_nBufferLength) {
			delete m_pAudioBuf;
			m_pAudioBuf = new unsigned char[nLen + 1024];
			m_nBufferLength = nLen + 1024;
			HBXLOG("change mem \r\n");
		}
		memset(m_pAudioBuf, 0, m_nBufferLength);
		if (m_bConvertFormat) {
			//转换音频格式
			AVFrameToAudio(m_pMediaFile->AudioContext(), pFrame, m_pAudioBuf, nLen);
		} else {
			nLen = pFrame->linesize[0] * pFrame->channels;
			memcpy(m_pAudioBuf, pFrame->data, nLen);
		}
		if (CHbxAudioPlay::m_aCallBack)
			CHbxAudioPlay::m_aCallBack(nLen, m_pAudioBuf);
	}
    catch(exception &e){
		CHbxInteractive api;
		api.UpdateMediaInfo("error:play audio\r\n",_INFO_ERROR_TAG_);
		HBXLOG("decode and play audio erro \r\n");
	}
	return nRet;
}

#endif //
