package com.adai.camera.novatek.settting;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.MainTabActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.SpUtils;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author huangxy
 */
public class NovatekWifiNameActivity extends BaseActivity implements View.OnClickListener {

    private TextView mTvComplete, mTvPrefix;
    private EditText mEtSsid;
    private String mLastSsid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_wifi_name);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle("SSID");
        mTvComplete = (TextView) findViewById(R.id.right_text);
        mTvComplete.setText(R.string.complete_);
        mTvComplete.setVisibility(View.VISIBLE);
        mTvPrefix = (TextView) findViewById(R.id.tv_prefix);
        mEtSsid = (EditText) findViewById(R.id.et_ssid);
        mLastSsid = httpGetCamName();
        String[] lsx;
        if (mLastSsid.startsWith("360-L9_")) {
            lsx = mLastSsid.split("360-L9_");
            mTvPrefix.setText("360-L9_");
        } else {
            lsx = mLastSsid.split("LSX_G9_");
            mTvPrefix.setText("LSX_G9_");
        }
        mEtSsid.setText(lsx[lsx.length - 1]);
    }

    private void initEvent() {
        mTvComplete.setOnClickListener(this);
    }

    private String httpGetCamName() {
        WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        String strSSID = mWifiInfo.getSSID();
        if (!TextUtils.isEmpty(strSSID)) {
            if (strSSID.startsWith("\"") && strSSID.endsWith("\"")) {
                strSSID = strSSID.substring(1, strSSID.length() - 1);
            }
        }
        return strSSID;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
                checkCameraSetting();
                break;
            default:
                break;
        }
    }

    private void checkCameraSetting() {
        final String ssid = mEtSsid.getText().toString();
        if (ssid.equals(mLastSsid)) {
            return;
        }
        if (TextUtils.isEmpty(ssid) || ssid.length() < 1) {
            //WIFI密码为空
            Toast.makeText(this, getString(R.string.ssid_too_short), Toast.LENGTH_SHORT)
                    .show();
        } else {//发送命令
            showpDialog();
            if (CameraUtils.hasSDCard && CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE && CameraUtils.isRecording) {
                CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                    @Override
                    public void success() {
                        CameraUtils.sendCmd(Contacts.URL_SET_SSID + mTvPrefix.getText().toString() + ssid, mCmdListener);
                    }

                    @Override
                    public void error(String error) {
                        hidepDialog();
                        showToast(R.string.set_failure);
                    }
                });
            } else {
                CameraUtils.sendCmd(Contacts.URL_SET_SSID + mTvPrefix.getText().toString() + ssid, mCmdListener);
            }
        }
    }

    private CameraUtils.CmdListener mCmdListener = new CameraUtils.CmdListener() {
        @Override
        public void onResponse(String response) {
            InputStream is;
            try {
                is = new ByteArrayInputStream(response.getBytes("utf-8"));
                DomParseUtils domParseUtils = new DomParseUtils();
                MovieRecord record = domParseUtils.getParserXml(is);
                if (record != null && record.getStatus().equals("0")) {
                    int cmd = Integer.valueOf(record.getCmd());
                    switch (cmd) {
                        case NovatekWifiCommands.CAMERA_SET_WIFI_SSID:
                            SpUtils.putString(NovatekWifiNameActivity.this, "SSID", mTvPrefix.getText().toString() + mEtSsid.getText().toString());
//                            if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
//                            CameraUtils.changeMode(CameraUtils.MODE_MOVIE, new CameraUtils.ModeChangeListener() {
//                                @Override
//                                public void success() {
//                                    CameraUtils.CURRENT_MODE = CameraUtils.MODE_MOVIE;
                            showToast(R.string.set_success);
                            hidepDialog();
                            CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
                            startActivity(MainTabActivity.class);
//                                }
//
//                                @Override
//                                public void failure(Throwable throwable) {
//                                    showToast(R.string.set_success);
//                                    hidepDialog();
//                                    CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
//                                    startActivity(MainTabActivity.class);
//                                }
//                            });
//                            } else {
//                                CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
//                                    @Override
//                                    public void success() {
//                                        hidepDialog();
//                                        UIUtils.postDelayed(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                showToast(R.string.set_success);
//                                                hidepDialog();
//                                                CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
//                                                startActivity(MainTabActivity.class);
//                                            }
//                                        }, 500);
//                                    }
//
//                                    @Override
//                                    public void error(String error) {
//                                        hidepDialog();
//                                        showToast(R.string.set_success);
//                                        CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
//                                        startActivity(MainTabActivity.class);
//                                    }
//                                });
//                            }
                            break;
                        default:
                            break;
                    }
                } else {
                    hidepDialog();
                    showToast(R.string.set_failure);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                hidepDialog();
                showToast(R.string.set_failure);
            }

        }

        @Override
        public void onErrorResponse(Exception volleyError) {
            hidepDialog();
            showToast(R.string.set_failure);
        }
    };
}
