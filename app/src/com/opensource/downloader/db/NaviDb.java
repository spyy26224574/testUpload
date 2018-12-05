package com.opensource.downloader.db;

/*
 * author  daimeng
 * 管理收藏地址数据库
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ipcamera.domain.Address;

import java.util.ArrayList;
import java.util.List;


public class NaviDb {

	private static MyOpenHelper openHelper;
	private static final String DATABASE_NAME = "gdknavi.db"; // 数据库名称
	private static final int DATABASE_VERSION = 2; // 数据库版本
	private static final String TABLE_NAME = "ADDRESS";
	private SQLiteDatabase db;
	private Cursor cursor;
	private List<Address> list = new ArrayList<Address>();
	/*
	 *  改写数据库创建和管理的辅助类
	 */
	private static class MyOpenHelper extends SQLiteOpenHelper {
		// 第三个参数CursorFactory指定在执行查询时获得一个游标实例的工厂类,设置为null,代表使用系统默认的工厂类
		public MyOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// 创建contacts表，SQL表达式时提供的字段类型和长度仅为提高代码的可读性。
			String createSql = "CREATE TABLE  IF NOT EXISTS "
					+ TABLE_NAME
					+ " (_id integer primary key autoincrement,name varchar(20),address varchar(20),Collect_Latitude varchar(20),Collect_Longitude varchar(20))";
			db.execSQL(createSql);

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// 更新
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
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
	public NaviDb(Context context){
		openHelper = MyOpenHelper.getInstance(context);
	}
	
	public void delTable(Context context) {
		// db = this.openHelper.getWritableDatabase();
		// db.execSQL("DROP TABLE IF EXISTS appsetting");
		openHelper.deleteDatabase(context);
		db.close();

	}
	/*
	 * 保存一个地址
	 */
	public void save(Address address){
		// 开启事务
		// db.beginTransaction();
		try {		
		db = openHelper.getWritableDatabase();
		String sql = "INSERT INTO "
				+ TABLE_NAME
				+ "(name,address,Collect_Latitude,Collect_Longitude) values (?,?,?,?)";
	
		Object[] bindArgs = {
				address.getName(),
				address.getAddress(),
				address.getCollectLatitude(),
				address.getCollectLongitude()};
		db.execSQL(sql, bindArgs);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.close();

		}
	}
	/*
	 * 返回所有的地址信息，显示在listView上面
	 */
	public List<Address> find(){
		String sql = " SELECT * FROM " + TABLE_NAME;
		db = openHelper.getReadableDatabase();
		cursor = db.rawQuery(sql, null);
		while(cursor.moveToNext()){
			Address address = new Address();
			address.setName(cursor.getString(1));
			address.setAddress(cursor.getString(2));
			address.setCollectLatitude(cursor.getString(3));
			address.setCollectLongitude(cursor.getString(4));
			list.add(address);
		}
		db.close();
		return list;
		
	}
	/*
	 * 删除常用地址
	 */
	public void delete(Address address){
		db = openHelper.getWritableDatabase();
		String delete = address.getAddress();
		String sql = "DELETE FROM " + TABLE_NAME + " where address = "  + "'" + delete + "'";
		//Object[] bindArgs = { address.getName(),address.getAddress() };
		db.execSQL(sql);
		db.close();
	}
	
	

	

}
