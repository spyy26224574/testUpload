package com.adai.gkdnavi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkd.bean.MessageBean;
import com.adai.gkd.db.MessageCenterDao;
import com.adai.gkdnavi.fragment.LocalVoicePhotoFragment;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MovieRecord;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressLint("HandlerLeak") 
public class LocalPhotoActivity extends FragmentActivity {

	protected static final String TAG = "LocalPhotoActivity";
	protected static final long INTERVALTIME = 1000*3;//间隔时间
	private static final String KEYVALUE = "LocalVoicePhotoFragment";
	private boolean mHasMobileNetwork=false;//移动数据网络的状态
	private ImageButton mLocalPhoto;
	private FragmentManager mFragmentManager;
	private LinearLayout mLinearLayout;
	private WifiManager mWifiManager;
	private ConnectivityManager mConnectivityManager;
	private LinkWifi mLinkWifi;
	private ProgressDialog mProgressDialog;
	private long startTime,endTime;
	private static final int START = 0;
	private static final int END = 1;
	private static final int RECORDING = 2;
	private static final int SHOWDATA = 3;
	private static final int NETWORKERROR=4;
	private static final int VOLLEYTIMEOUT = 5000;
	private static final String RECORD = "record";
	private static final String MOVING = "moving";
	private int iCurrentWifiNetID = -1;
	private boolean isShowAlertDialog = false;//确保弹出停止录制视频的窗口只显示一次
	private boolean mFlag= false;
	private int count = 0;//定义下载成功的次数
	private List<FileDomain> listget = new ArrayList<FileDomain>();
	private int accessIntentCount=0;
	private VoiceTimeDB mVoiceTimeDB;
	private MessageCenterDao messageCenterDao;
	private String stringExtra;
	private ArrayList<FileDomain> arrayList;
	private AlertDialog mShowRecordDialog;
	private Socket socket;
    private VLCApplication app;
    private int netmode;
	private Handler msgHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START://查询摄像头的状态
				++accessIntentCount;
                    sendCmdForResult(Contacts.URL_QUERY_CURRENT_STATUS);
				break;
			case END:
				//连接手动连接wifi
				mProgressDialog.dismiss();
				if (!mFlag) {
					showAlertDialog();
				}
				break;
			case RECORDING:
				//显示停止录制
				mProgressDialog.dismiss();
				if (!mFlag) {
					showRecordingAlertDialog();
				}
//				showRecordingAlertDialog();
				break;
			case SHOWDATA:
				//显示数据
                    accessInternet(Contacts.URL_FILE_LIST);
				
