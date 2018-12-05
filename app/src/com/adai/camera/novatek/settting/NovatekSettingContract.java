package com.adai.camera.novatek.settting;

import android.content.Context;
import android.util.SparseArray;

import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

/**
 * Created by huangxy on 2017/8/5 15:27.
 */

public interface NovatekSettingContract {
    interface View extends IBaseView {
        Context getAttachedContext();

        void showLoading(String string);

        void hideLoading();

        void settingsInited();

        void getStatusSuccess();
    }

    abstract class Presenter extends BasePresenterImpl<View> {
        interface CmdCallback {
            void success(int commandId, String par);

            void failed(int commandId, String par, String error);
        }

        public abstract void setOnCmdCallback(CmdCallback cmdListener);

        public abstract void init();

        public abstract void getStatus();

        public abstract boolean cmdIsSupported(int commandId);

        public abstract String getState(int commandId);

        public abstract String getStateId(int commandId);

        public abstract SparseArray<String> getMenuItem(int commandId);

        public abstract void sendCmd(int commandId, String par);
    }
}
