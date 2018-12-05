package com.adai.camera.sunplus.tool;

import android.content.Intent;
import android.net.Uri;

import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.bean.SunplusDownloadInfo;
import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.bean.SunplusMinuteFileDownLoadInfo;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ThreadPoolManager;
import com.adai.gkdnavi.utils.ThreadPoolProxy;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.example.ipcamera.application.VLCApplication;
import com.icatch.wificam.customer.type.ICatchFile;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Created by huangxy on 2017/4/12 10:00.
 */

public class SunplusMinuteFileDownloadManager {
    private static final String TAG = "DownloadManager";
    public static final int STATE_NONE = 0; // 未下载
    public static final int STATE_WAITTING = 1; // 等待
    public static final int STATE_DOWNLOADING = 2; // 下载中
    public static final int STATE_PAUSE = 3; // 暂停
    public static final int STATE_DOWNLOADED = 4;//下载完成
    public static final int STATE_FAILED = 5; //下载失败
    public static boolean isDownloading;
    private static SunplusMinuteFileDownloadManager instance;
    private ThreadPoolProxy mPool;

    private Map<Integer, SunplusMinuteFileDownLoadInfo> mInfos = new LinkedHashMap<>();
    private List<DownloadObserver> mObservers = new LinkedList<>();

    private SunplusMinuteFileDownloadManager() {
        mPool = ThreadPoolManager.getInstance().getDownloadPool();
    }


