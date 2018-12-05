package com.adai.gkdnavi.utils;

import android.content.Context;
import android.text.TextUtils;

import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.CheckVersionTask;
import com.example.ipcamera.application.VLCApplication;

import java.io.IOException;

public class CameraUpdateUtil {

    public interface DownloadComplete {
        void onDownloadComplte(String path, boolean sucess);

        void onDownloading(int progress);
    }

    DownloadComplete downloadComplete;

    public void setDownloadComplete(DownloadComplete downloadComplete) {
        this.downloadComplete = downloadComplete;
    }

    private static final String CAME_LOCAL_PATH = "CAME_LOCAL_PATH";
//    private static final String LOCAL_PATH = VLCApplication.OTA_PATH + "/O5.bin";

    private Context context;

    public CameraUpdateUtil(Context context) {
        this.context = context;
    }

    public static String getlocalpath() {
        return SpUtils.getString(VLCApplication.getAppContext(), CAME_LOCAL_PATH, "");
    }

    public void downloadFile(String url, final String md5) {
        String fileName = url.substring(url.lastIndexOf("/") + 1);
//        NetworkInfo networkInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
//        if (networkInfo == null || networkInfo.getType() != ConnectivityManager.TYPE_WIFI) return;
        HttpUtil.getInstance().downloadFile(url, VLCApplication.OTA_PATH + "/" + fileName, new HttpUtil.DownloadCallback() {

            @Override
            public void onDownloading(int progress) {
                if (downloadComplete != null) {
                    downloadComplete.onDownloading(progress);
                }
            }

            @Override
            public void onDownloadComplete(String path) {
                try {
                    if (downloadComplete != null) {
                        if (md5.toLowerCase().equals(CheckVersionTask.fileMD5(path).toLowerCase())) {
                            downloadComplete.onDownloadComplte(path, true);
                            SpUtils.putString(VLCApplication.getAppContext(), CAME_LOCAL_PATH, path);
                        } else {
                            downloadComplete.onDownloadComplte(path, false);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDownladFail() {
                if (downloadComplete != null) {
                    downloadComplete.onDownloadComplte(null, false);
                }
            }
        });
    }

    public boolean checkFile(String md5) {
        if (TextUtils.isEmpty(md5)) return false;
        try {
            if (md5.toLowerCase().equals(CheckVersionTask.fileMD5(SpUtils.getString(VLCApplication.getAppContext(), CAME_LOCAL_PATH, "")).toLowerCase())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
