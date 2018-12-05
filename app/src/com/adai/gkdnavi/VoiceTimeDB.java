package com.adai.gkdnavi;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.adai.gkdnavi.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class VoiceTimeDB {
	
	private static MyOpenHelper openHelper;
	private static final String DATABASE_NAME = "gdk.db"; // 数据库名称
	private static final int DATABASE_VERSION = 3; // 数据库版本
	private static final String TABLE_NAME = "TIME";
	private static final String TAG = "VoiceTimeDB";
	private SQLiteDatabase db;
	private List<PhotoDomian> list = new ArrayList<PhotoDomian>();

	private static class MyOpenHelper extends SQLiteOpenHelper {
		public MyOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			String createSql = "CREATE TABLE  IF NOT EXISTS "+ TABLE_NAME + " (_id integer primary key autoincrement,number varchar(20),time varchar(20),milliseconds varchar(20),downloaded varchar(20),deviceid varchar(20))";
			db.execSQL(createSql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 更新
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
//			String sql="alter table "+ TABLE_NAME +" add deviceid varchar(20)";
//			db.execSQL(sql);
			onCreate(db);
		}

		/** 单例模式 **/
		public static synchronized MyOpenHelper getInstance(Context context) {
			if (openHelper == null) {
				openHelper = new MyOpenHelper(context);
			}
			return openHelper;
		}

		/**
			 * 删除数据库
		*/
		public boolean deleteDatabase(Context context) {
				return context.deleteDatabase(DATABASE_NAME);
			}
		}
	
	//构造函数创建数据库帮助类
	public VoiceTimeDB(Context context){
		openHelper = MyOpenHelper.getInstance(context);
	}
		
	public void delTable(Context context) {
		// db = this.openHelper.getWritableDatabase();
		// db.execSQL("DROP TABLE IF EXISTS appsetting");
		openHelper.deleteDatabase(context);
		db.close();
	}
	
	public void saveTime(PhotoDomian photoDomian){
		//Log.e(TAG, "saveTime");
		try {		
			db = openHelper.getWritableDatabase();
			String sql = "INSERT INTO "+ TABLE_NAME + "(number,time,milliseconds,downloaded,deviceid) values (?,?,?,?,?)";	
			Object[] bindArgs = {photoDomian.getNumber(),photoDomian.getTime(),photoDomian.getMilliseconds(),photoDomian.getDownloaded(),photoDomian.getDeviceId()};
			db.execSQL(sql, bindArgs);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				db.close();
			}
	}

	public synchronized List<PhotoDomian> findTime(String deviceid){
		Log.e(TAG, "findTime");
		list.clear();
		String sql = " SELECT * FROM " + TABLE_NAME ;
		db = openHelper.getReadableDatabase();
		Cursor cursor = null;
		if(StringUtils.isEmpty(deviceid)){
			sql+=" where downloaded=? order by time desc";
			cursor=db.rawQuery(sql, new String[]{"true"});
		}else{
			sql+=" where deviceid=? order by time desc";
			cursor=db.rawQuery(sql, new String[]{deviceid});
		}
		while(cursor!=null&&cursor.moveToNext()){
			Log.e(TAG, "findTime+moveToNext");
			PhotoDomian photoDomian = new PhotoDomian();
			photoDomian.setNumber(cursor.getString(1));
			photoDomian.setTime(cursor.getString(2));
			photoDomian.setMilliseconds(cursor.getString(3));
			photoDomian.setDownloaded(cursor.getString(4));
			photoDomian.setDeviceId(cursor.getString(5));
			Log.e(TAG, "list1="+list);
			list.add(photoDomian);
			Log.e(TAG, "list2="+list);
		}
		db.close();
		return list;
	}

	public void deleteTime(PhotoDomian photoDomian){
		db = openHelper.getWritableDatabase();
		String delete = photoDomian.getTime();
		String sql = "DELETE FROM " + TABLE_NAME + " where time = "  + "'" + delete + "'";
		//Object[] bindArgs = { address.getName(),address.getAddress() };
		db.execSQL(sql);
		db.close();
	}
	
	public int findMaxNumber(){
		String sql = " SELECT * FROM " + TABLE_NAME;
		db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		boolean moveToLast = cursor.moveToLast();
		int parseInt=0;
		if (moveToLast) {
			String string = cursor.getString(1);
			parseInt = Integer.parseInt(string);
		}
		if(cursor!=null){
			cursor.close();
		}
		db.close();
		return parseInt;
	}
	
	public void insert(String time){
		db = openHelper.getWritableDatabase();
		db.execSQL("update "+ TABLE_NAME + " set downloaded = ? where time = ?", new Object[]{"true", time});
		db.close();
	}
	
	public synchronized List<PhotoDomian> findNewMessage(){
		Log.e(TAG, "findNewMessage");
		list.clear();
		String sql = " SELECT * FROM " + TABLE_NAME +" order by time desc";
		db = openHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			Log.e(TAG, "findNewMessage+moveToNext");
			String string = cursor.getString(4);
			if ("false".equals(string)) {
				PhotoDomian photoDomian = new PhotoDomian();
				photoDomian.setNumber(cursor.getString(1));
				photoDomian.setTime(cursor.getString(2));
				photoDomian.setMilliseconds(cursor.getString(3));
				photoDomian.setDownloaded(cursor.getString(4));
				Log.e(TAG, "list="+list);
				list.add(photoDomian);
				Log.e(TAG, "list="+list);
			}
			
			
		}
		db.close();
		return list;
	}
	
	public synchronized int getNewMessageNumber(String deviceid){
		String sql="select count(time) from "+TABLE_NAME;
		Cursor cursor = null;
		db = openHelper.getReadableDatabase();
		if(!StringUtils.isEmpty(deviceid)){
			sql+=" where deviceid=? and downloaded=?";
			cursor = db.rawQuery(sql, new String[]{deviceid,"false"});
		}
		else{
//			cursor = db.rawQuery(sql, null);
			return 0;
		}
		int num=0;
		if(cursor!=null&&cursor.moveToFirst()){
			num=cursor.getInt(0);
			cursor.close();
		}
//		db.close();
		return num;
	}
}
