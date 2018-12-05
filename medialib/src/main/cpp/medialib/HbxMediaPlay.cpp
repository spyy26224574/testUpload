
#include "HbxMediaPlay.h"


#ifdef _WIN_
#include "HbxWavePlay.h"
#endif

#ifdef _ANDROID_

#include "HbxAndroidAudioPlay.h"

#endif
//audio
#ifdef _IPHONE_
#include "HbxIosAudioPlay.h"
#endif


//audio
CHbxAudioPlay *m_pAudio = NULL;

static void ACallBack(AVFrame *avframe) {
    if (m_pAudio) {
        m_pAudio->AddFrame(avframe);
    }
}

CHbxMediaPlay::CHbxMediaPlay() {
    m_pMediaFile = new CHbxBaseFile();
    m_pTasksCheduler = new CHbxBaseTasksCheduler();
    m_pReadThread = new CHbxReadThread();
    m_pHbxListFrame = new CHbxListFrame();
    m_pVideoDecodec = new CHbxVideoDecodec();

    m_nMediaStatus = _STATUS_MEDIA_STOP_;
    m_pTasksCheduler->m_MediaInfo = &m_MediaInfo;
    m_pReadThread->m_MediaInfo = &m_MediaInfo;

#ifdef _WIN_
    m_pAudio = new CHbxWavePlay();
#endif // 

#ifdef _ANDROID_
    m_pAudio = new CHbxAndroidAudioPlay();
#endif

#ifdef _IPHONE_
    m_pAudio = new CHbxIosAudioPlay();
#endif
    m_pReadThread->m_ListFrame = m_pHbxListFrame;
    m_pTasksCheduler->m_ListFrame = m_pHbxListFrame;
    m_pVideoDecodec->m_ListFrame = m_pHbxListFrame;
    CHbxAudioThread::m_aCallBack = ACallBack;
}


CHbxMediaPlay::~CHbxMediaPlay() {
    HBXLOG("~CHbxMediaPlay");
    Stop();
    DELETE_BUFFER(m_pAudio);

    if (m_pTasksCheduler) {
        m_pTasksCheduler->ExitThread();
        DELETE_BUFFER(m_pTasksCheduler);
    }

    if (m_pReadThread) {
        m_pReadThread->ExitThread();
        DELETE_BUFFER(m_pReadThread);
    }

    if (m_pVideoDecodec) {
        m_pVideoDecodec->ExitThread();
        DELETE_BUFFER(m_pVideoDecodec);
    }

    DELETE_BUFFER(m_pHbxListFrame);
    DELETE_BUFFER(m_pMediaFile);
}

int CHbxMediaPlay::GetFileFromByName(char *path) {
    if (strstr(path, "http") && (path[0] == 'h') && (path[1] == 't') && (path[2] == 't') &&
        (path[3] == 'p')) {
        return _HTTP_TYPE_MEDIA_FROM_;
    }
    if (strstr(path, "rtsp") && (path[0] == 'r') && (path[1] == 't') && (path[2] == 's') &&
        (path[3] == 'p')) {
        return _RTSP_TYPE_MEDIA_FROM_;
    }
    return _LOCAL_TYPE_MEDIA_FROM_;
}

int CHbxMediaPlay::Open(char *path, int type) {
    int nRet = 0;
    //文件类型
    if (!m_pMediaFile)
        return -1;

    if (m_nMediaStatus != _STATUS_MEDIA_STOP_)
        return 0;

    m_pHbxListFrame->Clean();
    //
    m_pHbxListFrame->MedieType(type);
    //
    nRet = m_pMediaFile->Open(path, &m_MediaInfo);
    if (nRet)
        return -1;
    //开始播放
    CHbxListFrame::ResetID(0);
    m_pReadThread->m_pMediaFile = m_pMediaFile;
    if (m_pAudio)
        m_pAudio->Start(m_pMediaFile);
    //启动解码线程
    m_pVideoDecodec->m_pMediaFile = m_pMediaFile;
    //

    m_pVideoDecodec->CreatThread();
    m_pTasksCheduler->CreatThread();
    m_pReadThread->CreatThread();
    m_nMediaStatus = _STATUS_MEDIA_PLAY_;
   
    return 0;
}

