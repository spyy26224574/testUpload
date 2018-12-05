package com.adai.gkdnavi.utils;

import android.text.TextUtils;

import com.adai.gkd.httputils.HttpUtil;
import com.example.ipcamera.application.VLCApplication;

/**
 * Created by admin on 2016/9/22.
 * 网络资源下载,路径统一管理
 */
public class NetworkDownloadUtils {
    private static final String downloadpath = VLCApplication.DOWNLOADPATH;

    public static String getLocalPath(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String[] strs = url.split("/");
        String localpath = VLCApplication.DOWNLOADPATH + "/" + strs[strs.length - 1];
        return localpath;
    }

    public static String getLocalTempPath(String url) {
        if (TextUtils.isEmpty(url)) return null;
        String[] strs = url.split("/");
        String localpath = VLCApplication.DOWNLOADPATH + "/" + strs[strs.length - 1] + ".temp";
        return localpath;
    }

    public static void downloadFile(String url, HttpUtil.DownloadCallback callback) {
        if (TextUtils.isEmpty(url)) return;
        HttpUtil.getInstance().downloadFile(url, getLocalPath(url), callback);
    }
}
