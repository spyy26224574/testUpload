package com.adai.camera.hisi.net;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.adai.gkdnavi.utils.LogUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MessageService extends Service {
    public static final String MESSAGE_ACTION = "com.hisilicon.dv.MESSAGE_ACTION";
    private static final String TAG = "MessageService";
    private static final int SERVER_PORT = 5678;

    private ServerSocket mServerSocket = null;
    private volatile boolean bStopService = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        bStopService = false;

        if (!mServerThread.isAlive()) {
            mServerThread.start();
        }

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        bStopService = true;

        try {
            if (null != mServerSocket && !mServerSocket.isClosed()) {
                mServerSocket.close();
            }
            if (null != mServerThread && mServerThread.isAlive()) {
                mServerThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /**
     * 服务主线程
     * Ondestroy 调用mServerSocket.close之后会
     * 发生mServerSocket.accept异常退出
     */
    private final Thread mServerThread = new Thread("messageServerThread") {
        @Override
        public void run() {

            do {
                try {
                    if (null != mServerSocket && !mServerSocket.isClosed()) {
                        mServerSocket.close();
                    }

                    mServerSocket = new ServerSocket(SERVER_PORT);
                    mServerSocket.setReuseAddress(true);

                    while (!bStopService) {
                        Log.d(TAG, "Server begin accept");
                        Socket socket = mServerSocket.accept();
                        socket.setSoTimeout(3000);
                        new RecvThread(MessageService.this, socket).start();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, e.getClass().getSimpleName());
                }

            } while (!bStopService);
        }
    };


    /**
     * 接收数据线程
     */
    private static class RecvThread extends Thread {

        private static final int BUFFER_LENGTH = 512;
        private Context mContext;
        private Socket mSocket;

        public RecvThread(Context context, Socket socket) {
            mContext = context;
            mSocket = socket;
        }

        @Override
        public void run() {

            int total = 0;
            int recvLength = 0;
            byte[] buffer = new byte[BUFFER_LENGTH];

            try {

                InputStream stream = mSocket.getInputStream();

                do {
                    recvLength = stream.read(buffer, total, BUFFER_LENGTH - total);

                    if (recvLength < 0) {
                        break; //返回-1流结束（断开）
                    }

                    total += recvLength;

                } while (true);

                if (total > 0) {
                    String data = new String(buffer, 0, total);
                    LogUtils.e(data);
                    Intent intent = new Intent(MESSAGE_ACTION);
                    intent.putExtra("data", data);
                    mContext.sendBroadcast(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.getClass().getSimpleName());
            } finally {
                //Closing this socket will also close the socket's InputStream and OutputStream.
                try {
                    mSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}