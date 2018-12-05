package com.adai.camera.mstar.preview;

import android.content.Context;
import android.content.res.Configuration;
import android.support.annotation.DrawableRes;

import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

/**
 * Created by huangxy on 2017/10/11 21:50.
 */

public interface MstarPreviewContract {
    interface View extends IBaseView {
        void changeOrientation(boolean isPortrait);

        Context getAttachedContext();

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

        void currentMode(int mode);

        void pictureVisible(boolean visible);

        void showRecordState(boolean isRecord);
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        abstract public void init();

        public abstract void initOrientation();

        public abstract void onConfigurationChanged(Configuration newConfig);

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

        public abstract void switchPip();

        public abstract void recordShot();
    }
}
