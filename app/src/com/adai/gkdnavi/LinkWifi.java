package com.adai.gkdnavi;

import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class LinkWifi {

    private static final String TAG = LinkWifi.class.getSimpleName();
    private WifiManager wifiManager;

    public enum WifiCipherType {
        WIFI_CIPHER_WEP, WIFI_CIPHER_WPA_EAP, WIFI_CIPHER_WPA_PSK, WIFI_CIPHER_WPA2_PSK, WIFI_CIPHER_NOPASS
    }

    public LinkWifi(Context context) {
        wifiManager = (WifiManager) context
                .getSystemService(Service.WIFI_SERVICE);
    }

    public boolean checkWifiState() {
        boolean isOpen = true;
        int wifiState = wifiManager.getWifiState();

        if (wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING
                || wifiState == WifiManager.WIFI_STATE_UNKNOWN
                || wifiState == WifiManager.WIFI_STATE_ENABLING) {
            isOpen = false;
        }

        return isOpen;
    }

    public boolean ConnectToNetID(int netID) {
        Method method = connectWifiByReflectMethod(netID);
        return method != null || wifiManager.enableNetwork(netID, true);
    }

    private Method connectWifiByReflectMethod(int netId) {
        Method connectMethod = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
            for (Method methodSub : wifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connect".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId, null);
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
            for (Method methodSub : wifiManager.getClass()
                    .getDeclaredMethods()) {
                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
                    Class<?>[] types = methodSub.getParameterTypes();
                    if (types != null && types.length > 0) {
                        if ("int".equalsIgnoreCase(types[0].getName())) {
                            connectMethod = methodSub;
                        }
                    }
                }
            }
            if (connectMethod != null) {
                try {
                    connectMethod.invoke(wifiManager, netId);
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
        List<WifiConfiguration> existingConfigs = wifiManager
                .getConfiguredNetworks();
        if (existingConfigs == null || existingConfigs.size() <= 0) return null;
        for (WifiConfiguration existingConfig : existingConfigs) {

            if (existingConfig.SSID != null && (existingConfig.SSID.equals("\"" + SSID + "\"") || existingConfig.SSID.equals(SSID))) {
                wifiManager.removeNetwork(existingConfig.networkId);
                wifiManager.saveConfiguration();
                return existingConfig;
            }
        }
        return null;
    }

    public int CreateWifiInfo2(ScanResult wifiinfo, String pwd) {
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

        WifiConfiguration config = CreateWifiInfo(true, wifiinfo.SSID,
                wifiinfo.BSSID, pwd, type);
        Log.e(TAG, "A 11111111");
        if (config != null) {
            int ret = wifiManager.addNetwork(config);
            Log.e(TAG, "A ret =" + ret);
            if (ret == -1) {
                config = CreateWifiInfo(false, wifiinfo.SSID,
                        wifiinfo.BSSID, pwd, type);
                if (config != null) {
                    Log.e(TAG, "A 2222222222");
                    ret = wifiManager.addNetwork(config);
                    Log.e(TAG, "A 3333333333 ret = " + ret);
                }
            }
            return ret;
        } else {
            return -1;
        }
    }

    public WifiConfiguration setMaxPriority(WifiConfiguration config) {
        int priority = getMaxPriority() + 1;
        if (priority > 99999) {
            priority = shiftPriorityAndSave();
        }

        config.priority = priority;
        wifiManager.updateNetwork(config);

        return config;
    }

    public WifiConfiguration CreateWifiInfo(boolean isFirst, String SSID, String BSSID,
                                            String password, WifiCipherType type) {

//        int priority;
        Log.e(TAG, "CreateWifiInfo: isFirts = " + isFirst + ",type = " + type + ",ssid = " + SSID + ",password = " + password);
//        WifiConfiguration config = this.IsExsits(SSID);
        IsExsits(SSID);
        Log.e(TAG, "A IsExsits");
        WifiConfiguration config = new WifiConfiguration();
//        if (config != null) {
//            return setMaxPriority(config);
//        }
//        Log.e(TAG, "CreateWifiInfo: config = " + config);
//        if (config == null) {
//            config = new WifiConfiguration();
//        }
//        config.allowedAuthAlgorithms.clear();
//        config.allowedGroupCiphers.clear();
//        config.allowedKeyManagement.clear();
//        config.allowedPairwiseCiphers.clear();
//        config.allowedProtocols.clear();
        config.SSID = isFirst == (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) ? ("\"" + SSID + "\"") : SSID;
//        config.status = WifiConfiguration.Status.ENABLED;

//        priority = getMaxPriority() + 1;
//        if (priority > 99999) {
//            priority = shiftPriorityAndSave();
//        }
//
//        config.priority = priority;
        if (type == WifiCipherType.WIFI_CIPHER_NOPASS) {
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
//            config.wepTxKeyIndex = 0;
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
//            config.hiddenSSID = true;
//            config.status = WifiConfiguration.Status.ENABLED;
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
//
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        } else if (type == WifiCipherType.WIFI_CIPHER_WPA_PSK) {

            config.preSharedKey = "\"" + password + "\"";
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        } else if (type == WifiCipherType.WIFI_CIPHER_WPA2_PSK) {

            config.preSharedKey = "\"" + password + "\"";
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
//            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
//
//            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
//
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
//            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
//            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        } else {
            return null;
        }

        return config;
    }

    private int getMaxPriority() {
        List<WifiConfiguration> localList = this.wifiManager
                .getConfiguredNetworks();
        int i = 0;
        Iterator<WifiConfiguration> localIterator = localList.iterator();
        while (true) {
            if (!localIterator.hasNext())
                return i;
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localIterator.next();
            if (localWifiConfiguration.priority <= i)
                continue;
            i = localWifiConfiguration.priority;
        }
    }

    private int shiftPriorityAndSave() {
        List<WifiConfiguration> localList = this.wifiManager.getConfiguredNetworks();
        sortByPriority(localList);
        int i = localList.size();
        for (int j = 0; ; ++j) {
            if (j >= i) {
                this.wifiManager.saveConfiguration();
                return i;
            }
            WifiConfiguration localWifiConfiguration = (WifiConfiguration) localList.get(j);
            localWifiConfiguration.priority = j;
            this.wifiManager.updateNetwork(localWifiConfiguration);
        }
    }

    private void sortByPriority(List<WifiConfiguration> paramList) {
        Collections.sort(paramList, new SjrsWifiManagerCompare());
    }

    class SjrsWifiManagerCompare implements Comparator<WifiConfiguration> {
        public int compare(WifiConfiguration paramWifiConfiguration1,
                           WifiConfiguration paramWifiConfiguration2) {
            return paramWifiConfiguration1.priority - paramWifiConfiguration2.priority;
        }
    }
}
