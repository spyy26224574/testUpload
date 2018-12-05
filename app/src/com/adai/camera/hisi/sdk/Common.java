package com.adai.camera.hisi.sdk;


import com.adai.gkdnavi.R;

/**
 * 业务层相关的通用配置，常量
 * 为解决代码重复而抽取出来的公用方法
 */

public class Common {

    public static final String CGI_PATH = "/cgi-bin/hi3510";
    public static final String ICGI_PATH = "/cgi-bin";

    public static final String DATA_DIRECTORY_NAME = "ActionCam";

    public static final int SD_STATE_OK = 0;
    public static final int SD_STATE_FULL = 1;
    public static final int SD_STATE_NONE = 2;
    public static final int SD_STATE_ERROR = 3;

    //客户端的工作状态表示, 简化了DV端的state表示
    public static final int WORK_STATE_RECORD = 0;
    public static final int WORK_STATE_TIMELAPSE = 1;
    public static final int WORK_STATE_TIMER = 2;
    public static final int WORK_STATE_IDLE = 3;
    public static final int WORK_STATE_VIDEO_LOOP = 4; //循环录像
    public static final int WORK_STATE_VIDEO_TIMELAPSE = 5; //缩时录像

    //DV主动发送来的异常事件
    public static final int EVENT_NORMAL = 0;
    public static final int EVENT_RECORD_SPACE_FULL = 1;
    public static final int EVENT_RECORD_ERROR = 2;
    public static final int EVENT_SNAPSHOT_SPACE_FULL = 3;
    public static final int EVENT_SNAPSHOT_ERROR = 4;
    public static final int EVENT_SDCARD_NOT_EXIST = 5;
    public static final int EVENT_SDCARD_ERROR = 6;
    public static final int EVENT_CHIP_TEMPERATURE_HIGH = 7;
    public static final int EVENT_BATTERY_TEMPERATURE_HIGH = 8;
    public static final int EVENT_LOW_POWER = 9;
    public static final int EVENT_USB_CONNECTED = 10;
    public static final int EVENT_SHUTDOWN = 11;
    public static final int EVENT_USB_DISCONNECTED = 12;
    //以下三个为普通告警
    public static final int EVENT_CHIP_TEMPERATURE_ALARM = 13;
    public static final int EVENT_BATTERY_TEMPERATURE_ALARM = 14;
    public static final int EVENT_LOW_POWER_ALARM = 15;

    public static final int EVENT_SDCARD_MOUNTED = 16;
    public static final int EVENT_AC_ON = 17;
    public static final int EVENT_AC_OFF = 18;

    public static final int EVENT_INVALID = 19;

    //下面的字符串资源下标对应EVENT_* 数值
    public static final int[] aEventStringRes = new int[]{
            R.string.event_normal,
            R.string.wifi_camera_storage,
            R.string.event_record_error,
            R.string.wifi_camera_storage,
            R.string.event_snapshot_error,
            R.string.wifi_sdcard,
            R.string.event_sdcard_error,
            R.string.event_chip_temperature_high,
            R.string.event_battery_temperature_high,
            R.string.event_low_power,
            R.string.event_usb_connected,
            R.string.event_shutdown,
            R.string.event_usb_disconnected,
            R.string.event_chip_temperature_alarm,
            R.string.event_battery_temperature_alarm,
            R.string.event_low_power_alarm,
            R.string.event_sdcard_mounted,
            R.string.event_ac_on,
            R.string.event_ac_off,
    };

    //录像、拍照时CGI返回的错误码，数值来自DV端
    public static final int ERR_NO_SD = 0xA3018000;
    public static final int ERR_SD_FULL = 0xA3018001;
    public static final int ERR_SD_ERROR = 0xA3018002;
    public static final int ERR_RECORD_NO_SPACE = 0xA3018003;
    public static final int ERR_LOOP_NO_SPACE = 0xA3018004;
    public static final int ERR_SANPSHOT_NO_SPACE = 0xA3018005;
    public static final int ERR_GET_CHANNEL_STATE_FAIL = 0xA3018006;
    public static final int ERR_CHANNEL_BUSY = 0xA3018007;
    public static final int ERR_START_CHANNEL_FAIL = 0xA3018008;
    public static final int ERR_STOP_CHANNEL_FAIL = 0xa3018009;
    public static final int ERR_SNAPSHOT_PRARM_ERROR = 0xa301800A;

