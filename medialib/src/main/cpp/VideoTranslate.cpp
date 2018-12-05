//
// Created by admin on 2016/11/9.
//

#include "VideoTranslate.h"

int VideoTranslate::getCurrentState() {
    return this->currentState;
}

int VideoTranslate::getProgress() {
    return this->progress;
}

void VideoTranslate::setProgressListner(funcProgressListner progress) {
    this->onprogress = progress;
}

void VideoTranslate::translate(const char *in_url, const char *out_url, int bitrate, int width,
                               int height) {
    std::thread transcode(&VideoTranslate::translateThread, this, in_url, out_url, bitrate, width,
                          height);
    transcode.detach();
}

void VideoTranslate::translateThread(const char *in_url, const char *out_url, int bitrate,
                                     int width, int height) {
    AVFormatContext *in_fmt_ctx, *out_fmt_ctx;
    AVCodec *pVideoCodec = NULL, *pAudioCodec = NULL;
    AVCodecContext *pVideoCodecCtx = NULL, *pAudioCodecCtx = NULL;
    struct SwsContext *sws_ctx;
    AVStream *in_stream, *out_stream,*out_audio_stream;
    AVFrame *pFrame,*oFrame;
    AVPacket in_pkt, out_pkt;
    int video_index = -1, audio_index = -1;
    this->currentState = 1;
    this->onProgressUpdate(currentState, 0, "start transcode");
    LOGE("start transcode width=%d,height=%d",width,height);
    av_register_all();
    avformat_network_init();
    avcodec_register_all();


    //input
    if (avformat_open_input(&in_fmt_ctx, in_url, NULL, NULL) < 0) {
        printf("can not open input file context");
    }
    if (avformat_find_stream_info(in_fmt_ctx, NULL) < 0) {
        printf("can not find input stream info!\n");
    }

    av_dump_format(in_fmt_ctx, 0, in_url, 0);
    int duration = in_fmt_ctx->duration / 1000000;
    LOGE("duration=%d", duration);
    //output
    avformat_alloc_output_context2(&out_fmt_ctx, NULL, NULL, out_url);
    if (!out_fmt_ctx) {
        printf("can not alloc output context!\n");
    }
    //open decoder & new out stream & open encoder
    for (int i = 0; i < in_fmt_ctx->nb_streams; i++) {
        if (in_fmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) {
            //open decoder
            if (0 > avcodec_open2(in_fmt_ctx->streams[i]->codec,
                                  avcodec_find_decoder(in_fmt_ctx->streams[i]->codec->codec_id),
                                  NULL)) {
                printf("can not find or open decoder!\n");
            }

            video_index = i;
            in_stream = in_fmt_ctx->streams[i];
            //new stream
            out_stream = avformat_new_stream(out_fmt_ctx, NULL);
            if (!out_stream) {
                printf("can not new stream for output!\n");
            }
            if (out_stream) {
                //copy the settings of AVCodecContext;
//                if (avcodec_copy_context(out_stream->codec, in_fmt_ctx->streams[i]->codec) < 0) {
//                    printf("Failed to copy context from input to output stream codec context\n");
//                    return;
//                }
                avcodec_parameters_from_context(out_stream->codecpar,in_fmt_ctx->streams[i]->codec);
                out_stream->codecpar->bit_rate=bitrate;
                out_stream->codecpar->width=width;
                out_stream->codecpar->height=height;
//                LOGE("start init out stream");
//                out_stream->codec->codec_tag = 0;
//                if (out_fmt_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
//                    out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
//                }
//                out_stream->sample_aspect_ratio = in_fmt_ctx->streams[i]->codec->sample_aspect_ratio; //fix it.
//                int frame_rate = 1 / (av_q2d(in_stream->codec->time_base) *
//                                      FFMAX(in_stream->codec->ticks_per_frame, 1));
//                out_stream->time_base = (AVRational) {1, frame_rate};
//                out_stream->codec->bit_rate = bitrate;
//                out_stream->codec->width = width;
//                out_stream->codec->height = height;
//                LOGE("end init out stream");
            }

            //set codec context param
            out_stream->codec->codec = avcodec_find_encoder(out_fmt_ctx->oformat->video_codec);
//            out_stream->codec->height = height;
//            out_stream->codec->width = width;
            out_stream->codec->height = in_fmt_ctx->streams[i]->codec->height;
            out_stream->codec->width = in_fmt_ctx->streams[i]->codec->width;

            //I do not know why the input file time_base is not correct
            //out_stream->codec->time_base = in_fmt_ctx->streams[i]->codec->time_base;

            out_stream->codec->time_base.num = in_fmt_ctx->streams[i]->avg_frame_rate.den;
            out_stream->codec->time_base.den = in_fmt_ctx->streams[i]->avg_frame_rate.num;

            out_stream->codec->sample_aspect_ratio = in_fmt_ctx->streams[i]->codec->sample_aspect_ratio;
            // take first format from list of supported formats
            out_stream->codec->pix_fmt = in_fmt_ctx->streams[i]->codec->pix_fmt;
            out_stream->codec->bit_rate = bitrate;
            out_stream->codec->pix_fmt = out_stream->codec->codec->pix_fmts[0];

            //open encoder
            if (!out_stream->codec->codec) {
                printf("can not find the encoder!\n");
            }
            LOGE("end init decode width=%d,bitrate=%d",out_stream->codec->width,out_stream->codec->bit_rate);
            if ((avcodec_open2(out_stream->codec, out_stream->codec->codec, NULL)) < 0) {
                printf("can not open the encoder\n");
            }

           // if (out_fmt_ctx->oformat->flags & AVFMT_GLOBALHEADER)
            //    out_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;

            LOGE("end init decode11 width=%d,bitrate=%d",out_stream->codec->width,out_stream->codec->bit_rate);
        }
        else if (in_fmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audio_index = i;
            pAudioCodecCtx = in_fmt_ctx->streams[i]->codec;
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
            out_audio_stream = avformat_new_stream(out_fmt_ctx, NULL);
            if (out_audio_stream) {
                //copy the settings of AVCodecContext;
                if (avcodec_copy_context(out_audio_stream->codec, in_fmt_ctx->streams[i]->codec) < 0) {
                    printf("Failed to copy context from input to output stream codec context\n");
                    return;
                }
                out_audio_stream->codec->codec_tag = 0;
          //      if (out_fmt_ctx->oformat->flags & AVFMT_GLOBALHEADER) {
              //      out_audio_stream->codec->flags |= CODEC_FLAG_GLOBAL_HEADER;
           //     }
                out_audio_stream->time_base = (AVRational) {1, pAudioCodecCtx->sample_rate};
            }
        }
    }

    av_dump_format(out_fmt_ctx, 0, out_url, 1);
    if (!(out_fmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&out_fmt_ctx->pb, out_url, AVIO_FLAG_WRITE) < 0) {
            printf("can not open output file handle!\n");
        }
    }

    if (avformat_write_header(out_fmt_ctx, NULL) < 0) {
        printf("can not write the header of the output file!\n");
    }


    sws_ctx = sws_getContext(in_stream->codec->width,
                             in_stream->codec->height,
                             in_stream->codec->pix_fmt,
                             width,
                             height,
                             in_stream->codec->pix_fmt,
                             SWS_BILINEAR,
                             NULL,
                             NULL,
                             NULL);
