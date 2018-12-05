package com.adai.gkdnavi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.adai.camera.CameraConstant;
import com.adai.gkd.bean.request.DeviceInfoPageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.adapter.ViewPagerAdapter;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.bumptech.glide.Glide;
import com.example.ipcamera.application.VLCApplication;
import com.tbruyelle.rxpermissions2.Permission;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.functions.Consumer;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.example.ipcamera.application.VLCApplication.APP_ROOT;
import static com.example.ipcamera.application.VLCApplication.CACHE;
import static com.example.ipcamera.application.VLCApplication.CALIBRATION_PATH;
import static com.example.ipcamera.application.VLCApplication.CUT_VIDEO_PATH;
import static com.example.ipcamera.application.VLCApplication.DOWNLOADPATH;
import static com.example.ipcamera.application.VLCApplication.LOCAL_PICTURE;
import static com.example.ipcamera.application.VLCApplication.LOG_PATH;
import static com.example.ipcamera.application.VLCApplication.OTA_PATH;
import static com.example.ipcamera.application.VLCApplication.TEMP_PATH;

/*
 * 引导页  介绍产品
 */
public class GuideActivity extends BaseActivity {
    //        implements OnClickListener,OnPageChangeListener {
//    // 定义ViewPager对象
//    private ViewPager viewPager;
//    // 定义ViewPager适配器
//    private ViewPagerAdapter vpAdapter;
//    // 定义一个ArrayList来存放View
//    private ArrayList<View> views;
//    // 引导图片资源
//    private static final int[] pics = {R.drawable.guide1, R.drawable.guide2,
//            R.drawable.guide3, R.drawable.guide4};
//    // 底部小点的图片
//    private ImageView[] points;
//    // 记录当前选中位置
//    private int currentIndex;
//    // 进入首页按钮
//    private Button login;
//    private Button retry;
//    private ImageView baidu_tts_status;
//    SharedPreferences sharedPreferences;
//    boolean isFirstRun;
//    Editor editor;
//    private VLCApplication app;
//
//    private boolean initsuccess;
//    //    private View app_mode_select;
////    private View car_model_select;
////    private View map_mode_select;
//    private CheckBox remindchoice;
//    //	private boolean lastchoice=false;
//    int versioncode = 1;
//    int lastcode = 0;

    private String TAG = getClass().getSimpleName();
    private ImageView logo;

