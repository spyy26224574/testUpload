package com.ligo.medialib;

/**
 * Created by admin on 2016/11/9.
 */

public class VideoUtilLib {
    static{
        System.loadLibrary("ffmpeg");
        System.loadLibrary("video-util");
    }

    public native void cutVideo(String in_url,String out_url,int start,int end);

    public native void transcode(String in_url,String out_url,int bitrate,int width,int height);

    public native void release();
}
