package com.adai.camera.mstar.filemanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.adai.camera.FileManagerConstant;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.filepicker.adapters.SectionsPagerAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class MstarFileVideoActivity extends BaseActivity implements MstarFileVideoContract.View {
    private MstarFileFragment mCyclicVideoFragment, mUrgentVideoFragment;
    private ViewPager mViewpager;
    private SectionsPagerAdapter mAdapter;
    private ArrayList<FileDomain> mCyclicVideos = new ArrayList<>();
    private ArrayList<FileDomain> mUrgentVideos = new ArrayList<>();
    private MstarFileVideoContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_file_manager);
        VLCApplication.getInstance().setAllowDownloads(true);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initView();
        if (mCyclicVideoFragment == null) {
            mCyclicVideoFragment = MstarFileFragment.newInstance(2, FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO);
        }
        mAdapter.addFragment(mCyclicVideoFragment, getString(R.string.cyclic_video));
        if (mUrgentVideoFragment == null) {
            mUrgentVideoFragment = MstarFileFragment.newInstance(2, FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO);
        }
        mAdapter.addFragment(mUrgentVideoFragment, getString(R.string.urgent_video));
        mViewpager.setOffscreenPageLimit(1);
        mViewpager.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new MstarFileVideoPresenter();
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
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    public void getFileSuccess() {
        hideLoading();
        mUrgentVideos.clear();
        mCyclicVideos.clear();
        mUrgentVideos.addAll(mPresenter.getEventFiles());
        mCyclicVideos.addAll(mPresenter.getNormalFiles());
        LogUtils.e(mUrgentVideos.toString());
        LogUtils.e(mCyclicVideos.toString());
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
