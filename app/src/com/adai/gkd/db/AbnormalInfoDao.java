package com.adai.gkd.db;

import java.util.ArrayList;
import java.util.List;

import com.adai.gkd.bean.AbnormalInfoBean;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AbnormalInfoDao {

	public static final String TB_NAME="tb_abnormalinfo";
	
	public static final String COLUMN_NAME_ID="_id";
	public static final String COLUMN_NAME_DEVICEID="deviceid";
	
	/**
	 * 故障ID,1胎压异常,2冷却液异常
	 * public int abnormal_id;
	 */
	public static final String COLUMN_NAME_ABNORMAL_ID="abnormal_id";
	/**
	 * 故障详情
	 * public String detail;
	 */
	public static final String COLUMN_NAME_DETAIL="detail";
	/**
	 * 时间，格式:"2016-01-01 00:00:00"
	 * public String happen_time;
	 */
	public static final String COLUMN_NAME_HAPPEN_TIME="happen_time";
	private GkdSqlHelper helper;
	
	public AbnormalInfoDao(Context context) {
		// TODO Auto-generated constructor stub
		helper=GkdSqlHelper.getInstance(context);
	}
	
	public long saveAbnormal(AbnormalInfoBean abnormal){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(COLUMN_NAME_ABNORMAL_ID, abnormal.abnormal_id);
		values.put(COLUMN_NAME_DETAIL, abnormal.detail);
		values.put(COLUMN_NAME_DEVICEID, abnormal.deviceid);
		values.put(COLUMN_NAME_HAPPEN_TIME, abnormal.happen_time);
		return db.insert(TB_NAME, null, values);
	}
	
	public List<AbnormalInfoBean> getAllInfo(){
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from "+TB_NAME, null);
		if(cursor!=null){
			List<AbnormalInfoBean> infos=new ArrayList<AbnormalInfoBean>();
			while (cursor.moveToNext()) {
				AbnormalInfoBean info=new AbnormalInfoBean();
				info.abnormal_id=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ABNORMAL_ID));
				info.detail=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DETAIL));
				info.deviceid=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DEVICEID));
				info.happen_time=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_HAPPEN_TIME));
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
