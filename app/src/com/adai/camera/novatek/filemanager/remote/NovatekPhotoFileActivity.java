package com.adai.camera.novatek.filemanager.remote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.FileRepository;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

public class NovatekPhotoFileActivity extends BaseActivity implements NovatekFileContract.View {
    private NovatekFilePresenter mPresenter;
    private NovatekRemoteFileFragment mNovatekRemoteFileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_photo_file);
        VLCApplication.getInstance().setAllowDownloads(true);
        init();
        initView();
        initFile();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new NovatekFilePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.photo);
        mNovatekRemoteFileFragment = NovatekRemoteFileFragment.newInstance(3, FileManagerConstant.TYPE_REMOTE_PHOTO);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mNovatekRemoteFileFragment).commit();
    }

    private void initFile() {
        mPresenter.initFile(FileManagerConstant.TYPE_REMOTE_PHOTO);
    }

    @Override
    public void showLoading() {
        showpDialog();
    }

    @Override
    public void showLoading(String string) {
        showpDialog(string);
    }

    @Override
    public void showLoading(@StringRes int res) {
        showpDialog(res);
    }

    @Override
    public void showToast(String string) {
        showpDialog(string);
    }

    @Override
    public void showToast(@StringRes int res) {
        super.showToast(res);
    }

    @Override
    public void hideLoading() {
        hidepDialog();
    }

    @Override
    public Context getAttachedContext() {
        return this;
    }

    @Override
    public Activity getAttachedActivity() {
        return this;
    }

    @Override
    public void deleteResult(boolean success) {

    }

    @Override
    public void respGetFileList(List<FileDomain> cameraFiles) {
        //获取到所有的视频文件
        FileRepository.ALL_PHOTO_LIST.clear();
        FileRepository.ALL_PHOTO_LIST.addAll(cameraFiles);
        if (FileRepository.ALL_PHOTO_LIST.size() > 0) {
            mNovatekRemoteFileFragment.setData(FileRepository.ALL_PHOTO_LIST);
        } else {
            mNovatekRemoteFileFragment.empty();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (FileRepository.ALL_PHOTO_LIST.size() > 0) {
                mNovatekRemoteFileFragment.setData(FileRepository.ALL_PHOTO_LIST);
            } else {
                mNovatekRemoteFileFragment.empty();
            }
        }
    }

    @Override
    protected void goBack() {
        boolean editMode = mNovatekRemoteFileFragment.isEditMode();
        if (editMode) {
            mNovatekRemoteFileFragment.setEditMode(false);
        } else {
            if (MinuteFileDownloadManager.isDownloading) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.notice)
                        .setMessage(R.string.downloading_exit)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.detachView();
                                finish();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
            } else {
                mPresenter.detachView();
                super.goBack();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
