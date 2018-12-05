package com.adai.camera.bean;

/**
 * Created by huangxy on 2017/3/29.
 */

public class CameraFactoryRtnMsg {
    public final static int RET_NO_DEVICE = 0;
    public final static int RET_HAS_NOVATEK_DEVICE = 1;
    public final static int RET_HAS_ALLWINNER_DEVICE = 2;
    public final static int RET_HAS_SUNPLUS_DEVICE = 3;
    public final static int RET_HAS_SHENGMAI_DEVICE = 4;
    public final static int RET_HAS_MSTAR = 5;
    public final static int RET_HAS_GP_DEVICE = 6;
    public final static int RET_HAS_HISI_DEVICE = 7;
//    public final static int RET_HAS_JIELI_DEVICE = 8;

    public int ret = -1;

    public CameraFactoryRtnMsg() {
    }

    public CameraFactoryRtnMsg(int ret) {
        this.ret = ret;
    }
}
