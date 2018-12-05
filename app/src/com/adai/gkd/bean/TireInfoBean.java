package com.adai.gkd.bean;

import java.io.Serializable;

import android.os.Parcel;
import android.os.Parcelable;

public class TireInfoBean implements Serializable/*,Parcelable*/{

	/**
	 * 
	 */
	public static final long serialVersionUID = 1L;
	
	public String id;
	public String user_id;
	public String deviceid;
	public String tire_pressure;
	public String remark;
	public String create_time;
	//传ArrayList参数到另一个Intent中
//	private TireInfoBean(Parcel parcel) {
//		id = parcel.readString();
//		user_id = parcel.readString();
//		deviceid = parcel.readString();
//		tire_pressure = parcel.readString();
//		remark = parcel.readString();
//		create_time = parcel.readString();
//	}
//     
//    public static final Parcelable.Creator<TireInfoBean> CREATOR = new Parcelable.Creator<TireInfoBean>() {  
//  
//        @Override  
//        public TireInfoBean createFromParcel(Parcel source) {  
//            return new TireInfoBean(source);  
//        }  
//  
//        @Override  
//        public TireInfoBean[] newArray(int size) {  
//            return new TireInfoBean[size];  
//        }  
//    };
//	
//	
//	@Override
//	public int describeContents() {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//	@Override
//	public void writeToParcel(Parcel dest, int flags) {
//		dest.writeString(id);  
//		dest.writeString(user_id);  
//		dest.writeString(deviceid);  
//		dest.writeString(tire_pressure);  
//        dest.writeString(remark);
//        dest.writeString(create_time);
//		
//	}
	
	
}
