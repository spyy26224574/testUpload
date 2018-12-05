//
// Created by admin on 2016/11/5.
//

#include "FfmpegPlayer.h"

FfmpegPlayer::FfmpegPlayer(JNIEnv *env, jstring url_, jobject surface) {
    url = env->GetStringUTFChars(url_, 0);
    nativeWindow = ANativeWindow_fromSurface(env, surface);
    init();
}

FfmpegPlayer::FfmpegPlayer() {
    init();
}

void FfmpegPlayer::init() {
    mListerner = NULL;
    previewFrames = new ObjectArray<AVFrame *>();
    mFramePool = new ObjectArray<AVFrame *>();
    pthread_mutex_init(&decode_mutex, NULL);
    pthread_mutex_init(&preview_mutex, NULL);
    pthread_mutex_init(&pool_mutex, NULL);
    pthread_cond_init(&preview_cond, NULL);

}

FfmpegPlayer::~FfmpegPlayer() {
    if (buf) {
        delete buf;
    }
    pthread_mutex_lock(&decode_mutex);
    av_frame_unref(pFrame);
    av_frame_free(&pFrame);
    pFrame = NULL;
    av_packet_free(&pPacket);
    pthread_mutex_unlock(&decode_mutex);
    clearPreviewFrame();
    clear_pool();
//    pthread_mutex_lock(&preview_mutex);
//    {
//        pthread_cond_signal(&preview_cond);
//    }
//    pthread_mutex_unlock(&preview_mutex);
    pthread_mutex_destroy(&decode_mutex);
    pthread_mutex_destroy(&preview_mutex);
    pthread_mutex_destroy(&pool_mutex);
    pthread_cond_destroy(&preview_cond);
    if (instance) {
        free(instance);
        instance = NULL;
    }
    LOGE("~FfmpegPlayer delete");
}

void FfmpegPlayer::start() {
//    pthread_create(&decode, NULL, &testPthread, (void *) url);
//    pthread_detach(decode);
//    pthread_create(&display, NULL, FfmpegPlayer::displayVideo, NULL);
//    pthread_detach(display);
    currentState = NONE;
    this->url = "/mnt/sdcard/server-share.h264";
//    std::thread decode(&FfmpegPlayer::decodeVideo,this,url);
//    decode.detach();
    std::thread display(&FfmpegPlayer::displayVideo, this);
    display.detach();
}

void FfmpegPlayer::stop() {
    this->currentState = STOP;
    this->video_start_time = 0.0;
    this->video_fps = 0.0;
//    std::thread
    LOGE("FfmpegPlayer stop");
}

void FfmpegPlayer::decodeVideo(const char *url) {
    LOGE("start decode thread tid=%d", gettid());
    restart:
    needRestart = false;
    notifyMediaInfo(START_PREPARE, "start preparing");
    const char *file_name = this->url;//"rtsp://192.168.1.254/1.mov";//
//    const char *file_name = "/storage/sdcard0/ligo/out.MOV";
    av_register_all();
    avfilter_register_all();
    avformat_network_init();
    notifyMediaInfo(PREPRARING, "start preparing");
    AVFormatContext *pFormatCtx = avformat_alloc_context();
    if (!pFormatCtx) {
        LOGE("init pformat error");
        return;
    }
    AVDictionary *options = NULL;
    av_dict_set(&options, "buffer_size", "1024000", 0); //设置缓存大小，1080p可将值调大
//    av_dict_set(&options, "rtsp_transport", "tcp", 0); //以udp方式打开，如果以tcp方式打开将udp替换为tcp
    av_dict_set(&options, "stimeout", "50000", 0); //设置超时断开连接时间，单位微秒
    av_dict_set(&options, "max_delay", "20000", 0); //设置最大时延
    // Open video file
    if (avformat_open_input(&pFormatCtx, file_name, NULL, NULL) != 0) {

        LOGE("Couldn't open file:%s\n", file_name);
        notifyMediaInfo(ERROR, "couldn't not open file");
        return; // Couldn't open file
    }
    // Retrieve stream information
    if (avformat_find_stream_info(pFormatCtx, NULL) < 0) {
        LOGE("Couldn't find stream information.");
        notifyMediaInfo(ERROR, "Couldn't find stream information.");
        return;
    }
    // Find the first video stream
    int videoStream = -1, audioStream = -1, i;
    for (i = 0; i < pFormatCtx->nb_streams; i++) {
        if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO
            && videoStream < 0) {
            videoStream = i;
            i_video_stream = pFormatCtx->streams[i];
        } else if (pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) {
            audioStream = i;
        }
    }
