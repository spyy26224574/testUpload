package com.adai.gkdnavi.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adai.gkdnavi.PhotoDomian;
import com.example.ipcamera.domain.MinuteFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/*
 * 这样的全局管理类很重要   以后开发的属性以及控制逻辑的都可以用这样的类进行管理        
 */
public class VoiceManager {
	/*
	 * static在程序中不会释放，所以能满足就用较小的满足
     */
	public static boolean isCameraBusy = false;
	public static boolean isWifiPasswordChange = false;
	public static HashSet<MinuteFile> selectedMinuteFile = new HashSet<>();
	public static  boolean isVoiceFromVoiceChatting = false;
	public static boolean isVoiceFromVoiceChattingDia = false;
	public static boolean isVoiceFromPhoneView = false;  //judge voice waker after call 
	public static boolean isVoiceFromMainView = false;
	public static char limitSpeedType = '0';
	public static boolean isRecording = true;
	public static boolean isRegister = false;
	public static boolean isRegisterVoice = false;
	public static boolean isGaodeNaviView = false;
	public static boolean isLogin = false;
	public static boolean isGoMap = false;
	public static boolean isGoPhone = false;
	public static boolean isHasFocus = false;
	public static boolean isConnectBaidu = false;
	public static boolean isSelectWhich = false;
	//public static int count = 0;
	public static List<PhotoDomian> list = new ArrayList<PhotoDomian>();
	public static String xuliehao;
	public static String gujianVersion;
	public static boolean isHaveNet = true;
	public static String xinghao;
	public static String obdversion;
	public static String cameraversion;
	public static int number = -1;
	public static boolean isGaodeNavi=false;
	
