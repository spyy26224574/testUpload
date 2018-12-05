//
// Created by huangxy on 2018/7/11.
//


#ifndef ROADCAM18710_H264TOJPG_H
#define ROADCAM18710_H264TOJPG_H
extern "C" {
#include <jni.h>
#include "libavcodec/avcodec.h"
#include "libavformat/avformat.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
}


int initDecoder();

void deInitDecoder();

int decodeFrame(char *data, int len, char *out_file);

#endif //ROADCAM18710_H264TOJPG_H
