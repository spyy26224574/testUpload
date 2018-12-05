

package com.adai.gkdnavi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.ipcamera.application.VLCApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
public class DownloadService extends  IntentService {
	public static final String LOCALAPPNAME = "LigoCam.apk";
	private static final String UPDATINFO = "clickupdate";
	private static final int DOWN_UPDATE = 1;
    private static final int Install = 2;
	private static final int DOWN_error = 3;
    private static final String TAG = "DownloadService";
	private static OnShowNumber onShowNumber;
	
	private IBinder mBinder = new MyBinder();
	
	public DownloadService() {
		super("DownloadService");
	}
	
	public DownloadService(String name) {
		super(name);
	}
	
	public interface OnShowNumber{
		void count(int load);
	}
	
	public void setShowNumber(OnShowNumber showNumber){
		this.onShowNumber = showNumber;
		Log.e(TAG, "onshownumber setting-------"+onShowNumber.toString());
	}
	
	private NotificationManager notificationMrg;
	private Map<String,Notification> notificationCache = new HashMap<String,Notification>();
	private Holder holder;
	int flag=0;
	//private String ServerVersion;
	private Context context;
//	private CheckVersionTask  CheckVersion = new CheckVersionTask(DownloadService.this);
	@Override
	public void onCreate() {
	    super.onCreate();
       //系统 通知栏
		notificationMrg = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
	
	 }	
 
//    状态栏视图更新
	private Notification displayNotificationMessage(Notification notification,int count,int flag,String url) 
	{	  
		SharedPreferences spf_otacontentdown = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
        String   applicationnameget  = spf_otacontentdown.getString("gspOTAAppName", "");
        RemoteViews contentView1 =notification.contentView;	
		//Log.e(otaTag,"update percent=="+count);
	    String   apkurl1get  = spf_otacontentdown.getString("gspOtaApkUrl", "");
	    if(url.equals(apkurl1get))
	    contentView1.setTextViewText(R.id.n_title,applicationnameget);
	    contentView1.setTextViewText(R.id.n_text, getString(R.string.ota_nowpercent) + count + "% ");
		contentView1.setProgressBar(R.id.n_progress, 100, count, false);
		notification.contentView = contentView1;
        //提交一个通知在状态栏中显示。如果拥有相同标签和相同id的通知已经被提交而且没有被移除，该方法会用更新的信息来替换之前的通知。	
		notificationMrg.notify(flag, notification);
		 return notification;
	}
	
	public class MyBinder extends Binder{
		public DownloadService getService() {
			return DownloadService.this;
		}
	}
	
  @Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	public static String getLocalappname(){
		return VLCApplication.OTA_PATH+"/"+LOCALAPPNAME;
	}

