package com.adai.gkdnavi;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.adai.camera.CameraConstant;
import com.adai.gkd.bean.request.CameraVersionBean;
import com.adai.gkd.contacts.Contacts;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public class GuanyuActivity extends BaseActivity implements OnClickListener {
    private static final String UPDATINFO = "clickupdate";
    private String ServerApkVer = null;
    private String serverApkUrl = null;
    private String serverMD5 = null;
    protected static final String TAG = "GuanyuActivity";
    private static final int haved_newversion = 0;
    private static final int Backdownload = 1;
    private static final int Serverexception = 2;
    private static final int NetOrSerexception = 3;
    private static final int PROGRESS = 4;
    private static final int INSTALL = 5;
    private static TextView localnumber, factory;
    private String strLocalVer = null;
    SharedPreferences gspOTACont = null;
    private TextView xuliehao, shexiangtounumber;
    public static final int TOOTHVERSION = 0x110;
    private boolean isConnectedBlue = false;
    private boolean mIsShowingDialog = false;
    private boolean mShowingNewestVersionDialog = false;
    private DownloadService mDownloadService;
    private ProgressButton mSoftWareUpdate;
    private ServiceConnection conn;
    private int is_beta = 0;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PROGRESS:
                    mSoftWareUpdate.setProgressEnable(true);
                    mSoftWareUpdate.setProgressBackground(getResources().getDrawable(R.drawable.progress_pressed));
                    mSoftWareUpdate.setBackgroundResource(R.drawable.progress_bg);
                    mSoftWareUpdate.setProgress(msg.arg2);
                    mSoftWareUpdate.setText(msg.arg2 + "%");
                    break;
                default:
                    break;
            }
        }

    };
    DownloadService.OnShowNumber callback = new DownloadService.OnShowNumber() {

        @Override
        public void count(int load) {
            Message message = Message.obtain();
            message.arg2 = load;
            message.what = PROGRESS;
            handler.sendMessage(message);
        }
    };

    private void bindService() {
        Intent service = new Intent("com.adai.gkdnavi.downloadservice");
        service.setPackage(getPackageName());
        conn = new ServiceConnection() {

            @Override
            public void onServiceDisconnected(ComponentName name) {

                mDownloadService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mDownloadService = ((DownloadService.MyBinder) service).getService();
                mDownloadService.setShowNumber(callback);
                Log.e("downloadservice", "downloadservice--------------------------------");
            }
        };
        boolean sucess = bindService(service, conn, BIND_AUTO_CREATE);
        System.out.println("bind");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_guanyu);

        initView();
        gspOTACont = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
        strLocalVer = gspOTACont.getString("gspLocalVerNo", "");//版本号
        localnumber = (TextView) findViewById(R.id.localbanbennumber);
        localnumber.setText(strLocalVer);
        getCamFactory();
