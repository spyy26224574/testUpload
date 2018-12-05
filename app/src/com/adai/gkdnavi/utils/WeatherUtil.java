package com.adai.gkdnavi.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.android.common.logging.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;

import android.content.Context;

public class WeatherUtil implements BDLocationListener {
	
	private int max_hour=24;
	private Callback callback;
	public interface Callback{
		public void onCallback(String[] data);
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	public void setMax_hour(int max_hour) {
		this.max_hour = max_hour;
	}

	String weatherurl="http://api.map.baidu.com/telematics/v3/weather?location=%s&output=json&ak=3b03fcd08c5211250b0f15f13782403e";
	LocationClient mLocClient;
	public WeatherUtil(Context context) {
		// TODO Auto-generated constructor stub
		mLocClient=new LocationClient(context);
		mLocClient.registerLocationListener(this);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setIsNeedAddress(true);
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(5000);
		mLocClient.setLocOption(option);
	}
	public void start(){
		mLocClient.start();
	}
	@Override
	public void onReceiveLocation(BDLocation loc) {
		// TODO Auto-generated method stub
		if(loc!=null){
			mLocClient.stop();
			String location=String.valueOf(loc.getLongitude())+","+String.valueOf(loc.getLatitude());
			String url=String.format(weatherurl, location);
			Log.e("weatherutil", url);
			new ReqestWeather(url).start();
		}else{
			
		}
	}
	
	class ReqestWeather extends Thread{
		String _url;
		public ReqestWeather(String url) {
			this._url=url;
		}
		@Override
		public void run() {
			try {
				URL url=new URL(_url);
				HttpURLConnection conn=(HttpURLConnection) url.openConnection();
				conn.setDoInput(true);
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setUseCaches(false);
				conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");  
				conn.setRequestProperty("Charsert", "UTF-8");   
				conn.setRequestProperty("Content-Type", "text/json;"); 
				conn.connect();
				InputStream is = conn.getInputStream();
				InputStreamReader isr = new InputStreamReader(is, "UTF-8");
				BufferedReader br=new BufferedReader(isr);
				StringBuffer result=new StringBuffer();
				String line=null;
				while((line=br.readLine())!=null){
					result.append(line);
				}
				JSONObject jObject=new JSONObject(result.toString());
				is.close();
				conn.disconnect();
				if(jObject.getInt("error")==0){
					JSONArray results = jObject.getJSONArray("results");
					if(results!=null&&results.length()>0){
						JSONObject object = results.getJSONObject(0);
						JSONArray weathers = object.getJSONArray("weather_data");
						if(weathers!=null&&weathers.length()>1){
							JSONObject weather = weathers.getJSONObject(0);
							JSONObject tomorow = weathers.getJSONObject(1);
							getData(weather,tomorow);
							return;
						}
					}
				}
				getData(null,null);
			}  catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				getData(null,null);
			}
		}
	}
	
	private String[] getData(JSONObject today,JSONObject tomorow){
		String[] data=null;
		Date date=new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int hour=calendar.get(Calendar.HOUR_OF_DAY)+1;
		int month=calendar.get(Calendar.MONTH)+1;
		if(today!=null&&tomorow!=null){
			try {
				String todayweather=today.getString("weather");
				String tomorowweather=tomorow.getString("weather");
				String todaytemprature=today.getString("temperature");
				String tomorowtemprature=tomorow.getString("temperature");
				String[] temps=todaytemprature.split("~");
				String[] totemps=tomorowtemprature.split("~");
				int todaymaxtemp=30;
				int tomorowmaxtemp=30;
				try{
					if(temps!=null&&temps.length>1){
						int temp1=Integer.parseInt(temps[0].trim());
						int temp2=Integer.parseInt(temps[1].substring(0, temps[1].length()-1).trim());
						todaymaxtemp=temp1>=temp2?temp1:temp2;
					}
					if(totemps!=null&&totemps.length>1){
						int temp1=Integer.parseInt(totemps[0].trim());
						int temp2=Integer.parseInt(totemps[1].substring(0, temps[1].length()-1).trim());
						tomorowmaxtemp=temp1>=temp2?temp1:temp2;
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				if(todaymaxtemp>25){
					if(hour<9){
						int temp=9-hour+1;
						data=getData(temp>max_hour?max_hour:temp);
					}else if(hour>=9&&hour<18){
						data=getData(1);
					}else{
//						if(tomorowmaxtemp>25){
//							data=getData(max_hour-hour+9+1);
//						}else{
//							data=getData(max_hour);
//						}
						data=getData(max_hour);
					}
				}else{
					if(tomorowmaxtemp>25){
						int temphour=24-hour+9+1;
						data=getData(temphour>=max_hour?max_hour:temphour);
					}else{
						data=getData(max_hour);
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				data=getData(1);
			}
		}else{
			if(hour<9){
				int temphour=9-hour+1;
				data=getData(temphour>=max_hour?max_hour:temphour);
			}else if(hour>=9&&hour<18){
				data=getData(1);
			}else{
				int temphour=24-hour+9+1;
				data=getData(temphour>=max_hour?max_hour:temphour);
			}
		}
		if(callback!=null){
			callback.onCallback(data);
		}
		return data;
	}
	
	public String[] getData(int hours){
		List<String> data=new ArrayList<String>();
		for(int i=0;i<hours;i++){
			data.add(String.valueOf(i+0.5)+"h");
			data.add(String.valueOf(i+1)+"h");
		}
		if(data.size()<max_hour*2)
		data.add(String.valueOf(hours+0.5)+"h");
		return data.toArray(new String[data.size()]);
	}
}
