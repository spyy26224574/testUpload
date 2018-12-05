package com.ligo.medialib;

import android.view.Surface;

/**
 * Created by admin on 2018-01-05.
 */

public class VideoDecoder {
    static {
        System.loadLibrary("media-lib");
        System.loadLibrary("ffmpeg");
    }

    public static final int VIDEO_TYPE_H264 = 0x11;
    private long player_id;

    public VideoDecoder(int width, int height) {
        player_id = createDecoder(VIDEO_TYPE_H264, width, height);
    }

    public void setSurface(Surface surface, int width, int height) {
        if (player_id > 0) {
            nativeSetSurface(player_id, surface, width, height);
        }
    }

    public void start() {
        if (player_id > 0) {
            nativeStart(player_id);
        }
    }

    public void decodeVideo(byte[] data, int len) {
        if (player_id > 0) {
            nativeDecodeVideo(player_id, data, len);
        }
    }

    public void initGles(int width, int height) {
        if (player_id > 0) {
            initGles(player_id, width, height);
        }
    }

    public void changeESLayout(int width, int height) {
        if (player_id > 0) {
            changeESLayout(player_id, width, height);
        }
    }

    public int drawESFrame() {
        if (player_id > 0) {
            return drawESFrame(player_id);
        }
        return -1;
    }

    public int drawYuv(byte[] data, int size) {
        if (player_id > 0) {
            return nativeDrawYuv(player_id, data, size);
        }
        return -1;
    }
    public void release() {
        if (player_id > 0) {
            nativeRelease(player_id);
            player_id = -1;
        }
    }

    private native long createDecoder(int videoType, int width, int height);

    private native void nativeSetSurface(long id, Surface surface, int width, int height);

    private native void nativeStart(long id);

    private native void nativeDecodeVideo(long id, byte[] data, int len);

    private native void nativeRelease(long id);

    private native void initGles(long id, int width, int height);

    private native void changeESLayout(long id, int width, int height);

    private native int drawESFrame(long id);

    private native int nativeDrawYuv(long id, byte[] data, int size);
}
