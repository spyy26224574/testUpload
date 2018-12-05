package com.ligo.medialib;

import android.view.Surface;

/**
 * Created by admin on 2016/11/3.
 */

public class MediaPlayLib {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("media-lib");
    }


    public interface MediaInfoListener {
        void onInfoUpdate(int state, String info);

        void onUpdateFrame(int size, int width, int height, int type);

        void onUpdateAudioFrame(byte[] data, int size);

        void onScreenshotData(byte[] data, int width, int height, String path);

        void update264Frame(byte[] data, int size, int width, int height, int type);
//        void onUpdateVideoInfo(int width, int height);
    }

    private Surface mSurface = null;
    private String mediaUrl = null;

    public native int[] getMediaWH(String path);

    public native void nativeSetFrameBuffer(byte[] frameBuffer);

    public native void nativeResume();

    public native void replay(String url);

    public native void nativeSetListener(MediaInfoListener listener);

    public native void setupSurface(Surface surface, int width, int height);

    public native void setUrl(String url);

    public native byte[] nativeStartPlay(int type);//1-remote 2-实时流 3-local

    public native void nativeSeek(int second);

    public native int nativeDuration();

    public native int nativeCurrent();

    public native byte[] playRtsp(String url, Surface surface);

    public native void nativeStopPlay();

    public native void nativePause();

    public native void init();

    public native int getCurrentState();

    public native void ChangeDecodec(int type);//1 SOFT 0 HARD

    public native int GetDecodec();

    public native void startCut(String localname);

    public native void stopCut();

    public native void startLive(String pushurl);

    public native void setIsLive(boolean isLive);

    public native void release();

    public native int startScreenshot(String localname);

    public native int sunGetInfoType(String localname);

    public native int sunSetInfoType(String localname, int type);

    public native int Cache();
}
