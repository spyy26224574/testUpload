package com.ligo.medialib;

/**
 * @author huangxy
 * @date 2018/1/17 14:37.
 */

public class YUVRenderer {
    static {
        System.loadLibrary("media-lib");
    }

    public static final int VIDEO_TYPE_H264 = 0x11;
    private long player_id = -1;

    public YUVRenderer() {
        player_id = createYuvRenderer();
    }

    public void initGles() {
        if (player_id > 0) {
            initGles(player_id);
        }
    }

    public void release() {
        if (player_id > 0) {
            nativeRelease(player_id);
            player_id = -1;
        }
    }

    public void changeEsLayout(int width, int height) {
        if (player_id > 0) {
            changeESLayout(player_id, width, height);
        }
    }

    public int drawYuv(byte[] data, int size, int width, int height) {
        if (player_id > 0) {
            return nativeDrawYuv(player_id, data, size, width, height);
        }
        return -1;
    }

    private native long createYuvRenderer();

    private native void initGles(long id);

    private native void nativeRelease(long id);

    private native void changeESLayout(long id, int width, int height);

    private native int nativeDrawYuv(long id, byte[] data, int size, int width, int height);

}
