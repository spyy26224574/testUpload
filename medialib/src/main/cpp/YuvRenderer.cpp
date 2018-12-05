//
// Created by huangxy on 2018/1/17.
//

#include "YuvRenderer.h"

YuvRenderer::YuvRenderer() {
    init();
}

YuvRenderer::~YuvRenderer() {
    pthread_mutex_destroy(&preview_mutex);
    if (instance) {
        free(instance);
        instance = NULL;
    }
}

void YuvRenderer::init() {
    pthread_mutex_init(&preview_mutex, NULL);
}

void YuvRenderer::initGles() {
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
//    LOGE("init() yT = %d, uT = %d, vT = %d.", instance->yTexture, instance->uTexture,
//         instance->vTexture);
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
    instance->pHeight = 1920;
    instance->pWidth = 1080;
    instance->state = 0;
//    LOGE("width = %d, height = %d", instance->pWidth, instance->pHeight);
    //清理背景
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    //允许深度检测
//	glEnable(GL_DEPTH_TEST);
    LOGE("%s %d error = %d", __FILE__, __LINE__, glGetError());
    pthread_mutex_unlock(&preview_mutex);
}

void YuvRenderer::changeESLayout(int width, int height) {
    pthread_mutex_lock(&preview_mutex);
    if (instance != 0) {
        instance->vWidth = width;
        instance->vHeight = height;
    }
    unsigned int eW = width, eH = height;

    glViewport(0, 0, eW, eH);

    pthread_mutex_unlock(&preview_mutex);
}

int YuvRenderer::drawYuv(char *data, int size, int width, int height) {
    jint ret = -1;
    if (instance != NULL) {
        if (data != NULL) {
            instance->pHeight = height;
            instance->pWidth = width;
            instance->yBufferSize = width * height;
            instance->uBufferSize = width / 2 * height / 2;
            instance->vBufferSize = width / 2 * height / 2;
            memcpy(instance->yBuffer, data, instance->yBufferSize);
            memcpy(instance->uBuffer, &data[instance->yBufferSize], instance->uBufferSize);
            memcpy(instance->vBuffer, &data[instance->yBufferSize + instance->uBufferSize],
                   instance->vBufferSize);
            instance->state = 1;
            if (instance->angel > 359.0) {
                instance->angel = 0.0;
            }
            instance->angel++;
            ret = 0;
        }
        if (instance->state == 1) {
            drawFrame(instance);
        }
    }

    return ret;
}