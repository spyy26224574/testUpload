package com.adai.gkdnavi;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.CameraConstant;
import com.adai.camera.CameraFactory;
import com.adai.camera.bean.CameraFactoryRtnMsg;
import com.adai.camera.bean.WifiBean;
import com.adai.camera.hisi.preview.HisiPreviewActivity;
import com.adai.camera.mstar.preview.MstarPreviewActivity;
import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.preview.NovatekPanoPreviewActivity;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.camera.sunplus.preview.SunplusPreviewActivity;
import com.adai.gkd.bean.LicenseBean;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.db.WifiDao;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.adapter.WifiRelayListAdapter;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.Summary;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MovieRecord;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class WifiConnectActivity extends BaseActivity implements OnClickListener {//, IConstant, IActions, OnWifiCallBack
    private static final String TAG = "WifiConnectActivity";
    protected static final int RECEIVERSSID = 0;
    protected static final int STARTLOADING = 1;
    protected static final int ENDLOADING = 2;
    protected static final int CONNECTED = 3;
    private static final int CONNECTFAILED = 4;
    protected static final int CONNECTED_NO_CAMERA = 5;
    private static final int CHECK_CAMERA = 6;
    private static final int CHECK_PERMISSION = 7;
    private static final int BACK_CONNECTED = 8;
    private WifiManager wifiManager = null;
    private Context context = null;
    private WifiRelayListAdapter wifiListAdapter;
    private ListView wifi_list;
    private ImageView refresh_list_btn;
    private TextView wifiName;
    private EditText wifiPassword;
    private Button add;

    private LinkWifi mLinkWifi;
    private ScanResult mScanResult;
    private ImageView mWifiPassWordStatus;
    private boolean isHidden = true;
    private ProgressDialog mProgressDialog;
    boolean connectToNetID;
    private boolean isConnecting = false;
    private LinearLayout ll_no_result_notice;

//    boolean isLicensedVia;

    boolean isFromSystem = false;
    boolean wantSaveInfo = true;

//    VLCApplication mApplication;

    private static final long timeOutNum = 30000;
    private WifiStateReceiver mWifiStateReceiver;
    private CameraFactory.CameraCtrlCallback mCameraCtrlCallback = new CameraFactory.CameraCtrlCallback() {
        @Override
        public void handlerCallback(CameraFactoryRtnMsg cameraFactoryRtnMsg) {
            Intent intent = new Intent();
            switch (cameraFactoryRtnMsg.ret) {
                case CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE:
                    getCameraType();
                    wifiHandler.sendEmptyMessage(CONNECTED);
                    saveWifiInfo(CameraFactory.ID_Novatek);
//                    intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                    startActivity(intent);
//                    WifiConnectActivity.this.finish();


//                    if (isLicensedVia) {//全景摄像头
//                    if (wifiManager.getConnectionInfo().getSSID().contains("CarDV_89E5_360")) {
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                    } else {//普通视频摄像头
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPreviewActivity.class);
//                    }
//
//                    startActivity(intent);
//                    WifiConnectActivity.this.finish();
                    break;
                case CameraFactoryRtnMsg.RET_HAS_MSTAR:
                    wifiHandler.sendEmptyMessage(CONNECTED);
                    saveWifiInfo(CameraFactory.ID_MSTAR);
                    intent.setClass(VLCApplication.getAppContext(), MstarPreviewActivity.class);
                    startActivity(intent);
                    WifiConnectActivity.this.finish();
                    break;
//                case CameraFactoryRtnMsg.RET_HAS_ALLWINNER_DEVICE:
//                    wifiHandler.sendEmptyMessage(CONNECTED);
//                    saveWifiInfo(CameraFactory.ID_Novatek);
//                    intent.setClass(VLCApplication.getAppContext(), AllWinnerPreviewActivity.class);
//                    startActivity(intent);
//                    WifiConnectActivity.this.finish();
//                    break;
//                case CameraFactoryRtnMsg.RET_HAS_GP_DEVICE:
//                    wifiHandler.sendEmptyMessage(CONNECTED);
//                    intent.setClass(VLCApplication.getAppContext(), GPPreviewActivity.class);
//                    startActivity(intent);
//                    saveWifiInfo(CameraFactory.ID_GP);
//                    WifiConnectActivity.this.finish();
//                    break;
//                case CameraFactoryRtnMsg.RET_HAS_SHENGMAI_DEVICE:
//                    wifiHandler.sendEmptyMessage(CONNECTED);
//                    intent.setClass(VLCApplication.getAppContext(), ShengmaiPreviewActivity.class);
//                    startActivity(intent);
//                    saveWifiInfo(CameraFactory.ID_Shengmai);
//                    WifiConnectActivity.this.finish();
//                    break;
                case CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE:
                    wifiHandler.sendEmptyMessage(CONNECTED);
                    saveWifiInfo(CameraFactory.ID_SUNPLUSE);
                    intent.setClass(VLCApplication.getAppContext(), SunplusPreviewActivity.class);
                    startActivity(intent);
                    WifiConnectActivity.this.finish();
                    break;
                case CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE:
                    wifiHandler.sendEmptyMessage(CONNECTED);
                    saveWifiInfo(CameraFactory.ID_HISI);
                    intent.setClass(VLCApplication.getAppContext(), HisiPreviewActivity.class);
                    startActivity(intent);
                    WifiConnectActivity.this.finish();
                    break;
                case CameraFactoryRtnMsg.RET_NO_DEVICE:
                    wifiHandler.sendEmptyMessage(CONNECTED_NO_CAMERA);
                    break;
//                case CameraFactoryRtnMsg.RET_HAS_JIELI_DEVICE:
//                    saveWifiInfo(CameraFactory.ID_JIELI);
//                    initjieli();
//                    break;
                default:
                    break;
            }
        }
    };

    private void saveWifiInfo(int product) {
        if (!wantSaveInfo)
            return;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SpUtils.putString(WifiConnectActivity.this, "SSID", wifiName.getText().toString());
//        if (isLicensedVia) {
//            SpUtils.putBoolean(WifiConnectActivity.this, "isPanoCamera", true);
//        } else {
//            SpUtils.putBoolean(WifiConnectActivity.this, "isPanoCamera", false);
//        }
        SpUtils.putString(WifiConnectActivity.this, "BSSID", wifiInfo.getBSSID());
        WifiUtil.setBssid(wifiInfo.getBSSID());
        SpUtils.putString(WifiConnectActivity.this, "pwd", wifiPassword.getText().toString().trim());
        SpUtils.putString(WifiConnectActivity.this, "wifi_encryption_type", mScanResult.capabilities);
        WifiDao wifiDao = new WifiDao();
        if (!TextUtils.isEmpty(wifiInfo.getBSSID())) {
            WifiBean wifiBean = new WifiBean();
            wifiBean.BSSID = wifiInfo.getBSSID();
            String ssid = wifiInfo.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            wifiBean.SSID = ssid;
            wifiBean.netId = wifiInfo.getNetworkId();
            wifiBean.encrypt = mScanResult.capabilities;
            wifiBean.pwd = wifiPassword.getText().toString().trim();
            wifiBean.product = product;
            wifiDao.insertWifi(wifiBean);
        }
    }

    public WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        if (existingConfigs == null || existingConfigs.size() <= 0) {
            return null;
        }
        for (WifiConfiguration existingConfig : existingConfigs) {

            if (existingConfig.SSID.equals("\"" + SSID + "\"") || existingConfig.SSID.equals(SSID)) {
                wifiManager.removeNetwork(existingConfig.networkId);
                wifiManager.saveConfiguration();
                return existingConfig;
            }
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wificonnect);
        init();
        initView();

