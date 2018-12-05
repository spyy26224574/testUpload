package com.ligo.medialib.opengl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/7 16:49.
 */
public class ShowType2Screen implements IShowType {
    private int mTriangleVerticesCount;
    private FloatBuffer mVerticeBuffer;
    private final int SCREEN_COUNT = 2;
    private float[][] mProjectMatrix = new float[SCREEN_COUNT][16];
    private float[][] mViewMatrix = new float[SCREEN_COUNT][16];
    private float[][] mModelMatrix = new float[SCREEN_COUNT][16];
    private float[][] mvpMatrixs = new float[SCREEN_COUNT][16];
    private float[] rotateAngleX = new float[SCREEN_COUNT];
    private float[] rotateAngleY = new float[SCREEN_COUNT];
    private int mMvpMatrixHandle;
    private float mWidth = 2160;
    private float mHeight = 2160;
    private static final float fovy = 60;
    private AnimatorSet mAnimatorSet;
    private ValueAnimator mScaleAnimator;
    private ValueAnimator mEyeZAnimator;

    public ShowType2Screen(FloatBuffer vertex) {
        mVerticeBuffer = vertex;
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
        for (int i = 0; i < SCREEN_COUNT; i++) {
            rotateAngleY[i] = i * 360 / 2 + 180;
            rotateAngleX[i] = 90 - fovy / 2;
            Matrix.setIdentityM(mModelMatrix[i], 0);
            Matrix.setIdentityM(mViewMatrix[i], 0);
            Matrix.setIdentityM(mProjectMatrix[i], 0);
            Matrix.setLookAtM(mViewMatrix[i], 0, 0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0f, 1.0f, 0.0f);
        }
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
        for (int i = 0; i < SCREEN_COUNT; i++) {
            Matrix.perspectiveM(mProjectMatrix[i], 0, fovy, (float) 2 * width / height, 0.1f, 300);
        }
    }


    @Override
    public void onDown(MotionEvent event) {

    }

    @Override
    public void onScale(float scaleFactor) {

    }

    private float[] mScale = {1.0f, 1.0f, 1.0f, 1.0f};

    private int tabScreen = 0;
    private int state = 0;//0-普通状态，1-放大状态
    private float eyeZ = 0.0f;

    @Override
    public void onDoubleTap(MotionEvent e) {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            return;
        }
        if (state == 0) {
            float x = e.getX();
            float y = e.getY();
            if (y <= mHeight / 2) {
                tabScreen = 1;
            } else {
                tabScreen = 0;
            }
        }

        startAnimation();
    }

    private void startAnimation() {
        if (mAnimatorSet != null && mAnimatorSet.isRunning()) {
            return;
        }
        if (mScaleAnimator == null) {
            mScaleAnimator = new ValueAnimator();
            mScaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mScale[tabScreen] = (float) animation.getAnimatedValue();
                    Matrix.perspectiveM(mProjectMatrix[tabScreen], 0, fovy, (3 - mScale[tabScreen]) * mWidth / mHeight, 0.1f, 300);

                }
            });
        }
        if (state == 0) {
            mScaleAnimator.setFloatValues(1.0f, 2.0f);
            mScaleAnimator.setInterpolator(new AccelerateInterpolator());
        } else {
            mScaleAnimator.setFloatValues(2.0f, 1.0f);
            mScaleAnimator.setInterpolator(new DecelerateInterpolator());
        }
        if (mEyeZAnimator == null) {
            mEyeZAnimator = new ValueAnimator();
            mEyeZAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    eyeZ = (float) animation.getAnimatedValue();
                    for (int i = 0; i < SCREEN_COUNT; i++) {
                        if (i == tabScreen) {
                            continue;
                        }
                        Matrix.setLookAtM(mViewMatrix[i], 0, 0f, 0.0f, eyeZ, 0.0f, 0.0f, 10.0f, 0f, 1.0f, 0.0f);
                    }
                }
            });
        }
        if (state == 0) {
            mEyeZAnimator.setFloatValues(0.0f, -2.0f);
            mEyeZAnimator.setInterpolator(new DecelerateInterpolator());
        } else {
            mEyeZAnimator.setFloatValues(-2.0f, 0.0f);

            mEyeZAnimator.setInterpolator(new AccelerateInterpolator());
        }

        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.playTogether(mScaleAnimator, mEyeZAnimator);
            mAnimatorSet.setDuration(2000);
            mAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (state == 0) {
                        state = 1;
                    } else {
                        state = 0;
                    }
                }
            });
        }
        mAnimatorSet.start();
    }

    @Override
    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float currentY = e2.getY();
        float currentX = e2.getX();
        rotateAngleY[0] += (-distanceX * 180.0f / w);
        if (rotateAngleY[0] < 0.f) {
            rotateAngleY[0] = 360.f + rotateAngleY[0];
        }
        if (rotateAngleY[0] > 360.f) {
            rotateAngleY[0] = rotateAngleY[0] - 360.f;
        }
        if (currentY <= 0 || currentY >= h || currentX <= 0 || currentX >= w) {
            return;
        }
        int currentScreen;
        if (currentY <= h / 2) {
            currentScreen = 1;
        } else {
            currentScreen = 0;
        }
        rotateAngleY[1] = rotateAngleY[0] + 180;
        rotateAngleX[currentScreen] += (-distanceY * 180.0f / h);
        if (rotateAngleX[currentScreen] < fovy / 2) {
            rotateAngleX[currentScreen] = fovy / 2;
        }
        if (rotateAngleX[currentScreen] > (90 - fovy / 2)) {
            rotateAngleX[currentScreen] = (90 - fovy / 2);
        }
    }

    @Override
    public void setMvpMatrixHandle(int mvpMatrixHandle) {
        mMvpMatrixHandle = mvpMatrixHandle;
    }

    @Override
    public void drawPoints() {
        for (int i = 0; i < 2; i++) {
            GLES20.glUniformMatrix4fv(mMvpMatrixHandle, 1, false, mvpMatrixs[i], 0);
            GLES20.glViewport(0, (int) (i * mHeight * (2 - mScale[i]) / 2), (int) (mWidth), (int) (mHeight * mScale[i] / 2));
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mTriangleVerticesCount);
        }
    }

    @Override
    public void onTransForm() {
        for (int i = 0; i < SCREEN_COUNT; i++) {
            Matrix.setIdentityM(mModelMatrix[i], 0);
            Matrix.rotateM(mModelMatrix[i], 0, rotateAngleX[i], 1.0f, 0, 0);
            Matrix.rotateM(mModelMatrix[i], 0, rotateAngleY[i], 0.0f, 0.0f, 1);
        }
        for (int i = 0; i < 2; i++) {
            Matrix.multiplyMM(mvpMatrixs[i], 0, mViewMatrix[i], 0, mModelMatrix[i], 0);
            Matrix.multiplyMM(mvpMatrixs[i], 0, mProjectMatrix[i], 0, mvpMatrixs[i], 0);
        }
    }

    @Override
    public FloatBuffer getVertex() {
        return mVerticeBuffer;
    }
}
