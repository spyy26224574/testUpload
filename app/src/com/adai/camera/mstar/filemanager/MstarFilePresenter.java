package com.adai.camera.mstar.filemanager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.adai.camera.mstar.CameraCommand;
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

/**
 * Created by huangxy on 2017/10/16 16:32.
 */

public class MstarFilePresenter extends MstarFileContract.Presenter {
    private static SimpleDateFormat mSdfRemoteFile;
    private SimpleDateFormat mSimpleDateFormat;
    private Context mContext;
    private ArrayList<FileDomain> mDeleteList;
    private ArrayList<MinuteFile> mMinuteFiles = new ArrayList<>();
    private ArrayList<FileDomain> mCameraFiles;
    private static final int DELETE_RECORD_FILE = 3;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DELETE_RECORD_FILE:
                    if (mDeleteList != null && mDeleteList.size() > 0) {
                        deleteRecordFile(mDeleteList);
                    } else {
                        mView.hideLoading();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void sortFile(ArrayList<FileDomain> cameraFiles) {
        mCameraFiles = cameraFiles;
        Collections.sort(cameraFiles, new Comparator<FileDomain>() {
            @Override
            public int compare(FileDomain o1, FileDomain o2) {
                return o2.name.compareTo(o1.name);
            }
        });
        if (mSimpleDateFormat == null) {
            mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
        mMinuteFiles.clear();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = getAbsTime(fileDomain.getTime());
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

    private long getAbsTime(String user_time) {
        long re_time = 0;
        try {
//            2017-10-14 18:20:59
            if (mSdfRemoteFile == null) {
                mSdfRemoteFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            Date d;
            d = mSdfRemoteFile.parse(user_time);
            re_time = d.getTime();
        } catch (Exception ignored) {
        }
        return re_time;
    }

    @Override
    public void deleteFile() {
        if (MinuteFileDownloadManager.isDownloading) {
            ToastUtil.showShortToast(mContext, mContext.getString(R.string.please_stop_download));
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.notice)
                .setMessage(R.string.navi_confDel)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mView.showLoading(VLCApplication.getAppContext().getString(R.string.deleting));
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mDeleteList = new ArrayList<>();
                                for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                                    for (FileDomain fileDomain : minuteFile.fileDomains) {
                                        mDeleteList.add(fileDomain);
                                    }
                                }
                                VoiceManager.selectedMinuteFile.clear();
                                sendMessage(DELETE_RECORD_FILE);
                            }
                        }).start();

                    }
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteRecordFile(@NonNull final ArrayList<FileDomain> deleteList) {
        if (deleteList.size() > 0) {
            CameraCommand.asynSendRequest(CameraCommand.commandSetdeletesinglefileUrl(deleteList.get(deleteList.size() - 1).fpath), new CameraCommand.RequestListener() {
                @Override
                public void onResponse(String result) {
                    if (result != null && (!result.equals("709\n???\n")) && (!result.contains("723"))) {
                        mCameraFiles.remove(deleteList.get(deleteList.size() - 1));
                        deleteList.remove(deleteList.size() - 1);
                        deleteRecordFile(deleteList);
                    } else {
                        //删除失败了直接删除下一个
                        deleteList.remove(deleteList.size() - 1);
                        deleteRecordFile(deleteList);
                    }
                }

                @Override
                public void onErrorResponse(String message) {
                    //删除失败了直接删除下一个
                    deleteList.remove(deleteList.size() - 1);
                    deleteRecordFile(deleteList);
                }
            });
        } else {
            mView.hideLoading();
            sortFile(mCameraFiles);
        }
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    @Override
    public void attachView(MstarFileContract.View view) {
        super.attachView(view);
        mContext = mView.getAttachedContext();
    }
}