//        mApplication = VLCApplication.getApplication();

        context = this;
        mLinkWifi = new LinkWifi(context);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Service.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        regWifiReceiver();

        wifiManager.startScan();
        wifiHandler.postDelayed(scanTimeOut, timeOutNum);

        mProgressDialog.setMessage(getResources().getStringArray(R.array.wifi_status)[1]);
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
        CameraFactory.getInstance().setCameraCtrlCallback(mCameraCtrlCallback);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.list_wifi_name);

        ll_no_result_notice = (LinearLayout) findViewById(R.id.ll_no_result_notice);
        wifi_list = (ListView) findViewById(R.id.listView);
        refresh_list_btn = (ImageView) findViewById(R.id.right_img);
        refresh_list_btn.setOnClickListener(this);
        wifiPassword = (EditText) findViewById(R.id.wifiPassword);
        wifiPassword.setText("12345678");
        wifiName = (TextView) findViewById(R.id.wifiName);
        add = (Button) findViewById(R.id.add);
        add.setOnClickListener(this);
        mWifiPassWordStatus = (ImageView) findViewById(R.id.iv_wifipassword_status);
        mWifiPassWordStatus.setOnClickListener(this);
        findViewById(R.id.iv_wifi).setOnClickListener(this);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(true);// 设置是否可以通过点击Back键取消
    }

    private void regWifiReceiver() {
        IntentFilter labelIntentFilter = new IntentFilter();
        labelIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        labelIntentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        labelIntentFilter.setPriority(1000);
        context.registerReceiver(wifiResultChange, labelIntentFilter);

    }

    private boolean isFirstLoadWifiList = true;
    private final BroadcastReceiver wifiResultChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                wifiHandler.removeCallbacks(scanTimeOut);
                showWifiList();
                Log.e(TAG, "onReceive: 搜索附近wifi完成");
                if (isFirstLoadWifiList) {
                    wifiHandler.sendEmptyMessage(ENDLOADING);
                    isFirstLoadWifiList = false;
                }
            }
