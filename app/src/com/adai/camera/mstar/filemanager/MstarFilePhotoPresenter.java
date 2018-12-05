package com.adai.camera.mstar.filemanager;

import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.UIUtils;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2017/10/23 11:00.
 */

public class MstarFilePhotoPresenter extends MstarFilePhotoContract.Presenter {
    private boolean isPlackbackMode = false;
    private String allDB[] = {"DCIM", "Normal", "Photo", "Event", "Parking"};
    private int pageCount = 32;
    private List<FileDomain> mPhotoFiles = new ArrayList<>();
    private int mFrom;
    private int filelistid = 0;

    public enum Format {
        mov, avi, mp4, jpeg, all
    }

    @Override
    public void onResume() {
        mView.showLoading();
        UIUtils.postDelayed(new Runnable() {
            @Override
            public void run() {
                CameraCommand.asynSendRequest(CameraCommand.commandEnterPlayback(), mEnterPlaybackListener);
            }
        }, 500);
    }

    @Override
    public void onPause() {
        mView.hideLoading();
        CameraCommand.asynSendRequest(CameraCommand.commandExitPlayback(), mExitPlaybackListener);
    }

    private CameraCommand.RequestListener mEnterPlaybackListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {
            if (!isPlackbackMode) {
                getFileList();
            } else {
                mView.hideLoading();
            }
            isPlackbackMode = true;
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.please_connect_camera);
        }
    };

    @Override
    public void getFileList() {
        mPhotoFiles.clear();
        filelistid = 0;
        mFrom = 0;
        getFile(allDB[2], mFrom, mGetFileListener);
    }

    private CameraCommand.RequestListener mGetFileListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {
            InputStream is = null;
            try {
                is = new ByteArrayInputStream(response.getBytes("utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            DomParseUtils domParseUtils = new DomParseUtils();
            try {
                List<FileDomain> fileDomains = domParseUtils.parseMstarFileXml(is);
                if (fileDomains != null) {
                    mPhotoFiles.addAll(fileDomains);
                    if (fileDomains.size() != pageCount) {
                        if (filelistid == 0) {
                            filelistid = 1;
                            mFrom = 0;
                            getFile(allDB[2], mFrom, mGetFileListener);
                        } else {
                            mView.hideLoading();
                            mView.getFileSuccess();
                        }
                    } else {
                        mFrom += fileDomains.size();
                        getFile(allDB[2], mFrom, mGetFileListener);
                    }
                } else {
                    if (filelistid == 0) {
                        filelistid = 1;
                        mFrom = 0;
                        getFile(allDB[2], mFrom, mGetFileListener);
                    } else {
                        mView.hideLoading();
                        mView.getFileSuccess();
                    }
                }
            } catch (Exception e) {
                mView.hideLoading();
                mView.getFileSuccess();
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
        }
    };

    @Override
    public List<FileDomain> getPhotoFiles() {
        return mPhotoFiles;
    }

    private void getFile(String directory, int from, CameraCommand.RequestListener requestListener) {
        String query = buildQuery(filelistid, directory, Format.all, pageCount, from);
        URL url = null;
        try {
            if (MstarCamera.CAM_IP == null) {
                MstarCamera.CAM_IP = CameraCommand.getCameraIp();
            }
            url = new URL("http://" + MstarCamera.CAM_IP + "/cgi-bin/Config.cgi?" + query);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        CameraCommand.asynSendRequest(url, requestListener);
    }

    private CameraCommand.RequestListener mExitPlaybackListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {

        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
        }
    };

    /**
     * @param filelistid 0-前摄像头，1-后摄像头
     * @param directory  文件夹
     * @param aFormat    类型mov, avi, mp4, jpeg, all
     * @param aCount     请求文件个数
     * @param aFrom      从aFrom开始
     */
    private static String buildQuery(Integer filelistid, String directory, Format aFormat, int aCount, int aFrom) {
        String action;// = "action=" + Action.reardir ;
        String property = "property=" + directory;
        String format = "format=" + aFormat.name();
        String count = "count=" + aCount;
        String from = "from=" + aFrom;
        if (filelistid == 0)
            action = "action=dir";
        else
            action = "action=reardir";
        return action + "&" + property + "&" + format + "&" + count + "&" + from;
    }

    @Override
    public void detachView() {
        if (isDetached) return;
        VLCApplication.getInstance().setAllowDownloads(false);
        MinuteFileDownloadManager.getInstance().cancle();
        MinuteFileDownloadManager.getInstance().removeAllObserver();
        super.detachView();
    }
}
