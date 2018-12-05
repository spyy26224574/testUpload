#include "cameraShader.h"

//extern const float dataVertex[];
//extern const float dataTexCoor[];

//extern void printData(void* data, const int size, const char * name);
//void bindTexture();
#define UNIT 1
#define TEXTURE_COOR_UNIT 1

//顶点着色器脚本代码
const char *codeVertexShader = \
"attribute vec3 aPosition;							\n" \
"uniform mat4 uMVPMatrix;	 						\n" \
"attribute vec2 aTexCoor; 							\n" \
"varying vec2 vTexCoor;		 						\n" \
"void main() 										\n" \
"{ 													\n" \
"	gl_Position = uMVPMatrix * vec4(aPosition, 1); 	\n" \
" 	vTexCoor = aTexCoor;							\n" \
"} 													\n" \
;

//-------------MATH---------------
const char *codeFragShader = \
"precision mediump float;											\n" \
"uniform sampler2D yTexture; 										\n" \
"uniform sampler2D uTexture; 										\n" \
"uniform sampler2D vTexture; 										\n" \
"varying vec2 vTexCoor;												\n" \
"void main()														\n" \
"{																	\n" \
"	float y = texture2D(yTexture, vTexCoor).r;						\n" \
"	float u = texture2D(uTexture, vTexCoor).r;											\n" \
"	float v = texture2D(vTexture, vTexCoor).r;													\n" \
"	vec3 yuv = vec3(y, u, v);												\n" \
"	vec3 offset = vec3(16.0 / 255.0, 128.0 / 255.0, 128.0 / 255.0);								\n" \
"	mat3 mtr = mat3(1.0, 1.0, 1.0, -0.001, -0.3441, 1.772, 1.402, -0.7141, 0.001);						\n" \
"	vec4 curColor = vec4(mtr * (yuv - offset), 1);												\n" \
"	gl_FragColor = curColor;													\n" \
"}																	\n" \
;


//渲染顶点坐标数据
const float dataVertex[] =
        {
                -1 * UNIT, 1 * UNIT, 0,
                -1 * UNIT, -1 * UNIT, 0,
                1 * UNIT, 1 * UNIT, 0,
                1 * UNIT, -1 * UNIT, 0
        };
//渲染纹理坐标数据
const float dataTexCoor[] =
        {
                0 * TEXTURE_COOR_UNIT, 0 * TEXTURE_COOR_UNIT,
                0 * TEXTURE_COOR_UNIT, 1 * TEXTURE_COOR_UNIT,
                1 * TEXTURE_COOR_UNIT, 0 * TEXTURE_COOR_UNIT,
                1 * TEXTURE_COOR_UNIT, 1 * TEXTURE_COOR_UNIT
        };

void
drawFrame(void *ins) {
//	if(DEBUG)
//	{
//		LOGI_EU("%s", __FUNCTION__);
//	}
    //清除深度缓冲,颜色缓冲
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT);
    Instance *instance = (Instance *) ins;
    if (instance == 0) {
        LOGW_EU("%s Program is NULL return!", __FUNCTION__);
        return;
    }
    //选择着色程序
    glUseProgram(instance->pProgram);
    //传入变换矩阵数据
    //		1.初始化旋转矩阵
    float *maMVPMatrix = getRotateM(NULL, 0, instance->angel, 0, 0, 1);
    //		2.初始化观察矩阵
    float *lookAtM = setLookAtM(NULL, 0,
                                0, 0, 0,//float eyeX, float eyeY, float eyeZ,
                                0, 0, -1,//float centerX, float centerY, float centerZ,
                                0, 1, 0//float upX, float upY, float upZ
    );
//    //		3.透视投影矩阵
//    float radio = (float) instance->vWidth / (float) instance->vHeight;
//    float *projM = frustumM(NULL, 0,
//                            -radio, radio,//float left, float right,
//                            -1, 1,//float bottom, float top,
//                            1, 4//float near, GLfloat far
//    );
//    //		3.2整合矩阵
    matrixMM4(maMVPMatrix, lookAtM);
//    matrixMM4(maMVPMatrix, projM);
    //		4.传矩阵数据到顶点着色器
    glUniformMatrix4fv(instance->maMVPMatrixHandle, 1, GL_FALSE, maMVPMatrix);
//	//		5.释放
    free(maMVPMatrix);
    free(lookAtM);
//    free(projM);
    //传入顶点数据到着色器程序
    glVertexAttribPointer(instance->maPositionHandle,
                          3,//GLint size X Y Z
                          GL_FLOAT,//GLenum type
                          GL_FALSE,//GLboolean normalized
                          3 * 4,//GLsizei stride
                          dataVertex//const GLvoid * ptr
    );
    //传入顶点纹理坐标
    glVertexAttribPointer(instance->maTexCoorHandle,
                          2,//S T
                          GL_FLOAT,//GLenum type
                          GL_FALSE,//GLboolean normalized
                          2 * 4,//GLsizei stride
                          dataTexCoor//const GLvoid * ptr
    );
    //激活并绑定纹理
    bindTexture(GL_TEXTURE0, instance->yTexture, instance->pWidth, instance->pHeight,
                instance->yBuffer);
    bindTexture(GL_TEXTURE1, instance->uTexture, instance->pWidth / 2, instance->pHeight / 2,
                instance->uBuffer);
    bindTexture(GL_TEXTURE2, instance->vTexture, instance->pWidth / 2, instance->pHeight / 2,
                instance->vBuffer);
    glUniform1i(instance->myTextureHandle, 0);
    glUniform1i(instance->muTextureHandle, 1);
    glUniform1i(instance->mvTextureHandle, 2);
//	printData(instance->yBuffer, 20, "yBuffer 10 : ");
//	printData(instance->uBuffer, 20, "uBuffer 10 : ");
//	printData(instance->vBuffer, 20, "vBuffer 10 : ");
    //允许顶点数据数组
    glEnableVertexAttribArray(instance->maPositionHandle);
    glEnableVertexAttribArray(instance->maTexCoorHandle);
    //绘制纹理矩形
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
}

void
bindTexture(GLenum texture_n, GLuint texture_id, GLsizei width, GLsizei height,
            const void *buffer) {
//	LOGI_EU("texture_n = %x, texture_id = %d, width = %d, height = %d", texture_n, texture_id, width, height);
    //处理纹理
    //		2.绑定纹理
    glActiveTexture(texture_n);//eg:GL_TEXTURE0
    //		1.1绑定纹理id
    glBindTexture(GL_TEXTURE_2D, texture_id);
    //		2.3设置采样模式
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    //		1.2输入纹理数据
    glTexImage2D(GL_TEXTURE_2D,
                 0,//GLint level
                 GL_LUMINANCE,//GLint internalformat
                 width,//GLsizei width
                 height,// GLsizei height,
                 0,//GLint border,
                 GL_LUMINANCE,//GLenum format,
                 GL_UNSIGNED_BYTE,//GLenum type,
                 buffer//const void * pixels
    );
};