				break;
			case NETWORKERROR:
				mProgressDialog.dismiss();
				Toast.makeText(getApplicationContext(), getString(R.string.request_failed), Toast.LENGTH_SHORT).show();
				((VLCApplication)getApplication()).resetRequestQueue();
				break;
			default:
				break;
			}
		};
	};
	private String mIsDownload;
	private MessageBean message;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo);
		File destDir = new File(VLCApplication.DOWNLOADPATH);
		if (!destDir.exists()) {
			destDir.mkdirs();
		}
        app = (VLCApplication) getApplication();
        netmode = getCurrentNetModel();
		stringExtra = this.getIntent().getStringExtra("TIME");
		mIsDownload = this.getIntent().getStringExtra("DOWNLOAD");
		String endtimeExtra=getIntent().getStringExtra("endTime");
		message=(MessageBean) getIntent().getSerializableExtra("message");
		mIsDownload=message.isread==1?"true":"false";
		SimpleDateFormat newformat=new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		try {
			startTime =newformat.parse(message.starttime).getTime();
			endTime=newformat.parse(message.endtime).getTime();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
//		SimpleDateFormat format=new SimpleDateFormat("yyyyMMddHHmmss");
//		String time=format.format(new Date(startTime));
//		try {
//			Date timedate = format.parse(time);
//			startTime=timedate.getTime();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		initView();
		mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mConnectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		mLinkWifi = new LinkWifi(LocalPhotoActivity.this);
		if(VoiceManager.getMobileDataState(this, null)){
			VoiceManager.setMobileData(this, false);
			mHasMobileNetwork = true;
		} 
        if (netmode == 0) {
		if(!mWifiManager.isWifiEnabled()){
			mWifiManager.setWifiEnabled(true);
            }
		}
//		if(!mWifiManager.isWifiEnabled()){
////			PermissionUtils.checkPermission(this, permission.CHANGE_WIFI_STATE, true);
////			int code=PermissionChecker.checkCallingPermission(this, permission.CHANGE_WIFI_STATE, getPackageName());
//			PermissionUtils.showPermissionDialog(this, permission.CHANGE_WIFI_STATE);
//		}
		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mProgressDialog.setCanceledOnTouchOutside(false);
		mProgressDialog.setCancelable(true);// 设置是否可以通过点击Back键取消 
		mProgressDialog.show();
		mProgressDialog.setMessage(getResources().getString(R.string.wifi_connecting));
		
		if (message.isread==1) {
			mProgressDialog.dismiss();
			initData();
		} else {
			VoiceManager.setDefaultNetwork(LocalPhotoActivity.this,true);
//			try {
//				VoiceManager.setMobileNetworkfromLollipop(LocalPhotoActivity.this,false);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			checkWifi();
//			connectWifi();
		}
		mVoiceTimeDB = new VoiceTimeDB(LocalPhotoActivity.this);
		messageCenterDao=new MessageCenterDao(this);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if ("true".equals(mIsDownload)) {
		} else {
			VoiceManager.setDefaultNetwork(LocalPhotoActivity.this,true);
			msgHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					checkWifi();
				}
			},300);
		}
		//checkWifi();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFlag=true;
//		String url = "http://192.168.1.254/?custom=1&cmd=2001&par=";
//		String URL = "http://192.168.1.254/?custom=1&cmd=2006&par=";
//		sendCmd(URL + SpUtils.getString(LocalPhotoActivity.this, MOVING, "0"));
//    	sendCmd(url + SpUtils.getString(LocalPhotoActivity.this, RECORD, "1"));
		//sendCmd(url);
		//unregisterReceiver(dismissDialog);