//    av_init_packet(&in_pkt);
//    av_init_packet(&out_pkt);
    pFrame = av_frame_alloc();
    oFrame=av_frame_alloc();
    if(oFrame){
        LOGE("init oframe sucess");
    }
    int got_frame, got_picture;
    int frame_index = 0;
    int i = 0;
    got_frame = -1;
    got_picture = -1;
//    in_pkt.data = NULL;
//    in_pkt.size = 0;
    int64_t pts_offset_v = -1, dts_offset_v = -1, pts_offset_a = -1, dts_offset_a = -1;
    LOGE("start transocde width=%d",out_stream->codec->width);
    while (av_read_frame(in_fmt_ctx, &in_pkt) >= 0) {

//        if (av_read_frame(in_fmt_ctx, &in_pkt) < 0) {
//            break;
//        }
        if (pts_offset_v < 0 || pts_offset_a < 0) {
            if (in_pkt.stream_index == video_index) {
                pts_offset_v = in_pkt.pts;
                dts_offset_v = in_pkt.dts;
            } else if (in_pkt.stream_index == audio_index) {
                pts_offset_a = in_pkt.pts;
                dts_offset_a = in_pkt.dts;
            }
        }
//        if (in_pkt.stream_index == video_index) {
//            //NSLog(@"Video: time: %f, endtime: %f", time, endtime);
//            in_pkt.pts = av_rescale_q_rnd(in_pkt.pts - pts_offset_v, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
//            in_pkt.dts = av_rescale_q_rnd(in_pkt.dts - dts_offset_v, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
//            in_pkt.duration = (int)av_rescale_q(in_pkt.duration, in_stream->time_base, out_stream->time_base);
//            in_pkt.pos = -1;
//        } else if (in_pkt.stream_index == audio_index) {
//            //NSLog(@"Audio: time: %f, endtime: %f", time, endtime);
//            in_pkt.pts = av_rescale_q_rnd(in_pkt.pts - pts_offset_a, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
//            in_pkt.dts = av_rescale_q_rnd(in_pkt.dts - dts_offset_a, in_stream->time_base, out_stream->time_base, AV_ROUND_NEAR_INF);
//            in_pkt.duration = (int)av_rescale_q(in_pkt.duration, in_stream->time_base, out_stream->time_base);
//            in_pkt.pos = -1;
//        }
        if (in_pkt.stream_index == video_index) {
            if (avcodec_decode_video2(in_fmt_ctx->streams[video_index]->codec, pFrame, &got_frame,
                                      &in_pkt) < 0) {
                printf("can not decoder a frame");
                break;
            }

            if (got_frame) {
//                pFrame->pts = i++;

//                out_pkt.data = NULL;
//                out_pkt.size = 0;
//                if(av_frame_copy(oFrame,pFrame)<0){
//                    LOGE("copy frame fail");
//                }
////                memcpy(oFrame, pFrame, sizeof(AVFrame));
//                sws_scale(sws_ctx, (uint8_t const *const *) pFrame->data,
//                          pFrame->linesize, 0, height,
//                          oFrame->data, oFrame->linesize);
//                LOGE("start encode");
//                if (avcodec_encode_video2(out_stream->codec, &out_pkt, pFrame, &got_picture) < 0) {
//                    printf("can not encode aframe!\n");
//                    break;
//                }
                avcodec_send_frame(out_stream->codec,oFrame);
                got_picture=avcodec_receive_packet(out_stream->codec,&out_pkt);
//                LOGE("end encode");
                if (got_picture>=0) {
                    printf("Succeed to encode frame: %5d\tsize:%5d\n", frame_index, out_pkt.size);

                    out_pkt.stream_index = out_stream->index;
                    frame_index++;
//                    av_write_frame(out_fmt_ctx, &out_pkt);
                    av_interleaved_write_frame(out_fmt_ctx, &out_pkt);
                }
                av_free_packet(&out_pkt);
                float time = in_pkt.pts *
                             (((float) in_stream->time_base.num) /
                              ((float) in_stream->time_base.den));
                this->progress = 100 * time / duration;
                this->currentState = 2;
                onProgressUpdate(this->currentState, this->progress, "transcoding");
                LOGE("progress=%d,width=%d,time=%f", this->progress, out_stream->codec->width, time);
            }
        }
        else if (in_pkt.stream_index == audio_index) {
//            avcodec_decode_audio4(in_fmt_ctx->streams[video_index]->codec, pFrame, &got_frame,
//                                  &in_pkt);
//            if(got_frame){
            in_pkt.pts = av_rescale_q_rnd(in_pkt.pts - pts_offset_a, in_stream->time_base, out_audio_stream->time_base, AV_ROUND_NEAR_INF);
            in_pkt.dts = av_rescale_q_rnd(in_pkt.dts - dts_offset_a, in_stream->time_base, out_audio_stream->time_base, AV_ROUND_NEAR_INF);
            in_pkt.duration = (int)av_rescale_q(in_pkt.duration, in_stream->time_base, out_audio_stream->time_base);
            in_pkt.pos = -1;
            if (av_interleaved_write_frame(out_fmt_ctx, &in_pkt) < 0) {
                printf("Error muxing packet\n");
                break;
            }
//            }
        }

        av_free_packet(&in_pkt);
    }
    av_frame_free(&pFrame);
//    int ret = avformat_flush(out_fmt_ctx);

//    if (ret < 0) {
//        printf("Flushing encoder failed");
//    }

    LOGE("start write trailer");
    //write file trailer
    av_write_trailer(out_fmt_ctx);

    LOGE("end wirite trailer");
    //clean
    avcodec_close(out_stream->codec);
    avcodec_close(in_fmt_ctx->streams[video_index]->codec);

    end:
    avformat_close_input(&in_fmt_ctx);

    if (out_fmt_ctx && !(out_fmt_ctx->oformat->flags & AVFMT_NOFILE)) {
        avio_close(out_fmt_ctx->pb);
    }
    avformat_free_context(out_fmt_ctx);

}

void VideoTranslate::onProgressUpdate(int state, int progress, char *info) {
    if (onprogress) {
        (*onprogress)(state, progress, info);
    }
}