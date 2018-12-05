package com.adai.gkdnavi;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adai.camera.CameraConstant;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.params.CamaraFileuploadParam;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.CameraUpdateUtil;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.WifiUtil;

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

public class FormalUpdateActivity extends BaseActivity implements View.OnClickListener {
    private TextView btnupdate_camera, updateinfo_camera, notice_camera, versionname_camera;
    private TextView btnupdate_app, updateinfo_app, notice_app, versionname_app;
    private ProgressBar camera_progress, app_progress;

    private View lineseek_camera, lineseek_app;


    private UpdataInfo appinfo;
    private String cameraversion, appversion, cameraUrl;

    private View camera_layout, app_layout;

    private DownloadService mDownloadService;
    private ServiceConnection conn;
    private int is_beta = 0;

    private View main_view;
    private TextView notice_warning;
    private String cameraMd5 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formal_update);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        btnupdate_camera = (TextView) findViewById(R.id.cameraupdate);
        btnupdate_app = (TextView) findViewById(R.id.appupdate);

        updateinfo_camera = (TextView) findViewById(R.id.cameraupdateinfo);
        updateinfo_app = (TextView) findViewById(R.id.appupdateinfo);

        versionname_camera = (TextView) findViewById(R.id.cameraversionname);
        versionname_app = (TextView) findViewById(R.id.appversionname);

        notice_camera = (TextView) findViewById(R.id.cameranotice);
        notice_app = (TextView) findViewById(R.id.appnotice);

        camera_progress = (ProgressBar) findViewById(R.id.camera_progress);
        app_progress = (ProgressBar) findViewById(R.id.app_progress);

        lineseek_camera = findViewById(R.id.lineseek_camera);
        lineseek_app = findViewById(R.id.lineseek_app);

        camera_layout = findViewById(R.id.camera_layout);
        app_layout = findViewById(R.id.app_layout);

        main_view = findViewById(R.id.main_view);
        notice_warning = (TextView) findViewById(R.id.notice_warning);
    }

    @Override
    protected void init() {
        super.init();
        setTitle(getString(R.string.check_upgrade));
        bindService();
        initdata();
    }


    private void initdata() {
        Intent intent = getIntent();
        cameraUrl = intent.getStringExtra("camera_url");
        cameraMd5 = SpUtils.getString(this, CameraConstant.CAMERA_MD5, "");
        cameraversion = intent.getStringExtra("cameraversion");
        String curCameraVersion = intent.getStringExtra("curCameraVersion");
        appversion = intent.getStringExtra("appversion");
        is_beta = intent.getIntExtra("is_beta", 0);
        if (is_beta == 1) {
            notice_warning.setVisibility(View.VISIBLE);
        }
        versionname_camera.setText(curCameraVersion);
        versionname_app.setText(appversion);
        btnupdate_camera.setOnClickListener(this);
        btnupdate_app.setOnClickListener(this);
        updateinfo_camera.setText(getString(R.string.newcheckversion) + cameraversion);
        String camLocalpath = CameraUpdateUtil.getlocalpath();
        File camLocalfile = new File(camLocalpath);
        try {
            if (camLocalfile.exists() && CheckVersionTask.fileMD5(camLocalpath).toLowerCase().equals(cameraMd5.toLowerCase())) {
                btnupdate_camera.setText(getString(R.string.upgrade));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        btnupdate_camera.setVisibility(View.VISIBLE);
        camera_layout.setVisibility(View.VISIBLE);
        checkOTA();
    }


    public void checkOTA() {
        showpDialog(getString(R.string.getting_server_information));
        new Thread() {
            @Override
            public void run() {
                try {
                    String httpUrl = com.adai.gkd.contacts.Contacts.VERSION_UPDATE;
                    if (is_beta == 1) {
                        httpUrl = com.adai.gkd.contacts.Contacts.BETA_VERSION_UPDATE;
                    }
                    HttpClient httpClient = new DefaultHttpClient();
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
                    HttpGet get = new HttpGet(httpUrl);
                    //System.out.println(">>>>>>>>>" + get);
                    HttpResponse response;
                    InputStream input;
                    response = httpClient.execute(get);
                    Message msg = Message.obtain();
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        InputStream responseStream = entity.getContent();
                        input = responseStream;
                        List<UpdataInfo> infos = CheckVersionTask.getUpdataInfo(input);
                        SharedPreferences ServerVersionset = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = ServerVersionset.edit();
                        for (UpdataInfo info : infos) {
                            if ("app".equals(info.getType())) {
                                appinfo = info;
                                editor.putString("gspOtaAPKVer", appinfo.getVersion());
                                editor.putString("gspOtaApkUrl", appinfo.getUrl());
                                editor.putString("gspOtaAPKMD5", appinfo.getMd5());
                            } else if ("camera".equals(info.getType())) {
                                editor.putString("camera_md5", info.getMd5());
                                editor.putString("camera_version", info.getVersion());
                                editor.putString("camera_url", info.getUrl());
                                editor.putString("camera_defaultcontent", info.getContentdefault());
                            }
                        }
                        editor.commit();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateinfo_camera.setText(getString(R.string.newcheckversion) + cameraversion);
                                String camLocalpath = CameraUpdateUtil.getlocalpath();
                                File camLocalfile = new File(camLocalpath);
                                try {
                                    if (camLocalfile.exists() && CheckVersionTask.fileMD5(camLocalpath).toLowerCase().equals(cameraMd5.toLowerCase())) {
                                        btnupdate_camera.setText(getString(R.string.upgrade));
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                btnupdate_camera.setVisibility(View.VISIBLE);
                                camera_layout.setVisibility(View.VISIBLE);
                                if (appinfo != null) {
                                    if (appversion != null) {
                                        if (appversion.toLowerCase().compareTo(appinfo.getVersion().toLowerCase()) < 0) {
                                            updateinfo_app.setText(getString(R.string.newcheckversion) + appinfo.getVersion() + "\n" + getUpdateContent(appinfo));
                                            String localpath = DownloadService.getLocalappname();
                                            File localfile = new File(localpath);
                                            try {
                                                if (localfile.exists() && CheckVersionTask.fileMD5(localpath).toLowerCase().equals(appinfo.getMd5().toLowerCase())) {
                                                    btnupdate_app.setText(getString(R.string.install));
                                                }
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            btnupdate_app.setVisibility(View.VISIBLE);
                                        } else {
                                            updateinfo_app.setText(R.string.current_is_last_version);
                                            updateinfo_app.setTextColor(Color.RED);
                                        }
                                        app_layout.setVisibility(View.VISIBLE);
                                    } else {
                                        updateinfo_app.setText(getString(R.string.not_get_the_current_version_information));
                                    }
                                } else {
                                    updateinfo_app.setText(getString(R.string.no_access_the_server_information));
                                }
                                hidepDialog();
                                main_view.setVisibility(View.VISIBLE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hidepDialog();
                                showToast(getString(R.string.failed_to_get_server_information));
                            }
                        });
                    }

                } catch (Exception e) {
//                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hidepDialog();
                            showToast(getString(R.string.failed_to_get_server_information));
                        }
                    });
                }
            }
        }.start();
    }

    private String getUpdateContent(UpdataInfo info) {
        if (info == null) return null;
        Locale local = Locale.getDefault();
        String language = local.getLanguage();
        String country = local.getCountry().toLowerCase();
        String content = null;
        if ("zh".equals(language)) {
            if ("cn".equals(country)) {
                content = info.getContentcn();
            } else if ("tw".equals(country)) {
                content = info.getContenttw();
            }
        } else {
            content = info.getContentdefault();
        }
        return content;
    }

    @Override
    public void onClick(View view) {
//        try {
//            if (iBinder != null && iBinder.myservice != null && iBinder.myservice.getConnected()) {
        view.setClickable(false);
        switch (view.getId()) {
            case R.id.cameraupdate:
                checkCamera();
                lineseek_camera.setVisibility(View.VISIBLE);
                break;
            case R.id.appupdate:
                updateApp();
                break;
        }
    }


    private void checkCamera() {
        CameraUpdateUtil update = new CameraUpdateUtil(mContext);
        String camMD5 = SpUtils.getString(this, CameraConstant.CAMERA_MD5, "");
        if (update.checkFile(camMD5)) {
//            updateCamera();
            checkCameraWifi();
        } else {
            update.setDownloadComplete(downloadComplete);
            update.downloadFile(cameraUrl, camMD5);
            lineseek_camera.setVisibility(View.VISIBLE);
        }
    }


    private void checkCameraWifi() {
        showpDialog(getString(R.string.are_surveillance_cameras));
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (WifiUtil.checkNetwork(mContext, getCurrentNetModel())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            updateCameraNew();
                            hidepDialog();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showConnectCameraDialog();
                            hidepDialog();
                        }
                    });
                }
            }
        }).start();
    }

    private void showConnectCameraDialog() {
        new AlertDialog.Builder(mContext).setMessage(getString(R.string.check_camera_connect_fails)).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getCurrentNetModel() == 0) {
                    Intent wifi = new Intent("android.settings.WIFI_SETTINGS");
                    FormalUpdateActivity.this.startActivityForResult(wifi, REQUEST_CODE_CONNECTWIFI);
                } else {
                    WifiUtil.getInstance().startAP(mContext);
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnupdate_camera.setClickable(true);
                lineseek_camera.setVisibility(View.GONE);
            }
        }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CONNECTWIFI:
                checkCameraWifi();
                break;
        }
    }

    private void updateCameraNew() {
        btnupdate_camera.setClickable(false);
        String getlocalpath = CameraUpdateUtil.getlocalpath();

        String deleteFile = Contacts.BASE_HTTP_IP + "/" + getlocalpath.substring(getlocalpath.lastIndexOf("/") + 1) + "?del=1";
        CameraUpdateUtil.getlocalpath();
        HttpUtil.getInstance().requestGet(deleteFile, null, BasePageBean.class, null);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CamaraFileuploadParam params = new CamaraFileuploadParam();
        params.fileupload1 = new File(CameraUpdateUtil.getlocalpath());
        notice_camera.setText(getString(R.string.transmitting));
        HttpUtil.getInstance().requestPostWithFile(Contacts.BASE_HTTP_IP, params, BasePageBean.class, new HttpUtil.Callback<BasePageBean>() {

            @Override
            public void onCallback(BasePageBean result) {
                System.out.println(result.message);
                if (result.ret == 0) {
                    notice_camera.setText(getString(R.string.restart_for_update));
//                    showToast("数据传输完成,设备重启后生效");
                } else {
                    showToast(getString(R.string.transmission_failure));
                    notice_camera.setText(getString(R.string.transmission_failure));
                    btnupdate_camera.setClickable(true);
                }
                hidepDialog();
            }
        }, new HttpUtil.UploadCallback() {
            @Override
            public void onUploadComplete() {

            }

            @Override
            public void onUploading(int progress) {
                camera_progress.setProgress(progress);
                camera_progress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onUploadFail() {

            }
        });
    }

    private CameraUpdateUtil.DownloadComplete downloadComplete = new CameraUpdateUtil.DownloadComplete() {
        @Override
        public void onDownloadComplte(String path, boolean sucess) {
            if (sucess) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        updateCamera();
                        checkCameraWifi();
                        notice_camera.setText(getString(R.string.download_cuccess));
                        btnupdate_camera.setText(getString(R.string.upgrade));
                        btnupdate_camera.setClickable(true);
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast(getString(R.string.download_failure));
                        notice_camera.setText(getString(R.string.download_failure));
                        btnupdate_camera.setClickable(true);
                    }
                });
            }
        }

        @Override
        public void onDownloading(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    notice_camera.setText(getString(R.string.downloading));
//                    seek_camera.setProgress(progress);
                    camera_progress.setProgress(progress);
                }
            });
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
                mDownloadService.setShowNumber(downcallback);
            }
        };
        bindService(service, conn, BIND_AUTO_CREATE);
    }

    private static final int APP_PROGRESS = 0X11;
    private static final int REQUEST_CODE_CONNECTWIFI = 0X12;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case APP_PROGRESS:
                    notice_app.setText(getString(R.string.downloading));
                    app_progress.setProgress(msg.arg2);
                    app_progress.setVisibility(View.VISIBLE);
                    if (msg.arg2 >= 100) {
                        notice_app.setText(getString(R.string.download_cuccess));
                        btnupdate_app.setText(getString(R.string.install));
                        btnupdate_app.setClickable(true);
                    }
                    break;
            }
        }
    };

    DownloadService.OnShowNumber downcallback = new DownloadService.OnShowNumber() {

        @Override
        public void count(int load) {
            Message message = Message.obtain();
            message.arg2 = load;
            message.what = APP_PROGRESS;
            handler.sendMessage(message);
        }
    };

    private void updateApp() {
        if (btnupdate_app.getText().equals(getString(R.string.install))) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(new File(DownloadService.getLocalappname())), "application/vnd.android.package-archive");
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra("otapakurl", appinfo.getUrl());
        mContext.startService(intent);
        lineseek_app.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        unbindService(conn);
        super.onDestroy();
    }
}