//    if (videoStream == -1) {
//        LOGE("Didn't find a video stream.1111111");
//        notifyMediaInfo(ERROR, "Didn't find a video stream");
//        return; // Didn't find a video stream
//    }

    // Get a pointer to the codec context for the video stream
    AVCodecContext *pCodecCtx = pFormatCtx->streams[videoStream]->codec;

//    startCut("/storage/sdcard0/testcut1.mp4");
    // Find the decoder for the video stream
    AVCodec *pCodec = NULL;
//    pCodec = avcodec_find_decoder_by_name("h264_mediacodec");
    if (pCodec == NULL) {
        pCodec = avcodec_find_decoder(pCodecCtx->codec_id);
    }
    LOGE("end find decoder width=%d,height=%d,pix_fmt=%d", pCodecCtx->width, pCodecCtx->height,
         pCodecCtx->pix_fmt);
    if (pCodec == NULL) {
        LOGE("Codec not found.");
        notifyMediaInfo(ERROR, "Codec not found.");
        return; // Codec not found
    }

    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
        LOGE("Could not open codec.");
//        return ; // Could not open codec
        notifyMediaInfo(ERROR, "Could not open codec.");
    }
    // 获取视频宽高
    videoWidth = pCodecCtx->width;
    videoHeight = pCodecCtx->height;

    notifyVideoSize(videoWidth, videoHeight);
    // 设置native window的buffer大小,可自动拉伸
    if (nativeWindow != NULL)
        ANativeWindow_setBuffersGeometry(nativeWindow, videoWidth, videoHeight,
                                         WINDOW_FORMAT_RGBA_8888);
//    ANativeWindow_Buffer windowBuffer;

//    if (avcodec_open2(pCodecCtx, pCodec, NULL) < 0) {
//        LOGE("Could not open codec.");
//        notifyMediaInfo(ERROR, "Could not open codec.");
//        return;
////        return ; // Could not open codec
//    }

    // Allocate video frame
    AVFrame *pFrame = av_frame_alloc();

    // 用于渲染
    pFrameRGBA = av_frame_alloc();
    if (pFrameRGBA == NULL || pFrame == NULL) {
        LOGE("Could not allocate video frame.");
        notifyMediaInfo(ERROR, "Could not open codec.");
        return;
//        return ;
    }

    // Determine required buffer size and allocate buffer
    // buffer中数据就是用于渲染的,且格式为RGBA
    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, pCodecCtx->width, pCodecCtx->height,
                                            1);
    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
                         pCodecCtx->width, pCodecCtx->height, 1);

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
    sws_ctx = sws_getContext(pCodecCtx->width,
                             pCodecCtx->height,
                             pCodecCtx->pix_fmt,
                             pCodecCtx->width,
                             pCodecCtx->height,
                             AV_PIX_FMT_RGBA,
                             SWS_BILINEAR,
                             NULL,
                             NULL,
                             NULL);

    notifyMediaInfo(PREPARED, "prepared end");
    int frameFinished;
    AVPacket packet;
    notifyMediaInfo(START_BUFFER, "start buffer");

    while (this->currentState != STOP && !needRestart) {

        int ret = -1;
        if ((ret = av_read_frame(pFormatCtx, &packet)) >= 0) {
            // Is this a packet from the video stream?
            if (packet.stream_index == videoStream) {

                // Decode video frame
//                avcodec_send_packet(pCodecCtx, &packet);
//                frameFinished=avcodec_receive_frame(pCodecCtx, pFrame);
                int ret = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);

                // 并不是decode一次就可解码出一帧
                if (frameFinished) {

                    if (video_fps !=
                        av_q2d(pCodecCtx->time_base) * FFMAX(pCodecCtx->ticks_per_frame, 1)) {
                        video_fps =
                                av_q2d(pCodecCtx->time_base) * FFMAX(pCodecCtx->ticks_per_frame, 1);
                        LOGE("video_fps: %f.\n", video_fps);
                    }
//                    if (video_fps > 0.0) {
//                        MediaFrame *mf = (MediaFrame *) malloc(sizeof(MediaFrame));
//                        mf->frame = (AVFrame *) malloc(sizeof(AVFrame));
//                        mf->next = NULL;
//                        mf->pts = video_fps * (++video_frame);
//                        memcpy(mf->frame, pFrame, sizeof(AVFrame));
//                        frame_queue_put(frameQueue, mf);
                    AVFrame *frame = getFrame();
                    memcpy(frame, pFrame, sizeof(AVFrame));
                    addPreviewFrame(frame);
//                    }
                    if (cutFlag == 1) {
                        cutFlag = 2;
                    }
//                    LOGE("decode success");
//                    if(cutFlag==2||cutFlag==3){
//                        saveFrame(pFrame);
//                    }
                } else {
                    LOGE("decode video fail finish=%d,ret=%d", frameFinished, ret);
                }
                if (cutFlag == 2 || cutFlag == 3) {
                    savePkt(&packet);
                }
            }
//            LOGE("stream_index=" +pFrame->key_frame);
            av_frame_unref(pFrame);
            av_packet_unref(&packet);
//            LOGE("read pkt success ret=%d",ret);
        } else {
            LOGE("read pkt fail ret=%d", ret);
            break;
        }
    }

