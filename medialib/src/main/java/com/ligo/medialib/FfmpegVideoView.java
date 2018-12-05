package com.ligo.medialib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

/**
 * Created by admin on 2016/11/3.
 */

@SuppressLint("NewApi")
public class FfmpegVideoView extends SurfaceView implements TextureView.SurfaceTextureListener,SurfaceHolder.Callback {


    private MediaPlayLib mediaPlayLib;
    public void setMediaInfoListener(MediaInfoListener mediaInfoListener) {
        this.mediaInfoListener = mediaInfoListener;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mSurface=surfaceHolder.getSurface();
        Log.e("surfaceCreated","surfaceCreated");
        if(isStart){
            isStart=false;
            Log.e("surfaceCreated","isStart="+isStart);
            startPlay();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.e("surfaceDestroyed","surfaceDestroyed");
//        stopPlay();
    }

    public enum States{
        NONE,
        START_PREPARE,
        PREPRARING,
        PREPARED,
        START_BUFFER,
        BUFFRING,
        START_PLAY,
        PAUSE,
        STOP
    }
    public interface MediaInfoListener{
        void onInfo(States state,String info);
    }
    private MediaInfoListener mediaInfoListener;
    private Surface mSurface =null;
    private String mediaUrl=null;
    /**
     * 是否开始播放标志，调用startplay后，如果页面还没初始化好，初始化成功后自动播放。
     */
    private boolean isStart=false;
    public FfmpegVideoView(Context context) {
        super(context);
        init();
    }

    public FfmpegVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FfmpegVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public FfmpegVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init(){
//        setSurfaceTextureListener(this);
        mediaPlayLib=new MediaPlayLib();
        getHolder().addCallback(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        if(mSurface!=null){
            mSurface=null;
        }
        mSurface=new Surface(surfaceTexture);
        if(isStart){
            isStart=false;
            startPlay();
        }
        Log.e("onsurfacea","onSurfaceTextureAvailable");
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    public void startPlay(){
        startPlay(null);
    }

    public void startPlay(String url){
        if(TextUtils.isEmpty(url)&&TextUtils.isEmpty(mediaUrl)){
            throw new NullPointerException("must use setMediaUrl first or use startPlay(String url)");
        }else if(!TextUtils.isEmpty(url)){
            mediaUrl=url;
        }
        if(mSurface !=null){
            mediaPlayLib.playRtsp(mediaUrl, mSurface);
            isStart=false;
        }else{
            isStart=true;
        }
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public void stopPlay(){
        mediaPlayLib.nativeStopPlay();
    }

//    public void pause(){
//        mediaPlayLib.nativePause();
//    }

    public void resume(){
        mediaPlayLib.nativeResume();
    }

//    private native void playRtsp(String url, Surface surface);
//    private native void nativeStopPlay();
//    private native void nativePause();
//    private native void nativeResume();
//    private native void nativeSetListener(MediaInfoListener listener);
}
