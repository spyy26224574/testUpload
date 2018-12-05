package com.adai.camera.sunplus.filemanager;

import android.app.Activity;
import android.content.Context;

import com.icatch.wificam.customer.type.ICatchFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.List;

/**
 * Created by huangxy on 2017/9/19 10:03.
 */

public interface SunplusFileActivityContract {
    interface View extends IBaseView {
        void showLoading();

        void hideLoading();

        Context getAttachedContext();

        Activity getAttachedActivity();

        void deleteResult(boolean success);

        void respGetFileList(List<ICatchFile> cameraFiles);

    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void initFile(int type);

        public abstract void onResume();
    }
}
