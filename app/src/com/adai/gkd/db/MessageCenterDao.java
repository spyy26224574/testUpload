package com.adai.gkd.db;

import java.util.ArrayList;
import java.util.List;

import com.adai.gkd.bean.MessageBean;
import com.adai.gkdnavi.utils.StringUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

public class MessageCenterDao {

	public static final String TB_NAME="tb_message";
	
	public static final String COLUMN_NAME_ID="_id";
	public static final String COLUMN_NAME_TYPE="type";
	public static final String COLUMN_NAME_CONTENT="content";
	public static final String COLUMN_NAME_DEVICEID="deviceid";
	public static final String COLUMN_NAME_STARTTIME="starttime";
	public static final String COLUMN_NAME_ENDTIME="endtime";
	public static final String COLUMN_NAME_URL="url";
	public static final String COLUMN_NAME_ISREAD="isread";
	public static final String COLUMN_NAME_TITLE="title";
	public static final String COLUMN_NAME_CREATETIME="createtime";
	public static final String COLUMN_NAME_ERRORCODE="errorcode";
	
	private GkdSqlHelper helper;
	public MessageCenterDao(Context context) {
		// TODO Auto-generated constructor stub
		helper=GkdSqlHelper.getInstance(context);
	}
	
	public long saveMessage(MessageBean message){
		if(message==null)return -1;
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(COLUMN_NAME_CONTENT, message.content);
		values.put(COLUMN_NAME_CREATETIME, message.createtime);
		values.put(COLUMN_NAME_DEVICEID, message.deviceid);
		values.put(COLUMN_NAME_ENDTIME, message.endtime);
		values.put(COLUMN_NAME_ISREAD, message.isread);
		values.put(COLUMN_NAME_STARTTIME, message.starttime);
		values.put(COLUMN_NAME_TITLE, message.title);
		values.put(COLUMN_NAME_TYPE, message.type);
		values.put(COLUMN_NAME_URL, message.url);
		values.put(COLUMN_NAME_ERRORCODE, message.errorcode);
		return db.insert(TB_NAME, null, values);
	}
	
	public void updateMessage(MessageBean message){
		if(message==null)return;
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values=new ContentValues();
		values.put(COLUMN_NAME_ISREAD, 1);
		db.update(TB_NAME, values, "_id=?", new String[]{String.valueOf(message.id)});
	}
	
	public void deleteMessage(MessageBean message){
		if(message==null)return;
		SQLiteDatabase db = helper.getWritableDatabase();
		db.delete(TB_NAME, "_id=?", new String[]{String.valueOf(message.id)});
	}
	
	public List<MessageBean> getAllMessage(String deviceid){
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor=null;
		try {
			if(!TextUtils.isEmpty(deviceid))
			cursor = db.rawQuery("select * from "+TB_NAME+" where "+COLUMN_NAME_DEVICEID+"=? order by "+COLUMN_NAME_CREATETIME+" desc", new String[]{deviceid});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(cursor!=null){
			List<MessageBean> messages=new ArrayList<MessageBean>();
			while (cursor.moveToNext()) {
				MessageBean message=new MessageBean();
				message.content=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CONTENT));
				message.createtime=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_CREATETIME));
				message.deviceid=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_DEVICEID));
				message.endtime=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ENDTIME));
				message.errorcode=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_ERRORCODE));
				message.id=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID));
				message.isread=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ISREAD));
				message.starttime=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_STARTTIME));
				message.title=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TITLE));
				message.type=cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TYPE));
				message.url=cursor.getString(cursor.getColumnIndex(COLUMN_NAME_URL));
				messages.add(message);
			}
			cursor.close();
			return messages;
		}
		return null;
	}
	
	public synchronized int getNewMessageNumber(String deviceid){
		String sql="select count(_id) from "+TB_NAME;
		Cursor cursor = null;
		SQLiteDatabase db = helper.getReadableDatabase();
		if(!StringUtils.isEmpty(deviceid)){
			sql+=" where deviceid=? and isread=?";
			cursor = db.rawQuery(sql, new String[]{deviceid,"0"});
		}
		else{
			return 0;
		}
		int num=0;
		if(cursor!=null&&cursor.moveToFirst()){
			num=cursor.getInt(0);
			cursor.close();
		}
		return num;
	}
}
