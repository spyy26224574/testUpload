package com.adai.gkdnavi.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.adai.camera.CameraFactory;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkd.httputils.HttpUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MovieRecord;

import org.apache.http.conn.util.InetAddressUtils;
import org.videolan.vlc.util.DomParseUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiUtil {
    private static final String TAG = "WifiUtil";
    private WifiBroadcastReceiver mWifiBroadcastReceiver;
    private boolean mIsRun;

    public enum WifiCipherType {
        WIFI_CIPHER_WEP, WIFI_CIPHER_WPA_EAP, WIFI_CIPHER_WPA_PSK, WIFI_CIPHER_WPA2_PSK, WIFI_CIPHER_NOPASS
    }

    public interface WifiConnectInfo {
        /**
         * 正在扫描
         */
        public void onScaning();

        /**
         * 连接中
         */
        public void onConnecting();

        /**
         * 成功连接到当前蓝牙设备对应摄像头wifi
         */
        public void onConnected();

        /**
         * 连接失败
         */
        public void onConnectedFailed();

        /**
         * 未找到设备
         */
        public void onNotfound();

        /**
         * 添加错误或失败
         */
        public void onError();

        /**
         * 添加错误或失败,需要删除系统连接
         */
        public void onErrorWantDelete();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (connectInfo == null) {
                return;
            }
            switch (msg.what) {
                case 0:
                    connectInfo.onScaning();
                    break;
                case 1:
                    connectInfo.onConnecting();
                    break;
                case 2:
                    connectInfo.onNotfound();
                    handler.removeCallbacks(timeOut);
                    break;
                case 3:
                    connectInfo.onConnected();
                    handler.removeCallbacks(timeOut);
                    unRegisterWifiCast();
                    break;
                case 4:
                    connectInfo.onConnectedFailed();
                    handler.removeCallbacks(timeOut);
                    unRegisterWifiCast();
                    break;
                case 5:
                    connectInfo.onError();
                    handler.removeCallbacks(timeOut);
                    unRegisterWifiCast();
                    break;
                case 6:
                    connectInfo.onErrorWantDelete();
                    handler.removeCallbacks(timeOut);
                    unRegisterWifiCast();
                    break;
                default:
                    break;
            }
        }
    };

    private Runnable timeOut = new Runnable() {

        @Override
        public void run() {
            LogUtils.e("超时");
            handler.sendEmptyMessage(4);
        }
    };

    private int timeOutNum = 25000;

    private static WifiUtil _instance;
    private String shareName = "currentwifiinfo";
    private String key_name = "key_name";
    private String key_mac = "key_mac";
    private String mSSID = "";
    private String mBSSID = "";
    private String mPwd = "12345678";
    private String mType = "PSK";
    private WifiConnectInfo connectInfo;
    private Context context;
    private WifiManager wifiManager;
    private AtomicBoolean mConnected = new AtomicBoolean(false);

