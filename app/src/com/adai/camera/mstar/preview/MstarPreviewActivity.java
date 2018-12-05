package com.adai.camera.mstar.preview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.mstar.adapter.MstarResolutionAdapter;
import com.adai.camera.mstar.data.MstarRepository;
import com.adai.camera.mstar.filemanager.MstarFilePhotoActivity;
import com.adai.camera.mstar.filemanager.MstarFileVideoActivity;
import com.adai.camera.mstar.setting.MstarSettingActivity;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.PlayerView;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.widget.piechart.ScreenUtils;

import java.util.List;


/**
 * @author huangxy
 */
public class MstarPreviewActivity extends BaseActivity implements MstarPreviewContract.View, View.OnClickListener, PlayerView.OnChangeListener {
    private RelativeLayout mRlTitle;
    private LinearLayout mLlPicture, mLlVideo, mLlPhoto, mLlResolution, mLlCurResolution;
    private TextView mHeadTitle, mTvResolution, mTvNoCardNotice, mTvTakePhoto;
    private ImageView mIvSetting, mIvTakePhoto;
    private PlayerView mPvVideo;
    private RelativeLayout mRlBottom, video_frame;
    private RecyclerView mRvResolution;
    private MstarPreviewContract.Presenter mPresenter;
    private RelativeLayout.LayoutParams landscapeParams;
    private RelativeLayout.LayoutParams portraitParams;
    private RelativeLayout.LayoutParams mLlPicturePortraitParams, mLLPictureLandscapeParams;
    private RelativeLayout.LayoutParams mRecordWaitPortraitParams, mRecordWaitLandscapeParams;
    private RelativeLayout.LayoutParams mRecordPortraitParams, mRecordLandscapeParams;
    private RadioButton mRbVideoMode, mRbPhotoMode;
    private RadioGroup mRgMode;
    private ImageView mIvPip, mIvHorizontalPip, iv_record, iv_fullscreen;
    private boolean isPortrait = true;
    private Handler mHandler = new Handler();

    RelativeLayout ll_record_wait;
    ImageView iv_record_wait;

    RelativeLayout horizontal_frame;
    ImageView iv_fullscreen_land;

    RelativeLayout rl_novatek_camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new MstarPreviewPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.onResume();
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
        landscapeParams = new RelativeLayout.LayoutParams(height - ScreenUtils.dp2px(this, 110), width);
        landscapeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        portraitParams = new RelativeLayout.LayoutParams(width, width * 9 / 16);
        portraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        portraitParams.addRule(RelativeLayout.BELOW, R.id.rl_title);

        mRecordWaitPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitPortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mRecordWaitPortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRecordWaitPortraitParams.bottomMargin = ScreenUtils.dp2px(this, 10);

        mRecordWaitLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ABOVE, R.id.ll_picture);
        mRecordWaitLandscapeParams.bottomMargin = ScreenUtils.dp2px(this, 20);
        mRecordWaitLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 40);

        mLlPicturePortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLlPicturePortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mLlPicturePortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mLlPicturePortraitParams.bottomMargin = ScreenUtils.dp2px(this, 50);
        mLLPictureLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLLPictureLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mLLPictureLandscapeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mLLPictureLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 10);

        mRecordPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
        mRecordPortraitParams.topMargin = ScreenUtils.dp2px(this, 10);
        mRecordPortraitParams.leftMargin = ScreenUtils.dp2px(this, 10);

        mRecordLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
        mRecordLandscapeParams.topMargin = ScreenUtils.dp2px(this, 40);
        mRecordLandscapeParams.leftMargin = ScreenUtils.dp2px(this, 20);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(SpUtils.getString(this, "SSID", ""));
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
        mLlResolution = (LinearLayout) findViewById(R.id.ll_resolution);
        mLlCurResolution = (LinearLayout) findViewById(R.id.ll_cur_resolution);
        mTvResolution = (TextView) findViewById(R.id.tv_cur_resolution);
        mRvResolution = (RecyclerView) findViewById(R.id.rv_resolution);
        mTvNoCardNotice = (TextView) findViewById(R.id.tv_no_card_notice);
        mTvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);

        ll_record_wait = (RelativeLayout) findViewById(R.id.ll_record_wait);
        ll_record_wait.setOnClickListener(this);
        iv_record_wait = (ImageView) findViewById(R.id.iv_record_wait);

        horizontal_frame = (RelativeLayout) findViewById(R.id.horizontal_frame);
        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
        iv_fullscreen_land.setOnClickListener(this);

        rl_novatek_camera = (RelativeLayout) findViewById(R.id.rl_novatek_camera);

        AnimationDrawable drawable = (AnimationDrawable) iv_record.getDrawable();
        drawable.start();
        mIvHorizontalPip = (ImageView) findViewById(R.id.iv_horizontal_pip);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        initParams(dm);
        showLoading(getString(R.string.in_the_buffer));
        mPresenter.initOrientation();
        mPresenter.init();
        autoHideView();
    }

    private void initEvent() {
        mLlPicture.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mLlPhoto.setOnClickListener(this);
        mLlVideo.setOnClickListener(this);
        mRbPhotoMode.setOnClickListener(this);
        mRbVideoMode.setOnClickListener(this);
        mIvPip.setOnClickListener(this);
//        mPvVideo.setOnClickListener(this);
        mIvHorizontalPip.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        video_frame.setOnClickListener(this);
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
        mPresenter.onConfigurationChanged(newConfig);
    }

    @Override
    public void changeOrientation(boolean isPortrait) {
        this.isPortrait = isPortrait;
        horizontal_frame.setVisibility(isPortrait ? View.GONE : View.VISIBLE);
        mRlBottom.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mRlTitle.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
//        mRgMode.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
//        iv_fullscreen.setBackgroundResource(isPortrait ? R.drawable.selector_fullscreen : R.drawable.selector_exit_fullscreen);
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
            video_frame.setLayoutParams(landscapeParams);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mLlPicture.setLayoutParams(mLLPictureLandscapeParams);
            ll_record_wait.setLayoutParams(mRecordWaitLandscapeParams);
            iv_record.setLayoutParams(mRecordLandscapeParams);
            iv_fullscreen.setVisibility(View.GONE);
        }
        mPvVideo.post(new Runnable() {
            @Override
            public void run() {
                mPvVideo.changeSurfaceSize();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPvVideo != null) {
            LogUtils.e("onPause");
//            mPvVideo.stop();
            mPvVideo.restartMediaPlayer();
        }
        mPresenter.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setCurrentLanguage();
        mPvVideo.restartMediaPlayer();
        mPresenter.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
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
                WifiUtil.getInstance().gotoWifiSetting(mContext);
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                MstarPreviewActivity.this.finish();
            }
        });
        builder.setCancelable(false).create().show();
    }

    @Override
    public void onBufferChanged(float buffer) {
        mPresenter.onBufferChanged(buffer);
    }

    @Override
    public void onLoadComplete() {
        mPresenter.onLoadComplete();
        changeResolution();
    }

    private void changeResolution() {
        int menuId;
        if (MstarCamera.CUR_MODE == MstarCamera.MODE_MOVIE) {
            menuId = MstarRepository.MENU_ID.menuVIDEO_RES;
            MstarRepository.Menu videoMenu = MstarRepository.getInstance().GetAutoMenu(MstarRepository.MENU_ID.menuVIDEO_RES);
            if (videoMenu != null && !TextUtils.isEmpty(MstarRepository.getInstance().getVideoresRet())) {
                mLlCurResolution.setVisibility(View.VISIBLE);
                mTvResolution.setText(MstarRepository.getInstance().getVideoresRet());
            } else {
                mLlCurResolution.setVisibility(View.GONE);
            }
        } else {
            menuId = MstarRepository.MENU_ID.menuIMAGE_RES;
            String imageresRet = MstarRepository.getInstance().getImageresRet();
            MstarRepository.Menu imageResMenu = MstarRepository.getInstance().GetAutoMenu(MstarRepository.MENU_ID.menuIMAGE_RES);
            if (imageResMenu != null && !TextUtils.isEmpty(imageresRet)) {
                mLlCurResolution.setVisibility(View.VISIBLE);
                mTvResolution.setText(MstarRepository.getInstance().getImageresRet());
            } else {
                mLlCurResolution.setVisibility(View.GONE);
            }
        }
        final MstarRepository.Menu menu = MstarRepository.getInstance().GetAutoMenu(menuId);
        if (menu != null) {
            List<String> menus = menu.GetMenuItemIdList();
            if (menus != null && menus.size() > 0) {
                MstarResolutionAdapter mstarResolutionAdapter = new MstarResolutionAdapter(menuId);
                mRvResolution.setLayoutManager(new LinearLayoutManager(this));
                mRvResolution.setAdapter(mstarResolutionAdapter);
                mstarResolutionAdapter.setOnItemClickListener(new MstarResolutionAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(final int menuId, final String param) {
                        if (param.equals(mTvResolution.getText().toString())) {
                            return;
                        }
                        showLoading(R.string.please_wait);
                        if (MstarCamera.IS_RECORDING) {
                            CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
                                @Override
                                public void onResponse(String response) {
                                    if (response != null && response.contains("OK")) {
                                        MstarCamera.IS_RECORDING = false;
                                        showRecordState(false);
                                        CameraCommand.asynSendRequest(CameraCommand.commandSetUrl(menuId, param), new CameraCommand.RequestListener() {
                                            @Override
                                            public void onResponse(String response) {
                                                if (CameraCommand.checkResponse(response)) {
                                                    showToast(R.string.set_success);
                                                } else {
                                                    showToast(R.string.set_failure);
                                                }
                                                if (menuId == MstarRepository.MENU_ID.menuVIDEO_RES) {
                                                    MstarRepository.getInstance().setVideoresRet(param);
                                                } else if (menuId == MstarRepository.MENU_ID.menuIMAGE_RES) {
                                                    MstarRepository.getInstance().setImageresRet(param);
                                                }
                                                mTvResolution.setText(param);
                                                CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
                                                    @Override
                                                    public void onResponse(String response) {
                                                        MstarCamera.IS_RECORDING = true;
                                                        showRecordState(true);
                                                        hidepDialog();
                                                    }

                                                    @Override
                                                    public void onErrorResponse(String message) {
                                                        hidepDialog();
                                                        showToast(R.string.start_record_failed);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onErrorResponse(String message) {
                                                showToast(R.string.set_failure);
                                                hidepDialog();
                                            }
                                        });
                                    } else {
                                        hideLoading();
                                        showToast(R.string.stop_recording_failed);
                                    }
                                }

                                @Override
                                public void onErrorResponse(String message) {
                                    hideLoading();
                                }
                            });
                        } else {
                            CameraCommand.asynSendRequest(CameraCommand.commandSetUrl(menuId, param), new CameraCommand.RequestListener() {
                                @Override
                                public void onResponse(String response) {
                                    if (CameraCommand.checkResponse(response)) {
                                        showToast(R.string.set_success);
                                    } else {
                                        showToast(R.string.set_failure);
                                    }
                                    if (menuId == MstarRepository.MENU_ID.menuVIDEO_RES) {
                                        MstarRepository.getInstance().setVideoresRet(param);
                                    } else if (menuId == MstarRepository.MENU_ID.menuIMAGE_RES) {
                                        MstarRepository.getInstance().setImageresRet(param);
                                    }
                                    mTvResolution.setText(param);
                                    CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            MstarCamera.IS_RECORDING = true;
                                            showRecordState(true);
                                            hidepDialog();
                                        }

                                        @Override
                                        public void onErrorResponse(String message) {
                                            hidepDialog();
                                            showToast(R.string.start_record_failed);
                                        }
                                    });
                                }

                                @Override
                                public void onErrorResponse(String message) {
                                    showToast(R.string.set_failure);
                                    hidepDialog();
                                }
                            });
                        }

                    }
                });
            }

        }
    }

    @Override
    public void onError() {
        mPresenter.onError();
    }

    @Override
    public void onPlayerError() {
        mPresenter.onPlayError();
    }

    @Override
    public void onStartPlay() {
        mPresenter.onStartPlay();
    }

    @Override
    public void onPlayNothing() {
        mPresenter.onPlayError();
    }

    @Override
    public void onEnd() {
        mPresenter.onEnd();
//        mPvVideo.restartMediaPlayer();
//        // 第五步：开始播放
//        mPvVideo.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_record_wait:
                if (MstarCamera.CUR_MODE == MstarCamera.MODE_MOVIE) {
                    mPresenter.changeMode(MstarCamera.MODE_PHOTO);
                } else {
                    mPresenter.changeMode(MstarCamera.MODE_MOVIE);
                }
                break;
            case R.id.iv_fullscreen:
            case R.id.iv_fullscreen_land:
                setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.iv_setting:
                if (MstarCamera.IS_RECORDING) {
                    showLoading(R.string.please_wait);
                    CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
                        @Override
                        public void onResponse(String response) {
                            hideLoading();
                            if (response != null && response.contains("OK")) {
                                hideLoading();
                                MstarCamera.IS_RECORDING = false;
                                showRecordState(false);
                                startActivity(MstarSettingActivity.class);
                            } else {
                                showToast(R.string.stop_recording_failed);
                            }
                        }

                        @Override
                        public void onErrorResponse(String message) {
                            hideLoading();
                        }
                    });
                } else {
                    startActivity(MstarSettingActivity.class);
                }
                break;
            case R.id.ll_video:
                startActivity(MstarFileVideoActivity.class);
                break;
            case R.id.ll_photo:
                startActivity(MstarFilePhotoActivity.class);
