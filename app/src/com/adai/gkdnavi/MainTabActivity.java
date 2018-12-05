package com.adai.gkdnavi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.Hjni.HbxFishEye;
import com.adai.camera.CameraConstant;
import com.adai.gkd.bean.LogoBean;
import com.adai.gkd.bean.PhoneAndCameraInfoBean;
import com.adai.gkd.bean.UpdateCorrectBean;
import com.adai.gkd.bean.params.PhoneAndCamParam;
import com.adai.gkd.bean.request.CameraVersionBean;
import com.adai.gkd.bean.request.PhoneAndCameraPageBean;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.fragment.MineFragment;
import com.adai.gkdnavi.fragment.SimpleApplicationFragment;
import com.adai.gkdnavi.fragment.SquareFragment;
import com.adai.gkdnavi.utils.CameraUpdateUtil;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;
import com.ffmpeg.FFmpeg;
import com.filepicker.adapters.SectionsPagerAdapter;
import com.oss.utils.OSSRequestUtil;

import java.io.File;

import static com.example.ipcamera.application.VLCApplication.runBackground;


public class MainTabActivity extends BaseActivity implements View.OnClickListener {
    private ViewPager mViewPager;
    private RadioGroup mRadioGroup;
    private RadioButton mRbApp, mRbAblum, mRbMine;
    private SectionsPagerAdapter adapter;
    private static final String CAME_LOGO_UPDATE_TIME = "CAME_LOGO_UPDATE_TIME";
    private static final String DEFAULT_LOGO_UPDATE_TIME = "DEFAULT_LOGO_UPDATE_TIME";
    private AlbumFragment mAlbumFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main_tab);
        initView();
        initEvent();
        postPhoneAndCamInfo();
        getCameraInfo();
        getLogo();
        getUpdateCorrectParam();
    }

    private void getUpdateCorrectParam() {
        String soft_version = HbxFishEye.GetCalibrationId() + "";
        Log.e("9999", "soft_version = " + soft_version);
//        soft_version = "0";

        RequestMethods.getUpdateCorrect(soft_version, new HttpUtil.Callback<UpdateCorrectBean>() {
            @Override
            public void onCallback(UpdateCorrectBean result) {
                if (result == null) {
                    return;
                }
                UpdateCorrectBean.DataBean data = result.data;
                if (data == null) {
                    return;
                }
//                Log.e("9999", "is_upgrade = " + result.data.is_upgrade + "soft_version = " + data.soft_version + "file_url = " + data.file_url);

                if (result.data.is_upgrade == 1) {
                    downUpdateCorrectParam(data);
                }
            }
        });
    }


    private void getLogo() {
        final String cameraVersionCurrent = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");

        RequestMethods.getLogo(getApplication().getPackageName(), getLanguage(), cameraVersionCurrent, new HttpUtil.Callback<LogoBean>() {
            @Override
            public void onCallback(LogoBean result) {
                if (result == null) {
                    return;
                }
                LogoBean.DataBean data = result.data;
                if (data == null) {
                    return;
                }
                String lastUpdateTime = SpUtils.getString(MainTabActivity.this, CAME_LOGO_UPDATE_TIME, "");
                if (lastUpdateTime.equals(data.update_time)) {
                    //如果上次更新的时间和服务器返回的时间是相同的那么就不用更新
                    return;
                }
                downLoadLogo(data);
            }
        });
    }

    @Override
    protected void initView() {
        mRbApp = (RadioButton) findViewById(R.id.rb_application);
//        mRbSquare = (RadioButton) findViewById(R.id.rb_square);
        mRbAblum = (RadioButton) findViewById(R.id.rb_album);
        mRbMine = (RadioButton) findViewById(R.id.rb_mine);
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_main);
        mRadioGroup.check(R.id.rb_album);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setOffscreenPageLimit(2);
        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mAlbumFragment = new AlbumFragment();
        adapter.addFragment(mAlbumFragment, getString(R.string.photo_album));
        adapter.addFragment(new SimpleApplicationFragment(), getString(R.string.remote));
