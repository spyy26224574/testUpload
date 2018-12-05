package com.ligo.medialib.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/10 14:03.
 */
public class ShowTypeCylinder implements IShowType {
    private int mTriangleVerticesCount;
    private FloatBuffer mVerticeBuffer;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mvpMatrixs = new float[16];
    private float mWidth = 2160;
    private float mHeight = 2160;
    private static final float MAX_ROTATEX = 135;
    private static final float MIN_ROTATEX = 45;
    private static final float INIT_ROTATEX = 70;
    private static final float MAX_SCALE = 1.7f;
    private static final float MIN_SCALE = 0.5f;
    private float rotateAngleY, rotateAngleX = INIT_ROTATEX;
    private float scaleFactory = 1.3f;
    private static final float fovy = 60;
    private final float START_EYEZ = (float) (-1 / Math.tan(fovy * Math.PI / 2 / 180)) - 0.7f;
    private float eyeZ = START_EYEZ;
    private int mMvpMatrixHandle;


    public ShowTypeCylinder(FloatBuffer vertexBuffer) {
        mVerticeBuffer = vertexBuffer;
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
        Matrix.setIdentityM(mProjectMatrix, 0);
        Matrix.setIdentityM(mViewMatrix, 0);
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.setLookAtM(mViewMatrix, 0, 0f, 0.0f, eyeZ, 0.0f, 0.0f, 1.0f, 0f, 1.0f, 0.0f);
    }

    @Override
    public void setVertex(FloatBuffer vertex) {
        mVerticeBuffer = vertex;
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
    }

    @Override
    public void updateSize(float width, float height) {
        mWidth = width;
        mHeight = height;
        Matrix.perspectiveM(mProjectMatrix, 0, fovy, width / height, 0.1f, 300);
    }


    @Override
    public void onDown(MotionEvent event) {

    }

    @Override
    public void onScale(float scaleFactor) {
        this.scaleFactory *= scaleFactor;
        if (this.scaleFactory < MIN_SCALE) {
            this.scaleFactory = MIN_SCALE;
        }
        if (this.scaleFactory > MAX_SCALE) {
            this.scaleFactory = MAX_SCALE;
        }
    }

    @Override
    public void onDoubleTap(MotionEvent event) {

    }

    @Override
    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        rotateAngleX += (distanceY * 180.0f / h);
        if (rotateAngleX > MAX_ROTATEX) {
            rotateAngleX = MAX_ROTATEX;
        }
        if (rotateAngleX < MIN_ROTATEX) {
            rotateAngleX = MIN_ROTATEX;
        }
        rotateAngleY += (distanceX * 180.0f / w);
        if (rotateAngleY < 0.f) {
            rotateAngleY = 360.f + rotateAngleY;
        }
        if (rotateAngleY > 360.f) {
            rotateAngleY = rotateAngleY - 360.f;
        }
    }

    @Override
    public void setMvpMatrixHandle(int mvpMatrixHandle) {
        mMvpMatrixHandle = mvpMatrixHandle;
    }

    @Override
    public void drawPoints() {
        GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrixs, 0);
        GLES20.glViewport(0, 0, (int) mWidth, (int) mHeight);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mTriangleVerticesCount);
    }

    @Override
    public void onTransForm() {
        Matrix.setIdentityM(mModelMatrix, 0);
        Matrix.rotateM(mModelMatrix, 0, rotateAngleX, 1.0f, 0, 0);
        Matrix.rotateM(mModelMatrix, 0, rotateAngleY, 0.0f, 0.0f, 1);
        Matrix.scaleM(mModelMatrix, 0, scaleFactory, scaleFactory, scaleFactory);
        Matrix.multiplyMM(mvpMatrixs, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrixs, 0, mProjectMatrix, 0, mvpMatrixs, 0);
    }

    @Override
    public FloatBuffer getVertex() {
        return mVerticeBuffer;
    }
}
