package com.ligo.medialib.opengl;

/**
 * @author huangxy
 * @date 2018/9/4 14:48.
 */
public class ShapeManagerJni {
    static {
        System.loadLibrary("shape-lib");
    }

    /**
     *
     * @param width
     * @param height
     * @param centerX
     * @param centerY
     * @param radius
     * @param type 0-半球，1-圆柱
     * @return
     */
    public static native float[] GetVertext(int width, int height, float centerX, float centerY, float radius,int type);
}
