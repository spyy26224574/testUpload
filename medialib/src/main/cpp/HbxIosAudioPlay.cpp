//
//  CHbxIosAudioPlay.cpp
//  PanoCamViewDemo
//
//  Created by huang on 2018/1/19.
//  Copyright © 2018年 henry. All rights reserved.
//

#include "HbxIosAudioPlay.h"
#ifdef _IOS_
CHbxIosAudioPlay::CHbxIosAudioPlay()
{
}


CHbxIosAudioPlay::~CHbxIosAudioPlay()
{
}

void CHbxIosAudioPlay::Start(CHbxBaseFile* mediafile)
{
    //init
    initOpenAL();
}

void CHbxIosAudioPlay::Pause()
{
    ALint  state;
    alGetSourcei(outSourceId, AL_SOURCE_STATE, &state);
    if (state == AL_PLAYING) {
        alSourcePause(outSourceId);
    }
}

void CHbxIosAudioPlay::Stop()
{
    alSourceStop(outSourceId);
    cleanUpOpenAL();
}

int CHbxIosAudioPlay::AddFrame(AVFrame *pFrame)
{
    if (!mContext) {
        initOpenAL();
    }
    int nLen = pFrame->linesize[0] * pFrame->channels << 1;
    char *pBuf = new char[nLen];
    memset(pBuf,0,nLen);
    // 转换音频格式
    AVFrameToAudio(pFrame, pBuf, nLen);
    int ret = 0;
    //样本数openal的表示方法
    ALenum format = 0;
    //buffer id 负责缓存,要用局部变量每次数据都是新的地址
    ALuint bufferID = 0;
    
    //创建一个buffer
    alGenBuffers(1, &bufferID);
    if (pFrame->channels == 1) {
        format = AL_FORMAT_MONO16;
    }else {
        format = AL_FORMAT_STEREO16;
    }
    //指定要将数据复制到缓冲区中的数据
    alBufferData(bufferID, format, pBuf, nLen, pFrame->sample_rate);
    free(pBuf);
    //附加一个或一组buffer到一个source上
    alSourceQueueBuffers(outSourceId, 1, &bufferID);
    //更新队列数据
    ret = updataQueueBuffer();
    bufferID = 0;
    return ret;
}

int CHbxIosAudioPlay::initOpenAL() {
    int ret = 0;
    // init openAL
    mDevice=alcOpenDevice(NULL);
    if (mDevice) {
        mContext = alcCreateContext(mDevice, NULL);
        alcMakeContextCurrent(mContext);
    }
    
    alGenSources(1, &outSourceId);
    alSpeedOfSound(1.0);
    alDopplerVelocity(1.0);
    alDopplerFactor(1.0);
    alSourcef(outSourceId, AL_PITCH, 1.0f);
    alSourcef(outSourceId, AL_GAIN, 1.0f);
    alSourcei(outSourceId, AL_LOOPING, AL_FALSE);
    alSourcef(outSourceId, AL_SOURCE_TYPE, AL_STREAMING);
    return ret;
}

void CHbxIosAudioPlay::cleanUpOpenAL() {
    alDeleteSources(1, &outSourceId);
    ALCcontext * Context = alcGetCurrentContext();
    
    if (Context)
    {
        alcMakeContextCurrent(NULL);
        alcDestroyContext(Context);
        mContext = NULL;
    }
    alcCloseDevice(mDevice);
    mDevice = NULL;
    
    int processed;
    alGetSourcei(outSourceId, AL_BUFFERS_PROCESSED, &processed);
    while(processed--) {
        alDeleteBuffers(1, &buff);
    }
}

int CHbxIosAudioPlay::updataQueueBuffer() {
    //播放状态字段
    ALint stateVaue = 0;
    
    //获取处理队列，得出已经播放过的缓冲器的数量
    alGetSourcei(outSourceId, AL_BUFFERS_PROCESSED, &m_numprocessed);
    //获取缓存队列，缓存的队列数量
    alGetSourcei(outSourceId, AL_BUFFERS_QUEUED, &m_numqueued);
    
    //获取播放状态，是不是正在播放
    alGetSourcei(outSourceId, AL_SOURCE_STATE, &stateVaue);
    
    if (stateVaue == AL_STOPPED ||
        stateVaue == AL_PAUSED ||
        stateVaue == AL_INITIAL)
    {

        if (stateVaue != AL_PLAYING)
        {
            alSourcePlay(outSourceId);
        }
    }
    //将已经播放过的的数据删除掉
    while(m_numprocessed --)
    {
        ALuint buff;
        //更新缓存buffer中的数据到source中
        alSourceUnqueueBuffers(outSourceId, 1, &buff);
        //删除缓存buff中的数据
        alDeleteBuffers(1, &buff);
        //得到已经播放的音频队列多少块
        m_IsplayBufferSize ++;
    }
    return 1;
}

