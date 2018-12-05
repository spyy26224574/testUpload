package com.adai.camera.sunplus.data;

import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;

import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.ICatchWificamState;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.type.ICatchFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GlobalInfo {
    public static final String DOWNLOAD_PATH = "/DCIM/WCMapp3/";
    public static final String UPDATEFW_FILENAME = "/sdcard/SportCamResoure/sphost.BRN";
    public static final String AUTO_DOWNLOAD_PATH = "/DCIM/WCMapp3/";
    public static final String PROPERTY_CFG = "/SportCamResoure/netconfig.properties";
    public static final String CAMERA_CONNECTINFO_DATEBASE_NAME = "camerainfo_db42.db";
    public static final String WIFILIST_SORT_WITH_STARTSTRING = "SBC";
    public static final int CAMERA_SLOTSQLITE_VERSION = 1;

    public static final int EVENT_BATTERY_ELETRIC_CHANGED = 0x1001;
    public static final int EVENT_SD_CARD_FULL = 0x1002;
    public static final int EVENT_VIDEO_OFF = 0x1003;
    public static final int EVENT_CAPTURE_COMPLETED = 0x1004;
    public static final int EVENT_CAPTURE_START = 0x1005;
    public static final int EVENT_FILE_ADDED = 0x1006;
    public static final int EVENT_VIDEO_ON = 0x1007;
    public static final int EVENT_CONNECTION_FAILURE = 0x1008;
    public static final int EVENT_TIME_LAPSE_STOP = 0x1009;
    public static final int EVENT_SERVER_STREAM_ERROR = 0x100A;
    public static final int EVENT_VIDEO_RECORDING_TIME = 0x100B;
    public static final int EVENT_FILE_DOWNLOAD = 0x110C;
    public static final int EVENT_FW_UPDATE_COMPLETED = 0x110D;
    public static final int EVENT_FW_UPDATE_POWEROFF = 0x110E;
    public static final int EVENT_NO_SD_CARD = 0x110F;

    public static final int MESSAGE_AWAKE_ONE_CAMERA = 0x2001;
    public static final int MESSAGE_REMOVE_CAMERA = 0x2002;
    public static final int MESSAGE_REMOVE_ALL_CAMERA = 0x2003;
    public static final int MESSAGE_AWAKE_ALL_CAMERA = 0x2004;
    public static final int MESSAGE_UPDATE_UI_BURST_ICON = 0x2005;
    public static final int MESSAGE_UPDATE_UI_WHITE_BALANCE_ICON = 0x2006;
    public static final int MESSAGE_UPDATE_UI_IMAGE_SIZE = 0x2007;
    public static final int MESSAGE_UPDATE_UI_VIDEO_SIZE = 0x2008;
    public static final int MESSAGE_UPDATE_UI_CAPTURE_DELAY = 0x2009;
    public static final int MESSAGE_FORMAT_SUCCESS = 0x200A;
    public static final int MESSAGE_FORMAT_FAILED = 0x200B;
    public static final int MESSAGE_FORMAT_SD_START = 0x200C;
    // public static final int MESSAGE_CAPTURE_COMPLETED = 0x200D;
    public static final int MESSAGE_SETTING_TIMELAPSE_STILL_MODE = 0x200F;
    public static final int MESSAGE_SETTING_TIMELAPSE_VIDEO_MODE = 0x2010;
    public static final int MESSAGE_ZOOM_COMPLETED = 0x2011;
    public static final int MESSAGE_GET_NEW_CAMERA = 0x2012;
    public static final int MESSAGE_UPDATE_UI_SLOW_MOTION = 0x2013;
    public static final int MESSAGE_UPDATE_UI_UPSIDE_DOWN = 0x2014;
    public static final int MESSAGE_AUTO_DOWNLOAD_COMPLETED = 0x2015;
    public static final int MESSAGE_UPDATE_FW_SUCCESS = 0x2016;
    public static final int MESSAGE_UPDATE_FW_FAILED = 0x2017;
    public static final int MESSAGE_UPDATE_FW_POWEROFF = 0x2018;
    public static final int MESSAGE_UPDATE_VIDEOPB_BAR = 0x2019;
    public static final int MESSAGE_RETURN_LOCAL_PHOTO_VIEW = 0x2019;
    public static final int MESSAGE_DELETE_CAMERA = 0x201A;
    public static final String CAMERA_FILTER = "WDV8000";
    // public static final String CAMERA_FILTER = "Cansonic";
    // public static final String CAMERA_PASSWORD = "88888888";
    public static final String CAMERA_PASSWORD = "1234567890";
    public static final int MESSAGE_CAMERA_SCAN_TIME_OUT = 0x2027;
    public static final int MESSAGE_CAMERA_SEARCH_SELECTED = 0x2025;

    // clint
    public static ICatchWificamPlayback currentphotoPlaybackClint;
    public static ICatchWificamControl currentActionClint;
    public static ICatchWificamVideoPlayback currentPlaybackClint;
    public static ICatchWificamPreview currentpreviewStreamClint;
    public static ICatchWificamInfo currentInfoClint;
    public static ICatchWificamProperty currentPropertiesClint;
    public static ICatchWificamState currentStateClint;

    //local play clint
    public static ICatchWificamPlayback localPlaybackClint;
    public static ICatchWificamControl localControlClint;
    public static ICatchWificamVideoPlayback localVideoPlaybackClint;

    private boolean isDownloading = false;
    private String ssid;
    private ProgressDialog progressDialog;

    private static GlobalInfo instance = new GlobalInfo();
    //	private Activity activity;
//	private Context context;
    public static int currentMode = 0;
    public String hotSpotName;
    private int reconnectTime;
    private SQLiteDatabase db;
    public static HashSet<SunplusMinuteFile> mSelectedMinuteFile = new HashSet<>();
    public static List<ICatchFile> previewFileList = new ArrayList<>();
    public static boolean autoDownloadAllow = false;
    public static float autoDownloadSizeLimit = 1.0f;// GB
    public static boolean forbidAudioOutput = false;
    public static double videoPbCurrentTime = -1.0;
    public static boolean isSdCardExist = true;
    public static String CurrentLocalPreviewPhotoPath = "";
    public static boolean isSupportAutoReconnection = false;
    public static boolean isSupportAudio = true;
    public static boolean isFirstInMainActivity = true;
    // test
    public static boolean isSupportBroadcast = false;
    public static boolean isSupportSetting = false;
    public static boolean saveSDKLog = false;
    public static boolean saveAPPLog = false;

    public static ICatchWificamSession localPlaySDKSession;
    public static boolean enableSoftwareDecoder = false;


    public static GlobalInfo getInstance() {
        if (instance == null) {
            instance = new GlobalInfo();
        }
        return instance;
    }

    // download statue
    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloadStatus(boolean isDownloading) {
        this.isDownloading = isDownloading;
    }

    // ssid
    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getSsid() {
        return ssid;
    }

}
