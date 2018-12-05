package com.ligo.medialib.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.view.MotionEvent;

import com.ligo.medialib.ShaderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


/**
 * @author huangxy
 * @date 2018/9/19 15:40.
 */
public class ImageRender implements GLSurfaceView.Renderer {
    private static final String TAG = ImageRender.class.getSimpleName();
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
            "uniform sampler2D uTexture;\n" +
                    "varying lowp vec2 vTextureCoord;\n" +
                    "void main(){\n" +
                    "gl_FragColor=texture2D(uTexture,vTextureCoord);\n" +
                    "}\n";
    private Bitmap mBitmap;
    private static final int FLOAT_SIZE_BYTES = 4;
    private final Context mContext;
    private int maPositionHandle;
    private int maTextureHandle;
    private volatile int textureId = -1;

    private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
    private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3;
    private static final int TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES;
    public static final int TYPE_SRC = 0;
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_2_SCREEN = 2;
    public static final int TYPE_4_SCREEN = 3;
    public static final int TYPE_HEMISPHERE = 4;
    public static final int TYPE_CYLINDER = 5;
    private ShowTypeProxy mShowTypeProxy;
    private IShowType mShowTypSrc, mShowTypeCircle, mShowType2Screen, mShowType4Screen, mShowTypeHemishpere, mShowTypeCylinder;
    private int curentType;
    private final Object BITMAP_LOCK = new Object();

    public void setBitmap(Bitmap bitmap) {
        synchronized (BITMAP_LOCK) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
//            Calibration.CalibrationBean calibration = CalibrationManager.getCalibration(mContext, width, height);
//            if (calibration == null) {
//                onChangeShowType(TYPE_SRC);
//                mShowTypeCircle = null;
//                mShowType2Screen = null;
//                mShowType4Screen = null;
//                mShowTypeHemishpere = null;
//                mShowTypeCylinder = null;
//            } else {
            mShowTypSrc = new ShowTypeSrc(width, height);
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
//            }
            onChangeShowType(curentType);
            mBitmap = bitmap;
            synchronized (BITMAP_LOCK) {
                BITMAP_LOCK.notifyAll();
            }
        }
    }

    public ImageRender(Context context) {
        mContext = context;
        mShowTypeProxy = new ShowTypeProxy();
    }

    public void onChangeShowType(int type) {
        curentType = type;
        switch (type) {
            case TYPE_SRC:
                mShowTypeProxy.setShowType(mShowTypSrc);
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


    @Override
    public void onDrawFrame(GL10 gl) {
        if (mBitmap == null) {
            synchronized (BITMAP_LOCK) {
                try {
                    BITMAP_LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isReleased) {
            return;
        }
        FloatBuffer verticeBuffer = mShowTypeProxy.getVertex();
        if (verticeBuffer == null) {
            return;
        }
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
        onTransform();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
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


    public void onDoubleTap(MotionEvent e) {
        mShowTypeProxy.onDoubleTap(e);
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
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
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        createTexture();
    }

    private void createTexture() {
        int[] texture = new int[1];
        if (mBitmap == null) {
            synchronized (BITMAP_LOCK) {
                try {
                    BITMAP_LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isReleased) {
            return;
        }
//        if (!mBitmap.isRecycled()) {
        //生成纹理
        GLES20.glGenTextures(1, texture, 0);
        //生成纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture[0]);
        //设置缩小过滤为使用纹理中坐标最接近的一个像素的颜色作为需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        //设置放大过滤为使用纹理中坐标最接近的若干个颜色，通过加权平均算法得到需要绘制的像素颜色
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //设置环绕方向S，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        //设置环绕方向T，截取纹理坐标到[1/2n,1-1/2n]。将导致永远不会与border融合
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        //根据以上指定的参数，生成一个2D纹理
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
//        mBitmap.recycle();
        textureId = texture[0];
//        }

    }

    private boolean isReleased;

    public void release() {
        isReleased = true;
        mBitmap = null;
        synchronized (BITMAP_LOCK) {
            BITMAP_LOCK.notifyAll();
        }
    }

}