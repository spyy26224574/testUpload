package com.adai.camera.novatek.preview;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;

import com.adai.camera.novatek.util.CameraUtils;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

/**
 * Created by huangxy on 2017/8/4 17:07.
 */

public interface NovatekPreviewContract {
    interface View extends IBaseView {
        void changeOrientation(boolean isPortrait);

        Context getAttachedContext();

        void showLoading(String string);

        void hideLoading();

        void exit();

        void showAlertDialog();

        void startPreview(boolean isHttp);

        void respChangePip(@DrawableRes int drawableRes);

        void showPip(int which);//-1隐藏，0竖屏，1横屏

        void stopPreview();

        void startPreview();

        void initPlayView(boolean isHttp);

        void startTakePhoto();

        void takePhotoEnd();

        void currentMode(int mode);

        void stopRecordTime();

        void startRecordTime(int time);

        void pictureVisible(boolean visible);

        void showRecordState(boolean isRecord);

        void currentProduct(CameraUtils.PRODUCT product);

        void audioChange(boolean isOpen);

        void isFishMode(boolean fishMode);
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        abstract public void init();

        public abstract void initOrientation();

        public abstract void connectSocket();

        public abstract void onConfigurationChanged(Configuration newConfig);

        public abstract void onPause();

        public abstract void onRestart();

        public abstract void onStop();

        public abstract void startConnectThread();

        public abstract void onBufferChanged(float buffer);

        public abstract void onLoadComplete();

        public abstract void onError();

        public abstract void onPlayError();

        public abstract void onEnd();

        public abstract void changeMode(int mode);

        public abstract void takePhoto();

        public abstract void onStartPlay();

        public abstract void changePip();

        public abstract String getState(int commandId);

        public abstract String getStateId(int commandId);

        public abstract void firstConnectSocket();

        public abstract void setResolution(int cmdId, int key);

        public abstract void recordShot();

        public abstract void onResume();

        public abstract String getCurrentPip();
    }
}