//    av_free(buffer);
//    av_free(pFrameRGBA);

    // Free the YUV frame
//    av_free(pFrame);

    // Close the codecs

    // Close the video file
    if (pFormatCtx != NULL)
        avformat_close_input(&pFormatCtx);
    if (pCodecCtx != NULL) {
        avcodec_close(pCodecCtx);
    }

//    avformat_free_context(pFormatCtx);
    if (currentState != STOP && needRestart == true) {
        goto restart;
    }
    currentState = STOP;
    LOGE("decodethread end");
}

int count = 1;

void FfmpegPlayer::displayVideo() {
    ANativeWindow_Buffer windowBuffer;
    AVFrame *pFrame_out = av_frame_alloc();
    LOGE("start display tid=%d", gettid());
    while (this->currentState != STOP) {
        if (nativeWindow == NULL) { continue; }
//        MediaFrame *frame = NULL;
//        LOGE("display while state = %d",this->currentState);
//        frame = frame_queue_get(frameQueue);
        AVFrame *frame = waitPreviewFrame();
        if (frame) {
            if (video_start_time == 0.0) {
                video_start_time = clock();
                notifyMediaInfo(START_PLAY, "start play");
            }
            currentState = PLAYING;
            if (screenshot_state == 1) {
                notifyScreenData(frame);
                screenshot_state = 0;
            }
            if (count++ < 10) {
                char path[50];
                sprintf(path, "/mnt/sdcard/Pictures/%d.yuv", count);
                int fd = open(path, O_RDWR | O_CREAT);
                int height = 720;
                int width = 1280;
                write(fd, frame->data[0], width * height);
                write(fd, frame->data[1], width * height / 4);
                write(fd, frame->data[2], width * height / 4);
                close(fd);
            }
            //Sync to machine clock
            long frame_timeout = (video_start_time + frame->pts) - clock();
//            LOGE("frame_timeout=%f", frame_timeout);
//            if (frame_timeout > 0.0) {
//                usleep(frame_timeout * 1000);
//            }
            if (this->isLiving) {
                notifyFrame(frame);
            }

            try {

                int lock = ANativeWindow_lock(nativeWindow, &windowBuffer, 0);

                if (lock >= 0) {
                    // 格式转换
                    int scale = sws_scale(sws_ctx, (uint8_t const *const *) pFrame_out->data,
                                          pFrame_out->linesize, 0, videoHeight,
                                          pFrameRGBA->data, pFrameRGBA->linesize);
                    LOGE("sws_scale = %d", scale);
                    // 获取stride
                    uint8_t *dst = (uint8_t *) windowBuffer.bits;
                    int dstStride = windowBuffer.stride * 4;
                    uint8_t *src = (pFrameRGBA->data[0]);
                    int srcStride = pFrameRGBA->linesize[0];

                    // 由于window的stride和帧的stride不同,因此需要逐行复制
                    for (int h = 0; h < videoHeight; h++) {
                        memcpy(dst + h * dstStride, src + h * srcStride, srcStride);
                    }

                    ANativeWindow_unlockAndPost(nativeWindow);
                } else {
                    LOGE("lock nativiwindow fail %d", lock);
                }
                av_frame_unref(pFrame_out);
            } catch (exception ex) {
                LOGE("exception ex=%s", ex.what());
            }
            recycleFrame(frame);
        }
    }
    LOGE("stop display %d", currentState);
//    frame_queue_clear(frameQueue);
//    free(frameQueue);
//    free(&windowBuffer);
//    free(nativeWindow);
    try {
        ANativeWindow_release(nativeWindow);
    } catch (exception ex) {
        LOGE("release nativewindow error");
    }
    nativeWindow = NULL;
    notifyMediaInfo(STOP, "play end");
    LOGE("play end");
}

