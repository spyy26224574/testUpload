package com.ligo.medialib.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.ligo.medialib.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author huangxy
 * @date 2018/9/10 16:59.
 */
public class VideoRenderYuv implements GLSurfaceView.Renderer {
    private static final String TAG = VideoRenderYuv.class.getSimpleName();
    private final String mVertexShader =
            "uniform mat4 uMVPMatrix;\n" +
                    "attribute vec4 aPosition;\n" +
                    "attribute vec2 aTextureCoord;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "  gl_Position = uMVPMatrix * aPosition;\n" +
                    "  vTextureCoord = aTextureCoord;\n" +
                    "}\n";
    private final String mFragmentShader =
            "uniform sampler2D SamplerY;\n" +
                    "uniform sampler2D SamplerU;\n" +
                    "uniform sampler2D SamplerV;\n" +
                    "varying lowp vec2 vTextureCoord;\n" +
                    "void main() {\n" +
                    "mediump vec3 yuv;\n" +
                    "lowp vec3 rgb;\n" +
                    "yuv.x = texture2D(SamplerY, vTextureCoord).r;\n" +
                    "yuv.y = texture2D(SamplerU, vTextureCoord).r - 0.5;\n" +
                    "yuv.z = texture2D(SamplerV, vTextureCoord).r - 0.5;\n" +
                    "rgb = mat3(1,1,1,\n" +
                    "0,-0.39465,2.03211,\n" +
                    "1.13983,-0.58060,0) * yuv;\n" +
                    "gl_FragColor = vec4(rgb, 1);\n" +
                    "}\n";
    private static final int FLOAT_SIZE_BYTES = 4;
    private final Context mContext;
    private int maPositionHandle;
    private int maTextureHandle;
    private int mFrameWidth = 800, mFrameHeight = 800;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    public static final int TYPE_SRC = 0;
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_2_SCREEN = 2;
    public static final int TYPE_4_SCREEN = 3;
    public static final int TYPE_HEMISPHERE = 4;
    public static final int TYPE_CYLINDER = 5;
    private ByteBuffer yBuffer, uBuffer, vBuffer;
    private int[] mTextureYUV = new int[3];
    private final Object YUV_LOCK = new Object();

    /**
     * 0-为普通状态，1-为进入半球中心状态
     */
    private ShowTypeProxy mShowTypeProxy;
    private IShowType mShowTypSrc, mShowTypeCircle, mShowType2Screen, mShowType4Screen, mShowTypeHemishpere, mShowTypeCylinder;
    private int curentType;

    public VideoRenderYuv(Context context) {
        mContext = context;
        mShowTypeProxy = new ShowTypeProxy();
    }

    public void initVertexData(int width, int height) {
//        Calibration.CalibrationBean calibration = CalibrationManager.getCalibration(mContext, width, height);
//        if (calibration == null) {
//            onChangeShowType(TYPE_SRC);
//            mShowTypeCircle = null;
//            mShowType2Screen = null;
//            mShowType4Screen = null;
//            mShowTypeHemishpere = null;
//            mShowTypeCylinder = null;
//            return;
//        }
        mShowTypSrc = new ShowTypeSrc(width, height);
        onChangeShowType(TYPE_SRC);
        float[] vertext = ShapeManagerJni.GetVertext(width, height, width / 2, height / 2, height < width ? height / 2 : width / 2, 0);
        if (vertext != null) {
            FloatBuffer verticeHemishpere = ByteBuffer.allocateDirect(vertext.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            verticeHemishpere.put(vertext).position(0);
            if (mShowTypeCircle == null) {
                mShowTypeCircle = new ShowTypeCircle(verticeHemishpere);
            } else {
                mShowTypeCircle.setVertex(verticeHemishpere);
            }
            if (mShowType2Screen == null) {
                mShowType2Screen = new ShowType2Screen(verticeHemishpere);
            } else {
                mShowType2Screen.setVertex(verticeHemishpere);
            }
            if (mShowType4Screen == null) {
                mShowType4Screen = new ShowType4Screen(verticeHemishpere);
            } else {
                mShowType4Screen.setVertex(verticeHemishpere);
            }
            if (mShowTypeHemishpere == null) {
                mShowTypeHemishpere = new ShowTypeHemisphere(verticeHemishpere);
            } else {
                mShowTypeHemishpere.setVertex(verticeHemishpere);
            }
        }
        float[] cylinderVertex = ShapeManagerJni.GetVertext(width, height, width / 2, height / 2, height < width ? height / 2 : width / 2, 1);
        if (cylinderVertex != null) {
            FloatBuffer verticeCylinder = ByteBuffer.allocateDirect(cylinderVertex.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            verticeCylinder.put(cylinderVertex).position(0);
            if (mShowTypeCylinder == null) {
                mShowTypeCylinder = new ShowTypeCylinder(verticeCylinder);
            } else {
                mShowTypeCylinder.setVertex(verticeCylinder);
            }
        }
        onChangeShowType(curentType);
    }

    public void onChangeShowType(int type) {
        curentType = type;
        switch (type) {
            case TYPE_SRC:
                if (mShowTypSrc != null) {
                    mShowTypeProxy.setShowType(mShowTypSrc);
                }
                break;
            case TYPE_CIRCLE:
                if (mShowTypeCircle != null) {
                    mShowTypeProxy.setShowType(mShowTypeCircle);
                }
                break;
            case TYPE_2_SCREEN:
                if (mShowType2Screen != null) {
                    mShowTypeProxy.setShowType(mShowType2Screen);
                }
                break;
            case TYPE_4_SCREEN:
                if (mShowType4Screen != null) {
                    mShowTypeProxy.setShowType(mShowType4Screen);
                }
                break;
            case TYPE_HEMISPHERE:
                if (mShowTypeHemishpere != null) {
                    mShowTypeProxy.setShowType(mShowTypeHemishpere);
                }
                break;
            case TYPE_CYLINDER:
                if (mShowTypeCylinder != null) {
                    mShowTypeProxy.setShowType(mShowTypeCylinder);
                }
                break;
        }
    }


    private void onTransform() {
        mShowTypeProxy.onTransForm();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mShowTypeProxy.updateSize(width, height);
    }

    public void setYuv420pData(byte[] data, int width, int height) {
        mFrameWidth = width;
        mFrameHeight = height;
        synchronized (YUV_LOCK) {
            if (yBuffer == null || uBuffer == null || vBuffer == null || yBuffer.limit() < width * height) {
                yBuffer = ByteBuffer.allocateDirect(width * height);
                uBuffer = ByteBuffer.allocateDirect(width * height / 4);
                vBuffer = ByteBuffer.allocateDirect(width * height / 4);
            }
            yBuffer.clear();
            uBuffer.clear();
            vBuffer.clear();
            yBuffer.put(data, 0, width * height);
            uBuffer.put(data, width * height, width * height / 4);
            vBuffer.put(data, width * height * 5 / 4, width * height / 4);
        }
    }

    public void setFrameInfo(int width, int height) {
        mFrameWidth = width;
        mFrameHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (yBuffer == null || uBuffer == null || vBuffer == null) {
            return;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        onTransform();
        synchronized (YUV_LOCK) {
            if (yBuffer == null || uBuffer == null || vBuffer == null) {
                return;
            }
            yBuffer.position(0);
            uBuffer.position(0);
            vBuffer.position(0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureYUV[0]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth, mFrameHeight, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, yBuffer);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureYUV[1]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth / 2, mFrameHeight / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, uBuffer);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureYUV[2]);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mFrameWidth / 2, mFrameHeight / 2, 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, vBuffer);
        }
        FloatBuffer verticeBuffer = mShowTypeProxy.getVertex();
        verticeBuffer.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
        GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticeBuffer);
        ShaderUtil.checkGlError("glVertexAttribPointer maPosition");
        GLES20.glEnableVertexAttribArray(maPositionHandle);
        ShaderUtil.checkGlError("glEnableVertexAttribArray maPositionHandle");