    public static final String BOOT_ACTION_IDLE = "idle";
    public static final String BOOT_ACTION_RECORD = "record";
    public static final String BOOT_ACTION_TIMELAPSE = "timelapse";

    public static final String VIDEO_MODE_NTSC = "NTSC";
    public static final String VIDEO_MODE_PAL = "PAL";

    public static final String SENSOR_117 = "117";
    public static final String SENSOR_34220 = "34220";

    public static final String KEY_MASTER_MODE_VIDEO_GROUP = "key_master_mode_video_group";
    public static final String KEY_MASTER_MODE_PHOTO_GROUP = "key_master_mode_photo_group";
    public static final String KEY_MASTER_MODE_MULTI_GROUP = "key_master_mode_multi_group";

    public static final String KEY_MODE_VIDEO_NORMAL_GROUP = "key_mode_video_normal_group";
    public static final String KEY_MODE_VIDEO_LOOP_GROUP = "key_mode_video_loop_group";
    public static final String KEY_MODE_VIDEO_TIMELAPSE_GROUP = "key_mode_video_timelapse_group";
    public static final String KEY_MODE_VIDEO_PHOTO_GROUP = "key_mode_video_photo_group";
    public static final String KEY_MODE_VIDEO_SLOW_GROUP = "key_mode_video_slow_group";
    public static final String KEY_MODE_PHOTO_SINGLE_GROUP = "key_mode_photo_single_group";
    public static final String KEY_MODE_PHOTO_TIMER_GROUP = "key_mode_photo_timer_group";
    public static final String KEY_MODE_PHOTO_RAW_GROUP = "key_mode_photo_raw_group";
    public static final String KEY_MODE_MULTI_BURST_GROUP = "key_mode_multi_burst_group";
    public static final String KEY_MODE_MULTI_TIMELAPSE_GROUP = "key_mode_multi_timelapse_group";
    public static final String KEY_MODE_MULTI_CONTINUOUS_GROUP = "key_mode_multi_continuous_group";

    public static final String KEY_MODE_VIDEO_NORMAL_RESOLUTION = "key_mode_video_normal_resolution";
    public static final String KEY_MODE_VIDEO_LOOP_RESOLUTION = "key_mode_video_loop_resolution";
    public static final String KEY_MODE_VIDEO_LOOP_TYPE = "key_mode_video_loop_type";
    public static final String KEY_MODE_VIDEO_TIMELAPSE_RESOLUTION = "key_mode_video_timelapse_resolution";
    public static final String KEY_MODE_VIDEO_TIMELAPSE_INTERVAL = "key_mode_video_timelapse_interval";
    public static final String KEY_MODE_VIDEO_PHOTO_VIDEO_RESOLUTION = "key_mode_video_photo_video_resolution";
    public static final String KEY_MODE_VIDEO_PHOTO_PHOTO_RESOLUTION = "key_mode_video_photo_photo_resolution";
    public static final String KEY_MODE_VIDEO_PHOTO_SNAP_MODE = "key_mode_video_photo_snap_mode";
    public static final String KEY_MODE_VIDEO_PHOTO_SNAP_INTERVAL = "key_mode_video_photo_snap_interval";
    public static final String KEY_MODE_VIDEO_PHOTO_MODE = "key_mode_video_photo_mode";
    public static final String KEY_MODE_VIDEO_PHOTO_LAPSE_INTERVAL = "key_mode_video_photo_lapse_interval";
    public static final String KEY_MODE_VIDEO_SLOW_RESOLUTION = "key_mode_video_slow_resolution";

    public static final String KEY_MODE_PHOTO_SINGLE_RESOLUTION = "key_mode_photo_single_resolution";
    public static final String KEY_MODE_PHOTO_SINGLE_SCENE = "key_mode_photo_single_scene";
    public static final String KEY_MODE_PHOTO_TIMER_RESOLUTION = "key_mode_photo_timer_resolution";
    public static final String KEY_MODE_PHOTO_TIMER_SCENE = "key_mode_photo_timer_scene";
    public static final String KEY_MODE_PHOTO_TIMER_TIME = "key_mode_photo_timer_time";
    public static final String KEY_MODE_PHOTO_RAW_RESOLUTION = "key_mode_photo_raw_resolution";