void FfmpegPlayer::notifyFrame(AVFrame *frame) {
    int len = frame->height * frame->width * 3 / 2;
    if (buf) {
        if (sizeof(buf) != len) {
            delete buf;
            buf = new char[len];
        }
    } else {
        buf = new char[len];
    }
    memset(buf, 0, frame->height * frame->width * 3 / 2);
    int height = frame->height;
    int width = frame->width;
    printf("decode video ok\n");
    int a = 0, i;
    for (i = 0; i < height; i++) {
        memcpy(buf + a, frame->data[0] + i * frame->linesize[0], width);
        a += width;
    }
    for (i = 0; i < height / 2; i++) {
        memcpy(buf + a, frame->data[1] + i * frame->linesize[1], width / 2);
        a += width / 2;
    }
    for (i = 0; i < height / 2; i++) {
        memcpy(buf + a, frame->data[2] + i * frame->linesize[2], width / 2);
        a += width / 2;
    }
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            jbyteArray jbyteArray = _env->NewByteArray(len);
            _env->SetByteArrayRegion(jbyteArray, 0, len, (jbyte *) buf);
            _env->CallVoidMethod(mListerner, updateframe_mid, jbyteArray, width, height);
        }
        jvm->DetachCurrentThread();
    }
//    delete buf;
}

void FfmpegPlayer::notifyVideoSize(int width, int height) {
    if (!jvm)
        return;
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            _env->CallVoidMethod(mListerner, updateframe_mid, NULL, width, height);
        }
        jvm->DetachCurrentThread();
    }
}

void FfmpegPlayer::notifyMediaInfo(States state, const char *message) {
    LOGE("%s", message);
    if (!jvm)
        return;
    currentState = state;
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            jstring jmsg = _env->NewStringUTF(message);
            _env->CallVoidMethod(mListerner, mid, currentState, jmsg);
        }
        jvm->DetachCurrentThread();
    }
}

void FfmpegPlayer::setListener(JNIEnv *env, jobject listener) {
    mListerner = env->NewGlobalRef(listener);
    env->GetJavaVM(&jvm);
//    if(jvm->GetEnv((void **) &_env, JNI_VERSION_1_6)>=0){
//        LOGE("int env sucess");
//    }
    listnerclass = env->GetObjectClass(listener);
    mid = env->GetMethodID(listnerclass, "onInfoUpdate",
                           "(ILjava/lang/String;)V");
    updateframe_mid = env->GetMethodID(listnerclass, "onUpdateFrame",
                                       "([BII)V");
    screenshotdata_mid = env->GetMethodID(listnerclass, "onScreenshotData",
                                          "([BIILjava/lang/String;)V");

    screenshotdata_mid = env->GetMethodID(listnerclass, "onScreenshotData",
                                          "([BIILjava/lang/String;)V");
}

