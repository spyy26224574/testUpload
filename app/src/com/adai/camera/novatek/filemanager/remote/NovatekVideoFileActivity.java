package com.adai.camera.novatek.filemanager.remote;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;

import com.adai.camera.FileManagerConstant;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.filepicker.adapters.SectionsPagerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;


public class NovatekVideoFileActivity extends BaseActivity implements NovatekVideoFileContract.View {
    private NovatekRemoteFileFragment mCyclicVideoFragment, mUrgentVideoFragment, mMonitorFragment;
    private ViewPager mViewpager;
    private SectionsPagerAdapter mAdapter;
    private NovatekVideoFilePresenter mPresenter;
    private ArrayList<FileDomain> mCyclicVideos = new ArrayList<>();
    private ArrayList<FileDomain> mUrgentVideos = new ArrayList<>();
    private ArrayList<FileDomain> mMonitorVideos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_video_file);
        VLCApplication.getInstance().setAllowDownloads(true);
        init();
        initView();
        if (mCyclicVideoFragment == null) {
            mCyclicVideoFragment = NovatekRemoteFileFragment.newInstance(2, FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO);
        }
        mAdapter.addFragment(mCyclicVideoFragment, getString(R.string.cyclic_video));
        if (mUrgentVideoFragment == null) {
            mUrgentVideoFragment = NovatekRemoteFileFragment.newInstance(2, FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO);
        }
        mAdapter.addFragment(mUrgentVideoFragment, getString(R.string.urgent_video));
        if (mMonitorFragment == null) {
            mMonitorFragment = NovatekRemoteFileFragment.newInstance(2, FileManagerConstant.TYPE_REMOTE_MONITOR_VIDEO);
        }
        mAdapter.addFragment(mMonitorFragment, getString(R.string.monitor_video));
        mViewpager.setOffscreenPageLimit(2);
        mViewpager.setAdapter(mAdapter);
        initFile();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new NovatekVideoFilePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.video);
        mViewpager = (ViewPager) findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        mAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        tabLayout.addTab(tabLayout.newTab().setText(R.string.cyclic_video), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.urgent_video));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
    }

    private void initFile() {
        mPresenter.initFile();
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
        super.showToast(string);
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
        mCyclicVideos.clear();
        mUrgentVideos.clear();
        for (FileDomain fileDomain : cameraFiles) {
            if (fileDomain.attr == 33) {
                mUrgentVideos.add(fileDomain);
            } else {
                if (fileDomain.fpath.contains("LAPSE")) {
                    mMonitorVideos.add(fileDomain);
                } else {
                    mCyclicVideos.add(fileDomain);
                }
            }
        }
        if (mCyclicVideos.size() > 0) {
            mCyclicVideoFragment.setData(mCyclicVideos);
        } else {
            mCyclicVideoFragment.empty();
        }
        if (mUrgentVideos.size() > 0) {
            mUrgentVideoFragment.setData(mUrgentVideos);
        } else {
            mUrgentVideoFragment.empty();
        }
        if (mMonitorVideos.size() > 0) {
            mMonitorFragment.setData(mMonitorVideos);
        } else {
            mMonitorFragment.empty();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void goBack() {
        int currentItem = mViewpager.getCurrentItem();
        if (currentItem == 0) {
            boolean editMode = mCyclicVideoFragment.isEditMode();
            if (editMode) {
                mCyclicVideoFragment.setEditMode(false);
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
        } else {
            boolean editMode = mUrgentVideoFragment.isEditMode();
            if (editMode) {
                mUrgentVideoFragment.setEditMode(false);
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
