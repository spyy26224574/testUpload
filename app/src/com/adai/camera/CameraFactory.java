package com.adai.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.adai.camera.bean.CameraFactoryRtnMsg;
import com.adai.camera.bean.WifiBean;
import com.adai.camera.hisi.HisiCamera;
import com.adai.camera.hisi.sdk.Common;
import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.product.INovatekCamera;
import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SunplusCamera;
import com.adai.gkd.db.WifiDao;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;

//import static com.jieli.stream.dv.running2.util.IConstant.WIFI_PREFIX;
//import static com.jieli.stream.dv.running2.util.IConstant.WIFI_PREFIX2;

/**
 * Created by huangxy on 2017/3/28.
 */

public class CameraFactory extends AbstractCameraFactory {
    private static final String TAG = "CameraFactory";
    private static ISunplusCamera curSunplusCamera;
    private static HisiCamera curHisiCamera;
    private static volatile CameraFactory INSTANCE = null;
    private CameraCtrlCallback mCameraCtrlCallback;
    public static final int ID_ALLWINNER = 0;
    public static final int ID_SUNPLUSE = 1;
    public static final int ID_Novatek = 2;
    public static final int ID_Shengmai = 3;
    public static final int ID_GP = 4;
    public static final int ID_MSTAR = 5;
    public static final int ID_HISI = 6;
    public static final int ID_JIELI = 7;
    public static int PRODUCT = 0;
    public final static String KEY_LAST_SSID = "last_ssid";
    private String mSsid = "";

    public interface CameraCtrlCallback {
        void handlerCallback(CameraFactoryRtnMsg cameraFactoryRtnMsg);
    }
    public void setCameraCtrlCallback(CameraCtrlCallback cameraCtrlCallback) {
        mCameraCtrlCallback = cameraCtrlCallback;
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String ssid = mSsid;
            if (!TextUtils.isEmpty(ssid)) {
                ssid = ssid.replaceAll("\"", "");
            }
            switch (msg.what) {
                case CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE:
                    SpUtils.putString(VLCApplication.getAppContext(), KEY_LAST_SSID, ssid);
                    PRODUCT = ID_Novatek;
                    if (mCameraCtrlCallback != null) {
                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE));
                    }
                    break;
                case CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE:
                    PRODUCT = ID_SUNPLUSE;
                    SpUtils.putString(VLCApplication.getAppContext(), KEY_LAST_SSID, ssid);
                    if (mCameraCtrlCallback != null) {
                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE));
                    }
                    break;
                case CameraFactoryRtnMsg.RET_HAS_MSTAR:
                    PRODUCT = ID_MSTAR;
                    SpUtils.putString(VLCApplication.getAppContext(), KEY_LAST_SSID, ssid);
                    if (mCameraCtrlCallback != null) {
                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_HAS_MSTAR));
                    }
                    break;
                case CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE:
                    SpUtils.putString(VLCApplication.getAppContext(), KEY_LAST_SSID, ssid);
                    PRODUCT = ID_HISI;
                    if (mCameraCtrlCallback != null) {
                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE));
                    }
                    break;
