package com.adai.camera.hisi.preview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
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

import com.adai.camera.CameraFactory;
import com.adai.camera.hisi.HisiCamera;
import com.adai.camera.hisi.adapter.HisiResolutionAdapter;
import com.adai.camera.hisi.filemanager.HisiFilePhotoActivity;
import com.adai.camera.hisi.filemanager.HisiFileVideoActivity;
import com.adai.camera.hisi.sdk.Command;
import com.adai.camera.hisi.sdk.Common;
import com.adai.camera.hisi.setting.HisiSettingActivity;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.PlayerView;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.UIUtils;
import com.widget.piechart.ScreenUtils;

import java.util.Arrays;
import java.util.List;

import static com.adai.camera.hisi.sdk.Common.ERR_CHANNEL_BUSY;
import static com.adai.camera.hisi.sdk.Common.ERR_GET_CHANNEL_STATE_FAIL;
import static com.adai.camera.hisi.sdk.Common.ERR_LOOP_NO_SPACE;
import static com.adai.camera.hisi.sdk.Common.ERR_NO_SD;
import static com.adai.camera.hisi.sdk.Common.ERR_RECORD_NO_SPACE;
import static com.adai.camera.hisi.sdk.Common.ERR_SANPSHOT_NO_SPACE;
import static com.adai.camera.hisi.sdk.Common.ERR_SD_ERROR;
import static com.adai.camera.hisi.sdk.Common.ERR_SD_FULL;
import static com.adai.camera.hisi.sdk.Common.ERR_SNAPSHOT_PRARM_ERROR;
import static com.adai.camera.hisi.sdk.Common.ERR_START_CHANNEL_FAIL;
import static com.adai.camera.hisi.sdk.Common.ERR_STOP_CHANNEL_FAIL;
import static com.adai.camera.hisi.sdk.Common.FAILURE;
import static com.adai.camera.hisi.sdk.Common.SD_STATE_ERROR;
import static com.adai.camera.hisi.sdk.Common.SD_STATE_NONE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_BURST;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_CONTINUOUS;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_MULTI_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_SINGLE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_TIMER;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_LOOP;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_NORMAL;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_PHOTO;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_SLOW;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_IDLE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_RECORD;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_TIMELAPSE;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_TIMER;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_VIDEO_LOOP;
import static com.adai.camera.hisi.sdk.Common.WORK_STATE_VIDEO_TIMELAPSE;

/**
 * @author huangxy
 */
public class HisiPreviewActivity extends BaseActivity implements HisiPreviewContract.View, View.OnClickListener, PlayerView.OnChangeListener {
    private RelativeLayout mRlTitle;
    private TextView mBack;
    private TextView mHeadTitle, mTvRecordTime, mTvTakePhoto;
    private ImageView mIvSetting, mIvTakePhoto;
    private RelativeLayout mVideoFrame;
    private PlayerView mPvVideo;
    private ImageView mIvRecord;
    private ImageView mIvFullscreen;
    private TextView mTvNoCardNotice;
    private LinearLayout mLlResolution;
    private LinearLayout mLlCurResolution;
    private TextView mTvCurResolution;
    private RecyclerView mRvResolution;
    private RelativeLayout mRlBottom;
    private ImageView mIvPip;
    private LinearLayout mLlVideo;
    private LinearLayout mLlPhoto;
    private LinearLayout mLlPicture;
    private ImageView mIvHorizontalPip;
    private RadioGroup mRgMode;
    private RadioButton mRbVideoMode;
    private RadioButton mRbPhotoMode;
    private static final int MSG_COMMAND_RESULT = 0;
    private static final int MSG_STATE_INFO = 1;  //普通的状态更新消息，如SD卡，电量信息
    private static final int MSG_HIDE_SAVED = 2;  //隐藏“已保存”相关提示
    private static final int MSG_PLAYER_CONNECT = 3; // 连接点播
    private static final int MSG_SETTING_CHANGED = 4; //拍照设置改变
    private static final int MSG_STOP_TIMER = 5;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_STOP_TIMER:
                    stopSecondTimer();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean isPortrait = true;
    private HisiPreviewContract.Presenter mPresenter;
    private RelativeLayout.LayoutParams landscapeParams;
    private RelativeLayout.LayoutParams portraitParams;
    private RelativeLayout.LayoutParams mLlPicturePortraitParams, mLLPictureLandscapeParams;
    private RelativeLayout.LayoutParams mRecordWaitPortraitParams, mRecordWaitLandscapeParams;
    private RelativeLayout.LayoutParams mRecordPortraitParams, mRecordLandscapeParams;
    private SecondTimer secondTimer;

