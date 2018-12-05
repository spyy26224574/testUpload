package com.adai.gkdnavi.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class HeadImageUtil {

	private static final String CACHE_PATH_ROOT=Environment.getExternalStorageDirectory().getAbsolutePath()+"/gdknavi/head/";
	private static HeadImageUtil _instance;
	
	public interface CacheComplete{
		/**
		 *
		 */
		public void onComplete();
	}
	private HeadImageUtil() {
		// TODO Auto-generated constructor stub
	}
	public static HeadImageUtil getInstance(){
		if(_instance==null){
			_instance=new HeadImageUtil();
			File root=new File(CACHE_PATH_ROOT);
			if(!root.exists()){
				root.mkdirs();
			}
		}
		return _instance;
	}
	/**
	 * ��������ͷ��
	 * @param complete
	 * @param urls
	 */
	public void cacheHead(CacheComplete complete,List<String> urls){
		if(urls==null)return;
		new CacheThread(complete, urls).start();
	}
	
	public String getCachePath(String url){
		if(TextUtils.isEmpty(url))return null;
		String[] strs=url.split("/");
		if(strs.length>1){
			return CACHE_PATH_ROOT+"/"+strs[strs.length-1];
		}
		return null;
	}
	
	class CacheThread extends Thread{
		private CacheComplete complete;
		private List<String> urls;
		public CacheThread(CacheComplete complete,List<String> urls) {
			// TODO Auto-generated constructor stub
			this.complete=complete;
			this.urls=urls;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			for (String path : urls) {
				try {
					URL url = new URL(path);
					HttpURLConnection conn=(HttpURLConnection) url.openConnection();
					InputStream input = conn.getInputStream();
					FileOutputStream fos=new FileOutputStream(getCachePath(path));
					byte[] buffer=new byte[1024];
					int len=-1;
					while ((len=input.read(buffer))!=-1) {
						fos.write(buffer,0,len);
					}
					input.close();
					fos.close();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			if(complete!=null){
				complete.onComplete();
			}
		}
	}
}