        verticeBuffer.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
        GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
                TRIANGLE_VERTICES_DATA_STRIDE_BYTES, verticeBuffer);
        ShaderUtil.checkGlError("glVertexAttribPointer maTextureHandle");
        GLES20.glEnableVertexAttribArray(maTextureHandle);
        ShaderUtil.checkGlError("glEnableVertexAttribArray maTextureHandle");
        drawPoints();
        ShaderUtil.checkGlError("glDrawArrays");
        GLES20.glFinish();
    }

    private void drawPoints() {
        mShowTypeProxy.drawPoints();
    }


    public void onDown(MotionEvent event) {
        mShowTypeProxy.onDown(event);
    }

    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mShowTypeProxy.onScroll(w, h, e1, e2, distanceX, distanceY);
    }


    public void onScale(float scaleFactor) {
        mShowTypeProxy.onScale(scaleFactor);
    }


    public void onDoubleTap(MotionEvent event) {
        mShowTypeProxy.onDoubleTap(event);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        int program = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        GLES20.glUseProgram(program);
        ShaderUtil.checkGlError("glUseProgram");
        maPositionHandle = GLES20.glGetAttribLocation(program, "aPosition");
        ShaderUtil.checkGlError("glGetAttribLocation aPosition");
        if (maPositionHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aPosition");
        }
        maTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord");
        ShaderUtil.checkGlError("glGetAttribLocation aTextureCoord");
        if (maTextureHandle == -1) {
            throw new RuntimeException("Could not get attrib location for aTextureCoord");
        }

        int muMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix");
        ShaderUtil.checkGlError("glGetUniformLocation uMVPMatrix");
        if (muMVPMatrixHandle == -1) {
            throw new RuntimeException("Could not get attrib location for uMVPMatrix");
        }
        mShowTypeProxy.setMvpMatrixHandle(muMVPMatrixHandle);

        int textureUniformY = GLES20.glGetUniformLocation(program, "SamplerY");
        ShaderUtil.checkGlError("glGetUniformLocation SamplerY");
        if (textureUniformY == -1) {
            throw new RuntimeException("Could not get attrib location for SamplerY");
        }
        int textureUniformU = GLES20.glGetUniformLocation(program, "SamplerU");
        ShaderUtil.checkGlError("glGetUniformLocation SamplerU");
        if (textureUniformU == -1) {
            throw new RuntimeException("Could not get attrib location for SamplerU");
        }
        int textureUniformV = GLES20.glGetUniformLocation(program, "SamplerV");
        ShaderUtil.checkGlError("glGetUniformLocation SamplerV");
        if (textureUniformV == -1) {
            throw new RuntimeException("Could not get attrib location for SamplerV");
        }

        GLES20.glGenTextures(3, mTextureYUV, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        bindTexture(mTextureYUV[0]);
        GLES20.glUniform1i(textureUniformY, 0);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        bindTexture(mTextureYUV[1]);
        GLES20.glUniform1i(textureUniformU, 1);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        bindTexture(mTextureYUV[2]);
        GLES20.glUniform1i(textureUniformV, 2);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    }

    private void bindTexture(int id) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, id);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    }

}
