package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.exception.IchInvalidPasswdException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchPtpInitFailedException;

/**
 * Created by huangxy on 2017/4/5.
 */

public class SunplusSession {
    private static final String TAG = "SunplusSession";
    private static int scanflag;
    private ICatchWificamSession session;
    private String ipAddress;
    private String uid;
    private String username;
    private String password;
    private boolean sessionPrepared = false;

    public SunplusSession(String ipAddress, String uid, String username, String password) {
        this.ipAddress = ipAddress;
        this.username = username;
        this.password = password;
        this.uid = uid;
    }

    public SunplusSession() {
    }

    public boolean prepareSession() {
        ICatchWificamConfig.getInstance().enablePTPIP();

        sessionPrepared = true;
        session = new ICatchWificamSession();
        boolean retValue = false;
        try {
            retValue = session.prepareSession("192.168.1.1", "anonymous", "anonymous@icatchtek.com");
        } catch (IchInvalidPasswdException | IchPtpInitFailedException e) {
            e.printStackTrace();
        }
        if (!retValue) {
            sessionPrepared = false;
        }
        Log.e(TAG, "SDKSession,prepareSession ret=" + retValue);
        return sessionPrepared;
    }

    public boolean prepareSession(String ip) {
        ICatchWificamConfig.getInstance().enablePTPIP();
        sessionPrepared = true;
        session = new ICatchWificamSession();
        boolean retValue = false;
        try {
            retValue = session.prepareSession(ip, "anonymous", "anonymous@icatchtek.com");
        } catch (IchInvalidPasswdException | IchPtpInitFailedException e) {
            e.printStackTrace();
        }
        if (!retValue) {
            sessionPrepared = false;
            Log.e(TAG, "SDKSession,prepareSession fail!");
        }
        return sessionPrepared;
    }

    public boolean prepareSession(String ip, boolean enablePTPIP) {
        if (enablePTPIP) {
            ICatchWificamConfig.getInstance().enablePTPIP();
        } else {
            ICatchWificamConfig.getInstance().disablePTPIP();
        }
        sessionPrepared = true;
        session = new ICatchWificamSession();
        boolean retValue = false;
        try {
            retValue = session.prepareSession(ip, "anonymous", "anonymous@icatchtek.com");
        } catch (IchInvalidPasswdException | IchPtpInitFailedException e) {
            e.printStackTrace();
        }
        if (!retValue) {
            sessionPrepared = false;
            Log.e(TAG, "SDKSession,prepareSession fail!");
        }
        return sessionPrepared;
    }

    public boolean isSessionOK() {
        return sessionPrepared;
    }

    public ICatchWificamSession getSDKSession() {
        return session;
    }

    public boolean checkWifiConnection() {
        boolean retValue = false;
        try {
            retValue = session.checkConnection();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        return retValue;
    }

    public boolean destroySession() {
        Boolean retValue = false;
        try {
            retValue = session.destroySession();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }

        return retValue;
    }

    public static boolean startDeviceScan() {
        boolean tempStartDeviceScanValue = ICatchWificamSession.startDeviceScan();
        if (tempStartDeviceScanValue) {
            scanflag = 1;
        }
        return tempStartDeviceScanValue;
    }

    public static void stopDeviceScan() {
        boolean tempStopDeviceScanValue;
        tempStopDeviceScanValue = scanflag != 1 || ICatchWificamSession.stopDeviceScan();
        scanflag = 0;
        Log.e(TAG, "stopDeviceScan: [Normal] -- SDKSession: End stopDeviceScan,tempStopDeviceScanValue=" + tempStopDeviceScanValue);
    }
}
