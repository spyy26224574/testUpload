package com.adai.camera.novatek.preview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.novatek.adapter.NovatekResolutionAdapter;
import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.camera.novatek.filemanager.remote.NovatekPhotoFileActivity;
import com.adai.camera.novatek.filemanager.remote.NovatekVideoFileActivity;
import com.adai.camera.novatek.settting.NovatekSettingActivity;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.PlayerView;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.domain.MovieRecord;
import com.widget.piechart.ScreenUtils;


public class NovatekPreviewActivity extends BaseActivity implements NovatekPreviewContract.View, PlayerView.OnChangeListener, View.OnClickListener {
    private RelativeLayout mRlTitle;
    private LinearLayout mLlPicture, mLlVideo, mLlPhoto, mLlResolution, mLlCurResolution;
    private TextView mHeadTitle, mTvTakePhoto, mTvResolution, mTvNoCardNotice, mTvRecordTime;
    private RecyclerView mRvResolution;
    private ImageView mIvSetting, mIvVoice;
    private PlayerView mPvVideo;
    private RelativeLayout mRlBottom, video_frame;
    private NovatekPreviewContract.Presenter mNovatekPreviewPresenter;
    private RelativeLayout.LayoutParams landscapeParams;
    private RelativeLayout.LayoutParams portraitParams;
    private RelativeLayout.LayoutParams mLlPicturePortraitParams, mLLPictureLandscapeParams;
    private RelativeLayout.LayoutParams mRecordWaitPortraitParams, mRecordWaitLandscapeParams;
    private RelativeLayout.LayoutParams mRecordPortraitParams, mRecordLandscapeParams;

    private RadioButton mRbVideoMode, mRbPhotoMode;
    private RadioGroup mRgMode;
    private ImageView mIvPip, mIvHorizontalPip, iv_record, iv_fullscreen, mIvTakePhoto;
    private boolean isPortrait = true;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private NovatekResolutionAdapter mNovatekResolutionAdapter;
    private java.lang.Runnable autoHideViewTask = new Runnable() {
        @Override
        public void run() {
            iv_fullscreen.setVisibility(View.GONE);
            mLlResolution.setVisibility(View.GONE);
        }
    };
    private boolean mIsHttp;
    private boolean mVoiceIsOpen;

    RelativeLayout ll_record_wait;
    ImageView iv_record_wait;
    RelativeLayout rl_novatek_camera;

    RelativeLayout horizontal_frame;
    ImageView iv_voice_land;
    //    ImageView iv_pip_land;
    ImageView iv_fullscreen_land;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_camera);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mNovatekPreviewPresenter = new NovatekPreviewPresenter();
        mNovatekPreviewPresenter.attachView(this);
        mNovatekPreviewPresenter.init();
    }

    private void initParams(DisplayMetrics dm) {
        int width = dm.widthPixels;//宽度height = dm.heightPixels ;//高度
        int height = dm.heightPixels;
        if (width > height) {
            width = width ^ height;
            height = width ^ height;
            width = width ^ height;
        }
//        landscapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        landscapeParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        landscapeParams = new RelativeLayout.LayoutParams(height - ScreenUtils.dp2px(this, 110), width);
        landscapeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        portraitParams = new RelativeLayout.LayoutParams(width, width * 9 / 16);
        portraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        portraitParams.addRule(RelativeLayout.BELOW, R.id.rl_title);
//
//        mLlPicturePortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 104), ScreenUtils.dp2px(this, 104));
//        mLlPicturePortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        mLlPicturePortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
//        mLlPicturePortraitParams.bottomMargin = ScreenUtils.dp2px(this, 32);
//        mLLPictureLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 104), ScreenUtils.dp2px(this, 104));
//        mLLPictureLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        mLLPictureLandscapeParams.addRule(RelativeLayout.CENTER_VERTICAL);
//        mLLPictureLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 16);
        //切换播放拍照 按钮

        mRecordWaitPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitPortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mRecordWaitPortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRecordWaitPortraitParams.bottomMargin = ScreenUtils.dp2px(this, 10);

        mRecordWaitLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//        mRecordWaitLandscapeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ABOVE, R.id.ll_picture);
        mRecordWaitLandscapeParams.bottomMargin = ScreenUtils.dp2px(this, 20);
        mRecordWaitLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 40);


        //播放  拍照按钮
        mLlPicturePortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLlPicturePortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mLlPicturePortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mLlPicturePortraitParams.bottomMargin = ScreenUtils.dp2px(this, 50);

        mLLPictureLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLLPictureLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mLLPictureLandscapeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mLLPictureLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 10);

        //播放状态 mRecordPortraitParams, mRecordLandscapeParams;

        mRecordPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
        mRecordPortraitParams.topMargin = ScreenUtils.dp2px(this, 10);
        mRecordPortraitParams.leftMargin = ScreenUtils.dp2px(this, 10);

        mRecordLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
