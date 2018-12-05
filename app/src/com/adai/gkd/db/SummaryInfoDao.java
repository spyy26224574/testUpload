package com.adai.gkd.db;

import java.util.ArrayList;
import java.util.List;

import com.adai.gkd.bean.SummaryInfoBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SummaryInfoDao {

	public static final String TB_NAME="tb_summaryinfo";
	
	public static final String COLUMN_NAME_ID="_id";
	public static final String COLUMN_NAME_DEVICEID="deviceid";
	
	/**
	 * 热车时长
	 * public float hot_time;
	 */
	public static final String COLUMN_NAME_HOT_TIME="hot_time";
	/**
	 * 怠速时长
	 * public float idling_time;
	 */
	public static final String COLUMN_NAME_IDLING_TIME="idling_time";
	/**
	 * 行驶时长
	 * public float travel_time;
	 */
	public static final String COLUMN_NAME_TRAVEL_TIME="travel_time";
	/**
	 * 怠速油耗
	 * public float idling_fuel;
	 */
	public static final String COLUMN_NAME_IDLING_FUEL="idling_fuel";
	/**
	 * 最高转速
	 * public float max_rotate_speed;
	 */
	public static final String COLUMN_NAME_MAX_ROTATE_SPEED="max_rotate_speed";
	/**
	 * 最高行驶速度
	 * public float max_speed;
	 */
	public static final String COLUMN_NAME_MAX_SPEED="max_speed";
	/**
	 * 急加速次数
	 * public int accelerate_count;
	 */
	public static final String COLUMN_NAME_ACCELERATE_count="accelerate_count";
	/**
	 * 急减速次数
	 * public int scram_count;
	 */
	public static final String COLUMN_NAME_SCRAM_COUNT="scram_count";
	/**
	 * 时间，格式:"2016-01-01 00:00:00"
	 * public String happen_time;
	 */
	public static final String COLUMN_NAME_HAPPEN_TIME="happen_time";
	
	GkdSqlHelper helper;
	public SummaryInfoDao(Context context) {
		// TODO Auto-generated constructor stub
		helper=GkdSqlHelper.getInstance(context);
	}
	
	public long saveSummaryInfo(SummaryInfoBean info){
		if(info==null)return -1;
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(COLUMN_NAME_ACCELERATE_count, info.accelerate_count);
		values.put(COLUMN_NAME_DEVICEID, info.deviceid);
		values.put(COLUMN_NAME_HAPPEN_TIME, info.happen_time);
		values.put(COLUMN_NAME_HOT_TIME, info.hot_time);
		values.put(COLUMN_NAME_IDLING_FUEL, info.idling_fuel);
		values.put(COLUMN_NAME_IDLING_TIME, info.idling_time);
		values.put(COLUMN_NAME_MAX_ROTATE_SPEED, info.max_rotate_speed);
		values.put(COLUMN_NAME_MAX_SPEED, info.max_speed);
		values.put(COLUMN_NAME_SCRAM_COUNT, info.scram_count);
		values.put(COLUMN_NAME_TRAVEL_TIME, info.travel_time);
		return db.insert(TB_NAME, null, values);
	}
	
	public List<SummaryInfoBean> getAllSummaryInfo(){
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from "+TB_NAME, null);
		if(cursor!=null){
			List<SummaryInfoBean> infos=new ArrayList<SummaryInfoBean>();
			while (cursor.moveToNext()) {
				SummaryInfoBean info=new SummaryInfoBean();
				info.accelerate_count=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACCELERATE_count));
				info.deviceid=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DEVICEID));
				info.happen_time=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_HAPPEN_TIME)).trim();
				info.hot_time=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_HOT_TIME));
				info.idling_fuel=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_IDLING_FUEL));
				info.idling_time=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_IDLING_TIME));
				info.max_rotate_speed=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_MAX_ROTATE_SPEED));
				info.max_speed=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_MAX_SPEED));
				info.scram_count=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_SCRAM_COUNT));
				info.travel_time=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_TRAVEL_TIME));
				infos.add(info);
			}
			cursor.close();
			return infos;
		}
		return null;
	}
	
	public void deleteSummaryInfo(){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete(TB_NAME, null, null);
	}
}
