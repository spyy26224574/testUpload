package com.adai.camera.hisi.filemanager;

import android.content.Context;

import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.ArrayList;

/**
 * @author huangxy
 * @date 2018/3/3 18:13.
 */

public interface HisiFileFragmentContract {
    interface View extends IBaseView {

        void sortFileEnd(ArrayList<MinuteFile> minuteFiles);

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