    public static final String KEY_MODE_MULTI_BURST_RESOLUTION = "key_mode_multi_burst_resolution";
    public static final String KEY_MODE_MULTI_BURST_RATE = "key_mode_multi_burst_rate";
    public static final String KEY_MODE_MULTI_TIMELAPSE_RESOLUTION = "key_mode_multi_timelapse_resolution";
    public static final String KEY_MODE_MULTI_TIMELAPSE_INTERVAL = "key_mode_multi_timelapse_interval";
    public static final String KEY_MODE_MULTI_CONTINUOUS_RESOLUTION = "key_mode_multi_continuous_resolution";
    public static final String KEY_MODE_MULTI_CONTINUOUS_RATE = "key_mode_multi_continuous_rate";


    //各工作模式下配置项的配置ID，值与板端枚举定义一致。CONFIG_主模式_配置项
    public static final int CONFIG_VIDEO_VIDEO_RESOLUTION = 0;  //配置视频分辨率
    public static final int CONFIG_VIDEO_TIMELAPSE_INTERVAL = 1;  //配置缩时录像间隔（也适用3559录像抓拍的间隔）
    public static final int CONFIG_VIDEO_PHOTO_SNAP_MODE = 2;  //配置录像+拍照的抓拍模式(仅3519)
    public static final int CONFIG_VIDEO_PHOTO_SNAP_INTERVAL = 3;  //配置录像+拍照的抓拍间隔(仅3519)
    public static final int CONFIG_VIDEO_PHOTO_PHOTO_RESOLUTION = 4;  //配置录像+拍照的拍照分辨率(仅3519)
    public static final int CONFIG_VIDEO_PHOTO_VIDEO_RESOLUTION = 5;  //配置录像+拍照的视频分辨率(仅3519)
    public static final int CONFIG_VIDEO_PHOTO_MODE = 6;  //配置录像+拍照的模式(新，3559)
    public static final int CONFIG_VIDEO_LOOP_TYPE = 7;  //配置循环录像类型(新，3559)

    public static final int CONFIG_PHOTO_RESOLUTION = 0;  //配置拍照模式下的照片分辨率
    public static final int CONFIG_PHOTO_TIMER = 1;  //配置拍照模式下的倒计时时间
    public static final int CONFIG_PHOTO_SCENE = 2;  //配置拍照模式下的场景

    public static final int CONFIG_MULTI_RESOLUTION = 0;  //配置多拍模式下各子模式的分辨率
    public static final int CONFIG_MULTI_BURST_RATE = 1;  //配置BURST速率
    public static final int CONFIG_MULTI_TIMELAPSE_INTERVAL = 2;  //配置TIMELAPSE间隔
    public static final int CONFIG_MULTI_CONTINUOUS_RATE = 3;  //配置CONTINUOUS速率


    public static final String KEY_REC_SETTING = "rec_setting";
    public static final String KEY_VIDEO_RESOLUTION = "video_resolution";
    public static final String KEY_FRAME_RATE = "frame_rate";
    public static final String KEY_FIELD_OF_VIEW = "field_of_view";
    public static final String KEY_VIDEO_MODE = "video_mode";
    public static final String KEY_AUDIO_CODEC = "audio_codec";

    public static final String KEY_PHOTO_RESOLUTION = "photo_resolution";
    public static final String KEY_BURST_RATE = "burst_rate";
    public static final String KEY_TIMELAPSE_INTERVAL = "timelapse_interval";
    public static final String KEY_TIMER_COUNT_DOWN = "timer_count_down";

    public static final String KEY_IMAGE_UPSIDEDOWN = "image_upsidedown";
    public static final String KEY_SPOT_METERING = "spot_metering";
    public static final String KEY_TIME_TAG = "time_tag";

    public static final String KEY_DELETE_ALL_FILES = "delete_all_files";
    public static final String KEY_FORMAT_SD_CARD = "format_sd_card";

