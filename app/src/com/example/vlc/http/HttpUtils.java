package com.example.vlc.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
/*
 * httputils 
 */
public class HttpUtils {
	private static DefaultHttpClient httpClient;
	static{
		httpClient = new DefaultHttpClient();
		ClientConnectionManager manager = httpClient.getConnectionManager();
		HttpParams params = httpClient.getParams();
		httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
	}

	/*
	 * get
	 */
	public static String getRequest(String url) throws ClientProtocolException, IOException{
		HttpGet get = new HttpGet(url);
		HttpResponse response = httpClient.execute(get);
		if(response.getStatusLine().getStatusCode() == 200){
			String result = EntityUtils.toString(response.getEntity());
			return result;
		}
		return null;
		
	}
	//post
	public static String postRequest(String url,Map<String, String> rawParams) throws ParseException, IOException{
		if(rawParams == null){
			rawParams = new HashMap<String, String>();
		}
		Scheme httScheme = new Scheme("http", PlainSocketFactory.getSocketFactory(), 80);
		httpClient.getConnectionManager().getSchemeRegistry().register(httScheme);
		HttpPost post = new HttpPost(url);
		HttpConnectionParams.setConnectionTimeout(post.getParams(), 30*1000);
		HttpConnectionParams.setSoTimeout(post.getParams(), 30*1000);
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if(rawParams != null){
			for(String key : rawParams.keySet()){
				params.add(new BasicNameValuePair(key, rawParams.get(key)));
			}
		}
		post.setEntity(new UrlEncodedFormEntity(params,"UTF_8"));
		HttpResponse response = httpClient.execute(post);
		if(response.getStatusLine().getStatusCode() == 200){
			String result = EntityUtils.toString(response.getEntity());
			return result;
		}
		return null;
		
	}
}