    public static String LOGO_PATH = VLCApplication.APP_ROOT + "logo.jpg";
    String mPermissions[] = new String[]{READ_PHONE_STATE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION,
            READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA};//    READ_CONTACTS, WRITE_CONTACTS,
    int permissionsCount = 0;
    boolean isPermissionSuccess = true;
    List<String> deniedAlwaysSet = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guide);
        logo = findViewById(R.id.splash_logo);
        String cameraVersionCurrent = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");
        if (TextUtils.isEmpty(cameraVersionCurrent)) {
            Glide.with(this).load(R.drawable.start_en).into(logo);
        } else {
//            "NT;NT96655;LSX0A;LSX01;100;LSX-G9;V03;G9-20181115V2.0;"
            String[] split = cameraVersionCurrent.split(";");
            if (split.length == 8) {
                if ("LSXG9".equals(split[5])) {
                    Glide.with(this).load(R.drawable.start).into(logo);
                } else {
                    Glide.with(this).load(R.drawable.start_en).into(logo);
                }
            } else {
                Glide.with(this).load(R.drawable.start_en).into(logo);
            }
        }
        initPermission();

    }

    private void initPermission() {
        RxPermissions rxPermission = new RxPermissions(GuideActivity.this);
        rxPermission.requestEach(mPermissions)
                .subscribe(new Consumer<Permission>() {
                    @Override
                    public void accept(Permission permission) throws Exception {
                        permissionsCount++;
                        if (permission.granted) {// 用户已经同意该权限
                            Log.e("9527", permission.name + " is granted.用户已经同意该权限");
                        } else if (permission.shouldShowRequestPermissionRationale) {// 用户拒绝了该权限，没有选中『不再询问』（Never ask again）,那么下次再次启动时，还会提示请求权限的对话框
                            isPermissionSuccess = false;
                            deniedAlwaysSet.add(permission.name);
                            Log.e("9527", permission.name + " is denied. More info should be provided.用户拒绝了该权限，没有选中『不再询问』");
                        } else {// 用户拒绝了该权限，并且选中『不再询问』
                            Log.e("9527", permission.name + " is denied.用户拒绝了该权限，并且选中『不再询问』");
                            isPermissionSuccess = false;
                            deniedAlwaysSet.add(permission.name);
                        }
                        if (permissionsCount == mPermissions.length) {
                            if (isPermissionSuccess) {
                                initFile();
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        Intent intent = new Intent(GuideActivity.this, MainTabActivity.class);
                                        startActivity(intent);
                                        GuideActivity.this.finish();
                                    }
                                }, 1500);

                                CurrentUserInfo.initUserinfo(mContext.getApplicationContext());
                                VLCApplication application = (VLCApplication) getApplication();
                                Throwable lastlog = application.getLastLog();
                                if (lastlog != null) {
                                    MobclickAgent.reportError(GuideActivity.this, lastlog);
                                }

                            } else {
                                showAlertSettingDialog(getPermissions(deniedAlwaysSet));
                            }
                        }
                    }
                });
    }

    private String getPermissions(List<String> mlist) {
        String deniedString;
        Set<String> deniedSet = new HashSet<>();

        for (int i = 0; i < mlist.size(); i++) {
            switch (mlist.get(i)) {
                case ACCESS_FINE_LOCATION:     //位置信息
                case ACCESS_COARSE_LOCATION:
                    deniedSet.add(getResources().getString(R.string.permission_location));
                    break;
                case READ_PHONE_STATE:       //电话
                case CALL_PHONE:
                    deniedSet.add(getResources().getString(R.string.permission_phone));
                    break;
                case READ_CONTACTS:           //通讯录
                    deniedSet.add(getResources().getString(R.string.permission_contacts));
                    break;
                case READ_EXTERNAL_STORAGE:        //存储空间
                case WRITE_EXTERNAL_STORAGE:
                    deniedSet.add(getResources().getString(R.string.permission_storage));
                    break;
                case CAMERA:                    //相机
                    deniedSet.add(getResources().getString(R.string.permission_camera));
                    break;
                case RECORD_AUDIO:       //麦克风
                    deniedSet.add(getResources().getString(R.string.permission_microphone));
                    break;
            }
        }
        deniedString = deniedSet.toString();
        Log.e(TAG, "deniedString = " + deniedString);
        return deniedString;
    }

    protected void showAlertSettingDialog(String deniedPermission) {
        if (isFinishing())
            return;
        AlertDialog.Builder builder = new AlertDialog.Builder(GuideActivity.this, R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle(getResources().getString(R.string.permission_request));
        builder.setMessage(getResources().getString(R.string.permission_checkmessage, deniedPermission));
        builder.setPositiveButton(getResources().getString(R.string.to_set),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                        finish();
                    }
                }
        );
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }


    private void initFile() {
        File destDir = new File(APP_ROOT);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File downloads = new File(DOWNLOADPATH);
        if (!downloads.exists()) {
            downloads.mkdirs();
        }
        File log = new File(LOG_PATH);
        if (!log.exists()) {
            log.mkdirs();
        }
        File localPicture = new File(LOCAL_PICTURE);
        if (!localPicture.exists()) {
            localPicture.mkdirs();
        }
        File ota = new File(OTA_PATH);
        if (!ota.exists()) {
            ota.mkdirs();
        }
        File temp = new File(TEMP_PATH);
        if (!temp.exists()) {
            temp.mkdirs();
        }
        File cutvideo = new File(CUT_VIDEO_PATH);
        if (!cutvideo.exists()) {
            cutvideo.mkdirs();
        }
        File cache = new File(CACHE);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        File calibrationFolder = new File(CALIBRATION_PATH);
        if (!calibrationFolder.exists()) {
            calibrationFolder.mkdirs();
        }

    }


