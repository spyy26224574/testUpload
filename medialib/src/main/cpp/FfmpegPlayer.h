//
// Created by admin on 2016/11/5.
//

#ifndef FFMPEGPLAYER_FFMPEGPLAYER_H
#define FFMPEGPLAYER_FFMPEGPLAYER_H

#define MAX_FRAME_SIZE 30
#define MAX_POOL_SIZE MAX_FRAME_SIZE+10

#define VIDEO_TYPE_H264 0x11

extern "C" {
#include <jni.h>
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
#include "include/libavutil/imgutils.h"
#include "include/libavutil/frame.h"
#include "include/libavutil/opt.h"
#include "include/libavfilter/avfilter.h"
#include "include/libavfilter/buffersink.h"
#include "include/libavfilter/buffersrc.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <unistd.h>
#include <fcntl.h>
#include <pthread.h>
#include "opengles/cameraShader.h"
}

#include "objectarray.h"
//#include "utilbase.h"

#include <thread>
#include <condition_variable>
#include <mutex>

using namespace std;

enum States {
    NONE,
    START_PREPARE,
    PREPRARING,
    PREPARED,
    START_BUFFER,
    BUFFRING,
    START_PLAY,
    PLAYING,
    PAUSE,
    STOP,
    ERROR
};
extern const char *codeVertexShader;
extern const char *codeFragShader;

class FfmpegPlayer {
public:

    void init();

    int createDecoder(int videoType, int width, int height);

    void setListener(JNIEnv *env, jobject listener);

    void setupSurface(JNIEnv *evn, jobject surface, int width, int height);

    void setUrl(const char *url);

    void start();

    void restart();

    void stop();

    void notifyMediaInfo(States state, const char *message);

    void notifyFrame(AVFrame *frame);

    void notifyVideoSize(int width, int height);

    void decodeVideo(const char *url);

    void displayVideo();

    void savePkt(AVPacket *pkt);

    void saveFrame(AVFrame *frame);

    int decodeFrame(char *data, int len);

    void stopCut();

    int startScreenshot(char *url);

    void notifyScreenData(AVFrame *pFrame);

    States getCurrentState();

    AVFrame *getFrame();

    void recycleFrame(AVFrame *frame);

    void addPreviewFrame(AVFrame *frame);

    AVFrame *waitPreviewFrame();

    void initGles(int width, int height);

    void changeESLayout(int width, int height);

    int drawESFrame();

    FfmpegPlayer();

    FfmpegPlayer(JNIEnv *env, jstring url, jobject surface);

    ~FfmpegPlayer();

    jint drawYuv(char *data, jint size);

private:
    const char *url;
    ANativeWindow *nativeWindow = NULL;
    struct SwsContext *sws_ctx;
    AVFrame *pFrameRGBA = NULL;
    AVFrame *pFrame = NULL;
    AVPacket *pPacket = NULL;
    int videoWidth = 0;
    int videoHeight = 0;
    int video_frame = 0;
    double video_fps, video_start_time = 0.0;
    States currentState = NONE;
    bool needRestart = false;
    jobject mListerner = NULL;
    JavaVM *jvm = NULL;
    jmethodID mid, updateframe_mid;
    jmethodID screenshot_mid;
    jmethodID screenshotdata_mid;
    jclass listnerclass;

    AVStream *i_video_stream;
    AVFormatContext *o_fmt_ctx;
    AVStream *o_video_stream;
    /**
     * 0为常态，1为准备剪切，2为开始剪切，3为停止剪切
     */
    int cutFlag = 0;

    int last_pts = 0;
    int last_dts = 0;

    int64_t pts = -1, dts = -1;

    char *buf = NULL;
    bool isLiving = false;

    char *screenshot_url;
    int screenshot_state = 0;

    ObjectArray<AVFrame *> *previewFrames;
    ObjectArray<AVFrame *> *mFramePool;

    void clear_pool();

    void clearPreviewFrame();

    pthread_mutex_t decode_mutex;
    pthread_mutex_t preview_mutex;
    pthread_cond_t preview_cond;
    pthread_mutex_t pool_mutex;

    AVCodecContext *pCodecCtx;

    struct Instance *instance = NULL;
};


#endif //FFMPEGPLAYER_FFMPEGPLAYER_H