void FfmpegPlayer::saveFrame(AVFrame *frame) {
    if (av_interleaved_write_uncoded_frame(o_fmt_ctx, 0, frame) >= 0) {
        LOGE("save frame success");
    }
    if (cutFlag == 3) {
        av_write_trailer(o_fmt_ctx);

        avcodec_close(o_fmt_ctx->streams[0]->codec);
        av_freep(&o_fmt_ctx->streams[0]->codec);
        av_freep(&o_fmt_ctx->streams[0]);

        avio_close(o_fmt_ctx->pb);
        av_free(o_fmt_ctx);
        cutFlag = 4;
        LOGE("cut end");
    }
}

void FfmpegPlayer::savePkt(AVPacket *i_pkt) {
    if (pts < 0 || dts < 0) {
        pts = i_pkt->pts;
        dts = i_pkt->dts;
    }
    i_pkt->pts = av_rescale_q_rnd(i_pkt->pts - pts, i_video_stream->time_base,
                                  o_video_stream->time_base, AV_ROUND_NEAR_INF);
    i_pkt->dts = av_rescale_q_rnd(i_pkt->dts - dts, i_video_stream->time_base,
                                  o_video_stream->time_base, AV_ROUND_NEAR_INF);
    i_pkt->duration = (int) av_rescale_q(i_pkt->duration, i_video_stream->time_base,
                                         o_video_stream->time_base);
    i_pkt->pos = -1;
//    i_pkt->flags |= AV_PKT_FLAG_KEY;
//    pts = i_pkt->pts;
//    i_pkt->pts += last_pts;
//    dts = i_pkt->dts;
//    i_pkt->dts += last_dts;
//    i_pkt->stream_index = 0;

    //printf("%lld %lld\n", i_pkt.pts, i_pkt.dts);
    static int num = 1;
    printf("frame %d\n", num++);
    int re = av_interleaved_write_frame(o_fmt_ctx, i_pkt);
    if (re >= 0) {
        LOGE("save pkt success");
    } else {
        LOGE("save pkt faile code = %d", re);
    }
    if (cutFlag == 3) {
        av_write_trailer(o_fmt_ctx);

        avcodec_close(o_fmt_ctx->streams[0]->codec);
        av_freep(&o_fmt_ctx->streams[0]->codec);
        av_freep(&o_fmt_ctx->streams[0]);

        avio_close(o_fmt_ctx->pb);
        av_free(o_fmt_ctx);
        cutFlag = 4;
        LOGE("cut end");
    }
}

void FfmpegPlayer::stopCut() {
    LOGE("stop cut");
    cutFlag = 3;
}

void FfmpegPlayer::setupSurface(JNIEnv *env, jobject surface, int width, int height) {
    nativeWindow = ANativeWindow_fromSurface(env, surface);
//    ANativeWindow_acquire(nativeWindow);
//    ANativeWindow_setBuffersGeometry(nativeWindow,videoWidth,videoHeight,WINDOW_FORMAT_RGBA_8888);
}

void FfmpegPlayer::setUrl(const char *url) {
    this->url = url;
}

void FfmpegPlayer::restart() {
    needRestart = true;
}

States FfmpegPlayer::getCurrentState() {
    return this->currentState;
}

int FfmpegPlayer::startScreenshot(char *url) {
    if (this->screenshot_state == 1) {
        return -1;
    }
    LOGE("start screenshot");
    this->screenshot_url = url;
    this->screenshot_state = 1;
    LOGE("screenshot state = %d", screenshot_state);
    return 1;
}

void
convert_yuv420_to_nv21(unsigned char *dest, const unsigned char *source, int width, int height) {
    int area = height * width;
    int sqarea = area >> 3;
    int qarea = area >> 2;


    int count = sqarea;
    const unsigned short *su = (const unsigned short *) (source + area);
    const unsigned short *sv = (const unsigned short *) (source + area + qarea);
    unsigned int *uv = (unsigned int *) (dest + area);

/* copy y as is */
    memcpy(dest, source, area);


    do {
        unsigned int u = *su++;
        unsigned int v = *sv++;

        *uv++ = (((u & 0x00FF) << 8) |
                 ((u & 0xFF00) << 16) |
                 ((v & 0x00FF)) |
                 ((v & 0xFF00) << 8));
    } while (--count);

}

