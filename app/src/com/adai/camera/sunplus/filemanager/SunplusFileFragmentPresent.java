package com.adai.camera.sunplus.filemanager;

import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.tool.SunplusMinuteFileDownloadManager;
import com.adai.gkdnavi.R;
import com.icatch.wificam.customer.type.ICatchFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by huangxy on 2017/9/19 10:18.
 */

public class SunplusFileFragmentPresent extends SunplusFileFragmentContract.Presenter {
    private static final int SORT_FILE_END = 1;
    private static final int DELETE_RECORD_FILE = 3;
    private ArrayList<ICatchFile> mCameraFiles;
    private ArrayList<SunplusMinuteFile> mSunplusMinuteFiles = new ArrayList<>();
    private ExecutorService executor;
    private Future<?> mDeleteFileFuture;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SORT_FILE_END:
                    mView.hideLoading();
                    mView.sortFileEnd(mSunplusMinuteFiles);
                    break;
            }
        }
    };

    @Override
    public void attachView(SunplusFileFragmentContract.View view) {
        super.attachView(view);
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void sortFile(ArrayList<ICatchFile> cameraFiles) {
        mCameraFiles = cameraFiles;
        Collections.sort(cameraFiles, new Comparator<ICatchFile>() {
            @Override
            public int compare(ICatchFile o1, ICatchFile o2) {
                return o2.getFileDate().compareTo(o1.getFileDate());
            }
        });
        mSunplusMinuteFiles.clear();
        SunplusMinuteFile sunplusMinuteFile = new SunplusMinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            String date = cameraFiles.get(i).getFileDate().replace("T", "");
            SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
            format.applyPattern("yyyyMMddHHmmss");
            Date parseData = null;
            try {
                parseData = format.parse(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            format.applyPattern("yyyy-MM-dd HH:mm:ss");
            String time = format.format(parseData);
            String minuteTime = time.substring(0, 16);
            String hourTime = time.substring(0, 13);
            if (mSunplusMinuteFiles.size() == 0) {
                sunplusMinuteFile.hourTime = hourTime;
                sunplusMinuteFile.minuteTime = minuteTime;
                sunplusMinuteFile.time = time;
                //第一次直接添加
                sunplusMinuteFile.isTitle = true;//第一个是title
                mSunplusMinuteFiles.add(sunplusMinuteFile);
                //加完title后加真正的内容
                sunplusMinuteFile = new SunplusMinuteFile();
                sunplusMinuteFile.hourTime = hourTime;
                sunplusMinuteFile.minuteTime = minuteTime;
                sunplusMinuteFile.time = time;
                sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
                mSunplusMinuteFiles.add(sunplusMinuteFile);
            } else if (minuteTime.equals(mSunplusMinuteFiles.get(mSunplusMinuteFiles.size() - 1).minuteTime)) {
                //说明是同一分钟的文件
//                if (cameraFiles.get(i).getFileType() == ICatchFileType.ICH_TYPE_IMAGE) {
//                    //如果是图片就放到同一分钟的图片下面
//                    if (sunplusMinuteFile.fileDomains.get(0).getFileType() == ICatchFileType.ICH_TYPE_IMAGE) {
//                        sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
//                    } else {
//                        sunplusMinuteFile = new SunplusMinuteFile();
//                        sunplusMinuteFile.hourTime = hourTime;
//                        sunplusMinuteFile.minuteTime = minuteTime;
//                        sunplusMinuteFile.time = time;
//                        sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
//                        mSunplusMinuteFiles.add(sunplusMinuteFile);
//                    }
//                } else {
                //视频不需要合并到通一分钟下
                sunplusMinuteFile = new SunplusMinuteFile();
                sunplusMinuteFile.hourTime = hourTime;
                sunplusMinuteFile.minuteTime = minuteTime;
                sunplusMinuteFile.time = time;
                sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
                mSunplusMinuteFiles.add(sunplusMinuteFile);
//                }
            } else {
                //不是通一分钟的文件,需要判断是不是同一个小时的文件
                if (hourTime.equals(mSunplusMinuteFiles.get(mSunplusMinuteFiles.size() - 1).hourTime)) {
                    //是同一个小时下的文件
                    sunplusMinuteFile = new SunplusMinuteFile();
                    sunplusMinuteFile.hourTime = hourTime;
                    sunplusMinuteFile.minuteTime = minuteTime;
                    sunplusMinuteFile.time = time;
                    sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
                    mSunplusMinuteFiles.add(sunplusMinuteFile);
                } else {
                    //不是一个小时下的文件
                    sunplusMinuteFile = new SunplusMinuteFile();
                    sunplusMinuteFile.isTitle = true;
                    sunplusMinuteFile.hourTime = hourTime;
                    sunplusMinuteFile.minuteTime = minuteTime;
                    sunplusMinuteFile.time = time;
                    mSunplusMinuteFiles.add(sunplusMinuteFile);
                    sunplusMinuteFile = new SunplusMinuteFile();
                    sunplusMinuteFile.hourTime = hourTime;
                    sunplusMinuteFile.minuteTime = minuteTime;
                    sunplusMinuteFile.time = time;
                    sunplusMinuteFile.fileDomains.add(cameraFiles.get(i));
                    mSunplusMinuteFiles.add(sunplusMinuteFile);
                }
//                }
            }
        }
        sendMessage(SORT_FILE_END);
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    @Override
    public void download() {
        for (SunplusMinuteFile minuteFile : GlobalInfo.mSelectedMinuteFile) {
            if (!minuteFile.isTitle) {
                SunplusMinuteFileDownloadManager.getInstance().download(minuteFile);
            }
        }
        mView.setEditMode(false);
    }

    @Override
    public void deleteFile() {
        if (SunplusMinuteFileDownloadManager.isDownloading) {
            mView.showToast(R.string.please_stop_download);
            return;
        }
        new AlertDialog.Builder(mView.getAttachedContext())
                .setTitle(R.string.notice)
                .setMessage(R.string.navi_confDel)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mView.showLoading(R.string.deleting);
                        mDeleteFileFuture = executor.submit(new DeleteFileListThread());
                    }
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    private class DeleteFileListThread implements Runnable {

        @Override
        public void run() {
            if (GlobalInfo.mSelectedMinuteFile.size() < 0) {
                return;
            }
            HashSet<SunplusMinuteFile> deleteFileList = new HashSet<>();
            deleteFileList.addAll(GlobalInfo.mSelectedMinuteFile);
            for (SunplusMinuteFile minuteFile : deleteFileList) {
                if (!minuteFile.isTitle) {
                    for (ICatchFile iCatchFile : minuteFile.fileDomains) {
                        boolean ret = FileOperation.getInstance().deleteFile(iCatchFile);
                        if (ret) {
                            mCameraFiles.remove(iCatchFile);
                        }
                    }
                }
            }
            sortFile(mCameraFiles);
        }
    }
}
