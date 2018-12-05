
#ifndef _HBX_FISHEYE_PUBLIC_H_
#define _HBX_FISHEYE_PUBLIC_H_

//#define _WIN_
#define _ANDROID_
//#define _IPHONE_ 
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

#define HBX_LOG_TAG  "HBXFE"

#ifdef _WIN_
#ifdef __cplusplus
extern "C" {
#endif
#include "../list/hbxList.h"
#ifdef __cplusplus
}
#endif
#include "../stdafx.h"
#if 0
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
#endif

#ifdef _IPHONE_
#ifdef __cplusplus
extern "C" {
#endif
#include "hbxList.h"
#ifdef __cplusplus
}
#endif

#define msleep(ms) usleep(1000*(ms))
#endif


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
#include "hbxList.h"
#ifdef __cplusplus
}
#endif
#define msleep(ms) usleep(1000*(ms))
#endif


#define RELEASE_BUFFER(buffer) if(buffer){ \
        free(buffer); \
        buffer = NULL; \
    }

#define DELETE_BUFFER(buffer) if(buffer){ \
        delete buffer; \
        buffer = NULL; \
    }

#define _DATABLOCK_MAX_  132
#define _DATAHEADER_MAX_ 20
#define _DATATAIL_MAX_   15

//
struct _HBX_FILE_INFO_INDEX_ {
    unsigned char nIndexType;
    unsigned char nPos[4];
};

#define PI 3.141592653589793f
//
enum {
    TAG_TAIL = 0, TAG_FILETYPE, TAG_EYEFISHID, TAG_VERSION, TAG_INDEX, TAG_CALIBRATION, TAG_SN
};

#define  _UCHAR_TO_UINT(chData, value) { \
value = chData[0]; \
value = (value << 8) + chData[1]; \
value = (value << 8) + chData[2]; \
value = (value << 8)  + chData[3]; \
}

#define  _UCHAR_TO_UINT_LIGO(chData, value) { \
value = chData[3]; \
value = (value << 8) + chData[2]; \
value = (value << 8) + chData[1]; \
value = (value << 8)  + chData[0]; \
}

#define  UINT_TO_UCHAR_(chData, value) { \
chData[0] = (value >> 24) & 0xff; \
chData[1] = (value >> 16) & 0xff; \
chData[2] = (value >> 8) & 0xff; \
chData[3] = value  & 0xff; \
}

using namespace std;
#endif 