//    private boolean hasRegister = false;

    private int currentid = -1;
    private int lastWifiNetid = -1;

    public int getCurrentid() {
        return currentid;
    }

    public int getLastWifiNetid() {
        return lastWifiNetid;
    }

    public WifiConnectInfo getConnectInfo() {
        return connectInfo;
    }

    public void setConnectInfo(WifiConnectInfo connectInfo) {
        this.connectInfo = connectInfo;
    }

    int key_type;

    Handler aphandler, closeWifiHandler;
    StratWifiApThread swat;
    CloseWifiThread cwt;
    StringBuilder resultList;
    ArrayList<String> connectedIP;

    private WifiUtil() {

    }

    public static WifiUtil getInstance() {
        if (_instance == null) {
            _instance = new WifiUtil();
        }
        return _instance;
    }

    public void saveWifi(String wifistr, Context context) {
        if (wifistr == null || context == null) {
            return;
        }
        String[] wifi = wifistr.split(",");
        if (wifi.length != 2) {
            return;
        }
        SharedPreferences pref = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        pref.edit().putString(key_name, wifi[0]).commit();
        pref.edit().putString(key_mac, wifi[1]).commit();
    }

    public String getWifiName(Context context) {
        SharedPreferences pref = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return pref.getString(key_name, null);
    }

    public String getWifiMac(Context context) {
        SharedPreferences pref = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        return pref.getString(key_mac, null);
    }

    public boolean connectWifi(Context context, String ssid, String bssid, String pwd, String mWifiType) {
        if (context == null) {
            return false;
        }
        mIsRun = true;
        mSSID = ssid;
        mBSSID = bssid;
        mPwd = pwd;
        mType = mWifiType;
        this.context = context;
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo currentconnect = wifiManager.getConnectionInfo();
        if (currentconnect != null && currentconnect.getNetworkId() != -1) {
            if (ssid.equals(currentconnect.getSSID()) && bssid.equals(currentconnect.getBSSID())) {
                if (connectInfo != null) {
                    connectInfo.onConnected();
                }
                return true;
            } else {
                lastWifiNetid = currentconnect.getNetworkId();
                wifiManager.disconnect();
            }
        }
        new ConnectThread(wifiManager, ssid, bssid, pwd, mWifiType).start();
        return true;
    }

    public boolean connectWIfi(Context context) {
        if (context == null) {
            return false;
        }
        String wifiname;
        String wifimac;
        String pwd;
        String wifitype;

        wifiname = SpUtils.getString(context, "SSID", "");
        if (!TextUtils.isEmpty(wifiname) && wifiname.startsWith("\"")) {
            wifiname = wifiname.substring(1, wifiname.length() - 1);
        }
        wifimac = SpUtils.getString(context, "BSSID", "");
        pwd = SpUtils.getString(context, "pwd", "12345678");
        wifitype = SpUtils.getString(context, "wifi_encryption_type", "PSK");


        return connectWifi(context, wifiname, wifimac, pwd, wifitype);
    }

    public static void setBssid(String bssid) {
        SpUtils.putString(VLCApplication.getAppContext(), "BSSID", bssid);
    }

    public static String getBssid() {
        return SpUtils.getString(VLCApplication.getAppContext(), "BSSID", "");
    }

    private boolean isfirst = true;

    private void setWifiBroadcast() {
        if (context == null || mWifiBroadcastReceiver != null) {
            return;
        }
        IntentFilter mWifiStateFilter = new IntentFilter();
//        mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
//        mWifiStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mWifiStateFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mWifiBroadcastReceiver = new WifiBroadcastReceiver();
        context.registerReceiver(mWifiBroadcastReceiver, mWifiStateFilter);
//        hasRegister = true;
        isfirst = true;
    }

    public void unRegisterWifiCast() {
//        if (hasRegister) {
        if (context != null && mWifiBroadcastReceiver != null) {
            try {
                context.unregisterReceiver(mWifiBroadcastReceiver);
                mWifiBroadcastReceiver = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//            hasRegister = false;
//        }
    }

    public void breakWifiThread() {
        mIsRun = false;
    }

    public void removeTimeOutRunnable() {
        handler.removeCallbacks(timeOut);
    }

    private class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent) {
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
//            if (isfirst) {
//                isfirst = false;
//                return;
//            }
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                WifiInfo connectionInfo = wifiManager.getConnectionInfo();
//                LogUtils.e(connectionInfo.toString());
                if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED && !TextUtils.isEmpty(mSSID) && connectionInfo.getSSID().replace("\"", "").equals(mSSID)) {
                    unRegisterWifiCast();
                    if (!TextUtils.isEmpty(mSSID) && connectionInfo.getSSID().replace("\"", "").equals(mSSID)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                handler.removeCallbacks(timeOut);
                                mIsRun = true;
                                int fail_count = 0;
                                while (mIsRun) {
                                    ConnectivityManager cm = (ConnectivityManager) VLCApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                                    final NetworkInfo networkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                                    if (networkInfo.isConnected()) {
                                        mIsRun = false;
                                        handler.sendEmptyMessage(3);
                                    } else {
                                        fail_count = fail_count + 1;
                                        if (fail_count >= 20) {
                                            mIsRun = false;
                                            handler.sendEmptyMessage(4);
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
                        handler.sendEmptyMessage(4);
                    }
                }
            }
//            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//                NetworkInfo info = intent
//                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
//                if (info != null) {
//                    //如果当前的网络连接成功并且网络连接可用
//                    if (info.getType() == ConnectivityManager.TYPE_WIFI)
//                        if (NetworkInfo.State.CONNECTED == info.getState() && info.isAvailable()) {
//
//                            handler.removeCallbacks(timeOut);
//                            WifiInfo wifi = wifiManager.getConnectionInfo();
//                            Log.e(TAG, "onReceive: curWifiSSid = " + wifi.getSSID().replaceAll("\"", "")
//                                    + ",mSSID = " + mSSID + " curWifiSSId == mSSID ?" + wifi.getSSID().replaceAll("\"", "").equals(mSSID));
//                            if (wifi.getSSID().replaceAll("\"", "").equals(mSSID)) {
//                                handler.sendEmptyMessage(3);
//                            } else {
//                                handler.sendEmptyMessage(4);
//                                wifiManager.removeNetwork(currentid);
//                            }
//                        }
//                }
//            }
        }
    }

    ;

//    /**
//     * Supplicant associating or authenticating is considered a handshake state {@hide}
//     */
//    public boolean isHandshakeState(SupplicantState state) {
//        switch (state) {
//            case AUTHENTICATING:
//            case ASSOCIATING:
//            case ASSOCIATED:
//            case FOUR_WAY_HANDSHAKE:
//            case GROUP_HANDSHAKE:
//                return true;
//            case COMPLETED:
//            case DISCONNECTED:
//            case INTERFACE_DISABLED:
//            case INACTIVE:
//            case SCANNING:
//            case DORMANT:
//            case UNINITIALIZED:
//            case INVALID:
//                return false;
//            default:
//                return false;
//        }
//    }

//    private void updateConnectionState(DetailedState state) {
//        if (state == null) return;
//        int netid = wifiManager.getConnectionInfo().getNetworkId();
//        if (netid != currentid) return;
//        String summary = Summary.get(context, wifiManager.getConnectionInfo().getSSID(),
//                state);
//
//        switch (state) {
//            case DISCONNECTED:
//                break;
//            case FAILED:
//                handler.sendEmptyMessage(4);
////                unRegisterWifiCast();
//                handler.removeCallbacks(timeOut);
//                break;
//            case CONNECTED:
//                handler.sendEmptyMessage(3);
////                unRegisterWifiCast();
//                handler.removeCallbacks(timeOut);
//                break;
//            case CONNECTING:
//                handler.sendEmptyMessage(1);
//                break;
//            default:
//                break;
//        }
//    }

    private class ConnectThread extends Thread {
        private WifiManager manager;
        private String name, mac, pwd, type;

        public ConnectThread(WifiManager manager, String name, String mac, String pwd, String mWifiType) {
            this.manager = manager;
            this.name = name;
            this.mac = mac;
            this.pwd = pwd;
            this.type = mWifiType;
        }

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
            final WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + name + "\"";
            switch (getScanResultSecurity(type)) {
                case "WEP":
                    Log.e("WEP", "WEP");
                    conf.wepKeys[0] = "\"" + pwd + "\"";
                    conf.wepTxKeyIndex = 0;
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    break;
                case "PSK":
                    Log.e("PSK", "PSK");
                    conf.preSharedKey = "\"" + pwd + "\"";
                    break;
                case "Open":
                    conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    break;
            }

            int netId = wifiManager.addNetwork(conf);
            if (netId == -1) {
                handler.sendEmptyMessage(6);
                return;
            }
            wifiManager.enableNetwork(netId, true);


            setWifiBroadcast();
            handler.removeCallbacks(timeOut);
            handler.postDelayed(timeOut, timeOutNum);


//            if (TextUtils.isEmpty(mac)) {
//                while (count < 5) {
//                    manager.startScan();
//                    List<ScanResult> results = manager.getScanResults();
//                    if (results == null) {
//                        continue;
//                    } else {
//                        for (ScanResult scanResult : results) {
//                            if (scanResult.SSID.equals(name) && scanResult.BSSID.equals(mac)) {
////                            WifiConfiguration config = this.IsExsits(scanResult.SSID);
////                            int networkid = -1;
////                            if (config != null) {
////                                networkid = config.networkId;
////                            } else {
//                                int networkid = createWifiInfo2(scanResult, pwd);
////                            }
//                                if (networkid == -1) {
//                                    handler.sendEmptyMessage(5);
//                                } else {
//                                    currentid = networkid;
////                                    try {
////                                        Thread.sleep(1500);
////                                    } catch (InterruptedException e) {
////                                        e.printStackTrace();
////                                    }
//                                    setWifiBroadcast();
//                                    Method method = connectWifiByReflectMethod(networkid);
//                                    if (method == null) {
//                                        manager.enableNetwork(networkid, true);
////                                        manager.reconnect();
//                                    }
////                                boolean b = manager.enableNetwork(networkid, true);
////                                Log.e("ryujin", "run: 连接成功？" + b);
//                                    LogUtils.e("添加了超时");
//                                    handler.removeCallbacks(timeOut);
//                                    handler.postDelayed(timeOut, timeOutNum);
//                                }
//                                return;
//                            }
//                        }
//                    }
//                    try {
//                        Thread.sleep(3000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    count++;
//                }
//                handler.sendEmptyMessage(2);
//            } else {
//                int networkid = createWifiInfo3(name, mac, pwd);
//                if (networkid == -1) {
//                    handler.sendEmptyMessage(5);
//                } else {
//                    currentid = networkid;
////                    try {
////                        Thread.sleep(1500);
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
//                    setWifiBroadcast();
//                    Method method = connectWifiByReflectMethod(networkid);
//                    if (method == null) {
//                        manager.enableNetwork(networkid, true);
//                    }
//                    LogUtils.e("添加了超时");
//                    handler.removeCallbacks(timeOut);
//                    handler.postDelayed(timeOut, timeOutNum);
//                }
//            }

        }

        public int createWifiInfo3(String ssid, String bssid, String pwd) {
            WifiCipherType type;
            String wifi_encryption_type = SpUtils.getString(context, "wifi_encryption_type", "");
            if (wifi_encryption_type.contains("WPA2-PSK")) {
                type = WifiCipherType.WIFI_CIPHER_WPA2_PSK;
            } else if (wifi_encryption_type.contains("WPA-PSK")) {
                type = WifiCipherType.WIFI_CIPHER_WPA_PSK;
            } else if (wifi_encryption_type.contains("WPA-EAP")) {
                type = WifiCipherType.WIFI_CIPHER_WPA_EAP;
            } else if (wifi_encryption_type.contains("WEP")) {
                type = WifiCipherType.WIFI_CIPHER_WEP;
            } else {
                type = WifiCipherType.WIFI_CIPHER_NOPASS;
            }
            WifiConfiguration config = CreateWifiInfo(ssid,
                    bssid, pwd, type);
            if (config != null) {
                return manager.addNetwork(config);
            } else {
                return -1;
            }
        }

        int createWifiInfo2(ScanResult wifiinfo, String pwd) {
            WifiCipherType type;

            if (wifiinfo.capabilities.contains("WPA2-PSK")) {
                type = WifiCipherType.WIFI_CIPHER_WPA2_PSK;
            } else if (wifiinfo.capabilities.contains("WPA-PSK")) {
                type = WifiCipherType.WIFI_CIPHER_WPA_PSK;
            } else if (wifiinfo.capabilities.contains("WPA-EAP")) {
                type = WifiCipherType.WIFI_CIPHER_WPA_EAP;
            } else if (wifiinfo.capabilities.contains("WEP")) {
                type = WifiCipherType.WIFI_CIPHER_WEP;
            } else {
                type = WifiCipherType.WIFI_CIPHER_NOPASS;
            }

            WifiConfiguration config = CreateWifiInfo(wifiinfo.SSID,
                    wifiinfo.BSSID, pwd, type);
            if (config != null) {
                return manager.addNetwork(config);
            }
            return -1;

        }

        public WifiConfiguration CreateWifiInfo(String SSID, String BSSID,
                                                String password, WifiCipherType type) {

            int priority;

            this.IsExsits(SSID);

            WifiConfiguration config = new WifiConfiguration();
//            config.allowedAuthAlgorithms.clear();
//            config.allowedGroupCiphers.clear();
//            config.allowedKeyManagement.clear();
//            config.allowedPairwiseCiphers.clear();
//            config.allowedProtocols.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                config.SSID = SSID;
            } else {
                config.SSID = "\"" + SSID + "\"";
            }
            config.status = WifiConfiguration.Status.ENABLED;

//            priority = getMaxPriority() + 1;
//            if (priority > 99999) {
//                priority = shiftPriorityAndSave();
//            }
//
//            config.priority = priority;
            if (type == WifiCipherType.WIFI_CIPHER_NOPASS) {
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//                config.wepTxKeyIndex = 0;
            } else if (type == WifiCipherType.WIFI_CIPHER_WEP) {
                config.wepKeys[0] = "\"" + password + "\"";
                config.wepTxKeyIndex = 0;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.preSharedKey = "\"" + password + "\"";
//
//            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
            } else if (type == WifiCipherType.WIFI_CIPHER_WPA_EAP) {

                config.preSharedKey = "\"" + password + "\"";
//                config.hiddenSSID = true;
//                config.status = WifiConfiguration.Status.ENABLED;
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
//
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN | WifiConfiguration.Protocol.WPA);

            } else if (type == WifiCipherType.WIFI_CIPHER_WPA_PSK) {

                config.preSharedKey = "\"" + password + "\"";
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

            } else if (type == WifiCipherType.WIFI_CIPHER_WPA2_PSK) {

                config.preSharedKey = "\"" + password + "\"";
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//
//                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

            } else {
                return null;
            }

            return config;
        }

        public WifiConfiguration setMaxPriority(WifiConfiguration config) {
            int priority = getMaxPriority() + 1;
            if (priority > 99999) {
                priority = shiftPriorityAndSave();
            }

            config.priority = priority;
            manager.updateNetwork(config);

            return config;
        }

        private Method connectWifiByReflectMethod(int netId) {
            Method connectMethod = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                // 反射方法： connect(int, listener) , 4.2 <= phone's android version
                for (Method methodSub : manager.getClass()
                        .getDeclaredMethods()) {
                    if ("connect".equalsIgnoreCase(methodSub.getName())) {
                        Class<?>[] types = methodSub.getParameterTypes();
                        if (types != null && types.length > 0) {
                            if ("int".equalsIgnoreCase(types[0].getName())) {
                                connectMethod = methodSub;
                                break;
                            }
                        }
                    }
                }
                if (connectMethod != null) {
                    try {
                        connectMethod.invoke(manager, netId, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
                // 反射方法: connect(Channel c, int networkId, ActionListener listener)
                // 暂时不处理4.1的情况 , 4.1 == phone's android version
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                // 反射方法：connectNetwork(int networkId) ,
                // 4.0 <= phone's android version < 4.1
                for (Method methodSub : manager.getClass()
                        .getDeclaredMethods()) {
                    if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                        Class<?>[] types = methodSub.getParameterTypes();
                        if (types != null && types.length > 0) {
                            if ("int".equalsIgnoreCase(types[0].getName())) {
                                connectMethod = methodSub;
                                break;
                            }
                        }
                    }
                }
                if (connectMethod != null) {
                    try {
                        connectMethod.invoke(manager, netId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            } else {
                // < android 4.0
                return null;
            }
            return connectMethod;
        }

        public WifiConfiguration IsExsits(String SSID) {
            List<WifiConfiguration> existingConfigs = manager
                    .getConfiguredNetworks();
            if (existingConfigs == null || existingConfigs.size() <= 0) {
                return null;
            }
            for (WifiConfiguration existingConfig : existingConfigs) {

                if (("\"" + SSID + "\"").equals(existingConfig.SSID) || SSID.equals(existingConfig.SSID)) {
                    manager.removeNetwork(existingConfig.networkId);
                    manager.saveConfiguration();
                    return existingConfig;
                }
            }
            return null;
        }

        private int getMaxPriority() {
            List<WifiConfiguration> localList = this.manager
                    .getConfiguredNetworks();
            int i = 0;
            if (localList != null) {

                Iterator<WifiConfiguration> localIterator = localList.iterator();
                while (true) {
                    if (!localIterator.hasNext()) {
                        return i;
                    }
                    WifiConfiguration localWifiConfiguration = localIterator.next();
                    if (localWifiConfiguration.priority <= i) {
                        continue;
                    }
                    i = localWifiConfiguration.priority;
                }
            } else {
                return i;
            }
        }

        private int shiftPriorityAndSave() {
            List<WifiConfiguration> localList = manager.getConfiguredNetworks();
            sortByPriority(localList);
            int i = localList.size();
            for (int j = 0; ; ++j) {
                if (j >= i) {
                    manager.saveConfiguration();
                    return i;
                }
                WifiConfiguration localWifiConfiguration = (WifiConfiguration) localList.get(j);
                localWifiConfiguration.priority = j;
                this.manager.updateNetwork(localWifiConfiguration);
            }
        }

        private void sortByPriority(List<WifiConfiguration> paramList) {
            Collections.sort(paramList, new SjrsWifiManagerCompare());
        }

        class SjrsWifiManagerCompare implements Comparator<WifiConfiguration> {
            @Override
            public int compare(WifiConfiguration paramWifiConfiguration1,
                               WifiConfiguration paramWifiConfiguration2) {
                return paramWifiConfiguration1.priority - paramWifiConfiguration2.priority;
            }
        }

    }

    public void gotoWifiSetting(Context context) {
        Intent intent = new Intent("android.settings.WIFI_SETTINGS");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(wifi);
//		Intent intent = new Intent();
//		intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
//		intent.putExtra("extra_prefs_show_button_bar", true);
//intent.putExtra("extra_prefs_set_next_text", "完成");
//intent.putExtra("extra_prefs_set_back_text", "返回");
//		intent.putExtra("wifi_enable_next_on_connect", true);
        //TODO 改成startActivityForResult试试
        context.startActivity(intent);
    }


    public void startAP(Context context) {
        Log.e("9527", "startAP");
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        context.registerReceiver(wifiReceiver, new IntentFilter(
//                "android.net.wifi.WIFI_AP_STATE_CHANGED"));
        startWifiAp();
    }

    private ArrayList<String> getConnectIp() throws Exception {
        ArrayList<String> connectIpList = new ArrayList<String>();
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitted = line.split(" +");
            if (splitted != null && splitted.length >= 4) {
                String ip = splitted[0];
                connectIpList.add(ip);
            }
        }
        return connectIpList;
    }

    private void startWifiAp() {
        aphandler = new Handler();
        if (wifiManager.isWifiEnabled()) {
            closeWifiHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    startWifiApTh();
                    super.handleMessage(msg);
                }
            };
            cwt = new CloseWifiThread();
            Thread thread = new Thread(cwt);
            thread.start();

        } else {
            startWifiApTh();
        }

    }

    public void stopWifiAp() {
        try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            method2.invoke(wifiManager, config, false);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    class CloseWifiThread implements Runnable {
        public CloseWifiThread() {
            super();
        }

        @Override
        public void run() {
            int state = wifiManager.getWifiState();
            if (state == WifiManager.WIFI_STATE_ENABLED) {
                wifiManager.setWifiEnabled(false);
                closeWifiHandler.postDelayed(cwt, 1000);
            } else if (state == WifiManager.WIFI_STATE_DISABLING) {
                closeWifiHandler.postDelayed(cwt, 1000);
            } else if (state == WifiManager.WIFI_STATE_DISABLED) {
                closeWifiHandler.sendEmptyMessage(0);
            }

        }
    }

    private void startWifiApTh() {
        swat = new StratWifiApThread();
        Thread thread = new Thread(swat);
        thread.start();
    }

    class StratWifiApThread implements Runnable {
        public StratWifiApThread() {
            super();
        }

        @Override
        public void run() {
            WifiApUtil mWifiUtil = new WifiApUtil();
            int state = mWifiUtil.getWifiApState(wifiManager);
            Log.e("9527", "state = " + state);
            LogUtils.e("state=" + state);
            if (state == WifiApUtil.WIFI_AP_STATE_DISABLED) {
                mWifiUtil.stratWifiAp(wifiManager);
                aphandler.postDelayed(swat, 1000);
            } else if (state == WifiApUtil.WIFI_AP_STATE_ENABLING
                    || state == WifiApUtil.WIFI_AP_STATE_FAILED) {
                aphandler.postDelayed(swat, 1000);
            } else if (state == WifiApUtil.WIFI_AP_STATE_ENABLED) {
                // context.unregisterReceiver(wifiReceiver);
//                DhcpInfo info = wifiManager.getDhcpInfo();
//               Log.e("9527", "info = " + info.toString());

            }
        }

    }

    public class WifiApUtil {

        public static final int WIFI_AP_STATE_DISABLING = 10;
        public static final int WIFI_AP_STATE_DISABLED = 11;
        public static final int WIFI_AP_STATE_ENABLING = 12;
        public static final int WIFI_AP_STATE_ENABLED = 13;
        public static final int WIFI_AP_STATE_FAILED = 14;

        public void stratWifiAp(WifiManager wifiManager) {
            Method method1 = null;
            try {
                method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
                        WifiConfiguration.class, boolean.class);
                WifiConfiguration netConfig = new WifiConfiguration();
                // wifi热点名字
                netConfig.SSID = "LIGO-Cam";
                netConfig.allowedAuthAlgorithms
                        .set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                Log.e("9527", "KeyMgmt = " + WifiConfiguration.KeyMgmt.strings.length);
                for (int i = 0; i < WifiConfiguration.KeyMgmt.strings.length; i++) {
                    Log.e("9527", "KeyMgmt.strings[" + i + "] = "
                            + WifiConfiguration.KeyMgmt.strings[i]);

                    if (WifiConfiguration.KeyMgmt.strings[i].endsWith("WPA2_PSK")) {
                        key_type = i;
                    }

                }

                netConfig.allowedKeyManagement.set(key_type);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers
                        .set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers
                        .set(WifiConfiguration.GroupCipher.TKIP);
                // 密码
                netConfig.preSharedKey = "12345678";

                method1.invoke(wifiManager, netConfig, true);

            } catch (IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

        }

        public int getWifiApState(WifiManager wifiManager) {
            try {
                Method method = wifiManager.getClass().getMethod(
                        "getWifiApState");
                int i = (Integer) method.invoke(wifiManager);
                Log.e("9527", "wifi state:  " + i);
                return i;
            } catch (Exception e) {
                Log.e("9527", "Cannot get WiFi AP state" + e);
                return WIFI_AP_STATE_FAILED;
            }
        }
    }

//    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(action)) {
//                // 便携式热点的状态为：10---正在关闭；11---已关闭；12---正在开启；13---已开启
//                int state = intent.getIntExtra("wifi_state", 0);
//                int previous_wifi_state = intent.getIntExtra(
//                        "previous_wifi_state", 0);
//                Log.e("9527", "BroadcastReceiver wifi_state= " + state);
//                Log.e("9527", "BroadcastReceiver previous_wifi_state= "
//                        + previous_wifi_state);
//            }
//        }
//    };

    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress())) {
                        return ipaddress = "本机的ip是" + "：" + ip.getHostAddress();
                    }
                }

            }
        } catch (SocketException e) {
            Log.e("9527", "获取本地ip地址失败");
            e.printStackTrace();
        }
        return ipaddress;

    }

    private static String ipString = "14.215.177.37";//www.baidu.com

    public static boolean pingNet() {
        try {
            HttpUtil.GetDnsTask getDnsTask = new HttpUtil.GetDnsTask("www.baidu.com");
            Thread thread = new Thread(getDnsTask);
            thread.start();
            thread.join(1000);
            return getDnsTask.getInetAddress() != null;
        } catch (Exception e) {
            return false;
        }
//        Log.e("9527", "begin" + System.currentTimeMillis() + "");
//        String result = null;
//        try {
//            Process p = Runtime.getRuntime().exec("ping -c 1 -w 1 " + ipString);// ping网址3次
//            int status = p.waitFor();
//
//            // 读取ping的内容，可以不加
////            InputStream input = p.getInputStream();
////            BufferedReader in = new BufferedReader(new InputStreamReader(input));
////            StringBuffer stringBuffer = new StringBuffer();
////            String content = "";
////            while ((content = in.readLine()) != null) {
////                stringBuffer.append(content);
////            }
////            Log.e("------ping-----", "result content : " + stringBuffer.toString());
//
//            // ping的状态
//            if (p.exitValue() != 0) {
//                p.destroy();
//            }
//            Log.e("------ping-----", "status : " + status);
//            if (status == 0) {
//                result = "success";
//                Log.e("9527", "true" + System.currentTimeMillis() + "");
//                return true;
//            } else {
//                result = "failed";
//            }
//        } catch (IOException e) {
//            result = "IOException";
//        } catch (InterruptedException e) {
//            result = "InterruptedException";
//        } finally {
//            Log.d("----result---", "result = " + result);
//        }
//        Log.e("9527", "false" + System.currentTimeMillis() + "");
//        return false;

    }
