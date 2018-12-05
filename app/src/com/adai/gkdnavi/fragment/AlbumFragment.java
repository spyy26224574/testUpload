package com.adai.gkdnavi.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.novatek.filemanager.NovatekFileManagerFragment;
import com.adai.gkdnavi.R;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.adapters.SectionsPagerAdapter;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/10/26 17:25
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class AlbumFragment extends BaseFragment {
    private NovatekFileManagerFragment mVideoFragment, mPhotoFragment;
    public static final String ACTION_FRESH = "com.adai.gkdnavi.fragment.square.AlbumFragment.fresh";

    private boolean needRefreshVideo = false, needRefreshPicture = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_local_album, container, false);
        ViewPager viewpager = (ViewPager) rootView.findViewById(R.id.viewpager);
        TabLayout tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        SectionsPagerAdapter adapter = new SectionsPagerAdapter(getChildFragmentManager());
        if (mVideoFragment == null) {
            mVideoFragment = NovatekFileManagerFragment.newInstance(2, FileManagerConstant.TYPE_LOCAL_VIDEO, VLCApplication.DOWNLOADPATH);
        }
        adapter.addFragment(mVideoFragment, getString(R.string.video));
        if (mPhotoFragment == null) {
            mPhotoFragment = NovatekFileManagerFragment.newInstance(3, FileManagerConstant.TYPE_LOCAL_PICTURE, VLCApplication.DOWNLOADPATH);
        }
        adapter.addFragment(mPhotoFragment, getString(R.string.photo));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.video), true);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.photo));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewpager.setOffscreenPageLimit(1);
        viewpager.setAdapter(adapter);
        IntentFilter filter = new IntentFilter(ACTION_FRESH);
        mContext.registerReceiver(mFreshReceiver, filter);
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            setEditMode(false);
        }
    }

    public void setEditMode(boolean isEditMode) {
        if (mPhotoFragment != null && mPhotoFragment.isEditMode()) {
            mPhotoFragment.setEditMode(isEditMode);
        }
        if (mVideoFragment != null && mVideoFragment.isEditMode()) {
            mVideoFragment.setEditMode(isEditMode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (needRefreshVideo) {
            if (mVideoFragment != null) {
                mVideoFragment.initFile();
                needRefreshVideo = false;
            }
        }
        if (needRefreshPicture) {
            if (mPhotoFragment != null) {
                mPhotoFragment.initFile();
                needRefreshPicture = false;
            }
        }
    }

    public boolean isEditMode() {
        return (mPhotoFragment != null && mPhotoFragment.isEditMode()) || mVideoFragment != null && mVideoFragment.isEditMode();
    }

    private BroadcastReceiver mFreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isVideo = intent.getBooleanExtra("isVideo", false);
            if (isVideo) {
                needRefreshVideo = true;
            } else {
                needRefreshPicture = true;
            }
            if (isResumed()) {
                if (isVideo) {
                    if (mVideoFragment != null) {
                        mVideoFragment.initFile();
                    }
                } else {
                    if (mPhotoFragment != null) {
                        mPhotoFragment.initFile();
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mFreshReceiver);
    }
}
