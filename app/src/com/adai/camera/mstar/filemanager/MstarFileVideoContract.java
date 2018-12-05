package com.adai.camera.mstar.filemanager;

import android.app.Activity;
import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.List;

/**
 * Created by huangxy on 2017/10/16 11:48.
 */

public interface MstarFileVideoContract {
    interface View extends IBaseView {
        void showLoading();

        void showLoading(String string);

        void hideLoading();

        Context getAttachedContext();

        Activity getAttachedActivity();

        void deleteResult(boolean success);

        void getFileSuccess();
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void onResume();

        public abstract void onPause();

        public abstract void getFileList();

        public abstract List<FileDomain> getEventFiles();

        public abstract List<FileDomain> getNormalFiles();
    }
}
