package com.ligo.medialib.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/10 16:20.
 */
public class ShowTypeSrc implements IShowType {
    private int mTriangleVerticesCount;
    private FloatBuffer mVerticeBuffer;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mvpMatrixs = new float[16];
    private int mMvpMatrixHandle;
    private float mWidth, mHeight;
    private int mVideoWidth, mVideoHeight;

    public ShowTypeSrc(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        float[] vertex = new float[]{
                // X, Y, Z, U, V
                -1.0f, -1.0f, 0, 0.f, 1.f,
                1.0f, -1.0f, 0, 1.f, 1.f,
                -1.0f, 1.0f, 0, 0.f, 0.f,
                1.0f, 1.0f, 0, 1.f, 0.f,

        };
        mVerticeBuffer = ByteBuffer.allocateDirect(vertex.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mVerticeBuffer.put(vertex).position(0);
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
    }

    @Override
    public void setVertex(FloatBuffer vertex) {

    }

    @Override
    public void updateSize(float width, float height) {
        mWidth = width;
        mHeight = height;
        if (mWidth > 0 && mHeight > 0) {
            final float ratio = (mWidth > mHeight) ? (float) mWidth / (float) mHeight : (float) mHeight / (float) mWidth;
            if (mWidth > mHeight) {
                Matrix.orthoM(mProjectMatrix, 0, -ratio, ratio, -1f, 1f, -100f, 100f);
            } else {
                Matrix.orthoM(mProjectMatrix, 0, -1f, 1f, -ratio, ratio, -100f, 100f);
            }
        } else {
            Matrix.orthoM(mProjectMatrix, 0, -1, 1, -1, 1, -100f, 100f);
        }
        Matrix.multiplyMM(mvpMatrixs, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrixs, 0, mProjectMatrix, 0, mvpMatrixs, 0);
    }

    @Override
    public void onDown(MotionEvent event) {

    }

    @Override
    public void onScale(float scaleFactor) {

    }

    @Override
    public void onDoubleTap(MotionEvent event) {

    }

    @Override
    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

    }

    @Override
    public void setMvpMatrixHandle(int mvpMatrixHandle) {
        mMvpMatrixHandle = mvpMatrixHandle;
    }

    @Override
    public void drawPoints() {
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrixs, 0);
        GLES20.glViewport(0, 0, (int) mWidth, (int) mHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, mTriangleVerticesCount);
    }

    @Override
    public void onTransForm() {

    }

    @Override
    public FloatBuffer getVertex() {
        return mVerticeBuffer;
    }
}
