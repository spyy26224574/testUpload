package com.adai.camera.mstar;

/**
 * Created by huangxy on 2017/10/11 14:12.
 */

public class MstarCamera {
    public static final String DEFAULT_MJPEG_PUSH_URL = "/cgi-bin/liveMJPEG";
    public static final String DEFAULT_RTSP_MJPEG_AAC_URL = "/liveRTSP/av1";
    public static final String DEFAULT_RTSP_H264_AAC_URL = "/liveRTSP/av2";
    public static final String DEFAULT_RTSP_H264_PCM_URL = "/liveRTSP/av4";
    public static final String DEFAULT_RTSP_H264_URL = "/liveRTSP/v1";
    public static final String DEFAULT_MJPEG_PULL_URL = "/cgi-bin/staticMJPEG";
    public static String URL_STREAM;
    public static String CAM_IP;
    public static boolean IS_RECORDING = false;
    public static String SENSOR_MODE = "Videomode";
    public static final int MODE_PHOTO = 0;
    public static final int MODE_MOVIE = 1;
    public static int CUR_MODE = 1;

    public static boolean IsCameraInPreviewMode() {
        return SENSOR_MODE.equals("Videomode") ||
                SENSOR_MODE.equals("VIDEO") ||
                SENSOR_MODE.equals("Capturemode") ||
                SENSOR_MODE.equals("CAMERA") ||
                SENSOR_MODE.equals("BURST") ||
                SENSOR_MODE.equals("TIMELAPSE");
    }
}
