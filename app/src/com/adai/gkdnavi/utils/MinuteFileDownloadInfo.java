package com.adai.gkdnavi.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2016/12/14.
 */

public class MinuteFileDownloadInfo {
    public int key ;
    public List<DownLoadInfo> allDownloadInfos = new ArrayList<>();
    public List<DownLoadInfo> downloadedInfos = new ArrayList<>();
    public List<DownLoadInfo> waitDownloadInfos = new ArrayList<>();
    public int state = MinuteFileDownloadManager.STATE_NONE;
    public long allSize = 0L;
    public long downloadSize = 0L;
    public int progress = 0;
    public Runnable task; // 下载的任务

}
