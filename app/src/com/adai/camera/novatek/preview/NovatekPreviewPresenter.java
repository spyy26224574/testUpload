package com.adai.camera.novatek.preview;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import com.adai.camera.CameraConstant;
import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.data.NovatekDataSource;
import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.CameraVersionResponse;
import com.example.ipcamera.domain.CaptureResponse;
import com.example.ipcamera.domain.MovieRecord;
import com.example.ipcamera.domain.StreamUrlBean;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by huangxy on 2017/8/4 17:12.
 */

public class NovatekPreviewPresenter extends NovatekPreviewContract.Presenter {
    private NovatekRepository mNovatekRepository;
    private VLCApplication app;
    private boolean isPortrait;
    private WifiManager mWifiManager;
    private Socket socket;
    private boolean isSocketConnect;
    protected static final int START = 0;
    protected static final int END = 1;
    private static final int SHOW_PROGRESS = 2;
    private static final int ON_LOADED = 3;
    private static final int HIDE_OVERLAY = 4;
    private static final int HANDLER_START = 5;
    private static final int HANDLER_END = 6;
    private static final int CONNECT_SOCKET = 7;
    private static final int NOTICE_WIFI_CONNECT = 8;
    private boolean hasSD;
    private boolean isDestroyed;
    private boolean socketFlag = true;
    ConnectThread mConnectThread = new ConnectThread();
    private Runnable mHeartbeatCheck = new HeartbeatCheck();
    private static final int VOLLEYTIMEOUT = 5000;
    private long mStartTime;
    private boolean socketflag;
    private boolean isHttp = false;
    private boolean isStop;
    private boolean isPhotoMode;
    private String mPipCurStateId = "0";
    private boolean suportDoubleCam = false;
    @SuppressLint("HandlerLeak")
    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START:
                    if (isPaused) {
                        return;
                    }
                    socketFlag = true;
                    mNovatekRepository.initDataSource(new NovatekDataSource.DataSourceSimpleCallBack() {
                        @Override
                        public void success() {
                            mNovatekRepository.getStatus(new NovatekDataSource.DataSourceSimpleCallBack() {
                                @Override
                                public void success() {
                                    CameraUtils.syncTime(new CameraUtils.CmdListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            addCommand();
                                            mPipMenu = mNovatekRepository.getMenuItem(NovatekWifiCommands.CAMERA_PIP_STYLE);
                                            mPipCurStateId = mNovatekRepository.getCurStateId(NovatekWifiCommands.CAMERA_PIP_STYLE);
                                            String curMovieFov = mNovatekRepository.getCurStateId(NovatekWifiCommands.MOVIE_MOVIE_FOV);
                                            if (TextUtils.isEmpty(curMovieFov)) {
                                                mView.isFishMode(true);
                                            }else{
                                                if ("1".equals(curMovieFov)) {
                                                    mView.isFishMode(false);
                                                } else {
                                                    mView.isFishMode(true);
                                                }
                                            }
                                            if (mPipMenu != null && !TextUtils.isEmpty(mPipCurStateId)) {
                                                //支持前后双录
                                                isHttp = true;
                                                suportDoubleCam = true;
                                                respChangePip(mPipCurStateId);
                                            } else {
                                                mView.showPip(-1);
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(Exception volleyError) {
                                            addCommand();
                                            SparseArray<String> menuItem = mNovatekRepository.getMenuItem(NovatekWifiCommands.CAMERA_PIP_STYLE);
                                            mPipCurStateId = mNovatekRepository.getCurStateId(NovatekWifiCommands.CAMERA_PIP_STYLE);
                                            if (menuItem != null && !TextUtils.isEmpty(mPipCurStateId)) {
                                                //支持前后双录
                                                isHttp = true;
                                                suportDoubleCam = true;
                                                respChangePip(mPipCurStateId);
                                            } else {
                                                mView.showPip(-1);
                                            }
                                        }
                                    });

                                }

                                @Override
                                public void error(String error) {
                                    addCommand();
                                }
                            });

                        }

                        @Override
                        public void error(String error) {
                            addCommand();
                        }
                    });
                    break;
                case END:
                    socketFlag = false;
                    if (!isDestroyed) {
                        if (app.getCurrentNetModel() == 0) {
                            ToastUtil.showShortToast(app, app.getString(R.string.please_connect_camera));
//                            noticeWifiConnect();
                            // TODO:  提示wifi连接断开
                            mView.exit();
                            mView.hideLoading();
                        } else {
                            mView.showAlertDialog();
                        }
                    }
                    break;
                case HANDLER_START:
                    LogUtils.e("开启录制");
                    mView.stopPreview();
                    recordStatusChange(true);
                    break;
                case HANDLER_END:
                    LogUtils.e("停止录制");
                    mView.stopPreview();
                    recordStatusChange(false);
                    break;
                case CONNECT_SOCKET:
                    connectSocket();
                    mView.showLoading(VLCApplication.getAppContext().getResources().getString(R.string.Are_connected_camera));
                    break;
                case NOTICE_WIFI_CONNECT:
                    ToastUtil.showShortToast(app, VLCApplication.getAppContext().getString(R.string.connect_time_out_check_connect_mode));
//                    noticeWifiConnect();
                    mView.exit();
                    // TODO:  提示wifi连接断开
                    break;
                default:
                    break;
            }
        }
    };
    private SparseArray<String> mPipMenu;
    private String mPip = "FRONT";
    private WifiReceiver mReceiver;

    //0-前，1-前后，2-后前，3-后，4-后前，5-前后;// 1T1F 前置摄像头  1T1B2S 前主后辅 1T1S2B后主前辅 2T2F后置摄像头 2T1B2S后主前辅 2T1S2B前主后辅
    private void respChangePip(String pipCurStateId) {
        mPipCurStateId = pipCurStateId;

        int drawableRes = R.drawable.dualcam_front;
        if (mPipMenu != null) {
            mPip = mPipMenu.get(Integer.parseInt(mPipCurStateId));
            switch (mPip) {
                case "Front":
                case "FRONT":
                case "1T1F":
                    drawableRes = R.drawable.dualcam_front;
//                    mView.fishMode(true);
                    break;
                case "BOTH":
                case "1T1B2S":
                    drawableRes = R.drawable.dualcam_both_f;
                    break;
                case "BOTH2":
                case "1T1S2B":
                    drawableRes = R.drawable.dualcam_both_r;
                    break;
                case "Behind":
                case "BEHIND":
                case "2T2F":
                    drawableRes = R.drawable.dualcam_behind;
//                    mView.fishMode(false);
                    break;
                case "2T1B2S":
                    drawableRes = R.drawable.dualcam_both_r;
                    break;
                case "2T1S2B":
                    drawableRes = R.drawable.dualcam_both_f;
                    break;
                default:
                    break;
            }
        }
        mView.respChangePip(drawableRes);
        mView.showPip(isPortrait ? 0 : 1);
    }

    @Override
    public String getCurrentPip() {
        return mPip;
    }

    private boolean isStartRecording;

    private void toggleRecordStatus(final boolean startRecord) {
        mView.stopPreview();
        LogUtils.e("isStartRecording = " + isStartRecording);
//        CameraUtils.isRecording = startRecord;
//        mView.showRecordState(startRecord);
        if (isStartRecording) {
            return;
        }
        LogUtils.e("send startRecording");
        isStartRecording = true;
        mView.showLoading(startRecord ? app.getString(R.string.Opening_record) : app.getString(R.string.msg_center_stop_recording));
        CameraUtils.toggleRecordStatus(startRecord, new CameraUtils.ToggleStatusListener() {
            @Override
            public void success() {
                mView.showRecordState(startRecord);
                delayStartPreView(500);
                isStartRecording = false;
//                mView.initPlayView(isHttp);
//                mView.hideLoading();
            }

            @Override
            public void error(String error) {
                delayStartPreView(500);
//                mView.initPlayView(isHttp);
//                mView.hideLoading();
            }
        });

    }

    private void recordStatusChange(boolean startRecord) {
        LogUtils.e("isStartRecording = " + isStartRecording);
        CameraUtils.isRecording = startRecord;
        mView.showRecordState(startRecord);
        if (isStartRecording) {
            return;
        }
        LogUtils.e("send startRecording");
        isStartRecording = true;
        mView.showLoading(startRecord ? app.getString(R.string.Opening_record) : app.getString(R.string.msg_center_stop_recording));
        delayStartPreView(500);
    }

    public NovatekPreviewPresenter() {
        mNovatekRepository = NovatekRepository.getInstance();
    }

    @Override
    public void init() {
        mWifiManager = (WifiManager) VLCApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        app = VLCApplication.getInstance();
        new Thread(new HeartBeatRunnalbe()).start();
        registerWifiReceiver();
//        WifiInfo info = mWifiManager.getConnectionInfo();
//        if (info != null && !TextUtils.isEmpty(info.getBSSID())) {
//            String ssid = info.getSSID();
//            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
//                ssid = ssid.substring(1, ssid.length() - 1);
//            }
//            SpUtils.putString(VLCApplication.getAppContext(), "BSSID", info.getBSSID());
//            SpUtils.putString(VLCApplication.getAppContext(), "SSID", ssid);
//        }
    }

    private void registerWifiReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mReceiver = new WifiReceiver();
        mView.getAttachedContext().registerReceiver(mReceiver, filter);
    }

    private void unRegisterReceiver() {
        mView.getAttachedContext().unregisterReceiver(mReceiver);
        mReceiver = null;
    }

    @Override
    public void initOrientation() {
        Configuration configuration = VLCApplication.getAppContext().getResources().getConfiguration();
        isPortrait = configuration.orientation != Configuration.ORIENTATION_LANDSCAPE;
        mView.changeOrientation(isPortrait);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int layoutDirection = newConfig.orientation;
        isPortrait = layoutDirection != Configuration.ORIENTATION_LANDSCAPE;
        mView.changeOrientation(isPortrait);
        if (suportDoubleCam) {
            mView.showPip(isPortrait ? 0 : 1);
        }
    }

    @Override
    public void connectSocket() {
        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
//                    if (WifiUtil.checkNetwork(mView.getAttachedContext(), 0)) {
                    LogUtils.e("9527 Contacts.BASE_IP = " + Contacts.BASE_IP);
//                    socket = new Socket(Contacts.BASE_IP, 3333);
//                    socket.setSoTimeout(5000);
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(Contacts.BASE_IP, 3333), 5000);
                    socket.setSoTimeout(5000);
                    isSocketConnect = true;
                    new Thread(socketCmd).start();
