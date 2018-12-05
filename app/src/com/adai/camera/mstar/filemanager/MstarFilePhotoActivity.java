package com.adai.camera.mstar.filemanager;

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


public class MstarFilePhotoActivity extends BaseActivity implements MstarFilePhotoContract.View {
    private MstarFilePhotoPresenter mPresenter;
    private MstarFileFragment mMstarFileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_file_photo);
        VLCApplication.getInstance().setAllowDownloads(true);
        init();
        initView();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new MstarFilePhotoPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.photo);
        mMstarFileFragment = MstarFileFragment.newInstance(3, FileManagerConstant.TYPE_REMOTE_PHOTO);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mMstarFileFragment).commit();
    }

    @Override
    public void showLoading(@StringRes int res) {
        showpDialog(res);
    }

    @Override
    public void showToast(String string) {
        super.showToast(string);
    }

    @Override
    public void showToast(@StringRes int res) {
        super.showToast(res);
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
    public void getFileSuccess() {
        hideLoading();
        FileRepository.ALL_PHOTO_LIST.clear();
        FileRepository.ALL_PHOTO_LIST.addAll(mPresenter.getPhotoFiles());
        if (FileRepository.ALL_PHOTO_LIST.size() > 0) {
            mMstarFileFragment.setData(FileRepository.ALL_PHOTO_LIST);
        } else {
            mMstarFileFragment.empty();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (FileRepository.ALL_PHOTO_LIST.size() > 0) {
                mMstarFileFragment.setData(FileRepository.ALL_PHOTO_LIST);
            } else {
                mMstarFileFragment.empty();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void goBack() {
        boolean editMode = mMstarFileFragment.isEditMode();
        if (editMode) {
            mMstarFileFragment.setEditMode(false);
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
