package com.adai.camera.novatek.filemanager;

import android.os.Bundle;

import com.adai.camera.FileManagerConstant;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.example.ipcamera.application.VLCApplication;


public class NovatekDownloadPictureActivity extends BaseActivity {
    private NovatekFileManagerFragment mNovatekFileManagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_download_picture);
        initView();

    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.photo);
        mNovatekFileManagerFragment = NovatekFileManagerFragment.newInstance(3, FileManagerConstant.TYPE_LOCAL_PICTURE, VLCApplication.DOWNLOADPATH);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mNovatekFileManagerFragment).commit();
    }
}