void FfmpegPlayer::notifyScreenData(AVFrame *frame) {
    int len = frame->height * frame->width * 3 / 2;
    LOGE("start notifyscreendata");

    unsigned char *buf = new unsigned char[len];
    unsigned char *nv21buf = new unsigned char[len];
    memset(buf, 0, frame->height * frame->width * 3 / 2);
    int height = frame->height;
    int width = frame->width;
    printf("decode video ok\n");

    int a = 0, i;
    for (i = 0; i < height; i++) {
        memcpy(buf + a, frame->data[0] + i * frame->linesize[0], width);
        a += width;
    }
    for (i = 0; i < height / 2; i++) {
        memcpy(buf + a, frame->data[1] + i * frame->linesize[1], width / 2);
        a += width / 2;
    }
    for (i = 0; i < height / 2; i++) {
        memcpy(buf + a, frame->data[2] + i * frame->linesize[2], width / 2);
        a += width / 2;
    }
    convert_yuv420_to_nv21(nv21buf, buf, width, height);
    JNIEnv *_env = NULL;
    if (jvm->AttachCurrentThread(&_env, NULL) >= 0) {
        if (_env != NULL && mListerner != NULL) {
            jbyteArray jbyteArray = _env->NewByteArray(len);
            _env->SetByteArrayRegion(jbyteArray, 0, len, (jbyte *) nv21buf);
            jstring path = _env->NewStringUTF(screenshot_url);
            _env->CallVoidMethod(mListerner, screenshotdata_mid, jbyteArray, width, height, path);
        }
        jvm->DetachCurrentThread();
    }
    LOGE("on end notify screen data");
    delete buf;
}

AVFrame *FfmpegPlayer::getFrame() {
    AVFrame *frame = NULL;
    pthread_mutex_lock(&pool_mutex);
    {
        if (!mFramePool->isEmpty()) {
            frame = mFramePool->last();
        }
    }
    pthread_mutex_unlock(&pool_mutex);
    if UNLIKELY(!frame) {
        LOGW("allocate new frame");
//        frame = av_frame_alloc();
        frame = (AVFrame *) malloc(sizeof(AVFrame));
    }
    return frame;
}

void FfmpegPlayer::recycleFrame(AVFrame *frame) {
    pthread_mutex_lock(&pool_mutex);
    if (LIKELY(mFramePool->size() < MAX_POOL_SIZE)) {
        mFramePool->put(frame);
        frame = NULL;
    }
    pthread_mutex_unlock(&pool_mutex);
    if (UNLIKELY(frame)) {
        free(frame);
    }
}

void FfmpegPlayer::addPreviewFrame(AVFrame *frame) {
    pthread_mutex_lock(&preview_mutex);
    if (previewFrames->size() < MAX_FRAME_SIZE) {
        previewFrames->put(frame);
        frame = NULL;
        pthread_cond_signal(&preview_cond);
    } else {
        previewFrames->put(frame);
        frame = previewFrames->remove(0);
    }
    pthread_mutex_unlock(&preview_mutex);
    if (frame) {
        recycleFrame(frame);
    }
}

AVFrame *FfmpegPlayer::waitPreviewFrame() {
    AVFrame *frame = NULL;
    pthread_mutex_lock(&preview_mutex);
    {
//        if (!previewFrames->size()) {
//            pthread_cond_wait(&preview_cond, &preview_mutex);
//        }
        if (LIKELY(previewFrames->size() > 0)) {
            frame = previewFrames->remove(0);
        }
    }
    pthread_mutex_unlock(&preview_mutex);
    return frame;
}

