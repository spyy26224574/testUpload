
#include"public.h"
#include "HbxFrame.h"
#include "HbxThread.h"
#include <thread>
#include <condition_variable>
#include <mutex>


#ifndef  _HBX_BASEFILE_H_
#define _HBX_BASEFILE_H_

class CHbxBaseFile {
public:
    CHbxBaseFile();

    ~CHbxBaseFile();

protected:
    AVFormatContext *m_pFC;
    int m_nVideoStream;
    int m_nAudioStream;
    AVCodecContext *m_vCodecContext;
    AVCodecContext *m_aCodecContext;
    AVPacket *m_pkt;

public:
    int m_nStatus;
    CHbxMutex m_Mutex;
    int m_nCurrentDts;

public:
    int SoftVideoDecodec(CHbxFrame *hbxFrame);

    virtual int Open(const char *, struct MediaInfo *);

    virtual int Close();

    virtual int ReadFrame(CHbxFrame *hbxFrame);

    int GetCurrentDts() { return m_nCurrentDts; };

    virtual int Seek(int seek);

    AVCodecContext *AudioContext() { return m_aCodecContext; };

    AVCodecContext *VideoContext() { return m_vCodecContext; };

};


#endif // ! _HBX_BASEFILE_H_