//        adapter.addFragment(new SquareFragment(), getString(R.string.square));
        adapter.addFragment(new MineFragment(), getString(R.string.about_app));
        mViewPager.setAdapter(adapter);
    }

    private void initEvent() {
        mRbApp.setOnClickListener(this);
//        mRbSquare.setOnClickListener(this);
        mRbAblum.setOnClickListener(this);
        mRbMine.setOnClickListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0://相册
                        mRbAblum.setChecked(true);
                        break;
                    case 1://应用
                        mRbApp.setChecked(true);
                        break;
//                    case 1://广场
//                        mRbSquare.setChecked(true);
//                        SquareFragment squareFragment = (SquareFragment) adapter.getItem(1);
//                        squareFragment.loadFirstPage();
//                        break;

                    case 2://我的
                        mRbMine.setChecked(true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 上传手机和摄像头信息
     */
    private void postPhoneAndCamInfo() {
        String cameraVersionCurrent = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");
//        boolean isFirst = SpUtils.getBoolean(this, "phoneAndCam", false);
//        if (isFirst) {
        //如果是第一次
//            if (!TextUtils.isEmpty(cameraVersionCurrent)) {
        PhoneAndCamParam phoneAndCamParam = new PhoneAndCamParam();
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String imei = telephonyManager.getDeviceId();
//        String mobile_phone = telephonyManager.getLine1Number();
        phoneAndCamParam.imei = imei;
//        phoneAndCamParam.mobile_phone = mobile_phone;
        phoneAndCamParam.cam_version = cameraVersionCurrent;
        phoneAndCamParam.model = Build.MODEL;
        phoneAndCamParam.system_version = Build.VERSION.RELEASE;
        phoneAndCamParam.app_version = BuildConfig.VERSION_NAME;
        phoneAndCamParam.package_name = getApplication().getPackageName();
        phoneAndCamParam.language_code = getLanguage();
        phoneAndCamParam.deviceid = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");
        phoneAndCamParam.wifiMac = WifiUtil.getBssid();
        RequestMethods.postPhoneAndCamInfo(phoneAndCamParam, new HttpUtil.Callback<PhoneAndCameraPageBean>() {
            @Override
            public void onCallback(PhoneAndCameraPageBean result) {
                if (result.ret == 0) {
                    PhoneAndCameraInfoBean data = result.data;
                    if (data != null) {
                        if (!TextUtils.isEmpty(data.oss_address)) {
                            Log.e(_TAG_, "onCallback: " + data.oss_address);
                            SpUtils.putString(MainTabActivity.this, "oss_address", data.oss_address);
                            OSSRequestUtil.getInstance().setOssAddress(data.oss_address);
                        }
                        SpUtils.putString(MainTabActivity.this, "authority_level", data.authority_level);
                        SpUtils.putBoolean(MainTabActivity.this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, data.is_support_report != 0);
                    }
                }
            }
        });
//            }
//        }

    }

    /**
     * 获取摄像头信息
     */
    private void getCameraInfo() {
        final String cameraVersionCurrent = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");
        if (TextUtils.isEmpty(cameraVersionCurrent)) {
            return;
        }
        RequestMethods.getCameraInfo(cameraVersionCurrent, new HttpUtil.Callback<CameraVersionBean>() {
            @Override
            public void onCallback(CameraVersionBean cameraVersionBean) {
                if (cameraVersionBean == null) {
                    return;
                }
                CameraVersionBean.CameraVersionData data = cameraVersionBean.data;
                if (data == null) {
                    return;
                }
                String server_cam_version = data.cam_version;
                final String update_time = data.logo_update_time;
                if (!TextUtils.isEmpty(server_cam_version)) {
                    SpUtils.putString(MainTabActivity.this, CameraConstant.CAMERA_FACTORY, data.factory_name);
                    SpUtils.putString(MainTabActivity.this, CameraConstant.CAMERA_VERSION, server_cam_version);
                    SpUtils.putString(MainTabActivity.this, CameraConstant.CAMERA_MD5, data.cam_md5);
                    SpUtils.putBoolean(MainTabActivity.this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, data.if_support_report != 0);
                    if (cameraVersionCurrent.length() >= 16 && server_cam_version.length() >= 15) {
                        String firmwareVersionCurrent = cameraVersionCurrent.substring(16);
                        if (firmwareVersionCurrent.substring(0, 7).equals(server_cam_version.substring(0, 7))) {
                            if (firmwareVersionCurrent.compareTo(server_cam_version) < 0) {
                                //如果当前版本小于服务器上面的版本就下载固件
                                CameraUpdateUtil cameraUpdateUtil = new CameraUpdateUtil(MainTabActivity.this);
                                if (!cameraUpdateUtil.checkFile(data.cam_md5)) {
                                    cameraUpdateUtil.downloadFile(data.cam_url, data.cam_md5);
                                }
                            }
                        }
//                        String lastUpdateTime = SpUtils.getString(MainTabActivity.this, CAME_LOGO_UPDATE_TIME, "");
//                        if (lastUpdateTime.equals(update_time)) {
//                            //如果上次更新的时间和服务器返回的时间是相同的那么就不用更新
//                            return;
//                        }
//                        downLoadLogo(data, update_time);
                    }
                } else {
                    //如果服务器没有返回cameraVersion并且上次更新的时间不等于服务器给的时间，那么就下载默认的图片
//                    String lastUpdateTime = SpUtils.getString(MainTabActivity.this, DEFAULT_LOGO_UPDATE_TIME, "");
//                    if (lastUpdateTime.equals(update_time)) {
//                        return;
//                    }
//                    downLoadLogo(data, update_time);

                }

            }
        });
    }

    /**
     * 下载logo
     */
    private void downLoadLogo(final LogoBean.DataBean data) {
        File file = new File(GuideActivity.LOGO_PATH);
        if (file.exists()) {
            //如果文件已经存在了就删除再下载
            file.delete();
        }
        //开始下载logo
        HttpUtil.getInstance().downloadFile(data.logo_image, GuideActivity.LOGO_PATH, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                SpUtils.putString(MainTabActivity.this, CAME_LOGO_UPDATE_TIME, data.update_time);
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownladFail() {
                LogUtils.i("下载失败");
                //下载失败了就删掉下载到一部分的文件
                File file = new File(GuideActivity.LOGO_PATH);
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }

    /**
     * 下载全景顶点数据
     */
    private void downUpdateCorrectParam(final UpdateCorrectBean.DataBean data) {
        final File calibrationFolder = new File(VLCApplication.CALIBRATION_PATH);
        final File file = new File(VLCApplication.CALIBRATION_PATH + "/calibration.bin");


        //开始下载
        HttpUtil.getInstance().downloadFile(data.file_url, VLCApplication.CALIBRATION_PATH + "/calibration.bin1", new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                LogUtils.i("下载成功");

                if (file.exists()) {
                    file.delete();
                }
                renameFile(calibrationFolder.getAbsolutePath() + "/calibration.bin1", calibrationFolder.getAbsolutePath() + "/calibration.bin");

                HbxFishEye.Init(calibrationFolder.getAbsolutePath());
                runBackground(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("9999", "run: updateVertex");
                        HbxFishEye.UpdateVertex();
                        Log.e("9999", "run: 1111 updateVertex");
                    }
                });
            }

            @Override
            public void onDownloading(int progress) {
                LogUtils.i("下载中");

            }

            @Override
            public void onDownladFail() {
                LogUtils.i("下载失败");
                //下载失败了就删掉下载到一部分的文件
                File file = new File(VLCApplication.CALIBRATION_PATH + "/calibration.bin1");
                if (file.exists()) {
                    file.delete();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rb_album:
                mViewPager.setCurrentItem(0);
                break;
            case R.id.rb_application:
                mViewPager.setCurrentItem(1);
                break;
//            case R.id.rb_square:
//                mViewPager.setCurrentItem(1);
//                break;
            case R.id.rb_mine:
                mViewPager.setCurrentItem(2);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getRepeatCount() == 0) {
                if (mAlbumFragment != null && mAlbumFragment.isEditMode()) {
                    mAlbumFragment.setEditMode(false);
                } else {
                    this.exitApp();
                }
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MinuteFileDownloadManager.getInstance().cancle();
        MinuteFileDownloadManager.getInstance().removeAllObserver();
    }

    private long exitTime = 0;

    /**
     * 退出程序
     */
    private void exitApp() {
        // 判断2次点击事件时间
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(MainTabActivity.this, getResources().getString(R.string.exit_program), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(MainTabActivity.this);
            int netmode = shareprefrence.getInt("netmode", 0);
            FFmpeg.getInstance(getApplicationContext()).releaseTask();
            if (netmode == 1) {
                WifiUtil.getInstance().stopWifiAp();
                CheckSApContactService.getInstance().setApkisrunning(false);
                Log.e("9527", "apkisrunning 2= " + CheckSApContactService.apkisrunning);
            }
            finish();

        }
    }

    /**
     * 重命名文件
     *
     * @param oldPath 原来的文件地址
     * @param newPath 新的文件地址
     */
    public static void renameFile(String oldPath, String newPath) {
        File oleFile = new File(oldPath);
        File newFile = new File(newPath);
        //执行重命名
        oleFile.renameTo(newFile);
    }

}
