package com.adai.camera.novatek.consant;

/**
 * Created by huangxy on 2017/8/7 11:50.
 */

public final class NovatekWifiCommands {
    //Photo mode
    public static final int PHOTO_CAPTURE = 1001;
    public static final int PHOTO_SET_CAPTURE_SIZE = 1002;
    public static final int PHOTO_GET_FREE_CAPTURE_NUMBER = 1003;
    public static final int PHOTO_SHOW_DATE_TIME = 1101; //NT96660

    //Movie mode
    public static final int MOVIE_RECORD = 2001;    //needs to restart live view
    public static final int MOVIE_SET_RECORD_SIZE = 2002;
    public static final int MOVIE_CYCLIC_RECORD = 2003;
    public static final int MOVIE_HDR = 2004;    //高反差摄影
    public static final int MOVIE_SET_EV = 2005;    //整体曝光补偿: Needs to restart live view
    public static final int MOVIE_MOTION_DETECT = 2006;    //It is effective when recording
    public static final int MOVIE_RECORD_AUDIO = 2007;
    public static final int MOVIE_DATE_PRINT = 2008;
    public static final int MOVIE_GET_MAX_RECORD_TIME = 2009;
    public static final int MOVIE_SET_LIVE_VIEW_SIZE = 2010; //Not supported in NT96660
    public static final int MOVIE_GSENSOR = 2011;
    public static final int MOVIE_AUTO_RECORD = 2012;
    public static final int MOVIE_SET_RECORD_BITRATE = 2013;
    public static final int MOVIE_SET_LIVEVIEW_BITRATE = 2014;
    public static final int MOVIE_LIVE_VIEW = 2015;
    public static final int MOVIE_GET_RECORD_TIME = 2016;
    public static final int MOVIE_RAW_ENCODE_SAVE_JPEG = 2017; //NT96660
    public static final int MOVIE_GET_RAW_ENCODE_JPEG = 2018; //NT96660
    public static final int GET_STREAM_URL = 2019;//NT9666X
    public static final int IMAGE_QUALITY = 2024;//图片质量
    public static final int MOVIE_TIME_LAPSE = 2101; //缩时录像
    public static final int MOVIE_SLOW_MOTION = 2102;
    public static final int MOVIE_CYCLIC_RECORD_SWITCH = 8902;//循环录制开关
    public static final int MOVIE_MOVIE_FOV = 8904;
    public static final int MOVIE_RECORD_MODE = 8903;//0-监控模式(5fps) 1-标准模式
    public static final int DELAY_REC = 9803;//延时录像

    //Setup
    public static final int CAMERA_MODE_CHANGE = 3001;    //0-photo, 1-movie, 2-playback, 3-cmd
    public static final int CAMERA_QUERY_COMMANDS = 3002;
    public static final int CAMERA_SET_WIFI_SSID = 3003;    //32 bytes
    public static final int CAMERA_SET_WIFI_PASSWORD = 3004;    //26 bytes
    public static final int CAMERA_SET_DATE = 3005;   //yyyy-mm-dd
    public static final int CAMERA_SET_TIME = 3006;   //hh:mm:ss
    public static final int CAMERA_POWER_OFF = 3007;
    public static final int CAMERA_SET_LANGUAGE = 3008;
    public static final int CAMERA_SET_TV_FORMAT = 3009;  //NTSC, PAL
    public static final int CAMERA_FORMAT = 3010;  //1-SDCard, 0-flash
    public static final int CAMERA_RESET_SETTINGS = 3011;
    public static final int CAMERA_GET_VERSION = 3012;
    public static final int CAMERA_FIRMWARE_UPDATE = 3013;
    public static final int CAMERA_QUERY_STATUS = 3014;
    public static final int CAMERA_GET_FILE_LIST = 3015;
    public static final int CAMERA_IS_ALIVE = 3016;   //return result when it is alive, or nothing
    public static final int CAMERA_GET_DISK_FREE_SPACE = 3017;
    public static final int CAMERA_RECONNECT_WIFI = 3018;  //No return value
    public static final int CAMERA_GET_BATTERY_LEVEL = 3019;
    public static final int CAMERA_NOTIFY_STATUS = 3020;   //Notify system error, socket connection only.
    public static final int CAMERA_SAVE_MENU_SETTINGS = 3021;
    public static final int CAMERA_HARD_WARE_COMPONENTS = 3022; //NT96660
    public static final int CAMERA_REMOVE_LAST_USER = 3023; //NT96660
    public static final int CAMERA_CARD_STATUS = 3024; //NT96660: 0-removed, 1-inserted, 2-locked
    public static final int CAMERA_FIRMWARE_DOWNLOAD_URL = 3025; //NT96660
    public static final int CAMERA_FIRMWARE_UPDATE_PATH = 3026; //NT96660
    public static final int CAMERA_HFS_UPLOAD_FILE_RESULT = 3027; //NT96660
    public static final int CAMERA_PIP_STYLE = 3028; //NT96660: DUALCAM_FRONT, DUALCAM_BEHIND, DUALCAM_BOTH, DUALCAM_BOTH2，0-前，1-前后，2-后前，3-后，4-后前，5-前后
    public static final int CAMERA_GET_SSID_AND_PASSPHRASE = 3029; //NT96660: get ssid and passphrase
    public static final int CAMERA_RECORD_SIZE_LIST = 3030; //NT96660
    public static final int CAMERA_QUERY_MENU_ITEM = 3031; //NT96660: except movie record size list
    public static final int CAMERA_SENDSSIDANDPASSWORD = 3032; //NT96660
    public static final int CAMERA_WIFIMODE = 3033; //NT96660: 0-AP, 1-Station
    public static final int CAMERA_CONNECT = 3035;//时隽
    public static final int CAMERA_DISCONNECT = 3036;//时隽
    public static final int CAMERA_PARKINGGUARD = 3101;
    public static final int CAMERA_ANTISHAKE = 3102; //防斗
    public static final int CAMERA_FREQUENCY = 3103; //光源频率: 一般不开放
    public static final int CAMERA_CARMODE = 3104;
    public static final int CAMERA_GET_SD_SPACE = 8106;
    public static final int AUDIO_MUTE = 8901;//禁止语音播报
    //Playback mode;
    public static final int PLAYBACK_GET_THUMBNAIL = 4001;  //url?custom=1&cmd=4001
    public static final int PLAYBACK_GET_SCREEN_SHOT = 4002;
    public static final int PLAYBACK_DELETE_FILE = 4003;
    public static final int PLAYBACK_DELETE_ALL_FILES = 4004;
    public static final int PLAYBACK_MOVIE_FILE_INFO = 4005; //NT96660: width, height and length

