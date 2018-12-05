/**
 * Added by zhangyanhu C01012,2014-8-29
 */
package com.adai.camera.sunplus.bean;

import android.util.Log;

import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.icatch.wificam.customer.type.ICatchMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Added by zhangyanhu C01012,2014-8-29
 */
public class TimeLapseInterval {
	private static final String TAG = "TimeLapseInterval";
	public static final int TIME_LAPSE_INTERVAL_OFF = 0;
//	public static final int TIME_LAPSE_INTERVAL_2S = 2;
//	public static final int TIME_LAPSE_INTERVAL_5S = 5;
//	public static final int TIME_LAPSE_INTERVAL_10S = 10;
//	public static final int TIME_LAPSE_INTERVAL_20S = 20;
//	public static final int TIME_LAPSE_INTERVAL_30S = 30;
//	public static final int TIME_LAPSE_INTERVAL_1MIN = 60;
//	public static final int TIME_LAPSE_INTERVAL_5MIN = 300;
//	public static final int TIME_LAPSE_INTERVAL_10MIN = 600;
//	public static final int TIME_LAPSE_INTERVAL_30MIN = 1800;
//	public static final int TIME_LAPSE_INTERVAL_1HR = 3600;

	private String[] valueListString;
	private int[] valueListInt;

	public TimeLapseInterval(){
		initTimeLapseInterval();
	}

	public String getCurrentValue() {
		return convertTimeLapseInterval(cameraProperty.getCurrentTimeLapseInterval());
	}

	public String[] getValueStringList() {
		return valueListString;
	}

	public int[] getValueStringInt() {
		return valueListInt;
	}
	
	public boolean setValueByPosition(int position){
		boolean retValue;		
		retValue = CameraProperties.getInstance().setTimeLapseInterval(valueListInt[position]);
		return retValue;
	}
	
	private CameraProperties cameraProperty = CameraProperties.getInstance();
	
	public void initTimeLapseInterval() {
		Log.e(TAG,"[Normal] -- TimeLapseInterval: begin initTimeLapseInterval");
		
		if (!cameraProperty.cameraModeSupport(ICatchMode.ICH_MODE_TIMELAPSE)) {
			return;
		}
		List<Integer> list = cameraProperty.getSupportedTimeLapseIntervals();
		int length = list.size();
		ArrayList<String> tempArrayList = new ArrayList<String>();
		valueListInt = new int[length];
		
		for (int ii = 0; ii < length; ii++) {
			tempArrayList.add(convertTimeLapseInterval(list.get(ii)));
			valueListInt[ii] = list.get(ii);
		}
		
		valueListString = new String[tempArrayList.size()];
		for(int ii = 0;ii < tempArrayList.size();ii++){
			valueListString[ii] = tempArrayList.get(ii);
		}
		Log.e(TAG, "initTimeLapseInterval: [Normal] -- TimeLapseInterval: end initTimeLapseInterval timeLapseInterval =" + valueListString.length);
	}
	
	public static String convertTimeLapseInterval(int value){
		if(value == 0){
			return "OFF";
		}
		String time = "";
		//JIRA ICOM-1764 Start Add by b.jiang 20160111
		if(value == -2){
			return "0.5 Sec";
		}
		//JIRA ICOM-1764 End Add by b.jiang 20160111
		int h = value / 3600;
		int m = (value % 3600) / 60;
		int s = value % 60;
		if(h > 0){
			time = time + h+" HR ";
		}
		if(m > 0){
			time = time + m + " Min ";
		}
		if(s > 0){
			time = time + s + " Sec";
		}
		return time;
	}
}
