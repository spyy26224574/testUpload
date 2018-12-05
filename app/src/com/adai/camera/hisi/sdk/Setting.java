package com.adai.camera.hisi.sdk;

import android.util.Log;

import com.adai.camera.hisi.net.HttpProxy;
import com.adai.camera.hisi.net.HttpResult;
import com.adai.camera.hisi.net.StringParser;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


/**
 * 获取和设置DV的各种属性
 */
public class Setting {

    private static final String TAG = "Setting";

    /**
     * 获取相机工作状态，录像、定时拍、延时拍、空闲。
     *
     * @return 返回String对象, record, timelapse, timer, idle
     * 失败返回null
     */
    public static String getWorkState(String ip) {
        String url;
        url = String.format("http://%s%s/getworkstate.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForStringByKey(url, "state");
    }

    /**
     * 获取视频信息（分辨率，帧率）
     *
     * @param ip  DV IP地址
     * @param map 输出参数存放视频属性键值对；
     *            键:resolution 分辨率
     *            键:fps 帧率
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int getVideoInfo(String ip, Map<String, String> map) {
        String url;
        url = String.format("http://%s%s/getvideoinfo.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForMap(url, map);
    }

    /**
     * 设置视频信息（分辨率，帧率）
     *
     * @param ip         DV IP地址
     * @param resolution 分辨率 1080P/720P
     * @param frameRate  帧率 30/60
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setVideoInfo(String ip, String resolution, int frameRate) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setvideoinfo.cgi?&-resolution=%s&-fps=%d",
                ip,
                Common.CGI_PATH,
                resolution,
                frameRate);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取电池状态信息
     *
     * @param ip DV IP地址
     * @return 非null成功，null失败
     * 电量，键： capacity 0~100
     * 充电状态，键：charge 0/1
     * 插电源状态，键：ac 0/1
     */
    public static Common.BatteryInfo getBatteryInfo(String ip) {
        Map<String, String> map = new TreeMap<String, String>();
        String url;
        url = String.format("http://%s%s/getbatterycapacity.cgi?",
                ip,
                Common.CGI_PATH);

        int ret = HttpProxy.doForMap(url, map);

        if (Common.FAILURE == ret) {
            return null;
        }

        Common.BatteryInfo batteryInfo = new Common.BatteryInfo();
        String capacity = map.get("capacity");
        String charge = map.get("charge");
        String ac = map.get("ac");

        try {
            if (null != charge) {
                batteryInfo.bCharging = charge.equals("1");
            }

            if (null != ac) {
                batteryInfo.bAC = ac.equals("1");
            }

            if (null != capacity) {
                batteryInfo.capactiy = Integer.parseInt(capacity);
                if (batteryInfo.capactiy > 100 || batteryInfo.capactiy < 0) {
                    batteryInfo.capactiy = 0;
                }
            }
        } catch (NumberFormatException e) {
            batteryInfo.capactiy = 0;
        }

        return batteryInfo;
    }

    /**
     * 获取SD卡状态
     *
     * @param ip DV IP地址
     * @return SdCardInfo对象成功，null失败，
     * SD卡状态, 键： sdstate
     * SD卡容量, 键： total
     * SD卡已使用，键： used
     */
    public static Common.SdCardInfo getSdState(String ip) {
        Map<String, String> map = new TreeMap<String, String>();
        String url;
        url = String.format("http://%s%s/getsdstate.cgi?",
                ip,
                Common.CGI_PATH);

        int ret = HttpProxy.doForMap(url, map);

        if (Common.FAILURE == ret) {
            return null;
        }

        Common.SdCardInfo info = new Common.SdCardInfo();
        String state = map.get("sdstate");
        String total = map.get("total");
        String used = map.get("used");

        if (state.equals("SDOK")) {
            info.sdState = Common.SD_STATE_OK;
        } else if (state.equals("SDFULL")) {
            info.sdState = Common.SD_STATE_FULL;
        } else if (state.equals("SDNONE")) {
            info.sdState = Common.SD_STATE_NONE;
        } else if (state.equals("SDERROR")) {
            info.sdState = Common.SD_STATE_ERROR;
        } else {
            return null;
        }

        if (null != total) {
            info.total = Integer.parseInt(total.replace(" MB", ""));
        } else {
            info.total = -1;
        }

        if (null != used) {
            info.used = Integer.parseInt(used.replace(" MB", ""));
        } else {
            info.used = -1;
        }

        return info;

    }

    /**
     * 获取视场角:150度或170度
     *
     * @param ip DV IP地址
     * @return 150/170; FAILURE(-1)失败
     */
    public static int getViewField(String ip) {
        String url;
        url = String.format("http://%s%s/getviewfield.cgi?",
                ip,
                Common.CGI_PATH);

        int viewField = HttpProxy.doForIntByKey(url, "fov");

        if (150 == viewField || 170 == viewField) {
            return viewField;
        } else {
            return Common.FAILURE;
        }
    }

    /**
     * 设置视场角
     *
     * @param ip        DV IP地址
     * @param viewField 150/170
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setViewField(String ip, int viewField) {
        if (viewField != 150 && viewField != 170) {
            return Common.FAILURE;
        }

        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setviewfield.cgi?&-fov=%d",
                ip,
                Common.CGI_PATH,
                viewField);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取循环录像状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE开，FALSE关，null失败
     */
    public static Boolean getLoopRecord(String ip) {
        String url;
        url = String.format("http://%s%s/getlooprecord.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置循环录像状态（开/关）
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setLoopRecord(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setlooprecord.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取图像翻转状态
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE开，FALSE关，null失败
     */
    public static Boolean getFlip(String ip) {
        String url;
        url = String.format("http://%s%s/getflip.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置图像翻转
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setFlip(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setflip.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取连拍信息
     *
     * @param map 输出参数，以“time”,“count”为键存储连拍的时间和张数
     *            键： time
     *            键： count
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int getBurstInfo(String ip, Map<String, Integer> map) {
        String url;
        url = String.format("http://%s%s/getburstinfo.cgi?",
                ip,
                Common.CGI_PATH);

        //执行HTTP连接操作
        HttpResult httpResult = HttpProxy.doHttpGetForContent(url);

        if (httpResult.statusCode != HttpResult.HTTP_OK) {
            Log.e("getBurstInfo", httpResult.content);
            return Common.FAILURE;
        }

        TreeMap<String, String> tempMap = new TreeMap<String, String>();
        StringParser.getKeyValueMap(httpResult.content, tempMap);
        String strTime = tempMap.get("time");
        String strCount = tempMap.get("count");

        if (null == strTime || null == strCount) {
            Log.e("getBurstInfo", httpResult.content);
            return Common.FAILURE;
        }

        int time, count;
        try {
            time = Integer.parseInt(strTime);
            count = Integer.parseInt(strCount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return Common.FAILURE;
        }

        map.put("time", time);
        map.put("count", count);

        return Common.SUCCESS;
    }


    /**
     * 设置连拍信息。1秒3张、1秒5张、1秒6张、3秒9张、3秒18张
     *
     * @param time  连拍时间
     * @param count 连拍张数
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setBurstInfo(String ip, int time, int count) {
        if (time < 0 || time > 3 || count < 0 || count > 18) {
            Log.e("setBurstInfo", "Invalid args");
            return Common.FAILURE;
        }

        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setburstinfo.cgi?&-time=%d&-count=%d",
                ip,
                Common.CGI_PATH,
                time,
                count);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取定时拍照间隔.1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @return int定时拍照间隔秒数， FAILURE(-1)失败
     */
    public static int getTimelapseInfo(String ip) {
        String url;
        url = String.format("http://%s%s/gettimelapseinfo.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "time");
    }


    /**
     * 设置定时拍照间隔。1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @param time 定时拍照间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setTimelapseInfo(String ip, int time) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/settimelapseinfo.cgi?&-time=%d",
                ip,
                Common.CGI_PATH,
                time);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取延时拍照时间.1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @return int延时拍照秒数， FAILURE(-1)失败
     */
    public static int getTimerInfo(String ip) {
        String url;
        url = String.format("http://%s%s/gettimerinfo.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "time");
    }


    /**
     * 设置延时拍照间隔。1秒、3秒，5秒，10秒，30秒，60秒
     *
     * @param time 延时拍照间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setTimerInfo(String ip, int time) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/settimerinfo.cgi?&-time=%d",
                ip,
                Common.CGI_PATH,
                time);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取缩时录像多长时间一帧： 0（实际表示0.5），1秒、2秒，5秒，10秒，30秒，60秒
     *
     * @return int 秒数， FAILURE(-1)失败
     */
    public static int getRecordTimelapseInfo(String ip) {
        String url;
        url = String.format("http://%s%s/getrecordtimelapse.cgi?",
                ip, Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "time");
    }


    /**
     * 设置缩时录像几秒钟一帧。 0(实际表示0.5)、1秒、2秒，5秒，10秒，30秒，60秒
     *
     * @param time 每帧间隔时间
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setRecordTimelapseInfo(String ip, int time) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setrecordtimelapse.cgi?&-time=%d",
                ip, Common.CGI_PATH, time);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取开机操作。开机录像、开机定时拍照、无操作
     *
     * @return BOOT_ACTION_IDLE无操作、BOOT_ACTION_RECORD开机录像、BOOT_ACTION_TIMELAPSE开机定时拍照, null失败
     */
    public static String getBootAction(String ip) {
        String url;
        url = String.format("http://%s%s/getbootaction.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForStringByKey(url, "action");
    }


    /**
     * 设置开机操作
     *
     * @param action BOOT_ACTION_IDLE无操作、BOOT_ACTION_RECORD开机录像、BOOT_ACTION_TIMELAPSE开机定时拍照
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setBootAction(String ip, String action) {
        String url;
        url = String.format("http://%s%s/setbootaction.cgi?&-action=%s",
                ip,
                Common.CGI_PATH,
                action);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取音频编码状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public static Boolean getAudioEncode(String ip) {
        String url;
        url = String.format("http://%s%s/getaudioencode.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置音频编码状态（开/关）
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setAudioEncode(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setaudioencode.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取视频制式，NTSC/PAL
     *
     * @param ip DV IP地址
     * @return String对象："NTSC"，"PAL"，null失败
     */
    public static String getVideoMode(String ip) {
        String url;
        url = String.format("http://%s%s/getvideonorm.cgi?",
                ip,
                Common.CGI_PATH);

        String value = HttpProxy.doForStringByKey(url, "videonorm");

        if (null == value) {
            return null;
        }

        if (value.equals(Common.VIDEO_MODE_NTSC)
                || value.equals(Common.VIDEO_MODE_PAL)) {
            return value;
        } else {
            return null;
        }

    }

    /**
     * 设置视频制式，
     *
     * @param ip        DV IP地址
     * @param videoMode "NTSC","PAL"
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setVideoMode(String ip, String videoMode) {
        String url;
        url = String.format("http://%s%s/setvideonorm.cgi?&-videonorm=%s",
                ip,
                Common.CGI_PATH,
                videoMode);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取设备信息
     *
     * @param ip  DV IP地址
     * @param map 输出参数，设备属性键值对
     *            键：name 设备名
     *            键：serialnum 序列号
     *            键：softversion 软件版本
     *            键：hardversion 硬件版本
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int getDeviceAttr(String ip, Map<String, String> map) {
        if (null == map) {
            return Common.FAILURE;
        }

        String url;
        url = String.format("http://%s%s/getdeviceattr.cgi",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForMap(url, map);
    }

    /**
     * 设置DV端时间
     *
     * @param ip       DV IP地址
     * @param calendar 含年月日时分秒
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setSystemTime(String ip, GregorianCalendar calendar) {
        if (null == calendar) {
            return Common.FAILURE;
        }

        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setsystime.cgi?&-time=%04d%02d%02d%02d%02d%02d",
                ip,
                Common.CGI_PATH,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                calendar.get(Calendar.SECOND));

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取中心点测光状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public static Boolean getSpotMeter(String ip) {
        String url;
        url = String.format("http://%s%s/getspotmeter.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置中心点测光开启或关闭
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setSpotMeter(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setspotmeter.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取时间OSD状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public static Boolean getTimeOsd(String ip) {
        String url;
        url = String.format("http://%s%s/gettimeosd.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置时间OSD开启或关闭
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setTimeOsd(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/settimeosd.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取LED闪烁提示状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public static Boolean getLedState(String ip) {
        String url;
        url = String.format("http://%s%s/getledstate.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置LED闪烁提示状态：开启或关闭
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setLedState(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setledstate.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取蜂鸣器状态，开或关
     *
     * @param ip DV IP地址
     * @return Boolean对象：TRUE打开，FALSE关闭，null失败
     */
    public static Boolean getBuzzerState(String ip) {
        String url;
        url = String.format("http://%s%s/getbuzzer.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForBoolean(url);
    }

    /**
     * 设置蜂鸣器状态：开启或关闭
     *
     * @param ip      DV IP地址
     * @param enabled true打开，false关闭
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setBuzzerState(String ip, boolean enabled) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setbuzzer.cgi?&-enable=%d",
                ip,
                Common.CGI_PATH,
                enabled ? 1 : 0);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 恢复出厂设置
     *
     * @param ip
     * @return void
     */
    public static void restoreFactorySettings(String ip) {
        setSocketNoReply("/reset.cgi?", ip);
    }


    /**
     * 设置WIFI名字或密码
     *
     * @param ssid     WIFI名字，null表示不设置
     * @param password WIFI密码，null表示不设置
     * @return void
     */
    public static void setWifi(String ip, String ssid, String password) {
        StringBuilder builder = new StringBuilder("/setwifi.cgi?");
        if (ssid != null) {
            builder.append("&-wifissid=");
            builder.append(ssid);
            Log.d(TAG, "setWifi:ssid = " + ssid);
        }
        if (password != null) {
            builder.append("&-wifikey=");
            builder.append(password);
            Log.d(TAG, "setWifi:passwd = " + password);
        }

        setSocketNoReply(builder.toString(), ip);
    }

    /**
     * 设置WIFI STA模式
     *
     * @param ssid     WIFI名字，null表示不设置
     * @param password WIFI密码，null表示不设置
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setWifiToSta(String ip, String ssid, String password) {
        StringBuilder builder = new StringBuilder("http://");
        builder.append(ip);
        builder.append(Common.ICGI_PATH);
        builder.append("/setwifista.cgi?");

        if (null != ssid) {
            builder.append("&-ssid=");
            builder.append(ssid);
        }

        if (null != password) {
            builder.append("&-key=");
            builder.append(password);
        }

        String url = builder.toString();

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 设置WIFI AP模式
     *
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setWifiToAp(String ip) {
        String url;
        url = String.format("http://%s%s/setwifista.cgi?",
                ip,
                Common.ICGI_PATH);
        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取定时关机时间. 0(关闭），1、5，10，30，60分钟
     *
     * @return int定时关机的当前时间， FAILURE(-1)失败
     */
    public static int getAutoShutdown(String ip) {
        String url;
        url = String.format("http://%s%s/getautoshutdown.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "time");
    }

    /**
     * 设置定时关机。
     *
     * @param time 定时关机时间, 0(关闭），1、5，10，30，60分钟
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setAutoShutdown(String ip, int time) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setautoshutdown.cgi?&-time=%d",
                ip,
                Common.CGI_PATH,
                time);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取屏幕自动休眠倒计时时间
     *
     * @return int 0(关闭），1、3、5分钟， FAILURE(-1)失败
     */
    public static int getScreenAutoSleep(String ip) {
        String url;
        url = String.format("http://%s%s/getscreenautosleep.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "time");
    }

    /**
     * 设置屏幕自动休眠倒计时时间
     *
     * @param time 0(关闭），1、3、5分钟
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setScreenAutoSleep(String ip, int time) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setscreenautosleep.cgi?&-time=%d",
                ip,
                Common.CGI_PATH,
                time);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 获取屏幕亮度
     *
     * @return int 100、60、30（百分比）， FAILURE(-1)失败
     */
    public static int getScreenBrightness(String ip) {
        String url;
        url = String.format("http://%s%s/getscreenbrightness.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "brightness");
    }

    /**
     * 设置屏幕亮度
     *
     * @param brightness 100、60、30（百分比）
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setScreenBrightness(String ip, int brightness) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setscreenbrightness.cgi?&-brightness=%d",
                ip,
                Common.CGI_PATH,
                brightness);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取DV端开机时UI模式
     *
     * @return int  0 录像,  1 拍照,  2 定时拍照,  3 延时拍照,  4 连拍, -1失败
     */
    public static int getPowerOnUiMode(String ip) {
        String url;
        url = String.format("http://%s%s/getpoweronuimode.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "uimode");
    }

    /**
     * 设置DV端开机时UI模式
     *
     * @param uimode 0 录像,  1 拍照,  2 定时拍照,  3 延时拍照,  4 连拍
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int setPowerOnUiMode(String ip, int uimode) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setpoweronuimode.cgi?&-uimode=%d",
                ip,
                Common.CGI_PATH,
                uimode);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 专用发送接口，发送后直接关闭；专门针对恢复出厂设置时，板端不回复导致socket等待
     */
    private static void setSocketNoReply(String strUrl, String ip) {
        Socket clientSocket = null;
        OutputStream os = null;
        Log.d(TAG, "strUrl:" + strUrl);
        try {
            clientSocket = new Socket(ip, 80);
            clientSocket.setSoTimeout(6000);
            os = clientSocket.getOutputStream();

            String head = "GET " + Common.CGI_PATH +
                    strUrl +
                    " HTTP/1.1\r\n" +
                    "Host: " + ip + "\r\n" +
                    "Connection: Keep-Alive\r\n" +
                    "User-Agent: HiCamera\r\n\r\n";

            os.write(head.getBytes());
        } catch (UnknownHostException e) {
            Log.d(TAG, "strUrl:" + "UnknownHostException");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "strUrl:" + "IOException");
            e.printStackTrace();
        } finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
                if (os != null) {
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 使用设备关机/休眠（板端可能不回复“Success”）
     */
    public static int sleep(String ip) {
        String url;
        url = String.format("http://%s%s/wifisleep.cgi?",
                ip, Common.CGI_PATH);

        return HttpProxy.doForSuccess(url);
    }

    //将字符串MAC地址 "00:03:7f:49:46:4c" 转换为 byte[6]
    public static byte[] macAddres2ByteArray(String macAddress) {
        byte[] buffer = new byte[6];

        if (macAddress.length() != 17 || !macAddress.contains(":")) {
            return null;
        }

        try {
            String parts[] = macAddress.split(":");
            for (int i = 0; i < 6; i++) {
                Integer integerValue = Integer.parseInt(parts[i], 16);
                buffer[i] = integerValue.byteValue();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return buffer;
    }

    /**
     * 发送WakeOnLAN UDP魔术包，唤醒板端
     * 格式：6个0xFF 再加上 重复16次的MAC地址
     *
     * @param mac 6字节数组，十六进制板端MAC地址
     */
    public static void wakeupDevice(String ip, byte[] mac) {

        if (null == mac) {
            Log.d(TAG, "mac address byte[] is null");
            return;
        }

        byte[] buffer = new byte[6 * 17];

        int i = 0;
        //6个0xFF字节
        for (i = 0; i < 6; i++) {
            buffer[i] = (byte) 0xFF;
        }

        //重复16次MAC地址
        for (i = 1; i <= 16; i++) {
            System.arraycopy(mac, 0, buffer, i * 6, 6);
        }

        try {
            //IP地址最后一段改为 .255
            int lastDot = ip.lastIndexOf('.');
            ip = ip.substring(0, lastDot) + ".255";

            Log.d(TAG, "wake up , IP:" + ip);

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, new InetSocketAddress(ip, 9));
            DatagramSocket socket = new DatagramSocket();
            for (i = 0; i < 5; i++) {
                socket.send(packet);
            }
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getBitRate(String ip) {
        String url;
        url = String.format("http://%s%s/getbitrate.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "bitrate");
    }


    public static int setBitRate(String ip, int bitrate) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setbitrate.cgi?&-bitrate=%d",
                ip,
                Common.CGI_PATH,
                bitrate);

        return HttpProxy.doForSuccess(url);
    }

    public static int getWifiChannel(String ip) {
        String url;
        url = String.format("http://%s%s/getwifichannel.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForIntByKey(url, "wifichannel");
    }


    public static int setWifiChannel(String ip, int channel) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setwifichannel.cgi?&-wifichannel=%d",
                ip,
                Common.CGI_PATH,
                channel);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取设备能力集。如  表示是否可待机，是否有4g模块。
     *
     * @return "standby,4g"； 没有为 null
     */
    public static String getDevCapabilities(String ip) {
        String url;
        url = String.format("http://%s%s/getdevcapabilities.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForStringByKey(url, "devcapabilities");
    }

    /**
     * 获取当前工作模式
     *
     * @return 成功返回大于等于0的工作模式，失败返回 -1
     */
    public static int getWorkMode(String ip, Map<String, String> map) {
        String url;
        url = String.format("http://%s%s/getworkmode.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForMap(url, map);
    }

    /**
     * 设置新工作模式
     *
     * @param workmode WORK_MODE_XXX
     * @return 成功返回SUCCESS(0), 失败返回FAILURE(-1)
     */
    public static int setWorkMode(String ip, int workmode) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setworkmode.cgi?&-workmode=%d?",
                ip,
                Common.CGI_PATH,
                workmode);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 获取特定工作模式下特定配置项的能力集合
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项
     * @return 成功返回字符串：”xxx,xxx,xxx,…” ， 失败返回null
     */
    public static String getCapability(String ip, int workmode, int type) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/getcapability.cgi?&-workmode=%d&-type=%d",
                ip, Common.CGI_PATH,
                workmode, type);

        return HttpProxy.doForStringByKey(url, "capability");
    }

    /**
     * 获取特定工作模式下特定配置项的数值
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项
     * @return 成功返回字符串：”xxx” ， 失败返回null
     */
    public static String getParameter(String ip, int workmode, int type) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/getparameter.cgi?&-workmode=%d&-type=%d",
                ip, Common.CGI_PATH,
                workmode, type);

        return HttpProxy.doForStringByKey(url, "value");
    }

    /**
     * 设置特定工作模式下特定配置项的数值
     *
     * @param workmode 工作模式， WORK_MODE_XXX
     * @param type     特定配置项
     * @param value    配置项的值
     * @return 成功返回SUCCESS(0), 失败FAILURE-1
     */
    public static int setParameter(String ip, int workmode, int type, String value) {
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/setparameter.cgi?&-workmode=%d&-type=%d&-value=%s",
                ip, Common.CGI_PATH,
                workmode, type, value);

        return HttpProxy.doForSuccess(url);
    }
}