    //Upload;
    public static final int FIRMWARE_UPDATE = 5001; //NT96660
    public static final int MAC_ADDRESS = 8000;
    public static final int PARKING_MONITOR = 8020;//停车监控，时隽
    public static final int CAMERA_GET_NEW_VERSION = 8567;
    public static final int CAMERA_SET_SENSOR_ROTATE = 9984; //旋转自动摆正图像
    public static final int CAMERA_SET_SHARPNESS = 9985; //Sharpness10
    public static final int CAMERA_SET_COLOR = 9986;  //Color
    public static final int CAMERA_SET_PHOTO_QUALITY = 9987;  //Photo quality
    public static final int CAMERA_SET_CONTINUE_SHOT = 9988;  //连拍
    public static final int CAMERA_SET_SELF_TIMER = 9989;  //定时拍照
    public static final int CAMERA_SET_ISO = 9990;  //ISO
    public static final int CAMERA_SET_WB = 9991;  //White balance
    public static final int CAMERA_SET_BACKLIGHT = 9992;  //Back light
    public static final int CAMERA_SET_FILE_PROTECT = 9993; //上锁文档
    public static final int CAMERA_MJPEG_RTSP = 9994; //RTSP or HTTP live
    public static final int CAMERA_FCWS = 9995; //轨道偏移
    public static final int CAMERA_LDWS = 9996;
    public static final int CAMERA_GPSVOICE = 9997; //GPS voice
    public static final int CAMERA_SET_GPS = 9998; //GPS

    //Error code;
    public static final int RET_OK = 0;
    public static final int RET_RECORD_STARTED = 1;
    public static final int RET_RECORD_STOPPED = 2;
    public static final int RET_WIFI_DISCONNECTED = 3;
    public static final int RET_MIC_ON = 4;
    public static final int RET_MIC_OFF = 5;
    public static final int RET_POWER_OFF = 6;
    public static final int RET_REMOVE_BY_USER = 7;
    public static final int ERROR_NO_FILE = -1;
    public static final int ERROR_FILE_EXIF = -2;
    public static final int ERROR_NO_BUFFER = -3;
    public static final int ERROR_FILE_READ_ONLY = -4;
    public static final int ERROR_FILE_DELETE = -5;
    public static final int ERROR_DELETE_FAILED = -6;
    public static final int ERROR_RECORD_FULL = -7;
    public static final int ERROR_RECORD_WRITE_FAILED = -8;
    public static final int ERROR_RECORD_SLOW = -9;
    public static final int ERROR_BATTERY_LOW = -10;
    public static final int ERROR_STORAGE_FULL = -11;
    public static final int ERROR_FOLDER_FULL = -12;
    public static final int ERROR_EXECUTE = -13;
    public static final int ERROR_FW_WRITE_CHECK_SUM_FAILED = -14;
    public static final int ERROR_FW_READ_FROM_NAND_FAILED = -15;
    public static final int ERROR_FW_WRITE = -16;
    public static final int ERROR_FW_READ_CHECK_SUM_FAILED = -17;
    public static final int ERROR_FW_READ = -18;
    public static final int ERROR_FW_INVALID_SOURCE_STORAGE = -19;
    public static final int ERROR_FW_UPDATE_OFF_SET = -20;
    public static final int ERROR_CAMMAND_INVALID_PARAMETER = -21;
    public static final int ERROR_CAMMAND_NOT_FOUND = -256;
}
