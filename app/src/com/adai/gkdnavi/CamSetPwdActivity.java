package com.adai.gkdnavi;

import android.app.Activity;
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
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.utils.SpUtils;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;

public class CamSetPwdActivity extends Activity implements OnClickListener {
	private String XCTag = "CamSetPwdActivity";
	private Button imageBtnSave;
	private EditText editSSIDPass;
	private EditText editSSIDPasstwo;
	private String strSSIDPass;
	private String strSSIDPasstwo;
	private ImageButton pwdBack;

    //public static final String SELECTMOVEPATH = Contacts.URL_SET_PASSPHRASE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_camsetpwd);
		initView();
	}

	private void initView() {
		imageBtnSave = (Button) findViewById(R.id.cam_pwdsave);
		imageBtnSave.setOnClickListener(this);
		editSSIDPass = (EditText) findViewById(R.id.campwd_newset1);
		editSSIDPasstwo = (EditText) findViewById(R.id.campwd_newset2);
		pwdBack = (ImageButton) findViewById(R.id.cam_pwdback);

		pwdBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CamSetPwdActivity.this.finish();
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.cam_pwdsave:
			strSSIDPass = editSSIDPass.getText().toString();
			strSSIDPasstwo = editSSIDPasstwo.getText().toString();
			if (strSSIDPass.length() < 8 || strSSIDPasstwo.length() < 8) {
				Toast.makeText(this, getText(R.string.password_too_short), Toast.LENGTH_SHORT)
						.show();
				break;
			}

			if (strSSIDPass.equals(strSSIDPasstwo)) {

                    sendcmd(Contacts.URL_SET_PASSPHRASE + strSSIDPass);
				SystemClock.sleep(2000);
                    sendcmd(Contacts.URL_RECONNECT_WIFI);
			} else {
				Toast.makeText(this, getText(R.string.password_inconsistent), Toast.LENGTH_SHORT)
						.show();
			}
			break;

		}
	}

	private void sendcmd(final String url) {
		StringRequest req = new StringRequest(url,
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.e("9527", response);
                        if (url.equals(Contacts.URL_SET_PASSPHRASE + strSSIDPass)) {
							Toast.makeText(
									CamSetPwdActivity.this,
									getResources()
											.getString(
													R.string.wifi_password_modified_successfully),
									Toast.LENGTH_SHORT).show();
							SpUtils.putInt(getApplicationContext(),
									"NEYWORKID", -1);
							SpUtils.putString(getApplicationContext(), "BSSID",
									"");

							new Handler().postDelayed(new Runnable() {

								public void run() {
									// execute the task
									CamSetPwdActivity.this.finish();
								}

							}, 2000);

						}

					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						VolleyLog.e("Error: ", error.getMessage());
						Log.e("9527", "Error: " + error.getMessage());
                if (url.equals(Contacts.URL_SET_PASSPHRASE + strSSIDPass)) {
							Toast.makeText(
									CamSetPwdActivity.this,
									getResources()
											.getString(
													R.string.wifi_password_modified_fail),
									Toast.LENGTH_SHORT).show();

						}
					}
				});

		VLCApplication.getInstance().addToRequestQueue(req);

	}

}