//		try {
//			VoiceManager.setMobileNetworkfromLollipop(LocalPhotoActivity.this,true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		VoiceManager.setDefaultNetwork(LocalPhotoActivity.this,false);
	}
	
	private void restartRecord(){
        String url = Contacts.URL_MOVIE_RECORD;
//		String URL = "http://192.168.1.254/?custom=1&cmd=2006&par=";
//		sendCmd(URL + SpUtils.getString(LocalPhotoActivity.this, MOVING, "0"));
    	sendCmd(url + SpUtils.getString(LocalPhotoActivity.this, RECORD, "1"));
	}
	private void initView() {
		mLinearLayout = (LinearLayout) findViewById(R.id.activity_photo_ll_button);
		mLocalPhoto = (ImageButton) findViewById(R.id.activity_photo_ib_photo);
		mLocalPhoto.setImageDrawable(getResources().getDrawable(R.drawable.photo_selected));
	}

	private void initData() {
		mFragmentManager = getSupportFragmentManager();
		LocalVoicePhotoFragment localVoicePhotoFragment = new LocalVoicePhotoFragment();
		mFragmentManager.beginTransaction().replace(R.id.activity_photo_fl_container, localVoicePhotoFragment).commit();
		
	}
	
	private void showRecordingAlertDialog() {
		if (!isShowAlertDialog) {
			isShowAlertDialog = true;
			mShowRecordDialog = new AlertDialog.Builder(this).create();
			mShowRecordDialog.setTitle(getString(R.string.notice));
			mShowRecordDialog.setMessage(getString(R.string.wheter_stop_record));
			mShowRecordDialog.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.ok),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isShowAlertDialog = false;
							dialog.dismiss();
							mProgressDialog.setMessage(LocalPhotoActivity.this.getString(R.string.msg_center_stop_recording));
                            sendStopRecordCmd(Contacts.URL_MOVIE_RECORD + "0");
//							sendCmd("http://192.168.1.254/?custom=1&cmd=2006&par=0");
//							Message message = Message.obtain();
//							message.what = SHOWDATA;
//							msgHandler.sendMessage(message);
							mProgressDialog.show();
//							accessInternet("http://192.168.1.254/?custom=1&cmd=3015");
						}
					});
			mShowRecordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,getString(R.string.cancel),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							isShowAlertDialog = false;
							dialog.dismiss();
							LocalPhotoActivity.this.finish();
						}
					});
			mShowRecordDialog.setCancelable(false);
			mShowRecordDialog.show();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//mFlag=true;
		if (mShowRecordDialog!=null) {
			mShowRecordDialog.dismiss();
		}
		
	}
	
    Connectthread connectthread = new Connectthread();
	 private void showAlertDialog() {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getString(R.string.notice));
        if (netmode == 0) {
            builder.setMessage(getString(R.string.wifi_checkmessage));
        } else {
            builder.setMessage(getString(R.string.ap_checkmessage));
        }
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (netmode == 0) {
                            //							Intent intent = new Intent(LocalPhotoActivity.this, WifiConnectActivity.class);
//							startActivity(intent);

                            WifiUtil.getInstance().gotoWifiSetting(LocalPhotoActivity.this);

                        } else {
                            WifiUtil.getInstance().startAP(LocalPhotoActivity.this);
                            connectthread.start();
                        }


                    }
                });
        builder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        LocalPhotoActivity.this.finish();
                    }
                });
        builder.setCancelable(false);
        builder.create().show();
    }

    class Connectthread extends Thread {

        @Override
        public void run() {
            if (app.getApisConnect()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        checkWifi();
                    }
                });

            } else {
                msgHandler.postDelayed(connectthread, 500);
            }


        }

    }

//	 private void autoconnect(){
//		mProgressDialog.show();
//		WifiUtil.getInstance().setConnectInfo(new WifiUtil.WifiConnectInfo() {
//
//			@Override
//			public void onScaning() {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onNotfound() {
//				// TODO Auto-generated method stub
//				sendHanleInitMsg(END);
//			}
//
//			@Override
//			public void onError() {
//				// TODO Auto-generated method stub
//				sendHanleInitMsg(END);
//			}
//
//			@Override
//			public void onConnecting() {
//				// TODO Auto-generated method stub
//
//			}
//
//			@Override
//			public void onConnectedFailed() {
//				// TODO Auto-generated method stub
//				sendHanleInitMsg(END);
//			}
//
//			@Override
//			public void onConnected() {
//				// TODO Auto-generated method stub
//				Message msg = msgHandler.obtainMessage();
//				msg.what=START;
//				msgHandler.sendMessageDelayed(msg,2000);
////				sendHanleInitMsg(START);
//			}
//		});
//		WifiUtil.getInstance().connectWIfi(this);
//	}

