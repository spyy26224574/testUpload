

#include "FFmpeg.h"

CHbxMutex  g_cs;
CHbxMutex  g_vidoeFramecs;

#ifdef _ANDROID_
void *ThreadRead(void *lpParameter) {
#endif
#ifdef _WIN_
DWORD WINAPI ThreadRead(void *lpParameter) {
#endif
    CFFmpeg* pThis = (CFFmpeg*)lpParameter;
    pThis->OnReadFrame();
    return NULL;
}


CFFmpeg::CFFmpeg()
{
	m_nStatus = _STATUS_STOP_;
	m_nThreadExit = false;
	m_bHaveCmd = false;

	m_pMediaFile = NULL;
    m_pMediaFile = new CHbxBaseFile();
}


CFFmpeg::~CFFmpeg()
{
	DeleteBitmap();
	g_cs.Lock();
	g_cs.UnLock();
	m_pMediaFile = NULL;
    if(m_pMediaFile)
        delete m_pMediaFile;
    m_pMediaFile = NULL;

}
int CFFmpeg::Open(char * path)
{
	int nRet = 0;
	//init
 //  if(!m_pMediaFile)
 //      m_pMediaFile = new CHbxBaseFile();
	DeleteBitmap();
	m_nThreadExit = false;

	//文件类型
	if (!m_pMediaFile)
		return -1;
    nRet = m_pMediaFile->Open(path);
    if (nRet)
		return -1;
    //开始播放
    m_nStatus = _STATUS_PLAY_;
    CHbxThread thread;
    thread.CreatThread(ThreadRead,(void*)this);
	return 0;
}

int CFFmpeg::OnReadFrame()
{
	int nRet = 0;
	if (!m_pMediaFile)
		return 0;

	g_cs.Lock();

	while (m_nStatus != _STATUS_STOP_) {
		CHbxFrame* hbxFrame = NULL;
		nRet = m_pMediaFile->ReadFrame(&hbxFrame);
		if (nRet == -1) {
            continue;
        }
        if (nRet == -2) {
            break;
        }
		//视频
		if (hbxFrame) {
			g_vidoeFramecs.Lock();
			m_Bitmap.push_back(hbxFrame);
			g_vidoeFramecs.UnLock();
        }

		m_bWaitting = true;
		while (!m_nThreadExit)
		{
			//处理消息
			if (m_bHaveCmd)
			{
				msleep(5);
				continue;
			}
			//暂停播放，解码
			if (m_nStatus == _STATUS_PAUSE_)
			{
				msleep(20);
				continue;
			}
			//设置一个缓存
			if (m_Bitmap.size() > _COUNT_BUFFER_FRAME_)
			{
				msleep(10);
			}
			else
				break;
		}
		m_bWaitting = false;
	}
	m_nStatus = _STATUS_STOP_;

	g_cs.UnLock();
    LOGE("decode thread exit \r\n");
}


int  CFFmpeg::Pause()
{
	if (m_pMediaFile)
	{
		m_bHaveCmd = true;
		while (!m_bWaitting)
			msleep(10);
		m_nStatus = _STATUS_PAUSE_;
		m_bHaveCmd = false;
	}
	return 0;
}

int CFFmpeg::Play()
{
	if (m_pMediaFile)
	{
		m_bHaveCmd = true;
		while (!m_bWaitting)
			msleep(10);
		m_nStatus = _STATUS_PLAY_;
		m_bHaveCmd = false;
	}
	return 0;
}

int CFFmpeg::Seek(int seek)
{
	int nRet = 0;
	if (m_pMediaFile)
	{
		m_bHaveCmd = true;

		while (!m_bWaitting)
			msleep(1);

		if (m_bWaitting == true)
		{
			nRet = m_pMediaFile->Seek(seek);
			msleep(100);
		}
		m_bHaveCmd = false;
	}
	return 0;
}

int CFFmpeg::Stop()
{
	m_nStatus = _STATUS_STOP_;
	m_nThreadExit = true;

	g_cs.Lock();
	g_cs.UnLock();

	DeleteBitmap();
	//
	if(m_pMediaFile)
		m_pMediaFile->Close();
	return 0;
}


void CFFmpeg::GetWidthAndHigh(int & width, int &high)
{
	if (m_pMediaFile) {
		m_pMediaFile->GetWidthAndHigh(width, high);
	}
}
CHbxFrame* CFFmpeg::GetRawBitmap()
{
	CHbxFrame* pPic = NULL;
	std::list<CHbxFrame*>::iterator it;

	g_vidoeFramecs.Lock();
	for(it = m_Bitmap.begin();it != m_Bitmap.end();it++)
	{
		pPic = *(it);
		if (pPic->m_pPic == NULL)
			break;
		else
			pPic = NULL;
	}
	g_vidoeFramecs.UnLock();

	return pPic;
}

CHbxFrame* CFFmpeg::GetBitmap()
{
	CHbxFrame* pPic = NULL;
	std::list<CHbxFrame*>::iterator it;
	g_vidoeFramecs.Lock();
	it = m_Bitmap.begin();
	if (m_Bitmap.end() != it)
	{
		pPic = *(it);
		m_Bitmap.pop_front();
	}
	g_vidoeFramecs.UnLock();
	return pPic;
}


void  CFFmpeg::PopBitmap()
{
	g_vidoeFramecs.Lock();
	m_Bitmap.pop_front();
	g_vidoeFramecs.UnLock();
}

int CFFmpeg::Status()
{
	return m_nStatus;
}

void CFFmpeg::DeleteBitmap()
{
	g_vidoeFramecs.Lock();
	std::list<CHbxFrame*>::iterator it;
	for (it = m_Bitmap.begin(); it != m_Bitmap.end(); it++)
		delete *it;
	m_Bitmap.clear();
	g_vidoeFramecs.UnLock();
}



int CFFmpeg::Duration()
{
	if (m_pMediaFile)
	return m_pMediaFile->Duration();
}

int CFFmpeg::Current()
{
	if (m_pMediaFile)
		return m_pMediaFile->Current();
}


int   CFFmpeg::GetSpsPps(unsigned char  ** sps) {

    unsigned char  * spsinfo = NULL;
    if (m_pMediaFile) {
        spsinfo = new unsigned char[m_pMediaFile->m_nPpsLength+m_pMediaFile->m_nSpsLength];

        memcpy(spsinfo,m_pMediaFile->m_pSps,m_pMediaFile->m_nSpsLength);
        *sps = spsinfo;
        memcpy(&spsinfo[m_pMediaFile->m_nSpsLength],m_pMediaFile->m_pPps,m_pMediaFile->m_nPpsLength);
        return m_pMediaFile->m_nPpsLength +m_pMediaFile->m_nSpsLength;
    }
    return 0;
}
void CFFmpeg::SetDecodeType(int type)
{
    if (m_pMediaFile) {
        m_pMediaFile->SetDecodeType(type);
    }
}