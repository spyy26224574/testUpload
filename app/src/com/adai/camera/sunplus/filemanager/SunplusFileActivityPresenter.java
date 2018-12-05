package com.adai.camera.sunplus.filemanager;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.tool.SunplusMinuteFileDownloadManager;
import com.adai.gkdnavi.R;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangxy on 2017/9/19 10:05.
 */

public class SunplusFileActivityPresenter extends SunplusFileActivityContract.Presenter {
    private int mType;
    private ExecutorService executor;
    private ArrayList<SunplusMinuteFile> mSunplusMinuteFiles = new ArrayList<>();
    private FileOperation mFileOperation = FileOperation.getInstance();
    private static final int GET_FILE_COMPLETE = 1;
    private List<ICatchFile> mCameraFiles = new ArrayList<>();
    private Future<?> mGetFileFuture;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_FILE_COMPLETE:
                    mView.hideLoading();
                    mView.respGetFileList(mCameraFiles);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void attachView(SunplusFileActivityContract.View view) {
        super.attachView(view);
        executor = Executors.newSingleThreadExecutor();

    }

    @Override
    public void initFile(int type) {
        mType = type;
        mView.showLoading(R.string.getting_filelist);
        mSunplusMinuteFiles.clear();
        mGetFileFuture = executor.submit(new GetFileListThread());
    }

    private class GetFileListThread implements Runnable {

        @Override
        public void run() {
            mCameraFiles.clear();
            ICatchFileType iCatchFileType = ICatchFileType.ICH_TYPE_ALL;
            switch (mType) {
                case FileManagerConstant.TYPE_REMOTE_PHOTO:
                    iCatchFileType = ICatchFileType.ICH_TYPE_IMAGE;
                    break;
                case FileManagerConstant.TYPE_REMOTE_VIDEO:
                    iCatchFileType = ICatchFileType.ICH_TYPE_VIDEO;
                    break;
                default:
                    break;
            }
            mCameraFiles.addAll(mFileOperation.getFileList(iCatchFileType));
            sendMessage(GET_FILE_COMPLETE);
        }
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }


    @Override
    public void detachView() {
        super.detachView();
        SunplusMinuteFileDownloadManager.getInstance().removeAllObserver();
        FileOperation.getInstance().cancelDownload();
        SunplusMinuteFileDownloadManager.getInstance().stop();
        if (mGetFileFuture != null && !mGetFileFuture.isDone()) {
            mGetFileFuture.cancel(true);
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException e) {
            // (Re-)cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onResume() {

    }
}
