package com.adai.camera.novatek.filemanager.remote;

import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/8/9 17:16.
 */

public interface NovatekRemoteFileContract {
    interface View extends IBaseView {

        void sortFileEnd(ArrayList<MinuteFile> minuteFiles);


        void showLoading(String string);

        void hideLoading();

        void empty();

        Context getAttachedContext();

        void setEditMode(boolean editMode);
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void sortFile(ArrayList<FileDomain> cameraFiles);

        public abstract void download();
    }
}
