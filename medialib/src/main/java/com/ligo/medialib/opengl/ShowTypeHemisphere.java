package com.ligo.medialib.opengl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/10 10:26.
 */
public class ShowTypeHemisphere implements IShowType {
    private int mTriangleVerticesCount;
    private FloatBuffer mVerticeBuffer;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mvpMatrixs = new float[16];
    private float mWidth = 2160;
    private float mHeight = 2160;
    private float rotateAngleY, rotateAngleX;
    private float eyeZ;
    private AnimatorSet mAnimatorSet;
    private int status = 0;
    private boolean isAnimate = false;
    private static final float fovy = 60;
    private int mMvpMatrixHandle;
    private final float START_EYEZ = (float) (-1 / Math.tan(fovy * Math.PI / 2 / 180)) - 0.4f;
    private final float MAX_ROTATEX = 135;
    private final float MIN_ROTATEX = 45;
    private final float INIT_ROTATEX = 70;
    private ValueAnimator mMagnifyAnimator;
    private ValueAnimator mRotateAnimator;

    ShowTypeHemisphere(FloatBuffer vertex) {
        mVerticeBuffer = vertex;
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
        eyeZ = START_EYEZ;
        rotateAngleX = INIT_ROTATEX;
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
        if (scaleFactor > 1 && status == 0) {
            startAnimate();
        }
        if (scaleFactor < 1 && status == 1) {
            startAnimate();
        }
    }

    @Override
    public void onDoubleTap(MotionEvent event) {
        startAnimate();
    }

    @Override
    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            return;
        }
        if (status == 0) {
            rotateAngleX += (distanceY * 180.0f / h);
            if (rotateAngleX > MAX_ROTATEX) {
                rotateAngleX = MAX_ROTATEX;
            }
            if (rotateAngleX < MIN_ROTATEX) {
                rotateAngleX = MIN_ROTATEX;
            }
        } else {
            rotateAngleX -= (distanceY * 180.0f / h);
            if (rotateAngleX < fovy / 2) {
                rotateAngleX = fovy / 2;
            }
            if (rotateAngleX > (90 - fovy / 2)) {
                rotateAngleX = (90 - fovy / 2);
            }
        }
        if (status == 0) {
            rotateAngleY += (distanceX * 180.0f / w);
        } else {
            rotateAngleY -= (distanceX * 180.0f / w);
        }
        if (rotateAngleY < 0.f) {
            rotateAngleY = 360.f + rotateAngleY;
        }
        if (rotateAngleY > 360.f) {
            rotateAngleY = rotateAngleY - 360.f;
        }
//

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
        Matrix.multiplyMM(mvpMatrixs, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrixs, 0, mProjectMatrix, 0, mvpMatrixs, 0);
    }

    @Override
    public FloatBuffer getVertex() {
        return mVerticeBuffer;
    }

    private void startAnimate() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            return;
        }

        if (mMagnifyAnimator == null) {
            mMagnifyAnimator = new ValueAnimator();
            mMagnifyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    eyeZ = (float) animation.getAnimatedValue();
                    Matrix.setLookAtM(mViewMatrix, 0, 0f, 0.0f, eyeZ, 0.0f, 0.0f, 1.0f, 0f, 1.0f, 0.0f);
                }
            });
        }
        if (status == 0) {
            mMagnifyAnimator.setFloatValues(START_EYEZ, 0);
        } else {
            mMagnifyAnimator.setFloatValues(0, START_EYEZ);
        }

        if (mRotateAnimator == null) {
            mRotateAnimator = new ValueAnimator();
            mRotateAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    rotateAngleX = (float) animation.getAnimatedValue();
                }
            });
        }
        if (status == 0) {
            mRotateAnimator.setFloatValues(rotateAngleX, 90 - fovy / 2);
        } else {
            mRotateAnimator.setFloatValues(rotateAngleX, INIT_ROTATEX);
        }
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(2500);
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    isAnimate = true;
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    isAnimate = false;
                }
            });
            mAnimatorSet.playTogether(mMagnifyAnimator, mRotateAnimator);
        }
        if (status == 0) {
            status = 1;
        } else {
            status = 0;
        }
        mAnimatorSet.start();
    }

}
