package com.adai.gkdnavi;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.adai.gkdnavi.utils.LogUtils;
import com.example.ipcamera.application.VLCApplication;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class CheckSApContactService extends Service {


    private static CheckSApContactService instance = null;

    public static synchronized CheckSApContactService getInstance() {
        if (instance == null) {
            instance = new CheckSApContactService();
        }
        return instance;

    }

    private CheckAPThread thread = new CheckAPThread();
    private final static int OK = 1;
    private final static int ERROR = 2;
    private final static int NEXT = 3;
    public static Boolean isConnect = false;

    private VLCApplication app;

    public static boolean apkisrunning = true; // 是否运行中

    private Handler handler1 = new Handler() {
        public void handleMessage(Message msg) {
            int msg_num = msg.what;
            LogUtils.e("msg.num:"+msg_num);
            switch (msg_num) {
                case OK:
                    Log.e("9527", "OK");
                    isConnect = true;
                    app.setApisConnect(true);
                    break;
                case ERROR:
                    Log.w("9527", "ERROR");
                    isConnect = false;
                    app.setApisConnect(false);
                    break;
                case NEXT:
                    Log.w("9527", "NEXT");
                    break;
                default:
                    break;
            }
        }
    };


    public void setApkisrunning(boolean isrun) {
        apkisrunning = isrun;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("9527", "CheckSApContactService onCreate ");
        apkisrunning = true;
        app = (VLCApplication) getApplication();
        statCheck();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("9527", "CheckSApContactService onStartCommand ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void statCheck() {
        thread.start();
    }

    private class CheckAPThread extends Thread {

        @Override
        public void run() {

            while (apkisrunning) {
                Socket socket = new Socket();
                try {
                    socket.connect(
                            new InetSocketAddress("192.168.43.22", 3333), 5000);
                    handler1.sendEmptyMessage(OK);

                } catch (IOException e) {
                    e.printStackTrace();
                    handler1.sendEmptyMessage(ERROR);
                } finally {
                    //handler1.sendEmptyMessage(NEXT);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    try {
                        CheckAPThread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }

        }

    }
}
