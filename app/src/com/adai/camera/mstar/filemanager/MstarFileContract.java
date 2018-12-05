package com.adai.camera.mstar.filemanager;

import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/10/16 16:26.
 */

public interface MstarFileContract {
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

        public abstract void deleteFile();

    }
}