//			else if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
//				showWifiList();
//			}
        }
    };
    //    private WifiInfo mConnectionInfo;
    private List<ScanResult> mWifiList;

    private void showWifiList() {
        mWifiList = wifiManager.getScanResults();
//        Log.e(TAG, "showWifiList: "+mWifiList.size());
        if (mWifiList == null || mWifiList.size() == 0) {
            ll_no_result_notice.setVisibility(View.VISIBLE);
            hideSoftInput();
        } else {
            ll_no_result_notice.setVisibility(View.GONE);
        }
//        mConnectionInfo = wifiManager.getConnectionInfo();
//        String bssid = mConnectionInfo.getBSSID();
        //String bssid = mConnectionInfo.getBSSID().replace("\"", "");
//        Iterator<ScanResult> iterator = mWifiList.iterator();
//        while (iterator.hasNext()) {
//            ScanResult scanResult = (ScanResult) iterator.next();
//            if (scanResult.BSSID.equals(bssid)) {
//                iterator.remove();
//            }
//        }
        Iterator<ScanResult> iterator = mWifiList.iterator();
        while (iterator.hasNext()) {
            ScanResult scanResult = iterator.next();
            if (TextUtils.isEmpty(scanResult.SSID)) {
                iterator.remove();
            }
        }
        Collections.sort(mWifiList, new Comparator<ScanResult>() {

            @Override
            public int compare(ScanResult oldval, ScanResult newval) {
                int data1 = oldval.level;
                int data2 = newval.level;
                if (data1 < data2) {
                    return 1;
                } else if (data1 == data2) {
                    return 0;
                }
                return -1;
            }

        });
        wifiListAdapter = new WifiRelayListAdapter(context, mWifiList);
        wifi_list.setAdapter(wifiListAdapter);

        wifi_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                ScanResult scanResult = mWifiList.get(position);
                Message msg = Message.obtain();
                msg.what = RECEIVERSSID;
                msg.obj = scanResult;
                wifiHandler.sendMessage(msg);
                wifiListAdapter.changeSelected(position);

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_img:
                Toast.makeText(context, getString(R.string.refresh), Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
                break;
            case R.id.iv_wifi:
                SpUtils.putString(WifiConnectActivity.this, "SSID", "");
                WifiUtil.getInstance().gotoWifiSetting(WifiConnectActivity.this);
                isFromSystem = true;
                regWifiReceiver();
                break;
            case R.id.add:
                wantSaveInfo = true;
                isFromSystem = false;
                SpUtils.putString(WifiConnectActivity.this, "SSID", "");
//                isLicensedVia = false;
                String cur_ssid = wifiName.getText().toString();
                if (cur_ssid.startsWith("LSX_G9_") || cur_ssid.startsWith("360-L9_")) {//全景授权
                    Log.e("9527", "start license ");
                    wifiHandler.sendEmptyMessage(CHECK_PERMISSION);
                    wifiHandler.removeCallbacks(timeOut);

                    Log.e("9999", "mScanResult.BSSID=" + mScanResult.BSSID);
                    Set<String> set = SpUtils.getHashSet(context, "license");
                    if (set != null && set.contains(mScanResult.BSSID)) {
                        connectWifi();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getLicense(mScanResult.BSSID);
                            }
                        });
                    }
                } else {
                    Log.e("9527", "NOT 360camera");
                    showToast(R.string.not_support_device);
                }
                break;
            case R.id.iv_wifipassword_status:

                if (isHidden) {
                    // 设置EditText文本为可见的
                    wifiPassword
                            .setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());
                    //  改变UI的显示
                    mWifiPassWordStatus.setBackgroundDrawable(getResources()
                            .getDrawable(R.drawable.show_password));
                } else {
                    // 设置EditText文本为隐藏的
                    wifiPassword
                            .setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());
                    // 改变UI的显示
                    mWifiPassWordStatus.setBackgroundDrawable(getResources()
                            .getDrawable(R.drawable.password));
                }
                isHidden = !isHidden;
                wifiPassword.postInvalidate();
                // 切换后将EditText光标置于末尾
                CharSequence charSequence = wifiPassword.getText();
                if (charSequence != null) {
                    Spannable spanText = (Spannable) charSequence;
                    Selection.setSelection(spanText, charSequence.length());
                }
                break;
        }
    }

    private void getLicense(String mac) {
        String package_name = getApplication().getPackageName();
        RequestMethods.getLicense(mac, package_name, new HttpUtil.Callback<LicenseBean>() {
            @Override
            public void onCallback(LicenseBean result) {
                if (result != null && result.data != null && result.data.statusCode.equals("101")) {
                    SpUtils.installHashSet(context, "license", mScanResult.BSSID);
                    connectWifi();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShortToast(WifiConnectActivity.this, getString(R.string.cameras_permission_fail));
                            wifiHandler.sendEmptyMessage(ENDLOADING);
                        }
                    });

                }
            }
        });
    }

    private Handler wifiHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.e(TAG, "handleMessage: msg.what = " + msg.what);
            switch (msg.what) {
                case RECEIVERSSID:
                    mScanResult = (ScanResult) msg.obj;
                    wifiName.setText(mScanResult.SSID);
                    break;
                case STARTLOADING:
                    mProgressDialog.setMessage(getString(R.string.wifi_connecting));
                    mProgressDialog.show();
                    break;
                case ENDLOADING:
                    wifiHandler.removeCallbacks(timeOut);
                    mProgressDialog.dismiss();
                    break;
                case CONNECTED:
                    wifiHandler.removeCallbacks(timeOut);
                    mProgressDialog.dismiss();
                    break;
                case CONNECTFAILED:
//                    ToastUtil.showShortToast(WifiConnectActivity.this, getString(R.string.Connection_failed));
                    showConnectFailedDialog();
                    wifiHandler.removeCallbacks(timeOut);
                    mProgressDialog.dismiss();
                    break;
                case CONNECTED_NO_CAMERA://连接成功但是不是摄像头
                    ToastUtil.showShortToast(WifiConnectActivity.this, getString(R.string.wifi_connect_no_camera));
                    wifiHandler.removeCallbacks(timeOut);
                    mProgressDialog.dismiss();
                    break;
                case CHECK_CAMERA:
                    mProgressDialog.setMessage(getString(R.string.are_surveillance_cameras));
                    break;
                case CHECK_PERMISSION:
                    mProgressDialog.setMessage(getString(R.string.cameras_permission));
                    mProgressDialog.show();
                    break;
                case BACK_CONNECTED:
                    wantSaveInfo = false;
                    isFromSystem = true;
                    mProgressDialog.setMessage(getString(R.string.connecting_camera));
                    mProgressDialog.show();
                    CameraFactory.getInstance().sendSearchDevice();
                    break;
                default:
                    break;
            }
        }
    };

    private void showConnectFailedDialog() {
        if (isFinishing()) {
            return;
        }
        showAlertDialog(getString(R.string.dialog_add_network_failed), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpUtils.putString(WifiConnectActivity.this, "SSID", "");
                WifiUtil.getInstance().gotoWifiSetting(WifiConnectActivity.this);
                regWifiReceiver();
//                finish();
                isFromSystem = true;
            }
        }, null);
    }

    private void showConnectFailedDeleteDialog() {
        if (isFinishing()) {
            return;
        }
        showAlertDialog(getString(R.string.delete_system_wifi), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SpUtils.putString(WifiConnectActivity.this, "SSID", "");
                WifiUtil.getInstance().gotoWifiSetting(WifiConnectActivity.this);
                regWifiReceiver();
                isFromSystem = true;
            }
        }, null);
    }


    private void connectWifi() {
//        int networkId = mConnectionInfo.getNetworkId();
//        wifiManager.disableNetwork(networkId);
//        wifiManager.disconnect();
        String wifipwd = wifiPassword.getText().toString().trim();

        if (wifiName.getText().equals("WIFI")) {
            Toast.makeText(context, getString(R.string.wifi_choose),
                    Toast.LENGTH_SHORT).show();
            return;

        } else {
            if (wifipwd.length() < 3) {// && !wifiName.getText().toString().contains(WIFI_PREFIX)
                Toast.makeText(context,
                        getString(R.string.wifi_least_password),
                        Toast.LENGTH_SHORT).show();
            } else {
//                if (mLinkWifi.IsExsits(mScanResult.SSID) != null) {
//                    int netIDabc = mLinkWifi.IsExsits(mScanResult.SSID).networkId;
//                    wifiManager.removeNetwork(netIDabc);
//                }
                Message msg = Message.obtain();
                msg.what = STARTLOADING;
                wifiHandler.sendMessage(msg);
                wifiHandler.removeCallbacks(timeOut);
                LogUtils.e("设置了超时");
                wifiHandler.postDelayed(timeOut, timeOutNum);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);


//                device_cap = getScanResultSecurity(mScanResult);

                final WifiConfiguration conf = new WifiConfiguration();
                conf.SSID = "\"" + mScanResult.SSID + "\"";
                switch (getScanResultSecurity(mScanResult)) {
                    case "WEP":
                        Log.e("WEP", "WEP");
                        conf.wepKeys[0] = "\"" + wifipwd + "\"";
                        conf.wepTxKeyIndex = 0;
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                        break;
                    case "PSK":
                        Log.e("PSK", "PSK");
                        conf.preSharedKey = "\"" + wifipwd + "\"";
                        break;
                    case "Open":
                        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                        break;
                }

//                IntentFilter intentFilter = new IntentFilter();
//                intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//                registerReceiver(mConnectionReceiver, intentFilter);

                int netId = wifiManager.addNetwork(conf);
                if (netId == -1) {
//                    Toast.makeText(getBaseContext(), getString(R.string.Add_network_failure), Toast.LENGTH_SHORT).show();
                    Message ms = Message.obtain();
                    ms.what = ENDLOADING;
                    Log.e(TAG, "connectWifi: 添加网络失败");
                    wifiHandler.sendMessage(ms);
                    showConnectFailedDeleteDialog();
//                    showConnectFailedDialog();
                    return;
                }
                isConnecting = false;
                wifiManager.enableNetwork(netId, true);


//                int netID = -1;
//                netID = mLinkWifi.CreateWifiInfo2(mScanResult, wifipwd);
//                Log.e(TAG, "connectWifi: netid = " + netID);
//                if (netID == -1) {
//                    Toast.makeText(getBaseContext(), getString(R.string.Add_network_failure), Toast.LENGTH_SHORT).show();
//                    Message ms = Message.obtain();
//                    ms.what = ENDLOADING;
//                    Log.e(TAG, "connectWifi: 添加网络失败");
//                    wifiHandler.sendMessage(ms);
//                    showConnectFailedDialog();
//                    return;
//                }
//
////				wifiPassword.setText("");
//                isConnecting = false;
//                connectToNetID = mLinkWifi.ConnectToNetID(netID);
//                Log.e(TAG, "3333333333333333");
                wifiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setWifiBroadcast();
                    }
                }, 500);
