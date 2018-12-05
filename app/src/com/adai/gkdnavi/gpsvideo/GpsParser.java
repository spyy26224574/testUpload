package com.adai.gkdnavi.gpsvideo;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.adai.gkdnavi.utils.LogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import SunGps.IHttpReadCallback;
import SunGps.SunGpsInterface;


/**
 * Created by admin on 2016/11/22.
 */

public class GpsParser {
    public static final String TAG = "GpsParser";
    private int mEncrypttype;
//    private IHttpReadCallback mHttpReadCallback = new IHttpReadCallback() {
//        @Override
//        public long onRead(byte[] buffer, long pos, int len, int id) {
//            LogUtils.e("buffer.length = " + buffer.length + ",pos = " + pos + ",len = " + len
//                    + ",id = " + id);
//            byte[] bytes = {0x30, 0x31, 0x33, 0x34};
//            System.arraycopy(bytes, 0, buffer, 0, bytes.length);
//            return 0;
//        }
//    };

    private class GpsTagInfo {
        public int locaton;
        public int length;

        public GpsTagInfo(int location, int length) {
            this.locaton = location;
            this.length = length;
        }
    }

    public GpsInfoCallback getCallback() {
        return callback;
    }

    public void setCallback(GpsInfoCallback callback) {
        this.callback = callback;
    }

    public interface GpsInfoCallback {
        void onGpsInfo(List<GpsInfoBean> gpsInfo, int encryptType);
    }

    private GpsInfoCallback callback;
    //    private List<LatLng> gps_infos;
    private List<GpsInfoBean> mGpsInfos;

    public void parseFile(String url) {
        if (TextUtils.isEmpty(url)) {
            handler.sendEmptyMessage(0);
            return;
        }

        if (url.startsWith("http://") || url.startsWith("https://")) {
            parseHttpFile(url);
        } else {
            parseLocalFile(url);
        }
    }

    private void parseLocalFile(final String url) {
        Log.e("9997", "url = " + url);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SunGpsInterface sunGpsInterface = new SunGpsInterface();
                sunGpsInterface.sunGpsInit();
                String gpsData = sunGpsInterface.sunGpsDecode(url, 0);
                if (!TextUtils.isEmpty(gpsData)) {
                    LogUtils.e("gpsData = " + gpsData);
                    String[] gpsDataArray = gpsData.split(";");
                    if (gpsDataArray.length > 0) {
                        mGpsInfos = new ArrayList<>();
                        for (String gpsItem : gpsDataArray) {
                            parseGpsItem(gpsItem);
                        }
                    }
                } else {
                    LogUtils.e("没有gps数据");
                }
                sunGpsInterface.sunGpsExit();
                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (callback != null) {
                switch (msg.what) {
                    case 0:
                        callback.onGpsInfo(mGpsInfos, mEncrypttype);
                        break;
                }
            }
        }
    };

    private void parseHttpFile(final String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final URL url_ = new URL(url);
                    final HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
                    conn.setRequestMethod("GET");
                    final int contentLength = conn.getContentLength();
                    conn.disconnect();
                    SunGpsInterface sunGpsInterface = new SunGpsInterface();
                    sunGpsInterface.sunGpsInit();
                    sunGpsInterface.sunGpsReadCallBack(new IHttpReadCallback() {
                        @Override
                        public long onRead(byte[] buffer, long pos, int len, int id) {
                            LogUtils.e("pos = " + pos + ",len = " + len + ",id = " + id);
                            if (id == 0) {
                                LogUtils.e("contentLength = " + contentLength);
                                return contentLength;
                            } else if (id == 1) {
                                LogUtils.e("id = 1");
                                try {
                                    HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
                                    conn.setRequestMethod("GET");
                                    conn.setRequestProperty("RANGE", "bytes=" + pos + "-" + (pos + len - 1));
                                    conn.connect();
                                    InputStream inputStream = conn.getInputStream();
                                    byte[] data = new byte[len];
                                    int length = -1;
                                    int readedLength = 0;
                                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(conn.getContentLength());
                                    while ((length = inputStream.read(data)) != -1 && readedLength < len) {
                                        LogUtils.e("length = " + length);
                                        byteArrayOutputStream.write(data, 0, length);
                                        readedLength += length;
                                    }
                                    byte[] src = byteArrayOutputStream.toByteArray();
                                    LogUtils.e(Arrays.toString(src));
                                    LogUtils.e("src.length = " + src.length);
                                    conn.disconnect();
//                                    if (readedLength > len) {
//                                        System.arraycopy(src, 0, buffer, 0, len);
//                                        return len;
//                                    } else {
                                    System.arraycopy(src, 0, buffer, 0, src.length);
//                                        return src.length;
//                                    }
                                    return src.length;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    return 0;
                                }
                            }
                            return 0;
                        }
                    });
//                    sunGpsInterface.sunGpsReadCallBack(mHttpReadCallback);
                    String gpsData = sunGpsInterface.sunGpsDecode(url, 1);
                    LogUtils.e("gpaData = " + gpsData);
                    if (!TextUtils.isEmpty(gpsData)) {
                        LogUtils.e("gpsData = " + gpsData);
                        String[] gpsDataArray = gpsData.split(";");
                        if (gpsDataArray.length > 0) {
                            mGpsInfos = new ArrayList<>();
                            for (String gpsItem : gpsDataArray) {
                                parseGpsItem(gpsItem);
                            }
                        }
                    } else {
                        LogUtils.e("没有gps数据");
                    }
                    sunGpsInterface.sunGpsExit();

                } catch (IOException e) {
                    e.printStackTrace();
                }
