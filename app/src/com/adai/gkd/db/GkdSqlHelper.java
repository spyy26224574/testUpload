package com.adai.gkd.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class GkdSqlHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "gkddb";
    private static final int DB_VERSION = 6;
    private static GkdSqlHelper _instance;

    private static final String MESSAGE_TABLE_CREATE = "CREATE TABLE  IF NOT EXISTS "
            + MessageCenterDao.TB_NAME + " ("
            + MessageCenterDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + MessageCenterDao.COLUMN_NAME_CONTENT + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_DEVICEID + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_TITLE + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_STARTTIME + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_ENDTIME + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_TYPE + " INTEGER,"
            + MessageCenterDao.COLUMN_NAME_ISREAD + " INTEGER,"
            + MessageCenterDao.COLUMN_NAME_URL + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_ERRORCODE + " TEXT,"
            + MessageCenterDao.COLUMN_NAME_CREATETIME + " TEXT);";

    private static final String SINGLEINFO_TABLE_CREATE = "CREATE TABLE  IF NOT EXISTS "
            + SingleInfoDao.TB_NAME + " ("
            + SingleInfoDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SingleInfoDao.COLUMN_NAME_ACCELERATE_COUNT + " INTEGER,"
            + SingleInfoDao.COLUMN_NAME_BATTERY_VOLTAGE + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_DEVICEID + " TEXT,"
            + SingleInfoDao.COLUMN_NAME_DYNAMICAL_FUEL + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_ENGINE_SPEED + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_ERROR_COUNT + " INTEGER,"
            + SingleInfoDao.COLUMN_NAME_FUEL + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_TOTAL_FUEL + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_HAPPEN_TIME + " TEXT,"
            + SingleInfoDao.COLUMN_NAME_SCRAM_COUNT + " INTEGER,"
            + SingleInfoDao.COLUMN_NAME_SPEED + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_TAP + " FLOAT,"
            + SingleInfoDao.COLUMN_NAME_THW + " FLOAT);";

    private static final String SUMMARYINFO_TABLE_CREATE = "CREATE TABLE  IF NOT EXISTS "
            + SummaryInfoDao.TB_NAME + " ("
            + SummaryInfoDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + SummaryInfoDao.COLUMN_NAME_ACCELERATE_count + " INTEGER,"
            + SummaryInfoDao.COLUMN_NAME_DEVICEID + " TEXT,"
            + SummaryInfoDao.COLUMN_NAME_HAPPEN_TIME + " TEXT,"
            + SummaryInfoDao.COLUMN_NAME_HOT_TIME + " FLOAT,"
            + SummaryInfoDao.COLUMN_NAME_IDLING_FUEL + " FLOAT,"
            + SummaryInfoDao.COLUMN_NAME_IDLING_TIME + " FLOAT,"
            + SummaryInfoDao.COLUMN_NAME_MAX_ROTATE_SPEED + " FLOAT,"
            + SummaryInfoDao.COLUMN_NAME_MAX_SPEED + " FLOAT,"
            + SummaryInfoDao.COLUMN_NAME_SCRAM_COUNT + " INTEGER,"
            + SummaryInfoDao.COLUMN_NAME_TRAVEL_TIME + " FLOAT);";

    private static final String ABNORMALINFO_TABLE_CREATE = "CREATE TABLE  IF NOT EXISTS "
            + AbnormalInfoDao.TB_NAME + " ("
            + AbnormalInfoDao.COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + AbnormalInfoDao.COLUMN_NAME_DEVICEID + " TEXT,"
            + AbnormalInfoDao.COLUMN_NAME_HAPPEN_TIME + " TEXT,"
            + AbnormalInfoDao.COLUMN_NAME_ABNORMAL_ID + " INTEGER,"
            + AbnormalInfoDao.COLUMN_NAME_DETAIL + " TEXT);";

    private static final String CREATA_WIFI_TABLE = "CREATE TABLE "
            + WifiDao.TABLE_NAME + " ("
            + WifiDao.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + WifiDao.BSSID + " TEXT, "
            + WifiDao.SSID + " TEXT, "
            + WifiDao.PWD + " TEXT, "
            + WifiDao.TIME + " TEXT, "
            + WifiDao.ENCRYPT + " TEXT, "
            + WifiDao.PRODUCT + " INTEGER,"
            + WifiDao.NETID + " INTEGER);";
    private static final String DROP_WIFI_TABLE = "DROP TABLE IF EXISTS " + WifiDao.TABLE_NAME;

    public static GkdSqlHelper getInstance(Context context) {
        if (_instance == null) {
            _instance = new GkdSqlHelper(context);
        }
        return _instance;
    }

    private GkdSqlHelper(Context context) {
        // TODO Auto-generated constructor stub
        this(context, DB_NAME, null, DB_VERSION);
    }

    private GkdSqlHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL(MESSAGE_TABLE_CREATE);
        db.execSQL(SINGLEINFO_TABLE_CREATE);
        db.execSQL(SUMMARYINFO_TABLE_CREATE);
        db.execSQL(ABNORMALINFO_TABLE_CREATE);
        db.execSQL(CREATA_WIFI_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL(MESSAGE_TABLE_CREATE);
        db.execSQL(SINGLEINFO_TABLE_CREATE);
        db.execSQL(SUMMARYINFO_TABLE_CREATE);
        db.execSQL(ABNORMALINFO_TABLE_CREATE);
        db.execSQL(DROP_WIFI_TABLE);
        db.execSQL(CREATA_WIFI_TABLE);
    }

}