int CHbxIosAudioPlay::AVFrameToAudio(AVFrame* pAudioDecodeFrame, char *buffer, int &len)
{
    SwrContext * swr_ctx = NULL;
    int data_size = 0;
    int ret = 0;
    int64_t src_ch_layout = AV_CH_LAYOUT_STEREO;
    int64_t dst_ch_layout = AV_CH_LAYOUT_STEREO;
    int dst_nb_channels = 0;
    int dst_linesize = 0;
    int src_nb_samples = 0;
    int dst_nb_samples = 0;
    int max_dst_nb_samples = 0;
    uint8_t **dst_data = NULL;
    int resampled_data_size = 0;
    
    enum AVSampleFormat out_sample_fmt =  AVSampleFormat::AV_SAMPLE_FMT_S16;
    int out_channels = pAudioDecodeFrame->channels;
    int out_sample_rate = pAudioDecodeFrame->sample_rate;
    
    
    src_ch_layout = (pAudioDecodeFrame->channel_layout &&
                     pAudioDecodeFrame->channels ==
                     av_get_channel_layout_nb_channels(pAudioDecodeFrame->channel_layout)) ?
    pAudioDecodeFrame->channel_layout :
    av_get_default_channel_layout(pAudioDecodeFrame->channels);
    
    if (out_channels == 1)
    {
        dst_ch_layout = src_ch_layout;
        out_channels = pAudioDecodeFrame->channels;
        out_sample_rate = pAudioDecodeFrame->sample_rate;
    }
    else if (out_channels == 2)
    {
        dst_ch_layout = AV_CH_LAYOUT_STEREO;
    }
    
    if(src_ch_layout <= 0)
        return -1;
    
    src_nb_samples = pAudioDecodeFrame->nb_samples;
    if(src_nb_samples <= 0)
        return -1;
    
    swr_ctx = swr_alloc_set_opts(NULL, //AV_CH_LAYOUT_MONO,
                                 dst_ch_layout,           //输出通道布局
                                 out_sample_fmt,
                                 out_sample_rate,
                                 src_ch_layout, //3 4 AV_CH_LAYOUT_MONO
                                 (AVSampleFormat)pAudioDecodeFrame->format, //6  8 AV_SAMPLE_FMT_FLTP
                                 pAudioDecodeFrame->sample_rate, //44100 16000
                                 0, NULL);
    
    if (!swr_ctx)
        return -1;
    
    swr_init(swr_ctx);
    max_dst_nb_samples = dst_nb_samples =
    av_rescale_rnd(src_nb_samples, out_sample_rate, pAudioDecodeFrame->sample_rate, AV_ROUND_INF);
    
    if(max_dst_nb_samples <= 0)
        return -1;
    
    dst_nb_channels = av_get_channel_layout_nb_channels(dst_ch_layout);
    ret = av_samples_alloc_array_and_samples(&dst_data, &dst_linesize, dst_nb_channels,
                                             dst_nb_samples, (AVSampleFormat)out_sample_fmt, 0);
    if(ret < 0)
        return -1;
    
    
    dst_nb_samples = av_rescale_rnd(swr_get_delay(swr_ctx, pAudioDecodeFrame->sample_rate) +
                                    src_nb_samples, out_sample_rate, pAudioDecodeFrame->sample_rate, AV_ROUND_INF);
    if(dst_nb_samples <= 0)
        return -1;
    
    if(dst_nb_samples > max_dst_nb_samples)
    {
        av_free(dst_data[0]);
        ret = av_samples_alloc(dst_data, &dst_linesize, dst_nb_channels,
                               dst_nb_samples, (AVSampleFormat)out_sample_fmt, 0);
        max_dst_nb_samples = dst_nb_samples;
    }
    
    data_size = av_samples_get_buffer_size(NULL, pAudioDecodeFrame->channels,
                                           pAudioDecodeFrame->nb_samples,(AVSampleFormat)pAudioDecodeFrame->format, 0);
    
    if(data_size <= 0)
        return -1;
    
    resampled_data_size = data_size;
    
    if(swr_ctx)
    {
        ret = swr_convert(swr_ctx, dst_data, dst_nb_samples,
                          (const uint8_t **)pAudioDecodeFrame->data, pAudioDecodeFrame->nb_samples);
        if(ret <= 0)
            return -1;
        
        resampled_data_size = av_samples_get_buffer_size(&dst_linesize, dst_nb_channels,
                                                         ret, (AVSampleFormat)out_sample_fmt, 0);
        
        len = resampled_data_size;
        if(resampled_data_size <= 0)
        {
            return -1;
        }
    }
    else
        return -1;
    
    if (dst_data)
    {
        memcpy(buffer, dst_data[0], resampled_data_size);
        av_freep(&dst_data[0]);
    }
    av_freep(&dst_data);
    dst_data = NULL;
    
    if(swr_ctx)
        swr_free(&swr_ctx);
    return 0;
}

#endif