//        mRecordLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mRecordLandscapeParams.addRule(RelativeLayout.LEFT_OF, R.id.tv_record_time);
        mRecordLandscapeParams.topMargin = ScreenUtils.dp2px(this, 5);
        mRecordLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 10);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(SpUtils.getString(this, "SSID", ""));
        horizontal_frame = (RelativeLayout) findViewById(R.id.horizontal_frame);
        iv_voice_land = (ImageView) findViewById(R.id.iv_voice_land);
        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
        iv_voice_land.setOnClickListener(this);
        iv_fullscreen_land.setOnClickListener(this);

        rl_novatek_camera = (RelativeLayout) findViewById(R.id.rl_novatek_camera);

        ll_record_wait = (RelativeLayout) findViewById(R.id.ll_record_wait);
        ll_record_wait.setOnClickListener(this);
        iv_record_wait = (ImageView) findViewById(R.id.iv_record_wait);

        mLlPicture = (LinearLayout) findViewById(R.id.ll_picture);
        mLlPhoto = (LinearLayout) findViewById(R.id.ll_photo);
        mLlVideo = (LinearLayout) findViewById(R.id.ll_video);
        mRlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        mHeadTitle = (TextView) findViewById(R.id.head_title);
        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mPvVideo = (PlayerView) findViewById(R.id.pv_video);
        mPvVideo.setCurrentSize(PlayerView.SURFACE_FILL);
        mRlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        mRgMode = (RadioGroup) findViewById(R.id.rg_mode);
        mRbVideoMode = (RadioButton) findViewById(R.id.rb_video_mode);
        mRbPhotoMode = (RadioButton) findViewById(R.id.rb_photo_mode);
        mIvPip = (ImageView) findViewById(R.id.iv_pip);
        iv_record = (ImageView) findViewById(R.id.iv_record);
        iv_fullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        video_frame = (RelativeLayout) findViewById(R.id.video_frame);
        mTvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        mLlResolution = (LinearLayout) findViewById(R.id.ll_resolution);
        mLlCurResolution = (LinearLayout) findViewById(R.id.ll_cur_resolution);
        mTvResolution = (TextView) findViewById(R.id.tv_cur_resolution);
        mRvResolution = (RecyclerView) findViewById(R.id.rv_resolution);
        mTvNoCardNotice = (TextView) findViewById(R.id.tv_no_card_notice);
        mIvVoice = (ImageView) findViewById(R.id.iv_voice);
        mTvRecordTime = (TextView) findViewById(R.id.tv_record_time);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        AnimationDrawable drawable = (AnimationDrawable) iv_record.getDrawable();
        drawable.start();
        mIvHorizontalPip = (ImageView) findViewById(R.id.iv_horizontal_pip);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        initParams(dm);
        VoiceManager.setDefaultNetwork(mContext, true);
        mNovatekPreviewPresenter.firstConnectSocket();
        showLoading(getString(R.string.in_the_buffer));
        mNovatekPreviewPresenter.initOrientation();
        autoHideView();
    }

    private void autoHideView() {
        mHandler.removeCallbacks(autoHideViewTask);
        mHandler.postDelayed(autoHideViewTask, 3000);
    }

    private void initEvent() {
        mLlPicture.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mLlPhoto.setOnClickListener(this);
        mLlVideo.setOnClickListener(this);
        mRbPhotoMode.setOnClickListener(this);
        mRbVideoMode.setOnClickListener(this);
        mIvPip.setOnClickListener(this);
        mPvVideo.setOnClickListener(this);
        mIvHorizontalPip.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        mIvVoice.setOnClickListener(this);
        mLlResolution.setOnClickListener(this);
        mRvResolution.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    autoHideView();
                } else if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    mHandler.removeCallbacks(autoHideViewTask);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mNovatekPreviewPresenter.onConfigurationChanged(newConfig);
    }

    @Override
    public void changeOrientation(boolean isPortrait) {
        this.isPortrait = isPortrait;
        horizontal_frame.setVisibility(isPortrait ? View.GONE : View.VISIBLE);
        mRlBottom.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mRlTitle.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        if (CameraUtils.currentProduct != CameraUtils.PRODUCT.SJ) {
//            mRgMode.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        }
//        iv_fullscreen.setBackgroundResource(isPortrait ? R.drawable.selector_fullscreen : R.drawable.selector_exit_fullscreen);
//        iv_fullscreen.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        if (isPortrait) {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.white));
            video_frame.setLayoutParams(portraitParams);
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            ll_record_wait.setLayoutParams(mRecordWaitPortraitParams);
            mLlPicture.setLayoutParams(mLlPicturePortraitParams);
            iv_record.setLayoutParams(mRecordPortraitParams);

        } else {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.dark_black));
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            video_frame.setLayoutParams(landscapeParams);

            mLlPicture.setLayoutParams(mLLPictureLandscapeParams);
            ll_record_wait.setLayoutParams(mRecordWaitLandscapeParams);
            iv_record.setLayoutParams(mRecordLandscapeParams);
        }
        mPvVideo.post(new Runnable() {
            @Override
            public void run() {
                mPvVideo.changeSurfaceSize();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        VoiceManager.isCameraBusy = false;
        mNovatekPreviewPresenter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        VoiceManager.isCameraBusy = true;
//        if (mPvVideo != null) {
//            LogUtils.e("onPause");
//            if (mPvVideo.isPlaying()) {
//                mPvVideo.stop();
//            }
//        }
//        mNovatekPreviewPresenter.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setCurrentLanguage();
        mNovatekPreviewPresenter.onRestart();
        mPvVideo.restartMediaPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPvVideo != null) {
            LogUtils.e("onPause");
            if (mPvVideo.isPlaying()) {
                mPvVideo.stop();
            }
        }
        mNovatekPreviewPresenter.onPause();
        mNovatekPreviewPresenter.onStop();
        stopRecordTime();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNovatekPreviewPresenter.detachView();
    }

    @Override
    public Context getAttachedContext() {
        return this;
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
    protected void goBack() {
        super.goBack();
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notice));
        if (getCurrentNetModel() == 0) {
            builder.setMessage(getString(R.string.wifi_checkmessage));
        } else {
            builder.setMessage(getString(R.string.ap_checkmessage));
        }
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (getCurrentNetModel() == 0) {
                    WifiUtil.getInstance().gotoWifiSetting(mContext);
                } else {

                    WifiUtil.getInstance().startAP(NovatekPreviewActivity.this);
                    mNovatekPreviewPresenter.startConnectThread();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                NovatekPreviewActivity.this.finish();
            }
        });
        builder.setCancelable(false).create().show();
    }

    @Override
    public void startPreview(boolean isHttp) {
        LogUtils.e("isHttp=" + isHttp);
        mPvVideo.initPlayer(isHttp ? Contacts.BASE_PREVIEW_HTTP : Contacts.BASE_RTSP);
        // 第五步：开始播放
        mPvVideo.start();
    }

    @Override
    public void onBufferChanged(float buffer) {
        mNovatekPreviewPresenter.onBufferChanged(buffer);
    }

    @Override
    public void onLoadComplete() {
        if (!CameraUtils.hasSDCard) {
            mTvNoCardNotice.setVisibility(View.VISIBLE);
            showRecordState(false);
        } else {
            mTvNoCardNotice.setVisibility(View.GONE);
        }
        mNovatekPreviewPresenter.onLoadComplete();
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
            CameraUtils.getRecordTime(new CameraUtils.RecordTimeCallback() {
                @Override
                public void success(int time) {
                    if (CameraUtils.isRecording) {
                        startRecordTime(time);
                    } else {
                        stopRecordTime();
                    }
                }

                @Override
                public void error(String error) {

                }
            });
            String curMovieResolution = NovatekRepository.getInstance().getCurState(NovatekWifiCommands.MOVIE_SET_RECORD_SIZE);
            if (!TextUtils.isEmpty(curMovieResolution)) {
                mTvResolution.setText(curMovieResolution);
            }
        } else {
            stopRecordTime();
            String curPhotoResolution = NovatekRepository.getInstance().getCurState(NovatekWifiCommands.PHOTO_SET_CAPTURE_SIZE);
            mTvResolution.setText(curPhotoResolution);
        }
        int cmd;
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
            cmd = NovatekWifiCommands.MOVIE_SET_RECORD_SIZE;
        } else {
            cmd = NovatekWifiCommands.PHOTO_SET_CAPTURE_SIZE;
        }
        final SparseArray<String> menuItem = NovatekRepository.getInstance().getMenuItem(cmd);
        if (menuItem != null && menuItem.size() > 0) {
            mNovatekResolutionAdapter = new NovatekResolutionAdapter(cmd);
            mRvResolution.setLayoutManager(new LinearLayoutManager(this));
            mRvResolution.setAdapter(mNovatekResolutionAdapter);
            mNovatekResolutionAdapter.setOnItemClickListener(new NovatekResolutionAdapter.ItemClickListener() {
                @Override
                public void onItemClick(int cmdId, int key) {
                    if (menuItem.get(key) != null && menuItem.get(key).equals(mTvResolution.getText().toString())) {
                        return;
                    }
                    mNovatekPreviewPresenter.setResolution(cmdId, key);
                }
            });
        }
        if (CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ) {
            mLlResolution.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError() {
        mNovatekPreviewPresenter.onError();
    }

    @Override
    public void onPlayerError() {
        mNovatekPreviewPresenter.onPlayError();
    }

    @Override
    public void onStartPlay() {
        mNovatekPreviewPresenter.onStartPlay();
    }

    @Override
    public void onPlayNothing() {
        mNovatekPreviewPresenter.onPlayError();
    }

    @Override
    public void onEnd() {
        mNovatekPreviewPresenter.onEnd();
//        mPvVideo.restartMediaPlayer();
//        // 第五步：开始播放
//        mPvVideo.start();
    }

    private boolean clickable = true;

    @Override
    public void onClick(View v) {
        if (!clickable) {
            return;
        }
        switch (v.getId()) {
            case R.id.ll_record_wait:
                if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
                    mNovatekPreviewPresenter.changeMode(MstarCamera.MODE_PHOTO);
                } else {
                    mNovatekPreviewPresenter.changeMode(MstarCamera.MODE_MOVIE);
                }
                break;
            case R.id.iv_voice:
            case R.id.iv_voice_land:
                if (CameraUtils.currentProduct == CameraUtils.PRODUCT.DCT) {
                    if (CameraUtils.isRecording || !CameraUtils.hasSDCard) {
                        showToast(R.string.wifi_stoprecordingbef);
                    } else {
                        clickable = false;
                        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_RECORD_AUDIO, mVoiceIsOpen ? "0" : "1", new CameraUtils.CmdCallback() {
                            @Override
                            public void success(int commandId, String par, MovieRecord movieRecord) {
                                clickable = true;
                                if ("0".equals(movieRecord.getStatus())) {
                                    audioChange(!mVoiceIsOpen);
                                } else {
                                    showToast(R.string.set_failure);
                                }
                            }

                            @Override
                            public void failed(int commandId, String par, String error) {
                                clickable = true;
                                showToast(R.string.set_failure);
                            }
                        });
                    }
//                    showpDialog();
//                    stopPreview();
//                    CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "0", new CameraUtils.CmdCallback() {
//                        @Override
//                        public void success(int commandId, String par, MovieRecord movieRecord) {
//                            if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
//                                CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_RECORD_AUDIO, mVoiceIsOpen ? "0" : "1", new CameraUtils.CmdCallback() {
//                                    @Override
//                                    public void success(int commandId, String par, MovieRecord movieRecord) {
////                                        clickable = true;
//                                        if ("0".equals(movieRecord.getStatus())) {
//                                            audioChange(!mVoiceIsOpen);
//                                        } else {
//                                            showToast(R.string.set_failure);
//                                        }
//                                        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
//                                            @Override
//                                            public void success(int commandId, String par, MovieRecord movieRecord) {
//                                                if ("0".equals(movieRecord.getStatus())) {
//                                                    hideLoading();
//                                                    startPreview();
//                                                } else {
//                                                    hideLoading();
//                                                    showToast(R.string.Check_connection);
//                                                    exit();
//                                                }
//                                            }
//
//                                            @Override
//                                            public void failed(int commandId, String par, String error) {
//                                                hideLoading();
//                                                showToast(R.string.Check_connection);
//                                                exit();
//                                            }
//                                        });
//                                    }
//
//                                    @Override
//                                    public void failed(int commandId, String par, String error) {
////                                        clickable = true;
//                                        showToast(R.string.set_failure);
//                                        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
//                                            @Override
//                                            public void success(int commandId, String par, MovieRecord movieRecord) {
//                                                if ("0".equals(movieRecord.getStatus())) {
//                                                    hideLoading();
//                                                    startPreview();
//                                                } else {
//                                                    hideLoading();
//                                                    showToast(R.string.Check_connection);
//                                                    exit();
//                                                }
//                                            }
//
//                                            @Override
//                                            public void failed(int commandId, String par, String error) {
//                                                hideLoading();
//                                                showToast(R.string.Check_connection);
//                                                exit();
//                                            }
//                                        });
//                                    }
//                                });
//                            } else {
//                                hideLoading();
//                                startPreview();
//                            }
//                        }
//
//                        @Override
//                        public void failed(int commandId, String par, String error) {
//                            hidepDialog();
//                            startPreview();
//                        }
//                    });
                }
                break;
            case R.id.iv_fullscreen:
                setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.iv_fullscreen_land:
                setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.iv_setting:
                mPvVideo.stop();
                if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO || !CameraUtils.isRecording) {
                    startActivity(NovatekSettingActivity.class);
                    return;
                }
                showLoading(R.string.msg_center_stop_recording);
                CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                    @Override
                    public void success() {
                        CameraUtils.isRecording = false;
                        hideLoading();
                        startActivity(NovatekSettingActivity.class);
                    }

                    @Override
                    public void error(String error) {
                        hideLoading();
                        showToast(R.string.stop_recording_failed);
                        startPreview(mIsHttp);
                    }
                });
                break;
            case R.id.ll_video:
                if (CameraUtils.hasSDCard) {
                    mPvVideo.stop();
                    if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO || !CameraUtils.isRecording) {
                        startActivity(NovatekVideoFileActivity.class);
                        return;
                    }
                    showLoading(R.string.msg_center_stop_recording);
                    CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                        @Override
                        public void success() {
                            CameraUtils.isRecording = false;
                            hideLoading();
                            startActivity(NovatekVideoFileActivity.class);
                        }

                        @Override
                        public void error(String error) {
                            hideLoading();
                            showToast(R.string.stop_recording_failed);
                            startPreview(mIsHttp);
                        }
                    });

                } else {
                    showToast(R.string.wifi_sdcard);
                }
                break;
            case R.id.ll_photo:
                if (CameraUtils.hasSDCard) {
                    mPvVideo.stop();
                    if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO || !CameraUtils.isRecording) {
                        startActivity(NovatekPhotoFileActivity.class);
                        return;
                    }
                    showLoading(R.string.msg_center_stop_recording);
                    CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                        @Override
                        public void success() {
                            CameraUtils.isRecording = false;
                            hideLoading();
                            startActivity(NovatekPhotoFileActivity.class);
                        }

                        @Override
                        public void error(String error) {
                            hideLoading();
                            showToast(R.string.stop_recording_failed);
                            startPreview(mIsHttp);
                        }
                    });

                } else {
                    showToast(R.string.wifi_sdcard);
                }

                break;
            case R.id.ll_picture:
