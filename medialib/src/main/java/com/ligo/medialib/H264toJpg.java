package com.ligo.medialib;

/**
 * @author huangxy
 * @date 2018/7/11 15:28.
 */
public class H264toJpg {
    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("media-lib");
    }

    public native static int nativeInitDecorder();

    public native static int nativeDecodeFrame(byte[] data, int len, String out_path);

    public native static void nativeDeInitDecoder();
}
