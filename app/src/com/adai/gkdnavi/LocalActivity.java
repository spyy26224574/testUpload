package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.adai.gkdnavi.fragment.LocalPhotoFragment;
import com.adai.gkdnavi.fragment.LocalVideoFragment;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;

public class LocalActivity extends FragmentActivity implements OnClickListener {

    private ImageButton mLocalVideo, mLocalPhoto;
    private FragmentManager mFragmentManager;
    private LinearLayout mLinearLayout;
    private boolean select_video = true;
    LocalVideoFragment mLocalVideoFragment;
    LocalPhotoFragment mLocalPhotoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.local_file);
        File destDir = new File(VLCApplication.DOWNLOADPATH);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_button);
        mLocalVideo = (ImageButton) findViewById(R.id.ib_video);
        mLocalPhoto = (ImageButton) findViewById(R.id.ib_photo);
        mLocalVideo.setOnClickListener(this);
        mLocalPhoto.setOnClickListener(this);
        mFragmentManager = getSupportFragmentManager();
        mLocalVideoFragment = new LocalVideoFragment();
        mLocalPhotoFragment = new LocalPhotoFragment();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fl_container, mLocalPhotoFragment);
        transaction.add(R.id.fl_container, mLocalVideoFragment);
        transaction.hide(mLocalPhotoFragment);
        transaction.commit();
        mLocalVideo.setImageDrawable(getResources().getDrawable(R.drawable.video_selected));
        mLocalPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo));
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.ib_video:
                if (!select_video) {
                    select_video = true;
                    transaction.show(mLocalVideoFragment);
                    transaction.hide(mLocalPhotoFragment);
                    mLocalVideo.setImageDrawable(getResources().getDrawable(R.drawable.video_selected));
                    mLocalPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                }
                break;
            case R.id.ib_photo:
                if (select_video) {
                    select_video = false;
                    transaction.show(mLocalPhotoFragment);
                    transaction.hide(mLocalVideoFragment);
                    mLocalVideo.setImageDrawable(getResources().getDrawable(R.drawable.video));
                    mLocalPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo_selected));
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:

                if (mLinearLayout.getVisibility() == (View.GONE)) {
                    mLinearLayout.setVisibility(View.VISIBLE);
                    Intent intent = new Intent("MESSAGE");
                    sendBroadcast(intent);
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

}