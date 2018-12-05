package com.adai.camera.hisi.sdk;

import android.util.Log;

import com.adai.camera.hisi.net.HttpProxy;
import com.adai.camera.hisi.net.HttpResult;

/**
 * DV指令的类型，结果类型
 */
public class Command {

    //指令类型
    public static final int ACTION_RECORD_START = 0;  //开始录像,34220
    public static final int ACTION_RECORD_STOP = 1;  //停止录像,34220
    public static final int ACTION_PHOTO = 2;  //拍照
    public static final int ACTION_BURST = 3;  //高速连拍
    public static final int ACTION_TIMELAPSE_START = 4;  //开始定时拍
    public static final int ACTION_TIMELAPSE_STOP = 5;     //停止定时拍
    public static final int ACTION_TIMER_START = 6;  //开始延时拍
    public static final int ACTION_TIMER_STOP = 7;  //停止延时拍
    public static final int ACTION_CONTINUOUS_START = 8;  //开始连续拍照
    public static final int ACTION_CONTINUOUS_STOP = 9;  //停止连续拍照

    public static final int ACTION_VIDEO_COMMON_START = 10;  //开始普通录像（117）
    public static final int ACTION_VIDEO_COMMON_STOP = 11;  //停止普通录像（117）
    public static final int ACTION_VIDEO_LOOP_START = 12;  //开始循环录像（117）
    public static final int ACTION_VIDEO_LOOP_STOP = 13;  //停止循环录像（117）
    public static final int ACTION_VIDEO_TIMELAPSE_START = 14;  //开始缩时录像（117）
    public static final int ACTION_VIDEO_TIMELAPSE_STOP = 15;  //停止缩时录像（117）
    public static final int ACTION_VIDEO_SNAP_START = 16;  //开始录像抓拍（3559）
    public static final int ACTION_VIDEO_SNAP_STOP = 17;  //停止录像抓拍（3559）
    public static final int ACTION_VIDEO_SLOW_START = 18;  //开始慢速录像（3559）
    public static final int ACTION_VIDEO_SLOW_STOP = 19;  //停止慢速录像（3559）
    public static final int ACTION_BUTT = 20;  //无效指令

    /**
     * 关于各条指令，配置好的CGI文件与参数。
     * 注意：字符串的下标索引 与 对应指令类型的常量数字对应相同，以支持根据指令类型直接取字符串
     */
    static final String[] cgiFilesParams = {
            "/record.cgi?&-cmd=start",
            "/record.cgi?&-cmd=stop",
            "/photo.cgi?&-type=photo",
            "/photo.cgi?&-type=photoburst",
            "/photo.cgi?&-type=phototimelapse&-cmd=start",
            "/photo.cgi?&-type=phototimelapse&-cmd=stop",
            "/photo.cgi?&-type=phototimer&-cmd=start",
            "/photo.cgi?&-type=phototimer&-cmd=stop",
            "/photo.cgi?&-type=continuous&-cmd=start",
            "/photo.cgi?&-type=continuous&-cmd=stop",

            "/record2.cgi?&-type=common&-cmd=start",
            "/record2.cgi?&-type=common&-cmd=stop",
            "/record2.cgi?&-type=loop&-cmd=start",
            "/record2.cgi?&-type=loop&-cmd=stop",
            "/record2.cgi?&-type=timelapse&-cmd=start",
            "/record2.cgi?&-type=timelapse&-cmd=stop",
            "/record2.cgi?&-type=recsnap&-cmd=start",
            "/record2.cgi?&-type=recsnap&-cmd=stop",
            "/record2.cgi?&-type=slow&-cmd=start",
            "/record2.cgi?&-type=slow&-cmd=stop",
    };


    /**
     * 下发DV指令（拍照，录像...），返回执行结果
     *
     * @param cmd 指令类型，范围从 ACTION_RECORD_START(0)--ACTION_BUTT-1(7)
     * @param ip  DV的IP地址
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static Common.Result executeCommand(String ip, int cmd) {
        Common.Result result = new Common.Result();
        result.cmd = cmd;
        //无效指令
        if (cmd >= ACTION_BUTT || cmd < 0) {
            Log.e("executeCommand", "Invalid command args");
            return result;
        }

        //组合http请求地址
        String url = String.format("http://%s%s%s",
                ip, Common.CGI_PATH, cgiFilesParams[cmd]);

        HttpResult httpResult = HttpProxy.doHttpGetForContent(url);

        if (httpResult.statusCode == HttpResult.HTTP_OK) {
            result.returnCode = Common.SUCCESS;
            result.errorCode = 0;
            return result;
        }

        result.returnCode = Common.FAILURE;
        if (httpResult.statusCode == HttpResult.HTTP_EXCEPTION) {
            return result;
        }

        try {
            // 返回数据例子： SvrFuncResult="-1560182781"
            int begin = "SvrFuncResult=\"".length();
            int end = httpResult.content.lastIndexOf("\"");

            String strErrorCode = httpResult.content.substring(begin, end);
            result.errorCode = Integer.parseInt(strErrorCode);
        } catch (Exception e) {
            result.errorCode = Common.FAILURE;
        }

        return result;
    }
}
