package com.adai.gkdnavi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.fragment.PhotoCameraFragment;
import com.adai.gkdnavi.fragment.VideoCameraFragment;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MovieRecordValue;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//import com.adai.camera.CameraConstant.HeartbeatCheck;

public class RemoteActivity extends BaseActivity implements OnClickListener {

    protected static final int START = 0;
    protected static final int END = 1;
    protected static final int RECORDING = 2;
    private static final int BEGIN = 3;
    private static final int NETWORKERROR = 4;
    private static final int INITDATA = 5;
    private static final int ERROR_GET_RECORD_STATE = 6;
    private static final int ERROR_STOP_RECORD = 7;
    private static final int ERROR_GET_FILELIST = 8;
    protected static final String RECORD = "record";
    protected static final String MOVING = "moving";
    protected static final String TAG = "RemoteActivity";
    private static final int VOLLEYTIMEOUT = 5000;
    private FragmentManager mFragmentManager;
    private LinearLayout mLinearLayout;
    private ImageButton mCameraVideo;
    private ImageButton mCameraPhoto;
    private WifiManager mWifiManager;
    private RelativeLayout mRlDownloadTitle;
    private ConnectivityManager mConnectivityManager;
    private ArrayList<ScanResult> mScanResults = new ArrayList<ScanResult>();
    private boolean flag = true;
    private LinkWifi mLinkWifi;
    private boolean isRecording = false; // ±êÊ¶Â¼Ïñ×´Ì¬
    private LinearLayout mLinearLayoutFilesLoading;
    private boolean select_video = true;
    private boolean mHasMobileNetwork = false;//移动数据网络的状态
    private int iCurrentWifiNetID = -1;
    private VLCApplication app;
    private long starttime = 0;
    private int netmode;
    /**
     * 上次连接的wifiid
     */
    private int lastWifiNetId = -1;

    private VideoCameraFragment videoCameraFragment;
    private PhotoCameraFragment photoCameraFragment;
    private TextView loadingtext;
    private boolean isActivityRun = false;
    private boolean hasregisterNet = false;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START:
                    mLinearLayoutFilesLoading.setVisibility(View.GONE);
//				sendCmd("http://192.168.1.254/?custom=1&cmd=2006&par=0");
//				initData();
                    CameraUtils.changeMode(2, new CameraUtils.ModeChangeListener() {
                        @Override
                        public void success() {
                            getFilelist();
                        }

                        @Override
                        public void failure(Throwable throwable) {
                            showErrorDialog(ERROR_GET_FILELIST);
                        }
                    });
//                    if(!hasregisterNet) {
//                        registerNetworkReceiver();
//                        hasregisterNet=true;
//                    }
                    break;
                case END:
                    //
                    mLinearLayoutFilesLoading.setVisibility(View.GONE);
                    showAlertDialog();
                    break;
                case RECORDING:
                    //
                    mLinearLayoutFilesLoading.setVisibility(View.GONE);
                    showRecordingAlertDialog();
                    break;
                case BEGIN:
                    //
                    mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
                    break;
                case NETWORKERROR:
//				Toast.makeText(getApplicationContext(), "请求出错,请重试", Toast.LENGTH_SHORT).show();
//				((VLCApplication)getApplication()).resetRequestQueue();
                    if (isActivityRun)
                        showErrorDialog(msg.arg1);
                    break;
                case INITDATA:
                    if (filesize > 600) {
                        showFileSizeDialog();
                    }
                    if (!isFinishing())
                        initData();
                    break;
                default:
                    break;
            }
        }

        ;
    };

    private void showFileSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.please_clear_files);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        builder.create().show();
    }

    private int currentErrorcode = -1;

    private void showErrorDialog(int errorcode) {
        currentErrorcode = errorcode;
        String message = "";
        switch (errorcode) {
            case ERROR_GET_FILELIST:
                message = getString(R.string.failed_get_filelist);
                break;
            case ERROR_GET_RECORD_STATE:
                message = getString(R.string.failed_get_recording_status);
                break;
            case ERROR_STOP_RECORD:
                message = getString(R.string.failed_stop_recording);
                break;
        }

        new AlertDialog.Builder(RemoteActivity.this).setTitle(R.string.notice).setMessage(message).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (currentErrorcode) {
                    case ERROR_GET_RECORD_STATE:
                        getRecondStatus();
                        break;
                    case ERROR_STOP_RECORD:
                        changeRecordState(false);
                        break;
                    case ERROR_GET_FILELIST:
//                        getFilelist();
                        changeRecordState(false);
                        break;
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        }).create().show();
    }

    private boolean isComeCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.local_file);
        isComeCamera = getIntent().getBooleanExtra("CameraConstant", false);//判断是否从CameraActivity来的
        mRlDownloadTitle = (RelativeLayout) findViewById(R.id.Rl_download_title);
        mRlDownloadTitle.setVisibility(isComeCamera ? View.VISIBLE : View.GONE);
        mLinearLayout = (LinearLayout) findViewById(R.id.ll_button);
        mLinearLayout.setVisibility(View.GONE);
        mCameraVideo = (ImageButton) findViewById(R.id.ib_video);
        mCameraPhoto = (ImageButton) findViewById(R.id.ib_photo);
        mLinearLayoutFilesLoading = (LinearLayout) findViewById(R.id.files_loading);
        loadingtext = (TextView) findViewById(R.id.loadingtext);
        mCameraVideo.setOnClickListener(this);
        mCameraPhoto.setOnClickListener(this);

        if (VoiceManager.getMobileDataState(this, null)) {
            VoiceManager.setMobileData(this, false);
            mHasMobileNetwork = true;
        }
        initFragment();
        // ###################
        Message msg = Message.obtain();
        msg.what = BEGIN;
        mHandler.sendMessage(msg);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mLinkWifi = new LinkWifi(RemoteActivity.this);
        app = (VLCApplication) getApplication();
        netmode = getCurrentNetModel();
        if (netmode == 0) {
            if (!mWifiManager.isWifiEnabled()) {
                mWifiManager.setWifiEnabled(true);
            }

        } else {
            //  AP模式
        }