//                recursionParseHttp(url, 0, 0);

                handler.sendEmptyMessage(0);
            }
        }).start();

    }

//    private void recursionParseHttp(String url, long skip, long totallen) {
//        if (totallen != 0 && skip >= totallen) return;
//        InputStream is = null;
//        try {
//            URL url_ = new URL(url);
//            HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
//            // 设置 User-Agent
////            conn.setRequestProperty("User-Agent", "NetFox");
//            conn.setRequestMethod("GET");
//            System.out.print("skip=" + skip + "\n");
//            if (skip > 0 && totallen > 0)
//                conn.setRequestProperty("RANGE", "bytes=" + skip + "-" + totallen);
//            conn.connect();
//
//            long len = totallen;
//            if (len <= 0) len = conn.getContentLength();
//            is = conn.getInputStream();
//            byte[] buffer = new byte[8];
//            if (is.read(buffer) > 0) {
//                int size = (buffer[0] << 24 & 0xffffffff) + (buffer[1] << 16 & 0xffffff) + (buffer[2] << 8 & 0xffff) + (buffer[3] & 0xff);
//                if (size <= buffer.length) {
//                    return;
//                }
//                byte[] typeb = new byte[4];
//                System.arraycopy(buffer, 4, typeb, 0, typeb.length);
//                String type = new String(typeb, "UTF-8");
//                System.out.println("size=" + size + ",type=" + type);
//                if ("skip".equals(type)) {
//                    parseHttpGps(is, size);
//                    conn.disconnect();
//                } else if ("moov".equals(type)) {
//                    conn.disconnect();
//                    recursionParseHttp(url, skip + buffer.length, len);
//                } else if ("gps ".equals(type)) {
//                    parseHttpTag(is, url);
//                    conn.disconnect();
//                } else {
//                    conn.disconnect();
//                    recursionParseHttp(url, skip + size, len);
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

