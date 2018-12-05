package com.adai.camera.novatek.settting;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.CameraConstant;
import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.camera.novatek.settting.subsetting.NovatekPasswordSettingActivity;
import com.adai.camera.novatek.settting.subsetting.NovatekSubSettingActivity;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.SpUtils;
import com.example.ipcamera.domain.MovieRecord;
import com.kyleduo.switchbutton.SwitchButton;


public class NovatekSettingActivity extends BaseActivity implements NovatekSettingContract.View, View.OnClickListener {
    private static final int REQ_SETTING = 1;
    private NovatekSettingContract.Presenter mPresenter;
    private RelativeLayout mXcCamera;
    private SwitchButton mSbSoundRecording;
    private SwitchButton mSbTimeWatermark;
    private SwitchButton mSbParkingMonitor;
    //    private RelativeLayout rl_motion_detection;
    private RelativeLayout rl_time_watermark, rl_parking_monitor, rl_camera_ssid;
    //    private SwitchButton sb_motion_detection;
    private RelativeLayout mRlGSensorLevel, rl_cyclic_record, rl_delay_rec, rl_high_preview;
    private TextView mTvCamSensorLevel, tv_cyclic_record;
    private RelativeLayout mRlCameraParams;
    private LinearLayout mLlCameraParams;
    private RelativeLayout rl_pano, mRlCameraQuality, rl_photo_resolution, rl_voice_broadcast, rl_cyclic_record_switch, rl_monitoring_mode;
    private TextView mTvCameraQuality, tv_photo_resolution, tv_delay_rec, tv_firmware_version, tv_sdcard_space, tv_ssid;
    private RelativeLayout mRlEv;
    private TextView mTvEv;
    private RelativeLayout mRlWb;
    private TextView mTvWb;
    private RelativeLayout mRlIso;
    private TextView mTvIso;
    private RelativeLayout mRlSharpness;
    private TextView mTvSharpness;
    private SwitchButton mSbWdr, sb_hardware_acceleration, sb_high_preview, sb_voice_broadcast, sb_cyclic_record_switch, sb_monitoring_mode, sb_pano;
    private RelativeLayout mRlFormat;
    private RelativeLayout mRlRecovery;
    private ImageView mIvCameraParams;
    private boolean mIsSwitchWantSend;
    private LinearLayout ll_record_mode, ll_photo_mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_setting);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new NovatekSettingPresenter();
        mPresenter.attachView(this);
        mPresenter.init();
        mPresenter.setOnCmdCallback(mCmdCallback);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.setting);
        mXcCamera = (RelativeLayout) findViewById(R.id.xc_camera);
        rl_pano = findViewById(R.id.rl_pano);
        mSbSoundRecording = (SwitchButton) findViewById(R.id.sb_sound_recording);
        mSbTimeWatermark = (SwitchButton) findViewById(R.id.sb_time_watermark_switch);
        rl_parking_monitor = (RelativeLayout) findViewById(R.id.rl_parking_monitor);
        mSbParkingMonitor = (SwitchButton) findViewById(R.id.sb_parking_monitor);
        rl_time_watermark = (RelativeLayout) findViewById(R.id.rl_time_watermark);
