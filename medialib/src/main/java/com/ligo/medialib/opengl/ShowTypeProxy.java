package com.ligo.medialib.opengl;

import android.view.MotionEvent;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/7 17:07.
 */
public class ShowTypeProxy implements IShowType {
    private IShowType mIShowType;
    private int mMvpMatrixHandle;
    private float mWidth, mHeight;

    public void setShowType(IShowType showType) {
        mIShowType = showType;
        mIShowType.setMvpMatrixHandle(mMvpMatrixHandle);
        if (mWidth == 0 || mHeight == 0) {
            return;
        }
        mIShowType.updateSize(mWidth, mHeight);
    }

    @Override
    public void updateSize(float width, float height) {
        mWidth = width;
        mHeight = height;
        if (mIShowType != null) {
            mIShowType.updateSize(width, height);
        }
    }


    @Override
    public void onDown(MotionEvent event) {
        if (mIShowType != null) {
            mIShowType.onDown(event);
        }
    }

    @Override
    public void onScale(float scaleFactor) {
        if (mIShowType != null) {
            mIShowType.onScale(scaleFactor);
        }
    }

    @Override
    public void onDoubleTap(MotionEvent event) {
        if (mIShowType != null) {
            mIShowType.onDoubleTap(event);
        }
    }

    @Override
    public void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (mIShowType != null) {
            mIShowType.onScroll(w, h, e1, e2, distanceX, distanceY);
        }
    }

    @Override
    public void setMvpMatrixHandle(int mvpMatrixHandle) {
        mMvpMatrixHandle = mvpMatrixHandle;
        if (mIShowType != null) {
            mIShowType.setMvpMatrixHandle(mvpMatrixHandle);
        }
    }

    @Override
    public void drawPoints() {
        if (mIShowType != null) {
            mIShowType.drawPoints();
        }
    }

    @Override
    public void onTransForm() {
        if (mIShowType != null) {
            mIShowType.onTransForm();
        }
    }

    @Override
    public FloatBuffer getVertex() {
        if (mIShowType == null) {
            return null;
        }
        return mIShowType.getVertex();
    }

    @Override
    public void setVertex(FloatBuffer vertex) {

    }
}
