package com.adai.camera.sunplus.setting;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.CameraFixedInfo;
import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.function.FormatSDCard;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.ToastUtil;
import com.icatch.wificam.customer.type.ICatchCameraProperty;
import com.kyleduo.switchbutton.SwitchButton;


public class SunplusSettingActivity extends BaseActivity implements View.OnClickListener {

    /*--------------- 变量 ---------------*/
    public static final String IS_VIDEO_MODE = "is_video_mode";
    private ISunplusCamera mSunplusCamera;
    private boolean isVideoMode = false;
    private CameraProperties mCameraProperties;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GlobalInfo.MESSAGE_FORMAT_SD_START:
                    break;
                case GlobalInfo.MESSAGE_FORMAT_SUCCESS:
                    hidepDialog();
                    if (!isFinishing()) {
                        ToastUtil.showShortToast(SunplusSettingActivity.this, getString(R.string.wifi_format_sd_success));
                    }
                    break;
                case GlobalInfo.MESSAGE_FORMAT_FAILED:
                    hidepDialog();
                    if (!isFinishing()) {
                        ToastUtil.showShortToast(SunplusSettingActivity.this, getString(R.string.wifi_format_sd_failure));
                    }
                    break;
                default:
                    break;
            }
        }
    };
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
    private SwitchButton mSbMotionDetection;
    private RelativeLayout mRlParkingMonitor;
    private SwitchButton mSbParkingMonitor;
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
    private RelativeLayout mRlContinuousShooting, mRlTimeTakingPictures, mRlLightSourceFrequency;
    private TextView mTvContinuousShooting, mTvTimeTakingPictures, mTvLightSourceFrequency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunplus_setting);
        init();
        initCamera();
        initView();
        initStatus();
        initEvent();
    }

    private void initCamera() {
        mSunplusCamera = CameraFactory.getInstance().getSunplusCamera();
        mCameraProperties = CameraProperties.getInstance();
        isVideoMode = getIntent().getBooleanExtra(IS_VIDEO_MODE, false);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        initStatus();
    }

    private void initStatus() {
        String cameraVersion = CameraFixedInfo.getInstance().getCameraVersion();
        if (!TextUtils.isEmpty(cameraVersion)) {
            String[] cameraInfos = cameraVersion.split(";");
            mTvCameraVersion.setText(cameraInfos[cameraInfos.length - 1]);
        }
        mLlRecordMode.setVisibility(isVideoMode ? View.VISIBLE : View.GONE);
        mLlPhotoMode.setVisibility(isVideoMode ? View.GONE : View.VISIBLE);
        //视频分辨率
        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_VIDEO_SIZE)) {
            mTvCameraQuality.setText(mSunplusCamera.getVideoSize().getCurrentValue());
            mRlCameraQuality.setVisibility(View.VISIBLE);
        } else {
            mRlCameraQuality.setVisibility(View.GONE);
        }
        //图片分辨率
        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_IMAGE_SIZE)) {
            mTvPhotoResolution.setText(mSunplusCamera.getImageSize().getCurrentUiStringInSetting());
            mRlPhotoResolution.setVisibility(View.VISIBLE);
        } else {
            mRlPhotoResolution.setVisibility(View.GONE);
        }
//        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_BURST_NUMBER)) {//连拍
//            mTvContinuousShooting.setText(mSunplusCamera.getBurst().getCurrentUiStringInSetting());
//        } else {
//            mRlContinuousShooting.setVisibility(View.GONE);
//        }
        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_WHITE_BALANCE)) {
            if (TextUtils.isEmpty(mSunplusCamera.getWhiteBalance().getCurrentUiStringInSetting())) {
                mRlWb.setVisibility(View.GONE);
            } else {
                mTvWb.setText(mSunplusCamera.getWhiteBalance().getCurrentUiStringInSetting());
                mRlWb.setVisibility(View.VISIBLE);
            }
        } else {
            mRlWb.setVisibility(View.GONE);
        }
