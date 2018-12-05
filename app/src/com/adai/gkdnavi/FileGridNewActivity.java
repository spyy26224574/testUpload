package com.adai.gkdnavi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.adai.gkdnavi.fragment.FileGridNewFragment1;
import com.adai.gkdnavi.fragment.square.AlbumNewFragment;
import com.adai.gkdnavi.utils.MinuteFileDownloadInfo;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.SpUtils;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MinuteFile;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FileGridNewActivity extends BaseActivity implements View.OnClickListener {
    //    private RelativeLayout mBottomLayout;
    private TextView mRightText;
    //    private FileGridNewFragment mFileGridFragment;
    //    private ImageView mIvDownload;
    private FileGridNewFragment1 mFileGridFragment;
    private int mType;
    private StateChangeReceiver mEditModeChangeReceiver;
    private boolean mFromCameraActivity;
    private boolean isEditMoade;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_grid_new);
        VLCApplication.getInstance().setAllowDownloads(true);
        initView();
        initEvent();
        initReceiver();
    }

    private void initReceiver() {
        mEditModeChangeReceiver = new StateChangeReceiver();
        IntentFilter filter = new IntentFilter(FileGridNewFragment1.ACTION_EDIT_MODE_CHANGE);
        registerReceiver(mEditModeChangeReceiver, filter);
    }

    private void initEvent() {
//        mIvDownload.setOnClickListener(this);
//        findViewById(R.id.iv_delete).setOnClickListener(this);
        mRightText.setOnClickListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mType = getIntent().getIntExtra("fileType", AlbumNewFragment.ALBUM_LOCAL_FILE);
        String title = getIntent().getStringExtra("title");
        String filePath = getIntent().getStringExtra("filePath");
        mFromCameraActivity = getIntent().getBooleanExtra("fromCameraActivity", false);
        setTitle(title);
        if (mType == AlbumNewFragment.ALBUM_RECORDER) {
            //记录仪相册
            String cameraName = httpGetCamName();
            if (!TextUtils.isEmpty(cameraName)) {
                setTitle(cameraName);
            }
        }
//        mBottomLayout = (RelativeLayout) findViewById(R.id.bottom_layout);
//        mIvDownload = (ImageView) findViewById(R.id.iv_download);
//        mIvDownload.setVisibility(mType == AlbumNewFragment.ALBUM_RECORDER ? View.VISIBLE : View.GONE);
        mRightText = (TextView) findViewById(R.id.right_text);
        mRightText.setText(R.string.select);
        mRightText.setVisibility(View.VISIBLE);
        mFileGridFragment = FileGridNewFragment1.newInstance(3, mType, filePath);
        mFileGridFragment.setOnFileStateChangeListener(new FileGridNewFragment1.OnFileStateChangeListener() {
            @Override
            public void deleted() {
                setEditMode(false);
            }

            @Override
            public void download() {
                setEditMode(false);
            }

        });
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mFileGridFragment).commit();
    }

    private String httpGetCamName() {
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int networkId = mWifiInfo.getNetworkId();
        SpUtils.putInt(this, "netWorkId", networkId);
        String strSSID = mWifiInfo.getSSID();
        if (!TextUtils.isEmpty(strSSID)) {
            if (strSSID.startsWith("\"") && strSSID.endsWith("\"")) {
                strSSID = strSSID.substring(1, strSSID.length() - 1);
            }
        }
        return strSSID;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
                if (getString(R.string.select).equals(mRightText.getText().toString())) {
                    setEditMode(true);
                } else {
                    setEditMode(false);
                }
                break;
        }
    }

    public void setEditMode(boolean editMode) {
        mRightText.setText(editMode ? getString(R.string.cancel) : getString(R.string.select));
//        mBottomLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        mFileGridFragment.setEditMode(editMode);
    }

    @Override
    protected void onStop() {
        super.onStop();
        ImageLoader.getInstance().stop();
    }

    @Override
    protected void goBack() {
        boolean isDownloading = false;
        if (mFileGridFragment.isEditMode()) {
            setEditMode(false);
        } else {
//            super.goBack();
            if (mType == AlbumNewFragment.ALBUM_RECORDER) {
                if (mFileGridFragment.getMinuteFiles() != null) {
                    for (MinuteFile minuteFile : mFileGridFragment.getMinuteFiles()) {
                        MinuteFileDownloadInfo minuteFileDownloadInfo = MinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(minuteFile);
                        if (minuteFileDownloadInfo.state == MinuteFileDownloadManager.STATE_DOWNLOADING) {
                            isDownloading = true;
                            break;
                        }

                    }
                    if (isDownloading) {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.notice)
                                .setMessage(R.string.downloading_exit)
                                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                }).setNegativeButton(R.string.cancel, null).show();
                    } else {
                        super.goBack();
                    }
                } else {
                    super.goBack();
                }
            } else {
                super.goBack();
            }
        }
    }

    private class StateChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case FileGridNewFragment1.ACTION_EDIT_MODE_CHANGE:
                    setEditMode(true);
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == FileGridNewFragment1.REQUEST_FILE_DELETE) {
                mFileGridFragment.getFileList();
            }
        }
    }

    @Override
    protected void onDestroy() {
        VLCApplication.getInstance().setAllowDownloads(false);
        MinuteFileDownloadManager.getInstance().removeAllObserver();
        super.onDestroy();
        if (mType == AlbumNewFragment.ALBUM_RECORDER) {
//            VoiceManager.isCameraBusy = true;
//            if (!mFromCameraActivity) {
//                CameraUtils.changeMode(CameraUtils.MODE_MOVIE, new CameraUtils.ModeChangeListener() {
//                    @Override
//                    public void success() {
//                        Log.e(_TAG_, "回复录制状态success: ");
//                        CameraUtils.saveStartRecord(true, new CameraUtils.CmdListener() {
//                            @Override
//                            public void onResponse(String response) {
//                                VoiceManager.isCameraBusy = false;
//                            }
//
//                            @Override
//                            public void onErrorResponse(Exception volleyError) {
//                                VoiceManager.isCameraBusy = false;
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void failure(Throwable throwable) {
//                        Log.e(_TAG_, "回复failure: ");
//                        VoiceManager.isCameraBusy = false;
//                    }
//                });
//            } else {
//                VoiceManager.isCameraBusy = false;
//            }
        }
        unregisterReceiver(mEditModeChangeReceiver);
    }
}
