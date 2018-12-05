package com.adai.camera.hisi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.adai.camera.hisi.sdk.Command;
import com.adai.camera.hisi.sdk.Common;
import com.adai.camera.hisi.sdk.Prefer;
import com.adai.camera.hisi.sdk.RemoteFileManager;
import com.adai.camera.hisi.sdk.Setting;
import com.adai.camera.hisi.sdk.WorkModeConfig;
import com.adai.gkdnavi.utils.LogUtils;

import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.adai.camera.hisi.sdk.Common.BatteryInfo;
import static com.adai.camera.hisi.sdk.Common.CONFIG_MULTI_BURST_RATE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_MULTI_CONTINUOUS_RATE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_MULTI_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_MULTI_TIMELAPSE_INTERVAL;
import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_SCENE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_TIMER;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_LOOP_TYPE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_PHOTO_MODE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_PHOTO_PHOTO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_PHOTO_SNAP_INTERVAL;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_PHOTO_SNAP_MODE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_PHOTO_VIDEO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_TIMELAPSE_INTERVAL;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_VIDEO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.DeviceAttr;
import static com.adai.camera.hisi.sdk.Common.FAILURE;
import static com.adai.camera.hisi.sdk.Common.KEY_DOWNLOAD_VIDEO;
import static com.adai.camera.hisi.sdk.Common.KEY_PHOTO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.KEY_PREVIEW_VIDEO;
import static com.adai.camera.hisi.sdk.Common.KEY_SOUND_PROMPT;
import static com.adai.camera.hisi.sdk.Common.MASTER_MODE_MULTI;
import static com.adai.camera.hisi.sdk.Common.MASTER_MODE_PHOTO;
import static com.adai.camera.hisi.sdk.Common.MASTER_MODE_VIDEO;
import static com.adai.camera.hisi.sdk.Common.Result;
import static com.adai.camera.hisi.sdk.Common.SUCCESS;
import static com.adai.camera.hisi.sdk.Common.SdCardInfo;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_BURST;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_CONTINUOUS;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_RAW;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_SINGLE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_TIMER;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_LOOP;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_NORMAL;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_PHOTO;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_SLOW;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_IDLE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_RECORD;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_TIMER;

/**
 * Created by huangxy on 2017/11/2 18:12.
 */

public class HisiCamera {
    private static final String TAG = HisiCamera.class.getSimpleName();
    public static String DEFAULTE_IP = "192.168.0.1";
    public String ip;
    public String macAddress;
    public int workState;
    public int mode;  //当前工作模式（主模式和子模式）
    public int[] slaveWorkMode; //某个主模式下，最近所选择的子模式，例：slaveWorkMode[MASTER_MODE_PHOTO]
    public BatteryInfo batteryInfo;
    public DeviceAttr deviceAttr;
    public SdCardInfo sdCardInfo;
    public WorkModeConfig modeConfig;  //工作模式相关的设置项
    public Prefer prefer;   //普通设置项
    private String defaultIP;
    public volatile boolean isSleeping;
    public String capability;   //设备的能力集

    /**
     * 获取文件列表时，每次最大获取文件个数
     */
    private static final int MAX_GET_FILE_COUNT = 100;

    public HisiCamera() {
        setIP(DEFAULTE_IP);
        macAddress = null;
        defaultIP = DEFAULTE_IP;
        mode = WORK_MODE_PHOTO_SINGLE;
        slaveWorkMode = new int[3];
        workState = WORK_STATE_IDLE;
        sdCardInfo = null;
        batteryInfo = null;
        deviceAttr = new DeviceAttr();
        modeConfig = new WorkModeConfig();
        prefer = new Prefer();
        isSleeping = false;
        capability = "";
    }

    /**
     * 是否下载大文件，若是小文件则false
     */
    public boolean isDownloadBigVideo() {
        if (null == prefer.downloadVideo) {
            prefer.downloadVideo = "Small";
            return false;
        }

        if (prefer.downloadVideo.equals("Big")) {
            return true;
        } else if (prefer.downloadVideo.equals("Small")) {
            return false;
        } else {
            prefer.downloadVideo = "Small";
            return false;
        }
    }