//    private void parseHttpTag(InputStream is, String url) {
//        try {
//            byte[] buffer = new byte[8];
//            int version = 0;
//            int dataCount = 0;
//            if (is.read(buffer) > 0) {
//                version = (buffer[0] << 24) | (buffer[1] << 16 & 0xffffff) | (buffer[2] << 8 & 0xffff) | (buffer[3] & 0xff);
//                dataCount = (buffer[4] << 24) | (buffer[5] << 16 & 0xffffff) | (buffer[6] << 8 & 0xffff) | (buffer[7] & 0xff);
//            }
//            if (dataCount > 0) {
//                mGpsInfos = new ArrayList<>();
//                ArrayList<GpsTagInfo> gpsTagInfos = new ArrayList<>();
//                for (int i = 0; i < dataCount; i++) {
//                    if (is.read(buffer) > 0) {
//                        int location = (buffer[0] << 24) | (buffer[1] << 16 & 0xffffff) | (buffer[2] << 8 & 0xffff) | (buffer[3] & 0xff);
//                        int length = (buffer[4] << 24) | (buffer[5] << 16 & 0xffffff) | (buffer[6] << 8 & 0xffff) | (buffer[7] & 0xff);
//                        gpsTagInfos.add(new GpsTagInfo(location, length));
//                    } else {
//                        break;
//                    }
//                }
//                is.close();
//                for (GpsTagInfo gpsTagInfo : gpsTagInfos) {
//                    if (gpsTagInfo.length < 152) {//其中16个字节header，4个字节个数，132个字节gps数据
//                        continue;
//                    }
//                    URL url_ = new URL(url);
//                    HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
//                    conn.setRequestMethod("GET");
//                    conn.setRequestProperty("RANGE", "bytes=" + (gpsTagInfo.locaton + 16) + "-" + (gpsTagInfo.locaton + gpsTagInfo.length));
//                    conn.connect();
//                    parseHttpGps(conn.getInputStream(), gpsTagInfo.length);
//                    conn.disconnect();
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void parseHttpGps(InputStream is, long length) {
//        if (length < 152) {
//            return;
//        }
//        try {
////            byte[] skipheader = new byte[16];
////            int encrypttype = 0;
////            if (is.read(skipheader) > 0) {
////                encrypttype = skipheader[15];
////                mEncrypttype = encrypttype;
////                String gpsinfo = new String(skipheader);
////            }
//            byte[] skipheader = new byte[16];
//            mEncrypttype = 0;
//            for (; ; ) {
//                int read = is.read(skipheader);
//                if (length >= 152 && read > 0) {
//                    String tag = new String(skipheader, "UTF-8");
//                    if (tag.contains("LIGOGPSINFO")) {
//                        mEncrypttype = skipheader[15];
//                        break;
//                    }
//                } else {
//                    break;
//                }
//                length -= read;
//            }
//            byte[] numbuffer = new byte[4];
//            int num = 0;
//            mGpsInfos = new ArrayList<>();
//            if (is.read(numbuffer) > 0) {
//                num = (numbuffer[3] << 24 & 0xffffffff) + (numbuffer[2] << 16 & 0xffffff) + (numbuffer[1] << 8 & 0xffff) + (numbuffer[0] & 0xff);
//            }
//            if (mEncrypttype == 0) {
//                byte[] framebuffer = new byte[4];
//                byte[] itembuffer = new byte[128];
//                while (num > 0) {
//                    if (is.read(framebuffer) > 0) {
//                        int frame = (framebuffer[3] << 24 & 0xffffffff) + (framebuffer[2] << 16 & 0xffffff) + (framebuffer[1] << 8 & 0xffff) + (framebuffer[0] & 0xff);
//                    }
//                    if (is.read(itembuffer) > 0) {
//                        String item = new String(itembuffer, "UTF-8");
//                        parseGpsItem(item);
//                    }
//                    num--;
//                }
//            } else if (mEncrypttype == 1 || mEncrypttype == 2) {
//                byte[] itemdata = new byte[132];
//                SunGpsInterface gpsInterface = new SunGpsInterface();
//                gpsInterface.SunSetEncType(mEncrypttype);
//                while (num > 0) {
//                    if (is.read(itemdata) > 0) {
//                        //                                for(int i=0;i<itemdata.length;i++){
//                        //                                    Log.e("itemdata",String.format("%x",itemdata[i]));
//                        //                                }
//                        byte[] decryptdata = new byte[132];
//                        gpsInterface.DecryptDataBlock(itemdata, decryptdata);
//                        byte[] framebuffer = new byte[4];
//                        byte[] itembuffer = new byte[128];
//                        System.arraycopy(decryptdata, 0, framebuffer, 0, framebuffer.length);
//                        System.arraycopy(decryptdata, framebuffer.length, itembuffer, 0, itembuffer.length);
//                        String item = new String(itembuffer, "UTF-8");
//                        try {
//                            parseGpsItem(item);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    num--;
//                }
//            }
//            is.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    private void recursionParse(RandomAccessFile raf) throws IOException {
//        byte[] buffer = new byte[8];
//        if (raf.read(buffer) > 0) {
//            int size = (buffer[0] << 24) + (buffer[1] << 16 & 0xffffff) + (buffer[2] << 8 & 0xffff) + (buffer[3] & 0xff);
//            if (size <= buffer.length) {
//                return;
//            }
//            byte[] typeb = new byte[4];
//            System.arraycopy(buffer, 4, typeb, 0, typeb.length);
//            String type = new String(typeb, "UTF-8");
//            if ("moov".equals(type)) {
//                recursionParse(raf);
//            } else if ("gps ".equals(type)) {
//                parseGpsTag(raf);
//            } else if ("skip".equals(type)) {
//                mGpsInfos = new ArrayList<>();
//                parseGps(raf, size);
//            } else {
//                int skipSize = size - buffer.length;
//                long filePointer = raf.getFilePointer();
//                raf.seek(filePointer + skipSize);
//                recursionParse(raf);
//            }
//        }
//    }

//    private void parseGpsTag(RandomAccessFile raf) throws IOException {
//        byte[] buffer = new byte[8];
//        int version = 0;
//        int dataCount = 0;
//        if (raf.read(buffer) > 0) {
//            version = (buffer[0] << 24) | (buffer[1] << 16 & 0xffffff) | (buffer[2] << 8 & 0xffff) | (buffer[3] & 0xff);
//            dataCount = (buffer[4] << 24) | (buffer[5] << 16 & 0xffffff) | (buffer[6] << 8 & 0xffff) | (buffer[7] & 0xff);
//        }
//        if (dataCount > 0) {
//            mGpsInfos = new ArrayList<>();
//            ArrayList<GpsTagInfo> gpsTagInfos = new ArrayList<>();
//            for (int i = 0; i < dataCount; i++) {
//                if (raf.read(buffer) > 0) {
//                    int location = (buffer[0] << 24) | (buffer[1] << 16 & 0xffffff) | (buffer[2] << 8 & 0xffff) | (buffer[3] & 0xff);
//                    int length = (buffer[4] << 24) | (buffer[5] << 16 & 0xffffff) | (buffer[6] << 8 & 0xffff) | (buffer[7] & 0xff);
//                    gpsTagInfos.add(new GpsTagInfo(location, length));
//                } else {
//                    break;
//                }
//            }
//            for (GpsTagInfo gpsTagInfo : gpsTagInfos) {
//                raf.seek(gpsTagInfo.locaton);
//                parseGps(raf, gpsTagInfo.length);
//            }
//        }
//    }

//    private void parseGps(RandomAccessFile raf, long length) throws IOException {
////        byte[] temp = new byte[16];
////        for (int i = 0; i < 7; i++) {
////            if (raf.read(temp) > 0) {
////                String tag = new String(temp, "UTF-8");
////                if (tag.contains("LIGOGPSINFO")) {
////                    break;
////                }
////            }
////        }
//        if (length < 152) {
//            return;
//        }
//        byte[] skipheader = new byte[16];
//        mEncrypttype = 0;
//        for (; ; ) {
//            int read = raf.read(skipheader);
//            if (length >= 152 && read > 0) {
//                String tag = new String(skipheader, "UTF-8");
//                if (tag.contains("LIGOGPSINFO")) {
//                    mEncrypttype = skipheader[15];
//                    break;
//                }
//            } else {
//                break;
//            }
//            length -= read;
//        }
////        if (raf.read(skipheader) > 0) {
////            String gpsinfo = new String(skipheader);
////        }
//        byte[] numbuffer = new byte[4];
//        int num = 0;
//        if (raf.read(numbuffer) > 0) {
//            num = (numbuffer[3] << 24) + (numbuffer[2] << 16 & 0xffffff) + (numbuffer[1] << 8 & 0xffff) + (numbuffer[0] & 0xff);
//        }
//        if (mEncrypttype == 0) {
//            byte[] framebuffer = new byte[4];
//            byte[] itembuffer = new byte[128];
//            while (num > 0) {
//                if (raf.read(framebuffer) > 0) {
//                    int frame = (framebuffer[3] << 24) + (framebuffer[2] << 16 & 0xffffff) + (framebuffer[1] << 8 & 0xffff) + (framebuffer[0] & 0xff);
//                }
//                if (raf.read(itembuffer) > 0) {
//                    String item = new String(itembuffer, "UTF-8");
//                    parseGpsItem(item);
//                }
//                num--;
//            }
//        } else if (mEncrypttype == 1 || mEncrypttype == 2) {
//            byte[] itemdata = new byte[132];
//            SunGpsInterface gpsInterface = new SunGpsInterface();
//            gpsInterface.SunSetEncType(1);
//            while (num > 0) {
//                if (raf.read(itemdata) > 0) {
//                    byte[] decryptdata = new byte[132];
//                    gpsInterface.DecryptDataBlock(itemdata, decryptdata);
//                    byte[] framebuffer = new byte[4];
//                    byte[] itembuffer = new byte[128];
//                    System.arraycopy(decryptdata, 0, framebuffer, 0, framebuffer.length);
//                    System.arraycopy(decryptdata, framebuffer.length, itembuffer, 0, itembuffer.length);
//                    String item = new String(itembuffer, "UTF-8");
//                    System.out.println("item=" + item + ",num=" + num);
//                    parseGpsItem(item);
//                }
//                num--;
//            }
//        }
//    }

    private void parseGpsItem(String item) {
        LogUtils.e(item);
        //0 2014/12/07  10:42:24 N:25.252378  E:114.223511 27.75 km/h x:+0.188 y:+0.188 z:+1.000
        String tempString = item.replace("  ", " ");
        String[] strs = tempString.split(" ");
        if (strs.length == 10) {
            String latstr = strs[3];
            String lngstr = strs[4];
            try {
                double lat = Double.parseDouble(latstr.substring(2));
                double lng = Double.parseDouble(lngstr.substring(2));
//                latLng = new LatLng(lat, lng);
//                gps_infos.add(latLng);
                GpsInfoBean gpsInfoBean = new GpsInfoBean();
                gpsInfoBean.time = strs[1] + " " + strs[2];
//                gpsInfoBean.latLng = latLng;
                gpsInfoBean.latitude = latstr.startsWith("N") ? lat : (-1 * lat);
                gpsInfoBean.longitude = lngstr.startsWith("E") ? lng : (-1 * lng);
                try {
                    gpsInfoBean.speed = Float.parseFloat(strs[5]);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                gpsInfoBean.x = Float.parseFloat(strs[7].substring(2));
                gpsInfoBean.y = Float.parseFloat(strs[8].substring(2));
                gpsInfoBean.z = Float.parseFloat(strs[9].substring(2));
                mGpsInfos.add(gpsInfoBean);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }
}
