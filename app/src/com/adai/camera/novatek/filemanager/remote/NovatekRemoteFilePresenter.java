package com.adai.camera.novatek.filemanager.remote;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

/**
 * Created by huangxy on 2017/8/9 17:21.
 */

public class NovatekRemoteFilePresenter extends NovatekRemoteFileContract.Presenter {
    SimpleDateFormat mSimpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();

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
                    mView.hideLoading();
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
                if (o2.timeCode > o1.timeCode) {
                    return 1;
                } else if (o2.timeCode < o1.timeCode) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        mSimpleDateFormat.applyPattern("yyyy-MM-dd HH:mm:ss");
        mMinuteFiles.clear();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = getAbsTime(CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ ? fileDomain.upTime : fileDomain.time);
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
            SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
            dateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
            Date d;
            d = dateFormat.parse(user_time);
            re_time = d.getTime();
        } catch (Exception ignored) {
        }
        return re_time;
    }

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
            if (deleteList.get(deleteList.size() - 1).attr == 33) {
                deleteList.remove(deleteList.size() - 1);
                deleteRecordFile(deleteList);
            } else {
                CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + deleteList.get(deleteList.size() - 1).fpath, new CameraUtils.CmdListener() {
                    @Override
                    public void onResponse(String response) {
                        InputStream is;
                        try {
                            is = new ByteArrayInputStream(response.getBytes("utf-8"));
                            DomParseUtils domParseUtils = new DomParseUtils();
                            MovieRecord record = domParseUtils.getParserXml(is);
                            if (record != null && record.getStatus().equals("0")) {
                                if (deleteList.get(deleteList.size() - 1).getSmallpath() != null) {
                                    //删除小档
                                    CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + deleteList.get(deleteList.size() - 1).getSmallpath(), new CameraUtils.CmdListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            //小档也删除了，删除下一个
                                            mCameraFiles.remove(deleteList.get(deleteList.size() - 1));
                                            deleteList.remove(deleteList.size() - 1);
                                            deleteRecordFile(deleteList);
                                        }

                                        @Override
                                        public void onErrorResponse(Exception volleyError) {
                                            mView.hideLoading();

                                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.deleted_failure));
                                        }
                                    });
                                } else {
                                    //没有小档直接删除下一个t
                                    mCameraFiles.remove(deleteList.get(deleteList.size() - 1));
                                    deleteList.remove(deleteList.size() - 1);
                                    deleteRecordFile(deleteList);
                                }
                            } else {
                                //删除失败了直接删除下一个
                                deleteList.remove(deleteList.size() - 1);
                                deleteRecordFile(deleteList);
                            }
                        } catch (UnsupportedEncodingException e) {
                            //删除失败了直接删除下一个
                            deleteList.remove(deleteList.size() - 1);
                            deleteRecordFile(deleteList);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorResponse(Exception volleyError) {
                        mView.hideLoading();
                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.deleted_failure));
                    }
                });
            }

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
    public void attachView(NovatekRemoteFileContract.View view) {
        super.attachView(view);
        mContext = mView.getAttachedContext();
    }
}