    RelativeLayout ll_record_wait;
    ImageView iv_record_wait;

    RelativeLayout horizontal_frame;
//    ImageView iv_fullscreen_land;

    RelativeLayout rl_novatek_camera;

    private void autoHideView() {
        mHandler.removeCallbacks(autoHideViewTask);
        mHandler.postDelayed(autoHideViewTask, 3000);
    }

    private java.lang.Runnable autoHideViewTask = new Runnable() {
        @Override
        public void run() {
            mIvFullscreen.setVisibility(View.GONE);
            mLlResolution.setVisibility(View.GONE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hisi_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new HisiPreviewPresenter();
        mPresenter.attachView(this);
        mPresenter.init();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mPvVideo != null) {
            mPvVideo.restartMediaPlayer();
        }
        mPresenter.onRestart();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPvVideo != null && mPvVideo.isPlaying()) {
            mPvVideo.stop();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopSecondTimer();
        mPresenter.onStop();
    }

    @Override
    protected void initView() {
        setTitle(SpUtils.getString(this, "SSID", ""));

        ll_record_wait = (RelativeLayout) findViewById(R.id.ll_record_wait);
        ll_record_wait.setOnClickListener(this);
        iv_record_wait = (ImageView) findViewById(R.id.iv_record_wait);

        horizontal_frame = (RelativeLayout) findViewById(R.id.horizontal_frame);
//        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
//        iv_fullscreen_land.setOnClickListener(this);

        rl_novatek_camera = (RelativeLayout) findViewById(R.id.rl_novatek_camera);

        mRlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        mBack = (TextView) findViewById(R.id.back);
        mHeadTitle = (TextView) findViewById(R.id.head_title);
        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mVideoFrame = (RelativeLayout) findViewById(R.id.video_frame);
        mPvVideo = (PlayerView) findViewById(R.id.pv_video);
        mPvVideo.setCurrentSize(PlayerView.SURFACE_FILL);
        mIvRecord = (ImageView) findViewById(R.id.iv_record);
        mIvFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        mTvNoCardNotice = (TextView) findViewById(R.id.tv_no_card_notice);
        mLlResolution = (LinearLayout) findViewById(R.id.ll_resolution);
        mLlCurResolution = (LinearLayout) findViewById(R.id.ll_cur_resolution);
        mTvCurResolution = (TextView) findViewById(R.id.tv_cur_resolution);
        mRvResolution = (RecyclerView) findViewById(R.id.rv_resolution);
        mRlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        mIvPip = (ImageView) findViewById(R.id.iv_pip);
        mLlVideo = (LinearLayout) findViewById(R.id.ll_video);
        mLlPhoto = (LinearLayout) findViewById(R.id.ll_photo);
        mLlPicture = (LinearLayout) findViewById(R.id.ll_picture);
        mIvHorizontalPip = (ImageView) findViewById(R.id.iv_horizontal_pip);
        mRgMode = (RadioGroup) findViewById(R.id.rg_mode);
        mRbVideoMode = (RadioButton) findViewById(R.id.rb_video_mode);
        mRbPhotoMode = (RadioButton) findViewById(R.id.rb_photo_mode);
        mTvRecordTime = (TextView) findViewById(R.id.tv_record_time);
        mTvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        AnimationDrawable drawable = (AnimationDrawable) mIvRecord.getDrawable();
        drawable.start();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        initParams(dm);
        mPresenter.connectSocket();
        mPresenter.initOrientation();
        autoHideView();
    }

    private void initParams(DisplayMetrics dm) {
        int width = dm.widthPixels;
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

    private void initEvent() {
        mBack.setOnClickListener(this);
        mLlPicture.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mLlPhoto.setOnClickListener(this);
        mLlVideo.setOnClickListener(this);
        mRbPhotoMode.setOnClickListener(this);
        mRbVideoMode.setOnClickListener(this);
        mIvPip.setOnClickListener(this);
        mIvHorizontalPip.setOnClickListener(this);
        mIvFullscreen.setOnClickListener(this);
        mVideoFrame.setOnClickListener(this);
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
        mRlBottom.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mRlTitle.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
//        mRgMode.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mIvFullscreen.setBackgroundResource(isPortrait ? R.drawable.selector_fullscreen : R.drawable.selector_exit_fullscreen);
        if (isPortrait) {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.white));
            mVideoFrame.setLayoutParams(portraitParams);
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            ll_record_wait.setLayoutParams(mRecordWaitPortraitParams);
            mLlPicture.setLayoutParams(mLlPicturePortraitParams);
            mIvRecord.setLayoutParams(mRecordPortraitParams);

        } else {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.dark_black));