//                FileGridActivity.actionStart(this, 3, FileManagerConstant.TYPE_LOCAL_PICTURE, VLCApplication.DOWNLOADPATH);
                break;
            case R.id.ll_picture:
//                if (CameraUtils.hasSDCard) {
//                    mNovatekPreviewPresenter.takePhoto();
//                } else {
//                    showToast(R.string.wifi_sdcard);
//                }
                mPresenter.recordShot();
                break;
            case R.id.rb_video_mode:
                mPresenter.changeMode(MstarCamera.MODE_MOVIE);
                break;
            case R.id.rb_photo_mode:
                mPresenter.changeMode(MstarCamera.MODE_PHOTO);
                break;
            case R.id.iv_pip:
            case R.id.iv_horizontal_pip:
                mPresenter.switchPip();
                break;
            case R.id.video_frame:
                if (mLlResolution.getVisibility() == View.VISIBLE) {
                    iv_fullscreen.setVisibility(View.GONE);
                    mLlResolution.setVisibility(View.GONE);
                    mHandler.removeCallbacks(autoHideViewTask);
                } else {
                    mRvResolution.setVisibility(View.GONE);
                    if (isPortrait) {
                        iv_fullscreen.setVisibility(View.VISIBLE);
                    } else {
                        iv_fullscreen.setVisibility(View.GONE);
                    }
                    mLlResolution.setVisibility(View.VISIBLE);
                    autoHideView();
                }

                break;
            case R.id.ll_resolution:
                mRvResolution.setVisibility(mRvResolution.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                int height = mRvResolution.getHeight();
                if (height > ScreenUtils.dp2px(this, 96)) {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mRvResolution.getLayoutParams();
                    layoutParams.height = ScreenUtils.dp2px(this, 96);
                    mRvResolution.setLayoutParams(layoutParams);
                }
                autoHideView();
                break;
            default:
                break;
        }
    }

    private void autoHideView() {
        mHandler.removeCallbacks(autoHideViewTask);
        mHandler.postDelayed(autoHideViewTask, 3000);
    }

    private java.lang.Runnable autoHideViewTask = new Runnable() {
        @Override
        public void run() {
            iv_fullscreen.setVisibility(View.GONE);
            mLlResolution.setVisibility(View.GONE);
        }
    };

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
    public void initPlayView() {
        mPvVideo.initPlayer(MstarCamera.URL_STREAM);
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
        LogUtils.e("currentMode = " + mode);
        MstarCamera.CUR_MODE = mode;
        mRgMode.check(mode == MstarCamera.MODE_MOVIE ? R.id.rb_video_mode : R.id.rb_photo_mode);
        if (mode == MstarCamera.MODE_MOVIE) {
            mLlPicture.setBackgroundResource(MstarCamera.IS_RECORDING ? R.drawable.bg_red_cicle : R.drawable.bg_circle);
        } else {
            mLlPicture.setBackgroundResource(R.drawable.bg_circle);
        }
        mTvTakePhoto.setText(mode == MstarCamera.MODE_MOVIE ? R.string.record : R.string.take_photo);
        mIvTakePhoto.setBackgroundResource(mode == MstarCamera.MODE_MOVIE ? R.drawable.record : R.drawable.preview_picture);
        iv_record_wait.setBackgroundResource(mode == MstarCamera.MODE_MOVIE ? R.drawable.preview_picture_orange : R.drawable.record_orange);

        changeResolution();
    }

    @Override
    public void pictureVisible(boolean visible) {
        mLlPicture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRecordState(boolean isRecord) {
        iv_record.setVisibility(isRecord ? View.VISIBLE : View.GONE);
        mLlPicture.setBackgroundResource(!MstarCamera.IS_RECORDING ? R.drawable.bg_circle : R.drawable.bg_red_cicle);

        if (MstarCamera.CUR_MODE == MstarCamera.MODE_MOVIE) {
            mLlPicture.setBackgroundResource(!MstarCamera.IS_RECORDING ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        } else {
            mLlPicture.setBackgroundResource(R.drawable.bg_circle);
        }

    }
}