//	private void connectWifi(){
//		mProgressDialog.show();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				int iTargetNetID=SpUtils.getInt(getApplicationContext(), "NEYWORKID", -1);
//				if(-1 == iTargetNetID)
//				{
//					sendHanleInitMsg(END);
//					return;
//				}
//				iCurrentWifiNetID = mWifiManager.getConnectionInfo().getNetworkId();
//				if(iCurrentWifiNetID==iTargetNetID){
//					sendHanleInitMsg(START);
//					return;
//				}
//				if(-1 != iCurrentWifiNetID)
//					disConnectionWifi(iCurrentWifiNetID); //disable current wifi
//
//				List<WifiConfiguration> configs = mWifiManager.getConfiguredNetworks();
//				if(configs!=null&&configs.size()>0){
//					for (WifiConfiguration wifiConfiguration : configs) {
//						if(wifiConfiguration.networkId==iTargetNetID){
//							if(mLinkWifi.ConnectToNetID(iTargetNetID))
//							{
//								iCurrentWifiNetID=iTargetNetID;
//								isWifiStable(wifiConfiguration.SSID,iTargetNetID);
//							}
//							else
//							{
//								sendHanleInitMsg(END);
//							}
//							return;
//						}
//					}
//				}
//				sendHanleInitMsg(END);
//			}
//		}).start();
//	}

    private void checkWifi() {
        // 创建socket对象,指定服务器端地址和端口号
        new Thread(new Runnable() {
            //Socket socket = null;
            @Override
            public void run() {
                try {
//					NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
//					if (activeNetworkInfo != null) {
//						int type = activeNetworkInfo.getType();
//						if (type != ConnectivityManager.TYPE_WIFI) {
//							Message msg = Message.obtain();
//							msg.what = END;
//							nHandler.sendMessage(msg);
//							// toggleMobileData(CameraConstant.this, false);
//							// SystemClock.sleep(3000);
//							// checkNetwork();
//						} else {
//							checkNetwork();
//						}
//					} else 
					{
					//Log.e(TAG,"socket start");
//					socket = new Socket("192.168.1.254", 3333);
//					Log.e(TAG,"new Socket 192.168.1.254 3333");
//					//SocketAddress socAddress = new InetSocketAddress("192.168.1.254", 3333);
// 					socket.setSoTimeout(5000);
                        if (!WifiUtil.checkNetwork(LocalPhotoActivity.this, 1)) {
                            sendHanleInitMsg(END);
                            return;
                        }
                        //Log.e(TAG,"socket 1.54 successs 001!!!!!!");
//					WifiInfo info = mWifiManager.getConnectionInfo();
//					if (info!=null) {
//						int networkId = info.getNetworkId();
//						if(networkId==-1){
//							sendHanleInitMsg(END);
//							return;
//						}
//						String bssid = info.getBSSID();
//						String ssid=info.getSSID();
//						SpUtils.putString(getApplicationContext(), "SSID", ssid);
//						SpUtils.putInt(getApplicationContext(), "NEYWORKID", networkId);
//						SpUtils.putString(getApplicationContext(), "BSSID", bssid);
//					}else{
//						sendHanleInitMsg(END);
//						return;
//					}
                        Message msg = Message.obtain();
                        msg.what = START;
                        msgHandler.sendMessage(msg);
                        //Log.e(TAG,"socket 1.54 successs 002!!!!!!");
                    }

                } catch (Exception e) {
                    //Log.e(TAG,"IOException e");
                    sendHanleInitMsg(END);
//					runOnUiThread(new Runnable() {
//						public void run() {
////							connectWifi();
////							autoconnect();
//							WifiUtil.getInstance().gotoWifiSetting(LocalPhotoActivity.this);
//						}
//					});
//					e.printStackTrace();
//					{
//					iCurrentWifiNetID = mWifiManager.getConnectionInfo().getNetworkId();
//					if(-1 != iCurrentWifiNetID)
//						disConnectionWifi(iCurrentWifiNetID); //disable current wifi
//						//Log.e(TAG, "disConnectionWifi(iCurrentWifiNetID)="+iCurrentWifiNetID);
//					int iTargetNetID=SpUtils.getInt(getApplicationContext(), "NEYWORKID", -1);
//					//Log.e(TAG, "checkwifi iTargetNetID = " + iTargetNetID);
//					if(-1 == iTargetNetID)
//					{
//						sendHanleInitMsg(END);
//						return;
//					}
//					mWifiManager.startScan();
//					//SystemClock.sleep(1000);
//					List<ScanResult> scanResults = mWifiManager.getScanResults();
//					//Log.e(TAG, "scanResults.size()="+scanResults.size());
//					if(0 != scanResults.size())
//					{
//						
//						//Log.e(TAG, "checkWifi() 00a scanResults.size " +  scanResults.size());
//						for(ScanResult scanResult : scanResults)
//						{
//							
//							if(scanResult.BSSID.equalsIgnoreCase(SpUtils.getString(getApplicationContext(), "BSSID", "")))
//							{
//								//Log.e(TAG, "checkWifi() 00b target BSSID = " + scanResult.BSSID + ",name =" + scanResult.SSID
//								//		+ ",iTargetNetID = "  + iTargetNetID);
//								
//								//boolean bNetIDConed = mLinkWifi.ConnectToNetID(iTargetNetID);
//								//Log.e(TAG, "mLinkWifi.ConnectToNetID(iTargetNetID)="+mLinkWifi.ConnectToNetID(iTargetNetID));
//								if(mLinkWifi.ConnectToNetID(iTargetNetID))
//								{
//									isWifiStable(scanResult.SSID,iTargetNetID);
//									//Log.e(TAG, "isWifiStable(scanResult.SSID,iTargetNetID)="+isWifiStable(scanResult.SSID,iTargetNetID));
//								}
//								else
//								{
//									sendHanleInitMsg(END);
//								}
//								return;
//							}
//						}
//						
//						sendHanleInitMsg(END);
//					}
//				  }
				}
//				finally{
//					try {
//						if (socket!=null) {
//							socket.close();
//						}
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
			}
		}).start();
	}
	private void sendCmdForResult(String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						DomParseUtils domParseUtils = new DomParseUtils();
						int i = domParseUtils.parastr(response);
						HashMap<String, String> map = domParseUtils.hMap;
						Iterator<Entry<String, String>> it = map.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<String, String> entry = it.next();
							String key = entry.getKey();
							String value = entry.getValue();
							//Log.e(TAG, ""+accessIntentCount);
							if ("2006".equals(key)) {
//								if (accessIntentCount==1) {
//									if ("1".equals(value)) {
//										SpUtils.putString(LocalPhotoActivity.this,MOVING, "1");
//									} else {
//										SpUtils.putString(LocalPhotoActivity.this,MOVING, "0");
//									}
//								}
							}else if ("2016".equals(key)) {
								if (accessIntentCount == 1) {
									if ("1".equals(value)) {
										// 录制
										SpUtils.putString(LocalPhotoActivity.this, RECORD, "1");
										Message msg = Message.obtain();
										msg.what = RECORDING;
										msgHandler.sendMessage(msg);
										Log.e(TAG, "value="+value+",RECORDING");
									} else {
										// 非录制
										SpUtils.putString(LocalPhotoActivity.this, RECORD, "0");
										Message msg = Message.obtain();
										msg.what = SHOWDATA;
										msgHandler.sendMessage(msg);
										Log.e(TAG, "value="+value+",SHOWDATA");
									}
								}
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						//VolleyLog.e("Error: ", error.getMessage());
						//Log.e(TAG, "Error: " + error.getMessage());
//						int statuscode=error.networkResponse.statusCode;
						msgHandler.sendEmptyMessage(NETWORKERROR);
					}
				});
		req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		VLCApplication.getInstance().addToRequestQueue(req);
	}
	
	private void sendCmd(final String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
							DomParseUtils domParseUtils=new DomParseUtils();
							MovieRecord record = domParseUtils.getParserXml(is);
							if (record!=null && record.getStatus().equals("0") && record.getCmd().equals("2001")) {
//								mWifiManager.disableNetwork(SpUtils.getInt(getApplicationContext(), "NEYWORKID", -1));
//								mWifiManager.disconnect();
//								mWifiManager.disableNetwork(SpUtils.getInt(getApplicationContext(), "NEYWORKID", -1));
//								if (mHasMobileNetwork) {
//									VoiceManager.setMobileData(getApplicationContext(), true);
//								}
								if (url.contains("cmd=2001&par=1")) {
									//Toast.makeText(getApplicationContext(), "cmd=2001&par=1", Toast.LENGTH_SHORT).show();
								}else if (url.contains("cmd=2001&par=0")) {
									//Toast.makeText(getApplicationContext(), "cmd=2001&par=0", Toast.LENGTH_SHORT).show();
								}
							}else {
								//Toast.makeText(getApplicationContext(), response, 1).show();
							}
							
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						//VolleyLog.e("Error: ", error.getMessage());
						//Log.e(TAG, "Error: " + error.getMessage());
						msgHandler.sendEmptyMessage(NETWORKERROR);
					}
				});
		req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		VLCApplication.getInstance().addToRequestQueue(req);
	}
	
	private void disConnectionWifi(int netId){
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
	
	private void sendHanleInitMsg(int iMsg){
		Message msg = Message.obtain();
		msg.what = iMsg;
		msgHandler.sendMessage(msg);
    }
	
	//判断wifi是否连接稳定,true is connted OK ,
    private boolean isWifiStable(String strTargetSSID,int iTargetNetID)
    {
    	int iCountTryConn = 0;
    	WifiInfo connectionWifiInfo = null;
    	boolean bTargetNetConned = false;
		do
		{
			if(50 >= iCountTryConn++)
				SystemClock.sleep(300);
			else
				break;
			NetworkInfo networkInfo = mConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			bTargetNetConned = networkInfo.isConnected();
			//Log.e(TAG, "connectWifi = " + iCountTryConn + ",bTargetNetConned = "+ bTargetNetConned);
		}while(!bTargetNetConned);
		
		if (51 > iCountTryConn) {
			connectionWifiInfo = mWifiManager.getConnectionInfo();
			//String sSID = mScanResult.SSID;
//			String strCurrentSSID = connectionWifiInfo.getSSID().replace("\"","");
			//Log.e(TAG,"strCurrentSSID =" + strCurrentSSID + ",strTargetSSID =" + strTargetSSID);
			if (iTargetNetID==connectionWifiInfo.getNetworkId()) {
				//Toast.makeText(this,	"连接上" + connectionWifiInfo.getSSID(),Toast.LENGTH_SHORT).show();
				//Log.e(TAG,"connted successfull and estable,now begain updateSize cmos");
				sendHanleInitMsg(START);
				return true;
			}
		}
		//Log.e(TAG,"connted failture");
		mWifiManager.disableNetwork(iTargetNetID);
		//SpUtils.clearDataBuf(getApplicationContext());
		sendHanleInitMsg(END);
    	return false;
    }
    
    @SuppressLint("SimpleDateFormat") 
    private void accessInternet(String url) {
    	mProgressDialog.setMessage(getString(R.string.msg_center_requst_files));
    	StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {

					@Override
					public void onResponse(String response) {
						arrayList = new ArrayList<FileDomain>();
						arrayList.clear();
						try {
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
							InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
							DomParseUtils dom = new DomParseUtils();
							listget = dom.parsePullXml(is);
							
							String re=".*[0-9]{4}_[0-9]{4}_[0-9]{6}.*";
							for (int i = 0; i < listget.size(); i++) {
								FileDomain file = listget.get(i);
								if(file.getName()==null||!file.getName().matches(re))continue;
								String fpath = file.getFpath();

								int indexPoint = fpath.lastIndexOf('\\');
								String fileStart = fpath.substring(0, indexPoint);
								Log.e("imageaaaaaaaaaaaa", fpath);
								if (fileStart.equalsIgnoreCase("A:\\CARDV\\PHOTO")) {
									if (file.getName().contains("JPG") && file.getSize() < 1024*1024 ) {
//										Log.e("imageeeeeeeeeeeeeeee", file.fpath);
										try{
											String substring = file.getName().substring(0, file.getName().lastIndexOf("_")).replace("_", "");
											long filetTime = sdf.parse(substring).getTime();
											if (filetTime <= (endTime + 1000) && filetTime >= startTime&&arrayList.size()<5) {
												arrayList.add(file);
											}
										}catch(Exception e){
											e.printStackTrace();
										}
									}
								}
							}
							
							Collections.sort(arrayList, new Comparator<FileDomain>() {

								@Override
								public int compare(FileDomain lhs,
										FileDomain rhs) {
									long timeCode = lhs.timeCode;
									long timeCode2 = rhs.timeCode;
									if (timeCode < timeCode2) {
										return 1;
									}
									return -1;
								}
							});
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						} catch (Exception e) {
							e.printStackTrace();
						}
						SystemClock.sleep(1500);
						if (arrayList.size()==0) {
							mProgressDialog.setMessage(getString(R.string.msg_center_no_download_files));
						}else {
							Iterator<FileDomain> iterators = arrayList.iterator();
							while (iterators.hasNext()) {
								FileDomain fileDomain = (FileDomain) iterators.next();
								if (arrayList.size() > count) {
									mProgressDialog.setMessage(getString(R.string.msg_center_downloading_files));
									download(fileDomain,arrayList.size());
								}
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						//VolleyLog.e("Error: ", error.getMessage());
						//Log.e(TAG, "Error: " + error.getMessage());
						msgHandler.sendEmptyMessage(NETWORKERROR);
					}
				});
		req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		VLCApplication.getInstance().addToRequestQueue(req);
	}
    
    private  synchronized void download(final FileDomain fileDomain , final int i){
        HttpUtils http = new HttpUtils();
        String url = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO + fileDomain.getName();
        String path = new File(VLCApplication.DOWNLOADPATH, fileDomain.getName()).getAbsolutePath();
        http.download(url, path, new RequestCallBack<File>() {
			
			@Override
			public void onSuccess(ResponseInfo<File> responseInfo) {
				//Toast.makeText(getApplicationContext(), "成功"+ fileDomain.getName(), 0).show();
				++count;
				if (i == count) {
					mProgressDialog.dismiss();
//					mVoiceTimeDB.insert(stringExtra);
					messageCenterDao.updateMessage(message);
					initData();
					restartRecord();
				}
			}
			
			@Override
			public void onFailure(HttpException error, String msg) {
				//Toast.makeText(getApplicationContext(), "失败"+ fileDomain.getName(), 0).show();
			}
		});
    }

    /**
     * 获取与摄像头连接类型
     * 0为摄像头起AP，1为手机端起AP
     *
     * @return
     */

    public int getCurrentNetModel() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(this);
        return shareprefrence.getInt("netmode", 0);
    }
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (mLinearLayout.getVisibility() == (View.GONE)) {
				mLinearLayout.setVisibility(View.VISIBLE);
				Intent intent = new Intent(KEYVALUE);
				sendBroadcast(intent);
			} else {
				finish();
			}
			break;
		}
		return true;
	}
    
    private void sendStopRecordCmd(String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
							DomParseUtils domParseUtils=new DomParseUtils();
							MovieRecord record = domParseUtils.getParserXml(is);
							if (record!=null && record.getStatus().equals("0") && record.getCmd().equals("2001")) {
//								sendCmdForStopMoving("http://192.168.1.254/?custom=1&cmd=2006&par=0");
                                accessInternet(Contacts.URL_FILE_LIST);
							}else {
								//Toast.makeText(getApplicationContext(), response, 1).show();
							}
							
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						//VolleyLog.e("Error: ", error.getMessage());
						//Log.e(TAG, "Error: " + error.getMessage());
						msgHandler.sendEmptyMessage(NETWORKERROR);
					}
				});
		req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
		VLCApplication.getInstance().addToRequestQueue(req);
	}
    
}
