package com.adai.camera.hisi.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;

import com.adai.camera.CameraFactory;
import com.adai.camera.hisi.HisiCamera;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import static com.adai.camera.hisi.sdk.Common.FAILURE;

/**
 * @author huangxy
 * @date 2018/3/3 18:18.
 */

public class HisiFilePresenter extends HisiFileFragmentContract.Presenter {
    private ArrayList<MinuteFile> mMinuteFiles = new ArrayList<>();
    private ArrayList<FileDomain> mCameraFiles;
    private SimpleDateFormat mSimpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
    private ArrayList<FileDomain> mDeleteList;
    private Context mContext;

    @Override
    public void attachView(HisiFileFragmentContract.View view) {
        super.attachView(view);
        mContext = mView.getAttachedContext();
    }

    @Override
    public void sortFile(ArrayList<FileDomain> cameraFiles) {
        mCameraFiles = cameraFiles;
        Collections.sort(cameraFiles, new Comparator<FileDomain>() {
            @Override
            public int compare(FileDomain o1, FileDomain o2) {
                if (o2.timeCode == o1.timeCode) {
                    return 0;
                }
                if (o2.timeCode > o1.timeCode) {
                    return 1;
                }
                return -1;
            }
        });
        mSimpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
        mMinuteFiles.clear();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = fileDomain.timeCode;
            String time = mSimpleDateFormat.format(new Date(lastModified));
            String minuteTime = time.substring(0, 16);
            String hourTime = time.substring(0, 13);
            int minute = 0;
            try {
                minute = Integer.valueOf(time.substring(14, 16));
            } catch (NumberFormatException ignore) {
            }
            String parentTime;
            if (minute <= 30) {
                parentTime = hourTime + ":00";
            } else {
                parentTime = hourTime + ":30";
            }
            if (mMinuteFiles.size() == 0) {
                minuteFile.parentTime = parentTime;
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.time = time;
                //第一次直接添加
                minuteFile.isTitle = true;//第一个是title
                mMinuteFiles.add(minuteFile);
                //加完title后加真正的内容
                minuteFile = new MinuteFile();
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.parentTime = parentTime;
                minuteFile.time = time;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                mMinuteFiles.add(minuteFile);
            } else if (minuteTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).minuteTime)) {
                //说明是同一分钟的文件
                minuteFile = new MinuteFile();
                minuteFile.parentTime = parentTime;
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.time = time;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                mMinuteFiles.add(minuteFile);
            } else {
                //不是通一分钟的文件,需要判断是不是同一个小时的文件
//                if (i != cameraFiles.size() - 1) {
                if (parentTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).parentTime)) {
                    //是同一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.parentTime = parentTime;
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                } else {
                    //不是一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.parentTime = parentTime;
                    minuteFile.isTitle = true;
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    mMinuteFiles.add(minuteFile);
                    minuteFile = new MinuteFile();
                    minuteFile.parentTime = parentTime;
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                }
            }
        }
        mView.sortFileEnd(mMinuteFiles);
    }

    @Override
    public void download() {
        for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
            if (!minuteFile.isTitle && minuteFile.isChecked) {
                MinuteFileDownloadManager.getInstance().download(minuteFile);
            }
        }
        mView.setEditMode(false);
    }

    @Override
    public void deleteFile() {
        if (MinuteFileDownloadManager.isDownloading) {
            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.please_stop_download));
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.notice)
                .setMessage(R.string.navi_confDel)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeleteList = new ArrayList<>();
                        for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                            mDeleteList.addAll(minuteFile.fileDomains);
                        }
                        VoiceManager.selectedMinuteFile.clear();
                        DeleteFileTask deleteFileTask = new DeleteFileTask();
                        deleteFileTask.execute();

                    }
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.showLoading(R.string.deleting);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
            for (FileDomain fileDomain : mDeleteList) {
                if (FAILURE != dv.deleteFile(fileDomain.fpath)) {
                    mCameraFiles.remove(fileDomain);
                } else {
                    break;
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sortFile(mCameraFiles);
        }
    }
}