//    public static boolean pingNet() {
//        String result = null;
//        try {
//            String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
//            Process p = Runtime.getRuntime().exec("ping -c 1 -w 100 " + ip);// ping网址3次
//            // 读取ping的内容，可以不加
//            InputStream input = p.getInputStream();
//            BufferedReader in = new BufferedReader(new InputStreamReader(input));
//            StringBuffer stringBuffer = new StringBuffer();
//            String content = "";
//            while ((content = in.readLine()) != null) {
//                stringBuffer.append(content);
//            }
//            Log.d("------ping-----", "result content : " + stringBuffer.toString());
//            // ping的状态
//            int status = p.waitFor();
//            if (status == 0) {
//                result = "success";
//                return true;
//            } else {
//                result = "failed";
//            }
//        } catch (IOException e) {
//            result = "IOException";
//        } catch (InterruptedException e) {
//            result = "InterruptedException";
//        } finally {
//            Log.d("----result---", "result = " + result);
//        }
//        return false;
//    }

    public static boolean checkNetwork(Context context, int netmode) {
        int tryCount = 0;
        while (tryCount++ < 3) {
//            if (checkNovatek(netmode)) {
//                try {
//                    URL url = new URL(Contacts.URL_GET_CAMERA_INFO);
//                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                    conn.setConnectTimeout(2000);
//                    conn.setReadTimeout(2000);
//                    conn.setUseCaches(false);
//                    conn.connect();
//                    InputStream inputStream = conn.getInputStream();
//                    DomParseUtils domParseUtils = new DomParseUtils();
//                    MovieRecordValue movieRecordValue = domParseUtils.getParserXmls(inputStream);
//                    String cameraInfo = movieRecordValue.getString();
//                    LogUtils.e("cameraInfo = " + cameraInfo);
//                    String[] split = cameraInfo.split(";");
//                    if (split.length != 8) {
//                        continue;
//                    }
//                    String product = split[2];
//                    LogUtils.e("product = " + product);
//                    for (String branding : CameraFactory.NOVATEK_SUPPORT_BRANDING) {
//                        if (branding.equals(product.toLowerCase())) {
//                            SpUtils.putString(context, CameraActivity.CAMERA_VERSION_CURRENT, cameraInfo);
//                            CameraFactory.deviceNumber = split[4];
//                            return true;
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
            return checkNovatek(netmode);
        }
        return false;
    }

    private static boolean checkNovatek(int netmode) {
        if (netmode == 0) {
            HttpURLConnection conn = null;
            try {
//                WifiManager wifiManager = (WifiManager) VLCApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//                WifiInfo info = wifiManager.getConnectionInfo();
//                if (info != null && info.getNetworkId() != -1 && info.getSSID().contains("O5-")) {
//                    return true;
//                }
                URL url = new URL(Contacts.URL_HEARTBEAT);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2500);
                conn.setReadTimeout(2500);
                conn.setUseCaches(false);
                conn.connect();
                InputStream is = conn.getInputStream();
                DomParseUtils domParseUtils = new DomParseUtils();
                MovieRecord state = domParseUtils.getParserXml(is);
                if (state != null) {
                    Log.e("wifiUtil 9527", "state = " + state.getStatus());
                }
                conn.disconnect();
                if (state != null && "0".equals(state.getStatus())) {
                    return true;
                }
            } catch (IOException e) {
                Log.e("wifiUtil 9527", "checkNetwork: " + e.getMessage());
                e.printStackTrace();
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return false;
        } else {

            Socket socket = new Socket();
            try {
                socket.connect(
                        new InetSocketAddress(Contacts.BASE_IP, 3333), 5000);
                return true;

            } catch (UnknownHostException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    public void checkAvailableNetwork(Context context) {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String wifiname = SpUtils.getString(context, CameraFactory.KEY_LAST_SSID, "");
        WifiInfo currentconnect = wifiManager.getConnectionInfo();
        if (currentconnect != null && currentconnect.getNetworkId() != -1) {
            int networkId = currentconnect.getNetworkId();
            if (wifiname.equals(currentconnect.getSSID().replaceAll("\"", ""))) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                wifiManager.disconnect();
                wifiManager.removeNetwork(networkId);
            }
        }
        UIUtils.postDelayed(new Runnable() {
            @Override
            public void run() {
                VoiceManager.isCameraBusy = false;
            }
        }, 1000);
    }

    private String getScanResultSecurity(String cap) {
//        final String cap = scanResult.capabilities;
        final String[] securityModes = {"WEP", "PSK"};
        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return "Open";
    }

}
