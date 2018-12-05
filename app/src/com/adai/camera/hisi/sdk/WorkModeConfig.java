package com.adai.camera.hisi.sdk;

/**
 * 工作模式相关的设置值和能力集
 * 字段的命名结构： 主模式 子模式 配置项； values表示能力集,如 "A,B,C,D"
 */
public class WorkModeConfig {

    public String videoNormalResolutionValues;  //能力集
    public String videoNormalResolution;        //当前值

    public String videoLoopResolutionValues;
    public String videoLoopResolution;

    public String videoTimelapseResolutionValues;
    public String videoTimelapseResolution;
    public String videoTimelapseIntervalValues;
    public String videoTimelapseInterval;

    //下面连续的几个在3559上不使用
    public String videoPhotoVideoResolutionValues;
    public String videoPhotoVideoResolution;        //视频+拍照模式下的视频分辨率
    public String videoPhotoPhotoResolutionValues;
    public String videoPhotoPhotoResolution;        //视频+拍照模式下的拍照分辨率
    public String videoPhotoSnapModeValues;
    public String videoPhotoSnapMode;
    public String videoPhotoSnapIntervalValues;
    public String videoPhotoSnapInterval;

    //目前仅出现在3559上--------------------------
    public String videoPhotoModeValues;
    public String videoPhotoMode;
    public String videoPhotoLapseInteralValues;
    public String videoPhotoLapseInteral;
    public String videoLoopTypeValues;
    public String videoLoopType;
    //------------------------------------------

    public String videoSlowResolutionValues;
    public String videoSlowResolution;

    public String photoSingleResolutionValues;
    public String photoSingleResolution;
    public String photoSingleSceneValues;
    public String photoSingleScene;

    public String photoTimerResolutionValues;
    public String photoTimerResolution;
    public String photoTimerSceneValues;
    public String photoTimerScene;
    public String photoTimerTimeValues;
    public String photoTimerTime;

    public String photoRawResolutionValues;
    public String photoRawResolution;

    public String multiBurstResolutionValues;
    public String multiBurstResolution;
    public String multiBurstRateValues;
    public String multiBurstRate;

    public String multiTimelapseResolutionValues;
    public String multiTimelapseResolution;
    public String multiTimelapseIntervalValues;
    public String multiTimelapseInterval;

    public String multiContinuousResolutionValues;
    public String multiContinuousResolution;
    public String multiContinuousRateValues;
    public String multiContinuousRate;
}