    /**
     * 是否预览大码流，小码流false
     */
    public boolean isPreviewBigBitRate() {
//        if (null == prefer.previewVideo) {
//            prefer.previewVideo = "Small";
//            return false;
//        }
//
//        if (prefer.previewVideo.equals("Big")) {
//            return true;
//        } else if (prefer.previewVideo.equals("Small")) {
//            return false;
//        } else {
//            prefer.previewVideo = "Small";
//            return false;
//        }
        return false;
    }


    /**
     * 加载本地设置，下载时默认下载小视频文件，点播预览界面默认为高清
     */
    public void loadLocalPreferences(Context context) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);

        prefer.photoResolution = preference.getString(KEY_PHOTO_RESOLUTION, "1080P");
        prefer.downloadVideo = preference.getString(KEY_DOWNLOAD_VIDEO, "Small");
        prefer.previewVideo = preference.getString(KEY_PREVIEW_VIDEO, "Small");
        prefer.soundPrompt = preference.getBoolean(KEY_SOUND_PROMPT, true);
    }

    /**
     * 获取DV端的工作模式相关配置
     */
    public void loadRemoteWorkModeConfig() {
        modeConfig.videoNormalResolutionValues = getCapability(WORK_MODE_VIDEO_NORMAL, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoNormalResolution = getParameter(WORK_MODE_VIDEO_NORMAL, CONFIG_VIDEO_VIDEO_RESOLUTION);

        modeConfig.videoLoopResolutionValues = getCapability(WORK_MODE_VIDEO_LOOP, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoLoopResolution = getParameter(WORK_MODE_VIDEO_LOOP, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoLoopTypeValues = getCapability(WORK_MODE_VIDEO_LOOP, CONFIG_VIDEO_LOOP_TYPE);
        modeConfig.videoLoopType = getParameter(WORK_MODE_VIDEO_LOOP, CONFIG_VIDEO_LOOP_TYPE);

        modeConfig.videoTimelapseResolutionValues = getCapability(WORK_MODE_VIDEO_TIMELAPSE, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoTimelapseResolution = getParameter(WORK_MODE_VIDEO_TIMELAPSE, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoTimelapseIntervalValues = getCapability(WORK_MODE_VIDEO_TIMELAPSE, CONFIG_VIDEO_TIMELAPSE_INTERVAL);
        modeConfig.videoTimelapseInterval = getParameter(WORK_MODE_VIDEO_TIMELAPSE, CONFIG_VIDEO_TIMELAPSE_INTERVAL);

        modeConfig.videoPhotoVideoResolutionValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_VIDEO_RESOLUTION);
        modeConfig.videoPhotoVideoResolution = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_VIDEO_RESOLUTION);
        modeConfig.videoPhotoPhotoResolutionValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_PHOTO_RESOLUTION);
        modeConfig.videoPhotoPhotoResolution = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_PHOTO_RESOLUTION);
        modeConfig.videoPhotoSnapModeValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_SNAP_MODE);
        modeConfig.videoPhotoSnapMode = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_SNAP_MODE);
        modeConfig.videoPhotoSnapIntervalValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_SNAP_INTERVAL);
        modeConfig.videoPhotoSnapInterval = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_SNAP_INTERVAL);

        modeConfig.videoPhotoModeValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_MODE);
        modeConfig.videoPhotoMode = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_PHOTO_MODE);
        modeConfig.videoPhotoLapseInteralValues = getCapability(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_TIMELAPSE_INTERVAL);
        modeConfig.videoPhotoLapseInteral = getParameter(WORK_MODE_VIDEO_PHOTO, CONFIG_VIDEO_TIMELAPSE_INTERVAL);

        modeConfig.videoSlowResolutionValues = getCapability(WORK_MODE_VIDEO_SLOW, CONFIG_VIDEO_VIDEO_RESOLUTION);
        modeConfig.videoSlowResolution = getParameter(WORK_MODE_VIDEO_SLOW, CONFIG_VIDEO_VIDEO_RESOLUTION);

        modeConfig.photoSingleResolutionValues = getCapability(WORK_MODE_PHOTO_SINGLE, CONFIG_PHOTO_RESOLUTION);
        modeConfig.photoSingleResolution = getParameter(WORK_MODE_PHOTO_SINGLE, CONFIG_PHOTO_RESOLUTION);
        modeConfig.photoSingleSceneValues = getCapability(WORK_MODE_PHOTO_SINGLE, CONFIG_PHOTO_SCENE);
        modeConfig.photoSingleScene = getParameter(WORK_MODE_PHOTO_SINGLE, CONFIG_PHOTO_SCENE);

        modeConfig.photoTimerResolutionValues = getCapability(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_RESOLUTION);
        modeConfig.photoTimerResolution = getParameter(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_RESOLUTION);
        modeConfig.photoTimerSceneValues = getCapability(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_SCENE);
        modeConfig.photoTimerScene = getParameter(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_SCENE);
        modeConfig.photoTimerTimeValues = getCapability(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_TIMER);
        modeConfig.photoTimerTime = getParameter(WORK_MODE_PHOTO_TIMER, CONFIG_PHOTO_TIMER);

        modeConfig.photoRawResolutionValues = getCapability(WORK_MODE_PHOTO_RAW, CONFIG_PHOTO_RESOLUTION);
        modeConfig.photoRawResolution = getParameter(WORK_MODE_PHOTO_RAW, CONFIG_PHOTO_RESOLUTION);

        modeConfig.multiBurstResolutionValues = getCapability(WORK_MODE_MULTI_BURST, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiBurstResolution = getParameter(WORK_MODE_MULTI_BURST, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiBurstRateValues = getCapability(WORK_MODE_MULTI_BURST, CONFIG_MULTI_BURST_RATE);
        modeConfig.multiBurstRate = getParameter(WORK_MODE_MULTI_BURST, CONFIG_MULTI_BURST_RATE);

        modeConfig.multiTimelapseResolutionValues = getCapability(WORK_MODE_MULTI_TIMELAPSE, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiTimelapseResolution = getParameter(WORK_MODE_MULTI_TIMELAPSE, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiTimelapseIntervalValues = getCapability(WORK_MODE_MULTI_TIMELAPSE, CONFIG_MULTI_TIMELAPSE_INTERVAL);
        modeConfig.multiTimelapseInterval = getParameter(WORK_MODE_MULTI_TIMELAPSE, CONFIG_MULTI_TIMELAPSE_INTERVAL);

        modeConfig.multiContinuousResolutionValues = getCapability(WORK_MODE_MULTI_CONTINUOUS, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiContinuousResolution = getParameter(WORK_MODE_MULTI_CONTINUOUS, CONFIG_MULTI_RESOLUTION);
        modeConfig.multiContinuousRateValues = getCapability(WORK_MODE_MULTI_CONTINUOUS, CONFIG_MULTI_CONTINUOUS_RATE);
        modeConfig.multiContinuousRate = getParameter(WORK_MODE_MULTI_CONTINUOUS, CONFIG_MULTI_CONTINUOUS_RATE);
    }

    /**
     * 获取DV端的设置
     */
    public void loadRemotePreferences() {
        String strValue;
        int ret;
        Boolean boolValue;

        getDevCapabilities();
        getWorkMode();

        TreeMap<String, String> strMap = new TreeMap<String, String>();
        ret = getVideoInfo(strMap);
        if (ret == SUCCESS) {
            strValue = strMap.get("resolution");
            if (null != strValue) {
                prefer.videoResolution = strValue;
            }
            strValue = strMap.get("fps");
            if (null != strValue) {
                prefer.frameRate = strValue;
            }
        }

        ret = getViewField();
        if (ret == 170 || ret == 150) {
            prefer.fieldOfView = Integer.toString(ret);
        }

        strValue = getVideoMode();
        if (null != strValue) {
            prefer.videoMode = strValue;
        }

        TreeMap<String, Integer> intMap = new TreeMap<String, Integer>();
        ret = getBurstInfo(intMap);
        if (ret == SUCCESS) {
            Integer time = intMap.get("time");
            Integer count = intMap.get("count");
            if (null != time && count != null) {
                prefer.burstTime = time;
                prefer.burstCount = count;
            }
        }

        ret = getTimelapseInfo();
        if (ret > 0) {
            prefer.timelapseInterval = ret;
        }

        ret = getTimerInfo();
        if (ret > 0) {
            prefer.timerCountDown = ret;
        }

        ret = getRecordTimelapseInfo();
        if (ret >= 0) {
            prefer.recordTimelapse = ret;
        }

        ret = getAutoShutdown();
        if (ret >= 0) {
            prefer.autoShutdown = Integer.toString(ret);
        }

        strValue = getBootAction();
        if (null != strValue) {
            prefer.bootAction = strValue;
        }

        boolValue = getAudioEncode();
        if (null != boolValue) {
            prefer.audioCodec = boolValue;
        }

        boolValue = getFlip();
        if (null != boolValue) {
            prefer.imageUpsidedown = boolValue;
        }

        boolValue = getSpotMeter();
        if (null != boolValue) {
            prefer.spotMetering = boolValue;
        }

        boolValue = getLedState();
        if (null != boolValue) {
            prefer.ledFlicker = boolValue;
        }

        boolValue = getTimeOsd();
        if (null != boolValue) {
            prefer.timeTag = boolValue;
        }

        boolValue = getBuzzerState();
        if (null != boolValue) {
            prefer.buzzerPrompt = boolValue;
        }

        ret = getScreenAutoSleep();
        if (ret >= 0) {
            prefer.screenAutoSleep = ret;
        }

        ret = getScreenBrightness();
        if (ret > 0) {
            prefer.screenBrightness = ret;
        }

        ret = getPowerOnUiMode();
        if (ret >= 0 && ret <= 4) {
            prefer.powerOnUiMode = ret;
        }

        if (supportWorkMode()) {
            loadRemoteWorkModeConfig();
        }
    }

    /**
     * 获取DV的RTSP点播地址
     *
     * @return DV 播放地址
     */
    public String getVideoRtspURL() {
        StringBuilder builder = new StringBuilder("rtsp://");
        builder.append(ip);
        builder.append(":554/").append("livestream/");
        if (isPreviewBigBitRate()) {
            builder.append("11");
        } else {
            builder.append("12");
        }

        return builder.toString();
    }

    /**
     * 获取DV的私有协议点播地址
     *
     * @return DV 播放地址
     */
    public String getVideoHttpURL() {
        StringBuilder builder = new StringBuilder("http://");
        builder.append(ip);
        builder.append(":80/");
        if (isPreviewBigBitRate()) {
            builder.append("11?");
        } else {
            builder.append("12?");
        }
        builder.append("trans=tcp&action=play&media=video_data");

        return builder.toString();
    }


    /**
     * 获取HTTP下载文件的路径
     */
    public String getDownloadPath() {
        return "http://" + ip + "/";
    }


    /**
     * 执行DV指令（拍照，录像...）
     *
     * @param cmd 指令类型，范围： ACTION_RECORD_START(0)--ACTION_TIMER(7)
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public Result executeCommand(int cmd) {
        return Command.executeCommand(ip, cmd);
    }


    /**
     * 获取DV工作状态
     *
     * @return 工作状态 WORK_STATE_*
     * 失败返回-1
     */
    public int getWorkState() {
        String strState = Setting.getWorkState(ip);
        if (strState == null) {
            return FAILURE;
        }

        if (strState.equals("idle")) {
            return WORK_STATE_IDLE;
        } else if (strState.equals("record")) {
            return WORK_STATE_RECORD;
        } else if (strState.equals("timelapse")) {
            return WORK_STATE_TIMELAPSE;
        } else if (strState.equals("timer")) {
            return WORK_STATE_TIMER;
        } else {
            return FAILURE;
        }
    }


    /**
     * 获取文件名列表
     *
     * @param start 从第start个文件开始，最前的文件索引为1，不是从0开始
     * @param end   获取到第end个文件（文件已按名字排序）
     * @param list  输出参数，解析文件名后添加到list
     * @return 获取到的文件名总数，Results.FAILURE(-1)失败
     */
    public int getFileList(int start, int end, List<String> list) {
        return RemoteFileManager.getFileList(ip, start, end, list);
    }

    /**
     * 从DV端获取文件总数
     *
     * @return 文件总数，FAILURE(-1)失败
     */
    public int getFileCount() {
        return RemoteFileManager.getFileCount(ip);
    }

    /**
     * 获取所有文件名列表
     *
     * @param list 输出参数，解析文件名后添加到list
     * @return int 获取到的文件数量；-1表示失败
     */
    public int getAllFileNames(List<String> list) {
        int fileCount = 0;
        int parseCount = 0;

        /**获取文件数量*/
        fileCount = RemoteFileManager.getFileCount(ip);
        if (fileCount < 0) {
            return FAILURE;
        }

        /**获取所有文件名,分多次获取，每次MAX_GET_FILE_COUNT个*/
        for (int i = 1; i <= fileCount; i += MAX_GET_FILE_COUNT) {

            parseCount += RemoteFileManager.getFileList(ip, i, i + MAX_GET_FILE_COUNT - 1, list);

        }

        return parseCount;
    }


    /**
     * 获取指定文件的属性信息，大小、时长（视频）...
     *
     * @param file 文件名
     * @param map  输出参数，解析出的属性以键值对的形式放入map;
     *             文件大小，键"size",单位字节
     *             视频时长，键"time",图片无此项
     *             创建时间,键"create",格式： "2014/07/30 20:30:48", 板端新RDK改格式为"20140730203048"
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int getFileInfo(String file, Map<String, String> map) {
        return RemoteFileManager.getFileInfo(ip, file, map);
    }

    /**
     * 删除一个DV远端文件
     *
     * @param file 要删除的文件名
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int deleteFile(String file) {
        return RemoteFileManager.deleteFile(ip, file);
    }

    /**
     * 删除DV端的多个文件
     *
     * @param list 要删除的文件名列表
     * @return 删除成功的文件个数
     */
    public int deleteFiles(List<String> list) {
        int delCount = 0;
        int result = -1;

        for (String file : list) {

            result = RemoteFileManager.deleteFile(ip, file);

            if (SUCCESS == result) {
                delCount++;
            }
        }

        return delCount;
    }

    /**
     * 获取视频信息（分辨率，帧率）
     *
     * @param map 输出参数存放视频属性键值对;
     *            分辨率 键:resolution，
     *            帧率 键:fps
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int getVideoInfo(Map<String, String> map) {
        return Setting.getVideoInfo(ip, map);
    }

    /**
     * 设置视频信息（分辨率，帧率）
     *
     * @param resolution 分辨率 1080P/720P
     * @param frameRate  帧率 30/60
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setVideoInfo(String resolution, int frameRate) {
        return Setting.setVideoInfo(ip, resolution, frameRate);
    }

    /**
     * 获取电量百分比值
     *
     * @return 范围0~100, FAILURE(-1)失败
     */
    public int getBatteryInfo() {
        BatteryInfo info = Setting.getBatteryInfo(ip);
        if (null == info) {
            this.batteryInfo = null;
            return FAILURE;
        }

        this.batteryInfo = info;
        return SUCCESS;
    }


    /**
     * 获取SD卡状态
     *
     * @return SUCCESS成功，FAILURE(-1)失败，
     */
    public int getSdCardInfo() {
        SdCardInfo info = Setting.getSdState(ip);
        if (null == info) {
            this.sdCardInfo = null;
            return FAILURE;
        }

        this.sdCardInfo = info;
        return SUCCESS;
    }


    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }


    /**
     * 获取视场角:150度或170度
     *
     * @return 150/170; FAILURE(-1)失败
     */
    public int getViewField() {
        return Setting.getViewField(ip);
    }

    /**
     * 设置视场角
     *
     * @param viewField 150/170
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setViewField(int viewField) {
        return Setting.setViewField(ip, viewField);
    }

    /**
     * 获取循环录像状态，开或关
     *
     * @return Boolean对象：TRUE开，FALSE关，null失败
     */
    public Boolean getLoopRecord() {
        return Setting.getLoopRecord(ip);
    }

    /**
     * 设置循环录像状态（开/关）
     *
     * @param isLoopOn true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setLoopRecord(boolean isLoopOn) {
        return Setting.setLoopRecord(ip, isLoopOn);
    }

    /**
     * 获取图像翻转状态
     *
     * @return Boolean对象：TRUE开，FALSE关，null失败
     */
    public Boolean getFlip() {
        return Setting.getFlip(ip);
    }

    /**
     * 设置图像翻转
     *
     * @param isUpsideDown true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setFlip(boolean isUpsideDown) {
        return Setting.setFlip(ip, isUpsideDown);
    }


    /**
     * 获取连拍信息
     *
     * @param map 输出参数，以“time”,“count”为键存储连拍的时间和张数
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int getBurstInfo(Map<String, Integer> map) {
        return Setting.getBurstInfo(ip, map);
    }


    /**
     * 设置连拍信息。1秒3张、1秒5张、1秒6张、3秒9张、3秒18张
     *
     * @param time  连拍时间
     * @param count 连拍张数
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setBurstInfo(int time, int count) {
        return Setting.setBurstInfo(ip, time, count);
    }

    /**
     * 获取定时拍照间隔.1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @return int定时拍照间隔秒数， FAILURE(-1)失败
     */
    public int getTimelapseInfo() {
        return Setting.getTimelapseInfo(ip);
    }

    /**
     * 设置定时拍照间隔。1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @param time 定时拍照间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setTimelapseInfo(int time) {
        return Setting.setTimelapseInfo(ip, time);
    }

    /**
     * 获取延时拍照时间.1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @return int延时拍照秒数， FAILURE(-1)失败
     */
    public int getTimerInfo() {
        return Setting.getTimerInfo(ip);
    }

    /**
     * 设置延时拍照间隔。1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @param time 延时拍照间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setTimerInfo(int time) {
        return Setting.setTimerInfo(ip, time);
    }

    /**
     * 获取缩时录像多长时间一帧： 0（实际表示0.5），1秒、2秒，5秒，10秒，30秒，60秒
     *
     * @return int 秒数， FAILURE(-1)失败
     */
    public int getRecordTimelapseInfo() {
        return Setting.getRecordTimelapseInfo(ip);
    }


    /**
     * 设置缩时录像几秒钟一帧。 0(实际表示0.5)、1秒、2秒，5秒，10秒，30秒，60秒
     *
     * @param time 每帧间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setRecordTimelapseInfo(int time) {
        return Setting.setRecordTimelapseInfo(ip, time);
    }

    /**
     * 获取开机操作。开机录像、开机定时拍照、无操作
     *
     * @return null失败，成功时为BOOT_ACTION_IDLE，BOOT_ACTION_RECORD，BOOT_ACTION_TIMELAPSE之一
     */
    public String getBootAction() {
        return Setting.getBootAction(ip);
    }

    /**
     * 设置开机操作
     *
     * @param action BOOT_ACTION_IDLE无操作、BOOT_ACTION_RECORD开机录像、BOOT_ACTION_TIMELAPSE开机定时拍照
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setBootAction(String action) {
        return Setting.setBootAction(ip, action);
    }

    /**
     * 获取音频编码状态，开或关
     *
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public Boolean getAudioEncode() {
        return Setting.getAudioEncode(ip);
    }

    /**
     * 设置音频编码状态（开/关）
     *
     * @param isAudioOn true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setAudioEncode(boolean isAudioOn) {
        return Setting.setAudioEncode(ip, isAudioOn);
    }


    /**
     * 获取视频制式，NTSC/PAL
     *
     * @return String对象："NTSC"，"PAL"，null失败
     */
    public String getVideoMode() {
        return Setting.getVideoMode(ip);
    }


    /**
     * 设置视频制式，
     *
     * @param videoMode VIDEO_MODE_NTSC("NTSC"), VIDEO_MODE_PAL("PAL")
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setVideoMode(String videoMode) {
        return Setting.setVideoMode(ip, videoMode);
    }

    /**
     * 获取设备信息,数据存在deviceAttr
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int getDeviceAttr(DeviceAttr attr) {
        Map<String, String> map = new TreeMap<String, String>();

        if (FAILURE == Setting.getDeviceAttr(ip, map)) {
            return FAILURE;
        }

        String value = map.get("name");
        if (null != value) {
            attr.name = value;
        } else {
            return FAILURE;
        }

        value = map.get("serialnum");
        if (null != value) {
            attr.serialNum = value;
        }

        value = map.get("softversion");
        if (null != value) {
            attr.softVersion = value;
        }

        value = map.get("hardversion");
        if (null != value) {
            attr.hardVersion = value;
        }

        value = map.get("type");
        if (null != value) {
            attr.type = value;
        }
        LogUtils.e(attr.toString());
        deviceAttr = attr;
        return SUCCESS;
    }

    /**
     * 设置手机端时间到DV
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setPhoneTime2Camera() {
        GregorianCalendar calendar = new GregorianCalendar();
        return Setting.setSystemTime(ip, calendar);
    }

    /**
     * 获取中心点测光状态，开或关
     *
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public Boolean getSpotMeter() {
        return Setting.getSpotMeter(ip);
    }

    /**
     * 设置中心点测光开启或关闭
     *
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setSpotMeter(boolean enabled) {
        return Setting.setSpotMeter(ip, enabled);
    }

    /**
     * 获取时间OSD状态，开或关
     *
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public Boolean getTimeOsd() {
        return Setting.getTimeOsd(ip);
    }


    /**
     * 设置时间OSD开启或关闭
     *
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setTimeOsd(boolean enabled) {
        return Setting.setTimeOsd(ip, enabled);
    }

    /**
     * 获取LED闪烁提示状态，开或关
     *
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public Boolean getLedState() {
        return Setting.getLedState(ip);
    }

    /**
     * 设置LED闪烁提示状态：开启或关闭
     *
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setLedState(boolean enabled) {
        return Setting.setLedState(ip, enabled);
    }


    /**
     * 获取蜂鸣器状态，开或关
     *
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public Boolean getBuzzerState() {
        return Setting.getBuzzerState(ip);
    }

    /**
     * 设置蜂鸣器状态：开启或关闭
     *
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setBuzzerState(boolean enabled) {
        return Setting.setBuzzerState(ip, enabled);
    }

    /**
     * 删除DV端的所有文件
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int deleteAllFiles() {
        return RemoteFileManager.deleteAllFiles(ip);
    }

    /**
     * 升级
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int upgrade() {
        return RemoteFileManager.upgrade(ip);
    }

    /**
     * 格式华SD卡
     *
     * @param map 格式化后的SD卡容量等信息,不想接收时map为null
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     * map 空闲空间 键 "sdfreespace"
     * map 全部空间 键 "sdtotalspace"
     */
    public int formatSdCard(Map<String, String> map) {
        if (null == map) {
            map = new TreeMap<String, String>();
        }

        RemoteFileManager.formatSdCard(ip, map);

        String sdstatus = map.get("sdstatus");

        if (null != sdstatus && sdstatus.equals("1")) {
            return SUCCESS;
        } else {
            return FAILURE;
        }
    }

    /**
     * 恢复出厂设置
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public void restoreFactorySettings() {
        Setting.restoreFactorySettings(ip);
    }

    /**
     * 设置WIFI密码
     *
     * @param ssid WIFI名字
     * @param pwd  WIFI密码
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public void setWifiSsidPassword(String ssid, String pwd) {
        Setting.setWifi(ip, ssid, pwd);
    }

    /**
     * 设置STA模式
     *
     * @param password WIFI名字，null表示不设置
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setWifiToSta(String ssid, String password) {
        return Setting.setWifiToSta(ip, ssid, password);
    }

    /**
     * 设置AP模式
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setWifiToAp() {
        return Setting.setWifiToAp(ip);
    }

    /**
     * 获取定时关机时间. 0(关闭），1、5，10，30，60分钟
     *
     * @return int定时关机的当前时间， FAILURE(-1)失败
     */
    public int getAutoShutdown() {
        return Setting.getAutoShutdown(ip);
    }


    /**
     * 设置定时关机。
     *
     * @param time 定时关机时间, 0(关闭），1、5，10，30，60分钟
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setAutoShutdown(int time) {
        return Setting.setAutoShutdown(ip, time);
    }


    /**
     * 获取屏幕自动休眠倒计时时间
     *
     * @return int 0(关闭），1、3、5分钟， FAILURE(-1)失败
     */
    public int getScreenAutoSleep() {
        return Setting.getScreenAutoSleep(ip);
    }

    /**
     * 设置屏幕自动休眠倒计时时间
     *
     * @param time 0(关闭），1、3、5分钟
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setScreenAutoSleep(int time) {
        return Setting.setScreenAutoSleep(ip, time);
    }

    /**
     * 获取屏幕亮度
     *
     * @return int 100、60、30（百分比）， FAILURE(-1)失败
     */
    public int getScreenBrightness() {
        return Setting.getScreenBrightness(ip);
    }

    /**
     * 设置屏幕亮度
     *
     * @param brightness 100、60、30（百分比）
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setScreenBrightness(int brightness) {
        return Setting.setScreenBrightness(ip, brightness);
    }

    /**
     * 获取DV端开机时UI模式
     *
     * @return int  0 录像,  1 拍照,  2 定时拍照,  3 延时拍照,  4 连拍, -1失败
     */
    public int getPowerOnUiMode() {
        return Setting.getPowerOnUiMode(ip);
    }

    /**
     * 设置DV端开机时UI模式
     *
     * @param uimode 0 录像,  1 拍照,  2 定时拍照,  3 延时拍照,  4 连拍
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public int setPowerOnUiMode(int uimode) {
        if (uimode < 0 || uimode > 4) {
            return FAILURE;
        }
        return Setting.setPowerOnUiMode(ip, uimode);
    }

    public String getDefaultIP() {
        return defaultIP;
    }


    /**
     * 使用设备关机/休眠（板端可能不回复“Success”）
     */
    public int sleep() {
        return Setting.sleep(ip);
    }

    /**
     * 发送WakeOnLAN UDP魔术包，唤醒板端
     */
    public int wakeup() {
        if (macAddress == null) {
            Log.d(TAG, "MAC address is null, wakeup failed");
            return FAILURE;
        }

        byte[] hexMacAddress = Setting.macAddres2ByteArray(macAddress);
        if (null == hexMacAddress) {
            Log.d(TAG, "MAC address is invalid, wakeup aborted, MAC:" + macAddress);
            return FAILURE;
        }

        Log.d(TAG, "wake up , MAC:" + macAddress);

        Setting.wakeupDevice(ip, hexMacAddress);
        return SUCCESS;
    }

    public int setBitRate(int bitrate) {
        return Setting.setBitRate(ip, bitrate);
    }

    public int getBitRate() {
        return Setting.getBitRate(ip);
    }

    public int setWifiChannel(int channel) {
        return Setting.setWifiChannel(ip, channel);
    }

    public int getWifiChannel() {
        return Setting.getWifiChannel(ip);
    }

    /**
     * 获取设备能力集。如  表示是否可待机，是否有4g模块。
     *
     * @return "standby,4g"； 没有为 ""
     */
    public String getDevCapabilities() {
        capability = Setting.getDevCapabilities(ip);
        return capability;
    }

    /**
     * DV是否可以休眠和唤醒
     *
     * @return
     */
    public boolean supportWakeSleep() {

        if (null != capability && capability.contains("standby")) {
            return true;
        }

        //旧版本的DV中， type=“117”的， 都具有休眠和唤醒能力
        if (null != deviceAttr && Common.SENSOR_117.equals(deviceAttr.type)) {
            return true;
        }

        return false;
    }

    /**
     * 是否支持新的工作模式及相关设置项。前提：已用CGI获取过capability
     */
    public boolean supportWorkMode() {
        if (null != capability && capability.contains("workmode")) {
            return true;
        }
        return false;
    }

    /**
     * 获取当前工作模式
     *
     * @return 成功返回大于等于0的工作模式，失败返回 -1
     */
    public int getWorkMode() {
        Map<String, String> map = new TreeMap<String, String>();

        Setting.getWorkMode(ip, map);
        try {
            int workmode = Integer.parseInt(map.get("workmode"));
            int workmodephoto = Integer.parseInt(map.get("workmodephoto"));
            int workmodemulti = Integer.parseInt(map.get("workmodemulti"));
            int workmodevideo = Integer.parseInt(map.get("workmodevideo"));

            this.mode = workmode;
            this.slaveWorkMode[MASTER_MODE_PHOTO] = workmodephoto;
            this.slaveWorkMode[MASTER_MODE_MULTI] = workmodemulti;
            this.slaveWorkMode[MASTER_MODE_VIDEO] = workmodevideo;
        } catch (Exception e) {
            e.printStackTrace();
            return FAILURE;
        }

        return SUCCESS;
    }

    /**
     * 设置新工作模式
     *
     * @param workmode WORK_MODE_XXX
     * @return 成功返回SUCCESS(0), 失败返回FAILURE(-1)
     */
    public int setWorkMode(int workmode) {
        return Setting.setWorkMode(ip, workmode);
    }

    /**
     * 获取特定工作模式下特定配置项的能力集合
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项
     * @return 成功返回字符串：”xxx,xxx,xxx,…” ， 失败返回null
     */
    public String getCapability(int workmode, int type) {
        return Setting.getCapability(ip, workmode, type);
    }

    /**
     * 获取特定工作模式下特定配置项的数值
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项
     * @return 成功返回字符串：”xxx” ， 失败返回null
     */
    public String getParameter(int workmode, int type) {
        return Setting.getParameter(ip, workmode, type);
    }

    /**
     * 设置特定工作模式下特定配置项的数值
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项，参考DV端代码的枚举定义值，定义于COMMON.CONFIG_XXX.
     * @param value    配置项的值
     * @return 成功返回SUCCESS(0), 失败FAILURE-1
     */
    public int setParameter(int workmode, int type, String value) {
        return Setting.setParameter(ip, workmode, type, value);
    }
}
