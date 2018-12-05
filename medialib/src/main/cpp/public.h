
#ifndef _PUBLIC_H_
#define _PUBLIC_H_

//#define _WIN_ 
#define _ANDROID_
//#define _IOS_ 
//#define _LINUX_ 



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <assert.h>

#include <fcntl.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <time.h>

#include <list>

#define HBX_LOG_TAG  "HBXMP"


#ifdef _WIN_
#ifdef __cplusplus
extern "C" {
#endif
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
#include "libavutil/opt.h"
#include "libavcodec/jni.h"
#include "libswresample/swresample.h"
#include "hbxList.h"
#ifdef __cplusplus
}
#endif
#include "stdafx.h"
#define HBXLOG(fmt, ...)  TRACE( "HBXMP: " fmt, ##__VA_ARGS__)
#endif

#ifdef _IOS_
#ifdef __cplusplus
extern "C" {
#endif
#include "avcodec.h"
#include "avformat.h"
#include "swscale.h"
#include "imgutils.h"
#include "opt.h"
#include "jni.h"
#include "swresample.h"
#include "swscale.h"
#include "hbxList.h"
#ifdef __cplusplus
}
#endif
#include <OpenAL/OpenAL.h>
#include <pthread.h>
#include <sys/time.h>
#include <unistd.h>

#define msleep(ms) usleep(1000*(ms))

#define HBXLOG(fmt, ...) printf("HBXMP: " fmt, ##__VA_ARGS__)
#endif

#define MIN_VALUE 1e-8
#define IS_DOUBLE_ZERO(d) (abs(d) < MIN_VALUE)

//
#define _STATUS_MEDIA_STOP_ 0
#define _STATUS_MEDIA_PLAY_  (_STATUS_MEDIA_STOP_ + 1)
#define _STATUS_MEDIA_PAUSE_ (_STATUS_MEDIA_PLAY_ + 1)

//
#define _STATUS_READ_STOP_ 0
#define _STATUS_READ_PLAY_  (_STATUS_READ_STOP_ + 1)
#define _STATUS_READ_PAUSE_ (_STATUS_READ_PLAY_ + 1)

//
#define _STATUS_THREAD_NEW_ 0
#define _STATUS_THREAD_RUN_   (_STATUS_THREAD_NEW_ + 1)
#define _STATUS_THREAD_PAUSE_ (_STATUS_THREAD_RUN_ + 1)
#define _STATUS_THREAD_EXIT_  (_STATUS_THREAD_PAUSE_ + 1)

//
#define _TYPE_MEDIA_FROM_  0
#define _HTTP_TYPE_MEDIA_FROM_    (_TYPE_MEDIA_FROM_ + 1)
#define _RTSP_TYPE_MEDIA_FROM_    (_HTTP_TYPE_MEDIA_FROM_ + 1)
#define _LOCAL_TYPE_MEDIA_FROM_   (_RTSP_TYPE_MEDIA_FROM_ + 1)
//
#define _NO_CACHE_  0
#define _NET_TYPE_CACHE_    (_NO_CACHE_ + 1)
#define _YUV_TYPE_CACHE_    (_NET_TYPE_CACHE_ + 1)

//
#define _INFO_STOP_TAG_  0
#define _INFO_PLAY_TAG_  1
#define _INFO_PAUSE_TAG_ 2
#define _INFO_ERROR_TAG_ 3


#define _MAX_BUFFER_FRAME_     150
#define _MIN_BUFFER_FRAME_     60

#define _LOW_BUFFER_YUV_      10  //2
#define _MID_BUFFER_YUV_      15  //3
#define _MAX_BUFFER_YUV_      30  //10

#define _VIDEO_INDEX_STREAM_    0
#define _AUDIO_INDEX_STREAM_    1
#define _YUV_INDEX_STREAM_      3

#ifdef _ANDROID_

#include <malloc.h>
#include <unistd.h>
#include <sys/time.h>
#include <unistd.h>
#include <sys/time.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <asm/types.h>
#include <linux/types.h>
#include <getopt.h>
#include <pthread.h>
#include <time.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C" {
#endif
#include <jni.h>
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libswscale/swscale.h"
#include "include/libavutil/imgutils.h"
#include "include/libavutil/opt.h"
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <unistd.h>
#include <pthread.h>
#include "include/libavcodec/jni.h"
#include "libswresample/swresample.h"
#include "hbxList.h"
#ifdef __cplusplus
}
#endif

#define HBXLOG(fmt, ...)  __android_log_print(ANDROID_LOG_ERROR, HBX_LOG_TAG, fmt, ##__VA_ARGS__)
#define msleep(ms) usleep(1000*(ms))
#endif

#ifdef _WIN_
#include <windows.h>
#include <malloc.h>
static inline int msleep(int ms)
{
#define WAITABLETIMER_MS_INTERVAL (1000*10)//waitable timer interval 100ns(s,ms,us,ns,ps)

    HANDLE tmr = NULL;
    LARGE_INTEGER to;

    if ((tmr = CreateWaitableTimer(NULL, FALSE, NULL)) == NULL) {//synchronization timer

        return -1;
    }

    to.QuadPart = (long long)(-(ms*WAITABLETIMER_MS_INTERVAL));
    if (!SetWaitableTimer(tmr, &to, 0, NULL, NULL, FALSE)) {
        CloseHandle(tmr);
        return -1;
    }

    if (WaitForSingleObject(tmr, INFINITE) != WAIT_OBJECT_0) {
        CloseHandle(tmr);
        return -1;
    }

    CloseHandle(tmr);

    return 0;
}
#endif

#define RELEASE_BUFFER(buffer) if(buffer){ \
        free(buffer); \
        buffer = NULL; \
    }

#define DELETE_BUFFER(buffer) if(buffer){ \
        delete buffer; \
        buffer = NULL; \
    }

#define _DELAY_TIME_        5
#define _DELAY_DTS_TIME_   500

struct MediaInfo {
    int nWidth;
    int nHeight;
    int nDuration;
    int spsppslength;
    int frame_rate;
    int av_codec_id;
    ///
    int audio_sample_fmt;
    int audio_sample_rate;
    int audio_ch_layout;
    int audio_channels;
    int audio_codec_id;
    int decodecstatus;
    int spslength;
    int ppslength;
    unsigned char sps[64];
    //
    long long first_pts;
public:
    void Init() {
        nWidth = 0;
        nHeight = 0;
        nDuration = 0;
        spsppslength = 0;

        av_codec_id = -1;
        ///
        audio_sample_fmt = -1;
        audio_sample_rate = 0;
        audio_ch_layout = -1;
        audio_channels = 0;
        audio_codec_id = -1;
        //
        first_pts = 0;
        decodecstatus = 0;
        spslength = 0;
        ppslength = 0;
    }
};

using namespace std;
#endif 