            mVideoFrame.setLayoutParams(landscapeParams);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mLlPicture.setLayoutParams(mLLPictureLandscapeParams);
            ll_record_wait.setLayoutParams(mRecordWaitLandscapeParams);
            mIvRecord.setLayoutParams(mRecordLandscapeParams);
            mIvFullscreen.setVisibility(View.GONE);
        }
        mPvVideo.post(new Runnable() {
            @Override
            public void run() {
                mPvVideo.changeSurfaceSize();
            }
        });
    }

    @Override
    public Context getAttachedContext() {
        return this;
    }

    @Override
    public void showLoading() {
        showpDialog();
    }

    @Override
    public void hideLoading() {
        hidepDialog();
    }

    @Override
    public void exit() {
        finish();
    }

    @Override
    public void showAlertDialog() {

    }

    @Override
    public void respChangePip(int drawableRes) {

    }

    @Override
    public void showPip(int which) {

    }

    @Override
    public void stopPreview() {
        if (mPvVideo != null && mPvVideo.isPlaying()) {
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
        mPvVideo.initPlayer(CameraFactory.getInstance().getHisiCamera().getVideoRtspURL());
        // 第四步:设置事件监听，监听缓冲进度等
        mPvVideo.setOnChangeListener(this);
        // 第五步：开始播放
        mPvVideo.start();
    }

    @Override
    public void startTakePhoto() {

    }

    @Override
    public void takePhotoEnd() {

    }

    @Override
    public void pictureVisible(boolean visible) {
//        mLlPicture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showRecordState(boolean isRecord) {
        mIvRecord.setVisibility(isRecord ? View.VISIBLE : View.GONE);
        if (!isPhotoMode()) {
            mLlPicture.setBackgroundResource(!isRecord ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        }
    }

    @Override
    public void eventEnable(boolean b) {
        mEventEnable = b;
    }

    @Override
    public void modeChange(boolean isPhotoMode) {
        mRgMode.check(!isPhotoMode ? R.id.rb_video_mode : R.id.rb_photo_mode);
        if (isPhotoMode) {
            mLlPicture.setBackgroundResource(R.drawable.bg_circle);
            mIvRecord.setVisibility(View.GONE);
        } else {
            mLlPicture.setBackgroundResource(isRecording() ? R.drawable.bg_red_cicle : R.drawable.bg_circle);
        }
        mTvRecordTime.setVisibility(isPhotoMode ? View.GONE : View.VISIBLE);
        mTvTakePhoto.setText(!isPhotoMode ? R.string.record : R.string.take_photo);
        mIvTakePhoto.setBackgroundResource(!isPhotoMode ? R.drawable.record : R.drawable.preview_picture);
        iv_record_wait.setBackgroundResource(!isPhotoMode ? R.drawable.preview_picture_orange : R.drawable.record_orange);


    }

    @Override
    public void updateInfoBar() {
        int mode = CameraFactory.getInstance().getHisiCamera().mode;
        String entries = "";
        String value = "";
        switch (mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                entries = CameraFactory.getInstance().getHisiCamera().modeConfig.videoNormalResolutionValues;
                value = CameraFactory.getInstance().getHisiCamera().modeConfig.videoNormalResolution;
                break;
            case WORK_MODE_PHOTO_SINGLE:
                entries = CameraFactory.getInstance().getHisiCamera().modeConfig.photoSingleResolutionValues;
                value = CameraFactory.getInstance().getHisiCamera().modeConfig.photoSingleResolution;
                break;
            default:
                break;
        }
        mTvCurResolution.setText(value);
        if (!TextUtils.isEmpty(entries)) {
            String[] array = entries.split(",");
            List<String> menuItem = Arrays.asList(array);
            HisiResolutionAdapter hisiResolutionAdapter = new HisiResolutionAdapter(mode, menuItem);
            mRvResolution.setLayoutManager(new LinearLayoutManager(this));
            mRvResolution.setAdapter(hisiResolutionAdapter);
            hisiResolutionAdapter.setOnItemClickListener(new HisiResolutionAdapter.ItemClickListener() {
                @Override
                public void onItemClick(int mode, String value) {
                    mPresenter.setResolution(mode, value);
                }
            });

        }
    }

    /**
     * 通用 秒计时器; 用于录像、定时拍照、延时拍照的计时及相关界面的刷新
     */
    class SecondTimer extends CountDownTimer {
        int time; //录像，定时拍照时表示已进行的秒数； 延时拍照时表示倒计时秒数

        public SecondTimer(int sec) {
            super(Long.MAX_VALUE, 1000);
            time = sec;
        }

        @Override
        public void onTick(long l) {
            boolean bCacelTime = false;
            switch (CameraFactory.getInstance().getHisiCamera().workState) {
                case WORK_STATE_VIDEO_TIMELAPSE:
                case WORK_STATE_VIDEO_LOOP:
                case WORK_STATE_RECORD:
                    mTvRecordTime.setText(time2String(time));
                    time++;
                    break;

                case WORK_STATE_TIMELAPSE:
//                    tvRecordTime.setText(String.format("%s/%dP",
//                            time2String(time), time / G.dv.prefer.timelapseInterval));
//                    time++;
                    break;

                case WORK_STATE_TIMER:
                    if (time <= 0) {
                        CameraFactory.getInstance().getHisiCamera().workState = WORK_STATE_IDLE;
//                        tvCountDownTime.setText("");
//                        updateUIByModeAndWorkState();
//                        playSound(HisiPreviewActivity.this, SOUND_CAMERA_CLICK);
//                        promptSaved();
                        bCacelTime = true;
                    }
//                    tvCountDownTime.setText(Integer.toString(time));
                    time--;
                    break;

                default:
                    bCacelTime = true;
                    break;
            }
            if (bCacelTime) {
                mHandler.sendEmptyMessage(MSG_STOP_TIMER);
            }
        }

        @Override
        public void onFinish() {
        }
    }

    /**
     * 输入视频时长的秒数，转换成"1:25:38"样式字符串
     */
    public static String time2String(int time) {

        int hour, min, second;

        hour = time / 3600;
        time = time % 3600;

        min = time / 60;
        time = time % 60;

        second = time;

        return String.format("%02d:%02d:%02d", hour, min, second);
    }

    /**
     * 开始秒定时器
     *
     * @param time 录像，定时拍照时应为已进行的时间，延时拍照时为剩余秒数
     */
    @Override
    public void startSecondTimer(int time) {
        if (secondTimer != null) {
            return;
        }
        secondTimer = new SecondTimer(time);
        secondTimer.start();
    }

    /**
     * 停止秒计时器
     */
    @Override
    public void stopSecondTimer() {
        if (null != secondTimer) {
            secondTimer.cancel();
            secondTimer = null;
            mTvRecordTime.setText(time2String(0));
        }
    }

    private boolean mEventEnable;

    @Override
    public void onClick(View v) {
        if (!mEventEnable) {
            return;
        }
        switch (v.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ll_record_wait:
                if (isPhotoMode()) {
                    mPresenter.changeMode(WORK_MODE_VIDEO_NORMAL);
                } else {
                    mPresenter.changeMode(WORK_MODE_PHOTO_SINGLE);
                }
                break;
            case R.id.iv_fullscreen:
                setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.iv_setting:
                gotoSetting();
                break;
            case R.id.ll_video:
                gotoFileManager(HisiFileVideoActivity.class);
                break;
            case R.id.ll_photo:
                gotoFileManager(HisiFilePhotoActivity.class);
                break;
            case R.id.ll_picture:
                mPresenter.recordShot();
                break;
            case R.id.rb_video_mode:
                mPresenter.changeMode(WORK_MODE_VIDEO_NORMAL);
                break;
            case R.id.rb_photo_mode:
                mPresenter.changeMode(WORK_MODE_PHOTO_SINGLE);
                break;
            case R.id.iv_pip:
            case R.id.iv_horizontal_pip:
                break;
            case R.id.video_frame:
                if (mIvFullscreen.getVisibility() == View.VISIBLE) {
                    mIvFullscreen.setVisibility(View.GONE);
                    mLlResolution.setVisibility(View.GONE);
                    mHandler.removeCallbacks(autoHideViewTask);
                } else {
                    mRvResolution.setVisibility(View.GONE);
                    mIvFullscreen.setVisibility(View.VISIBLE);
                    mLlResolution.setVisibility(View.VISIBLE);
                    autoHideView();
                }
                break;
            case R.id.ll_resolution:
                mRvResolution.setVisibility(mRvResolution.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                mRvResolution.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = mRvResolution.getHeight();
                        if (height > ScreenUtils.dp2px(HisiPreviewActivity.this, 96)) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mRvResolution.getLayoutParams();
                            layoutParams.height = ScreenUtils.dp2px(HisiPreviewActivity.this, 96);
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

    private void gotoFileManager(Class<?> clz) {
        if (CameraFactory.getInstance().getHisiCamera().sdCardInfo != null) {
            int state = CameraFactory.getInstance().getHisiCamera().sdCardInfo.sdState;
            if (state == SD_STATE_ERROR || state == SD_STATE_NONE) {
                showToast(R.string.error_no_sd);
                return;
            }
        }
        if (isRecording()) {
            new GotoFileManagerTask(clz).execute();
        } else {
            Intent intent = new Intent(HisiPreviewActivity.this, clz);
            startActivity(intent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GotoFileManagerTask extends AsyncTask<Void, Void, Common.Result> {
        private Class<?> mClass;

        GotoFileManagerTask(Class<?> clz) {
            mClass = clz;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected Common.Result doInBackground(Void... voids) {
            return CameraFactory.getInstance().getHisiCamera().executeCommand(Command.ACTION_RECORD_STOP);
        }

        @Override
        protected void onPostExecute(Common.Result result) {
            super.onPostExecute(result);
            processMsgCommand(result);
            hideLoading();
            UIUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventEnable(true);
                }
            }, 1000);
            if (result.returnCode != FAILURE) {
                Intent intent = new Intent(HisiPreviewActivity.this, mClass);
                startActivity(intent);
            }
        }
    }

    private void gotoSetting() {
        if (isRecording()) {
            new GotoSettingTask().execute();
        } else {
            Intent intent = new Intent(HisiPreviewActivity.this, HisiSettingActivity.class);
            intent.putExtra(HisiSettingActivity.IS_PHOTO_MODE, isPhotoMode());
            startActivity(intent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GotoSettingTask extends AsyncTask<Void, Void, Common.Result> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected Common.Result doInBackground(Void... voids) {
            return CameraFactory.getInstance().getHisiCamera().executeCommand(Command.ACTION_RECORD_STOP);
        }

        @Override
        protected void onPostExecute(Common.Result result) {
            super.onPostExecute(result);
            processMsgCommand(result);
            hideLoading();
            UIUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventEnable(true);
                }
            }, 1000);
            if (result.returnCode != FAILURE) {
                Intent intent = new Intent(HisiPreviewActivity.this, HisiSettingActivity.class);
                intent.putExtra(HisiSettingActivity.IS_PHOTO_MODE, isPhotoMode());
                startActivity(intent);
            }
        }
    }


    private void updateUIByModeAndWorkState() {
        UIUtils.post(new Runnable() {
            @Override
            public void run() {
                updateInfoBar();
                updateCommandBar();
            }
        });
    }

    /**
     * 更新底部命令栏
     */
    private void updateCommandBar() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        switch (CameraFactory.getInstance().getHisiCamera().mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    showRecordState(true);
                    modeChange(false);
                } else {
//                    executeCommandAndSendResult(Command.ACTION_RECORD_START);
                    showRecordState(false);
                    modeChange(false);
                }
                break;
            case WORK_MODE_PHOTO_SINGLE:
                modeChange(true);
                break;
            case WORK_MODE_MULTI_BURST:
                modeChange(true);
                break;

            case WORK_MODE_MULTI_TIMELAPSE:
                if (dv.workState == WORK_STATE_TIMELAPSE) {
                    modeChange(true);
                } else {
                    modeChange(true);
                }
                break;

            case WORK_MODE_PHOTO_TIMER:
                if (dv.workState == WORK_STATE_TIMER) {
                    modeChange(true);
                } else {
                    modeChange(true);
                }
                break;

            case WORK_MODE_MULTI_CONTINUOUS:
                modeChange(true);
                break;
            default:
                break;
        }
    }

    /**
     * 处理命令执行结果的消息
     */
    private void processMsgCommand(Common.Result result) {
        if (result.returnCode == FAILURE) {
            showToast(commandError2String(result.errorCode));
            updateUIByModeAndWorkState();
            return;
        }
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        switch (result.cmd) {
            case Command.ACTION_VIDEO_SNAP_STOP:
            case Command.ACTION_VIDEO_SLOW_STOP:
            case Command.ACTION_VIDEO_COMMON_STOP:
            case Command.ACTION_VIDEO_LOOP_STOP:
            case Command.ACTION_RECORD_STOP:
                stopSecondTimer();
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_PHOTO:
                showToast(R.string.takephoto_sucess);
                break;

            case Command.ACTION_VIDEO_SNAP_START:
            case Command.ACTION_VIDEO_SLOW_START:
            case Command.ACTION_VIDEO_COMMON_START:
            case Command.ACTION_VIDEO_LOOP_START:
            case Command.ACTION_RECORD_START:
                dv.workState = WORK_STATE_RECORD;
                startSecondTimer(0);
                break;

            case Command.ACTION_TIMELAPSE_START:
                dv.workState = WORK_STATE_TIMELAPSE;
                startSecondTimer(0);
                break;

            case Command.ACTION_TIMER_START:
                dv.workState = WORK_STATE_TIMER;
                int nTime = dv.prefer.timerCountDown;
                startSecondTimer(nTime);
                break;

            case Command.ACTION_TIMELAPSE_STOP:
                stopSecondTimer();
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_BURST:
                break;

            case Command.ACTION_TIMER_STOP:
                stopSecondTimer();
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_VIDEO_TIMELAPSE_START:
                dv.workState = WORK_STATE_VIDEO_TIMELAPSE;
                startSecondTimer(0);
                break;

            case Command.ACTION_VIDEO_TIMELAPSE_STOP:
                stopSecondTimer();
                dv.workState = WORK_STATE_IDLE;
                break;
            default:
                break;
        }
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    showRecordState(true);
                } else {
                    showRecordState(false);
                }
            default:
                break;
        }
    }

    private String commandError2String(int errorCode) {
        int errorRes;
        switch (errorCode) {
            case ERR_NO_SD:
                errorRes = R.string.error_no_sd;
                break;

            case ERR_SD_FULL:
                errorRes = R.string.error_sd_full;
                break;

            case ERR_SD_ERROR:
                errorRes = R.string.error_sd_error;
                break;

            case ERR_RECORD_NO_SPACE:
                errorRes = R.string.error_record_no_space;
                break;

            case ERR_LOOP_NO_SPACE:
                errorRes = R.string.error_loop_no_space;
                break;

            case ERR_SANPSHOT_NO_SPACE:
                errorRes = R.string.error_snapshot_no_space;
                break;

            case ERR_GET_CHANNEL_STATE_FAIL:
                errorRes = R.string.error_get_channel_state_fail;
                break;

            case ERR_CHANNEL_BUSY:
                errorRes = R.string.error_channel_busy;
                break;

            case ERR_START_CHANNEL_FAIL:
                errorRes = R.string.error_start_channel_fail;
                break;

            case ERR_STOP_CHANNEL_FAIL:
                errorRes = R.string.error_stop_channel_fail;
                break;

            case ERR_SNAPSHOT_PRARM_ERROR:
                errorRes = R.string.error_snapshot_param_error;
                break;

            default:
                errorRes = R.string.operation_failed;
                break;
        }

        return UIUtils.getString(errorRes);
    }

    private boolean isPhotoMode() {
        int mode = CameraFactory.getInstance().getHisiCamera().mode;
        switch (mode) {
            case WORK_MODE_PHOTO_SINGLE:
            case WORK_MODE_MULTI_BURST:
            case WORK_MODE_MULTI_TIMELAPSE:
            case WORK_MODE_PHOTO_TIMER:
            case WORK_MODE_MULTI_CONTINUOUS:
                return true;
            default:
                return false;
        }
    }

    private boolean isRecording() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        if (!dv.supportWorkMode()) {
            return false;
        }
        boolean isRecording;
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
//                    showToast(R.string.wifi_stoprecordingbef);
                    isRecording = true;
                } else {
                    isRecording = false;
                }
                break;
            default:
                isRecording = false;
                break;
        }
        return isRecording;
    }

    @Override
    public void showLoading(String string) {
        showpDialog(string);
    }

    @Override
    public void showLoading(int res) {
        showpDialog(res);
    }

    @Override
    public void showToast(String string) {
        super.showToast(string);
    }

    @Override
    public void showToast(int res) {
        super.showToast(res);
    }

    @Override
    public void onBufferChanged(float buffer) {
        mPresenter.onBufferChanged(buffer);
    }

    @Override
    public void onLoadComplete() {
        mPresenter.onLoadComplete();
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

    }

    @Override
    public void onEnd() {
        mPresenter.onEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