	//方向值 
	public static byte[] direct = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46};
	public static byte turn_back =1;
	public static byte turn_branch_center = 2;
	public static byte turn_branch_left = 3;
	public static byte turn_branch_left_straight = 4;
	public static byte turn_branch_right = 5;
	public static byte turn_branch_right_straight = 6;
	public static byte turn_dest = 7;
	public static byte turn_front = 8;
	public static byte turn_front_2branch_left = 9;
	public static byte turn_front_2branch_right = 10;
	public static byte turn_front_3branch_left = 11;
	public static byte turn_front_3branch_middle = 12;
	public static byte turn_front_3branch_right = 13;
	public static byte turn_inferry = 14;
	public static byte turn_left = 15;
	public static byte turn_left_2branch_left = 16;
	public static byte turn_left_2branch_right = 17;
	public static byte turn_left_3branch_left = 18;
	public static byte turn_left_3branch_middle = 19;
	public static byte turn_left_3branch_right = 20;
	public static byte turn_left_back = 21;
	public static byte turn_left_front = 22;
	public static byte turn_left_side = 23;
	public static byte turn_left_side_ic = 24;
	public static byte turn_left_side_main = 25;
	public static byte turn_lf_2branch_left = 26;
	public static byte turn_lf_2branch_right = 27;
	public static byte turn_rf_2branch_left = 28;
	public static byte turn_rf_2branch_right = 29;
	public static byte turn_right = 30;
	public static byte turn_right_2branch_left = 31;
	public static byte turn_right_2branch_right = 32;
	public static byte turn_right_3branch_left = 33;
	public static byte turn_right_3branch_middle = 34;
	public static byte turn_right_3branch_right = 35;
	public static byte turn_right_back = 36;
	public static byte turn_right_front = 37;
	public static byte turn_right_side = 38;
	public static byte turn_right_side_ic = 39;
	public static byte turn_right_side_main = 40;
	public static byte turn_ring = 41;
	public static byte turn_ring_out = 42;
	public static byte turn_start = 43;
	
	//voice action
    public static int  iVoiceAct = 0;  //voice action
    public static final  int iVoiceSelect = 1; //voice notice start voice service and please select
    public static final  int iVoiceStartCall = 2; //voice notice telephone now
    public static final  int iVoiceStartNaviView = 3;  //navigation view
    public static final  int iVoiceStartNavi = 4; //voice notice navigation now
    public static final  int ivoiceStartPhoneView = 5; //phone view
    public static String strVoicePhoneNumber = null; //voice phone number
    public static String strVoiceNaviTo = null; //voice navi to 
    
    /**
     * 检查当前网络是否可用
     * 
     * @param activity
     * @return
     */
    
    public static boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            
            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    System.out.println(i + "===状态===" + networkInfo[i].getState());
                    System.out.println(i + "===类型===" + networkInfo[i].getTypeName());
                    // 判断当前网络状态是否为连接状态           ap没有判断
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

	
	//className 进行判断
	public static boolean isServiceRunning(Context mContext,String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(100);//这个参数很重要，因为服务有优先级，手机系统本身自带很服务，如果设置小了就会获取不到，这是一个坑

        if (serviceList==null||!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
	/**
	 * 根据包名判断应用是否启动
	 * @param mContext
	 * @param packname  包名
	 * @return
	 */
	public static boolean isAppRunning(Context mContext,String packname){
		
		boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(100);//这个参数很重要，因为服务有优先级，手机系统本身自带很服务，如果设置小了就会获取不到，这是一个坑

        if (serviceList!=null&&!(serviceList.size()>0)) {
            return false;
        }

        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getPackageName().equals(packname) == true) {
                isRunning = true;
                break;
            }
        }
		activityManager=null;
		serviceList=null;
        return isRunning;
	}
	
	
	/** 
	 * 设置手机的移动数据 
	 */  
	public static void setMobileData(Context pContext, boolean pBoolean) {  
	  
	    try {  
	  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	  
	        Class<? extends ConnectivityManager> ownerClass = mConnectivityManager.getClass();  
	  
	        Class[] argsClass = new Class[1];  
	        argsClass[0] = boolean.class;  
	  
	        Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);  
	  
	        method.invoke(mConnectivityManager, pBoolean);  
	  
	    } catch (Exception e) {  
	        // TODO Auto-generated catch block  
	        e.printStackTrace();  
	        System.out.println("移动数据设置错误: " + e.toString());  
	    }  
	}  
	  
	/** 
	 * 返回手机移动数据的状态 
	 * 
	 * @param pContext 
	 * @param arg 
	 *            默认填null 
	 * @return true 连接 false 未连接 
	 */  
	public static boolean getMobileDataState(Context pContext, Object[] arg) {  
	  
	    try {  
	  
	        ConnectivityManager mConnectivityManager = (ConnectivityManager) pContext.getSystemService(Context.CONNECTIVITY_SERVICE);  
	  
	        Class<? extends ConnectivityManager> ownerClass = mConnectivityManager.getClass();  
	  
	        Class[] argsClass = null;  
	        if (arg != null) {  
	            argsClass = new Class[1];  
	            argsClass[0] = arg.getClass();  
	        }  
	  
	        Method method = ownerClass.getMethod("getMobileDataEnabled", argsClass);  
	  
	        Boolean isOpen = (Boolean) method.invoke(mConnectivityManager, arg);  
	  
	        return isOpen;  
	  
	    } catch (Exception e) {  
	        // TODO: handle exception  
	  
	        System.out.println("得到移动数据状态出错");  
	        return false;  
	    }
	    }

	public static void setDefaultNetwork(Context mContext,boolean isbind){
//		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			ConnectivityManager mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
//			if(isbind) {
//				Network[] networks = mConnectivityManager.getAllNetworks();
//				if (networks != null) {
//					for (Network network : networks) {
//						System.out.print(network.describeContents());
//						NetworkInfo networkinfo = mConnectivityManager.getNetworkInfo(network);
//						if (networkinfo.getType() == ConnectivityManager.TYPE_WIFI) {
////						ConnectivityManager.setProcessDefaultNetwork(network);
//							mConnectivityManager.bindProcessToNetwork(network);
//						}
//					}
//				}
//			}else{
//				mConnectivityManager.bindProcessToNetwork(null);
//			}
//		}
	}

	public static void setMobileNetworkfromLollipop(Context context,boolean isOn) throws Exception {
		String command = null;
		int state = isOn?1:0;
		try {
			// Get the current state of the mobile network.
//			state = isMobileDataEnabledFromLollipop(context) ? 0 : 1;
			// Get the value of the "TRANSACTION_setDataEnabled" field.
			String transactionCode = getTransactionCode(context);
			// Android 5.1+ (API 22) and later.
			if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
				SubscriptionManager mSubscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
				// Loop through the subscription list i.e. SIM list.
				for (int i = 0; i < mSubscriptionManager.getActiveSubscriptionInfoList().size(); i++) {
					if (transactionCode != null && transactionCode.length() > 0) {
						// Get the active subscription ID for a given SIM card.
						int subscriptionId = mSubscriptionManager.getActiveSubscriptionInfoList().get(i).getSubscriptionId();
						// Execute the command via `su` to turn off
						// mobile network for a subscription service.
						command = "service call phone " + transactionCode + " i32 " + subscriptionId + " i32 " + state;
						executeCommandViaSu(context, "-c", command);
					}
				}
			} else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP) {
				// Android 5.0 (API 21) only.
				if (transactionCode != null && transactionCode.length() > 0) {
					// Execute the command via `su` to turn off mobile network.
					command = "service call phone " + transactionCode + " i32 " + state;
					executeCommandViaSu(context, "-c", command);
				}
			}
		} catch(Exception e) {
			// Oops! Something went wrong, so we throw the exception here.
			throw e;
		}
	}

	private static boolean isMobileDataEnabledFromLollipop(Context context) {
		boolean state = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			state = Settings.Global.getInt(context.getContentResolver(), "mobile_data", 0) == 1;
		}
		return state;
	}
	private static String getTransactionCode(Context context) throws Exception {
		try {
			final TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			final Class<?> mTelephonyClass = Class.forName(mTelephonyManager.getClass().getName());
			final Method mTelephonyMethod = mTelephonyClass.getDeclaredMethod("getITelephony");
			mTelephonyMethod.setAccessible(true);
			final Object mTelephonyStub = mTelephonyMethod.invoke(mTelephonyManager);
			final Class<?> mTelephonyStubClass = Class.forName(mTelephonyStub.getClass().getName());
			final Class<?> mClass = mTelephonyStubClass.getDeclaringClass();
			final Field field = mClass.getDeclaredField("TRANSACTION_setDataEnabled");
			field.setAccessible(true);
			return String.valueOf(field.getInt(null));
		} catch (Exception e) {
			// The "TRANSACTION_setDataEnabled" field is not available,
			// or named differently in the current API level, so we throw
			// an exception and inform users that the method is not available.
			throw e;
		}
	}
	private static void executeCommandViaSu(Context context, String option, String command) {
		boolean success = false;
		String su = "su";
		for (int i=0; i < 3; i++) {
			// Default "su" command executed successfully, then quit.
			if (success) {
				break;
			}
			// Else, execute other "su" commands.
			if (i == 1) {
				su = "/system/xbin/su";
			} else if (i == 2) {
				su = "/system/bin/su";
			}
			try {
				// Execute command as "su".
				Runtime.getRuntime().exec(new String[]{su, option, command});
			} catch (IOException e) {
				success = false;
				// Oops! Cannot execute `su` for some reason.
				// Log error here.
			} finally {
				success = true;
			}
		}
	}

	public static void toggleData(boolean ison){
//		String cmd="svc data "+(ison?"enable":"disable");
//		excuteCmd(cmd);
		String cmd[]=new String[3];
		cmd[0]="svc";
		cmd[1]="data";
		cmd[2]=ison?"enable":"disable";
		try {
			execute(cmd,"/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param type wifi  data
     */
	public static void setPrefer(String type){
		String[] cmd=new String[3];
		cmd[0]="svc";
		cmd[1]=type;
		cmd[2]="prefer";
		try {
			execute(cmd,"/");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void excuteCmd(String cmd){
		Runtime runtime=Runtime.getRuntime();
		try {
			Process proc = runtime.exec(cmd);
			//如果有参数的话可以用另外一个被重载的exec方法
			//实际上这样执行时启动了一个子进程,它没有父进程的控制台
			//也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			// read the ls output
			String line = "";
			StringBuilder sb = new StringBuilder(line);
			while ((line = bufferedreader.readLine()) != null) {
				//System.out.println(line);
				sb.append(line);
				sb.append('\n');
			}
			if (proc.waitFor() != 0) {
				System.err.println("exit value = " + proc.exitValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static String execute ( String [] cmmand,String directory)
			throws IOException {
		String result = "" ;
		try {
			ProcessBuilder builder = new ProcessBuilder(cmmand);

			if ( directory != null )
				builder.directory ( new File( directory ) ) ;
			builder.redirectErrorStream (true) ;
			Process process = builder.start ( ) ;

			//得到命令执行后的结果
			InputStream is = process.getInputStream ( ) ;
			byte[] buffer = new byte[1024] ;
			while ( is.read(buffer) != -1 ) {
				result = result + new String (buffer) ;
			}
			is.close ( ) ;
		} catch ( Exception e ) {
			e.printStackTrace ( ) ;
		}
		return result ;
	}

	public static   void openAPN(Context context){
		List<APN> list = getAPNList(context);
		Uri uri = Uri.parse("content://telephony/carriers");
		for (APN apn : list) {
			ContentValues cv = new ContentValues();
			cv.put("apn", APNMatchTools.matchAPN(apn.apn));
			cv.put("type", APNMatchTools.matchAPN(apn.type));
			context.getContentResolver().update(uri, cv, "_id=?", new String[]{apn.id});
		}
	}
	public static void closeAPN(Context context){
		List<APN> list = getAPNList(context);
		Uri uri = Uri.parse("content://telephony/carriers");
		for (APN apn : list) {
			ContentValues cv = new ContentValues();
			cv.put("apn", APNMatchTools.matchAPN(apn.apn)+"mdev");
			cv.put("type", APNMatchTools.matchAPN(apn.type)+"mdev");
			context.getContentResolver().update(uri, cv, "_id=?", new String[]{apn.id});
		}
	}
	private static List<APN> getAPNList(Context context){
		String tag = "Main.getAPNList()";
//current不为空表示可以使用的APN
		String  projection[] = {"_id,apn,type,current"};
		Uri uri = Uri.parse("content://telephony/carriers");
		Cursor cr = context.getContentResolver().query(uri, projection, null, null, null);
		List<APN> list = new ArrayList<APN>();
		while(cr!=null && cr.moveToNext()){
			Log.d(tag, cr.getString(cr.getColumnIndex("_id")) + "  " + cr.getString(cr.getColumnIndex("apn")) + "  " + cr.getString(cr.getColumnIndex("type"))+ "  " + cr.getString(cr.getColumnIndex("current")));
			APN a = new APN();
			a.id = cr.getString(cr.getColumnIndex("_id"));
			a.apn = cr.getString(cr.getColumnIndex("apn"));
			a.type = cr.getString(cr.getColumnIndex("type"));
			list.add(a);
		}
		if(cr!=null)
			cr.close();
		return list;
	}
	public static class APN{
		String id;
		String apn;
		String type;
	}
}
