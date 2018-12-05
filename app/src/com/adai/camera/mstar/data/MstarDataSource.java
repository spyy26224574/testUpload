package com.adai.camera.mstar.data;

import android.support.annotation.NonNull;

import com.example.ipcamera.domain.FileDomain;

import java.util.List;

/**
 * Created by huangxy on 2017/10/14 11:10.
 */

public interface MstarDataSource {
    interface DataSourceSimpleCallBack {
        void success();

        void error(String error);
    }

    interface GetFileListCallback {
        void success(List<FileDomain> fileList);

        void failed(String error);
    }

    void initDataSource(@NonNull DataSourceSimpleCallBack dataSourceSimpleCallBack);

    void getStatus(@NonNull DataSourceSimpleCallBack dataSourceSimpleCallBack);

    void getFileList(@NonNull GetFileListCallback getFileListCallback);
}
