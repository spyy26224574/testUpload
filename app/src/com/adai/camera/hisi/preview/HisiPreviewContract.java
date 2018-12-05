package com.adai.camera.hisi.preview;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;

import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

/**
 * @author huangxy
 * @date 2017/11/20 14:40.
 */

public interface HisiPreviewContract {
    interface View extends IBaseView {
        void changeOrientation(boolean isPortrait);

        Context getAttachedContext();

        void showLoading();

        void hideLoading();

        void exit();

        void showAlertDialog();

        void respChangePip(@DrawableRes int drawableRes);

        void showPip(int which);//-1隐藏，0竖屏，1横屏

        void stopPreview();

        void startPreview();

        void initPlayView();

        void startTakePhoto();

        void takePhotoEnd();

        void pictureVisible(boolean visible);

        void showRecordState(boolean isRecord);

        void eventEnable(boolean enable);

        void modeChange(boolean isPhotoMode);

        void updateInfoBar();

        void startSecondTimer(int time);

        void stopSecondTimer();
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        abstract public void init();

        public abstract void initOrientation();

        public abstract void connectSocket();

        public abstract void onConfigurationChanged(Configuration newConfig);

        public abstract void onStart();

        public abstract void onPause();

        public abstract void onRestart();

        public abstract void onStop();

        public abstract void onBufferChanged(float buffer);

        public abstract void onLoadComplete();

        public abstract void onError();

        public abstract void onPlayError();

        public abstract void onEnd();

        public abstract void changeMode(int mode);

        public abstract void takePhoto();

        public abstract void onStartPlay();


        public abstract void onResume();

        public abstract void toggleMode();

        public abstract void recordShot();

        public abstract void setResolution(int mode, String value);
    }
}
