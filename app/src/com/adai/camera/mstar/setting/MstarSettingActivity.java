package com.adai.camera.mstar.setting;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.mstar.data.MstarRepository;
import com.adai.camera.mstar.setting.subsetting.MstarPasswordSettingActivity;
import com.adai.camera.mstar.setting.subsetting.MstarSubSettingActivity;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.kyleduo.switchbutton.SwitchButton;


public class MstarSettingActivity extends BaseActivity implements MstarSettingContract.View, View.OnClickListener {
    private RelativeLayout mXcCamera;
    private RelativeLayout mRlCameraParams;
    private ImageView mIvCameraParams;
    private LinearLayout mLlCameraParams;
    private LinearLayout mLlRecordMode;
    private RelativeLayout mRlSoundRecording;
    private SwitchButton mSbSoundRecording;
    private RelativeLayout mRlTimeWatermark;
    private SwitchButton mSbTimeWatermarkSwitch;
    private RelativeLayout mRlMotionDetection;
    private TextView mTvMotionDetection;
    private RelativeLayout mRlCyclicRecord;
    private ImageView mIvCyclicRecord;
    private TextView mTvCyclicRecord;
    private RelativeLayout mRlGsensorLevel;
    private TextView mTextView1;
    private ImageView mImageView1;
    private TextView mTvCamSensorLevel;
    private RelativeLayout mRlCameraQuality;
    private TextView mTextView3;
    private ImageView mImageView3;
    private TextView mTvCameraQuality;
    private LinearLayout mLlPhotoMode;
    private RelativeLayout mRlPhotoResolution;
    private ImageView mIvPhotoResolution;
    private TextView mTvPhotoResolution;
    private RelativeLayout mRlEv;
    private TextView mTextView2;
    private ImageView mImageView2;
    private TextView mTvEv;
    private RelativeLayout mRlWb;
    private TextView mTextView4;
    private ImageView mImageView4;
    private TextView mTvWb;
    private RelativeLayout mRlIso;
    private TextView mTextView5;
    private ImageView mImageView5;
    private TextView mTvIso;
    private RelativeLayout mRlSharpness;
    private TextView mTextView6;
    private ImageView mImageView6;
    private TextView mTvSharpness;
    private RelativeLayout mRlWdr;
    private SwitchButton mSbWdr;
    private RelativeLayout mRlFormat;
    private RelativeLayout mRlRecovery;
    private TextView mTvCameraVersion;
    private MstarSettingContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_settings);
        init();
        initView();
        initEvent();
    }

    private void initEvent() {
        mXcCamera.setOnClickListener(this);
        mRlCameraQuality.setOnClickListener(this);
        mRlPhotoResolution.setOnClickListener(this);
        mRlWb.setOnClickListener(this);
        mRlEv.setOnClickListener(this);
        mRlMotionDetection.setOnClickListener(this);
        mRlGsensorLevel.setOnClickListener(this);
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new MstarSettingPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.setting);
        mXcCamera = (RelativeLayout) findViewById(R.id.xc_camera);
        mRlCameraParams = (RelativeLayout) findViewById(R.id.rl_camera_params);
        mIvCameraParams = (ImageView) findViewById(R.id.iv_camera_params);
        mLlCameraParams = (LinearLayout) findViewById(R.id.ll_camera_params);
        mLlRecordMode = (LinearLayout) findViewById(R.id.ll_record_mode);
        mRlSoundRecording = (RelativeLayout) findViewById(R.id.rl_sound_recording);
        mSbSoundRecording = (SwitchButton) findViewById(R.id.sb_sound_recording);
        mRlTimeWatermark = (RelativeLayout) findViewById(R.id.rl_time_watermark);
        mSbTimeWatermarkSwitch = (SwitchButton) findViewById(R.id.sb_time_watermark_switch);
        mRlMotionDetection = (RelativeLayout) findViewById(R.id.rl_motion_detection);
        mTvMotionDetection = (TextView) findViewById(R.id.tv_motion_detection);
        mRlCyclicRecord = (RelativeLayout) findViewById(R.id.rl_cyclic_record);
        mIvCyclicRecord = (ImageView) findViewById(R.id.iv_cyclic_record);
        mTvCyclicRecord = (TextView) findViewById(R.id.tv_cyclic_record);
        mRlGsensorLevel = (RelativeLayout) findViewById(R.id.rl_gsensor_level);
        mTextView1 = (TextView) findViewById(R.id.textView1);
        mImageView1 = (ImageView) findViewById(R.id.imageView1);
        mTvCamSensorLevel = (TextView) findViewById(R.id.tv_cam_sensor_level);
        mRlCameraQuality = (RelativeLayout) findViewById(R.id.rl_camera_quality);
        mTextView3 = (TextView) findViewById(R.id.textView3);
        mImageView3 = (ImageView) findViewById(R.id.imageView3);
        mTvCameraQuality = (TextView) findViewById(R.id.tv_camera_quality);
        mLlPhotoMode = (LinearLayout) findViewById(R.id.ll_photo_mode);
        mRlPhotoResolution = (RelativeLayout) findViewById(R.id.rl_photo_resolution);
        mIvPhotoResolution = (ImageView) findViewById(R.id.iv_photo_resolution);
        mTvPhotoResolution = (TextView) findViewById(R.id.tv_photo_resolution);
        mRlEv = (RelativeLayout) findViewById(R.id.rl_ev);
        mTextView2 = (TextView) findViewById(R.id.textView2);
        mImageView2 = (ImageView) findViewById(R.id.imageView2);
        mTvEv = (TextView) findViewById(R.id.tv_ev);
        mRlWb = (RelativeLayout) findViewById(R.id.rl_wb);
        mTextView4 = (TextView) findViewById(R.id.textView4);
        mImageView4 = (ImageView) findViewById(R.id.imageView4);
        mTvWb = (TextView) findViewById(R.id.tv_wb);
        mRlIso = (RelativeLayout) findViewById(R.id.rl_iso);
        mTextView5 = (TextView) findViewById(R.id.textView5);
        mImageView5 = (ImageView) findViewById(R.id.imageView5);
        mTvIso = (TextView) findViewById(R.id.tv_iso);
        mRlSharpness = (RelativeLayout) findViewById(R.id.rl_sharpness);
        mTextView6 = (TextView) findViewById(R.id.textView6);
        mImageView6 = (ImageView) findViewById(R.id.imageView6);
        mTvSharpness = (TextView) findViewById(R.id.tv_sharpness);
        mRlWdr = (RelativeLayout) findViewById(R.id.rl_wdr);
        mSbWdr = (SwitchButton) findViewById(R.id.sb_wdr);
        mRlFormat = (RelativeLayout) findViewById(R.id.rl_format);
        mTvCameraVersion = (TextView) findViewById(R.id.tv_camera_version);
        mRlRecovery = (RelativeLayout) findViewById(R.id.rl_recovery);
        mPresenter.init();
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
    public Context getAttachedContext() {
        return this;
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
    public void settingsInited() {
        mPresenter.getStatus();
    }

    @Override
    public void getStatusSuccess() {
        MstarRepository.Menu videoMenu = MstarRepository.getInstance().GetAutoMenu(MstarRepository.MENU_ID.menuVIDEO_RES);
        if (videoMenu != null && !TextUtils.isEmpty(MstarRepository.getInstance().getVideoresRet())) {
            mRlCameraQuality.setVisibility(View.VISIBLE);
            mTvCameraQuality.setText(MstarRepository.getInstance().getVideoresRet());
        }
        String imageresRet = MstarRepository.getInstance().getImageresRet();
        MstarRepository.Menu imageResMenu = MstarRepository.getInstance().GetAutoMenu(MstarRepository.MENU_ID.menuIMAGE_RES);
        if (imageResMenu != null && !TextUtils.isEmpty(imageresRet)) {
            mRlPhotoResolution.setVisibility(View.VISIBLE);
            mTvPhotoResolution.setText(MstarRepository.getInstance().getImageresRet());
        }
        String gsensorRet = MstarRepository.getInstance().getGsensorRet();
        if (!TextUtils.isEmpty(gsensorRet)) {
            mRlGsensorLevel.setVisibility(View.VISIBLE);
            mTvCamSensorLevel.setText(gsensorRet);
        }
        String evRet = MstarRepository.getInstance().getEVRet();
        if (!TextUtils.isEmpty(evRet)) {
            mRlEv.setVisibility(View.VISIBLE);
            mTvEv.setText(evRet);
        }
        MstarRepository.Menu wbMenu = MstarRepository.getInstance().GetAutoMenu(MstarRepository.MENU_ID.menuWHITE_BALANCE);
        String awbRet = MstarRepository.getInstance().getAWBRet();
        if (wbMenu != null && !TextUtils.isEmpty(awbRet)) {
            mRlWb.setVisibility(View.VISIBLE);
            mTvWb.setText(awbRet);
        }
        String fwVersionRet = MstarRepository.getInstance().getFWVersionRet();
        if (!TextUtils.isEmpty(fwVersionRet)) {
            mTvCameraVersion.setText(fwVersionRet);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        showpDialog();
        mPresenter.getStatus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.xc_camera:
                startActivity(MstarPasswordSettingActivity.class);
                break;
            case R.id.rl_cyclic_record:
                break;
            case R.id.rl_gsensor_level://碰撞灵敏度
                MstarSubSettingActivity.actionStart(this, getString(R.string.camset_gsensor_level), MstarRepository.MENU_ID.menuGST);
                break;
            case R.id.rl_camera_params://摄像头参数toggle
                if (mLlCameraParams.getVisibility() == View.VISIBLE) {
                    mLlCameraParams.setVisibility(View.GONE);
                    mIvCameraParams.setBackgroundResource(R.drawable.jiantouslide);
                } else {
                    mLlCameraParams.setVisibility(View.VISIBLE);
                    mIvCameraParams.setBackgroundResource(R.drawable.arrow_down);
                }
                break;
            case R.id.rl_camera_quality://录制质量
                MstarSubSettingActivity.actionStart(this, getString(R.string.camset_recquality), MstarRepository.MENU_ID.menuVIDEO_RES);
                break;
            case R.id.rl_photo_resolution://拍照分辨率
                MstarSubSettingActivity.actionStart(this, getString(R.string.photo_resolution), MstarRepository.MENU_ID.menuIMAGE_RES);
                break;
            case R.id.rl_ev://曝光补偿
                MstarSubSettingActivity.actionStart(this, getString(R.string.exposure), MstarRepository.MENU_ID.menuEV);
                break;
            case R.id.rl_wb://白平衡
                MstarSubSettingActivity.actionStart(this, getString(R.string.white_balance), MstarRepository.MENU_ID.menuWHITE_BALANCE);
                break;
            case R.id.rl_motion_detection:
                break;
            case R.id.rl_iso://iso
                break;
            case R.id.rl_sharpness://锐度
                break;
            case R.id.rl_format://格式化记录仪
                break;
            case R.id.rl_recovery://恢复出厂设置
                break;
            default:
                break;
        }
    }
}
