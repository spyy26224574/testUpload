package com.adai.gkd.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.adai.gkd.bean.SingleInfoBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SingleInfoDao {

	public static final String TB_NAME="tb_singleinfo";
	
	public static final String COLUMN_NAME_ID="_id";
	public static final String COLUMN_NAME_DEVICEID="deviceid";
	/**
	 * 电瓶电压
	 * public float battery_voltage;
	 */
	public static final String COLUMN_NAME_BATTERY_VOLTAGE="battery_voltage";
	/**
	 * 发动机转速
	 * public float engine_speed;
	 */
	public static final String COLUMN_NAME_ENGINE_SPEED="engine_speed";
	/**
	 * 车速
	 * public float speed;
	 */
	public static final String COLUMN_NAME_SPEED="speed";
	/**
	 * 节气门开度
	 * public float tap; 
	 */
	public static final String COLUMN_NAME_TAP="tap";
	/**
	 * 冷却液温度
	 * public float thw;
	 */
	public static final String COLUMN_NAME_THW="thw";
	/**
	 * 瞬时油耗
	 * public float dynamical_fuel;
	 */
	public static final String COLUMN_NAME_DYNAMICAL_FUEL="dynamical_fuel";
	/**
	 * 油耗
	 * public float fuel;
	 */
	public static final String COLUMN_NAME_FUEL="fuel";
	/**
	 * 累积油耗
	 * public float total_fuel;
	 */
	public static final String COLUMN_NAME_TOTAL_FUEL="total_fuel";
	/**
	 * 故障次数
	 * public int error_count;
	 */
	public static final String COLUMN_NAME_ERROR_COUNT="error_count";
	/**
	 * 急加速次数
	 * public int accelerate_count;
	 */
	public static final String COLUMN_NAME_ACCELERATE_COUNT="accelerate_count";
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
	public SingleInfoDao(Context context) {
		// TODO Auto-generated constructor stub
		helper=GkdSqlHelper.getInstance(context);
	}
	public void saveSingleinfo(SingleInfoBean info){
		if(info==null)return;
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(COLUMN_NAME_ACCELERATE_COUNT, info.accelerate_count);
		values.put(COLUMN_NAME_BATTERY_VOLTAGE, info.battery_voltage);
		values.put(COLUMN_NAME_DEVICEID, info.deviceid);
		values.put(COLUMN_NAME_DYNAMICAL_FUEL, info.dynamical_fuel);
		values.put(COLUMN_NAME_ENGINE_SPEED, info.engine_speed);
		values.put(COLUMN_NAME_ERROR_COUNT, info.error_count);
		values.put(COLUMN_NAME_FUEL, info.fuel);
		values.put(COLUMN_NAME_TOTAL_FUEL, info.total_fuel);
		values.put(COLUMN_NAME_HAPPEN_TIME, info.happen_time);
		values.put(COLUMN_NAME_SCRAM_COUNT, info.scram_count);
		values.put(COLUMN_NAME_SPEED, info.speed);
		values.put(COLUMN_NAME_TAP, info.tap);
		values.put(COLUMN_NAME_THW, info.thw);
		db.insert(TB_NAME, null, values);
	}
	
	public List<SingleInfoBean> getAllSingleInfo(){
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from "+TB_NAME, null);
		if(cursor!=null){
			List<SingleInfoBean> infos=new ArrayList<SingleInfoBean>();
			while (cursor.moveToNext()) {
				SingleInfoBean info=new SingleInfoBean();
				info.accelerate_count=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACCELERATE_COUNT));
				info.battery_voltage=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_BATTERY_VOLTAGE));
				info.deviceid=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DEVICEID));
				info.dynamical_fuel=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_DYNAMICAL_FUEL));
				info.speed=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_ENGINE_SPEED));
				info.error_count=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ERROR_COUNT));
				info.fuel=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_FUEL));
				info.total_fuel=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_TOTAL_FUEL));
				info.happen_time=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_HAPPEN_TIME));
				info.scram_count=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_SCRAM_COUNT));
				info.speed=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_SPEED));
				info.tap=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_TAP));
				info.thw=cursor.getFloat(cursor.getColumnIndex(COLUMN_NAME_THW));
				infos.add(info);
			}
			cursor.close();
			return infos;
		}
		return null;
	}
	
	public void deleteAllinfo(){
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete(TB_NAME, null, null);
	}
}
