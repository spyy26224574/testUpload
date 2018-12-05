package com.adai.camera.novatek.filemanager.remote;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;

import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2017/8/9 16:40.
 */

public class NovatekVideoFilePresenter extends NovatekVideoFileContract.Presenter {
    protected static final int START = 0;
    private static final int EMPTY = 2;
    private static final int GET_FILELIST = 12;
    private static final int IS_DOWNLOADING = 14;
    private static final int ERROR_GET_RECORD_STATE = 6;
    private static final int ERROR_STOP_RECORD = 7;
    private static final int ERROR_GET_FILELIST = 8;
    private static final int ERROR_GET_SDCARD = 9;
    private List<FileDomain> mCameraFiles = new ArrayList<>();
    private Context mContext;
    private Activity mActivity;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case START:
//                    CameraUtils.changeMode(CameraUtils.MODE_PLAYBACK, new CameraUtils.ModeChangeListener() {
//                        @Override
//                        public void success() {
                    sendEmptyMessageDelayed(GET_FILELIST, 300);
//                        }
//
//                        @Override
//                        public void failure(Throwable throwable) {
//                            showErrorDialog(ERROR_GET_FILELIST);
//                        }
//                    });
                    break;
                case GET_FILELIST:
                    getFileList();
                    break;
                case EMPTY:
                    mView.hideLoading();
                    ToastUtil.showShortToast(mContext, VLCApplication.getAppContext().getString(R.string.no_file));
                    break;
                case IS_DOWNLOADING:
                    ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.please_stop_download));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void initFile() {
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.getting_filelist));

        sendMessage(START);

//        CameraUtils.getRecordStatus(new CameraUtils.RecordStatusListener() {
//            @Override
//            public void success(boolean isRecording) {
//                if (isRecording) {
//                    toggleRecordStatus(false);
//                } else {
//                }
//            }
//
//            @Override
//            public void error(String error) {
//                showErrorDialog(ERROR_GET_RECORD_STATE);
//                mView.hideLoading();
//            }
//        });
    }

    private void getFileList() {
        CameraUtils.getFileList(new CameraUtils.GetFileListListener() {
            @Override
            public void success(List<FileDomain> fileDomains) {
                mCameraFiles.clear();
                for (FileDomain fileDomain : fileDomains) {
                    if (!fileDomain.isPicture) {
                        mCameraFiles.add(fileDomain);
                    }
                }
                if (mCameraFiles == null || mCameraFiles.size() == 0) {
                    sendMessage(EMPTY);
                } else {
                    mView.hideLoading();
                    mView.respGetFileList(mCameraFiles);
                }
            }

            @Override
            public void error(String error) {
                mView.hideLoading();
                showErrorDialog(ERROR_GET_FILELIST);
            }
        });
    }

    private void showFileSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
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


//    private void toggleRecordStatus(boolean startRecord) {
//        CameraUtils.toggleRecordStatus(startRecord, new CameraUtils.ToggleStatusListener() {
//            @Override
//            public void success() {
//                sendMessage(START);
//            }
//
//            @Override
//            public void error(String error) {
//                showErrorDialog(ERROR_STOP_RECORD);
//            }
//        });
//    }

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
            default:
                break;
        }

        new AlertDialog.Builder(mActivity).setTitle(R.string.notice).setMessage(message).setPositiveButton(mActivity.getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (currentErrorCode) {
                    case ERROR_GET_RECORD_STATE:
//                        getCameraAlbum();
                        break;
                    case ERROR_STOP_RECORD:
//                        toggleRecordStatus(false);
                        break;
                    case ERROR_GET_FILELIST:
                        getFileList();
                        break;
                    case ERROR_GET_SDCARD:
//                        toggleRecordStatus(false);
                        break;
                    default:
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

//    private void getCameraAlbum() {
//        CameraUtils.getRecordStatus(new CameraUtils.RecordStatusListener() {
//            @Override
//            public void success(boolean isRecording) {
//                if (isRecording) {
//                    toggleRecordStatus(false);
//                } else {
//                    sendMessage(START);
//                }
//            }
//
//            @Override
//            public void error(String error) {
//                mView.hideLoading();
//                showErrorDialog(ERROR_GET_RECORD_STATE);
//            }
//        });
//    }

    @Override
    public void attachView(NovatekVideoFileContract.View view) {
        super.attachView(view);
        mContext = mView.getAttachedContext();
        mActivity = mView.getAttachedActivity();
    }

    @Override
    public void detachView() {
        if (isDetached) return;
        VLCApplication.getInstance().setAllowDownloads(false);
        MinuteFileDownloadManager.getInstance().cancle();
        MinuteFileDownloadManager.getInstance().removeAllObserver();
        super.detachView();
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }
}
