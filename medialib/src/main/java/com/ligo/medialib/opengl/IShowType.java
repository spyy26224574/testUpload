package com.ligo.medialib.opengl;

import android.view.MotionEvent;

import java.nio.FloatBuffer;

/**
 * @author huangxy
 * @date 2018/9/7 14:57.
 */
public interface IShowType {

    void updateSize(float width, float height);

    void onDown(MotionEvent event);

    void onScale(float scaleFactor);

    void onDoubleTap(MotionEvent event);

    void onScroll(int w, int h, MotionEvent e1, MotionEvent e2, float distanceX, float distanceY);

    void setMvpMatrixHandle(int mvpMatrixHandle);

    void drawPoints();

    void onTransForm();

    FloatBuffer getVertex();

    void setVertex(FloatBuffer vertex);
}
