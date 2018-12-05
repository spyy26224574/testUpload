package com.ligo.medialib.opengl;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/7 15:01.
 */
public class ShowTypeCircle implements IShowType {
    private static final String TAG = ShowTypeCircle.class.getSimpleName();
    private int mTriangleVerticesCount;
    private FloatBuffer mVerticeBuffer;
    private float[] mProjectMatrix = new float[16];
    private float[] mViewMatrix = new float[16];
    private float[] mModelMatrix = new float[16];
    private float[] mvpMatrixs = new float[16];
    private float mWidth = 2160;
    private float mHeight = 2160;
    private float lastX, lastY;
    private float rotateAngleY, rotateAngleX, rotateAngleZ;
    private float eyeZ;
    private AnimatorSet mAnimatorSet;
    private int status = 0;
    private boolean isAnimate = false;
    private int mMvpMatrixHandle;
    private static final float fovy = 60;
    private ValueAnimator mMagnifyAnimator;
    private ValueAnimator mRotateAnimator;

    ShowTypeCircle(FloatBuffer vertex) {
        mVerticeBuffer = vertex;
        mTriangleVerticesCount = mVerticeBuffer.limit() / 5;
        eyeZ = (float) (-1 / Math.tan(fovy * Math.PI / 2 / 180));
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
    public void onTransForm() {
        Matrix.setIdentityM(mModelMatrix, 0);
        if (status == 0 && !isAnimate) {
            Matrix.rotateM(mModelMatrix, 0, rotateAngleZ, 0.0f, 0.0f, 1);
        } else {
            Matrix.rotateM(mModelMatrix, 0, rotateAngleX, 1.0f, 0, 0);
            Matrix.rotateM(mModelMatrix, 0, rotateAngleY, 0.0f, 0.0f, 1);
        }
        Matrix.multiplyMM(mvpMatrixs, 0, mViewMatrix, 0, mModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrixs, 0, mProjectMatrix, 0, mvpMatrixs, 0);
    }

    @Override
    public FloatBuffer getVertex() {
        return mVerticeBuffer;
    }

    @Override
    public void onDown(MotionEvent event) {
        lastX = event.getX();
        lastY = event.getY();
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
        float currentY = e2.getY();
        float currentX = e2.getX();
        if (status == 0) {
            if (currentX >= 0 && currentX <= w && currentY >= 0 && currentY <= h) {
                float x1 = e1.getX();
                float x2 = e2.getX();
                float y2 = e2.getY();
                double d = Math.abs(x1 - x2) + Math.abs(x1 - x2);
                if (d <= 20) {
                    return;
                }

                float deg = getCircleAngle(new PointF(w / 2, h / 2), new PointF(lastX, lastY), new PointF(x2, y2));
                rotateAngleZ -= deg;
                lastX = x2;
                lastY = y2;
            }
        } else {
            rotateAngleY += (-distanceX * 180.0f / w);
            if (rotateAngleY < 0.f) {
                rotateAngleY = 360.f + rotateAngleY;
            }
            if (rotateAngleY > 360.f) {
                rotateAngleY = rotateAngleY - 360.f;
            }

            rotateAngleX += (-distanceY * 180.0f / h);
            if (rotateAngleX < fovy / 2) {
                rotateAngleX = fovy / 2;
            }
            if (rotateAngleX > (90 - fovy / 2)) {
                rotateAngleX = (90 - fovy / 2);
            }
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
            mMagnifyAnimator.setFloatValues((float) (-1 / Math.tan(fovy * Math.PI / 2 / 180)), 0);
        } else {
            mMagnifyAnimator.setFloatValues(0, (float) (-1 / Math.tan(fovy * Math.PI / 2 / 180)));
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
            mRotateAnimator.setFloatValues(0, 90 - fovy / 2);
        } else {
            mRotateAnimator.setFloatValues(rotateAngleX, 0);
        }
        if (mAnimatorSet == null) {
            mAnimatorSet = new AnimatorSet();
            mAnimatorSet.setDuration(2000);
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
            rotateAngleY = rotateAngleZ;
            status = 1;
        } else {
            rotateAngleZ = rotateAngleY;
            status = 0;
        }
        mAnimatorSet.start();
    }

    float getCircleAngle(PointF center, PointF first, PointF second) {
        PointF a = new PointF();
        a.x = first.x - center.x;
        a.y = first.y - center.y;
        PointF b = new PointF();
        b.x = second.x - center.x;
        b.y = second.y - center.y;

        a.y = -a.y;
        b.y = -b.y;
        double ra = Math.sqrt(a.x * a.x + a.y * a.y);
        double rb = Math.sqrt(b.x * b.x + b.y * b.y);
        if (ra == 0.0f) {
            ra = 0.0000001f;
        }
        if (rb == 0.0f) {
            rb = 0.0000001f;
        }
        double angleA = Math.asin(a.y / ra) * 180 / Math.PI;
        if (a.x < 0) {
            angleA = 180 - angleA;
        }
        double angleB = Math.asin(b.y / rb) * 180 / Math.PI;
        if (b.x < 0) {
            angleB = 180 - angleB;
        }
        return (float) (angleB - angleA);
    }
}
