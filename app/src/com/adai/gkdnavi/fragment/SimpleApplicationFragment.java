package com.adai.gkdnavi.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.camera.bean.CameraFactoryRtnMsg;
import com.adai.camera.hisi.preview.HisiPreviewActivity;
import com.adai.camera.mstar.preview.MstarPreviewActivity;
import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.preview.NovatekPanoPreviewActivity;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.camera.sunplus.preview.SunplusPreviewActivity;
import com.adai.gkd.bean.AdvertisementInfoBean;
import com.adai.gkd.bean.LicenseBean;
import com.adai.gkd.bean.request.AdvertisementPagebean;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.CheckSApContactService;
import com.adai.gkdnavi.CheckVersionTask;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.WifiConnectActivity;
import com.adai.gkdnavi.adapter.ImageAdapter;
import com.adai.gkdnavi.utils.AutoPlayGallery;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.adai.gkdnavi.utils.WifiUtil.WifiConnectInfo;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MovieRecord;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Set;

/**
 * Created by huangxy on 2017/7/18 11:03.
 */

public class SimpleApplicationFragment extends BaseFragment implements View.OnClickListener {
    private RelativeLayout reView;
    private WifiManager mWifiManager;
    private static final int MSG_CAMERA_WANT_DELETE = 1;
    private static final int MSG_CAMERA_UNCONNECT = 2;
    private static final int MSG_CAMERA_LICENSE = 3;
    private static final int MSG_CAMERA_LICENSE_SUCCESS = 4;
    private static final int MSG_WIFI_NO_LINK = 5;
    //    private AutoPlayGallery mAutoPlayGallery;
    private CheckVersionTask CheckVersion = null;
    private Thread OTACheckThread;
    SharedPreferences spf_otacontentmain = null;
    private TextView dv_name, tv_connect_other;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    String strCurrentTime = format.format(new java.util.Date());
    private Intent intent_ap;
    private boolean isToCameraPreview;
    private AlertDialog mPermissionNoticeDialog;

    private String TAG = getClass().getSimpleName();
//    boolean isInCheckWifi = false;//为TRUE只判断  为FALSE进入摄像头界面

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CAMERA_WANT_DELETE:
                    isToCameraPreview = true;
                    hidepDialog();
                    showConnectFailedDeleteDialog();
                    break;
                case MSG_CAMERA_UNCONNECT:
                    isToCameraPreview = true;
                    hidepDialog();
                    showConnectCameraDialog();
                    break;
                case MSG_CAMERA_LICENSE:
                    isToCameraPreview = true;
                    hidepDialog();
                    showWantLicenseDialog();
                    break;
                case MSG_CAMERA_LICENSE_SUCCESS:
                    isToCameraPreview = true;
                    hidepDialog();
                    showLicenseSuccessDialog();
                    break;
                case MSG_WIFI_NO_LINK:
                    isToCameraPreview = true;
                    hidepDialog();
                    showWifiNoLinkDialog();
                    break;
                default:
                    break;
            }
        }
    };
    private CameraFactory.CameraCtrlCallback mCameraCtrlCallback = new CameraFactory.CameraCtrlCallback() {
        @Override
        public void handlerCallback(CameraFactoryRtnMsg cameraFactoryRtnMsg) {
            Intent intent = new Intent();
            handler.removeMessages(MSG_CAMERA_UNCONNECT);
            switch (cameraFactoryRtnMsg.ret) {
                case CameraFactoryRtnMsg.RET_NO_DEVICE:
                    handler.removeMessages(MSG_CAMERA_UNCONNECT);
                    handler.sendEmptyMessage(MSG_CAMERA_UNCONNECT);
                    break;
                case CameraFactoryRtnMsg.RET_HAS_NOVATEK_DEVICE:
                    isToCameraPreview = true;
//                    String ssid = SpUtils.getString(mContext, "SSID", "");
//                    boolean isPanoCamera = SpUtils.getBoolean(mContext, "isPanoCamera", false);
//                    Log.e("9527", "isPanoCamera = " + isPanoCamera);
//                    if (isPanoCamera) {
//                    getCameraType();
                    intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
                    startActivity(intent);
//                    if (mWifiManager.getConnectionInfo().getSSID().contains("CarDV_89E5_360")) {
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                    } else {
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPreviewActivity.class);
//                    }
//                    startActivity(intent);
                    break;
                case CameraFactoryRtnMsg.RET_HAS_MSTAR:
                    isToCameraPreview = true;
                    intent.setClass(VLCApplication.getAppContext(), MstarPreviewActivity.class);
                    startActivity(intent);
                    break;
//                case CameraFactoryRtnMsg.RET_HAS_ALLWINNER_DEVICE:
//                    isToCameraPreview = true;
//                    intent.setClass(VLCApplication.getAppContext(), AllWinnerPreviewActivity.class);
//                    startActivity(intent);
//                    break;
//                case CameraFactoryRtnMsg.RET_HAS_GP_DEVICE:
//                    isToCameraPreview = true;
//                    intent.setClass(VLCApplication.getAppContext(), GPPreviewActivity.class);
//                    startActivity(intent);
//                    break;
//                case CameraFactoryRtnMsg.RET_HAS_SHENGMAI_DEVICE:
//                    isToCameraPreview = true;
//                    intent.setClass(VLCApplication.getAppContext(), ShengmaiPreviewActivity.class);
//                    startActivity(intent);
//                    isToCameraPreview = true;
//                    break;
                case CameraFactoryRtnMsg.RET_HAS_SUNPLUS_DEVICE:
                    isToCameraPreview = true;
                    intent.setClass(VLCApplication.getAppContext(), SunplusPreviewActivity.class);
                    startActivity(intent);
//                    isToCameraPreview = true;
                    break;
                case CameraFactoryRtnMsg.RET_HAS_HISI_DEVICE:
                    isToCameraPreview = true;
                    intent.setClass(VLCApplication.getAppContext(), HisiPreviewActivity.class);
                    startActivity(intent);
                    break;
//                case CameraFactoryRtnMsg.RET_HAS_JIELI_DEVICE:
//                    isToCameraPreview = true;
//                    initjieli();
//                    break;
                default:
                    break;
            }
            hidepDialog();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_simple_application, container, false);
        reView = (RelativeLayout) rootView.findViewById(R.id.review);
        reView.setOnClickListener(this);
        dv_name = (TextView) rootView.findViewById(R.id.dv_name);
