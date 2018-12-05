package com.adai.gkdnavi.utils;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.Hjni.HbxFishEye;
import com.adai.camera.CameraConstant;
import com.adai.gkdnavi.R;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Created by huangxy on 2016/12/14.
 */

public class MinuteFileDownloadManager {
    private static final String TAG = "DownloadManager";
    public static final int STATE_NONE = 0; // 未下载
    public static final int STATE_WAITTING = 1; // 等待
    public static final int STATE_DOWNLOADING = 2; // 下载中
    public static final int STATE_PAUSE = 3; // 暂停
    public static final int STATE_DOWNLOADED = 4;//下载完成
    public static final int STATE_FAILED = 5; //下载失败
    public static boolean isDownloading = false;
    private static MinuteFileDownloadManager instance;
    private ThreadPoolProxy mPool;

    private Map<Integer, MinuteFileDownloadInfo> mInfos = new LinkedHashMap<>();
    private HashSet<DownloadObserver> mObservers = new LinkedHashSet<>();

    private MinuteFileDownloadManager() {
        mPool = ThreadPoolManager.getInstance().getDownloadPool();
    }


    public static MinuteFileDownloadManager getInstance() {
        if (instance == null) {
            synchronized (MinuteFileDownloadManager.class) {
                if (instance == null) {
                    instance = new MinuteFileDownloadManager();
                }
            }
        }
        return instance;
    }

    /**
     * 下载的观察者
     */
    public interface DownloadObserver {
        void onDownloadStateChanged(MinuteFileDownloadInfo info);
    }

