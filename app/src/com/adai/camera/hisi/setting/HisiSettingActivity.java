package com.adai.camera.hisi.setting;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.SparseArray;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.camera.hisi.HisiCamera;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.MainTabActivity;
import com.adai.gkdnavi.R;
import com.kyleduo.switchbutton.SwitchButton;

import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_LOOP_TYPE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_VIDEO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.FAILURE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_SINGLE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_LOOP;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_NORMAL;

public class HisiSettingActivity extends BaseActivity implements View.OnClickListener {
    //视频设置父控件
    private RelativeLayout rl_aotu_record;
    private RelativeLayout rl_video_resolution;
    private RelativeLayout rl_time_lapse_video;
    private RelativeLayout rl_slow_photography;
    private RelativeLayout rl_loop_recording;
    private RelativeLayout rl_light_source_frequency;
    private RelativeLayout rl_microphone;
    private RelativeLayout rl_wdr;
    private RelativeLayout rl_gyroscope;
    private RelativeLayout rl_date_tag;
    private RelativeLayout rl_motiondetection;
    //图片设置父控件
    private RelativeLayout rl_photo_resolution;
    private RelativeLayout rl_time_taking_pictures;
    private RelativeLayout rl_continuous_shooting;
    private RelativeLayout rl_image_quality;
    private RelativeLayout rl_anti_shake;
    //设置父控件
    private RelativeLayout rl_pip;
    private RelativeLayout rl_exposure_compensation;
    private RelativeLayout rl_white_balance;
    private RelativeLayout rl_color;
    private RelativeLayout rl_imaging_field;
    private RelativeLayout rl_iso;
    private RelativeLayout rl_sharpness;
    private RelativeLayout rl_gps;
    private RelativeLayout rl_rotate;
    private RelativeLayout rl_underwater_mode;
    //系统设置父控件
    private RelativeLayout rl_screen_protector;
    private RelativeLayout rl_timed_shutdown;
    private RelativeLayout rl_tv_format;
    private RelativeLayout rl_set_time;
    private RelativeLayout rl_key_sound;
    private RelativeLayout rl_camera_language;
    private RelativeLayout rl_camera_wifi_settings;
    private RelativeLayout rl_format_camera;
    private RelativeLayout rl_restore_settings;
    private RelativeLayout rl_camera_version;
    //UISwitchButton
    private SwitchButton sb_auto_record;
    private SwitchButton sb_microphone;
    private SwitchButton sb_wdr;
    private SwitchButton sb_gyroscope;
    private SwitchButton sb_date_tag;
    private SwitchButton sb_anti_shake;
    private SwitchButton sb_gps;
    private SwitchButton sb_rotate;
    private SwitchButton sb_underwater_mode;
    private SwitchButton sb_motiondetection;
    private SwitchButton sb_center_photometry;
    private SwitchButton sb_led_state;
    private SwitchButton sb_key_sound;
    //视频设置TextView
    private TextView tv_video_resolution;
    private TextView tv_time_lapse_video;
    private TextView tv_slow_photography;
    private TextView tv_loop_recording;
    private TextView tv_light_source_frequency;
    //图片设置TextView
    private TextView tv_photo_resolution;
    private TextView tv_time_taking_pictures;
    private TextView tv_continuous_shooting;
    private TextView tv_image_quality;
    //设置TextView
    private TextView tv_pip;
    private TextView tv_exposure_compensation;
    private TextView tv_white_balance;
    private TextView tv_color;
    private TextView tv_imaging_field;
    private TextView tv_iso;
    private TextView tv_sharpness;
    //系统设置TextView
    private TextView tv_screen_protector;
    private TextView tv_screen_brightness;
    private TextView tv_timed_shutdown;
    private TextView tv_tv_format;
    private TextView tv_set_time;
    private TextView tv_camera_language;

    private TextView tv_camera_version;


    private boolean switchwantsend = true;

    private boolean is_photo_mode;
    private LinearLayout ll_photo_list;

