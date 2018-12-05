package com.adai.gkdnavi;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.utils.SpUtils;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;

public class CamSetNameActivity extends Activity implements OnClickListener {
	private String XCTag = "CamSetNameActivity";
	private Button ibSave; // save icon
	private ImageButton tvBack;
	private TextView tvSSIDName; // ssid name
	String strhttpCMD; // http command line
	String strCurrSSIDName; // current conected ssid name
	//String strSetCamSSID = Contacts.URL_SET_SSID;
	private WifiManager localWifiManager;// 提供Wifi管理的各种主要API，主要包含wifi的扫描、建立连接、配置信息等
	private WifiInfo wifiConnectedInfo;// 已经建立好网络链接的信息
	private boolean isRecording = false; // false -no recording

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		//Log.e(XCTag, "oncreate.......");
		setContentView(R.layout.activity_camsetname);
		initView();

	}

	private void initView() {
		// TODO Auto-generated method stub
		ibSave = (Button) findViewById(R.id.cam_namesave);
		tvBack = (ImageButton) findViewById(R.id.cam_nameback);
		tvSSIDName = (TextView) findViewById(R.id.cam_ssidname);
		// tvSSIDName.setOnClickListener(this);
		ibSave.setOnClickListener(this);
		tvBack.setOnClickListener(this);
		strCurrSSIDName = httpGetCamName();
		tvSSIDName.setText(strCurrSSIDName);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.cam_nameback: // camer name
			CamSetNameActivity.this.finish();
			break;
		case R.id.cam_namesave:
			// new Thread(new Runnable() {
			//
			// @Override
			// public void run() {
			// // TODO Auto-generated method stub

			EditText editSSIDName = (EditText) findViewById(R.id.camnameedit);
			String strSSIDName = editSSIDName.getText().toString();
			if (strSSIDName.equals(tvSSIDName)) {
				// Toast.makeText(this, "SSID is no change", 0).show();
				Log.e(XCTag, "ssid is no changed..");
			} else if (0 == strSSIDName.length() || strSSIDName.isEmpty()
					|| strSSIDName.equalsIgnoreCase("false")) {
				// Toast.makeText(this, "SSID 不能为空", 0).show();
				Log.e(XCTag, "SSID 不能为空..");
			} else {
				// Toast.makeText(this, "start change ssid" +
				// strSSIDName , 0).show();
				Log.e(XCTag, "start change ssid to =." + strSSIDName);
				httpSetCamName();
			}
			// }
			// }).start();
			break;
		default:
			break;
		}
	}

	// 使用此函数，假定wifi已正确连接到摄像头的热点，否则不允许使用。
	private String httpGetCamName() {
		String strSSID = null;
		localWifiManager = (WifiManager) this.getSystemService(this
				.getApplicationContext().WIFI_SERVICE);
		wifiConnectedInfo = localWifiManager.getConnectionInfo();
		strSSID = wifiConnectedInfo.getSSID();
		return strSSID;

	}

//	private void camStopDialog(final String strInSSIDName) {
//		AlertDialog.Builder altBuilder = new Builder(CamSetNameActivity.this);
//		altBuilder.setMessage(getString(R.string.wifi_stoprecordalert));
//		altBuilder.setTitle(getString(R.string.wifi_stopwarning));
//		altBuilder.setPositiveButton(R.string.wifi_stopconfirm,
//				new DialogInterface.OnClickListener() {
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						// stop cam recording,cmd = 2001&par=0
//						sendcmd("http://192.168.1.254/?custom=1&cmd=2001&par=0");
//						// change ssid name
//						strhttpCMD = strSetCamSSID + strInSSIDName;
//						sendcmd(strhttpCMD);
//						// must reconnected
//						strhttpCMD = "http://192.168.1.254/?custom=1&cmd=3018";
//						// check ssid
//						strCurrSSIDName = httpGetCamName();
//						Log.e(XCTag, "current ssid name = " + strCurrSSIDName);
//					}
//				});
//
//		altBuilder.setNeutralButton(getString(R.string.wifi_stopcancel),
//				new DialogInterface.OnClickListener() {
//
//					@Override
//					public void onClick(DialogInterface arg0, int arg1) {
//						// TODO Auto-generated method stub
//						// return
//					}
//				});
//
//		altBuilder.create().show();
//	}

	private void httpSetCamName() {
		// change ssid name
		EditText editSSIDName = (EditText) findViewById(R.id.camnameedit);
		String strSSIDName = editSSIDName.getText().toString();
		strhttpCMD = Contacts.URL_SET_SSID + strSSIDName;
		sendcmd(strhttpCMD);
		// must reconnected
		SystemClock.sleep(1000);
		sendcmd(Contacts.URL_RECONNECT_WIFI);
		// check ssid
		strCurrSSIDName = httpGetCamName();
		Log.e(XCTag, "current ssid name = " + strCurrSSIDName);
	}

	private void sendcmd(final String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						if (url.equals(strhttpCMD)) {
							Toast.makeText(
									CamSetNameActivity.this,
									getResources()
											.getString(
													R.string.wifi_name_modified_successfully),
									Toast.LENGTH_SHORT).show();
							SpUtils.putInt(getApplicationContext(),
									"NEYWORKID", -1);
							SpUtils.putString(getApplicationContext(), "BSSID",
									"");

							new Handler().postDelayed(new Runnable() {

								public void run() {
									// execute the task
									CamSetNameActivity.this.finish();
								}

							}, 2000);

						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						Log.e("9527", "Error: " + error.getMessage());
						if (url.equals(strhttpCMD)) {
							Toast.makeText(
									CamSetNameActivity.this,
									getResources().getString(
											R.string.wifi_name_modified_fail),
									Toast.LENGTH_SHORT).show();

						}
					}
				});

		VLCApplication.getInstance().addToRequestQueue(req);

	}

}
