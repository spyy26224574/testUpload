package com.adai.camera.novatek.filemanager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by huangxy on 2017/8/8 11:00.
 */

public class NovatekFileManagerPresenter extends NovatekFileManagerContract.Presenter {
    private static SimpleDateFormat mSdfRemoteFile;
    private Context mContext;
    private Activity mActivity;
    protected static final int START = 0;
    private static final int SORT_FILE_END = 1;
    private static final int EMPTY = 2;
    private static final int DELETE_RECORD_FILE = 3;
    private static final int GET_FILELIST = 12;
    private static final int IS_DOWNLOADING = 14;
    private static final int ERROR_GET_RECORD_STATE = 6;
    private static final int ERROR_STOP_RECORD = 7;
    private static final int ERROR_GET_FILELIST = 8;
    private static final int ERROR_GET_SDCARD = 9;
    private ArrayList<MinuteFile> mMinuteFiles = new ArrayList<>();
    private String mFilePath = "";
    private List<FileDomain> mCameraFiles = new ArrayList<>();
    private int mType;
    private ArrayList<FileDomain> mDeleteList;
    private int get_filelist_time = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START:
                    CameraUtils.changeMode(CameraUtils.MODE_PLAYBACK, new CameraUtils.ModeChangeListener() {
                        @Override
                        public void success() {
                            CameraUtils.getSDCardStatus(new CameraUtils.SDCardStatusListener() {
                                @Override
                                public void success(int status) {
                                    if (status > 0) {
                                        sendEmptyMessageDelayed(GET_FILELIST, 200);
                                    } else {
                                        showErrorDialog(ERROR_GET_SDCARD);
                                    }
                                }

                                @Override
                                public void error(String error) {
                                    showErrorDialog(ERROR_GET_SDCARD);
                                }
                            });
                        }

                        @Override
                        public void failure(Throwable throwable) {
                            showErrorDialog(ERROR_GET_FILELIST);
                        }
                    });
                    break;
                case GET_FILELIST:
                    getFileList();
                    break;
                case SORT_FILE_END:
                    mView.hideLoading();
                    mView.sortFileEnd(mMinuteFiles);
                    break;
                case EMPTY:
                    mView.hideLoading();
                    mView.empty();
//                    ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.no_file));
                    break;
                case DELETE_RECORD_FILE:
                    mView.hideLoading();
                    if (mDeleteList != null && mDeleteList.size() > 0) {
                        deleteRecordFile(mDeleteList);
                    } else {
                        mView.hideLoading();
                    }
                    break;
                case IS_DOWNLOADING:
                    ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.please_stop_download));
                    break;
            }
        }
    };

    @Override
    public void attachView(NovatekFileManagerContract.View view) {
        super.attachView(view);
        mContext = mView.getAttachedContext();
        mActivity = mView.getAttachedActivity();
    }

    @Override
    void deleteFile() {
        if (mType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO || mType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO) {
            if (MinuteFileDownloadManager.isDownloading) {
                sendMessage(IS_DOWNLOADING);
                return;
            }
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
                                if (mType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO || mType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO) {
                                    mDeleteList = new ArrayList<>();
                                    for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                                        for (FileDomain fileDomain : minuteFile.fileDomains) {
                                            mDeleteList.add(fileDomain);
                                        }
                                    }
                                    VoiceManager.selectedMinuteFile.clear();
                                    sendMessage(DELETE_RECORD_FILE);
                                } else {
                                    ContentResolver contentResolver = mView.getAttachedContext().getContentResolver();
                                    String toastMessage = VLCApplication.getAppContext().getString(R.string.deleted_success);
                                    loop1:
                                    for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                                        for (FileDomain fileDomain : minuteFile.fileDomains) {
                                            File file = new File(fileDomain.fpath);
                                            if (file.isFile() && file.exists()) {
                                                if (file.delete()) {
                                                    String where = (fileDomain.isPicture ? MediaStore.Images.Media.DATA :
                                                            MediaStore.Video.Media.DATA) + "='" + fileDomain.fpath + "'";
                                                    contentResolver.delete(fileDomain.isPicture ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                                            : MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
                                                    mCameraFiles.remove(fileDomain);
                                                } else {
                                                    toastMessage = VLCApplication.getAppContext().getString(R.string.deleted_failure);
                                                    break loop1;
                                                }
                                            }
                                        }
                                    }
                                    final String finalToastMessage = toastMessage;
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ToastUtil.showShortToast(VLCApplication.getAppContext(), finalToastMessage);
                                        }
                                    });
                                    VoiceManager.selectedMinuteFile.clear();
                                    sortLocalFiles(mCameraFiles);
                                    sendMessage(SORT_FILE_END);
                                }
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mView.deleteResult(true);
                                    }
                                });
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
            handlerRecorderData(mCameraFiles);
            mView.deleteResult(true);
        }
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<MinuteFile> handlerRecorderData(@NonNull List<FileDomain> cameraFiles) {
        for (FileDomain fileDomain : cameraFiles) {
            String lowerCasePath = fileDomain.getFpath().toLowerCase();
            if (lowerCasePath.endsWith(".mov") || lowerCasePath.endsWith(".mp4") || lowerCasePath.endsWith(".avi") || lowerCasePath.endsWith(".TS")) {
                fileDomain.isPicture = false;
            }
        }
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        ArrayList<MinuteFile> minuteFiles = new ArrayList<>();
        mMinuteFiles.clear();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = getAbsTime(CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ ? fileDomain.upTime : fileDomain.time);
            String time = simpleDateFormat.format(new Date(lastModified));
            String minuteTime = time.substring(0, 16);
            String hourTime = time.substring(0, 13);
            if (mMinuteFiles.size() == 0) {
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
                minuteFile.time = time;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                mMinuteFiles.add(minuteFile);
            } else if (minuteTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).minuteTime)) {
                //说明是同一分钟的文件
                minuteFile = new MinuteFile();
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.time = time;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                mMinuteFiles.add(minuteFile);
            } else {
                //不是通一分钟的文件,需要判断是不是同一个小时的文件
//                if (i != cameraFiles.size() - 1) {
                if (hourTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).hourTime)) {
                    //是同一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                } else {
                    //不是一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.isTitle = true;
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    mMinuteFiles.add(minuteFile);
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                }
//                }
            }
        }
        return mMinuteFiles;
    }

    public static long getAbsTime(String user_time) {
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

    @Override
    void initFile(int type, final String filePath) {
        mType = type;
        mFilePath = filePath;
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.getting_filelist));
        switch (type) {
            case FileManagerConstant.TYPE_LOCAL_PICTURE:
                getLocalPicture(filePath);
                break;
            case FileManagerConstant.TYPE_LOCAL_VIDEO:
                getLocalVideo(filePath);
                break;
            case FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO:
            case FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO:
            case FileManagerConstant.TYPE_REMOTE_ALL:
            case FileManagerConstant.TYPE_REMOTE_PHOTO:
                CameraUtils.getRecordStatus(new CameraUtils.RecordStatusListener() {
                    @Override
                    public void success(boolean isRecording) {
                        if (isRecording) {
                            toggleRecordStatus(false);
                        } else {
                            sendMessage(START);
                        }
                    }

                    @Override
                    public void error(String error) {
                        showErrorDialog(ERROR_GET_RECORD_STATE);
                        mView.hideLoading();
                    }
                });
                break;
        }
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

    private void getLocalVideo(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File root = new File(filePath);
                File[] files = root.listFiles();
                mCameraFiles.clear();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String filepath = file.getAbsolutePath();
                        if (FileTypeUtil.getFileType(filepath) == FileTypeUtil.TYPE_VIDEO) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = false;
                            fileDomain.fpath = filepath;
                            fileDomain.setSize(file.length());
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setName(file.getName());
                            mCameraFiles.add(fileDomain);
                        }
                    }
                    if (mCameraFiles.size() == 0) {
                        sendMessage(EMPTY);
                        return;
                    }
                    sortLocalFiles(mCameraFiles);
                    sendMessage(SORT_FILE_END);
                } else {
                    sendMessage(EMPTY);
                }
            }
        }).start();
    }

    private void getLocalPicture(final String filePath) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File root = new File(filePath);
                File[] files = root.listFiles();
                mCameraFiles.clear();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String filepath = file.getAbsolutePath();
                        if (filepath.endsWith("JPG") || filepath.endsWith("jpg") || filepath.endsWith("PNG") || filepath.endsWith("png")|| filepath.endsWith("ts")) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = true;
                            fileDomain.fpath = filepath;
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setSize(file.length());
                            fileDomain.setName(file.getName());
                            mCameraFiles.add(fileDomain);
                        }
                    }
                    if (mCameraFiles.size() == 0) {
                        sendMessage(EMPTY);
                        return;
                    }
                    sortLocalFiles(mCameraFiles);
                    sendMessage(SORT_FILE_END);
                } else {
                    sendMessage(EMPTY);
                }
            }
        }).start();
    }

    private int currentErrorCode = -1;

    private void showErrorDialog(int errorCode) {
        mView.hideLoading();
        currentErrorCode = errorCode;
        String message = "";
        if (mActivity == null || mActivity.isFinishing()) return;
        switch (errorCode) {
            case ERROR_GET_FILELIST:
                message = VLCApplication.getAppContext().getString(R.string.failed_get_filelist);
                break;
            case ERROR_GET_RECORD_STATE:
                message = VLCApplication.getAppContext().getString(R.string.failed_get_recording_status);
                break;
            case ERROR_STOP_RECORD:
                message = VLCApplication.getAppContext().getString(R.string.failed_stop_recording);
                break;
            case ERROR_GET_SDCARD:
                message = VLCApplication.getAppContext().getString(R.string.failed_sdcard);
        }

        new AlertDialog.Builder(mActivity).setTitle(R.string.notice).setMessage(message).setPositiveButton(mActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (currentErrorCode) {
                    case ERROR_GET_RECORD_STATE:
                        getCameraAlbum();
                        break;
                    case ERROR_STOP_RECORD:
                        toggleRecordStatus(false);
                        break;
                    case ERROR_GET_FILELIST:
                        getFileList();
                        break;
                    case ERROR_GET_SDCARD:
                        toggleRecordStatus(false);
                        break;
                }
            }
        }).setNegativeButton(mActivity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mActivity.finish();
            }
        }).create().show();
    }

    /**
     * 获取文件列表，同{@link #initFile(int, String)}的区别是不需要检查摄像头的状态直接获取列表
     */
    public void getFileList() {
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.getting_filelist));
        switch (mType) {
            case FileManagerConstant.TYPE_LOCAL_PICTURE:
                getLocalPicture(mFilePath);
                break;
            case FileManagerConstant.TYPE_LOCAL_VIDEO:
                getLocalVideo(mFilePath);
                break;
            case FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO:
            case FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO:
            case FileManagerConstant.TYPE_REMOTE_ALL:
            case FileManagerConstant.TYPE_REMOTE_PHOTO:
                CameraUtils.getFileList(new CameraUtils.GetFileListListener() {
                    @Override
                    public void success(List<FileDomain> fileDomains) {
                        get_filelist_time = 0;
                        mCameraFiles.clear();
                        for (FileDomain fileDomain : fileDomains) {
                            if (!fileDomain.isPicture) {
                                if (mType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO && fileDomain.attr != 33) {
                                    mCameraFiles.add(fileDomain);
                                }
                                if (mType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO && fileDomain.attr == 33) {
                                    mCameraFiles.add(fileDomain);
                                }
                            }
                        }
                        if (mCameraFiles == null || mCameraFiles.size() == 0) {
                            ToastUtil.showShortToast(mContext, VLCApplication.getAppContext().getString(R.string.no_file));
                            sendMessage(EMPTY);
                        } else if (mCameraFiles.size() >= 600) {
                            showFileSizeDialog();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mMinuteFiles = handlerRecorderData(mCameraFiles);
                                    sendMessage(SORT_FILE_END);
                                }
                            }).start();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mMinuteFiles = handlerRecorderData(mCameraFiles);
                                    sendMessage(SORT_FILE_END);
                                }
                            }).start();

                        }
                    }

                    @Override
                    public void error(String error) {
                        get_filelist_time++;
                        if (get_filelist_time == 3) {
                            get_filelist_time = 0;
                            mView.hideLoading();
                            showErrorDialog(ERROR_GET_FILELIST);
                        } else {
                            sendMessage(GET_FILELIST);
                        }
                    }
                });
                break;
        }
    }

    private void showFileSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(VLCApplication.getAppContext());
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.please_clear_files);
        builder.setPositiveButton(VLCApplication.getAppContext().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        builder.create().show();
    }

    private void getCameraAlbum() {
        CameraUtils.getRecordStatus(new CameraUtils.RecordStatusListener() {
            @Override
            public void success(boolean isRecording) {
                if (isRecording) {
                    toggleRecordStatus(false);
                } else {
                    sendMessage(START);
                }
            }

            @Override
            public void error(String error) {
                mView.hideLoading();
                showErrorDialog(ERROR_GET_RECORD_STATE);
            }
        });
    }

    private void toggleRecordStatus(boolean startRecord) {
        CameraUtils.toggleRecordStatus(startRecord, new CameraUtils.ToggleStatusListener() {
            @Override
            public void success() {
                sendMessage(START);
            }

            @Override
            public void error(String error) {
                showErrorDialog(ERROR_STOP_RECORD);
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<MinuteFile> sortLocalFiles(List<FileDomain> fileList) {
        mMinuteFiles.clear();
        if (fileList != null && fileList.size() > 0) {
            //先对文件列表排序，方便之后的分组
            Collections.sort(fileList, new Comparator<FileDomain>() {
                @Override
                public int compare(FileDomain o1, FileDomain o2) {
                    long l = new File(o1.fpath).lastModified();
                    long l1 = new File(o2.fpath).lastModified();
                    int ret;
                    if (l > l1) {
                        ret = -1;
                    } else if (l < l1) {
                        ret = 1;
                    } else {
                        ret = 0;
                    }
                    return ret;
                }
            });
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            MinuteFile minuteFile = new MinuteFile();
            for (int i = 0; i < fileList.size(); i++) {
                File file = new File(fileList.get(i).fpath);
                long lastModified = file.lastModified();//最后修改的时间戳
                String time = simpleDateFormat.format(new Date(lastModified));
                String minuteTime = time.substring(0, 16);
                String hourTime = time.substring(0, 10);//实际上是按照天来分的（hourTime指的是天）
                if (mMinuteFiles.size() == 0) {
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = minuteTime;
                    minuteFile.isTitle = true;
                    mMinuteFiles.add(minuteFile);
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(fileList.get(i));
                    mMinuteFiles.add(minuteFile);
                } else if (minuteTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).minuteTime)) {
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = minuteTime;
                    minuteFile.fileDomains.add(fileList.get(i));
                    mMinuteFiles.add(minuteFile);
                } else {
                    if (hourTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).hourTime)) {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(fileList.get(i));
                        mMinuteFiles.add(minuteFile);
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = minuteTime;
                        minuteFile.isTitle = true;
                        mMinuteFiles.add(minuteFile);
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = time;
                        minuteFile.fileDomains.add(fileList.get(i));
                        mMinuteFiles.add(minuteFile);
                    }
                }
            }

        }
        return mMinuteFiles;
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }
}
