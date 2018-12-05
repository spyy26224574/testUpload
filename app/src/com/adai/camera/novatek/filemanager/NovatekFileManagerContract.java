package com.adai.camera.novatek.filemanager;

import android.app.Activity;
import android.content.Context;

import com.example.ipcamera.domain.MinuteFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/8/8 10:14.
 */

public interface NovatekFileManagerContract {
    interface View extends IBaseView {
        void showLoading();

        Context getAttachedContext();

        void sortFileEnd(ArrayList<MinuteFile> minuteFiles);

        Activity getAttachedActivity();

        void deleteResult(boolean success);

        void empty();

        void setEditMode(boolean isEditMode);
    }

    abstract class Presenter extends BasePresenterImpl<View> {

        abstract void deleteFile();

        abstract void initFile(int type, String filePath);

        public abstract void download();
    }
}