//        rl_motion_detection = (RelativeLayout) findViewById(R.id.rl_motion_detection);
//        sb_motion_detection = (SwitchButton) findViewById(R.id.sb_motion_detection);
        rl_cyclic_record = (RelativeLayout) findViewById(R.id.rl_cyclic_record);
        tv_cyclic_record = (TextView) findViewById(R.id.tv_cyclic_record);
        mRlGSensorLevel = (RelativeLayout) findViewById(R.id.rl_gsensor_level);
        mTvCamSensorLevel = (TextView) findViewById(R.id.tv_cam_sensor_level);
        mRlCameraParams = (RelativeLayout) findViewById(R.id.rl_camera_params);
        mIvCameraParams = (ImageView) findViewById(R.id.iv_camera_params);
        mLlCameraParams = (LinearLayout) findViewById(R.id.ll_camera_params);
        mRlCameraQuality = (RelativeLayout) findViewById(R.id.rl_camera_quality);
        mTvCameraQuality = (TextView) findViewById(R.id.tv_camera_quality);
        rl_delay_rec = (RelativeLayout) findViewById(R.id.rl_delay_rec);
        tv_delay_rec = (TextView) findViewById(R.id.tv_delay_rec);
        ll_record_mode = (LinearLayout) findViewById(R.id.ll_record_mode);
        ll_photo_mode = (LinearLayout) findViewById(R.id.ll_photo_mode);
        rl_photo_resolution = (RelativeLayout) findViewById(R.id.rl_photo_resolution);
        tv_photo_resolution = (TextView) findViewById(R.id.tv_photo_resolution);
        mRlEv = (RelativeLayout) findViewById(R.id.rl_ev);
        mTvEv = (TextView) findViewById(R.id.tv_ev);
        mRlWb = (RelativeLayout) findViewById(R.id.rl_wb);
        mTvWb = (TextView) findViewById(R.id.tv_wb);
        mRlIso = (RelativeLayout) findViewById(R.id.rl_iso);
        mTvIso = (TextView) findViewById(R.id.tv_iso);
        mRlSharpness = (RelativeLayout) findViewById(R.id.rl_sharpness);
        mTvSharpness = (TextView) findViewById(R.id.tv_sharpness);
        mSbWdr = (SwitchButton) findViewById(R.id.sb_wdr);
        mRlFormat = (RelativeLayout) findViewById(R.id.rl_format);
        mRlRecovery = (RelativeLayout) findViewById(R.id.rl_recovery);
        rl_camera_ssid = (RelativeLayout) findViewById(R.id.rl_camera_ssid);
        sb_hardware_acceleration = (SwitchButton) findViewById(R.id.sb_hardware_acceleration);
        rl_high_preview = (RelativeLayout) findViewById(R.id.rl_high_preview);
        sb_high_preview = (SwitchButton) findViewById(R.id.sb_high_preview);
        rl_voice_broadcast = (RelativeLayout) findViewById(R.id.rl_voice_broadcast);
        sb_voice_broadcast = (SwitchButton) findViewById(R.id.sb_voice_broadcast);
        rl_cyclic_record_switch = (RelativeLayout) findViewById(R.id.rl_cyclic_record_switch);
        sb_cyclic_record_switch = (SwitchButton) findViewById(R.id.sb_cyclic_record_switch);
        rl_monitoring_mode = (RelativeLayout) findViewById(R.id.rl_monitoring_mode);
        sb_monitoring_mode = (SwitchButton) findViewById(R.id.sb_monitoring_mode);
        tv_firmware_version = (TextView) findViewById(R.id.tv_firmware_version);
        tv_sdcard_space = (TextView) findViewById(R.id.tv_sdcard_space);
        tv_ssid = (TextView) findViewById(R.id.tv_ssid);
        sb_pano = (SwitchButton) findViewById(R.id.sb_pano);
        //        if (CameraUtils.currentProduct == CameraUtils.PRODUCT.DCT) {