//    private void initData() {
//
//        // 定义一个布局并设置参数
//        LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
//                LinearLayout.LayoutParams.FILL_PARENT,
//                LinearLayout.LayoutParams.FILL_PARENT);
//        // 初始化引导图片列表
//        for (int i = 0; i < pics.length; i++) {
//            ImageView iv = new ImageView(this);
//            iv.setLayoutParams(mParams);
//            iv.setImageResource(pics[i]);
//            views.add(iv);
//
//        }
//        // 设置数据
//        viewPager.setAdapter(vpAdapter);
//        // 设置监听
//        viewPager.setOnPageChangeListener(this);
//        // 初始化底部小点
//        initPoint();
//
//    }
//
//    private void initPoint() {
//
//        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llPoint);
//
//        points = new ImageView[pics.length];
//
//        // 循环取得小点图片
//        for (int i = 0; i < pics.length; i++) {
//            // 得到一个LinearLayout下面的每一个子元素
//            points[i] = (ImageView) linearLayout.getChildAt(i);
//            // 默认都设为灰色
//            points[i].setEnabled(true);
//            // 给每个小点设置监听
//            points[i].setOnClickListener(this);
//            // 设置位置tag，方便取出与当前位置对应
//            points[i].setTag(i);
//        }
//        linearLayout.setVisibility(View.VISIBLE);
//        // 设置当面默认的位置
//        currentIndex = 0;
//        // 设置为白色，即选中状态
//        points[currentIndex].setEnabled(false);
//    }
//
//
//    @Override
//    protected void initView() {
//
//        // 实例化ArrayList对象
//        views = new ArrayList<>();
//        // 实例化ViewPager
//        viewPager = (ViewPager) findViewById(R.id.viewpager);
//        // 实例化ViewPager适配器
//        vpAdapter = new ViewPagerAdapter(views);
//        baidu_tts_status = (ImageView) findViewById(R.id.baidu_tts_status);
//        login = (Button) findViewById(R.id.login);
//        login.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                editor = sharedPreferences.edit();
//                editor.putBoolean("isFirstRun", false);
//                editor.apply();
//                Intent intent = new Intent(GuideActivity.this,
//                        MainTabActivity.class);
//                startActivity(intent);
//                GuideActivity.this.finish();
//
//            }
//        });
//
//        retry = (Button) findViewById(R.id.retry);
//        retry.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//
//                LogUtils.e("111");
//                UIHandler.sendEmptyMessage(1);
//                LogUtils.e("222");
//
////                new Handler().postDelayed(new Runnable() {
////                    public void run() {
////                        initsuccess = speechUtil.initAuthInfo();
////                        LogUtils.e("retry initsuccess = " + initsuccess);
////                        if (initsuccess) {
////                            UIHandler.sendEmptyMessage(2);
////                        } else {
////                            UIHandler.sendEmptyMessage(3);
////                        }
////
////                    }
////                }, 100);
////				new InitAuthTask().execute(speechUtil);
//            }
//        });
//    }
//
//    @Override
//    public void onPageScrollStateChanged(int position) {
//
//
//    }
//
//    @Override
//    public void onPageScrolled(int arg0, float arg1, int arg2) {
//
//
//    }
//
//    @Override
//    public void onPageSelected(int position) {
//
//        setCurDot(position);
//
//        if (position == pics.length - 1) {
//            baidu_tts_status.setVisibility(View.VISIBLE);
//            baidu_tts_status.setImageResource(R.drawable.init_baidu_tts);
//            login.setVisibility(View.INVISIBLE);
//            retry.setVisibility(View.INVISIBLE);
//            UIHandler.sendEmptyMessage(2);
//
////            new Handler().postDelayed(new Runnable() {
////                public void run() {
////
////
////                    speechUtil = new SpeechUtil(app);
////                    initsuccess = speechUtil.initAuthInfo();
////                    LogUtils.e("initsuccess = " + initsuccess);
////                    if (initsuccess) {
////
////                    } else {
////                        UIHandler.sendEmptyMessage(3);
////
////                    }
////
////                }
////            }, 200);
////			new InitAuthTask().execute(speechUtil);
//        } else {
//            login.setVisibility(View.INVISIBLE);
//            retry.setVisibility(View.INVISIBLE);
//            baidu_tts_status.setVisibility(View.INVISIBLE);
//        }
//    }
//
//    private void setCurDot(int position) {
//
//        if (position < 0 || position > pics.length - 1
//                || currentIndex == position) {
//            return;
//        }
//        points[position].setEnabled(false);
//        points[currentIndex].setEnabled(true);
//
//        currentIndex = position;
//    }
//
//    @Override
//    protected void onDestroy() {
//
//        super.onDestroy();
//
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//            default:
//                int position = (Integer) v.getTag();
//                setCurView(position);
//                setCurDot(position);
//                break;
//        }
//    }
//
//    private void gotoMain() {
//        Intent intent = new Intent(GuideActivity.this,
//                MainTabActivity.class);
//        startActivity(intent);
////		sharedPreferences.edit().putBoolean("isremindchoice", remindchoice.isChecked()).commit();
//        GuideActivity.this.finish();
//    }
//
//    private void setCurView(int position) {
//
//        if (position < 0 || position >= pics.length) {
//            return;
//        }
//        viewPager.setCurrentItem(position);
//    }
//
//    private Handler UIHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1: // 初始状态
//                    LogUtils.e("333");
//                    baidu_tts_status.setVisibility(View.VISIBLE);
//                    baidu_tts_status.setImageResource(R.drawable.init_baidu_tts);
//                    login.setVisibility(View.INVISIBLE);
//                    retry.setVisibility(View.INVISIBLE);
//                    LogUtils.e("444");
//
//                    break;
//                case 2: // 成功状态
//                    baidu_tts_status.setVisibility(View.VISIBLE);
//                    baidu_tts_status
//                            .setImageResource(R.drawable.init_baidu_tts_success);
//                    login.setVisibility(View.VISIBLE);
//                    retry.setVisibility(View.INVISIBLE);
//                    editor = sharedPreferences.edit();
//                    editor.putBoolean("isFirstRun", false);
//                    editor.putInt("versioncode", versioncode);
//                    editor.apply();
////                    app_mode_select.setVisibility(View.VISIBLE);
//                    setAppmode(1);
//                    gotoMain();
////                    map_mode_select.setVisibility(View.VISIBLE);
//                    break;
//                case 3:// 失败状态
//                    baidu_tts_status.setVisibility(View.VISIBLE);
//                    baidu_tts_status
//                            .setImageResource(R.drawable.init_baidu_tts_fail);
//                    login.setVisibility(View.INVISIBLE);
//                    retry.setVisibility(View.VISIBLE);
//                    break;
//            }
//
//        }
//    };

}