    private LinearLayout ll_camera_list;
    public static final String IS_PHOTO_MODE = "IS_PHOTO_MODE";
    private HisiCamera mHisiCamera;
    private SparseArray<String> mScreenSleepMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hisi_setting);
        is_photo_mode = getIntent().getBooleanExtra(IS_PHOTO_MODE, false);
        init();
        initView();
        bindViewAndData();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mHisiCamera = CameraFactory.getInstance().getHisiCamera();
        String[] screenSleepEntries = getResources().getStringArray(R.array.screen_auto_sleep_entries);
        int[] screenSleepValues = getResources().getIntArray(R.array.screen_auto_sleep_values);
        mScreenSleepMap = new SparseArray<>();
        for (int i = 0; i < screenSleepValues.length; i++) {
            mScreenSleepMap.put(screenSleepValues[i], screenSleepEntries[i]);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        bindViewAndData();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.video_setting);
        if (is_photo_mode) {
            setTitle(R.string.capture_setting);
        }
        //标题栏
        ll_photo_list = (LinearLayout) findViewById(R.id.ll_photo_list);
        ll_camera_list = (LinearLayout) findViewById(R.id.ll_camera_list);

        //视频设置
        rl_aotu_record = (RelativeLayout) findViewById(R.id.rl_aotu_record);
        rl_video_resolution = (RelativeLayout) findViewById(R.id.rl_video_resolution);
        rl_time_lapse_video = (RelativeLayout) findViewById(R.id.rl_time_lapse_video);
        rl_slow_photography = (RelativeLayout) findViewById(R.id.rl_slow_photography);
        rl_loop_recording = (RelativeLayout) findViewById(R.id.rl_loop_recording);
        rl_light_source_frequency = (RelativeLayout) findViewById(R.id.rl_light_source_frequency);
        rl_microphone = (RelativeLayout) findViewById(R.id.rl_microphone);
        rl_wdr = (RelativeLayout) findViewById(R.id.rl_wdr);
        rl_gyroscope = (RelativeLayout) findViewById(R.id.rl_gyroscope);
        rl_date_tag = (RelativeLayout) findViewById(R.id.rl_date_tag);
        rl_motiondetection = (RelativeLayout) findViewById(R.id.rl_motiondetection);
        //图片设置
        rl_photo_resolution = (RelativeLayout) findViewById(R.id.rl_photo_resolution);
        rl_time_taking_pictures = (RelativeLayout) findViewById(R.id.rl_time_taking_pictures);
        rl_continuous_shooting = (RelativeLayout) findViewById(R.id.rl_continuous_shooting);
        rl_image_quality = (RelativeLayout) findViewById(R.id.rl_image_quality);
        rl_anti_shake = (RelativeLayout) findViewById(R.id.rl_anti_shake);
        //设置
        rl_pip = (RelativeLayout) findViewById(R.id.rl_pip);
        rl_exposure_compensation = (RelativeLayout) findViewById(R.id.rl_exposure_compensation);
        rl_white_balance = (RelativeLayout) findViewById(R.id.rl_white_balance);
        rl_color = (RelativeLayout) findViewById(R.id.rl_color);
        rl_imaging_field = (RelativeLayout) findViewById(R.id.rl_imaging_field);
        rl_iso = (RelativeLayout) findViewById(R.id.rl_iso);
        rl_sharpness = (RelativeLayout) findViewById(R.id.rl_sharpness);
        rl_gps = (RelativeLayout) findViewById(R.id.rl_gps);
        rl_rotate = (RelativeLayout) findViewById(R.id.rl_rotate);
        rl_underwater_mode = (RelativeLayout) findViewById(R.id.rl_underwater_mode);
        //系统设置
        rl_screen_protector = (RelativeLayout) findViewById(R.id.rl_screen_protector);
        rl_timed_shutdown = (RelativeLayout) findViewById(R.id.rl_timed_shutdown);
        rl_tv_format = (RelativeLayout) findViewById(R.id.rl_tv_format);
        rl_set_time = (RelativeLayout) findViewById(R.id.rl_set_time);
        rl_key_sound = (RelativeLayout) findViewById(R.id.rl_key_sound);
        rl_camera_language = (RelativeLayout) findViewById(R.id.rl_camera_language);
        rl_camera_wifi_settings = (RelativeLayout) findViewById(R.id.rl_camera_wifi_settings);
        rl_format_camera = (RelativeLayout) findViewById(R.id.rl_format_camera);
        rl_restore_settings = (RelativeLayout) findViewById(R.id.rl_restore_settings);
        rl_camera_version = (RelativeLayout) findViewById(R.id.rl_camera_version);

        //视频设置
        tv_video_resolution = (TextView) findViewById(R.id.tv_video_resolution);
        tv_time_lapse_video = (TextView) findViewById(R.id.tv_time_lapse_video);
        tv_slow_photography = (TextView) findViewById(R.id.tv_slow_photography);
        tv_loop_recording = (TextView) findViewById(R.id.tv_loop_recording);
        tv_light_source_frequency = (TextView) findViewById(R.id.tv_light_source_frequency);
        //图片设置
        tv_photo_resolution = (TextView) findViewById(R.id.tv_photo_resolution);
        tv_time_taking_pictures = (TextView) findViewById(R.id.tv_time_taking_pictures);
        tv_continuous_shooting = (TextView) findViewById(R.id.tv_continuous_shooting);
        tv_image_quality = (TextView) findViewById(R.id.tv_image_quality);
        //设置
        tv_pip = (TextView) findViewById(R.id.tv_pip);
        tv_exposure_compensation = (TextView) findViewById(R.id.tv_exposure_compensation);
        tv_white_balance = (TextView) findViewById(R.id.tv_white_balance);
        tv_color = (TextView) findViewById(R.id.tv_color);
        tv_imaging_field = (TextView) findViewById(R.id.tv_imaging_field);
        tv_iso = (TextView) findViewById(R.id.tv_iso);
        tv_sharpness = (TextView) findViewById(R.id.tv_sharpness);
        //系统设置
        tv_screen_protector = (TextView) findViewById(R.id.tv_screen_protector);
        tv_screen_brightness = (TextView) findViewById(R.id.tv_screen_brightness);
        tv_timed_shutdown = (TextView) findViewById(R.id.tv_timed_shutdown);
        tv_tv_format = (TextView) findViewById(R.id.tv_tv_format);
        tv_set_time = (TextView) findViewById(R.id.tv_set_time);
        tv_camera_language = (TextView) findViewById(R.id.tv_camera_language);
        tv_camera_version = (TextView) findViewById(R.id.tv_camera_version);
        //UISwitchButton
        sb_auto_record = (SwitchButton) findViewById(R.id.sb_auto_record);
        sb_microphone = (SwitchButton) findViewById(R.id.sb_microphone);
        sb_wdr = (SwitchButton) findViewById(R.id.sb_wdr);
        sb_gyroscope = (SwitchButton) findViewById(R.id.sb_gyroscope);
        sb_date_tag = (SwitchButton) findViewById(R.id.sb_date_tag);
        sb_anti_shake = (SwitchButton) findViewById(R.id.sb_anti_shake);
        sb_gps = (SwitchButton) findViewById(R.id.sb_gps);
        sb_rotate = (SwitchButton) findViewById(R.id.sb_rotate);
        sb_underwater_mode = (SwitchButton) findViewById(R.id.sb_underwater_mode);
        sb_key_sound = (SwitchButton) findViewById(R.id.sb_key_sound);
        sb_motiondetection = (SwitchButton) findViewById(R.id.sb_motiondetection);
        sb_center_photometry = (SwitchButton) findViewById(R.id.sb_center_photometry);
        sb_led_state = (SwitchButton) findViewById(R.id.sb_led_state);
        rl_imaging_field.setVisibility(View.GONE);
        rl_underwater_mode.setVisibility(View.GONE);
        rl_key_sound.setVisibility(View.GONE);
        rl_gyroscope.setVisibility(View.GONE);

        rl_set_time.setVisibility(View.GONE);

        if (is_photo_mode) {
            ll_camera_list.setVisibility(View.GONE);
        } else {
            ll_photo_list.setVisibility(View.GONE);
        }
    }

    private void initEvent() {
        rl_video_resolution.setOnClickListener(this);
        rl_time_lapse_video.setOnClickListener(this);
        rl_slow_photography.setOnClickListener(this);
        rl_loop_recording.setOnClickListener(this);
        rl_light_source_frequency.setOnClickListener(this);
        rl_photo_resolution.setOnClickListener(this);
        rl_time_taking_pictures.setOnClickListener(this);
        rl_continuous_shooting.setOnClickListener(this);
        rl_image_quality.setOnClickListener(this);
        rl_pip.setOnClickListener(this);
        rl_exposure_compensation.setOnClickListener(this);
        rl_white_balance.setOnClickListener(this);
        rl_color.setOnClickListener(this);
        rl_imaging_field.setOnClickListener(this);
        rl_iso.setOnClickListener(this);
        rl_sharpness.setOnClickListener(this);
        rl_screen_protector.setOnClickListener(this);
        rl_timed_shutdown.setOnClickListener(this);
        rl_tv_format.setOnClickListener(this);
        rl_set_time.setOnClickListener(this);
        rl_camera_language.setOnClickListener(this);
        rl_camera_wifi_settings.setOnClickListener(this);
        rl_format_camera.setOnClickListener(this);
        rl_restore_settings.setOnClickListener(this);
        rl_camera_version.setOnClickListener(this);
        findViewById(R.id.rl_screen_protector).setOnClickListener(this);
        sb_date_tag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!switchwantsend) {
                    return;
                }
                SetDataTagTask setDataTagTask = new SetDataTagTask();
                setDataTagTask.execute(isChecked);
            }
        });
        sb_rotate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!switchwantsend) {
                    return;
                }
                SetRotateTask setRotateTask = new SetRotateTask();
                setRotateTask.execute(isChecked);
            }
        });
        sb_center_photometry.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!switchwantsend) {
                    return;
                }
                SetSpotMeterTask setSpotMeterTask = new SetSpotMeterTask();
                setSpotMeterTask.execute(isChecked);
            }
        });
    }

    /**
     * 设置中心点
     */
    @SuppressLint("StaticFieldLeak")
    private class SetSpotMeterTask extends AsyncTask<Boolean, Void, Integer> {
        private boolean value;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
            switchwantsend = false;
        }

        @Override
        protected Integer doInBackground(Boolean... booleans) {
            value = booleans[0];
            return mHisiCamera.setSpotMeter(booleans[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            hidepDialog();
            if (FAILURE == integer) {
                sb_center_photometry.setChecked(!sb_center_photometry.isChecked());
                showToast(R.string.set_failure);
            } else {
                mHisiCamera.prefer.spotMetering = value;
            }
            switchwantsend = true;
        }
    }

    /**
     * 设置图片翻转
     */
    @SuppressLint("StaticFieldLeak")
    private class SetRotateTask extends AsyncTask<Boolean, Void, Integer> {
        private boolean value;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
            switchwantsend = false;
        }

        @Override
        protected Integer doInBackground(Boolean... booleans) {
            value = booleans[0];
            return mHisiCamera.setFlip(booleans[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            hidepDialog();
            if (FAILURE == integer) {
                sb_rotate.setChecked(!sb_rotate.isChecked());
                showToast(R.string.set_failure);
            } else {
                mHisiCamera.prefer.imageUpsidedown = value;
            }
            switchwantsend = true;
        }
    }

    /**
     * 设置日期显示
     */
    @SuppressLint("StaticFieldLeak")
    private class SetDataTagTask extends AsyncTask<Boolean, Void, Integer> {
        private boolean value;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
            switchwantsend = false;
        }

        @Override
        protected Integer doInBackground(Boolean... booleans) {
            value = booleans[0];
            return mHisiCamera.setTimeOsd(booleans[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            hidepDialog();
            if (FAILURE == integer) {
                sb_date_tag.setChecked(!sb_date_tag.isChecked());
                showToast(R.string.set_failure);
            } else {
                mHisiCamera.prefer.timeTag = value;
            }
            switchwantsend = true;
        }
    }

    /**
     * 格式化sd卡
     */
    @SuppressLint("StaticFieldLeak")
    private class FormatTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            return mHisiCamera.formatSdCard(null);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            hidepDialog();
            if (FAILURE == integer) {
                showToast(com.adai.gkdnavi.R.string.wifi_format_sd_failure);
            } else {
                showToast(com.adai.gkdnavi.R.string.wifi_format_sd_success);
            }
        }
    }

    /**
     * 恢复出厂设置
     */
    @SuppressLint("StaticFieldLeak")
    private class ResetTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            mHisiCamera.restoreFactorySettings();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startActivity(MainTabActivity.class);
        }
    }

    private void bindViewAndData() {
        switchwantsend = false;
        if (!mHisiCamera.supportWorkMode()) {
            return;
        }
        tv_video_resolution.setText(mHisiCamera.modeConfig.videoNormalResolution);
        tv_camera_version.setText(mHisiCamera.deviceAttr.softVersion);
        tv_loop_recording.setText(mHisiCamera.modeConfig.videoLoopType);
        sb_date_tag.setChecked(mHisiCamera.prefer.timeTag);
        tv_photo_resolution.setText(mHisiCamera.modeConfig.photoSingleResolution);
        sb_rotate.setChecked(mHisiCamera.prefer.imageUpsidedown);
        tv_tv_format.setText(mHisiCamera.prefer.videoMode);
        sb_center_photometry.setChecked(mHisiCamera.prefer.spotMetering);
        tv_screen_protector.setText(mScreenSleepMap.get(mHisiCamera.prefer.screenAutoSleep));
        switchwantsend = true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_video_resolution:
                HisiSubSettingActivity.startAction(this, HisiSubSettingActivity.TYPE_WORK_MODE_TYPE, getString(R.string.Video_resolution), WORK_MODE_VIDEO_NORMAL, CONFIG_VIDEO_VIDEO_RESOLUTION);
                break;
            case R.id.rl_loop_recording:
                HisiSubSettingActivity.startAction(this, HisiSubSettingActivity.TYPE_WORK_MODE_TYPE, getString(R.string.cyclic_record), WORK_MODE_VIDEO_LOOP, CONFIG_VIDEO_LOOP_TYPE);
                break;
            case R.id.rl_photo_resolution:
                HisiSubSettingActivity.startAction(this, HisiSubSettingActivity.TYPE_WORK_MODE_TYPE, getString(R.string.photo_resolution), WORK_MODE_PHOTO_SINGLE, CONFIG_PHOTO_RESOLUTION);
                break;
            case R.id.rl_format_camera:
                AlertDialog.Builder altBSdformat = new AlertDialog.Builder(this);
                altBSdformat.setMessage(getString(R.string.set_isneedformat));
                altBSdformat.setTitle(getString(R.string.wifi_stopwarning));
                altBSdformat.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        FormatTask formatTask = new FormatTask();
                        formatTask.execute();
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
            case R.id.rl_screen_protector:
                HisiSubSettingActivity.startAction(this, HisiSubSettingActivity.TYPE_SCREEN_SLEEP, getString(R.string.screensavers), 0, 0);
                break;
            case R.id.rl_restore_settings:
                AlertDialog.Builder altBSdfactory = new AlertDialog.Builder(this);
                altBSdfactory.setMessage(getString(R.string.camset_recovery_question));
                altBSdfactory.setTitle(getString(R.string.notice));
                altBSdfactory.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        ResetTask resetTask = new ResetTask();
                        resetTask.execute();
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
}
