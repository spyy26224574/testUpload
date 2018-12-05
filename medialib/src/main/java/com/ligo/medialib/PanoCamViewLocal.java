package com.ligo.medialib;

import android.content.Context;
import android.media.MediaPlayer;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;

import com.ligo.medialib.opengl.VideoRenderHard;

import java.io.IOException;

/**
 * @author huangxy
 * @date 2018/3/27 16:15.
 */

public class PanoCamViewLocal extends GLSurfaceView implements ScaleGestureDetector.OnScaleGestureListener,
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnInfoListener, VideoRenderHard.OnSurfaceAvailableListener {
    private static final String TAG = "PanoCamViewLocal";
    private VideoRenderHard mVideoRender;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    public boolean isInit;
    private MediaPlayer mMediaPlayer;
//    private int mCameraId;
//    private int mCameraType;
    private Surface mSurface;
    private String mUrl;

    @Override
    public void onSurfaceAvailable(Surface surface) {
        mSurface = surface;
        if (started) {
            startPlay(mUrl);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.e(TAG, "onPrepared: ");
//        int ret = mVideoRender.initVertexData(mCameraId, mCameraType, mp.getVideoWidth(), mp.getVideoHeight());
        mVideoRender.initVertexData(mp.getVideoWidth(), mp.getVideoHeight());
        mMediaPlayer.start();
        if (mOnChangeListener != null) {
            mOnChangeListener.onLoadComplete(1);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "onCompletion: ");
        if (mOnChangeListener != null) {
            mOnChangeListener.onEnd();
        }
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        Log.e(TAG, "onSeekComplete: ");
        if (mOnChangeListener != null) {
            mOnChangeListener.onSeekComplete();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError: what = " + what + ",extra = " + extra);
        if (mOnChangeListener != null) {
            mOnChangeListener.onError("" + what);
        }
        return false;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        Log.e(TAG, "onBufferingUpdate: " + percent);
        if (mOnChangeListener != null) {
            mOnChangeListener.onBuffering(percent);
        }
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onInfo: what =" + what + ",extra = " + extra);
        if (mOnChangeListener != null) {
            mOnChangeListener.onInfo(what);
        }
        return false;
    }


    public void onChangeShowType(int type) {
        mVideoRender.onChangeShowType(type);
    }

    public PanoCamViewLocal(Context context) {
        this(context, null);
    }

    public PanoCamViewLocal(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        if (isInit) {
            return;
        }
        setEGLContextClientVersion(2);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        mVideoRender = new VideoRenderHard(context);
        mVideoRender.setOnSurfaceAvailableListener(this);
        setRenderer(mVideoRender);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        scaleGestureDetector = new ScaleGestureDetector(context, this);
        gestureDetector = new GestureDetector(context, this);
        gestureDetector.setOnDoubleTapListener(this);
        isInit = true;
    }

    public void reInit(Context context) {
        if (isInit) {
            return;
        }
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnSeekCompleteListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnInfoListener(this);
        isInit = true;
    }


    private OnChangeListener mOnChangeListener;

    public void setOnChangeListener(OnChangeListener onChangeListener) {
        mOnChangeListener = onChangeListener;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        Log.d("VideoPlayerView", "onScale :" + scaleFactor);
        mVideoRender.onScale(scaleFactor);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d("VideoPlayerView", "onScaleBegin :");
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        Log.d("PanoCamView1", "onScaleEnd :");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.scaleGestureDetector.onTouchEvent(event);
        this.gestureDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return true;
        //return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        Log.d("PanoCamView1", "onDown: x =" + event.getX() + ",y = " + event.getY());
        mVideoRender.onDown(event);
        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        Log.d("PanoCamView1", "onFling: " + event1.toString() + event2.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        Log.d("PanoCamView1", "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("PanoCamView1", "onScroll: " + e1.toString() + e2.toString());
        int w = this.getWidth();
        int h = this.getHeight();
        mVideoRender.onScroll(w, h, e1, e2, distanceX, distanceY);
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        Log.d("PanoCamView1", "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        Log.d("PanoCamView1", "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        Log.d("PanoCamView1", "onDoubleTap: " + event.toString());
        mVideoRender.onDoubleTap(event);
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d("PanoCamView1", "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d("PanoCamView1", "onSingleTapConfirmed: " + event.toString());
        return true;
    }


    public int getCurrent() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (mMediaPlayer != null) {
            return mMediaPlayer.getDuration();
        }
        return 0;
    }

    public void seek(int millisecond) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(millisecond);
        }
    }

    private boolean started = false;

    public void startPlay(String url) {
        mUrl = url;
        started = true;
        if (mSurface == null) {
            return;
        }
        started = false;
        mMediaPlayer.setSurface(mSurface);
        mMediaPlayer.reset();
        try {
            mMediaPlayer.setDataSource(url);
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        Log.e(TAG, "stopPlay: ");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    public void pause() {
        Log.e(TAG, "pause: ");
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void resume() {
        Log.e(TAG, "resume: ");
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    public interface OnChangeListener {
        void onLoadComplete(int ret);

        void onBuffering(int percent);

        void onSeekComplete();

        void onError(String errorMessage);

        void onEnd();

        void onInfo(int what);
    }


    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public boolean isPause() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void release() {
        isInit = false;
        Log.e(TAG, "release: ");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mSurface != null) {
            mSurface.release();
        }
        if (mVideoRender != null) {
            mVideoRender = null;
        }
    }

    public void changePlayer() {
        isInit = false;
        Log.e(TAG, "release: ");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
//        if (mSurface != null) {
//            mSurface.release();
//        }
        if (mVideoRender != null) {
//            mVideoRender = null;
        }
    }

}
