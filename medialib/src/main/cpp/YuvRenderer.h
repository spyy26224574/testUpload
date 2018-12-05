//
// Created by huangxy on 2018/1/17.
//

#ifndef ROADCAM_YUVRENDERER_H
#define ROADCAM_YUVRENDERER_H
extern "C" {
#include <jni.h>
#include <pthread.h>
#include "opengles/cameraShader.h"
}

#include "utilbase.h"

extern const char *codeVertexShader;
extern const char *codeFragShader;

class YuvRenderer {
public:
    YuvRenderer();

    ~YuvRenderer();

    void initGles();

    void changeESLayout(int width, int height);

    int drawYuv(char *data, int size, int width, int height);

private:
    void init();

    pthread_mutex_t preview_mutex;
    struct Instance *instance = NULL;
};

#endif //ROADCAM_YUVRENDERER_H