//        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_CAPTURE_DELAY)) {
//            mTvTimeTakingPictures.setText(mSunplusCamera.getCaptureDelay().getCurrentUiStringInPreview());
//        } else {
//            mRlTimeTakingPictures.setVisibility(View.GONE);
//        }
        if (mCameraProperties.hasFuction(ICatchCameraProperty.ICH_CAP_LIGHT_FREQUENCY)) {
            String currentFrequencyInSetting = mSunplusCamera.getElectricityFrequency().getCurrentUiStringInSetting();
            if (TextUtils.isEmpty(currentFrequencyInSetting)) {
                mRlLightSourceFrequency.setVisibility(View.GONE);
            } else {
                mTvLightSourceFrequency.setText(currentFrequencyInSetting);
                mRlLightSourceFrequency.setVisibility(View.VISIBLE);
            }
        } else {
            mRlLightSourceFrequency.setVisibility(View.GONE);
        }
    }

    private void initEvent() {
        mRlCameraQuality.setOnClickListener(this);
        mRlPhotoResolution.setOnClickListener(this);
        mRlFormat.setOnClickListener(this);
        mRlWb.setOnClickListener(this);
        mRlLightSourceFrequency.setOnClickListener(this);
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
        mSbMotionDetection = (SwitchButton) findViewById(R.id.sb_motion_detection);
        mRlParkingMonitor = (RelativeLayout) findViewById(R.id.rl_parking_monitor);
        mSbParkingMonitor = (SwitchButton) findViewById(R.id.sb_parking_monitor);
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
        mRlRecovery = (RelativeLayout) findViewById(R.id.rl_recovery);
        mTvCameraVersion = (TextView) findViewById(R.id.tv_camera_version);
        mRlContinuousShooting = (RelativeLayout) findViewById(R.id.rl_continuous_shooting);
        mTvContinuousShooting = (TextView) findViewById(R.id.tv_continuous_shooting);
        mRlWb = (RelativeLayout) findViewById(R.id.rl_wb);
        mTvWb = (TextView) findViewById(R.id.tv_wb);
        mRlTimeTakingPictures = (RelativeLayout) findViewById(R.id.rl_time_taking_pictures);
        mTvTimeTakingPictures = (TextView) findViewById(R.id.tv_time_taking_pictures);
        mRlLightSourceFrequency = (RelativeLayout) findViewById(R.id.rl_light_source_frequency);
        mTvLightSourceFrequency = (TextView) findViewById(R.id.tv_light_source_frequency);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, SunplusSubSettingActivity.class);
        switch (v.getId()) {
            case R.id.rl_light_source_frequency:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.LIGHT_SOURCE_FREQUENCY);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.frequency));
                startActivity(intent);
                break;
            case R.id.rl_camera_quality:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.VIDEO_RESOLUTION);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.camset_recquality));
                startActivity(intent);
                break;
            case R.id.rl_continuous_shooting:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.CONTINUOUS_SHOOTING);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.sequence));
                startActivity(intent);
                break;
            case R.id.rl_photo_resolution:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.PHOTO_RESOLUTION);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.photo_resolution));
                startActivity(intent);
                break;
            case R.id.rl_wb:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.WHITE_BALANCE);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.white_balance));
                startActivity(intent);
                break;
            case R.id.rl_time_taking_pictures:
                intent.putExtra(SunplusSubSettingActivity.CURRENT_TYPE, SunplusSubSettingActivity.TIME_TAKING_PICTURES);
                intent.putExtra(SunplusSubSettingActivity.TEXT_TITLE, getString(R.string.timing_capture));
                startActivity(intent);
                break;
            case R.id.rl_format:
                if (mCameraProperties.isSDCardExist()) {
                    showFormatConfirmDialog();
                } else {
                    ToastUtil.showShortToast(this, getString(R.string.wifi_sdcard));
                }
                break;
            default:
                break;
        }
    }

    private void showFormatConfirmDialog() {
        AlertDialog.Builder altBSdformat = new AlertDialog.Builder(this);
        altBSdformat.setMessage(getString(R.string.set_isneedformat));
        altBSdformat.setTitle(getString(R.string.wifi_stopwarning));
        altBSdformat.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                showpDialog();
                FormatSDCard formatSDCard = new FormatSDCard(mHandler);
                formatSDCard.start();
            }
        });

        altBSdformat.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                arg0.dismiss();
            }
        });

        altBSdformat.create().show();
    }
}
