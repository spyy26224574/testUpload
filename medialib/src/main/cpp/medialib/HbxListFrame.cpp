
#include "HbxListFrame.h"

static int gVideoID = 0, gAudioID = 0;

void CHbxListFrame::ResetID(int id) {
    gVideoID = 0;
    gAudioID = 0;
}

CHbxListFrame::CHbxListFrame() {
    list_init(&m_Video);
    list_init(&m_Audio);
    list_init(&m_YuvList);
    //
    m_VideoListSize = 0;
    m_YuvListSize = 0;
    //
    m_MaxYuvCache = _LOW_BUFFER_YUV_;
    //
    m_nCurFileType = _LOCAL_TYPE_MEDIA_FROM_;
    m_nReadStatus = _STATUS_READ_STOP_;
}


CHbxListFrame::~CHbxListFrame() {
    Clean();
}

void CHbxListFrame::Push(CHbxFrame *hbxFrame) {
    m_Mutex.Lock();
    if (hbxFrame->m_nIndex == _AUDIO_INDEX_STREAM_) {
        hbxFrame->m_nID = gAudioID;
        list_add_tail(&(hbxFrame->m_framelist), &m_Audio);
        gAudioID++;
    } else if (hbxFrame->m_nIndex == _VIDEO_INDEX_STREAM_) {
        hbxFrame->m_nID = gVideoID;
        list_add_tail(&(hbxFrame->m_framelist), &m_Video);
        m_VideoListSize++;
        gVideoID++;
    }
    if (hbxFrame->m_nIndex == 3) {
        list_add_tail(&(hbxFrame->m_framelist), &m_YuvList);
        m_YuvListSize++;
        //   HBXLOG("push:m_MaxYuvCache:%d m_YuvListSize=%d ...............\r\n",m_MaxYuvCache,m_YuvListSize);
    }
    m_Mutex.UnLock();
}

CHbxFrame *CHbxListFrame::YuvPop() {
    CHbxFrame *frame = NULL;
    struct list_head *item = NULL;
    //
    m_Mutex.Lock();
    item = list_pop(&m_YuvList);
    if (item) {
        frame = (CHbxFrame *) item;
        m_YuvListSize--;
        //HBXLOG("pop:m_MaxYuvCache:%d m_YuvListSize=%d \r\n",m_MaxYuvCache,m_YuvListSize);
    }
    //
    item = NULL;
    m_Mutex.UnLock();
    return frame;
}

CHbxFrame *CHbxListFrame::VideoPop() {
    CHbxFrame *frame = NULL;
    struct list_head *item = NULL;
    if (m_MaxYuvCache <= m_YuvListSize)
        return NULL;
    m_Mutex.Lock();
    item = list_pop(&m_Video);
    if (item) {
        frame = (CHbxFrame *) item;
        m_VideoListSize--;
    }
    //
    item = NULL;
    m_Mutex.UnLock();
    return frame;
}

CHbxFrame *CHbxListFrame::AudioPop() {
    CHbxFrame *frame = NULL;
    struct list_head *item = NULL;
    m_Mutex.Lock();
    item = list_pop(&m_Audio);
    if (item)
        frame = (CHbxFrame *) item;
    item = NULL;
    m_Mutex.UnLock();
    return frame;
}

int CHbxListFrame::VideoSize() {
    return m_VideoListSize;
}

int CHbxListFrame::YuvSize() {
    return m_YuvListSize;
}

void CHbxListFrame::Clean() {
    m_Mutex.Lock();
    //
    m_nCurFileType = _LOCAL_TYPE_MEDIA_FROM_;
    try {
        CHbxFrame *frame = NULL;
        if (m_Audio.next) {
            struct list_head *head = &m_Audio;
            for (frame = (CHbxFrame *) head->next;
                 head->next != NULL; frame = (CHbxFrame *) head->next) {
                list_del(&(frame->m_framelist), head);
                DELETE_BUFFER(frame);
            }
        }

        if (m_Video.next) {
            struct list_head *head = &m_Video;
            for (frame = (CHbxFrame *) head->next;
                 head->next != NULL; frame = (CHbxFrame *) head->next) {
                list_del(&(frame->m_framelist), head);
                DELETE_BUFFER(frame);
            }
        }
        m_VideoListSize = 0;
        if (m_YuvList.next) {
            struct list_head *head = &m_YuvList;
            for (frame = (CHbxFrame *) head->next;
                 head->next != NULL; frame = (CHbxFrame *) head->next) {
                list_del(&(frame->m_framelist), head);
                DELETE_BUFFER(frame);
            }
        }
        m_YuvListSize = 0;
    }
    catch (exception &e) {
        HBXLOG("error: CHbxListFrame::Clean \r\n");
    }
    m_Mutex.UnLock();
}

void CHbxListFrame::SetMaxYuvCache(int count) {
    if (count < 100)
        m_MaxYuvCache = count;
}

int CHbxListFrame::GetMaxYuvCache() {
    return m_MaxYuvCache;
}

bool CHbxListFrame::NeedHttpCache() {
    //1 http file
    //2 m_VideoListSize > _MIN_BUFFER_FRAME_
    //3 file is no finish
    if ((m_VideoListSize < _MIN_BUFFER_FRAME_) && (m_nReadStatus == _STATUS_READ_PLAY_) &&
        (m_nCurFileType == _HTTP_TYPE_MEDIA_FROM_))
        return true;
    return false;
}
//