//        mAutoPlayGallery = (AutoPlayGallery) rootView.findViewById(R.id.gallery);
        tv_connect_other = (TextView) rootView.findViewById(R.id.tv_connect_other);
        tv_connect_other.setOnClickListener(this);
//        setWifiBroadcast();
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        String ssid = SpUtils.getString(mContext, "SSID", "");
        LogUtils.e("onResume ssid =" + ssid);
        if (!TextUtils.isEmpty(ssid)) {
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            dv_name.setText(ssid);
        } else {
            dv_name.setText("");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        setpDialogCancelable(true);
//        initAdvertisement();
        CameraUtils.changContactsVariable(0);
//        CameraFactory.getInstance().initCamera(mContext);

        CheckVersion = new CheckVersionTask(mContext);
        // check ota msg
        OTACheckThread = new Thread(checkOtaRunnable);
        OTACheckThread.start();
        setpDialogDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isToCameraPreview) {
                    isToCameraPreview = false;
                    return;
                }
                WifiUtil.getInstance().removeTimeOutRunnable();
                WifiUtil.getInstance().unRegisterWifiCast();
                WifiUtil.getInstance().breakWifiThread();
//                WifiUtil.getInstance().checkAvailableNetwork(mContext);
                showConnectCameraDialog();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        isToCameraPreview = false;
    }

    public void setSharedPreference() {
        SharedPreferences.Editor spf_editor = spf_otacontentmain.edit();
        // 存放数据
        spf_editor.putString("gspLastTime", strCurrentTime);
        spf_editor.apply();
    }

    private Runnable checkOtaRunnable = new Runnable() {

        @Override
        public void run() {

            // 得到本地版本号
            PackageManager APKmanager;
            PackageInfo PackInfo = null;
            String strLocalVer = null;
            APKmanager = mContext.getPackageManager();
            try {
                PackInfo = APKmanager.getPackageInfo(
                        mContext.getPackageName(), 0);
                strLocalVer = PackInfo.versionName;// 版本号
                // 将本地版本号存储
                spf_otacontentmain = mContext.getSharedPreferences("gspOta",
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor contenteoneditor = spf_otacontentmain
                        .edit();
                contenteoneditor.putString("gspLocalVerNo", strLocalVer);
                contenteoneditor.apply();
            } catch (PackageManager.NameNotFoundException e1) {

                e1.printStackTrace();
            }

            // 如果配置里面是空的 就存储值 且执行操作
            // 获取ss文件中User对应的数据，注意第二个参数，若此键值对中暂时没有数值，则默认返回第二个参数的值
            String strLastTime = spf_otacontentmain
                    .getString("gspLastTime", "");
            if ("".equals(strLastTime)) {
                setSharedPreference();
                CheckVersion.run();
            }
            try {
                // 当前时间，和存储的时间 如果相差一天 就重新存储值
                int iDifDate = daysBetween(strCurrentTime, strLastTime);
                if (iDifDate != 0) {
                    // removeSharedPreference();
                    setSharedPreference();
                    CheckVersion.run();
                }
            } catch (ParseException e) {

                e.printStackTrace();
            }

        }
    };

    // 字符串的日期格式的计算
    public static int daysBetween(String smdate, String bdate)
            throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(sdf.parse(smdate));
        long time1 = cal.getTimeInMillis();
        cal.setTime(sdf.parse(bdate));
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

//    private ImageAdapter advAdapter = null;
//
//    private void initAdvertisement() {
//        advAdapter = new ImageAdapter(mContext);
//        mAutoPlayGallery.setAdapter(advAdapter);
//        for (int i = 0; i < 3; i++) {
//            getAdv(i);
//        }
//    }
//
//    private void getAdv(final int index) {
//        AdvertisementInfoBean adv = readAdv(index);
//        if (adv != null && advAdapter != null) {
//            advAdapter.addAdv(adv, index);
//        }
//        RequestMethods.advertisementGet(2, index, new HttpUtil.Callback<AdvertisementPagebean>() {
//            @Override
//            public void onCallback(AdvertisementPagebean result) {
//                if (result != null) {
//                    if (result.data != null && result.data.size() > 0) {
//                        AdvertisementInfoBean adv = result.data.get(0);
//                        saveAdv(index, adv);
//                        if (advAdapter != null) {
//                            advAdapter.addAdv(adv, index);
//                        }
//                    }
//                }
//            }
//        });
//    }
//
//    private AdvertisementInfoBean readAdv(int index) {
//        FileInputStream is = null;
//        ObjectInputStream ois = null;
//        try {
//            is = mContext.openFileInput("advdata" + index);
//            ois = new ObjectInputStream(is);
//            return (AdvertisementInfoBean) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (ois != null) {
//                try {
//                    ois.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }
//
//    private void saveAdv(int index, AdvertisementInfoBean adv) {
//        FileOutputStream fos = null;
//        ObjectOutputStream oos = null;
//        try {
//            fos = mContext.openFileOutput("advdata" + index, Context.MODE_PRIVATE);
//            oos = new ObjectOutputStream(fos);
//            oos.writeObject(adv);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fos != null) {
//                try {
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (oos != null) {
//                try {
//                    oos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    @Override
    public void onStart() {
        super.onStart();
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_connect_other:
                startActivity(new Intent(mContext, WifiConnectActivity.class));
                break;
            case R.id.review:
                if (VoiceManager.isCameraBusy) {
                    showToast(R.string.camera_isbusy);
                    return;
                }

                showpDialog(R.string.wifi_waitdownload);
                String curSSID = mWifiManager.getConnectionInfo().getSSID();//当前连接的wifi
                if (curSSID.startsWith("\"") && curSSID.endsWith("\"")) {
                    curSSID = curSSID.substring(1, curSSID.length() - 1);
                }

                String curBSSID = mWifiManager.getConnectionInfo().getBSSID();//当前连接的wifi MAC
                if (curBSSID != null && curBSSID.startsWith("\"") && curBSSID.endsWith("\"")) {
                    curBSSID = curBSSID.substring(1, curBSSID.length() - 1);
                }
                Log.e(TAG, "curSSID = " + curSSID + ",curBSSID = " + curBSSID);

                int networkId = mWifiManager.getConnectionInfo().getNetworkId();
                String ssid = SpUtils.getString(mContext, "SSID", "");
                boolean isWifiConnected = isWifiConnected(mContext.getApplicationContext());
                Log.e(TAG, "networkId = " + networkId + ",ssid = " + ssid + ",isWifiConnected = " + isWifiConnected);
//                String bssid = SpUtils.getString(mContext, "BSSID", "");

                if (TextUtils.isEmpty(ssid)) {
                    Set<String> set = SpUtils.getHashSet(mContext, "license");
                    if (set != null && set.contains(curBSSID)) {//已激活设备 直接开始连接
                        showpDialog(R.string.connecting_camera);
                        CameraFactory.getInstance().setCameraCtrlCallback(mCameraCtrlCallback);
                        CameraFactory.getInstance().sendSearchDevice();
                    } else {
                        hidepDialog();
                    }

                } else {
                    showpDialog(R.string.connecting_camera);

                    if (ssid.equals(curSSID) && networkId != -1) {//当前连接即为所需WIFI
                        CameraFactory.getInstance().setCameraCtrlCallback(mCameraCtrlCallback);
                        CameraFactory.getInstance().sendSearchDevice();
                    } else {//开始连接已经保存的WIFI
                        connectLastDevice();
                    }

//                    if (ssid.startsWith("LSX_G9_") || ssid.startsWith("360-L9_")) {//全景摄像头
//                        new LicenseThread(ssid, curSSID, bssid, networkId).start();
//                    } else {//非全景摄像头
//                        if (ssid.equals(curSSID) && networkId != -1) {
//                            CameraFactory.getInstance().setCameraCtrlCallback(mCameraCtrlCallback);
//                            CameraFactory.getInstance().sendSearchDevice();
//                        } else {
//                            connectLastDevice();
//                        }
//                    }
                }
                break;
            default:
                break;
        }
    }

    private void connectLastDevice() {
        WifiUtil.getInstance().setConnectInfo(new WifiConnectInfo() {
            @Override
            public void onScaning() {

            }

            @Override
            public void onConnecting() {
                LogUtils.e("onConnecting()");
            }

            @Override
            public void onConnected() {
                LogUtils.e("onConnected");
//                handler.sendEmptyMessage(MSG_CAMERA_CONNECTED_TO_CAMERA);
                CameraFactory.getInstance().setCameraCtrlCallback(mCameraCtrlCallback);
                CameraFactory.getInstance().sendSearchDevice();
            }

            @Override
            public void onConnectedFailed() {
                LogUtils.e("onConnectedFailed");
                handler.removeMessages(MSG_CAMERA_UNCONNECT);
                handler.sendEmptyMessage(MSG_CAMERA_UNCONNECT);
                hidepDialog();
            }

            @Override
            public void onNotfound() {
                LogUtils.e("onNotfound");
                hidepDialog();
                handler.removeMessages(MSG_CAMERA_UNCONNECT);
                handler.sendEmptyMessage(MSG_CAMERA_UNCONNECT);
            }

            @Override
            public void onError() {
                LogUtils.e("onError");
                handler.removeMessages(MSG_CAMERA_UNCONNECT);
                handler.sendEmptyMessage(MSG_CAMERA_UNCONNECT);
                hidepDialog();
            }

            @Override
            public void onErrorWantDelete() {
                LogUtils.e("onNotfound");
                handler.removeMessages(MSG_CAMERA_UNCONNECT);
                handler.sendEmptyMessage(MSG_CAMERA_WANT_DELETE);
                hidepDialog();

            }
        });
        WifiUtil.getInstance().connectWIfi(mContext);
    }

    private void showConnectCameraDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.wifi_checkmessage));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(mContext, WifiConnectActivity.class));
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }

    private void showWantLicenseDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.wifi_want_license));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }

    private void showLicenseSuccessDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.wifi_license_success));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }

    private void showWifiNoLinkDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.wifi_no_link));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }


    private void getLicense(String mac) {
        String package_name = getActivity().getApplication().getPackageName();
        RequestMethods.getLicense(mac, package_name, new HttpUtil.Callback<LicenseBean>() {
            @Override
            public void onCallback(LicenseBean result) {
                if (result != null && result.data != null && result.data.statusCode.equals("101")) {//激活成功 提示连接已经激活的摄像头WiFi
                    String wantLicenseMac = SpUtils.getString(mContext, "wantLicenseMac", "");
                    SpUtils.installHashSet(mContext, "license", wantLicenseMac);
                    SpUtils.putString(mContext, "wantLicenseMac", "");
                    handler.sendEmptyMessage(MSG_CAMERA_LICENSE_SUCCESS);
                } else {//激活失败
                    handler.sendEmptyMessage(MSG_CAMERA_LICENSE);
                }
            }
        });
    }

    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetworkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void showConnectFailedDeleteDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        builder.setMessage(getString(R.string.delete_system_wifi));
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                WifiUtil.getInstance().gotoWifiSetting(getActivity());
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }


//    private void getCameraType() {
//        final Intent intent = new Intent();
//        CameraUtils.sendCmd(NovatekWifiCommands.CAMERA_GET_NEW_VERSION, "", new CameraUtils.CmdCallback() {//获取摄像头信息
//            @Override
//            public void success(int commandId, String par, MovieRecord movieRecord) {
//                String cameraVersionCurrent = movieRecord.getString();
//                if (!TextUtils.isEmpty(cameraVersionCurrent)) {
//                    String[] cameraInfos = cameraVersionCurrent.split(";");
//                    if (cameraInfos.length == 8 && cameraInfos[4].equals("100")) {
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                        startActivity(intent);
//                    } else {
//                        intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                        startActivity(intent);
//                    }
//                } else {
//                    intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
//                    startActivity(intent);
//
//                }
//
//            }
//
//            @Override
//            public void failed(int commandId, String par, String error) {
//                intent.setClass(VLCApplication.getAppContext(), NovatekPanoPreviewActivity.class);
////                非全景摄像头
//            }
//        });
//    }
}
