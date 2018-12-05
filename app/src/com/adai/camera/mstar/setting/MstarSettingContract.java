package com.adai.camera.mstar.setting;

import android.content.Context;

import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

/**
 * Created by huangxy on 2017/10/14 11:06.
 */

public interface MstarSettingContract {
    interface View extends IBaseView {
        Context getAttachedContext();

        @Override
        void showLoading(String string);

        void hideLoading();

        void settingsInited();

        void getStatusSuccess();
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void init();

        public abstract void getStatus();

    }
}
