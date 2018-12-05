//
//  CHbxIosAudioPlay.hpp
//  PanoCamViewDemo
//
//  Created by huang on 2018/1/19.
//  Copyright © 2018年 henry. All rights reserved.
//

#ifndef CHbxIosAudioPlay_h
#define CHbxIosAudioPlay_h

#include "public.h"
#include "HbxAudioPlay.h"
#ifdef _IOS_
class CHbxIosAudioPlay : public CHbxAudioPlay
{
public:
    CHbxIosAudioPlay();
    ~CHbxIosAudioPlay();
    
protected:
    ALCcontext *mContext;
    ALCdevice  *mDevice;
    ALuint     outSourceId;
    ALuint     buff;
    
    int m_numprocessed;             //队列中已经播放过的数量
    int m_numqueued;
    long long m_IsplayBufferSize;   //已经播放了多少个音频缓存数目
public:
    virtual void Start(CHbxBaseFile* mediafile);
    virtual void Pause();
    virtual void Stop();
    virtual int AddFrame(AVFrame *pFrame);
    
private:
    //初始化openal
    int initOpenAL();
    //释放openal
    void cleanUpOpenAL();
    
    int updataQueueBuffer();
    
    int AVFrameToAudio(AVFrame* pAudioDecodeFrame, char *buffer, int &len);
};
#endif
#endif /* CHbxIosAudioPlay_hpp */