    public static final String KEY_WIFI_SSID = "wifi_ssid";
    public static final String KEY_WIFI_PASSWORD = "wifi_password";

    public static final String KEY_AUTO_SHUTDOWN = "auto_shutdown";
    public static final String KEY_SCREEN_AUTO_SLEEP = "screen_auto_sleep";
    public static final String KEY_SCREEN_BRIGHTNESS = "screen_brightness";
    public static final String KEY_POWERON_UI_MODE = "poweron_ui_mode";
    public static final String KEY_BOOT_ACTION = "boot_action";
    public static final String KEY_LED_FLICKER = "led_flicker";
    public static final String KEY_BUZZER_PROMPT = "buzzer_prompt";

    public static final String KEY_SET_DATETIME = "set_datetime";
    public static final String KEY_RESTORE_SETTINGS = "restore_settings";
    public static final String KEY_ABOUT_CAMERA = "about_camera";

    public static final String KEY_DOWNLOAD_VIDEO = "download_video";
    public static final String KEY_PREVIEW_VIDEO = "preview_video";
    public static final String KEY_SOUND_PROMPT = "sound_prompt";
    public static final String KEY_CLEAR_CACHE = "clear_cache";

    public static final String KEY_MODIFY_DV_NAME = "modify_dv_name";


    public static final int MASTER_MODE_PHOTO = 0; //主模式 PHOTO
    public static final int MASTER_MODE_MULTI = 1; //主模式 MULTI
    public static final int MASTER_MODE_VIDEO = 2; //主模式 VIDEO
    public static final int MASTER_MODE_PLAY = 3; //主模式 PLAYBACK

    public static final int WORK_MODE_PHOTO_SINGLE = 0;
    public static final int WORK_MODE_PHOTO_TIMER = 1;
    public static final int WORK_MODE_PHOTO_RAW = 2;

    public static final int WORK_MODE_MULTI_BURST = 10;
    public static final int WORK_MODE_MULTI_TIMELAPSE = 11;
    public static final int WORK_MODE_MULTI_CONTINUOUS = 12;

    public static final int WORK_MODE_VIDEO_NORMAL = 20;
    public static final int WORK_MODE_VIDEO_LOOP = 21;
    public static final int WORK_MODE_VIDEO_TIMELAPSE = 22;
    public static final int WORK_MODE_VIDEO_PHOTO = 23;
    public static final int WORK_MODE_VIDEO_SLOW = 24;


    public static final int SUCCESS = 0;
    public static final int FAILURE = -1;


    public static String CURRENT_AV_TRANSPORT_URI = "currentPath";
    public static String CURRENT_MEDIA_PATHS = "currentPaths";


    /**
     * 设备信息，用于储存getDeviceAttr返回的数据
     */
    public static class DeviceAttr {


        public String name;
        public String serialNum;
        public String softVersion;
        public String hardVersion;
        public String type;

        public DeviceAttr() {
            name = "";
            serialNum = "";
            softVersion = "";
            hardVersion = "";
            type = SENSOR_117;
        }

        @Override
        public String toString() {
            return "DeviceAttr{" +
                    "name='" + name + '\'' +
                    ", serialNum='" + serialNum + '\'' +
                    ", softVersion='" + softVersion + '\'' +
                    ", hardVersion='" + hardVersion + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    /**
     * SD卡信息
     */
    public static class SdCardInfo {
        public int sdState; //SD_STATE_*
        public int total; //总大小，单位 MB
        public int used;  //已使用量， 单位MB

        public SdCardInfo() {
            sdState = SD_STATE_OK;
            total = -1;
            used = -1;
        }
    }

    /**
     * 电池信息
     */
    public static class BatteryInfo {
        public int capactiy;     //电量0~100
        public boolean bCharging; //充电状态
        public boolean bAC;      //true表示接充电器

        public BatteryInfo() {
            capactiy = 0;
            bCharging = false;
            bAC = false;
        }
    }

    /**
     * 执行录像拍照等指令时的结果
     */
    public static class Result {
        public int returnCode;   //执行成功SUCCESS,失败FAILURE
        public int errorCode; //返回的错误码
        public int cmd;//命令

        public Result() {
            returnCode = FAILURE;
            errorCode = -1;
            cmd = -1;
        }
    }
}
