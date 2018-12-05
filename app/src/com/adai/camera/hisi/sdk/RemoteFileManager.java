package com.adai.camera.hisi.sdk;

import android.util.Log;

import com.adai.camera.hisi.net.HttpProxy;
import com.adai.camera.hisi.net.HttpResult;
import com.adai.camera.hisi.net.StringParser;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 文件管理类，提供文件总数、文件列表，文件信息，删除，下载等操作
 */

public class RemoteFileManager {

    /**
     * 从DV端获取文件总数
     *
     * @param ip DV端的IP地址
     * @return 文件总数，FAILURE(-1)失败
     */
    public static int getFileCount(String ip) {
        //组合http请求地址
        String url = String.format("http://%s%s%s",
                ip,
                Common.CGI_PATH,
                "/getfilecount.cgi?");

        return HttpProxy.doForIntByKey(url, "count");
    }

    /**
     * 获取文件名列表
     *
     * @param ip
     * @param start 从第start个文件开始，最前的文件索引为1，不是从0开始
     * @param end   获取到第end个文件（文件已按名字排序）
     * @param list  输出参数，解析文件名后添加到list
     * @return 获取到的文件名总数，Results.FAILURE(-1)失败
     */
    public static int getFileList(String ip, int start, int end, List<String> list) {
        //组合http请求地址
        String url;
        url = String.format(Locale.ENGLISH,"http://%s%s/getfilelist.cgi?&-start=%d&-end=%d",
                ip,
                Common.CGI_PATH,
                start,
                end);

        //执行HTTP连接操作
        HttpResult httpResult = HttpProxy.doHttpGetForContent(url);

        //执行出错
        if (httpResult.statusCode != HttpURLConnection.HTTP_OK) {
            Log.e("getFileList", httpResult.content);
            return Common.FAILURE;
        }

        return StringParser.getStringList(httpResult.content, list);
    }


    /**
     * 获取指定文件的属性信息，大小、时长（视频）...
     *
     * @param ip
     * @param file 文件名
     * @param map  输出参数
     *             文件大小，键"size",单位字节
     *             视频时长，键"time",图片无此项
     *             创建时间,键"create",格式： "2014/07/30 20:30:48"
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int getFileInfo(String ip, String file, Map<String, String> map) {
        //组合http请求地址
        String url;
        url = String.format("http://%s%s/getfileinfo.cgi?&-name=%s",
                ip,
                Common.CGI_PATH,
                file);

        return HttpProxy.doForMap(url, map);
    }


    /**
     * 删除DV端的文件
     *
     * @param ip
     * @param file 要删除的文件名
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int deleteFile(String ip, String file) {
        //组合http请求地址
        String url;
        url = String.format("http://%s%s/deletefile.cgi?&-name=%s",
                ip,
                Common.CGI_PATH,
                file);

        return HttpProxy.doForSuccess(url);
    }

    /**
     * 删除DV端的所有文件
     *
     * @param ip
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int deleteAllFiles(String ip) {
        //组合http请求地址
        String url;
        url = String.format("http://%s%s/deleteallfiles.cgi?",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForSuccess(url);
    }


    /**
     * 格式华SD卡
     *
     * @param ip
     * @return 获取格式化后SD信息 键值对的数量
     */
    public static int formatSdCard(String ip, Map<String, String> map) {
        //组合http请求地址
        String url;
        url = String.format("http://%s%s/sdcommand.cgi?-format&-partition=1",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForMap(url, map);
    }

    /**
     * 升级
     *
     * @param ip
     * @return SUCCESS(0)成功， FAILURE(-1)失败
     */
    public static int upgrade(String ip) {
        //组合http请求地址
        String url;
        url = String.format("http://%s%s/upgrade.cgi",
                ip,
                Common.CGI_PATH);

        return HttpProxy.doForSuccess(url);
    }
}