package com.ligo.medialib.opengl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;

import com.ligo.medialib.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

/**
 * @author huangxy
 * @date 2018/9/10 20:04.
 */
public class VideoRenderHard implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
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
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
                    "}\n";
    private static final int FLOAT_SIZE_BYTES = 4;
    private int maPositionHandle;
    private int maTextureHandle;
    private int mFrameWidth = 800, mFrameHeight = 800;
    private int mTextureID;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    public static final int TYPE_SRC = 0;
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_2_SCREEN = 2;
    public static final int TYPE_4_SCREEN = 3;
    public static final int TYPE_HEMISPHERE = 4;
    public static final int TYPE_CYLINDER = 5;
    /**
     * 0-为普通状态，1-为进入半球中心状态
     */
    private ShowTypeProxy mShowTypeProxy;
    private IShowType mShowTypSrc, mShowTypeCircle, mShowType2Screen, mShowType4Screen, mShowTypeHemishpere, mShowTypeCylinder;
    private SurfaceTexture mSurface;
    private boolean updateSurface = false;
    private OnSurfaceAvailableListener mOnSurfaceAvailableListener;
    private Context mContext;
    private int curentType;

    public VideoRenderHard(Context context) {
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

    public void setFrameInfo(int width, int height) {
        mFrameWidth = width;
        mFrameHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (updateSurface) {
                mSurface.updateTexImage();
                updateSurface = false;
            }
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        onTransform();
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

        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);

        mTextureID = textures[0];
        GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, mTextureID);
        ShaderUtil.checkGlError("glBindTexture mTextureID");
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        mSurface = new SurfaceTexture(mTextureID);
        mSurface.setOnFrameAvailableListener(this);

        Surface surface = new Surface(mSurface);
        if (mOnSurfaceAvailableListener != null) {
            mOnSurfaceAvailableListener.onSurfaceAvailable(surface);
        }
    }

    public interface OnSurfaceAvailableListener {
        void onSurfaceAvailable(Surface surface);
    }

    public void setOnSurfaceAvailableListener(OnSurfaceAvailableListener onSurfaceAvailableListener) {
        mOnSurfaceAvailableListener = onSurfaceAvailableListener;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        updateSurface = true;
    }
}
