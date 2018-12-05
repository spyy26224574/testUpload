//
// Created by admin on 2016/11/9.
//

#include "VideoCut.h"

void VideoCut::cutVideo(const char *in_url, const char *out_url, int startTime, int endTime) {
//    this->in_url=in_url;
//    this->out_url=out_url;
//    this->startTime=startTime;
//    this->endTime=endTime;
    std::thread cutthread(&VideoCut::cutThread,this, in_url,out_url,startTime,endTime);
    cutthread.detach();

}

void VideoCut::cutThread(const char *in_url, const char *out_url, int startTime_, int endTime_) {
    AVFormatContext *pInFormatCtx = NULL, *pOutFormatCtx = NULL;
    AVCodec *pVideoCodec = NULL, *pAudioCodec = NULL;
    AVCodecContext *pVideoCodecCtx = NULL, *pAudioCodecCtx = NULL;
    int videoIndex = -1, audioIndex = -1;
    this->currentState=1;
    this->progress=0;
    onCallback(this->currentState,this->progress,"start cut");
    av_register_all();
    avformat_network_init();

    //Open file
    if(avformat_open_input(&pInFormatCtx, in_url, NULL, NULL) < 0) {
        fprintf(stderr, "avformat_open_input() failed.\n");
        return;
    }
    if (avformat_find_stream_info(pInFormatCtx, NULL) < 0) {
        fprintf(stderr, "avformat_find_stream_info() failed.\n");
        return;
    }
    av_dump_format(pInFormatCtx, 0, in_url, 0);
    avformat_alloc_output_context2(&pOutFormatCtx, NULL, NULL, out_url);
    if (!pOutFormatCtx)
    {
        printf( "Could not create output1 context\n");
        return;
    }
    //Find codec and codecCtx
    for (int i = 0; i < pInFormatCtx->nb_streams; i++) {
        if (pInFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            videoIndex = i;
            pVideoCodecCtx = pInFormatCtx->streams[i]->codec;
            pVideoCodec = avcodec_find_decoder(pVideoCodecCtx->codec_id);
            if (!pVideoCodec) {
                fprintf(stderr, "Video: avcodec_find_decoder() failed.\n");
                return;
            }
            if (avcodec_open2(pVideoCodecCtx, pVideoCodec, NULL) < 0) {
                fprintf(stderr, "Video: avcodec_open() failed.\n");
                return;
            }

            //create video stream
            AVStream *videoStream = avformat_new_stream(pOutFormatCtx, NULL);
            if (videoStream) {
                //copy the settings of AVCodecContext;
                if (avcodec_copy_context(videoStream->codec, pInFormatCtx->streams[i]->codec) < 0)
                {
                    printf("Failed to copy context from input to output stream codec context\n");
                    return;
                }
                videoStream->codec->codec_tag = 0;
               // if(pOutFormatCtx->oformat->flags & AVFMT_GLOBALHEADER)
             //   {
                 //   videoStream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
               // }
                videoStream->sample_aspect_ratio = pInFormatCtx->streams[i]->codec->sample_aspect_ratio; //fix it.
                int frame_rate = 1 / (av_q2d(pVideoCodecCtx->time_base) * FFMAX(pVideoCodecCtx->ticks_per_frame, 1));
                videoStream->time_base = (AVRational){ 1, frame_rate};
            }
        } else if (pInFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO){
            audioIndex = i;
            pAudioCodecCtx = pInFormatCtx->streams[i]->codec;
            pAudioCodec = avcodec_find_decoder(pAudioCodecCtx->codec_id);
            if (!pAudioCodec) {
                fprintf(stderr, "Audio: avcodec_find_decoder() failed.\n");
                return;
            }
            if (avcodec_open2(pAudioCodecCtx, pAudioCodec, NULL) < 0) {
                fprintf(stderr, "Audio: avcodec_open() failed.\n");
                return;
            }

            //create audio stream
            AVStream *audioStream = avformat_new_stream(pOutFormatCtx, NULL);
            if (audioStream) {
                //copy the settings of AVCodecContext;
                if (avcodec_copy_context(audioStream->codec, pInFormatCtx->streams[i]->codec) < 0)
                {
                    printf( "Failed to copy context from input to output stream codec context\n");
                    return;
                }
                audioStream->codec->codec_tag = 0;
             //   if(pOutFormatCtx->oformat->flags & AVFMT_GLOBALHEADER)
             //   {
                //    audioStream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
               // }
                audioStream->time_base = (AVRational){ 1, pAudioCodecCtx->sample_rate};
            }
        }
    }
    av_dump_format(pOutFormatCtx, 0, out_url, 1);

    //Seek to start time
    float starttime = startTime_;
    float endtime = endTime_;
    AVRational default_timebase;
    default_timebase.num = 1;
    default_timebase.den = AV_TIME_BASE;

    AVStream *inVideoStream = pInFormatCtx->streams[videoIndex];
    AVStream *inAudioStream = pInFormatCtx->streams[audioIndex];
    int64_t seek_time_v = av_rescale_q((int64_t)( starttime * AV_TIME_BASE ), default_timebase, inVideoStream->time_base);
    int64_t seek_time_a = av_rescale_q((int64_t)( starttime * AV_TIME_BASE ), default_timebase, inAudioStream->time_base);
    //int64_t endtime_int64 = av_rescale_q((int64_t)( endtime * AV_TIME_BASE ), default_timebase, inVideoStream->time_base);

    if(avformat_seek_file(pInFormatCtx, videoIndex, INT64_MIN, seek_time_v, INT64_MAX, 0) < 0 ||
       avformat_seek_file(pInFormatCtx, audioIndex, INT64_MIN, seek_time_a, INT64_MAX, 0) < 0 ) {
        // error... do something...
        return; // usually 0 is used for success in C, but I am following your code.
    }
    avcodec_flush_buffers(inVideoStream->codec);
    avcodec_flush_buffers(inAudioStream->codec);

    //open ouput file
    if (!(pOutFormatCtx->oformat->flags & AVFMT_NOFILE))
    {
        if (avio_open(&pOutFormatCtx->pb, out_url, AVIO_FLAG_WRITE) < 0)
        {
            printf( "Could not open output file '%s'", out_url);
            return;
        }
    }
    if (avformat_write_header(pOutFormatCtx, NULL) < 0)
    {
        printf( "Error occurred when opening video output file\n");
        return;
    }

    //Read packet
    AVPacket pkt;
    int64_t pts_offset_v = -1, dts_offset_v = -1, pts_offset_a = -1, dts_offset_a = -1;
    while(av_read_frame(pInFormatCtx, &pkt) >= 0) {
        AVStream *in_stream = pInFormatCtx->streams[pkt.stream_index];
        AVStream *out_stream = pOutFormatCtx->streams[pkt.stream_index];
        float time = pkt.pts * (((float)in_stream->time_base.num) / ((float)in_stream->time_base.den));

        //Calculate pts and dts offset
        if (pts_offset_v < 0 || pts_offset_a < 0) {
            if (pkt.stream_index == videoIndex) {
                pts_offset_v = pkt.pts;
                dts_offset_v = pkt.dts;
            } else if (pkt.stream_index == audioIndex) {
                pts_offset_a = pkt.pts;
                dts_offset_a = pkt.dts;
            }
        }

        // copy packet
        if (time <= endtime) {
            if (pkt.stream_index == videoIndex) {
                //NSLog(@"Video: time: %f, endtime: %f", time, endtime);
                pkt.pts = av_rescale_q_rnd(pkt.pts - pts_offset_v, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
                pkt.dts = av_rescale_q_rnd(pkt.dts - dts_offset_v, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
                pkt.duration = (int)av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
                pkt.pos = -1;
            } else if (pkt.stream_index == audioIndex) {
                //NSLog(@"Audio: time: %f, endtime: %f", time, endtime);
                pkt.pts = av_rescale_q_rnd(pkt.pts - pts_offset_a, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
                pkt.dts = av_rescale_q_rnd(pkt.dts - dts_offset_a, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
                pkt.duration = (int)av_rescale_q(pkt.duration, in_stream->time_base, out_stream->time_base);
                pkt.pos = -1;
            }

            //Update progress
            float progress = (time - startTime_) *100 / (endtime-starttime);
            if (progress < 100 && progress > this->progress) {
                currentState=2;
                this->progress = progress;
                onCallback(currentState,this->progress,"cutting");
            }

            //Write into file
            if (av_interleaved_write_frame(pOutFormatCtx, &pkt) < 0){
                printf( "Error muxing packet\n");
                break;
            }
            LOGE("pts=%d",pkt.pts);
            av_free_packet(&pkt);
        } else {
            break; // exit the loop
        }
    }

    //Write trailer
    av_write_trailer(pOutFormatCtx);

    //close input
    if (pVideoCodecCtx != NULL) {
        avcodec_close(pVideoCodecCtx);
    }
    if (pAudioCodecCtx != NULL) {
        avcodec_close(pAudioCodecCtx);
    }
    avformat_close_input(&pInFormatCtx);
    avformat_free_context(pInFormatCtx);

    //close output
    if (pOutFormatCtx && !(pOutFormatCtx->oformat->flags & AVFMT_NOFILE))
        avio_close(pOutFormatCtx->pb);
    avformat_free_context(pOutFormatCtx);
    this->currentState=3;
    onCallback(this->currentState,100,"cut end");
}

void VideoCut::setCallback(funCallback callback) {
    this->callback=callback;
}

void VideoCut::onCallback(int state, int progress, char *info) {
    if(this->callback){
        (*(this->callback))(state,progress,info);
    }
}

int VideoCut::getCurrentState() {
    return this->currentState;
}

int VideoCut::getProgress() {
    return this->progress;
}