package com.adai.camera.sunplus.filemanager;

import android.content.Context;

import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.icatch.wificam.customer.type.ICatchFile;
import com.ivew.IBaseView;
import com.presenter.BasePresenterImpl;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/9/19 10:16.
 */

public interface SunplusFileFragmentContract {
    interface View extends IBaseView {

        void sortFileEnd(ArrayList<SunplusMinuteFile> minuteFiles);


        void hideLoading();

        Context getAttachedContext();

        void setEditMode(boolean editMode);

        void empty();

    }

    abstract class Presenter extends BasePresenterImpl<View> {

        public abstract void sortFile(ArrayList<ICatchFile> cameraFiles);

        public abstract void download();

        public abstract void deleteFile();
    }
}