//        if (!mWifiManager.isWifiEnabled()) {
//            PermissionUtils.showPermissionDialog(this, permission.CHANGE_WIFI_STATE);
//        }
//        try {
//            VoiceManager.setMobileNetworkfromLollipop(RemoteActivity.this,false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        VoiceManager.setDefaultNetwork(RemoteActivity.this, true);
        checkWifi();
        isActivityRun = true;
//		connectWifi();
    }

    private void initFragment() {
        if (videoCameraFragment == null)
            videoCameraFragment = new VideoCameraFragment();
        if (photoCameraFragment == null)
            photoCameraFragment = new PhotoCameraFragment();
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.add(R.id.fl_container, photoCameraFragment);
        transaction.add(R.id.fl_container, videoCameraFragment);
        transaction.hide(photoCameraFragment);
        transaction.commit();
    }

    @Override
    protected void onRestart() {

        super.onRestart();
        VoiceManager.setDefaultNetwork(RemoteActivity.this, true);
        checkWifi();
        isActivityRun = true;
//		connectWifi();
    }

    private void initData() {
        mLinearLayoutFilesLoading.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        // mRadioGroup.check(R.id.bt_1);
        mCameraVideo.setImageDrawable(getResources().getDrawable(
                R.drawable.video_selected));
        mCameraPhoto.setImageDrawable(getResources().getDrawable(
                R.drawable.photo));
        photoCameraFragment.initDataWithActivity();
        videoCameraFragment.initdatawithactivity();
    }

    private List<FileDomain> filelist = null;
    private int filesize = 0;

    public List<FileDomain> getFiles() {
        return filelist;
    }

    public void setFiles(List<FileDomain> filelist) {
        this.filelist = filelist;
    }

    private void getFilelist() {
        loadingtext.setText(R.string.getting_filelist);
        mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Contacts.URL_FILE_LIST, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response) && response.startsWith("<?xml")) {
                    InputStream is = null;
                    try {
                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    DomParseUtils dom = new DomParseUtils();
                    try {
                        filelist = dom.parsePullXml(is);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (filelist != null && filelist.size() > 0) {
                        filesize = filelist.size();
                        Log.e("9527", "filesize = " + filesize);
                        sendHanleInitMsg(INITDATA);
//						changeRecordState(true);
                        return;
                    }
                }
                Message msg = mHandler.obtainMessage();
                msg.what = NETWORKERROR;
                msg.arg1 = ERROR_GET_FILELIST;
                mHandler.sendMessage(msg);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Message msg = mHandler.obtainMessage();
                msg.what = NETWORKERROR;
                msg.arg1 = ERROR_GET_FILELIST;
                mHandler.sendMessage(msg);
            }
        });
        request.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(request);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    private void getRecondStatus() { //

        mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
        loadingtext.setText(R.string.getting_recording_status);
        String url = Contacts.URL_MOVIE_RECORDING_TIME;
        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, response);
                        try {
                            // Log.e("info", "result = " + result);
                            DomParseUtils dom = new DomParseUtils();
                            InputStream is = new ByteArrayInputStream(response
                                    .getBytes("utf-8"));
                            MovieRecordValue movieRecordValue = dom
                                    .getParserXmls(is);
                            if ("0".equals(movieRecordValue.getValue())) {
                                //
                                Message msg = Message.obtain();
                                msg.what = START;
                                mHandler.sendMessage(msg);
                            } else {
                                isRecording = true;
                                //
                                Message msg = Message.obtain();
                                msg.what = RECORDING;
                                mHandler.sendMessage(msg);
                            }
                            Log.e(TAG, "isRecording =" + isRecording);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Log.e(TAG, "Error: " + error.getMessage());
                Message msg = mHandler.obtainMessage();
                msg.what = NETWORKERROR;
                msg.arg1 = ERROR_GET_RECORD_STATE;
                mHandler.sendMessage(msg);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    private void getRecondStatusFirst() { //

        String url = Contacts.URL_MOVIE_RECORDING_TIME;
        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, response);
                        try {
                            // Log.e("info", "result = " + result);
                            DomParseUtils dom = new DomParseUtils();
                            InputStream is = new ByteArrayInputStream(response
                                    .getBytes("utf-8"));
                            MovieRecordValue movieRecordValue = dom
                                    .getParserXmls(is);
                            if ("0".equals(movieRecordValue.getValue())) {
                                isRecording = false;
                                //
                                SpUtils.putString(RemoteActivity.this, RECORD,
                                        "0");
                                Message msg = Message.obtain();
                                msg.what = START;
                                mHandler.sendMessage(msg);
                            } else {
                                isRecording = true;
                                //
                                SpUtils.putString(RemoteActivity.this, RECORD,
                                        "1");
                                Message msg = Message.obtain();
                                msg.what = RECORDING;
                                mHandler.sendMessage(msg);
                            }
                            Log.e(TAG, "isRecording =" + isRecording);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Log.e(TAG, "2Error: " + error.getMessage());
                // mHandler.sendEmptyMessage(NETWORKERROR);
                Message msg = mHandler.obtainMessage();
                msg.what = NETWORKERROR;
                msg.arg1 = ERROR_GET_RECORD_STATE;
                mHandler.sendMessage(msg);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    private void changeRecordState(boolean record) {
        if (!record) {
            mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
            loadingtext.setText(R.string.msg_center_stop_recording);
        }
        String cmd = Contacts.URL_MOVIE_RECORD + (record ? "1" : "0");
        sendCmd(cmd);
    }

    private void showRecordingAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.wheter_stop_record));
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendCmd(Contacts.URL_MOVIE_RECORD + "0");
//						Message message = Message.obtain();
//						message.what = START;
//						mHandler.sendMessage(message);
                    }
                });
        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        RemoteActivity.this.finish();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }

    private AlertDialog connectDialog;
    Connectthread connectthread = new Connectthread();

    protected void showAlertDialog() {

        if (isFinishing()) return;
        if (connectDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.notice));
            if (netmode == 0) {
                builder.setMessage(getString(R.string.wifi_checkmessage));
            } else {
                builder.setMessage(getString(R.string.ap_checkmessage));
            }
            builder.setPositiveButton(getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            if (netmode == 0) {
                                WifiUtil.getInstance().gotoWifiSetting(RemoteActivity.this);
                            } else {
                                WifiUtil.getInstance().startAP(RemoteActivity.this);
                                mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
                                loadingtext.setText(R.string.connecting_camera);
                                connectthread.start();
                            }


                        }
                    }

            );
            builder.setNegativeButton(

                    getString(R.string.cancel),

                    new DialogInterface.OnClickListener()

                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            RemoteActivity.this.finish();
                        }
                    }

            );
            builder.setCancelable(false);
            connectDialog = builder.create();
        }
        if (!connectDialog.isShowing()) {
            connectDialog.show();
        }
    }

    class Connectthread extends Thread {

        @Override
        public void run() {
            if (app.getApisConnect()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkWifi();
                    }
                });

            } else {
                if (starttime == 0) {
                    starttime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - starttime > 1000 * 20) {
                    Log.e("9527", "连接超时，请检查摄像头是否打开");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadingtext.setText(R.string.connect_time_out_check_connect_mode);
                        }
                    });

                } else {
                    mHandler.postDelayed(connectthread, 500);
                }

            }
        }

    }


    private void sendHanleInitMsg(int iMsg) {
        Message msg = Message.obtain();
        msg.what = iMsg;
        mHandler.sendMessage(msg);
    }


    private void checkWifi() {

        if (mLinearLayoutFilesLoading.getVisibility() == View.GONE) {
            mLinearLayoutFilesLoading.setVisibility(View.VISIBLE);
        }
        loadingtext.setText(R.string.are_surveillance_cameras);
        new Thread(new Runnable() {
            //Socket socket = null;

            @Override
            public void run() {

                try {
                    if (netmode == 0) {
                        if (!WifiUtil.checkNetwork(RemoteActivity.this, 0)) {
                            sendHanleInitMsg(END);
                            return;
                        }
                        WifiInfo info = mWifiManager.getConnectionInfo();
                        if (info != null) {
                            int networkId = info.getNetworkId();
                            if (networkId == -1) {
                                sendHanleInitMsg(END);
                                return;
                            }
                            String bssid = info.getBSSID();
                            String ssid = info.getSSID();
                            SpUtils.putString(getApplicationContext(), "SSID", ssid);
                            SpUtils.putInt(getApplicationContext(), "NEYWORKID",
                                    networkId);
                            SpUtils.putString(getApplicationContext(), "BSSID",
                                    bssid);
                        } else {
                            sendHanleInitMsg(END);
                            return;
                        }

                    } else {

                        if (!app.getApisConnect()) {
                            sendHanleInitMsg(END);
                            return;
                        }
                    }


                    if (filelist == null || filelist.size() == 0) {
                        getRecondStatusFirst();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mLinearLayoutFilesLoading.setVisibility(View.GONE);
                            }
                        });
                    }

                } catch (Exception e) {
                    sendHanleInitMsg(END);
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        switch (v.getId()) {
            case R.id.ib_video:
                if (select_video == false) {
                    select_video = true;
                    if (videoCameraFragment == null)
                        videoCameraFragment = new VideoCameraFragment();
                    transaction.show(videoCameraFragment);
                    transaction.hide(photoCameraFragment);
                    mCameraVideo.setImageDrawable(getResources().getDrawable(R.drawable.video_selected));
                    mCameraPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo));
                }
                break;
            case R.id.ib_photo:
                if (select_video == true) {
                    select_video = false;
                    if (photoCameraFragment == null)
                        photoCameraFragment = new PhotoCameraFragment();
                    transaction.show(photoCameraFragment);
                    transaction.hide(videoCameraFragment);
                    mCameraVideo.setImageDrawable(getResources().getDrawable(R.drawable.video));
                    mCameraPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo_selected));
                }
                break;
            default:
                break;
        }
        transaction.commit();
    }

    private void sendCmd(final String url) {
        StringRequest req = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, response);
                        if (isActivityRun && url.endsWith("par=0")) {
                            Message message = Message.obtain();
                            message.what = START;
                            mHandler.sendMessage(message);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Log.e(TAG, "3Error: " + error.getMessage());
                Message msg = mHandler.obtainMessage();
                msg.what = NETWORKERROR;
                msg.arg1 = ERROR_STOP_RECORD;
                mHandler.sendMessage(msg);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        isActivityRun = false;
        if (isComeCamera) {
            CameraUtils.changeMode(CameraUtils.CURRENT_MODE, new CameraUtils.ModeChangeListener() {
                @Override
                public void success() {

                }

                @Override
                public void failure(Throwable throwable) {

                }
            });
        } else {
            CameraUtils.changeMode(CameraUtils.MODE_MOVIE, new CameraUtils.ModeChangeListener() {
                @Override
                public void success() {
                    CameraUtils.saveStartRecord();
                }

                @Override
                public void failure(Throwable throwable) {

                }
            });
            VoiceManager.setDefaultNetwork(RemoteActivity.this, false);
        }
    }


    /**
     * 获取与摄像头连接类型
     * 0为摄像头起AP，1为手机端起AP
     *
     * @return
     */
    public int getCurrentNetModel() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(this);
        return shareprefrence.getInt("netmode", 0);
    }
}