package com.adai.gkdnavi.utils;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Log;

import com.adai.camera.novatek.contacts.Contacts;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    public static final int STATE_NONE = 0; // 未下载
    public static final int STATE_WAITTING = 1; // 等待
    public static final int STATE_DOWNLOADING = 2; // 下载中
    public static final int STATE_PAUSE = 3; // 暂停
    public static final int STATE_DOWNLOADED = 4;//下载完成
    public static final int STATE_FAILED = 5; //下载失败

    private VLCApplication app;
    private static DownloadManager instance;
    private ThreadPoolProxy mPool;

    private Map<String, DownLoadInfo> mInfos = new LinkedHashMap<String, DownLoadInfo>();
    private List<DownloadObserver> mObservers = new LinkedList<DownloadManager.DownloadObserver>();

    private DownloadManager() {
        mPool = ThreadPoolManager.getInstance().getDownloadPool();
    }


    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    //获取下载信息
    public DownLoadInfo getDownloadInfo(FileDomain bean) {

        //下载完成
        String savePath = getCachePath(bean.getName());
        File file = new File(savePath);
        if (file.exists()) {
            if (file.length() == bean.getSize()) {
                DownLoadInfo info = generateDownloadInfo(bean);
                info.state = STATE_DOWNLOADED;
                return info;
            }
        }
        DownLoadInfo info = mInfos.get(bean.getName());
        if (info != null) {
            return info;
        }
        if (file.exists()) {
            //System.out.println("下载失败");
            info = generateDownloadInfo(bean);
            // ######## 状态值: 下载失败 ##############
            info.state = STATE_FAILED;
            file.delete();
            // ##################################
            return info;
        }
        //未下载
        return generateDownloadInfo(bean);

    }


    private DownLoadInfo generateDownloadInfo(FileDomain bean) {
        DownLoadInfo info = new DownLoadInfo();
        info.fileName = bean.getName();
        info.downloadUrl = bean.getFpath();
        info.savePath = getCachePath(bean.getName());
        info.size = bean.getSize();
        return info;
    }

    // 获取存储路径：/GKD/name.mov
    public String getCachePath(String fileName) {
        //String dir = FileUtils.getDir("GKD");
        File file = new File(VLCApplication.DOWNLOADPATH, fileName);
        return file.getAbsolutePath();
    }

    public String getTempPath(String fileName) {
        //String dir = FileUtils.getDir("GKD");
        File file = new File(VLCApplication.DOWNLOADPATH, fileName + ".temp");
        return file.getAbsolutePath();
    }
    public void download(FileDomain bean) {
        final DownLoadInfo info = generateDownloadInfo(bean);
        info.state = STATE_WAITTING;
        notifyStateChanged(info);
        // 添加到下载记录中
        mInfos.put(bean.getName(), info);
        DownloadTask task = new DownloadTask(info);
        info.task = task;
        mPool.execute(task);//
    }


    //通知下载状态改变
    private synchronized void notifyStateChanged(DownLoadInfo info) {
        ListIterator<DownloadObserver> iterator = mObservers.listIterator();
        while (iterator.hasNext()) {
            DownloadObserver observer = iterator.next();
            observer.onDownloadStateChanged(info);
        }
    }


    class DownloadTask implements Runnable {
        private DownLoadInfo mInfo;

        public DownloadTask(DownLoadInfo info) {
            this.mInfo = info;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            mInfo.state = STATE_DOWNLOADING;
            notifyStateChanged(mInfo);
            String temppath = getTempPath(mInfo.fileName);
            File file = new File(temppath);
            long progress = 0;// 当前的进度
            if (file.exists()) {
                progress = file.length();
            }
//			HttpUtils utils = new HttpUtils();

            //#########################
//            String fileName = mInfo.fileName;
            String filePath = mInfo.downloadUrl;
            String url;
            // FIXME: 2016/10/28 暂时修改规则
//			if (fileName.contains("JPG")) {
//				url = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO + mInfo.fileName;
//			} else {
//				if (filePath.contains("RO")) {
//					url = Contacts.URL_GET_THUMBNAIL_HEAD_RO
//							+ mInfo.fileName;
//				} else {
//					url = Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE + mInfo.fileName;
//				}
//			}
            url = (Contacts.BASE_HTTP_IP + filePath.substring(filePath.indexOf(":") + 1)).replace("\\", "/");
//			RequestParams params=new RequestParams();
//			params.addQueryStringParameter("name", mInfo.downloadUrl);// 下载的地址
//			params.addQueryStringParameter("RANGE", "bytes=" + progress);// 断点的位置

            InputStream is = null;
            OutputStream os = null;
            BufferedOutputStream bos = null;
            BufferedInputStream bis = null;
            try {
//				ResponseStream responseStream = utils.sendSync(HttpMethod.GET,
//						url, params);
//				is = responseStream.getBaseStream();// 获取输入流
                URL urll = new URL(url);
                HttpURLConnection httpConnection = (HttpURLConnection) urll.openConnection();
                // 设置 User-Agent
                httpConnection.setRequestProperty("User-Agent", "NetFox");
                // 设置断点续传的开始位置
                httpConnection.setRequestProperty("RANGE", "bytes=" + progress);
                is = httpConnection.getInputStream();
                os = new FileOutputStream(file, true);// 追加
//                bos = new BufferedOutputStream(os);
//                bis = new BufferedInputStream(is);
                boolean isPaused = false;
                byte[] buffer = new byte[1024 * 16];
                int len = -1;
                int percent = 0;
                while ((len = is.read(buffer)) != -1) {
                    // 将缓存区写到本地,输出操作
                    os.write(buffer, 0, len);

                    // 获取进度
                    progress += len;

                    // ########## 进度改变 ################
                    mInfo.currentProgress = progress;
                    // 通知更新
                    percent += len;
                    if (percent * 100 / mInfo.size >= 1) {
                        notifyStateChanged(mInfo);
                        percent = 0;
                    }
                    // ##################################

                    if (mInfo.state == STATE_PAUSE) {
                        // 说明用户暂停了
                        isPaused = true;
                        break;
                    }
                    if (mInfo.state == STATE_NONE) {
                        // 说明用户暂停了
                        // isPaused = true;
                        break;
                    }
                    if (!VLCApplication.allowDownloads) {
                        break;
                    }

                }

                if (mInfo.state == STATE_NONE) {
                    file.delete();
                } else {

                    if (isPaused) {
                        // 暂停
                        // ######## 状态值: 暂停 ##############
                        mInfo.state = STATE_PAUSE;
                        notifyStateChanged(mInfo);
                        // ##################################
                    } else {
                        if (file.length() == mInfo.size) {
                            // ######## 状态值: 下载成功 ##############
                            mInfo.state = STATE_DOWNLOADED;
                            String cachepath = getCachePath(mInfo.fileName);
                            File cachefile = new File(cachepath);
                            file.renameTo(cachefile);
                            file = cachefile;
                            notifyStateChanged(mInfo);

                            VLCApplication.getInstance().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                            if (getExtensionName(file.getName()).equals("MOV") || getExtensionName(file.getName()).toLowerCase().equals("mp4")) {
                                Log.e("9527", "filepath=" + file.getAbsolutePath() + "filename=" + file.getName());
                                //Log.e("9527", "filename=" + file.getName());
                                Bitmap bitmap = getVideoThumbnail(file.getAbsolutePath());
                                saveMyBitmap(file.getName(), bitmap);
                            }
                            // ##################################
                        } else {
                            if (mInfo.state == STATE_DOWNLOADING) {
                                mInfo.state = STATE_PAUSE;
                                notifyStateChanged(mInfo);
                            } else {
                                // ######## 状态值: 下载失败 ##############
                                mInfo.state = STATE_FAILED;
                                notifyStateChanged(mInfo);
                                file.delete();
                                // ##################################
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

                System.out.println("线程执行完成--exception");

                // ######## 状态值: 下载失败 ##############
                mInfo.state = STATE_FAILED;
                notifyStateChanged(mInfo);
//				file.delete();
            } finally {
                mInfo.task = null;
//                IOUtils.close(bos);
                IOUtils.close(os);
//                IOUtils.close(bis);
                IOUtils.close(is);
            }

        }

    }

    /**
     * 暂停下载
     *
     * @param bean
     */
    public void pause(FileDomain bean) {

        DownLoadInfo info = mInfos.get(bean.getName());

        // 不做处理
        if (info == null) {
            return;
        }

        if (info.state == STATE_DOWNLOADING) {
            // 设置下载的状态为暂停
            info.state = STATE_PAUSE;
            //#############
            //notifyStateChanged(info);
            //##############
        }
    }

    /**
     * 取消下载
     *
     * @param bean
     */
    public void cancel(FileDomain bean) {
        DownLoadInfo info = mInfos.get(bean.getName());
        if (info != null && info.task != null) {
            mPool.remove(info.task);

            // ######## 状态值: 未下载 ##############
            info.state = STATE_NONE;
            notifyStateChanged(info);
            // ##################################
        }
    }

    public synchronized void addObserver(DownloadObserver observer) {
        if (!mObservers.contains(observer)) {
            mObservers.add(observer);
        }
    }

    /**
     * 下载的观察者
     */
    public interface DownloadObserver {
        void onDownloadStateChanged(DownLoadInfo info);
    }

    public synchronized void deleteObserver(DownloadObserver observer) {
        mObservers.remove(observer);
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void saveMyBitmap(String bitName, Bitmap mBitmap) {
        String newFileName = bitName.substring(0, bitName.lastIndexOf("."));

        File f = new File(VLCApplication.DOWNLOADPATH + "/" + newFileName + ".PNG");
        try {
            f.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }
}