//                if (CameraUtils.hasSDCard) {
//                    if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
//                        mNovatekPreviewPresenter.takePhoto();
//                    } else if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
//                        mNovatekPreviewPresenter.toggleRecord();
//                    }
//                } else {
//                    showToast(R.string.wifi_sdcard);
//                }
                if (CameraUtils.hasSDCard) {
                    mNovatekPreviewPresenter.recordShot();
                } else {
                    showToast(R.string.wifi_sdcard);
                }
                break;
            case R.id.rb_video_mode:
                mNovatekPreviewPresenter.changeMode(MstarCamera.MODE_MOVIE);
                break;
            case R.id.rb_photo_mode:
                mNovatekPreviewPresenter.changeMode(MstarCamera.MODE_PHOTO);
                break;
            case R.id.iv_pip:
            case R.id.iv_horizontal_pip:
                mNovatekPreviewPresenter.changePip();
                break;
            case R.id.pv_video:
                if (mLlResolution.getVisibility() == View.VISIBLE) {
                    iv_fullscreen.setVisibility(View.GONE);
                    mLlResolution.setVisibility(View.GONE);
                    mHandler.removeCallbacks(autoHideViewTask);
                } else {
                    mRvResolution.setVisibility(View.GONE);
                    if (isPortrait) {
                        iv_fullscreen.setVisibility(View.VISIBLE);
                    }
                    if (CameraUtils.currentProduct != CameraUtils.PRODUCT.SJ) {
                        mLlResolution.setVisibility(View.VISIBLE);
                    }
                    autoHideView();
                }
                break;
            case R.id.ll_resolution:
                mRvResolution.setVisibility(mRvResolution.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                mRvResolution.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = mRvResolution.getHeight();
                        if (height > ScreenUtils.dp2px(NovatekPreviewActivity.this, 96)) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mRvResolution.getLayoutParams();
                            layoutParams.height = ScreenUtils.dp2px(NovatekPreviewActivity.this, 96);
                            mRvResolution.setLayoutParams(layoutParams);
                        }
                    }
                });
                autoHideView();
                break;
            default:
                break;
        }
    }

    @Override
    public void respChangePip(int drawableRes) {
        mIvPip.setBackgroundResource(drawableRes);
        mIvHorizontalPip.setImageResource(drawableRes);
    }

    @Override
    public void showPip(int which) {
        switch (which) {
            case -1:
                mIvPip.setVisibility(View.GONE);
                mIvHorizontalPip.setVisibility(View.GONE);
                break;
            case 0:
                mIvPip.setVisibility(View.VISIBLE);
                mIvHorizontalPip.setVisibility(View.GONE);
                break;
            case 1:
                mIvPip.setVisibility(View.GONE);
                mIvHorizontalPip.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void stopPreview() {
        if (mPvVideo.isPlaying()) {
            LogUtils.e("stopPreview");
            mPvVideo.stop();
        }
    }

    @Override
    public void startPreview() {
        if (!mPvVideo.isPlaying()) {
            mPvVideo.start();
        }
    }

    @Override
    public void initPlayView(boolean isHttp) {
        LogUtils.e("isHttp=" + isHttp);
        mIsHttp = isHttp;
        mPvVideo.initPlayer(mIsHttp ? Contacts.BASE_PREVIEW_HTTP : Contacts.BASE_RTSP);
        // 第四步:设置事件监听，监听缓冲进度等
        mPvVideo.setOnChangeListener(this);
        // 第五步：开始播放
        mPvVideo.start();
    }

    @Override
    public void startTakePhoto() {
        mPvVideo.stop();
        mLlPicture.setBackgroundResource(R.drawable.bg_circle);
    }

    @Override
    public void takePhotoEnd() {
        mPvVideo.start();
        mLlPicture.setBackgroundResource(R.drawable.bg_circle);
    }

    @Override
    public void currentMode(int mode) {
        if (CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ) {
            return;
        }
        if (mode == CameraUtils.MODE_MOVIE && CameraUtils.currentProduct == CameraUtils.PRODUCT.DCT) {
            mIvVoice.setVisibility(View.VISIBLE);
            iv_voice_land.setVisibility(View.VISIBLE);
        } else {
            mIvVoice.setVisibility(View.GONE);
            iv_voice_land.setVisibility(View.GONE);
        }
        LogUtils.e("currentMode = " + mode);
        mRgMode.check(mode == CameraUtils.MODE_MOVIE ? R.id.rb_video_mode : R.id.rb_photo_mode);
        mLlPicture.setBackgroundResource(mode == CameraUtils.MODE_MOVIE && !CameraUtils.isRecording ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        if (mode == CameraUtils.MODE_PHOTO) {
            mLlPicture.setBackgroundResource(R.drawable.bg_circle);
        }
        mTvTakePhoto.setText(mode == CameraUtils.MODE_MOVIE ? R.string.record : R.string.take_photo);
        mTvRecordTime.setVisibility(mode == CameraUtils.MODE_MOVIE ? View.VISIBLE : View.GONE);
        mIvTakePhoto.setBackgroundResource(mode == CameraUtils.MODE_MOVIE ? R.drawable.record : R.drawable.preview_picture);

        iv_record_wait.setBackgroundResource(mode == CameraUtils.MODE_MOVIE ? R.drawable.preview_picture_orange : R.drawable.record_orange);

    }

    @Override
    public void stopRecordTime() {
        mHandler.removeCallbacks(recordTimer);
        recordTime = 0;
        mTvRecordTime.setText("00:00:00");
    }

    @Override
    public void startRecordTime(int time) {
        recordTime = time;
        mHandler.removeCallbacks(recordTimer);
        mHandler.postDelayed(recordTimer, 1000);
    }

    private int recordTime = 0;
    private Runnable recordTimer = new Runnable() {
        @Override
        public void run() {
            //每分钟同步一下时间
            LogUtils.e("recordTime = " + recordTime);
            if (recordTime != 0 && recordTime % 60 == 0) {
                CameraUtils.getRecordTime(new CameraUtils.RecordTimeCallback() {
                    @Override
                    public void success(int time) {
                        if (CameraUtils.isRecording) {
                            startRecordTime(time);
                        } else {
                            stopRecordTime();
                        }
                    }

                    @Override
                    public void error(String error) {
                    }
                });
            }
            recordTime++;
            int hour = recordTime / 3600;
            int minute = recordTime % 3600 / 60;
            int seconds = recordTime % 60;
            mTvRecordTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds));
            mHandler.postDelayed(this, 1000);
        }
    };

    @Override
    public void pictureVisible(boolean visible) {
        mLlPicture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRecordState(boolean isRecord) {
        CameraUtils.isRecording = isRecord;
        iv_record.setVisibility(isRecord ? View.VISIBLE : View.GONE);
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
            mLlPicture.setBackgroundResource(!CameraUtils.isRecording ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        } else {
            mLlPicture.setBackgroundResource(CameraUtils.hasSDCard ? R.drawable.bg_circle : R.drawable.bg_circle);
        }
    }

    @Override
    public void isFishMode(boolean fishMode) {

    }

    @Override
    public void audioChange(boolean isOpen) {
        NovatekRepository.getInstance().setCurStateId(NovatekWifiCommands.MOVIE_RECORD_AUDIO, isOpen ? "1" : "0");
        mVoiceIsOpen = isOpen;
        mIvVoice.setBackgroundResource(mVoiceIsOpen ? R.drawable.voiced : R.drawable.mute);
        iv_voice_land.setBackgroundResource(mVoiceIsOpen ? R.drawable.voiced : R.drawable.mute);
    }

    @Override
    public void currentProduct(CameraUtils.PRODUCT product) {
        switch (product) {
            case SJ:
                mRgMode.setVisibility(View.GONE);
                mLlPicture.setBackgroundResource(R.drawable.bg_circle);
                ll_record_wait.setVisibility(View.INVISIBLE);
                mTvTakePhoto.setText(R.string.snapshot);
                mIvVoice.setVisibility(View.GONE);
                iv_voice_land.setVisibility(View.GONE);
                break;
            case DCT:
                String curAudio = NovatekRepository.getInstance().getCurStateId(NovatekWifiCommands.MOVIE_RECORD_AUDIO);
                mIvVoice.setVisibility(View.VISIBLE);
                mIvVoice.setBackgroundResource("0".equals(curAudio) ? R.drawable.mute : R.drawable.voiced);
                iv_voice_land.setVisibility(View.VISIBLE);
                iv_voice_land.setBackgroundResource("0".equals(curAudio) ? R.drawable.mute : R.drawable.voiced);
                break;
            default:
                mIvVoice.setVisibility(View.GONE);
                iv_voice_land.setVisibility(View.GONE);
                break;
        }
    }
}