//                case CameraFactoryRtnMsg.RET_HAS_JIELI_DEVICE:
//                    SpUtils.putString(VLCApplication.getAppContext(), KEY_LAST_SSID, ssid);
//                    if (mCameraCtrlCallback != null) {
//                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_HAS_JIELI_DEVICE));
//                    }
//                    break;
                case CameraFactoryRtnMsg.RET_NO_DEVICE:
                    if (mCameraCtrlCallback != null) {
                        mCameraCtrlCallback.handlerCallback(new CameraFactoryRtnMsg(CameraFactoryRtnMsg.RET_NO_DEVICE));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private CameraFactory() {
    }

    public static CameraFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (CameraFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new CameraFactory();
                    curSunplusCamera = new SunplusCamera();
                    curHisiCamera = new HisiCamera();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public INovatekCamera getNovatekCamera() {
        return null;
    }

    @Override
    public ISunplusCamera getSunplusCamera() {
        return curSunplusCamera;
    }

    public HisiCamera getHisiCamera() {
        return curHisiCamera;
    }

    public void initCamera(Activity context) {
    }

    public void deInitCamera(Activity context) {
    }

    private boolean isSearchByProduct = false;

    public void sendSearchDevice() {
        new Thread(new Runnable() {
            @SuppressWarnings("AlibabaAvoidManuallyCreateThread")
            @Override
            public void run() {
                WifiDao wifiDao = new WifiDao();
                WifiManager mWifi = (WifiManager) UIUtils.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = mWifi.getConnectionInfo();
                if (info != null) {
                    mSsid = info.getSSID();
//                    if (!TextUtils.isEmpty(mSsid)) {
//                        WifiBean wifi = wifiDao.getWifi(mSsid);
//                        if (wifi != null) {
//                            int product = wifi.product;
//                            searchDeviceByProduct(product);
//                            return;
//                        }
//                    }
                }
//                String cameraIp = getCameraIp();
//                if (Contacts.BASE_IP_WIFI.equals(cameraIp)) {
                    searchDeviceByProduct(ID_Novatek);
//                } else {
//                    searchAllDevice();
//                }
            }
        }).start();


    }

    public void insertWifi2DB(Context context, int product) {
        WifiDao wifiDao = new WifiDao();
        WifiManager mWifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = mWifi.getConnectionInfo();
        if (info != null) {
            if (!TextUtils.isEmpty(info.getSSID()) && !"<unknown ssid>".equals(info.getSSID()) && !"0x".equals(info.getSSID().toLowerCase())) {
                WifiBean wifiBean = new WifiBean();
                wifiBean.BSSID = info.getBSSID();
                String ssid = info.getSSID();
                if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }
                wifiBean.SSID = ssid;
                wifiBean.netId = info.getNetworkId();
                wifiBean.product = product;
                wifiDao.insertWifi(wifiBean);
            }
        }
    }

    public String getCameraIp() {
        WifiManager wifiManager = (WifiManager) VLCApplication.getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

        if (dhcpInfo != null && dhcpInfo.gateway != 0) {
            return intToIp(dhcpInfo.gateway);
        }
        return null;
    }

    public String intToIp(int addr) {
        return ((addr & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "." + (addr >>> 8 & 0xFF));
    }

    private void searchDeviceByProduct(final int product) {
        switch (product) {
            case ID_SUNPLUSE:
                if (curSunplusCamera.prepareSession()) {
                    sendMessage(CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE);
                } else {
                    searchAllDevice();
                }
                break;
            case ID_Novatek:
                if (WifiUtil.checkNetwork(UIUtils.getContext(), 0) || VLCApplication.getInstance().getApisConnect()) {
                    //为了让回调统一到主线程
                    sendMessage(CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE);
                } else {
                    sendMessage(CameraFactoryRtnMsg.RET_NO_DEVICE);
                }
                break;
            case ID_MSTAR:
                String result = CameraCommand.sendRequest(CameraCommand.commandQueryAV1Url());
                if (!TextUtils.isEmpty(result)) {
                    try {
                        String[] lines_temp = result.split("Camera.Preview.RTSP.av=");
                        String[] lines = lines_temp[1].split(System.getProperty("line.separator"));
                        int av = Integer.valueOf(lines[0]);
                        switch (av) {
                            case 1:    // liveRTSP/av1 for RTSP MJPEG+AAC
                                MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_MJPEG_AAC_URL;
                                break;
                            case 2: // liveRTSP/v1 for RTSP H.264
                                MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_URL;
                                break;
                            case 3: // liveRTSP/av2 for RTSP H.264+AAC
                                MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_AAC_URL;
                                break;
                            case 4: // liveRTSP/av4 for RTSP H.264+PCM
                                MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_PCM_URL;
                                break;
                            default:
                                break;
                        }
                        sendMessage(CameraFactoryRtnMsg.RET_HAS_MSTAR);
                    } catch (Exception ignore) {
                        searchAllDevice();
                    }
                } else {
                    searchAllDevice();
                }
                break;
            case ID_HISI:
                String cameraIp = getCameraIp();
                curHisiCamera.setIP(cameraIp);
                Common.DeviceAttr deviceAttr = new Common.DeviceAttr();
                if (Common.FAILURE == curHisiCamera.getDeviceAttr(deviceAttr)) {
                    searchAllDevice();
                } else {
                    mHandler.sendEmptyMessage(CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE);
                }
                break;
            case ID_JIELI:
                searchAllDevice();
                break;
            default:
                break;
        }

    }

    private void searchAllDevice() {
        String cameraIp = getCameraIp();
        curHisiCamera.setIP(cameraIp);
        Common.DeviceAttr deviceAttr = new Common.DeviceAttr();
        if (Common.FAILURE != curHisiCamera.getDeviceAttr(deviceAttr)) {
            mHandler.sendEmptyMessage(CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE);
        } else {
            String result = CameraCommand.sendRequest(CameraCommand.commandQueryAV1Url());
            if (!TextUtils.isEmpty(result)) {
                try {
                    String[] lines_temp = result.split("Camera.Preview.RTSP.av=");
                    String[] lines = lines_temp[1].split(System.getProperty("line.separator"));
                    int av = Integer.valueOf(lines[0]);
                    switch (av) {
                        case 1:    // liveRTSP/av1 for RTSP MJPEG+AAC
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_MJPEG_AAC_URL;
                            break;
                        case 2: // liveRTSP/v1 for RTSP H.264
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_URL;
                            break;
                        case 3: // liveRTSP/av2 for RTSP H.264+AAC
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_AAC_URL;
                            break;
                        case 4: // liveRTSP/av4 for RTSP H.264+PCM
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_PCM_URL;
                            break;
                        default:
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_MJPEG_AAC_URL;
                            break;
                    }
                    sendMessage(CameraFactoryRtnMsg.RET_HAS_MSTAR);
                } catch (Exception ignore) {
                    if (WifiUtil.checkNetwork(UIUtils.getContext(), 0)) {
                        sendMessage(CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE);
                    } else {
                        sendMessage(CameraFactoryRtnMsg.RET_NO_DEVICE);
                    }
                }
            } else if (curSunplusCamera.prepareSession()) {
                curSunplusCamera.getSunplusSession().checkWifiConnection();
                sendMessage(CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE);
            } else if (WifiUtil.checkNetwork(UIUtils.getContext(), 0)) {
                sendMessage(CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE);
//            } else if (mSsid.contains(WIFI_PREFIX) || mSsid.contains(WIFI_PREFIX2)) {
//                sendMessage(CameraFactoryRtnMsg.RET_HAS_JIELI_DEVICE);
            } else {
                sendMessage(CameraFactoryRtnMsg.RET_NO_DEVICE);
            }
        }

    }


    private void sendMessage(int what) {
        mHandler.removeMessages(what);
        mHandler.sendEmptyMessage(what);
    }
}
