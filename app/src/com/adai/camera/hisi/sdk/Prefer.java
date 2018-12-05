package com.adai.camera.hisi.sdk;

/**DV端设置和本地设置项*/
public class Prefer {

    //远端配置

    /**视频分辨率,  "1080P", "720P" */
    public String videoResolution ;
    public String frameRate;
    public String fieldOfView;
    public String videoMode;

    /**连拍时间， 1、3（秒）*/
    public int burstTime;

    /**连拍张数， 3、5、6、9、18（张）*/
    public int burstCount;

    /**定时拍照时间间隔，1、3、5、30、60（秒）*/
    public int timelapseInterval;

    /**延时拍照时间间隔，1、3、5、30、60（秒）*/
    public int timerCountDown;

    /**缩时录像几秒一帧*/
    public int recordTimelapse;

    /**屏幕倒计时休眠时间， 0（关闭）、1、3、5分钟*/
    public int screenAutoSleep;

    /*屏幕亮度, 100 60 30(百分比)*/
    public int screenBrightness;
    /**开机UI模式, 0 录像,  1 拍照,  2 定时拍照,  3 延时拍照,  4 连拍*/
    public int powerOnUiMode;

    public String wifiSSID;
    public String wifiPassword;
    public String autoShutdown;
    public String bootAction;

    public boolean audioCodec;
    public boolean imageUpsidedown;
    public boolean spotMetering;
    public boolean timeTag;
    public boolean ledFlicker;
    public boolean buzzerPrompt;

    //本地配置
    public String photoResolution;
    public String downloadVideo;
    public String previewVideo;

    public boolean soundPrompt;
    
    public String dvName;

    /**构造，默认值*/
    public Prefer()
    {
        videoResolution = "1080P";
        frameRate = "30";
        fieldOfView = "150";
        videoMode = "NTSC";
        burstTime = 1;
        burstCount = 5;
        timelapseInterval = 10;
        timerCountDown = 10;
        recordTimelapse = 10;
        screenAutoSleep = 0;
        screenBrightness = 100;
        powerOnUiMode = 0;
        wifiSSID = "";
        wifiPassword = "";
        autoShutdown = "0";
        bootAction = "idle";

        audioCodec = false;
        imageUpsidedown = false;
        spotMetering = false;
        timeTag = false;
        ledFlicker = true;
        buzzerPrompt = true;

        photoResolution = "1080P";
        downloadVideo = "Small";
        previewVideo = "Small";

        soundPrompt = true;
        dvName="";
    }
}
