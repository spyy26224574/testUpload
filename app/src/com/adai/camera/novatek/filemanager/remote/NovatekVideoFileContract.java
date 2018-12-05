package com.adai.camera.novatek.filemanager.remote;

import android.app.Activity;
import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.List;

/**
 * Created by huangxy on 2017/8/9 16:26.
 */

public interface NovatekVideoFileContract {
    interface View extends IBaseView {
        void showLoading();

        Context getAttachedContext();

        Activity getAttachedActivity();

        void deleteResult(boolean success);

        void respGetFileList(List<FileDomain> cameraFiles);
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void initFile();
    }
}