//                Log.e(TAG, "44444444444444");
//                if (!connectToNetID) {
//                    Toast.makeText(context,
//                            getString(R.string.Password_mistake) + mScanResult.SSID,
//                            Toast.LENGTH_SHORT).show();
//                    Message ms = Message.obtain();
//                    ms.what = ENDLOADING;
//                    Log.e(TAG, "connectWifi: 连接失败");
//                    wifiHandler.sendMessage(ms);
//                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(wifiResultChange);
        unRegiseterBroadcast();
    }

    private void unRegiseterBroadcast() {
        if (mWifiStateReceiver != null) {
            unregisterReceiver(mWifiStateReceiver);
            mWifiStateReceiver = null;
        }
    }

    private void setWifiBroadcast() {
        if (mWifiStateReceiver == null) {
            mWifiStateReceiver = new WifiStateReceiver();
            IntentFilter mWifiStateFilter = new IntentFilter();
//            mWifiStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(mWifiStateReceiver, mWifiStateFilter);
        }
    }

    private class WifiStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                LogUtils.e(connectionInfo.toString());
                String device = wifiName.getText().toString();
                mProgressDialog.setMessage(Summary.get(WifiConnectActivity.this, WifiInfo.getDetailedStateOf(connectionInfo.getSupplicantState())));
                if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED && (!TextUtils.isEmpty(device) && connectionInfo.getSSID().replace("\"", "").equals(device) || isFromSystem)) {
                    unRegiseterBroadcast();
                    if ((!TextUtils.isEmpty(device) && connectionInfo.getSSID().replace("\"", "").equals(device)) || isFromSystem) {
                        wifiHandler.removeCallbacks(timeOut);
                        LogUtils.e("连接上了wifi");
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                boolean isRun = true;
                                int fail_count = 0;
                                while (isRun && !isFinishing()) {
                                    ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
                                    final NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

//                                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
//                                    SupplicantState supState;
//                                    supState = wifiInfo.getSupplicantState();
//
//                                    if ((supState.equals(SupplicantState.DISCONNECTED))) {
//                                        fail_count = fail_count + 1;
//                                        if (fail_count >= 15) {
//                                            isRun = false;
//                                            wifiHandler.sendEmptyMessage(CONNECTFAILED);
//                                        }
//                                    }

                                    if (networkInfo.isConnected()) {
                                        isRun = false;
                                        wifiHandler.sendEmptyMessage(CHECK_CAMERA);
                                        CameraFactory.getInstance().sendSearchDevice();
//                                        if (WifiUtil.checkNetwork(VLCApplication.getAppContext(), 0)) {
//                                            SpUtils.putString(WifiConnectActivity.this, "SSID", wifiName.getText().toString());
//                                            SpUtils.putString(WifiConnectActivity.this, "BSSID", wifiInfo.getBSSID());
//                                            SpUtils.putString(WifiConnectActivity.this, "pwd", wifiPassword.getText().toString().trim());
//                                            SpUtils.putString(WifiConnectActivity.this, "wifi_encryption_type", mScanResult.capabilities);
//                                            WifiDao wifiDao = new WifiDao();
//                                            if (!TextUtils.isEmpty(wifiInfo.getBSSID())) {
//                                                WifiBean wifiBean = new WifiBean();
//                                                wifiBean.BSSID = wifiInfo.getBSSID();
//                                                String ssid = wifiInfo.getSSID();
//                                                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
//                                                    ssid = ssid.substring(1, ssid.length() - 1);
//                                                }
//                                                wifiBean.SSID = ssid;
//                                                wifiBean.netId = wifiInfo.getNetworkId();
//                                                wifiBean.encrypt = mScanResult.capabilities;
//                                                wifiBean.pwd = wifiPassword.getText().toString().trim();
//                                                wifiBean.product = CameraFactory.ID_Novatek;
//                                                wifiDao.insertWifi(wifiBean);
//                                            }
//                                            wifiHandler.sendEmptyMessage(CONNECTED);
//                                        } else {
//                                            wifiHandler.sendEmptyMessage(CONNECTED_NO_CAMERA);
//                                        }
                                    } else {
                                        fail_count = fail_count + 1;
                                        if (fail_count >= 20) {
                                            isRun = false;
                                            wifiHandler.sendEmptyMessage(CONNECTFAILED);
                                        }
                                    }

                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }).start();
                    } else {
                        wifiHandler.sendEmptyMessage(CONNECTFAILED);
                    }
                }


            }