int CHbxMediaPlay::OpenFile(const char *path, int type) {
    int nRet = 0;
    //文件类型
    if (!m_pMediaFile)
        return -1;

    if (m_nMediaStatus != _STATUS_MEDIA_STOP_)
        return 0;

    m_pHbxListFrame->Clean();
    //
    m_pHbxListFrame->MedieType(type);
    //
    nRet = m_pMediaFile->Open(path, &m_MediaInfo);
    if (nRet)
        return -1;
    //开始播放
    CHbxListFrame::ResetID(0);
    m_pReadThread->m_pMediaFile = m_pMediaFile;
    if (m_pAudio)
        m_pAudio->Start(m_pMediaFile);
    //启动解码线程
    m_pVideoDecodec->m_pMediaFile = m_pMediaFile;
    //
    return 0;
}

int CHbxMediaPlay::Start() {
    m_pVideoDecodec->CreatThread();
    m_pTasksCheduler->CreatThread();
    m_pReadThread->CreatThread();
    m_nMediaStatus = _STATUS_MEDIA_PLAY_;
    return 0;
}

int CHbxMediaPlay::Seek(int seek) {
    //清空以前的数据，暂停解码
    if (m_nMediaStatus != _STATUS_MEDIA_STOP_) {
        m_pReadThread->Seek(seek);
    }
    return 0;
}

int CHbxMediaPlay::Stop() {
    //清空以前的数据，暂停解码
    if (m_nMediaStatus != _STATUS_MEDIA_STOP_) {
        m_pTasksCheduler->Pause();
        m_pReadThread->ExitThread();
        m_pVideoDecodec->ExitThread();
        m_pTasksCheduler->ExitThread();

        if (m_pAudio)
            m_pAudio->Stop();
        m_pHbxListFrame->Clean();
        m_pMediaFile->Close();
        m_nMediaStatus = _STATUS_MEDIA_STOP_;

        HBXLOG("CHbxMediaPlay::Stop........\r\n");
    }
    return 0;
}

int CHbxMediaPlay::Status() {
    return m_nMediaStatus;
}

int CHbxMediaPlay::Cache() {
    if (m_MediaInfo.frame_rate > 0)
        return m_pHbxListFrame->VideoSize() / m_MediaInfo.frame_rate;
    return m_pHbxListFrame->VideoSize() / 30;
}

int CHbxMediaPlay::Duration() {
    return m_MediaInfo.nDuration;
}

int CHbxMediaPlay::Current() {
    if ((m_pReadThread->GetDecodecStatus() == _STATUS_READ_STOP_) &&
        (m_pHbxListFrame->VideoSize() == 0) && (m_pHbxListFrame->YuvSize() == 0))
        return -1;
    return m_pTasksCheduler->Current();
}

int CHbxMediaPlay::Pause() {
    if (m_nMediaStatus == _STATUS_MEDIA_PLAY_) {
        m_pTasksCheduler->Pause();
        m_nMediaStatus = _STATUS_MEDIA_PAUSE_;
    }
    return 0;
}

int CHbxMediaPlay::Play() {
    if (m_nMediaStatus == _STATUS_MEDIA_PAUSE_) {
        m_pTasksCheduler->Play();
        m_nMediaStatus = _STATUS_MEDIA_PLAY_;
    }
    return 0;
}

int CHbxMediaPlay::Sound(int volume) {
    m_pAudio->SetVolume(volume);
    return 0;
}

int CHbxMediaPlay::Mute(bool bmute) {
    m_pAudio->SetMute();
    return 0;
}

//1 soft ,0 hard
int CHbxMediaPlay::ChangeDecodec(int type) {
    m_pVideoDecodec->SetDecodecType(type);
    return 0;
}

int CHbxMediaPlay::GetDecodec() {
    return m_pVideoDecodec->GetDecodecType();
}

int CHbxMediaPlay::Width() {
    return m_MediaInfo.nWidth;
}

int CHbxMediaPlay::Height() {
    return m_MediaInfo.nHeight;
}