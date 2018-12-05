//
// Created by huangxy on 2018/7/11.
//



#include "H264toJpg.h"
#include "log.h"

AVFrame *pFrame = NULL;
AVPacket *pPacket = NULL;
AVCodecContext *pCodecCtx;
AVFormatContext *pFormatCtx;
AVFrame *pFrameScale = NULL;

#define SCALE_WIDTH 640
#define SCALE_HEIGHT 360
#define SCALE_ENABLE 1

/**
 * 将AVFrame(YUV420格式)保存为JPEG格式的图片
 *
 * @param width YUV420的宽
 * @param height YUV42的高
 *
 */
int MyWriteJPEG(AVFrame *pFrame, int width, int height, char *out_file);

int initDecoder() {
    AVCodecID codecID = AV_CODEC_ID_H264;
    av_register_all();
    codecID = AV_CODEC_ID_H264;
    AVCodec *codec = avcodec_find_decoder(codecID);
    if (codec == NULL) {
        LOGE("find codec fail");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(codec);
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

    if (avcodec_open2(pCodecCtx, codec, NULL) < 0) {
        LOGE("open codec error\r\n");
        return -1;
    }

    pFrame = av_frame_alloc();

    pPacket = av_packet_alloc();
    av_init_packet(pPacket);


    pFrameScale = av_frame_alloc();
    int numBytes = av_image_get_buffer_size(pCodecCtx->pix_fmt, SCALE_WIDTH, SCALE_HEIGHT, 1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameScale->data, pFrameScale->linesize, buffer, pCodecCtx->pix_fmt,
                         SCALE_WIDTH, SCALE_HEIGHT, 1);

    pFrameScale->width = SCALE_WIDTH;
    pFrameScale->height = SCALE_HEIGHT;
    return 0;
}

void deInitDecoder() {
    if (pCodecCtx) {
        avcodec_free_context(&pCodecCtx);
    }
    if (pFrame) {
        av_frame_free(&pFrame);
    }
    if (pPacket) {
        av_packet_free(&pPacket);
    }
}

int decodeFrame(char *data, int len, char *out_file) {

    pPacket->size = len;
    pPacket->data = (uint8_t *) data;
    int got_frame = 0;
    int nRet = 0;
    if (pPacket->size > 0) {
        nRet = avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, pPacket);
        if (nRet < 0) {
            LOGE("avcodec_decode_video2:%d,data=%x,%x,%x,%x,%x,%x,%x,%x\r\n", nRet, data[0],
                 data[1], data[2], data[3], data[4], data[5], data[6], data[7]);
            av_packet_unref(pPacket);
            return nRet;
        }

        if (got_frame) {
            if (SCALE_ENABLE) {

                SwsContext *sws_context = sws_getContext(pFrame->width, pFrame->height,
                                                         pCodecCtx->pix_fmt,
                                                         SCALE_WIDTH,
                                                         SCALE_HEIGHT,
                                                         pCodecCtx->pix_fmt, SWS_BILINEAR, NULL,
                                                         NULL, NULL);
                LOGE("sws_contex=%d", sws_context);
                if (sws_context) {
                    int scale = sws_scale(sws_context, (const uint8_t *const *) pFrame->data,
                                          pFrame->linesize, 0, pFrame->height,
                                          pFrameScale->data, pFrameScale->linesize);
                    LOGE("end scale = %d", scale);
                    sws_freeContext(sws_context);
                    if (scale > 0) {
                        MyWriteJPEG(pFrameScale, pFrameScale->width, pFrameScale->height, out_file);
                    }
                }
            } else {

                MyWriteJPEG(pFrame, pFrame->width, pFrame->height, out_file);
            }

        }
    }
    av_packet_unref(pPacket);

    return 0;
}

/**
 * 将AVFrame(YUV420格式)保存为JPEG格式的图片
 *
 * @param width YUV420的宽
 * @param height YUV42的高
 *
 */
int MyWriteJPEG(AVFrame *pFrame, int width, int height, char *out_file) {

    // 分配AVFormatContext对象
    AVFormatContext *pFormatCtx = avformat_alloc_context();

    // 设置输出文件格式
    pFormatCtx->oformat = av_guess_format("mjpeg", NULL, NULL);
    // 创建并初始化一个和该url相关的AVIOContext
    if (avio_open(&pFormatCtx->pb, out_file, AVIO_FLAG_READ_WRITE) < 0) {
        LOGE("Couldn't open output file.");
        return -1;
    }

    // 构建一个新stream
    AVStream *pAVStream = avformat_new_stream(pFormatCtx, 0);
    if (pAVStream == NULL) {
        return -1;
    }

    // 设置该stream的信息
    AVCodecContext *pCodecCtx = pAVStream->codec;

    LOGE("width=%d,height=%d", width, height);
    pCodecCtx->codec_id = pFormatCtx->oformat->video_codec;
    pCodecCtx->codec_type = AVMEDIA_TYPE_VIDEO;
    pCodecCtx->pix_fmt = AV_PIX_FMT_YUVJ420P;
    pCodecCtx->width = width;
    pCodecCtx->height = height;
    pCodecCtx->time_base.num = 1;
    pCodecCtx->time_base.den = 25;

    // Begin Output some information
    av_dump_format(pFormatCtx, 0, out_file, 1);
    // End Output some information

    // 查找解码器
    AVCodec *pCodec = avcodec_find_encoder(pCodecCtx->codec_id);
    if (!pCodec) {
        LOGE("Codec not found.");
        return -1;
    }
    // 设置pCodecCtx的解码器为pCodec
    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("Could not open codec.");
        return -1;
    }

    //Write Header
    avformat_write_header(pFormatCtx, NULL);

    int y_size = pCodecCtx->width * pCodecCtx->height;

    //Encode
    // 给AVPacket分配足够大的空间
    AVPacket pkt;
    av_new_packet(&pkt, y_size * 3);

    //
    int got_picture = 0;
    int ret = avcodec_encode_video2(pCodecCtx, &pkt, pFrame, &got_picture);
    if (ret < 0) {
        LOGE("Encode Error.\n");
        return -1;
    }
    if (got_picture == 1) {
        //pkt.stream_index = pAVStream->index;
        ret = av_write_frame(pFormatCtx, &pkt);
    }

    av_free_packet(&pkt);

    //Write Trailer
    av_write_trailer(pFormatCtx);

    LOGE("Encode Successful.\n");

    if (pAVStream) {
        avcodec_close(pAVStream->codec);
    }
    avio_close(pFormatCtx->pb);
    avformat_free_context(pFormatCtx);

    return 0;
}