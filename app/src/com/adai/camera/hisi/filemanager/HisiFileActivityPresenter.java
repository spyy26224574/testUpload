package com.adai.camera.hisi.filemanager;

import android.annotation.SuppressLint;
import android.os.AsyncTask;

import com.adai.camera.CameraFactory;
import com.adai.camera.FileManagerConstant;
import com.adai.camera.hisi.HisiCamera;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.ToastUtil;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.adai.camera.hisi.sdk.Common.FAILURE;

/**
 * @author huangxy
 * @date 2017/12/1 17:55.
 */

public class HisiFileActivityPresenter extends HisiFileActivityContract.Presenter {
    private List<FileDomain> mCameraFiles = new ArrayList<>();
    private int mType;
    @Override
    public void initFile(int type) {
        mType = type;
        GetFileTask getFileTask = new GetFileTask();
        getFileTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetFileTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.showLoading(VLCApplication.getAppContext().getString(R.string.getting_filelist));
        }

        @Override
        protected Void doInBackground(Void... voids) {
            HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
            int fileCount = dv.getFileCount();
            if (fileCount > 0) {
                List<String> fileNameList = new ArrayList<>();
                int nameCount = dv.getFileList(0, fileCount, fileNameList);
                if (FAILURE != nameCount) {
                    mCameraFiles.clear();
                    for (String fileName : fileNameList) {
                        Map<String, String> mapInfo = new HashMap<>();
                        int fileInfo = dv.getFileInfo(fileName, mapInfo);
                        if (FAILURE != fileInfo) {
                            FileDomain fileDomain = new FileDomain();
                            try {
                                fileDomain.size = Long.valueOf(mapInfo.get("size"));
                                fileDomain.fpath = fileName;
                                fileDomain.setSmallpath(fileName);
                                fileDomain.name = mapInfo.get("path");
                                fileDomain.isPicture = FileTypeUtil.getFileType(fileDomain.name) == FileTypeUtil.TYPE_IMG;
                                SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
                                format.applyPattern("yyyyMMddHHmmss");
                                Date create = format.parse(mapInfo.get("create"));
                                fileDomain.timeCode = create.getTime();
                                fileDomain.baseUrl = "http://" + dv.getIP() + "/";
                                fileDomain.setThumbnailUrl(fileDomain.baseUrl + fileName.substring(0, fileName.lastIndexOf(".") + 1) + "THM");
                                fileDomain.setDownloadPath(fileDomain.baseUrl + fileName);
                                if (mType == FileManagerConstant.TYPE_REMOTE_VIDEO) {
                                    if (!fileDomain.isPicture) {
                                        mCameraFiles.add(fileDomain);
                                    }
                                } else {
                                    if (fileDomain.isPicture) {
                                        mCameraFiles.add(fileDomain);
                                    }
                                }
                            } catch (NumberFormatException ignored) {

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mCameraFiles == null || mCameraFiles.size() == 0) {
                mView.hideLoading();
                ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.no_file));
            } else {
                mView.hideLoading();
                mView.respGetFileList(mCameraFiles);
            }
        }
    }
}
