package com.adai.gkd.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.adai.camera.bean.WifiBean;
import com.example.ipcamera.application.VLCApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/10 11:07
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class WifiDao {
    public static final String TABLE_NAME = "wifis";
    public static final String ID = "id";
    public static final String BSSID = "bssid";
    public static final String SSID = "ssid";
    public static final String NETID = "netid";
    public static final String TIME = "time";
    public static final String PRODUCT = "product";
    public static final String PWD = "pwd";
    public static final String ENCRYPT = "encrypt";
    private GkdSqlHelper dbHelper;

    public WifiDao() {
        dbHelper = GkdSqlHelper.getInstance(VLCApplication.getInstance().getApplicationContext());
    }

    synchronized public List<WifiBean> getAllWifi() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        ArrayList<WifiBean> wifis = null;
        if (db.isOpen()) {
            Cursor cursor = db.query(WifiDao.TABLE_NAME, null, null, null, null, null, WifiDao.TIME + " desc");
            wifis = new ArrayList<>();
            while (cursor.moveToNext()) {
                WifiBean wifiBean = new WifiBean();
                wifiBean.BSSID = cursor.getString(cursor.getColumnIndex(WifiDao.BSSID));
                wifiBean.SSID = cursor.getString(cursor.getColumnIndex(WifiDao.SSID));
                wifiBean.netId = cursor.getInt(cursor.getColumnIndex(WifiDao.NETID));
                wifiBean.product = cursor.getInt(cursor.getColumnIndex(WifiDao.PRODUCT));
                wifiBean.pwd = cursor.getString(cursor.getColumnIndex(WifiDao.PWD));
                wifiBean.encrypt = cursor.getString(cursor.getColumnIndex(WifiDao.ENCRYPT));
                wifis.add(wifiBean);
            }
            cursor.close();
        }
        return wifis;
    }

    synchronized public int deleteWifi(String ssid) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        return db.delete(WifiDao.TABLE_NAME, WifiDao.SSID + "=?", new String[]{ssid});
    }

    /**
     * 插入wifi信息，如果已经有了将会更新
     *
     * @param wifiBean
     * @return
     */
    synchronized public long insertWifi(WifiBean wifiBean) {
        if (wifiBean.SSID.startsWith("\"") && wifiBean.SSID.endsWith("\"")) {
            wifiBean.SSID = wifiBean.SSID.substring(1, wifiBean.SSID.length() - 1);
        }
        WifiBean wifi = getWifi(wifiBean.SSID);
        if (wifi == null) {
            //数据库中没有就插入
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(WifiDao.BSSID, wifiBean.BSSID);
            values.put(WifiDao.SSID, wifiBean.SSID);
            values.put(WifiDao.NETID, wifiBean.netId);
            values.put(WifiDao.PRODUCT, wifiBean.product);
            values.put(WifiDao.TIME, String.valueOf(System.currentTimeMillis()));
            values.put(WifiDao.PWD, wifiBean.pwd);
            values.put(WifiDao.ENCRYPT, wifiBean.encrypt);
            return db.insert(WifiDao.TABLE_NAME, null, values);
        } else {
            //数据库中有就更新
            return updateWifi(wifiBean);
        }
    }

    /**
     * 更新wifi
     *
     * @param wifiBean
     * @return
     */
    synchronized public long updateWifi(WifiBean wifiBean) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (wifiBean.netId != -1) {
            values.put(WifiDao.NETID, wifiBean.netId);
        }
        if (!TextUtils.isEmpty(wifiBean.BSSID)) {
            values.put(WifiDao.BSSID, wifiBean.BSSID);
        }
        if (wifiBean.product != 0) {
            values.put(WifiDao.PRODUCT, wifiBean.product);
        }
        values.put(WifiDao.TIME, String.valueOf(System.currentTimeMillis()));
        if (!TextUtils.isEmpty(wifiBean.pwd)) {
            values.put(WifiDao.PWD, wifiBean.pwd);
        }
        if (!TextUtils.isEmpty(wifiBean.encrypt)) {
            values.put(WifiDao.ENCRYPT, wifiBean.encrypt);
        }
        return db.update(WifiDao.TABLE_NAME, values, WifiDao.SSID + "=?", new String[]{wifiBean.SSID});
    }

    synchronized public WifiBean getWifi(String ssid) {
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(WifiDao.TABLE_NAME, null, WifiDao.SSID + "=?", new String[]{ssid}, null, null, null);
        WifiBean wifiBean = null;
        while (cursor.moveToNext()) {
            wifiBean = new WifiBean();
            wifiBean.BSSID = cursor.getString(cursor.getColumnIndex(WifiDao.BSSID));
            wifiBean.SSID = cursor.getString(cursor.getColumnIndex(WifiDao.SSID));
            wifiBean.netId = cursor.getInt(cursor.getColumnIndex(WifiDao.NETID));
            wifiBean.product = cursor.getInt(cursor.getColumnIndex(WifiDao.PRODUCT));
            wifiBean.pwd = cursor.getString(cursor.getColumnIndex(WifiDao.PWD));
            int columnIndex = cursor.getColumnIndex(WifiDao.ENCRYPT);
            if (columnIndex != -1) {
                wifiBean.encrypt = cursor.getString(columnIndex);
            }
        }
        cursor.close();
        return wifiBean;
    }
}