 private boolean isNetwordAvaliable(Context context) {
  		boolean result = false;
        ConnectivityManager connectManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectManager == null) {
  			result = false;
  		} else {
  			NetworkInfo[] netInfo = connectManager.getAllNetworkInfo();
  			for (NetworkInfo info : netInfo) {
  				if (info.getState() == NetworkInfo.State.CONNECTED) {
  					result = true;
  					break;
  				}
  			}
  		}
  		return result;
  	}
	
  public void loadFile(String strUrl,int flag)
	{
	  if(TextUtils.isEmpty(strUrl))return;
		Intent notificationIntent = new Intent(getApplicationContext(), this.getClass());
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		addflag设置跳转类型
		PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(), 0,notificationIntent, 0);
		// 创建Notifcation对象，设置图标，提示文字
		long number = 100;
		Notification notification = new Notification(R.drawable.ic_launcher,getString(R.string.ota_downtitle),number);// 设定Notification出现时的声音，一般不建议自定义 System.currentTimeMillis()
		notification.flags |= Notification.FLAG_ONGOING_EVENT;//出现在 “正在运行的”栏目下面	
		RemoteViews contentView1 = new RemoteViews(getPackageName(),R.layout.notification_version);	
		contentView1.setImageViewResource(R.id.in_n_icon, R.drawable.ic_launcher);
	    contentView1.setTextViewText(R.id.n_title,getString(R.string.ota_predownload));
		contentView1.setTextViewText(R.id.n_text, getString(R.string.ota_nowpercent) + 0 + "% ");
		contentView1.setProgressBar(R.id.n_progress, 100, 0, false);
		notification.contentView = contentView1;
		notification.contentIntent = contentIntent;
		double dMsg2Ui = 0.0; //update ui Threshold
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
		HttpGet get = new HttpGet(strUrl);
		HttpResponse response;
		SharedPreferences spf_otacontentdown=getSharedPreferences("gspOta", Context.MODE_PRIVATE);
		File file = null;
		try {
			response = client.execute(get);
			HttpEntity entity = response.getEntity();
			double length = entity.getContentLength();
			InputStream isSteam = entity.getContent();
 		    //使用InputStream对文件进行读取，就是字节流的输入
			FileOutputStream fileOutputStream = null;
		  
            if (isSteam != null){
			   //String   apkurl1get  = spf_otacontentdown.getString("gspOtaApkUrl", "");
			   //if(url.equals(apkurl1get)){
			      file = new File(VLCApplication.OTA_PATH, LOCALAPPNAME);
			   //}
			   				    
			   fileOutputStream = new FileOutputStream(file);
			   byte[] buf = new byte[1024];
			   int iReadbytes = -1;
			   float fReadCount = 0;
				
               //ch中存放从buf字节数组中读取到的字节个数
				while ((iReadbytes = isSteam.read(buf)) != -1) {
					fileOutputStream.write(buf, 0, iReadbytes);
					fReadCount += iReadbytes;
					//从字节数组读取数据read(buf)后，返回，读取的个数，count中保存，已下载的数据字节数
					double temp=fReadCount/length;
					if(temp>=dMsg2Ui){
						dMsg2Ui+=0.01;
						load = (int) ( fReadCount*100/length);
						if (onShowNumber!=null) {
							Log.e(TAG, "calll---------------"+onShowNumber);
							onShowNumber.count(load);
						}
					    sendMsg(DOWN_UPDATE,load,strUrl,notification,flag,null); 
					    //Log.e(otaTag, ">>>>>load = " + load + ",temp = "+ temp+",count=" +fReadCount + ",m = " + dMsg2Ui );
				    }
					//如果后台下载过程中没网了就  停止状态栏
					if (!isNetwordAvaliable(getApplicationContext())) {
						 //System.out.println("我要关闭关闭");
						 Log.e(TAG,"network is avaliabe");
						 notificationMrg.cancel(flag);
						 if (file!=null) {
								boolean delete = file.delete();
								Log.e(TAG, "delete="+delete);
						 }
						 Log.e(TAG, "DOWN_error000");
						 sendMsg(DOWN_error,0,strUrl,notification,0,null);
                         //Toast.makeText(getApplicationContext(), getString(R.string.ota_netcantuse), 1).show();
                         break;
					  } //	函数调用handler发送信息
				}
             }
            if(fileOutputStream!=null){
            	fileOutputStream.flush();
				fileOutputStream.close();
            }
            //文件输出流为空，则表示下载完成，安装apk文件
			Uri localFilePath=Uri.fromFile(file);
			Log.e(TAG, "download successfull,file path= "+localFilePath);
		    //本地的md5
			//服务器上的md5
  		    String   strServerMD5  = spf_otacontentdown.getString("gspOtaAPKMD5", "");
  		    String str_Localmd5 =CheckVersionTask.fileMD5(file.getAbsolutePath());
    	    //System.out.println(serverMD5+"***************get xml  check***************************");
    	    //System.out.println(saveFileName+"saveFileNamesaveFileName  cunchumd5");
  		    Log.e(TAG, "serverMD5 =" + strServerMD5);
  		    Log.e(TAG, "Localmd5 =" + str_Localmd5);
    	    if (strServerMD5.compareTo(str_Localmd5) == 0) {
    	    	  //Log.e(otaTag, "download successful");
    	    	Log.e(TAG, "Install");
    	    	sendMsg(Install,0,strUrl,notification,0,localFilePath);
    	    	  //fileOutputStream.flush();
    	      }else{
    	    	  //Log.e(otaTag, "download fail..");
    	    	  if (file!=null) {
    	    		  file.delete();
    	    	  }
    	    	  notificationMrg.cancel(flag);
    	    	  Log.e(TAG, "DOWN_error1111");
    	    	  sendMsg(DOWN_error,0,strUrl,notification,0,null);
    	      }
//			if (fileOutputStream != null) {
//				fileOutputStream.close();
//			}
		} catch (IOException e) {
			if (file!=null) {
				file.delete();
			}
			notificationMrg.cancel(flag);
			Log.e(TAG, "DOWN_error2222");
			sendMsg(DOWN_error,0,strUrl,notification,0,null);
		}catch (Exception e) {
			if (file!=null) {
				file.delete();
			}
			notificationMrg.cancel(flag);
			sendMsg(DOWN_error,0,strUrl,notification,0,null);
		}
	}
	
	private void sendMsg(int what,int c,String url,Notification notification,int flag,Uri APKuri) {
		Message msg = new Message();
		msg.what= what;//用来识别发送消息的类型
		msg.arg1=0;
		holder=new Holder();
		holder.count=c;
		holder.url=url;
		holder.flag=flag;
		holder.notify=notification;
		holder.Uri=APKuri;
		msg.obj=holder;//消息传递的自定义对象信息
		handler.sendMessage(msg);
	}
	// 定义一个Handler，用于处理下载线程与主线程间通讯
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			final Holder data=(Holder) msg.obj;
		  if (!Thread.currentThread().isInterrupted()) {
		
//				判断下载线程是否中断
				switch (msg.what) {
				
				case DOWN_UPDATE://1
					if(data.count>=99){
						notificationMrg.cancel(data.flag);	
						break;
					}
					Notification notification;
					if(notificationCache.containsKey(data.url)){	
						//每次更新时，先以key，扫描hashmap，存在则读取出来。
						notification = notificationCache.get(data.url);				
						notification=displayNotificationMessage(notification,data.count ,data.flag,data.url);
						notificationCache.put(data.url, notification);
					}
					else 
					{
						//第一次更新时，传入notification对象每次，将notification对象存入hashmap中
						notification=data.notify;
						notification=displayNotificationMessage(notification,data.count ,data.flag,data.url);
						notificationCache.put(data.url, notification);
					}
					
					break;
				case Install://2
					VLCApplication.mIsDownloadedAPK = false;
					SharedPreferences spf_otacontentdown=getSharedPreferences("gspOta", Context.MODE_PRIVATE);
	 			     String   apkurl1get  = spf_otacontentdown.getString("gspOtaApkUrl", "");
					if(data.url.equals(apkurl1get)){			
						//Toast it1 = null;
						Toast.makeText(getApplication(), getString(R.string.download_cuccess), Toast.LENGTH_SHORT).show();
						sendUpdateInfo();
			          }
					 
					openfile(data.Uri);
					break;
				case DOWN_error://-1
					String error = msg.getData().getString("error");
					//Toast.makeText(getApplication(), error, 1).show();
					Toast.makeText(getApplication(), getString(R.string.download_newversion_failed), Toast.LENGTH_SHORT).show();
					sendUpdateInfo();
					VLCApplication.mIsDownloadedAPK = false;
					Log.e(TAG, "下载失败！，请重试...");
					break;
//					否则输出错误提示
				}
			}
			super.handleMessage(msg);
		}
	};
	public int load;
	public void openfile(Uri url){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(url,"application/vnd.android.package-archive");
		startActivity(intent);
	}
	public class Holder{
		Notification notify;
		String url;
		int count;
		int flag;
		Uri Uri;
	}
	@Override
	protected void onHandleIntent(Intent intent) {

		//System.out.println("Get intent:" + intent);
		final String strOtaUrl = intent.getStringExtra("otapakurl");//当为4G网络状态时候，这里的strOtaUrl为空
//		SharedPreferences  apkurl1Preferences = getSharedPreferences("gspOta", Context.MODE_PRIVATE);
//		final String string = apkurl1Preferences.getString("gspOtaApkUrl", "");
		// 注:此处url必须声明为final常量，否则是不会被子线程中读取到的
		//System.out.println("Get intent url:" + strOtaUrl);
		Log.e(TAG, "strOtaUrl = "+ strOtaUrl);
		Runnable start = new Runnable() {
			@Override
			public void run() {
				loadFile(strOtaUrl, ++flag);
				//loadFile(string, flag);
			}
		};
		new Thread(start) {
		}.start();

	}

	private void sendUpdateInfo() {
		Intent intent=new Intent(UPDATINFO);
		sendBroadcast(intent);
	}
	
}
	
	
	 