int FfmpegPlayer::createDecoder(int videoType, int width, int height) {
    AVCodecID codecID = AV_CODEC_ID_H264;
    av_register_all();
    switch (videoType) {
        case VIDEO_TYPE_H264:
            codecID = AV_CODEC_ID_H264;
            break;
        default:
            codecID = AV_CODEC_ID_H264;
            break;
    }
    AVCodec *codec = avcodec_find_decoder(codecID);
    if (codec == NULL) {
        LOGE("find codec fail");
        return -1;
    }
    pCodecCtx = avcodec_alloc_context3(codec);
    pCodecCtx->width = width;
    pCodecCtx->height = height;
//    pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;
//    pCodecCtx->time_base = {25, 1};

    if (avcodec_open2(pCodecCtx, codec, NULL) < 0) {
        LOGE("open codec error\r\n");
        return -1;
    }

    pFrame = av_frame_alloc();

    pPacket = av_packet_alloc();
    av_init_packet(pPacket);
//    // 用于渲染
//    pFrameRGBA = av_frame_alloc();
//    if (pFrameRGBA == NULL) {
//        LOGE("Could not allocate video frame.");
//        return -1;
////        return ;
//    }

    videoWidth = width;
    videoHeight = height;
    // Determine required buffer size and allocate buffer
//    // buffer中数据就是用于渲染的,且格式为RGBA
//    int numBytes = av_image_get_buffer_size(AV_PIX_FMT_RGBA, width, height, 1);
//    uint8_t *buffer = (uint8_t *) av_malloc(numBytes * sizeof(uint8_t));
//    av_image_fill_arrays(pFrameRGBA->data, pFrameRGBA->linesize, buffer, AV_PIX_FMT_RGBA,
//                         pCodecCtx->width, pCodecCtx->height, 1);

    // 由于解码出来的帧格式不是RGBA的,在渲染之前需要进行格式转换
//    sws_ctx = sws_getContext(pCodecCtx->width,
//                             pCodecCtx->height,
//                             pCodecCtx->pix_fmt,
//                             pCodecCtx->width,
//                             pCodecCtx->height,
//                             AV_PIX_FMT_RGBA,
//                             SWS_BILINEAR,
//                             NULL,
//                             NULL,
//                             NULL);

    return 0;
}

void FfmpegPlayer::clearPreviewFrame() {
    pthread_mutex_lock(&preview_mutex);
    {
        for (int i = 0; i < previewFrames->size(); i++)
            recycleFrame(previewFrames->operator[](i));
        previewFrames->clear();
    }
    pthread_mutex_unlock(&preview_mutex);
}

void FfmpegPlayer::clear_pool() {
    pthread_mutex_lock(&pool_mutex);
    {
        const int n = mFramePool->size();
        for (int i = 0; i < n; i++) {
            AVFrame *frame = mFramePool->operator[](i);
            if (frame) {
                free(frame);
//                av_frame_unref(frame);
//                av_frame_free(&frame);
            }
        }
        mFramePool->clear();
    }
    pthread_mutex_unlock(&pool_mutex);
}

int FfmpegPlayer::decodeFrame(char *data, int len) {
//    LOGE("decodeFrame start");
    AVFrame *frame = NULL;

    pPacket->size = len;
    pPacket->data = (uint8_t *) data;

//    LOGE("decode frame len=%d", len);
    int got_frame = 0;
    int nRet = 0;
    if (pPacket->size > 0) {
//        avcodec_send_packet(pCodecCtx, pPacket);
        pthread_mutex_lock(&decode_mutex);
        if (!pFrame) {
            pthread_mutex_unlock(&decode_mutex);
            return -1;
        }
        nRet = avcodec_decode_video2(pCodecCtx, pFrame, &got_frame, pPacket);
        if (nRet < 0) {
            LOGE("avcodec_decode_video2:%d\r\n", nRet);
            av_packet_unref(pPacket);
            pthread_mutex_unlock(&decode_mutex);

            return nRet;
        }

        if (got_frame) {
            frame = getFrame();
            memcpy(frame, pFrame, sizeof(AVFrame));
            addPreviewFrame(frame);
//            LOGE("gotframe=%d,nret=%d", got_frame, nRet);
        }

        pthread_mutex_unlock(&decode_mutex);
    }
    av_packet_unref(pPacket);
//    LOGE("decodeFrame end");
    return 0;
}

