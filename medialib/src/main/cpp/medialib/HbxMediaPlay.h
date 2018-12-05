
#ifndef _HBX_MEDIA_PLAY_H_
#define _HBX_MEDIA_PLAY_H_

#include "HbxBaseFile.h"
#include "HbxBaseTasksCheduler.h"
#include "HbxReadThread.h"
#include "HbxListFrame.h"
#include "HbxAudioPlay.h"
#include "HbxVideoDecodec.h"

class CHbxMediaPlay {
public:
    CHbxMediaPlay();

    ~CHbxMediaPlay();

protected:
    CHbxBaseFile *m_pMediaFile;
    CHbxBaseTasksCheduler *m_pTasksCheduler;
    CHbxReadThread *m_pReadThread;
    CHbxListFrame *m_pHbxListFrame;
    CHbxVideoDecodec *m_pVideoDecodec;
    //
    int m_nMediaStatus;

    int GetFileFromByName(char *path);

public:
    struct MediaInfo m_MediaInfo;

    int OpenFile(const char *path, int type = _HTTP_TYPE_MEDIA_FROM_);

    int Start();

    int Open(char *path, int type = _HTTP_TYPE_MEDIA_FROM_);

    int Seek(int seek);

    int Stop();

    int Status();

    int Duration();

    int Current();

    int Pause();

    int Play();

    int Cache();

    int Sound(int volume);

    int Mute(bool bmute);

    //1 soft ,0 hard
    int ChangeDecodec(int type);

    int GetDecodec();

    //
    int Width();

    int Height();
};

#endif