//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//                NetworkInfo info = intent
//                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                if (info != null) {
//                    //如果当前的网络连接成功并且网络连接可用
//                    if (info.getType() == ConnectivityManager.TYPE_WIFI) {
//                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    WifiInfo wifi = wifiManager.getConnectionInfo();
//                                    if (wifi.getSSID().replaceAll("\"", "").equals(wifiName.getText())) {
//                                        if (WifiUtil.checkNetwork(VLCApplication.getAppContext(), 0)) {
//                                            SpUtils.putString(WifiConnectActivity.this, "SSID", wifiName.getText().toString());
//                                            SpUtils.putString(WifiConnectActivity.this, "BSSID", wifi.getBSSID());
//                                            SpUtils.putString(WifiConnectActivity.this, "pwd", wifiPassword.getText().toString().trim());
//                                            SpUtils.putString(WifiConnectActivity.this, "wifi_encryption_type", mScanResult.capabilities);
//                                            WifiDao wifiDao = new WifiDao();
//                                            if (!TextUtils.isEmpty(wifi.getBSSID())) {
//                                                WifiBean wifiBean = new WifiBean();
//                                                wifiBean.BSSID = wifi.getBSSID();
//                                                String ssid = wifi.getSSID();
//                                                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
//                                                    ssid = ssid.substring(1, ssid.length() - 1);
//                                                }
//                                                wifiBean.SSID = ssid;
//                                                wifiBean.netId = wifi.getNetworkId();
//                                                wifiBean.encrypt = mScanResult.capabilities;
//                                                wifiBean.pwd = wifiPassword.getText().toString().trim();
//                                                wifiBean.product = CameraFactory.ID_Novatek;
//                                                wifiDao.insertWifi(wifiBean);
//                                            }
//                                        }
//                                        wifiHandler.sendEmptyMessage(CONNECTED);
//                                    }
//                                }
//                            }).start();
//                        } else {
//                            mProgressDialog.setMessage(getResources().getStringArray(R.array.wifi_status)[2]);
//                        }
//                    }
//                }
//            }
        }

    }

    private Runnable timeOut = new Runnable() {

        @Override
        public void run() {
            LogUtils.e("连接超时");
            wifiHandler.sendEmptyMessage(CONNECTFAILED);
        }
    };

    private Runnable scanTimeOut = new Runnable() {
        @Override
        public void run() {
            LogUtils.e("扫描超时");
            wifiHandler.sendEmptyMessage(ENDLOADING);
        }
    };

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("9527", "onRestart isFromSystem = " + isFromSystem);

        String curBSSID = wifiManager.getConnectionInfo().getBSSID();//当前连接的wifi MAC
        if (curBSSID != null && curBSSID.startsWith("\"") && curBSSID.endsWith("\"")) {
            curBSSID = curBSSID.substring(1, curBSSID.length() - 1);
        }
        Set<String> set = SpUtils.getHashSet(context, "license");
        if (set != null && set.contains(curBSSID)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (isFromSystem) {
                        int count = 0;
                        while (count++ < 5) {

                            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                            Log.e("9999", "  connectionInfo = " + connectionInfo.toString());
                            if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                                Log.e("9999", "开始连接流程");
                                wifiHandler.postDelayed(timeOut, timeOutNum);
                                wifiHandler.sendEmptyMessage(BACK_CONNECTED);
                                break;
                            }

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }


                }
            }).start();
        }


    }

    private void getCameraType() {
        final Intent intent = new Intent();
        CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_GET_NEW_VERSION, "", new CameraUtils.CmdCallback() {//获取摄像头信息
            @Override
            public void success(int commandId, String par, MovieRecord movieRecord) {
                String cameraVersionCurrent = movieRecord.getString();
                SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_VERSION_CURRENT, cameraVersionCurrent);
                intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
                startActivity(intent);
                WifiConnectActivity.this.finish();
            }

            @Override
            public void failed(int commandId, String par, String error) {
                intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
                startActivity(intent);
                WifiConnectActivity.this.finish();
            }
        });
    }


    private String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK"};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return "Open";
    }

}