void FfmpegPlayer::initGles(int pWidth, int pHeight) {
    LOGE("init() gles");
    pthread_mutex_lock(&preview_mutex);
    instance = (Instance *) malloc(sizeof(Instance));
    memset(instance, 0, sizeof(Instance));
    //	1.初始化着色器
    GLuint shaders[2] = {0};
    shaders[0] = initShader(codeVertexShader, GL_VERTEX_SHADER);
    shaders[1] = initShader(codeFragShader, GL_FRAGMENT_SHADER);
    instance->pProgram = initProgram(shaders, 2);
    instance->maMVPMatrixHandle = glGetUniformLocation(instance->pProgram, "uMVPMatrix");
    instance->maPositionHandle = glGetAttribLocation(instance->pProgram, "aPosition");
    instance->maTexCoorHandle = glGetAttribLocation(instance->pProgram, "aTexCoor");
    instance->myTextureHandle = glGetUniformLocation(instance->pProgram, "yTexture");
    instance->muTextureHandle = glGetUniformLocation(instance->pProgram, "uTexture");
    instance->mvTextureHandle = glGetUniformLocation(instance->pProgram, "vTexture");
    instance->angel = 0;
    //	2.初始化纹理
    //		2.1生成纹理id
    glGenTextures(1, &instance->yTexture);
    glGenTextures(1, &instance->uTexture);
    glGenTextures(1, &instance->vTexture);
    LOGE("init() yT = %d, uT = %d, vT = %d.", instance->yTexture, instance->uTexture,
         instance->vTexture);
    LOGE("%s %d error = %d", __FILE__, __LINE__, glGetError());
    //	3.分配Yuv数据内存
    instance->yBufferSize = sizeof(char) * 1080 * 1920;
    instance->uBufferSize = sizeof(char) * 1080 / 2 * 1920 / 2;
    instance->vBufferSize = sizeof(char) * 1080 / 2 * 1920 / 2;
    instance->yBuffer = (signed char *) (char *) malloc(instance->yBufferSize);
    instance->uBuffer = (signed char *) (char *) malloc(instance->uBufferSize);
    instance->vBuffer = (signed char *) (char *) malloc(instance->vBufferSize);
    memset(instance->yBuffer, 0, instance->yBufferSize);
    memset(instance->uBuffer, 0, instance->uBufferSize);
    memset(instance->vBuffer, 0, instance->vBufferSize);
    instance->pHeight = pHeight;
    instance->pWidth = pWidth;
    instance->state = 0;
    LOGE("width = %d, height = %d", instance->pWidth, instance->pHeight);
    //清理背景
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    //允许深度检测
//	glEnable(GL_DEPTH_TEST);
    LOGE("%s %d error = %d", __FILE__, __LINE__, glGetError());
    pthread_mutex_unlock(&preview_mutex);
}

void FfmpegPlayer::changeESLayout(int width, int height) {
    pthread_mutex_lock(&preview_mutex);
    if (instance != 0) {
        instance->vWidth = width;
        instance->vHeight = height;
    }
    unsigned int eW = width, eH = height;

    glViewport(0, 0, eW, eH);
    pthread_mutex_unlock(&preview_mutex);
}


int FfmpegPlayer::drawESFrame() {
    jint ret = -1;
    if (instance != NULL) {
        AVFrame *yuv_frame = waitPreviewFrame();
//        LOGE("on draw es frame");
        if (yuv_frame != NULL) {
            memcpy(instance->yBuffer, yuv_frame->data[0], instance->yBufferSize);
            memcpy(instance->uBuffer, yuv_frame->data[1],
                   instance->uBufferSize);
            memcpy(instance->vBuffer, yuv_frame->data[2],
                   instance->vBufferSize);
            instance->state = 1;
//            if (instance->angel > 359.0) {
//                instance->angel = 0.0;
//            }
//            instance->angel++;
            recycleFrame(yuv_frame);
            ret = 0;
        }
        if (instance->state == 1) {
            drawFrame(instance);
        }
//        LOGE("ydata=%x,%x,%x,%x", ((char *) instance->yBuffer)[0],
//             ((char *) instance->yBuffer)[1], ((char *) instance->yBuffer)[2],
//             ((char *) instance->yBuffer)[3]);

    }
    return ret;
}

jint FfmpegPlayer::drawYuv(char *data, jint size) {
    jint ret = -1;
    if (instance != NULL) {
        if (data != NULL) {
            memcpy(instance->yBuffer, data, instance->yBufferSize);
            memcpy(instance->uBuffer, data, instance->uBufferSize);
            memcpy(instance->vBuffer, data, instance->vBufferSize);
            instance->state = 1;
            ret = 0;
        }
        if (instance->state == 1) {
            drawFrame(instance);
        }
    }
    return ret;
}