//                    } else {
//                        LogUtils.e("9527 run: checkNetwork false");
//                        sendMsg(END);
//                    }
                } catch (Exception e) {
                    sendMsg(END);
                }
            }
        }).start();
    }

    private void addCommand() {
        CameraUtils.getStreamUrl(new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                DomParseUtils domParseUtils = new DomParseUtils();
                StreamUrlBean streamUrlBean = domParseUtils.parseSimpleXml(response, StreamUrlBean.class);
                if (streamUrlBean != null && !TextUtils.isEmpty(streamUrlBean.MovieLiveViewLink)) {
                    if (streamUrlBean.MovieLiveViewLink.contains("http")) {
                        //支持前后双录
                        isHttp = true;
//                        mView.showPip(isPortrait ? 0 : 1);
                    }
//                    else {
//                        mView.showPip(-1);
//                    }
                }
                addCommandNext();
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                addCommandNext();
            }
        });

    }

    private void addCommandNext() {
        CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_GET_NEW_VERSION, "", new CameraUtils.CmdCallback() {//获取摄像头信息
            @Override
            public void success(int commandId, String par, MovieRecord movieRecord) {
                String cameraVersionCurrent = movieRecord.getString();
                if (!TextUtils.isEmpty(cameraVersionCurrent)) {
                    SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_VERSION_CURRENT, cameraVersionCurrent);
                }
                CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                    @Override
                    public void success(int commandId, String par, MovieRecord movieRecord) {
                        initMode();
                    }

                    @Override
                    public void failed(int commandId, String par, String error) {
                        initMode();
                    }
                });
            }

            @Override
            public void failed(int commandId, String par, String error) {
                CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                    @Override
                    public void success(int commandId, String par, MovieRecord movieRecord) {
                        initMode();
                    }

                    @Override
                    public void failed(int commandId, String par, String error) {
                        initMode();
                    }
                });
            }
        });
    }

    private void initMode() {
        if (isPhotoMode) {
            CameraUtils.isRecording = false;
            mView.showRecordState(false);
            CameraUtils.changeMode(CameraUtils.MODE_PHOTO, new CameraUtils.ModeChangeListener() {
                @Override
                public void success() {
//                    getCameraVersion();
                    CameraUtils.CURRENT_MODE = CameraUtils.MODE_PHOTO;
                    mView.startPreview(true);
                }

                @Override
                public void failure(Throwable throwable) {
                    LogUtils.e(throwable.getMessage());
                    mView.hideLoading();
                }
            });
        } else {
            StringRequest req = new StringRequest(Contacts.URL_MODE_CHANGE + "1",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            LogUtils.e(response);
                            try {
                                CameraUtils.CURRENT_MODE = CameraUtils.MODE_MOVIE;
                                mView.currentMode(CameraUtils.MODE_MOVIE);
                                InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                                final DomParseUtils domParseUtils = new DomParseUtils();
                                MovieRecord record = domParseUtils.getParserXml(is);
                                if (record != null && record.getStatus().equals("0")) {
                                    CameraUtils.sendCmd(Contacts.URL_WIFIAPP_CMD_MJPEG_RTSP, new CameraUtils.CmdListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                                                MovieRecord parserXml = domParseUtils.getParserXml(is);
                                                if (parserXml != null && parserXml.getStatus().equals("0")) {
                                                    isHttp = true;
                                                }
                                                getCameraVersion();
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                                mView.hideLoading();
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(Exception volleyError) {
                                            mView.hideLoading();
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                mView.hideLoading();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.e("Error: ", error.getMessage());
                    mView.hideLoading();
                }
            });
            req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            VLCApplication.getInstance().addToRequestQueue(req);
        }
    }

    /**
     * socket处理线程
     */
    private Runnable socketCmd = new Runnable() {

        @Override
        public void run() {

            try {
                Message msg = Message.obtain();
                msg.what = START;
                nHandler.sendMessageDelayed(msg, 500);
                LogUtils.e("socket 1.54 successs 002!!!!!!");
                nHandler.postDelayed(mHeartbeatCheck, 500);
                // 获得输入流
                while (isSocketConnect && !socket.isClosed()) {
                    DataInputStream input = new DataInputStream(socket.getInputStream());
                    byte[] buffer;
                    buffer = new byte[input.available()];
                    if (buffer.length != 0) {
                        // 读取缓冲区
                        input.read(buffer);
                        String msg1 = new String(buffer, "GBK");// 注意转码，不然中文会乱码。
                        LogUtils.e("msg1 = " + msg1);
                        InputStream is = null;
                        try {
                            is = new ByteArrayInputStream(msg1.getBytes("utf-8"));
                        } catch (UnsupportedEncodingException e1) {
                            e1.printStackTrace();
                        }
                        DomParseUtils dom = new DomParseUtils();
                        MovieRecord movieRecord = null;
                        try {
                            movieRecord = dom.getParserXml(is);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (movieRecord != null) {
                            LogUtils.e("movieRecord.status:" + movieRecord.getStatus() + "  ,movieRecord.getCmd:" + movieRecord.getCmd());
                        }
                        final MovieRecord finalMovieRecord = movieRecord;
                        UIUtils.post(new Runnable() {
                            @Override
                            public void run() {
                                if (finalMovieRecord != null && finalMovieRecord.getCmd().equals("3020")) {
                                    if ("1".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("开启了录制");
                                        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
                                            nHandler.sendEmptyMessage(HANDLER_START);
                                        }
                                    } else if ("2".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("关闭了录制");
                                        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
                                            nHandler.sendEmptyMessage(HANDLER_END);
                                        }
                                    } else if ("4".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("录音开启");
                                        mView.audioChange(true);
                                    } else if ("5".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("录音关闭");
                                        mView.audioChange(false);
                                    } else if ("6".equals(finalMovieRecord.getStatus()) || "7".equals(finalMovieRecord.getStatus())) {
                                        mView.exit();
                                    } else if ("10".equals(finalMovieRecord.getStatus())) {
                                        mView.showToast(R.string.is_take_photo);
                                    } else if ("11".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("拍照模式");
                                        mView.showLoading(R.string.switching_photo_mode);
                                        CameraUtils.isRecording = false;
                                        mView.showRecordState(false);
                                        isPhotoMode = true;
                                        mView.startPreview(true);
                                        mView.pictureVisible(true);
                                        CameraUtils.CURRENT_MODE = CameraUtils.MODE_PHOTO;
                                        mView.currentMode(CameraUtils.CURRENT_MODE);
                                        mView.startPreview(true);
                                    } else if ("12".equals(finalMovieRecord.getStatus())) {
                                        LogUtils.e("录制模式");
                                        mView.showLoading(R.string.switching_record_mode);
                                        CameraUtils.CURRENT_MODE = CameraUtils.MODE_MOVIE;
                                        mView.currentMode(CameraUtils.CURRENT_MODE);
                                        mView.hideLoading();
                                        mView.startPreview(isHttp);
                                    } else if ("15".equals(finalMovieRecord.getStatus())) {
                                        //循环录制了一个视频
                                        mView.startRecordTime(0);
                                    }
                                }
                            }
                        });
                    }
                }
            } catch (IOException e) {
                LogUtils.e(e.getMessage());
                e.printStackTrace();
            }

        }
    };
    private boolean isPaused = false;

    @Override
    public void onResume() {
        isPaused = false;
    }

    @Override
    public void onPause() {
        isPaused = true;
        nHandler.removeCallbacks(mHeartbeatCheck);
    }

    @Override
    public void onRestart() {
        mView.showLoading(app.getString(R.string.Are_connected_camera));
        isStop = false;
        socketflag = true;
        VoiceManager.setDefaultNetwork(app, true);
        connectSocket();

//        nHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        }, 1000);
    }

    @Override
    public void onStop() {
        isStop = true;
        socketflag = false;
        try {
            socket.close();
            isSocketConnect = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLoadComplete() {
        if (CameraUtils.isCardFull) {
            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.wifi_camera_storage));
        }
        String curState = NovatekRepository.getInstance().getCurStateId(NovatekWifiCommands.MOVIE_RECORD_AUDIO);
        if (curState != null) {
            mView.audioChange("1".equals(curState));
        }
        isStartRecording = false;
        retryCount = 0;
        mView.hideLoading();
    }

    @Override
    public void onError() {
        ToastUtil.showShortToast(app, app.getString(R.string.Check_connection));
        mView.exit();
    }

    private int retryCount = 0;

    @Override
    public void onPlayError() {
        LogUtils.e("onPlayError " + retryCount);
        isHttp = !isHttp;
        if (retryCount == 6) {
            mView.hideLoading();
            ToastUtil.showShortToast(app, app.getString(R.string.Abnormal_play));
            mView.exit();
        } else {
            retryCount++;
            delayStartPreView(300);
        }
    }

    @Override
    public void onEnd() {
        delayStartPreView(500);
    }

    @Override
    public void changeMode(final int mode) {
        if (CameraUtils.CURRENT_MODE != mode) {
            mView.stopPreview();
            String message = VLCApplication.getAppContext().getString(mode == CameraUtils.MODE_MOVIE ? R.string.switching_record_mode : R.string.switching_photo_mode);
            mView.showLoading(message);
            if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE) {
                CameraUtils.changeMode(mode, new CameraUtils.ModeChangeListener() {
                    @Override
                    public void success() {
                        mView.hideLoading();
                        CameraUtils.isRecording = false;
                        mView.showRecordState(false);
                        isPhotoMode = true;
                        CameraUtils.CURRENT_MODE = mode;
                        mView.startPreview(true);
                        mView.currentMode(CameraUtils.CURRENT_MODE);
                        mView.pictureVisible(true);
                    }

                    @Override
                    public void failure(Throwable throwable) {
                        mView.hideLoading();
                        mView.startPreview();
                        mView.currentMode(CameraUtils.CURRENT_MODE);
                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.switch_failed));
                    }
                });
            } else {
                CameraUtils.changeMode(mode, new CameraUtils.ModeChangeListener() {
                    @Override
                    public void success() {
                        isPhotoMode = false;
                        CameraUtils.CURRENT_MODE = mode;
                        mView.currentMode(CameraUtils.CURRENT_MODE);
                        if (CameraUtils.hasSDCard) {
                            CameraUtils.saveChangeRecordState(true, new CameraUtils.CmdListener() {
                                @Override
                                public void onResponse(String response) {
                                    mView.hideLoading();
                                    CameraUtils.isRecording = true;
                                    mView.showRecordState(true);
                                    mView.startPreview(isHttp);
                                    if (!"".equals(response)) {//没有改变录制状态，不需要init播放器
//                                        InputStream is = null;
//                                        try {
//                                            is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                                        } catch (UnsupportedEncodingException e) {
//                                            e.printStackTrace();
//                                        }
//                                        DomParseUtils domParseUtils = new DomParseUtils();
//                                        MovieRecord record = domParseUtils.getParserXml(is);
//                                        if (record != null && record.getStatus().equals("0")) {
//                                        } else {
//                                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.start_record_failed));
//                                        }
                                        InputStream is;
                                        try {
                                            is = new ByteArrayInputStream(response.getBytes("utf-8"));
                                            DomParseUtils domParseUtils = new DomParseUtils();
                                            MovieRecord record = domParseUtils.getParserXml(is);
                                            if (record != null) {
                                                int status = Integer.valueOf(record.getStatus());
                                                switch (status) {
                                                    case NovatekWifiCommands.RET_OK://开启录制成功
                                                        break;
                                                    case NovatekWifiCommands.ERROR_STORAGE_FULL:
                                                    case NovatekWifiCommands.ERROR_FOLDER_FULL:
                                                    case NovatekWifiCommands.ERROR_RECORD_FULL:
                                                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.wifi_camera_storage));
                                                        break;
                                                    default:
                                                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.start_record_failed));
                                                        break;
                                                }
                                            }
                                        } catch (UnsupportedEncodingException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                @Override
                                public void onErrorResponse(Exception volleyError) {
                                    mView.hideLoading();
                                    mView.startPreview(isHttp);
                                }
                            });
                        } else {
                            mView.hideLoading();
                            mView.startPreview(isHttp);
                        }
                    }

                    @Override
                    public void failure(Throwable throwable) {
                        mView.hideLoading();
                        mView.startPreview();
                        mView.currentMode(CameraUtils.CURRENT_MODE);
                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.switch_failed));
                    }
                });
            }
        }
    }

    @Override
    public void takePhoto() {
        if (CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ) {
            mView.startTakePhoto();
            mView.showLoading(R.string.please_wait);
            CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_RAW_ENCODE_SAVE_JPEG, "", new CameraUtils.CmdCallback() {
                @Override
                public void success(int commandId, String par, MovieRecord movieRecord) {
                    if ("0".equals(movieRecord.getStatus())) {
                        mView.showToast(R.string.takephoto_sucess);
                    } else {
                        mView.showToast(R.string.takephoto_failed);
                    }
                    mView.takePhotoEnd();
                    mView.hideLoading();
                }

                @Override
                public void failed(int commandId, String par, String error) {
                    mView.takePhotoEnd();
                    mView.hideLoading();
                    mView.showToast(R.string.takephoto_failed);
                }
            });
            return;
        }
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
            mView.startTakePhoto();
            mView.showLoading(VLCApplication.getAppContext().getString(R.string.please_wait));
            CameraUtils.sendCmd(Contacts.URL_TAKE_PHOTO, new CameraUtils.CmdListener() {
                @Override
                public void onResponse(String response) {
                    InputStream is = null;
                    try {
                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    } catch (UnsupportedEncodingException e1) {
                        e1.printStackTrace();
                    }

                    DomParseUtils dom = new DomParseUtils();
                    try {
                        final CaptureResponse caputreResponse = dom.getCaputreResponse(is);
                        if ("0".equals(caputreResponse.status)) {
//                            final String imageUrl = (Contacts.BASE_HTTP_IP + caputreResponse.path.substring(caputreResponse.path.indexOf(":") + 1)).replace("\\", "/");
//                            LogUtils.e("onSuccess: imageUrl=" + imageUrl);
                            mView.hideLoading();
                            mView.takePhotoEnd();
                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.takephoto_sucess));
                        } else if ("-11".equals(caputreResponse.status)) {
                            mView.hideLoading();
                            mView.takePhotoEnd();
                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.wifi_camera_storage));
                        } else if ("-12".equals(caputreResponse.status)) {
                            mView.hideLoading();
                            mView.takePhotoEnd();
                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.storage_full));
                        } else {
                            mView.hideLoading();
                            mView.takePhotoEnd();
                            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.takephoto_failed));
                        }
                    } catch (Exception e) {
                        mView.hideLoading();
                        mView.takePhotoEnd();
                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.takephoto_failed));
                    }
                }

                @Override
                public void onErrorResponse(Exception volleyError) {
                    mView.hideLoading();
                    mView.takePhotoEnd();
                    ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.takephoto_failed));
                }
            });
        }
    }

    @Override
    public void recordShot() {
        if (CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ) {
            takePhoto();
            return;
        }
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
            takePhoto();
        } else {
            if (CameraUtils.isRecording) {
                toggleRecordStatus(false);
            } else {
                if (CameraUtils.hasSDCard) {
                    toggleRecordStatus(true);
                } else {
                    ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.wifi_sdcard));
                }
            }
        }
    }

    @Override
    public void onStartPlay() {
        LogUtils.e("retryCount = " + retryCount);
    }

    @Override
    public void changePip() {
        mView.showLoading(VLCApplication.getAppContext().getString(R.string.crop__wait));
        mView.stopPreview();
        String par = "0";
        //0-前，1-前后，2-后前，3-后，4-后前，5-前后// 1T1F 前置摄像头  1T1B2S 前主后辅 1T1S2B后主前辅 2T2F后置摄像头 2T1B2S后主前辅 2T1S2B前主后辅
        switch (mPipCurStateId) {
            case "0"://前
                par = "3";
                break;
            case "3"://后
                par = "1";
                break;
            case "1":
            case "5"://前后
                par = "2";
                break;
            case "2":
            case "4"://后前
                par = "0";
                break;
            default:
                break;
        }
        if (mPipMenu != null && !TextUtils.isEmpty(mPipCurStateId)) {
            int index = mPipMenu.indexOfKey(Integer.parseInt(mPipCurStateId));
            if (index == -1 || index - 1 == mPipMenu.size()) {
                index = 0;
            } else {
                index++;
            }
            par = String.valueOf(mPipMenu.keyAt(index));
        }
        CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_PIP_STYLE, par, mCmdCallback);
    }

    private CameraUtils.CmdCallback mCmdCallback = new CameraUtils.CmdCallback() {
        @Override
        public void success(int commandId, String par, MovieRecord movieRecord) {
            mView.hideLoading();
            switch (commandId) {
                case NovatekWifiCommands.CAMERA_PIP_STYLE:
                    mView.startPreview(true);
                    respChangePip(par);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void failed(int commandId, String par, String error) {
            switch (commandId) {
                case NovatekWifiCommands.CAMERA_PIP_STYLE:
                    mView.startPreview();
                    break;
                default:
                    break;
            }
            mView.hideLoading();
            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.set_failure));
        }
    };

    public void delayStartPreView(int delayMillis) {
        nHandler.removeCallbacks(delayStartPreViewTask);
        nHandler.postDelayed(delayStartPreViewTask, delayMillis);
    }

    private Runnable delayStartPreViewTask = new Runnable() {
        @Override
        public void run() {
            if (isPaused) {
                return;
            }
            mView.startPreview(isHttp);
        }
    };

    @Override
    public void onBufferChanged(float buffer) {
        if (buffer >= 100) {
            mView.hideLoading();
        } else {
            mView.showLoading(app.getString(R.string.in_the_buffer));
        }
    }


    private class ConnectThread extends Thread {

        @Override
        public void run() {
            LogUtils.e("app.getApisConnect:" + app.getApisConnect());
            if (app.getApisConnect()) {
                sendMsg(CONNECT_SOCKET);
            } else {
                if (mStartTime == 0) {
                    mStartTime = System.currentTimeMillis();
                }
                if (System.currentTimeMillis() - mStartTime > 1000 * 20) {
                    mStartTime = 0;
                    LogUtils.e("9527 连接超时，请检查摄像头是否打开");
                    sendMsg(NOTICE_WIFI_CONNECT);
                } else {
                    nHandler.postDelayed(mConnectThread, 500);
                }
            }


        }

    }

    @Override
    public void startConnectThread() {
        mConnectThread.start();
    }

    private class HeartBeatRunnalbe implements Runnable {

        @Override
        public void run() {
            while (!isDestroyed) {
                CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_IS_ALIVE, null, null);
                SystemClock.sleep(8 * 1000);
            }
        }
    }

    public class WifiReceiver extends BroadcastReceiver {
        private static final String TAG = "wifiReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                Log.i(TAG, "wifi信号强度变化");
            }
            //wifi连接上与否
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {

                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    Log.i(TAG, "wifi断开");
                    mView.showToast(R.string.camera_disconnect);
                    mView.stopPreview();
                    mView.exit();
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    //获取当前wifi名称
                    Log.i(TAG, "连接到网络 " + wifiInfo.getSSID());
                }
            }
            //wifi打开与否
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                if (wifistate == WifiManager.WIFI_STATE_DISABLED) {
                    Log.i(TAG, "系统关闭wifi");
                } else if (wifistate == WifiManager.WIFI_STATE_ENABLED) {
                    Log.i(TAG, "系统开启wifi");
                }
            }
        }
    }

    private class HeartbeatCheck implements Runnable {
        @Override
        public void run() {
            if (socketflag) {
                try {
                    if (socket != null && socket.isConnected()) {
                        socket.sendUrgentData(0xFF);
                    } else {
                        if (isWifiConnected()) {
                            isSocketConnect = false;
                            connectSocket();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isSocketConnect = false;
                    connectSocket();
                }
                nHandler.postDelayed(this, 2000);
            }
        }

    }

    private void getCameraVersion() {
        StringRequest req = new StringRequest(Contacts.URL_GET_CAMERA_VERSION, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                DomParseUtils dom = new DomParseUtils();
                try {
                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    CameraVersionResponse cameraVersionResponse = dom.getCameraVersionResponse(is);
                    String current_version = cameraVersionResponse.string;
                    SpUtils.putString(VLCApplication.getAppContext(), CameraConstant.CAMERA_FIRMWARE_VERSION, current_version);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                startRecord();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                startRecord();
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    private void startRecord() {
        if (CameraUtils.hasSDCard) {
            CameraUtils.saveChangeRecordState(true, new CameraUtils.CmdListener() {
                @Override
                public void onResponse(String response) {
                    UIUtils.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mView.initPlayView(isHttp);
                        }
                    }, 500);
                    if ("".equals(response)) {
                        CameraUtils.isRecording = true;
                        mView.showRecordState(true);
                    } else {
                        InputStream is;
                        try {
                            is = new ByteArrayInputStream(response.getBytes("utf-8"));
                            DomParseUtils domParseUtils = new DomParseUtils();
                            MovieRecord record = domParseUtils.getParserXml(is);
                            if (record != null) {
                                int status = Integer.valueOf(record.getStatus());
                                switch (status) {
                                    case NovatekWifiCommands.ERROR_RECORD_FULL:
                                        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.wifi_camera_storage));
                                        break;
                                    default:
                                        CameraUtils.isRecording = true;
                                        mView.showRecordState(true);
                                        break;
                                }
                            }
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                            mView.initPlayView(isHttp);
                        }
                    }
                }

                @Override
                public void onErrorResponse(Exception volleyError) {
                    mView.initPlayView(isHttp);
                }
            });
        } else {
            mView.initPlayView(isHttp);
        }

    }

    /**
     * 判断wifi是否连接
     */
    private boolean isWifiConnected() {
        WifiInfo info = mWifiManager.getConnectionInfo();
        return info != null && info.getNetworkId() != -1;
    }

    private void sendMsg(int iMsg) {
        Message msg = Message.obtain();
        msg.what = iMsg;
        nHandler.sendMessage(msg);
    }

    @Override
    public void detachView() {
        super.detachView();
        Log.e("NovatekPreviewPresenter", "detachView");
        unRegisterReceiver();
        isDestroyed = true;
        nHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                VoiceManager.isCameraBusy = false;
            }
        }, 1000);