//        Log.e(TAG, "GuanyuActivity onCreate strLocalVer =" + strLocalVer);

        IntentFilter filter = new IntentFilter(UPDATINFO);
        registerReceiver(myUpdateInfo, filter);

    }


    protected void initView() {
        super.initView();
        setTitle(R.string.version_info);
//		xinghao = (TextView) findViewById(R.id.xinghaonumber);
        String cameraVersion = SpUtils.getString(this, CameraConstant.CAMERA_FIRMWARE_VERSION, "");
//        if (cameraVersion.length() >= 15) {
//            cameraVersion = cameraVersion.substring(16);
//        }
        factory = (TextView) findViewById(R.id.manufacturers);
//        factory.setText(SpUtils.getString(this, CameraConstant.CAMERA_FACTORY, ""));
        xuliehao = (TextView) findViewById(R.id.xuliehaonumber);
//        if (cameraVersion.length() >= 7) {
//            xuliehao.setText(cameraVersion.substring(4, 7));
//        }
        shexiangtounumber = (TextView) findViewById(R.id.shexiangtounumber);
        shexiangtounumber.setText(cameraVersion);
        mSoftWareUpdate = (ProgressButton) findViewById(R.id.softwareupdate);
        mSoftWareUpdate.setOnClickListener(this);
        //mSoftWareUpdate.setProgressEnable(false);
        if (VLCApplication.mIsDownloadedAPK) {
            setButtonProperty(getString(R.string.In_the_download), false, R.color.gray);
        }
        bindService();

//        findViewById(R.id.devicemodeline).setOnClickListener(this);
    }

    private void showDetermineDownloadDialog() {
        AlertDialog.Builder bulider = new AlertDialog.Builder(GuanyuActivity.this);
        View view = LayoutInflater.from(GuanyuActivity.this).inflate(R.layout.network_state, null);
        Button btnUpdate = (Button) view.findViewById(R.id.btn_network_update);
        Button btnCancel = (Button) view.findViewById(R.id.btn_network_cancel);
        bulider.setView(view);
        final AlertDialog dialog = bulider.create();
        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
//				Intent intent=new Intent(GuanyuActivity.this, DownloadService.class);
//				 //SharedPreferences  apkurl1Preferences = getSharedPreferences("ServerVersion", Context.MODE_PRIVATE);
//			    //String   apkurl1get  = gspOTACont.getString("gspOtaApkUrl", "");
//				intent.putExtra("otapakurl", serverApkUrl);
//				//由intent启动service，后台运行下载进程，在服务中调用notifycation状态栏显示进度条
//				//mContext是个上下文对象，由activity传递过来的，就相当于activity
//				GuanyuActivity.this.startService(intent);

                if (!VLCApplication.mIsDownloadedAPK) {
                    Intent intent = new Intent(GuanyuActivity.this, DownloadService.class);
                    intent.putExtra("otapakurl", serverApkUrl);
                    GuanyuActivity.this.startService(intent);
//					mDownloadService.loadFile(serverApkUrl,10001);
                    localnumber = (TextView) findViewById(R.id.localbanbennumber);
                    localnumber.setText(" " + strLocalVer);
                    VLCApplication.mIsDownloadedAPK = true;
                    //mIsShowingDialog= false;
                    setButtonProperty(getResources().getString(R.string.In_the_download), false, R.color.gray);
                }
                Log.e(TAG, "showDetermineDownloadDialog GuanyuCheckOTA");
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // 点击取消按钮响应事件：
                dialog.dismiss();
                setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void initControls() {
        mIsShowingDialog = true;
        TextView text;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        AlertDialog.Builder bulider = new AlertDialog.Builder(GuanyuActivity.this);
        View view = LayoutInflater.from(GuanyuActivity.this).inflate(R.layout.chekversion_dialog, null);
        Button btnUpdate = (Button) view.findViewById(R.id.btn_update);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        //服务器版本号
        //SharedPreferences sPreferences = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
        //String   versionnumberget  = gspOTACont.getString("gspOtaAPKVer", "");
        //Log.i(otaTag,"GuanyuActivity initControls getxml versionnumber = " + versionnumberget);
        String strUpdatecontent = gspOTACont.getString("updatecontent", "");
        TextView txtUpdateMsg = (TextView) view.findViewById(R.id.contentone);
        txtUpdateMsg.setText(strUpdatecontent);


        TextView Versionnumber = (TextView) view.findViewById(R.id.banbennumber);
        Versionnumber.setText(getString(R.string.newcheckversion) + ":" + ServerApkVer);
        bulider.setView(view);
        final AlertDialog dialog = bulider.create();
        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 点击立即更新按钮响应事件：
                dialog.dismiss();
                //########################
//					ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//					NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                mIsShowingDialog = false;
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {//WIFI
                    Log.e(TAG, "连接wifi网络");
                    if (!VLCApplication.mIsDownloadedAPK) {
                        Intent intent = new Intent(GuanyuActivity.this, DownloadService.class);
                        intent.putExtra("otapakurl", serverApkUrl);
                        GuanyuActivity.this.startService(intent);
//									mDownloadService.loadFile(serverApkUrl,10001);
                        localnumber = (TextView) findViewById(R.id.localbanbennumber);
                        localnumber.setText(" " + strLocalVer);
                        VLCApplication.mIsDownloadedAPK = true;
                        //setButtonProperty("下载中...", false, R.color.gray);
                        //bindService();
                    }
                } else if (type == ConnectivityManager.TYPE_MOBILE) {//MOBILE
                    Log.e(TAG, "连接移动网络");
                    showDetermineDownloadDialog();
                }
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 点击取消按钮响应事件：
                dialog.dismiss();
                mIsShowingDialog = false;
                setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void GuanyuCheckOTA() {
        new Thread() {
            @Override
            public void run() {
                try {
//            		String httpUrl = "http://120.27.194.224/upload/appota/gh801l.xml";
//					String httpUrl = "http://192.168.1.39/versionupdate/gkd801l.xml";
                    String httpUrl = Contacts.VERSION_UPDATE;
                    String httpUrl_beta = Contacts.BETA_VERSION_UPDATE;
                    if (is_beta == 1) {
                        httpUrl = httpUrl_beta;
                    }
//				httpUrl=httpUrl_beta;
                    HttpClient httpClient = new DefaultHttpClient();
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
                    HttpGet get = new HttpGet(httpUrl);
                    //System.out.println(">>>>>>>>>" + get);
                    HttpResponse response;
                    InputStream input;
                    response = httpClient.execute(get);
                    Message msg = Message.obtain();
//					 if(isNotWifi){
//						 showDetermineDownloadDialog();
//						 isNotWifi = false;
//					 }
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream responseStream = entity.getContent();
                        input = responseStream;
                        List<UpdataInfo> infos = CheckVersionTask.getUpdataInfo(input);
                        if (infos == null || infos.size() <= 0) {
                            msg.what = NetOrSerexception;
                            Log.e(TAG, "NetWork error");
                            mHandler.sendMessage(msg);
                            return;
                        }
                        SharedPreferences ServerVersionset = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = ServerVersionset.edit();
                        UpdataInfo updateInfo = infos.get(0); // 调用解析方法
                        for (UpdataInfo info : infos) {
                            if ("app".equals(info.getType())) {
                                updateInfo = info;
                            } else if ("camera".equals(info.getType())) {
                                editor.putString("camera_md5", info.getMd5());
                                editor.putString("camera_version", info.getVersion());
                                editor.putString("camera_url", info.getUrl());
                                editor.putString("camera_defaultcontent", info.getContentdefault());
                            }
                        }
                        ServerApkVer = updateInfo.getVersion(); // 获得服务器版本
                        Log.e(TAG, "ServerApkVer=" + ServerApkVer);
                        //String string = SpUtils.getString(getApplicationContext(), "successdownloadapkfile", "");

                        serverApkUrl = updateInfo.getUrl(); //get server xml apk url
                        //String substring = serverApkUrl.substring(serverApkUrl.lastIndexOf("/")+1);
                        //Log.e(TAG, "substring+GuanyuCheckOTA="+substring);
                        serverMD5 = updateInfo.getMd5(); //get server xml apk url
                        //Log.e(otaTag, "ota server info : ServerVersion=" +ServerApkVer + ",serverApkUrl" + serverApkUrl);
                        //String yanzhen = updateInfo.getMd5();//得到服务器MD5   flag改成yanzhen

                        //get update infomation
                        String strUpdatecontent = null;
                        Locale local = Locale.getDefault();
                        String language = local.getLanguage();
                        String country = local.getCountry().toLowerCase();
                        List<UpdateDescriptioninfo> descriptions = updateInfo.getDescriptions();
                        if (descriptions == null || descriptions.size() <= 0) {
                            msg.what = NetOrSerexception;
                            Log.e(TAG, "NetWork error");
                            mHandler.sendMessage(msg);
                            return;
                        }
                        if ("zh".equals(language)) {
                            if ("cn".equals(country)) {
                                strUpdatecontent = updateInfo.getContentcn().replace(",", "\n");

                            } else if ("tw".equals(country)) {
                                strUpdatecontent = updateInfo.getContenttw().replace(",", "\n");

                                //spf_otacontenteditor.commit();
                            }
                        } else {
                            strUpdatecontent = updateInfo.getContentdefault().replace(",", "\n");
                        }
                        //存储服务器版本号

                        editor.putString("gspOtaAPKVer", ServerApkVer);
                        editor.putString("gspOtaApkUrl", serverApkUrl);
                        editor.putString("gspOtaAPKMD5", serverMD5);
                        editor.putString("updatecontent", strUpdatecontent);
                        editor.commit();
                        //得到本地版本号
                        // SharedPreferences sPreferences = getSharedPreferences("versionnumber", Context.MODE_PRIVATE);
                        //String   versionnumberget  = sPreferences.getString("versionnumber", "");
                        //System.out.println("versionnumberget ="+versionnumberget );

//                        if ((VoiceManager.gujianVersion != null && mcuInfo != null && VoiceManager.gujianVersion.compareTo(mcuInfo.getVersion()) < 0) ||
//                                (VoiceManager.cameraversion != null && cameraInfo != null && VoiceManager.cameraversion.compareTo(cameraInfo.getVersion()) < 0)) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
//                                    Intent update = new Intent(mContext, FormalUpdateActivity.class);
//                                    update.putExtra("hudversion", VoiceManager.gujianVersion);
//                                    update.putExtra("obdversion", VoiceManager.obdversion);
//                                    update.putExtra("cameraversion", VoiceManager.cameraversion);
//                                    update.putExtra("appversion", strLocalVer);
//                                    update.putExtra("is_beta", is_beta);
//                                    startActivity(update);
//                                }
//                            });
//                            return;
//                        }
                        if (ServerApkVer.toString().compareTo(strLocalVer.toString()) <= 0) {
                            // Log.e(otaTag,"bigrosmall = " + ServerApkVer.toString().compareTo(strLocalVer.toString()) );
                            msg.what = haved_newversion;//
                            mHandler.sendMessage(msg);
                            Log.e(TAG, "haved_newversion");
                        } else {
                            String localApkVersion = discoverVersion();
                            if (ServerApkVer != null && ServerApkVer.equals(localApkVersion)) {//判断下载成功的版本号
                                Uri findUri = findUri();
                                if (findUri != null) {
                                    msg.what = INSTALL;//
                                    msg.obj = findUri;
                                    mHandler.sendMessage(msg);
                                    return;
                                }
                            }
                            msg.what = Backdownload;//
                            mHandler.sendMessage(msg);
                            Log.e(TAG, "Backdownload");
                        }
                    } else {
                        msg.what = Serverexception;
                        mHandler.sendMessage(msg);
                        Log.e(TAG, "Serverexception");
                    }
                } catch (IOException e) {

                    Log.e(TAG, "checkota failture....");
                    Message msg = new Message();
                    msg.what = NetOrSerexception;
                    Log.e(TAG, "NetWork error");
                    mHandler.sendMessage(msg);
                    e.printStackTrace();
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }.start();
    }

    // // Handler消息接收机制
    public Handler mHandler = new Handler() {
        // //Handler接收到相应消息进行刷新ui等操作
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case haved_newversion://这里采用DialogFragment，没用新版本。
                    if (!mShowingNewestVersionDialog) {
                        showHavedNewVersion();
                    }
                    break;
                case Backdownload:
                    if (!mIsShowingDialog) {
                        initControls();
                    }
                    break;
                case Serverexception:
                    ToastUtil.showShortToast(getApplicationContext(), getString(R.string.ota_serviceexp)); //R.string.ota_serviceexp "服务器异常,请稍后再试"
                    setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
                    break;
                case NetOrSerexception:
                    ToastUtil.showShortToast(getApplication(), getString(R.string.ota_netorserexp)); //R.string.ota_serviceexp "服务器异常,请稍后再试"
                    setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
                    break;
                case INSTALL:
                    openfile((Uri) msg.obj);
                    setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
                    break;
                default:
                    break;
            }
//  		mSoftWareUpdate.setClickable(true);
//  		mSoftWareUpdate.setText(getString(R.string.version_detection));
//			mSoftWareUpdate.setBackgroundDrawable(getResources().getDrawable(R.drawable.selector));
//			setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
        }

    };

    // 判断是否有可用的网络连接
    private boolean isNetwordAvaliable(Context context) {
        boolean result = false;
        ConnectivityManager connectManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectManager == null) {
            result = false;
        } else {
            NetworkInfo[] netInfo = connectManager.getAllNetworkInfo();
            for (NetworkInfo info : netInfo) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    //还有问题
    protected void showHavedNewVersion() {
//	   HavedNewDialogFragment dialogFragment = new HavedNewDialogFragment();
//	   dialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
//	   dialogFragment.show(getFragmentManager(), "hello");
        mShowingNewestVersionDialog = true;
        AlertDialog.Builder bulider = new AlertDialog.Builder(GuanyuActivity.this);
        View view = LayoutInflater.from(GuanyuActivity.this).inflate(R.layout.havedchedialog, null);
        bulider.setView(view);
        final AlertDialog dialog = bulider.create();
        Button know = (Button) view.findViewById(R.id.known);
        know.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dialog.dismiss();
                mShowingNewestVersionDialog = false;
                setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(myUpdateInfo);
        Log.e(TAG, "onDestroy mIsShowingDialog=" + mIsShowingDialog);
        unbindService(conn);
    }

    BroadcastReceiver myUpdateInfo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            VLCApplication.mIsDownloadedAPK = false;
            mIsShowingDialog = false;
            setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
            mSoftWareUpdate.setProgressEnable(false);
            Log.e(TAG, "myUpdateInfo");
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.softwareupdate:
                if (is_beta == 1) {
                    Intent update = new Intent(mContext, FormalUpdateActivity.class);
                    update.putExtra("hudversion", VoiceManager.gujianVersion);
                    update.putExtra("obdversion", VoiceManager.obdversion);
                    update.putExtra("cameraversion", VoiceManager.cameraversion);
                    update.putExtra("appversion", strLocalVer);
                    update.putExtra("is_beta", is_beta);
                    startActivity(update);
                } else {
                    if (VLCApplication.mIsDownloadedAPK) {
                        ToastUtil.showShortToast(getApplicationContext(), getString(R.string.ota_downloading));
                        return;
                    }
//                    String md5 = SpUtils.getString(GuanyuActivity.this, CameraConstant.CAMERA_MD5, "");
//                    CameraUpdateUtil cameraUpdateUtil = new CameraUpdateUtil(GuanyuActivity.this);
//                    if (cameraUpdateUtil.checkFile(md5)) {
//                        setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
//                        Intent update = new Intent(mContext, FormalUpdateActivity.class);
//                        String cameraInfo = SpUtils.getString(GuanyuActivity.this, CameraConstant.CAMERA_VERSION, "");
//                        String cameraVersion = "";
//                        if (cameraInfo.length() >= 16) {
//                            cameraVersion = cameraInfo.substring(16);
//                        }
//                        update.putExtra("cameraversion", cameraVersion);
//                        update.putExtra("appversion", strLocalVer);
//                        update.putExtra("is_beta", is_beta);
//                        startActivity(update);
//                        return;
//                    }
                    setButtonProperty(getResources().getString(R.string.test_version), false, R.color.gray);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (WifiUtil.pingNet()) {//有网络
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        checkCamAndApp();//这里只是Wifi才获取吗
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        ToastUtil.showShortToast(getApplicationContext(), getString(R.string.nonetwork));//没用可用的网络
                                        setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);

                                    }
                                });

                            }

                        }
                    }).start();
                }
                break;
            default:
                break;
        }

    }

    /**
     * 获取摄像头厂家
     */
    private void getCamFactory() {
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
                if (!TextUtils.isEmpty(server_cam_version)) {
                    SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_FACTORY, data.factory_name);
                    SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_VERSION, server_cam_version);
                    SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_MD5, data.cam_md5);
                    SpUtils.putBoolean(GuanyuActivity.this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, data.if_support_report != 0);

                }
                factory.setText(data.factory_name);
                xuliehao.setText(data.machine_model);
            }
        });
    }

    /**
     * 检查cam和app是否有更新
     */
    private void checkCamAndApp() {
        final String cameraVersionCurrent = SpUtils.getString(this, CameraConstant.CAMERA_VERSION_CURRENT, "");
        if (!TextUtils.isEmpty(cameraVersionCurrent) && cameraVersionCurrent.length() >= 16) {
            RequestMethods.getCameraInfo(cameraVersionCurrent, new HttpUtil.Callback<CameraVersionBean>() {
                @Override
                public void onCallback(CameraVersionBean cameraVersionBean) {

                    if (cameraVersionBean == null) {
                        GuanyuCheckOTA();
                        return;
                    }
                    if (cameraVersionBean.ret == 0) {
                        CameraVersionBean.CameraVersionData data = cameraVersionBean.data;
                        if (data == null) {
                            return;
                        }
                        String server_cam_version = data.cam_version;
                        if (!TextUtils.isEmpty(server_cam_version)) {
                            SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_FACTORY, data.factory_name);
                            SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_VERSION, server_cam_version);
                            SpUtils.putString(GuanyuActivity.this, CameraConstant.CAMERA_MD5, data.cam_md5);
                            if (cameraVersionCurrent.length() >= 16 && server_cam_version.length() >= 15) {
                                String firmwareVersionCurrent = cameraVersionCurrent.substring(16);
                                if (firmwareVersionCurrent.length() >= 15 && server_cam_version.length() >= 15) {
                                    if (firmwareVersionCurrent.substring(0, 7).equals(server_cam_version.substring(0, 7))) {
                                        if (firmwareVersionCurrent.substring(8, 11).compareTo(server_cam_version.substring(8, 11)) < 0) {
                                            //如果当前版本小于服务器上面的版本就下载固件
//                                            CameraUpdateUtil cameraUpdateUtil = new CameraUpdateUtil(GuanyuActivity.this);
//                                            if (!cameraUpdateUtil.checkFile(data.cam_md5)) {
//                                            cameraUpdateUtil.downloadFile(data.cam_url, data.cam_md5);
                                            setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
                                            Intent update = new Intent(mContext, FormalUpdateActivity.class);
                                            update.putExtra("cameraversion", server_cam_version);
                                            update.putExtra("curCameraVersion", firmwareVersionCurrent);
                                            update.putExtra("camera_url", data.cam_url);
                                            update.putExtra("appversion", strLocalVer);
                                            update.putExtra("is_beta", is_beta);
                                            startActivity(update);
                                            return;
//                                            }
                                        }
                                    }
                                }

                            }
                        }
                        GuanyuCheckOTA();
                    } else if (cameraVersionBean.ret == -1) {
                        ToastUtil.showShortToast(GuanyuActivity.this, getString(R.string.network_timeout));
                        setButtonProperty(getString(R.string.version_detection), true, R.drawable.selector);
                    } else {
                        GuanyuCheckOTA();
                    }
                }
            });
        } else {
            GuanyuCheckOTA();
        }

    }

    private void setButtonProperty(String string, boolean b, int id) {
        if (!"".equals(string)) {
            mSoftWareUpdate.setText(string);
        }
        mSoftWareUpdate.setClickable(b);
        if (b) {
            mSoftWareUpdate.setBackgroundDrawable(getResources().getDrawable(id));
        } else {
            mSoftWareUpdate.setBackgroundColor(getResources().getColor(id));
        }
    }

    private Uri findUri() {
        Uri localFilePath = null;
        File file = new File(VLCApplication.OTA_PATH);
        File[] listFiles = file.listFiles();
        for (File currenFile : listFiles) {
            if (currenFile.isFile() && currenFile.getName().equals(DownloadService.LOCALAPPNAME)) {
                File file2 = new File(file, DownloadService.LOCALAPPNAME);
                localFilePath = Uri.fromFile(file2);
            }
        }
        return localFilePath;
    }

    private void openfile(Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(url, "application/vnd.android.package-archive");
        startActivity(intent);
    }

    private String discoverVersion() {
        String version = null;
        File file = new File(VLCApplication.OTA_PATH);
        File[] listFiles = file.listFiles();
        PackageManager pm = getPackageManager();
        for (File currenFile : listFiles) {
            if (currenFile.isFile() && currenFile.getName().equals(DownloadService.LOCALAPPNAME)) {
                File file2 = new File(file, DownloadService.LOCALAPPNAME);
                String absolutePath = file2.getAbsolutePath();
                PackageInfo info = pm.getPackageArchiveInfo(absolutePath, PackageManager.GET_ACTIVITIES);
                if (info != null) {
                    version = info.versionName;       //得到版本信息
                    Log.e(TAG, version);
                }
            }
        }
        return version;
    }


}