    /***
     * 获取下载信息
     */
    public MinuteFileDownloadInfo getMinuteFileDownloadInfo(MinuteFile minuteFile) {
        MinuteFileDownloadInfo cacheMinuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (cacheMinuteFileDownloadInfo != null) {
            return cacheMinuteFileDownloadInfo;
        }
        MinuteFileDownloadInfo minuteFileDownloadInfo = new MinuteFileDownloadInfo();
        minuteFileDownloadInfo.key = minuteFile.hashCode();
        for (FileDomain fileDomain : minuteFile.fileDomains) {
            String cachePath = getDownloadPath(fileDomain.getName());
            File file = new File(cachePath);
            DownLoadInfo downLoadInfo = generateDownloadInfo(fileDomain);
            if (file.exists()) {//由于下载完后才将文件后缀从.temp变成原先的后缀，所以文件存在就认为是下载完成了的
//                if (file.length() == fileDomain.getSize()) {
                downLoadInfo.state = STATE_DOWNLOADED;
                minuteFileDownloadInfo.downloadedInfos.add(downLoadInfo);
                minuteFileDownloadInfo.downloadSize += downLoadInfo.size;
//                } else {
//                    downLoadInfo.state = STATE_FAILED;
//                    file.delete();
//                    minuteFileDownloadInfo.waitDownloadInfos.add(downLoadInfo);
//                    minuteFileDownloadInfo.state = STATE_FAILED;
//                }
            } else {
                minuteFileDownloadInfo.waitDownloadInfos.add(downLoadInfo);
                String tempPath = getTempPath(downLoadInfo.fileName);
                File tempFile = new File(tempPath);
                if (tempFile.exists()) {
                    minuteFileDownloadInfo.downloadSize += tempFile.length();
                }
            }
            minuteFileDownloadInfo.allSize += downLoadInfo.size;
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

    private DownLoadInfo generateDownloadInfo(FileDomain fileDomain) {
        DownLoadInfo downLoadInfo = new DownLoadInfo();
        downLoadInfo.fileName = fileDomain.getName();
        downLoadInfo.downloadUrl = fileDomain.getDownloadPath();
        downLoadInfo.savePath = getDownloadPath(fileDomain.getName());
        downLoadInfo.size = fileDomain.getSize();
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

    public void download(MinuteFile bean) {
        MinuteFileDownloadInfo info = getMinuteFileDownloadInfo(bean);
        Log.e(TAG, "download: state = " + info.state);
        if (info.state == STATE_DOWNLOADED || info.state == STATE_DOWNLOADING) return;
        info.state = STATE_WAITTING;
        notifyStateChanged(info);
        // 添加到下载记录中
        mInfos.put(bean.hashCode(), info);
        MinuteFileDownloadManager.DownloadTask task = new MinuteFileDownloadManager.DownloadTask(info);
        info.task = task;
        mPool.execute(task);//

    }

    public void cancle() {
        mPool.shutdownNow();
    }

    public void addInfo(MinuteFile bean) {
        MinuteFileDownloadInfo minuteFileDownloadInfo = getMinuteFileDownloadInfo(bean);
        mInfos.put(bean.hashCode(), minuteFileDownloadInfo);
    }

    private class DownloadTask implements Runnable {
        private MinuteFileDownloadInfo mMinuteFileDownloadInfo;

        public DownloadTask(MinuteFileDownloadInfo info) {
            this.mMinuteFileDownloadInfo = info;
        }

        @Override
        public void run() {
            mMinuteFileDownloadInfo.state = STATE_DOWNLOADING;
            isDownloading = true;
            notifyStateChanged(mMinuteFileDownloadInfo);
            //开始下载未下载或未完成的文件
            for (int i = 0; i < mMinuteFileDownloadInfo.waitDownloadInfos.size(); i++) {
                DownLoadInfo downLoadInfo = mMinuteFileDownloadInfo.waitDownloadInfos.get(i);
                String filePath = downLoadInfo.downloadUrl;
//                String path;
//                path = (Contacts.BASE_HTTP_IP + filePath.substring(filePath.indexOf(":") + 1)).replace("\\", "/");
                String tempPath = getTempPath(downLoadInfo.fileName);
                File file = new File(tempPath);
                long downloadedSize = 0L;//当前下载文件已下载的大小
                if (file.exists()) {
                    downloadedSize = file.length();
                }
                InputStream is = null;
                OutputStream os = null;
                try {
                    URL url = new URL(filePath);
                    HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
                    // 设置 User-Agent
                    httpConnection.setRequestProperty("User-Agent", "NetFox");
                    // 设置断点续传的开始位置
                    httpConnection.setRequestProperty("RANGE", "bytes=" + downloadedSize + "-");
                    int contentLength = httpConnection.getContentLength();
                    if(downLoadInfo.downloadUrl.startsWith("http://192.168.1.1:8080")) {
                        //杰理摄像头无法获取ContentLength  使用返回的文件长度作为length
                        contentLength = (int) downLoadInfo.size;
                    }
//                    LogUtils.e("contentLength=" + contentLength);
                    is = httpConnection.getInputStream();
                    os = new FileOutputStream(file, true);// 追加
                    byte[] buffer = new byte[1024 * 16];
                    int len = -1;
                    int downloadedLength = 0;
                    if(downLoadInfo.downloadUrl.startsWith("http://192.168.1.1:8080")) {
                        //杰理摄像头无法获取ContentLength  使用返回的文件长度作为length
                        downloadedLength = (int) file.length();
                    }
                    while ((len = is.read(buffer)) != -1) {
                        if (mMinuteFileDownloadInfo.state == STATE_PAUSE) {
                            break;
                        }
                        if (mMinuteFileDownloadInfo.state == STATE_NONE) {
                            break;
                        }
                        if (!VLCApplication.allowDownloads) {
                            break;
                        }
                        os.write(buffer, 0, len);
                        downloadedLength += len;
                        mMinuteFileDownloadInfo.downloadSize += len;

                        try {
                            if (mMinuteFileDownloadInfo.downloadSize * 100 / mMinuteFileDownloadInfo.allSize - mMinuteFileDownloadInfo.progress >= 1) {
                                //每下载了1%才更新进度
                                notifyStateChanged(mMinuteFileDownloadInfo);
                                mMinuteFileDownloadInfo.progress = (int) (mMinuteFileDownloadInfo.downloadSize * 100 / mMinuteFileDownloadInfo.allSize);
                                //                            Log.e(TAG, "run: progress:" + mMinuteFileDownloadInfo.progress);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (!VLCApplication.allowDownloads) {
                        break;
                    }
                    if (mMinuteFileDownloadInfo.state == STATE_NONE) {
                        isDownloading = false;
                        file.delete();
                        break;
                    } else {
                        if (mMinuteFileDownloadInfo.state == STATE_PAUSE) {
                            notifyStateChanged(mMinuteFileDownloadInfo);
                            isDownloading = false;
                            break;
                        } else {
//                            LogUtils.e("downloadLength = " + downloadedLength + " contentLength = " + contentLength);
//                            if (file.length() == downLoadInfo.size) {
                            if (downloadedLength == contentLength) {
                                //该文件下载完成,更改后缀名
//                                Log.e(TAG, "run: " + file.getName() + "下载完成");
                                mMinuteFileDownloadInfo.downloadSize = mMinuteFileDownloadInfo.downloadSize + (downLoadInfo.size - contentLength);
                                String cachePath = getDownloadPath(downLoadInfo.fileName);
                                File cacheFile = new File(cachePath);
                                file.renameTo(cacheFile);
                                file = cacheFile;
                                isDownloading = false;
                                VLCApplication.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                                //需要判断当前是否360摄像头网络
                                String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
                                Log.e("9999", "product_model = " + product_model);
//                                if (product_model.equals("100")) {
                                    HbxFishEye.SaveId2File(file.getPath(), 3, 1);
//                                    Log.e("9999", "是全景文件");
//                                } else {
//
//                                    Log.e("9999", "不是全景文件");
//                                }
                            } else {
                                if (mMinuteFileDownloadInfo.state == STATE_DOWNLOADING) {
                                    mMinuteFileDownloadInfo.state = STATE_PAUSE;
                                    notifyStateChanged(mMinuteFileDownloadInfo);
                                    isDownloading = false;
                                    break;
                                } else {
                                    //该文件下载失败
                                    mPool.remove(mMinuteFileDownloadInfo.task);
                                    mMinuteFileDownloadInfo.state = STATE_FAILED;
                                    notifyStateChanged(mMinuteFileDownloadInfo);
                                    file.delete();
                                    isDownloading = false;
                                    break;
                                }
                            }
                            if (mMinuteFileDownloadInfo.downloadSize >= mMinuteFileDownloadInfo.allSize) {
                                //下载完成
                                mPool.remove(mMinuteFileDownloadInfo.task);
//                                Log.e(TAG, "run: 下载完成");
                                mMinuteFileDownloadInfo.state = STATE_DOWNLOADED;
                                notifyStateChanged(mMinuteFileDownloadInfo);
                                isDownloading = false;
                                break;
                            }
                        }
                    }

                } catch (IOException e) {
//                    Log.e(TAG, "run: " + file.getName() + "下载中断");
                    e.printStackTrace();
                    mMinuteFileDownloadInfo.state = STATE_FAILED;
                    notifyStateChanged(mMinuteFileDownloadInfo);
                    isDownloading = false;
                    break;
                } finally {
                    isDownloading = false;
                    IOUtils.close(os);
                    IOUtils.close(is);
                }
            }
        }

    }

    /**
     * 暂停下载
     *
     * @param minuteFile
     */
    public void pause(MinuteFile minuteFile) {
        MinuteFileDownloadInfo minuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (minuteFileDownloadInfo == null) return;
        if (minuteFileDownloadInfo.state == STATE_DOWNLOADING) {
            mPool.remove(minuteFileDownloadInfo.task);
            minuteFileDownloadInfo.state = STATE_PAUSE;
        }
    }

    /**
     * 取消下载
     *
     * @param minuteFile
     */
    public void cancel(MinuteFile minuteFile) {
        MinuteFileDownloadInfo minuteFileDownloadInfo = mInfos.get(minuteFile.hashCode());
        if (minuteFileDownloadInfo != null && minuteFileDownloadInfo.task != null) {
            mPool.remove(minuteFileDownloadInfo.task);
            minuteFileDownloadInfo.state = STATE_NONE;
            notifyStateChanged(minuteFileDownloadInfo);
        }
    }

    //通知下载状态改变
    private synchronized void notifyStateChanged(final MinuteFileDownloadInfo info) {
        if (info.state == STATE_DOWNLOADED) {
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtil.showShortToast(VLCApplication.getInstance(), info.allDownloadInfos.get(0).fileName + VLCApplication.getInstance().getString(R.string.download_cuccess));
                }
            });
        }
        for (DownloadObserver observer : mObservers) {
            observer.onDownloadStateChanged(info);
        }
    }

    public synchronized void addObserver(MinuteFileDownloadManager.DownloadObserver observer) {
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
