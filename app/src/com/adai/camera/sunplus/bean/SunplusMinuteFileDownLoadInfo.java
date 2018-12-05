package com.adai.camera.sunplus.bean;


import com.adai.camera.sunplus.tool.SunplusMinuteFileDownloadManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2017/4/12 10:15.
 */

public class SunplusMinuteFileDownLoadInfo {
    public int key ;
    public List<SunplusDownloadInfo> allDownloadInfos = new ArrayList<>();
    public List<SunplusDownloadInfo> downloadedInfos = new ArrayList<>();
    public List<SunplusDownloadInfo> waitDownloadInfos = new ArrayList<>();
    public int state = SunplusMinuteFileDownloadManager.STATE_NONE;
    public long allSize = 0L;
    public long downloadSize = 0L;
    public int progress = 0;
    public Runnable task; // 下载的任务
}