//        VoiceManager.isCameraBusy = true;
//        if (VoiceManager.isWifiPasswordChange) {
//            VoiceManager.isWifiPasswordChange = false;
////            WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//            VoiceManager.isCameraBusy = false;
//            return;
//        }
//        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
//            CameraUtils.changeMode(CameraUtils.MODE_MOVIE, new CameraUtils.ModeChangeListener() {
//                @Override
//                public void success() {
//                    if (CameraUtils.hasSDCard) {
//                        CameraUtils.startRecord(false, new CameraUtils.CmdListener() {
//                            @Override
//                            public void onResponse(String response) {
//                                CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                                    @Override
//                                    public void onResponse() {
////                                        WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                                        VoiceManager.isCameraBusy = false;
//                                    }
//                                });
//                            }
//
//                            @Override
//                            public void onErrorResponse(Exception volleyError) {
//                                CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                                    @Override
//                                    public void onResponse() {
////                                        WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                                        VoiceManager.isCameraBusy = false;
//
//                                    }
//                                });
//                            }
//                        });
//                    } else {
//                        CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                            @Override
//                            public void onResponse() {
////                                WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                                VoiceManager.isCameraBusy = false;
//                            }
//                        });
//
//                    }
//                }
//
//                @Override
//                public void failure(Throwable throwable) {
//                    CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                        @Override
//                        public void onResponse() {
//                            VoiceManager.isCameraBusy = false;
////                            WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                        }
//                    });
//                }
//            });
//        } else {
//            if (CameraUtils.hasSDCard && !CameraUtils.isRecording) {
//                CameraUtils.startRecord(false, new CameraUtils.CmdListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                            @Override
//                            public void onResponse() {
//                                VoiceManager.isCameraBusy = false;
////                                WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onErrorResponse(Exception volleyError) {
//                        CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                            @Override
//                            public void onResponse() {
////                                WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                                VoiceManager.isCameraBusy = false;
//                            }
//                        });
//                    }
//                });
//            } else {
//                CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_DISCONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
//                    @Override
//                    public void onResponse() {
////                        WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
//                        VoiceManager.isCameraBusy = false;
//                    }
//                });
//            }
//        }
    }

    @Override
    public String getState(int commandId) {
        return mNovatekRepository.getCurState(commandId);
    }

    @Override
    public String getStateId(int commandId) {
        return mNovatekRepository.getCurStateId(commandId);
    }

    @Override
    public void firstConnectSocket() {
        CameraUtils.sendCmdIgnoreResponse(NovatekWifiCommands.CAMERA_CONNECT, "", new CameraUtils.cmdListenerIgnoreResponse() {
            @Override
            public void onResponse() {
                connectSocket();
            }
        });
    }

    @Override
    public void setResolution(final int cmdId, final int key) {
        mView.stopPreview();
        if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
            mView.showLoading(R.string.please_wait);
            CameraUtils.sendCmd(cmdId, String.valueOf(key), new CameraUtils.CmdCallback() {
                @Override
                public void success(int commandId, String par, MovieRecord movieRecord) {
                    mView.hideLoading();
                    if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                        NovatekRepository.getInstance().setCurStateId(commandId, par);
                        mView.showToast(R.string.set_success);
                    } else {
                        mView.showToast(R.string.set_failure);
                    }
                    mView.initPlayView(isHttp);
                }

                @Override
                public void failed(int commandId, String par, String error) {
                    mView.hideLoading();
                    mView.startPreview();
                    mView.showToast(R.string.set_failure);
                }
            });
            return;
        }
        if (!CameraUtils.isRecording || !CameraUtils.hasSDCard) {
            mView.showLoading(R.string.please_wait);
            //停止流
            CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "0", new CameraUtils.CmdCallback() {
                @Override
                public void success(int commandId, String par, MovieRecord movieRecord) {
                    if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                        //设置分辨率
                        CameraUtils.sendCmd(cmdId, String.valueOf(key), new CameraUtils.CmdCallback() {
                            @Override
                            public void success(int commandId, String par, MovieRecord movieRecord) {
                                if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                                    NovatekRepository.getInstance().setCurStateId(commandId, par);
                                    //开启流
                                    CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                        @Override
                                        public void success(int commandId, String par, MovieRecord movieRecord) {
                                            mView.hideLoading();
                                            mView.initPlayView(isHttp);
                                            mView.showToast(R.string.set_success);
                                        }

                                        @Override
                                        public void failed(int commandId, String par, String error) {
                                            mView.hideLoading();
                                            mView.initPlayView(isHttp);
                                            mView.showToast(R.string.set_success);
                                        }
                                    });
                                } else {
                                    //开启流
                                    CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                        @Override
                                        public void success(int commandId, String par, MovieRecord movieRecord) {
                                            mView.hideLoading();
                                            mView.showToast(R.string.set_failure);
                                            mView.initPlayView(isHttp);
                                        }

                                        @Override
                                        public void failed(int commandId, String par, String error) {
                                            mView.hideLoading();
                                            mView.showToast(R.string.set_failure);
                                            mView.initPlayView(isHttp);
                                        }
                                    });
                                }

                            }

                            @Override
                            public void failed(int commandId, String par, String error) {
                                //开启流
                                CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                    @Override
                                    public void success(int commandId, String par, MovieRecord movieRecord) {
                                        mView.hideLoading();
                                        mView.showToast(R.string.set_failure);
                                        mView.initPlayView(isHttp);
                                    }

                                    @Override
                                    public void failed(int commandId, String par, String error) {
                                        mView.hideLoading();
                                        mView.showToast(R.string.set_failure);
                                        mView.initPlayView(isHttp);
                                    }
                                });
                            }
                        });
                    } else {
                        mView.hideLoading();
                        mView.showToast(R.string.set_failure);
                        mView.startPreview();
                    }

                }

                @Override
                public void failed(int commandId, String par, String error) {
                    mView.hideLoading();
                    mView.showToast(R.string.set_failure);
                    mView.startPreview();
                }
            });
        } else {
            //关闭录制
            mView.showLoading(R.string.msg_center_stop_recording);
            CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                @Override
                public void success() {
                    mView.showRecordState(false);
                    mView.showLoading(R.string.please_wait);
                    //停止流
                    CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "0", new CameraUtils.CmdCallback() {
                        @Override
                        public void success(int commandId, String par, MovieRecord movieRecord) {
                            if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                                //设置分辨率
                                CameraUtils.sendCmd(cmdId, String.valueOf(key), new CameraUtils.CmdCallback() {
                                    @Override
                                    public void success(int commandId, String par, MovieRecord movieRecord) {
                                        if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                                            mView.showToast(R.string.set_success);
                                            mView.showLoading(R.string.Opening_record);
                                            NovatekRepository.getInstance().setCurStateId(commandId, par);
                                            //开启流
                                            CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                                @Override
                                                public void success(int commandId, String par, MovieRecord movieRecord) {
                                                    CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                                        @Override
                                                        public void success() {
                                                            mView.showRecordState(true);
                                                            mView.hideLoading();
                                                            mView.initPlayView(isHttp);
                                                        }

                                                        @Override
                                                        public void error(String error) {
                                                            mView.hideLoading();
                                                            mView.initPlayView(isHttp);
                                                            mView.showToast(R.string.open_record_failed);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void failed(int commandId, String par, String error) {
                                                    CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                                        @Override
                                                        public void success() {
                                                            mView.showRecordState(true);
                                                            mView.hideLoading();
                                                            mView.initPlayView(isHttp);
                                                        }

                                                        @Override
                                                        public void error(String error) {
                                                            mView.hideLoading();
                                                            mView.initPlayView(isHttp);
                                                            mView.showToast(R.string.open_record_failed);
                                                        }
                                                    });
                                                }
                                            });
                                        } else {
                                            //设置失败
                                            mView.showToast(R.string.set_failure);
                                            mView.showLoading(R.string.Opening_record);
                                            //开启流
                                            CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                                @Override
                                                public void success(int commandId, String par, MovieRecord movieRecord) {
                                                    CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                                        @Override
                                                        public void success() {
                                                            //开启录制成功
                                                            mView.showRecordState(true);
                                                            mView.hideLoading();
                                                            mView.initPlayView(isHttp);
                                                        }

                                                        @Override
                                                        public void error(String error) {
                                                            mView.hideLoading();
                                                            mView.showToast(R.string.open_record_failed);
                                                            mView.initPlayView(isHttp);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void failed(int commandId, String par, String error) {
                                                    mView.hideLoading();
                                                    mView.showToast(R.string.set_failure);
                                                    mView.initPlayView(isHttp);
                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void failed(int commandId, String par, String error) {
                                        //设置失败
                                        mView.showToast(R.string.set_failure);
                                        mView.showLoading(R.string.Opening_record);
                                        //开启流
                                        CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_LIVE_VIEW, "1", new CameraUtils.CmdCallback() {
                                            @Override
                                            public void success(int commandId, String par, MovieRecord movieRecord) {
                                                CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                                    @Override
                                                    public void success() {
                                                        //开启录制成功
                                                        mView.showRecordState(true);
                                                        mView.hideLoading();
                                                        mView.initPlayView(isHttp);
                                                    }

                                                    @Override
                                                    public void error(String error) {
                                                        mView.hideLoading();
                                                        mView.showToast(R.string.open_record_failed);
                                                        mView.initPlayView(isHttp);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void failed(int commandId, String par, String error) {
                                                mView.hideLoading();
                                                mView.showToast(R.string.set_failure);
                                                mView.initPlayView(isHttp);
                                            }
                                        });
                                    }
                                });
                            } else {
                                //设置失败
                                mView.showToast(R.string.set_failure);
                                mView.showLoading(R.string.Opening_record);
                                CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                    @Override
                                    public void success() {
                                        mView.showRecordState(true);
                                        mView.hideLoading();
                                        mView.initPlayView(isHttp);
                                    }

                                    @Override
                                    public void error(String error) {
                                        mView.hideLoading();
                                        mView.showToast(R.string.open_record_failed);
                                        mView.initPlayView(isHttp);
                                    }
                                });
                            }


                        }

                        @Override
                        public void failed(int commandId, String par, String error) {
                            //设置失败
                            mView.showToast(R.string.set_failure);
                            mView.showLoading(R.string.Opening_record);
                            CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
                                @Override
                                public void success() {
                                    mView.showRecordState(true);
                                    mView.hideLoading();
                                    mView.initPlayView(isHttp);
                                }

                                @Override
                                public void error(String error) {
                                    mView.hideLoading();
                                    mView.showToast(R.string.open_record_failed);
                                    mView.initPlayView(isHttp);
                                }
                            });
                        }
                    });
                }

                @Override
                public void error(String error) {
                    mView.hideLoading();
                    mView.showToast(R.string.stop_recording_failed);
                    mView.startPreview();
                }
            });
        }
    }
}
