package com.adai.gkdnavi;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class CamSetAdvActivity extends Activity implements OnClickListener {
	private String XCTag = "CamSetAdvActivity";
	private RelativeLayout LineCamSensorLevel;
	private RelativeLayout LineCamTvModel;
	private RelativeLayout LineCamFileTime;
	private RelativeLayout LineCamsdformat;
	private RadioGroup mGsensorSettingRadioGroup;
	private RelativeLayout mGsensorSetting;
	private RadioGroup mTvmodelSettingRadioGroup;
	private RelativeLayout mTvmodelSetting;
	private RadioGroup mFiletimeSettingRadioGroup;
	private RelativeLayout mFiletimeSetting;
	private Button CamFactory;

	private TextView cam_sensor_level;
	private TextView cam_palntsc;
	private TextView cam_filetime;

	String value_filetime;
	String value_sensorLevel;
	String value_tvModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_camsetadv);
        initView();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getStatus();
	}

	private void initView() {
		// TODO Auto-generated method stub
		LineCamSensorLevel = (RelativeLayout) findViewById(R.id.camadvset_gsensor);
		LineCamTvModel = (RelativeLayout) findViewById(R.id.camadvset_tvmodel);
		LineCamFileTime = (RelativeLayout) findViewById(R.id.camadvset_filetime);
		LineCamsdformat = (RelativeLayout) findViewById(R.id.camadvset_sdformat);
		mGsensorSetting = (RelativeLayout) findViewById(R.id.gsensor_setting);
		mGsensorSettingRadioGroup = (RadioGroup) findViewById(R.id.gsensor_setting_radiogroup);
		mTvmodelSetting = (RelativeLayout) findViewById(R.id.tvmodel_setting);
		mTvmodelSettingRadioGroup = (RadioGroup) findViewById(R.id.tvmodel_setting_radiogroup);
		mFiletimeSetting = (RelativeLayout) findViewById(R.id.filetime_setting);
		mFiletimeSettingRadioGroup = (RadioGroup) findViewById(R.id.filetime_setting_radiogroup);

		cam_sensor_level = (TextView) findViewById(R.id.cam_sensor_level);
		cam_palntsc = (TextView) findViewById(R.id.cam_palntsc);
		cam_filetime = (TextView) findViewById(R.id.cam_filetime);

		mGsensorSettingRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						// 设置录像质量
						case R.id.gsensor_off:
                                String Gsensoroff = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY + "0";
							sendcmd(Gsensoroff);
							cam_sensor_level.setText(R.string.close);
							break;
						case R.id.gsensor_low:
                                String Gsensorlow = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY + "1";
							sendcmd(Gsensorlow);
							cam_sensor_level.setText(R.string.low);
							break;
						case R.id.gsensor_med:
                                String Gsensormed = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY + "2";
							sendcmd(Gsensormed);
							cam_sensor_level.setText(R.string.middle);
							break;
						case R.id.gsensor_high:
                                String Gsensorhigh = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY + "3";
							sendcmd(Gsensorhigh);
							cam_sensor_level.setText(R.string.high);
							break;
						}

					}

				});

		mTvmodelSettingRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						// set recording color mode
						case R.id.tvmodel_ntsc:
                                String tvmodel_ntsc = Contacts.URL_TV_FORMAT+"0";
							sendcmd(tvmodel_ntsc);
							cam_palntsc.setText("NTSC");
							break;
						case R.id.tvmodel_pal:
                                String tvmodel_pal = Contacts.URL_TV_FORMAT+"1";
							sendcmd(tvmodel_pal);
							cam_palntsc.setText("PAL");
							break;

						}

					}

				});

		mFiletimeSettingRadioGroup
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						switch (checkedId) {
						// set circle time
						case R.id.filetime_off:
                                String filetime_off = Contacts.URL_CYCLIC_RECORD + "0";
							sendcmd(filetime_off);
							cam_filetime.setText(R.string.close);
							break;
						case R.id.filetime_3:
                                String filetime_3 = Contacts.URL_CYCLIC_RECORD + "1";
							sendcmd(filetime_3);
							cam_filetime.setText(R.string.three_minute);
							break;
						case R.id.filetime_5:
                                String filetime_5 = Contacts.URL_CYCLIC_RECORD + "2";
							sendcmd(filetime_5);
							cam_filetime.setText(R.string.five_minute);
							break;
						case R.id.filetime_10:
                                String filetime_10 = Contacts.URL_CYCLIC_RECORD + "3";
							sendcmd(filetime_10);
							cam_filetime.setText(R.string.ten_minute);
							break;
						}

					}

				});

		CamFactory = (Button) findViewById(R.id.camadvset_factory);
		LineCamSensorLevel.setOnClickListener(this);
		LineCamTvModel.setOnClickListener(this);
		LineCamFileTime.setOnClickListener(this);
		LineCamsdformat.setOnClickListener(this);
		CamFactory.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.camadvset_gsensor: // camer name
			if (mGsensorSetting.getVisibility() != View.VISIBLE) {
				mGsensorSetting.setVisibility(View.VISIBLE);
			} else {
				mGsensorSetting.setVisibility(View.GONE);
			}
			break;
		case R.id.camadvset_tvmodel:
			if (mTvmodelSetting.getVisibility() != View.VISIBLE) {
				mTvmodelSetting.setVisibility(View.VISIBLE);
			} else {
				mTvmodelSetting.setVisibility(View.GONE);
			}
			break;
		case R.id.camadvset_filetime:
			if (mFiletimeSetting.getVisibility() != View.VISIBLE) {
				mFiletimeSetting.setVisibility(View.VISIBLE);
			} else {
				mFiletimeSetting.setVisibility(View.GONE);
			}
			break;
		case R.id.camadvset_sdformat:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(getString(R.string.set_isneedformat));
			builder.setTitle(getString(R.string.notice));
			builder.setPositiveButton(getString(R.string.confirm),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
                                String Gsensoroff = Contacts.URL_FORMAT + "1";
							sendCmdForResult(Gsensoroff);
						}
					});

			builder.setNegativeButton(getString(R.string.cancel),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.setCancelable(false).create().show();
			break;
		case R.id.camadvset_factory:
			//Toast.makeText(this, "camera restore factory...", 0).show();
			AlertDialog.Builder builder_factory = new AlertDialog.Builder(this);
			builder_factory.setMessage(R.string.camset_recovery_question);
			builder_factory.setTitle(R.string.notice);
			builder_factory.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
                                String Gsensoroff = Contacts.URL_SYSTEM_RESET;
							sendCmdForResult(Gsensoroff);
						}
					});

			builder_factory.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder_factory.setCancelable(false).create().show();
			break;
		}
	}
	private void sendCmdForResult(final String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.e("9527", response);
						try {
							InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
							DomParseUtils domParseUtils=new DomParseUtils();
							MovieRecord record = domParseUtils.getParserXml(is);
							if (record!=null && record.getStatus().equals("0")) {
                                if (url.equals(Contacts.URL_SYSTEM_RESET)) {//恢复出厂设置
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_system_reset_success), Toast.LENGTH_SHORT).show();
                                } else if (url.equals(Contacts.URL_FORMAT + "1")) {//格式化成功
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_format_sd_success), Toast.LENGTH_SHORT).show();
								}else {
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.set_success), Toast.LENGTH_SHORT).show();
								}
							}else {
                                if (url.equals(Contacts.URL_SYSTEM_RESET)) {//恢复出厂设置失败
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_system_reset_failure), Toast.LENGTH_SHORT).show();	
                                } else if (url.equals(Contacts.URL_FORMAT + "1")) {//格式化失败
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_format_sd_failure), Toast.LENGTH_SHORT).show();
								}else {
									Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.set_failure), Toast.LENGTH_SHORT).show();
								}
							}
						} catch (UnsupportedEncodingException e) {
                            if (url.equals(Contacts.URL_SYSTEM_RESET)) {//恢复出厂设置失败
								Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_system_reset_failure), Toast.LENGTH_SHORT).show();
                            } else if (url.equals(Contacts.URL_FORMAT + "1")) {//格式化失败
								Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.wifi_format_sd_failure), Toast.LENGTH_SHORT).show();
							}else {
								Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.set_failure), Toast.LENGTH_SHORT).show();
							}
							e.printStackTrace();
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						Log.e("9527", "Error: " + error.getMessage());
						Toast.makeText(CamSetAdvActivity.this, getResources().getString(R.string.set_failure), Toast.LENGTH_SHORT).show();
					}
				});

		VLCApplication.getInstance().addToRequestQueue(req);
	}
	private void sendcmd(String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.e("9527", response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						Log.e("9527", "Error: " + error.getMessage());
					}
				});

		VLCApplication.getInstance().addToRequestQueue(req);
	}

	private void getStatus() {
         String URL = Contacts.URL_QUERY_CURRENT_STATUS;
		StringRequest req = new StringRequest(URL,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.e("9527", response);
						DomParseUtils domParseUtils = new DomParseUtils();
						int i = domParseUtils.parastr(response);
						HashMap<String, String> map = domParseUtils.hMap;
						System.out.println(i);
						Iterator<Entry<String, String>> it = map.entrySet()
								.iterator();
						while (it.hasNext()) {
							Map.Entry<String, String> entry = it.next();
							String key = entry.getKey();
							String value = entry.getValue();
							// TODO
							if ("2003".equals(key)) {
								// 分节时长
								value_filetime = value;
								if (value_filetime.equals("0")) {
									cam_filetime.setText(R.string.close);
									mFiletimeSettingRadioGroup.check(R.id.filetime_off);
								} else if (value_filetime.equals("1")) {
									cam_filetime.setText(R.string.three_minute);
									mFiletimeSettingRadioGroup.check(R.id.filetime_3);
								} else if (value_filetime.equals("2")) {
									cam_filetime.setText(R.string.five_minute);
									mFiletimeSettingRadioGroup.check(R.id.filetime_5);
								} else if (value_filetime.equals("3")) {
									cam_filetime.setText(R.string.ten_minute);
									mFiletimeSettingRadioGroup.check(R.id.filetime_10);
								}

							} else if ("2011".equals(key)) {
								// 碰撞等级
								value_sensorLevel = value;
								if (value_sensorLevel.equals("0")) {
									cam_sensor_level.setText(R.string.close);
									mGsensorSettingRadioGroup.check(R.id.gsensor_off);
								} else if (value_sensorLevel.equals("1")) {
									cam_sensor_level.setText(R.string.low);
									mGsensorSettingRadioGroup.check(R.id.gsensor_low);
								} else if (value_sensorLevel.equals("2")) {
									cam_sensor_level.setText(R.string.middle);
									mGsensorSettingRadioGroup.check(R.id.gsensor_med);
								} else if (value_sensorLevel.equals("3")) {
									cam_sensor_level.setText(R.string.high);
									mGsensorSettingRadioGroup.check(R.id.gsensor_high);
								}
							} else if ("3009".equals(key)) {
								// 电视模式
								value_tvModel = value;
								if (value_tvModel.equals("0")) {
									cam_palntsc.setText("NTSC");
									mTvmodelSettingRadioGroup.check(R.id.tvmodel_ntsc);
								} else if (value_tvModel.equals("1")) {
									cam_palntsc.setText("PAL");
									mTvmodelSettingRadioGroup.check(R.id.tvmodel_pal);
								}
							}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						Log.e("9527", "Error: " + error.getMessage());
					}
				});

		VLCApplication.getInstance().addToRequestQueue(req);
	}
}