//            rl_camera_ssid.setVisibility(View.VISIBLE);
//        } else {
//            rl_camera_ssid.setVisibility(View.GONE);
//        }
//        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
//            ll_record_mode.setVisibility(View.GONE);
//        } else {
//            ll_photo_mode.setVisibility(View.GONE);
//        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getStatusSuccess();
    }

    @Override
    public void getStatusSuccess() {
        String cameraVersion = SpUtils.getString(this, CameraConstant.CAMERA_FIRMWARE_VERSION, "");
        tv_firmware_version.setText(cameraVersion);
//        boolean pano = SpUtils.getBoolean(this, CameraConstant.CAMERA_PANO, true);
//        sb_pano.setChecked(pano);
        String curMovieFov = mPresenter.getStateId(NovatekWifiCommands.MOVIE_MOVIE_FOV);
        if (!TextUtils.isEmpty(curMovieFov)) {
            rl_pano.setVisibility(View.VISIBLE);
            sb_pano.setChecked(curMovieFov.equals("0"));
        } else {
            rl_pano.setVisibility(View.GONE);
        }
        boolean hard_acceleration = SpUtils.getBoolean(this, "hard_acceleration", false);
        sb_hardware_acceleration.setChecked(hard_acceleration);
        String curCyclicRecordSwitch = mPresenter.getStateId(NovatekWifiCommands.MOVIE_CYCLIC_RECORD_SWITCH);
        if (!TextUtils.isEmpty(curCyclicRecordSwitch)) {
            rl_cyclic_record_switch.setVisibility(View.VISIBLE);
            sb_cyclic_record_switch.setChecked(!curCyclicRecordSwitch.equals("0"));
        }
        String curAudioMute = mPresenter.getStateId(NovatekWifiCommands.AUDIO_MUTE);
        if (!TextUtils.isEmpty(curAudioMute)) {
            rl_voice_broadcast.setVisibility(View.VISIBLE);
            sb_voice_broadcast.setChecked(!curAudioMute.equals("0"));
        }
        String curHighPreview = mPresenter.getStateId(NovatekWifiCommands.MOVIE_SET_LIVE_VIEW_SIZE);
        if (!TextUtils.isEmpty(curHighPreview)) {
            rl_high_preview.setVisibility(View.VISIBLE);
            sb_high_preview.setChecked(!curHighPreview.equals("0"));
        }
//        String curRecordMode = mPresenter.getStateId(NovatekWifiCommands.MOVIE_RECORD_MODE);
//        if (!TextUtils.isEmpty(curRecordMode)) {
//            rl_monitoring_mode.setVisibility(View.VISIBLE);
//            sb_monitoring_mode.setChecked(curRecordMode.equals("0"));
//        }
        String curAudio = mPresenter.getStateId(NovatekWifiCommands.MOVIE_RECORD_AUDIO);
        if (!TextUtils.isEmpty(curAudio)) {
            mSbSoundRecording.setChecked(!curAudio.equals("0"));
        }
        String curGSensorLevel = mPresenter.getState(NovatekWifiCommands.MOVIE_GSENSOR);
        if (!TextUtils.isEmpty(curGSensorLevel)) {
            mRlGSensorLevel.setVisibility(View.VISIBLE);
            try {
                mTvCamSensorLevel.setText(curGSensorLevel);
            } catch (Exception ignore) {

            }
        }
        String curCyclicRecord = mPresenter.getState(NovatekWifiCommands.MOVIE_CYCLIC_RECORD);
        if (!TextUtils.isEmpty(curCyclicRecord)) {
            rl_cyclic_record.setVisibility(View.VISIBLE);
            tv_cyclic_record.setText(curCyclicRecord);
        }
        String curRecordSize = mPresenter.getState(NovatekWifiCommands.MOVIE_SET_RECORD_SIZE);
        if (!TextUtils.isEmpty(curRecordSize)) {
            mRlCameraQuality.setVisibility(View.VISIBLE);
            mTvCameraQuality.setText(curRecordSize);
        }
        String curCaptureSize = mPresenter.getState(NovatekWifiCommands.IMAGE_QUALITY);
        if (!TextUtils.isEmpty(curCaptureSize)) {
            rl_photo_resolution.setVisibility(View.VISIBLE);
            tv_photo_resolution.setText(curCaptureSize);
        }
        String curEv = mPresenter.getState(NovatekWifiCommands.MOVIE_SET_EV);
        if (!TextUtils.isEmpty(curEv)) {
            mRlEv.setVisibility(View.VISIBLE);
            mTvEv.setText(curEv);
        }
        tv_sdcard_space.setText(CameraUtils.FREE_MEMORY + "/" + CameraUtils.SDCARD_MEMORY);
        tv_ssid.setText(httpGetCamName());
        mIsSwitchWantSend = true;
    }

    private String httpGetCamName() {
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        String strSSID = mWifiInfo.getSSID();
        if (!TextUtils.isEmpty(strSSID)) {
            if (strSSID.startsWith("\"") && strSSID.endsWith("\"")) {
                strSSID = strSSID.substring(1, strSSID.length() - 1);
            }
        }
        return strSSID;

    }

    private void enableView(boolean enable) {
//        mSbSoundRecording.setEnabled(enable);
//        mSbWdr.setEnabled(enable);
//        mSbTimeWatermark.setEnabled(enable);
//        sb_motion_detection.setEnabled(enable);
    }

    private void initEvent() {
        mXcCamera.setOnClickListener(this);
        sb_pano.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                SpUtils.putBoolean(NovatekSettingActivity.this, CameraConstant.CAMERA_PANO, isChecked);
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_MOVIE_FOV, isChecked ? "0" : "1");
                }
            }
        });
        sb_monitoring_mode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_RECORD_MODE, isChecked ? "1" : "0");
                }
            }
        });
        sb_cyclic_record_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_CYCLIC_RECORD_SWITCH, isChecked ? "1" : "0");
                }
            }
        });
        sb_voice_broadcast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.AUDIO_MUTE, isChecked ? "1" : "0");
                }
            }
        });
        sb_high_preview.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_SET_LIVE_VIEW_SIZE, isChecked ? "1" : "0");
                }
            }
        });
        sb_hardware_acceleration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SpUtils.putBoolean(NovatekSettingActivity.this, "hard_acceleration", isChecked);
            }
        });
        //停车监控
        mSbParkingMonitor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.PARKING_MONITOR, isChecked ? "1" : "0");
                }
            }
        });
        //录音
        mSbSoundRecording.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_RECORD_AUDIO, isChecked ? "1" : "0");
                }
            }
        });
        //摄像时间水印
        mSbTimeWatermark.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    enableView(false);
                    showpDialog();
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_DATE_PRINT, isChecked ? "1" : "0");
                }
            }
        });