    public static SunplusMinuteFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (SunplusMinuteFileDownloadManager.class) {
                if (instance == null) {
                    instance = new SunplusMinuteFileDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 下载的观察者
     */
    public interface DownloadObserver {
        void onDownloadStateChanged(SunplusMinuteFileDownLoadInfo info);
    }

    /***
     * 获取下载信息
     */
    public SunplusMinuteFileDownLoadInfo getMinuteFileDownloadInfo(SunplusMinuteFile minuteFile) {
        SunplusMinuteFileDownLoadInfo cacheMinuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (cacheMinuteFileDownloadInfo != null) {
            return cacheMinuteFileDownloadInfo;
        }
        SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = new SunplusMinuteFileDownLoadInfo();
        minuteFileDownloadInfo.key = minuteFile.hashCode();
        for (ICatchFile fileDomain : minuteFile.fileDomains) {
            String cachePath = getDownloadPath(fileDomain.getFileName());
            File file = new File(cachePath);
            SunplusDownloadInfo downLoadInfo = generateDownloadInfo(fileDomain);
            if (file.exists()) {//由于下载完后才将文件后缀从.temp变成原先的后缀，所以文件存在就认为是下载完成了的
                if (file.length() == fileDomain.getFileSize()) {
                    downLoadInfo.state = STATE_DOWNLOADED;
                    minuteFileDownloadInfo.downloadedInfos.add(downLoadInfo);
                    minuteFileDownloadInfo.downloadSize += downLoadInfo.mICatchFile.getFileSize();
                } else {
                    downLoadInfo.state = STATE_FAILED;
                    file.delete();
                    minuteFileDownloadInfo.waitDownloadInfos.add(downLoadInfo);
                    minuteFileDownloadInfo.state = STATE_FAILED;
                }
            } else {
                minuteFileDownloadInfo.waitDownloadInfos.add(downLoadInfo);
                String tempPath = getTempPath(downLoadInfo.mICatchFile.getFileName());
                File tempFile = new File(tempPath);
                if (tempFile.exists()) {
                    minuteFileDownloadInfo.downloadSize += tempFile.length();
                }
            }
            minuteFileDownloadInfo.allSize += downLoadInfo.mICatchFile.getFileSize();
            minuteFileDownloadInfo.allDownloadInfos.add(downLoadInfo);
        }
        minuteFileDownloadInfo.progress = minuteFileDownloadInfo.allSize == 0 ? 0 : (int) (minuteFileDownloadInfo.downloadSize * 100 / minuteFileDownloadInfo.allSize);
        if (minuteFileDownloadInfo.progress == 100) {
            minuteFileDownloadInfo.state = STATE_DOWNLOADED;
        } else if (minuteFileDownloadInfo.progress == 0) {
            minuteFileDownloadInfo.state = STATE_NONE;
        } else {
            minuteFileDownloadInfo.state = STATE_PAUSE;
        }
        return minuteFileDownloadInfo;
    }

    private SunplusDownloadInfo generateDownloadInfo(ICatchFile iCatchFile) {
        SunplusDownloadInfo downLoadInfo = new SunplusDownloadInfo();
        downLoadInfo.savePath = getDownloadPath(iCatchFile.getFileName());
        downLoadInfo.mICatchFile = iCatchFile;
        return downLoadInfo;
    }


    // 获取存储路径：/GKD/name.mov
    public String getDownloadPath(String fileName) {
        File file = new File(VLCApplication.DOWNLOADPATH, fileName);
        return file.getAbsolutePath();
    }

    public String getTempPath(String fileName) {
        //String dir = FileUtils.getDir("GKD");
        File file = new File(VLCApplication.DOWNLOADPATH, fileName + ".temp");
        return file.getAbsolutePath();
    }

    public void download(SunplusMinuteFile bean) {
        SunplusMinuteFileDownLoadInfo info = getMinuteFileDownloadInfo(bean);
        if (info.state == STATE_DOWNLOADED) {
            return;
        }
        info.state = STATE_WAITTING;
        notifyStateChanged(info);
        // 添加到下载记录中
        mInfos.put(bean.hashCode(), info);
        DownloadTask task = new DownloadTask(info);
        info.task = task;
        mPool.execute(task);//

    }

    public void addInfo(SunplusMinuteFile bean) {
        SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = getMinuteFileDownloadInfo(bean);
        mInfos.put(bean.hashCode(), minuteFileDownloadInfo);
    }

    private class DownloadTask implements Runnable {
        private SunplusMinuteFileDownLoadInfo mMinuteFileDownloadInfo;

        public DownloadTask(SunplusMinuteFileDownLoadInfo info) {
            this.mMinuteFileDownloadInfo = info;
        }

        @Override
        public void run() {
            mMinuteFileDownloadInfo.state = STATE_DOWNLOADING;
            isDownloading = true;
            notifyStateChanged(mMinuteFileDownloadInfo);
            //开始下载未下载或未完成的文件
            for (int i = 0; i < mMinuteFileDownloadInfo.waitDownloadInfos.size(); i++) {
                SunplusDownloadInfo downLoadInfo = mMinuteFileDownloadInfo.waitDownloadInfos.get(i);
                String tempPath = getTempPath(downLoadInfo.mICatchFile.getFileName());
                File file = new File(tempPath);
                //TODO:  已下载过的可能重复下载
                isDownloading = true;
                if (mMinuteFileDownloadInfo.state == STATE_PAUSE) {
                    isDownloading = false;
                    break;
                }
                if (mMinuteFileDownloadInfo.state == STATE_NONE) {
                    isDownloading = false;
                    break;
                }
                boolean ret = FileOperation.getInstance().downloadFile(downLoadInfo.mICatchFile, tempPath);
                if (ret) {
                    //下载成功,更改后缀名
                    String cachePath = getDownloadPath(downLoadInfo.mICatchFile.getFileName());
                    File cacheFile = new File(cachePath);
                    file.renameTo(cacheFile);
                    file = cacheFile;
                    VLCApplication.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                    mMinuteFileDownloadInfo.downloadSize += downLoadInfo.mICatchFile.getFileSize();
                    if (mMinuteFileDownloadInfo.downloadSize >= mMinuteFileDownloadInfo.allSize) {
                        //下载完成
                        mMinuteFileDownloadInfo.state = STATE_DOWNLOADED;
                        notifyStateChanged(mMinuteFileDownloadInfo);
                    }
                } else {
                    //下载失败，删除文件
                    isDownloading = false;
                    mMinuteFileDownloadInfo.state = STATE_FAILED;
                    notifyStateChanged(mMinuteFileDownloadInfo);
                    file.delete();
                    break;
                }
            }
            isDownloading = false;
        }

    }

    /**
     * 暂停下载
     *
     * @param minuteFile
     */
    public void pause(SunplusMinuteFile minuteFile) {
        SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (minuteFileDownloadInfo == null) return;
        if (minuteFileDownloadInfo.state == STATE_DOWNLOADING) {
            minuteFileDownloadInfo.state = STATE_PAUSE;
        }
    }

    /**
     * 取消下载
     *
     * @param minuteFile
     */
    public void cancel(SunplusMinuteFile minuteFile) {
        SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (minuteFileDownloadInfo != null && minuteFileDownloadInfo.task != null) {
            mPool.remove(minuteFileDownloadInfo.task);
            minuteFileDownloadInfo.state = STATE_NONE;
            notifyStateChanged(minuteFileDownloadInfo);
        }
    }

    public void stop() {
        mPool.killPool();
    }

    //通知下载状态改变
    private synchronized void notifyStateChanged(final SunplusMinuteFileDownLoadInfo info) {
        if (info.state == STATE_DOWNLOADED) {
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showShortToast(VLCApplication.getInstance(), info.allDownloadInfos.get(0).mICatchFile.getFileName() + VLCApplication.getInstance().getString(R.string.download_cuccess));
                }
            });
        }
        ListIterator<DownloadObserver> iterator = mObservers.listIterator();
        while (iterator.hasNext()) {
            DownloadObserver observer = iterator.next();
            observer.onDownloadStateChanged(info);
        }
    }

    public synchronized void addObserver(DownloadObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }


    public synchronized void deleteObserver(MinuteFileDownloadManager.DownloadObserver observer) {
        mObservers.remove(observer);
    }

    public void removeAllObserver() {
        mObservers.clear();
    }
}
