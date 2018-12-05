package com.Hjni;

import android.content.res.AssetManager;

public class HbxFishEye {
    static {
        System.loadLibrary("hbxfisheye");
    }

    public static native float[] VertexfByFile(String name, AssetManager assetManager);

    /**
     * 初始化顶点数据
     *
     * @param path 标定数据存放位置,path c:\\project\\vertices
     */
    public static native void Init(String path);

    /**
     * 更新顶点数据
     */
    public static native void UpdateVertex();

    /**
     * 清空本地顶点数据
     */
    public static native void ClearVertex();

    /**
     * @param id     摄像头id
     * @param width  视频宽
     * @param height 视频高
     * @param type   展开类型 0-矩形，1-圆柱，2-半球
     * @return 顶点数据
     * @des 获取顶点数据
     */
    public static native float[] GetVertext(int id, int width, int height, int type);

    /**
     * @param fileName 文件路径
     * @des 保存id到文件
     */
    public static native void SaveId2File(String fileName, int type, int id);

    /**
     * 从文件中获取id
     *
     * @param fileName 文件路径
     */
    public static native int[] GetId(String fileName);

    public static native float[] GetAngel();

    /**
     * 获取calibration文件编码，用来更新calibration文件
     *
     * @param
     * @return
     */
    public static native int GetCalibrationId();

}
