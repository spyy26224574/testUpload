package com.ligo.medialib;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.ligo.medialib.opengl.ImageRender;

/**
 * @author huangxy
 * @date 2018/9/19 15:48.
 */
public class PanoImageView extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {
    private static final String TAG = PanoImageView.class.getSimpleName();
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private ImageRender mHemisphere;
    private Context mContext;

    public PanoImageView(Context context) {
        this(context, null);
    }

    public PanoImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
        mHemisphere = new ImageRender(mContext);
        setRenderer(mHemisphere);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void setBitmap(Bitmap bitmap) {
        mHemisphere.setBitmap(bitmap);
    }

    public void onChangeShowType(int type) {
        if (mHemisphere != null) {
            mHemisphere.onChangeShowType(type);
            requestRender();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.scaleGestureDetector.onTouchEvent(event);
        this.gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        mHemisphere.onDoubleTap(e);
//        requestRender();
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        mHemisphere.onDown(e);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        mHemisphere.onScroll(getWidth(), getHeight(), e1, e2, distanceX, distanceY);
//        requestRender();
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mHemisphere.onScale(detector.getScaleFactor());
//        requestRender();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    public void release() {
        mHemisphere.release();
    }
}