//        sb_motion_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (mIsSwitchWantSend) {
//                    showpDialog();
//                    enableView(false);
//                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_MOTION_DETECT, isChecked ? "1" : "0");
//                }
//            }
//        });
        mRlGSensorLevel.setOnClickListener(this);
        rl_cyclic_record.setOnClickListener(this);
        mRlCameraParams.setOnClickListener(this);
        mRlCameraQuality.setOnClickListener(this);
        rl_photo_resolution.setOnClickListener(this);
        mRlEv.setOnClickListener(this);
        mRlWb.setOnClickListener(this);
        mRlIso.setOnClickListener(this);
        mRlSharpness.setOnClickListener(this);
        //wdr
        mSbWdr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mIsSwitchWantSend) {
                    showpDialog();
                    enableView(false);
                    mPresenter.sendCmd(NovatekWifiCommands.MOVIE_HDR, isChecked ? "1" : "0");
                }
            }
        });
        mRlFormat.setOnClickListener(this);
        mRlRecovery.setOnClickListener(this);
        rl_camera_ssid.setOnClickListener(this);
        rl_delay_rec.setOnClickListener(this);
    }

    private NovatekSettingContract.Presenter.CmdCallback mCmdCallback = new NovatekSettingContract.Presenter.CmdCallback() {
        @Override
        public void success(int commandId, String par) {
            hideLoading();
//            if (movieRecord == null) {
//                showToast(R.string.set_failure);
//                return;
//            }
//            String status = movieRecord.getStatus();
            switch (commandId) {
                case NovatekWifiCommands.MOVIE_CYCLIC_RECORD_SWITCH:
                    mIsSwitchWantSend = false;
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        sb_cyclic_record_switch.setChecked(false);
                    } else {
                        sb_cyclic_record_switch.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.MOVIE_RECORD_MODE:
                    mIsSwitchWantSend = false;
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        sb_monitoring_mode.setChecked(true);
                    } else {
                        sb_monitoring_mode.setChecked(false);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.AUDIO_MUTE:
                    mIsSwitchWantSend = false;
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        sb_voice_broadcast.setChecked(false);
                    } else {
                        sb_voice_broadcast.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.MOVIE_SET_LIVE_VIEW_SIZE:
                    mIsSwitchWantSend = false;
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        sb_high_preview.setChecked(false);
                    } else {
                        sb_high_preview.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;

                    break;
                case NovatekWifiCommands.PARKING_MONITOR:
                    mIsSwitchWantSend = false;
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        mSbSoundRecording.setChecked(false);
                    } else {
                        mSbSoundRecording.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.MOVIE_RECORD_AUDIO:
                    mIsSwitchWantSend = false;
//                    if ("0".equals(status)) {
                    //设置成功
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        mSbSoundRecording.setChecked(false);
                    } else {
                        mSbSoundRecording.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.MOVIE_DATE_PRINT:
                    mIsSwitchWantSend = false;
//                    if ("0".equals(status)) {
                    //设置成功
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        mSbTimeWatermark.setChecked(false);
                    } else {
                        mSbTimeWatermark.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.MOVIE_HDR:
                    mIsSwitchWantSend = false;
//                    if ("0".equals(status)) {
                    //设置成功
                    if ("0".equals(par)) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        mSbWdr.setChecked(false);
                    } else {
                        mSbWdr.setChecked(true);
                    }
                    enableView(true);
                    mIsSwitchWantSend = true;
                    break;
                case NovatekWifiCommands.CAMERA_FORMAT:
//                    if ("0".equals(status)) {
                    showToast(R.string.wifi_format_sd_success);
                    tv_sdcard_space.setText(CameraUtils.SDCARD_MEMORY + "/" + CameraUtils.SDCARD_MEMORY);

//                    } else {
//                        showToast(R.string.wifi_format_sd_failure);
//                    }
                    break;
                case NovatekWifiCommands.CAMERA_RESET_SETTINGS:
                    showToast(R.string.wifi_system_reset_success);
                    mIsSwitchWantSend = false;
                    mPresenter.getStatus();
                    break;
                default:
//                    if ("0".equals(status)) {
                    showToast(R.string.set_success);
//                    } else {
//                        showToast(R.string.set_failure);
//                    }
                    enableView(true);
                    break;
            }
        }

        @Override
        public void failed(int commandId, String par, String error) {
            hidepDialog();
            showToast(R.string.set_failure);
            enableView(true);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_camera_ssid:
                startActivity(NovatekWifiNameActivity.class);
                break;
            case R.id.xc_camera://记录仪密码设置
                startActivity(NovatekPasswordSettingActivity.class);
                break;
            case R.id.rl_cyclic_record:
                NovatekSubSettingActivity.actionStart(this, getString(R.string.cyclic_record_time), NovatekWifiCommands.MOVIE_CYCLIC_RECORD);
                break;
            case R.id.rl_gsensor_level://碰撞灵敏度
                NovatekSubSettingActivity.actionStart(this, getString(R.string.camset_gsensor_level), NovatekWifiCommands.MOVIE_GSENSOR);
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
            case R.id.rl_delay_rec:
                NovatekSubSettingActivity.actionStart(this, getString(R.string.delay_rec), NovatekWifiCommands.DELAY_REC);
                break;
            case R.id.rl_camera_quality://录制质量
                NovatekSubSettingActivity.actionStart(this, getString(R.string.camset_recquality), NovatekWifiCommands.MOVIE_SET_RECORD_SIZE);
                break;
            case R.id.rl_photo_resolution://拍照分辨率
                NovatekSubSettingActivity.actionStart(this, getString(R.string.image_quality), NovatekWifiCommands.IMAGE_QUALITY);
                break;
            case R.id.rl_ev://曝光补偿
                NovatekSubSettingActivity.actionStart(this, getString(R.string.exposure), NovatekWifiCommands.MOVIE_SET_EV);
                break;
            case R.id.rl_wb://白平衡
                NovatekSubSettingActivity.actionStart(this, getString(R.string.white_balance), NovatekWifiCommands.CAMERA_SET_WB);
                break;
            case R.id.rl_iso://iso
                NovatekSubSettingActivity.actionStart(this, "ISO", NovatekWifiCommands.CAMERA_SET_ISO);
                break;
            case R.id.rl_sharpness://锐度
                NovatekSubSettingActivity.actionStart(this, getString(R.string.sharpness), NovatekWifiCommands.CAMERA_SET_SHARPNESS);
                break;
            case R.id.rl_format://格式化记录仪
                if (!CameraUtils.hasSDCard) {
                    showToast(R.string.wifi_sdcard);
                    return;
                }
                AlertDialog.Builder altBSdformat = new AlertDialog.Builder(this);
                altBSdformat.setMessage(getString(R.string.set_isneedformat));
                altBSdformat.setTitle(getString(R.string.wifi_stopwarning));
                altBSdformat.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        showpDialog(R.string.formatting);
                        mPresenter.sendCmd(NovatekWifiCommands.CAMERA_FORMAT, "1");
                    }
                });

                altBSdformat.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        arg0.dismiss();
                    }
                });

                altBSdformat.create().show();
                break;
            case R.id.rl_recovery://恢复出厂设置
                AlertDialog.Builder altBSdfactory = new AlertDialog.Builder(this);
                altBSdfactory.setMessage(getString(R.string.camset_recovery_question));
                altBSdfactory.setTitle(getString(R.string.notice));
                altBSdfactory.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        arg0.dismiss();
                        showpDialog();
                        mPresenter.sendCmd(NovatekWifiCommands.CAMERA_RESET_SETTINGS, "");
                    }
                });

                altBSdfactory.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        arg0.dismiss();
                    }
                });

                altBSdfactory.create().show();
                break;
            default:
                break;
        }
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
    public void settingsInited() {
        mIsSwitchWantSend = false;
        mPresenter.getStatus();
    }

    @Override
    protected void goBack() {
        super.goBack();
        showpDialog();
        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
            @Override
            public void success(int commandId, String par, MovieRecord movieRecord) {
                hidepDialog();
                finish();
            }

            @Override
            public void failed(int commandId, String par, String error) {
                hideLoading();
                finish();
            }
        });
    }
}
