package com.adai.camera.hisi.filemanager;

import android.app.Activity;
import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.List;

/**
 * @author huangxy
 * @date 2017/12/1 17:47.
 */

public interface HisiFileActivityContract {
    interface View extends IBaseView {
        void showLoading();

        Context getAttachedContext();

        Activity getAttachedActivity();

        void deleteResult(boolean success);

        void respGetFileList(List<FileDomain> cameraFiles);

    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void initFile(int typeRemotePhoto);
    }
}
