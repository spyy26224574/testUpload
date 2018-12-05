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

public interface HisiFileVideoContract {
    interface View extends IBaseView {
        void showLoading();

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
