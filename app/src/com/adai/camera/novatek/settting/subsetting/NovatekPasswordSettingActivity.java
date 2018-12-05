package com.adai.camera.novatek.settting.subsetting;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;


/**
 * @author huangxy
 */
public class NovatekPasswordSettingActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtPassword;
    private CheckBox mCbShowPassword;
    private TextView mTvComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_password_setting);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.wifi_password);
        mTvComplete = (TextView) findViewById(R.id.right_text);
        mTvComplete.setText(R.string.complete_);
        mTvComplete.setVisibility(View.VISIBLE);
        mEtPassword = (EditText) findViewById(R.id.et_password);
        mCbShowPassword = (CheckBox) findViewById(R.id.cb_show_password);
    }

    private void initEvent() {
        mTvComplete.setOnClickListener(this);
        mCbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mEtPassword.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                } else {
                    mEtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }

    private void checkCameraSetting() {
        final String password = mEtPassword.getText().toString();
        if (TextUtils.isEmpty(password) || password.length() < 8) {//WIFI密码为空
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT)
                    .show();
        } else {//发送命令
            showpDialog();
            if (CameraUtils.hasSDCard && CameraUtils.CURRENT_MODE == CameraUtils.MODE_MOVIE && CameraUtils.isRecording) {
                CameraUtils.toggleRecordStatus(false, new CameraUtils.ToggleStatusListener() {
                    @Override
                    public void success() {
                        CameraUtils.sendCmd(Contacts.URL_SET_PASSPHRASE + password, mCmdListener);
                    }

                    @Override
                    public void error(String error) {
                        hidepDialog();
                        showToast(R.string.set_failure);
                    }
                });
            } else {
                CameraUtils.sendCmd(Contacts.URL_SET_PASSPHRASE + password, mCmdListener);
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
                        case NovatekWifiCommands.CAMERA_SET_WIFI_PASSWORD:
                            SpUtils.putString(NovatekPasswordSettingActivity.this, "pwd", mEtPassword.getText().toString());
//                            if (CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
//                            CameraUtils.changeMode(CameraUtils.MODE_MOVIE, new CameraUtils.ModeChangeListener() {
//                                @Override
//                                public void success() {
//                                    CameraUtils.CURRENT_MODE = CameraUtils.MODE_MOVIE;
//                                        CameraUtils.toggleRecordStatus(true, new CameraUtils.ToggleStatusListener() {
//                                            @Override
//                                            public void success() {
//                                                hidepDialog();
//                                                UIUtils.postDelayed(new Runnable() {
//                                                    @Override
//                                                    public void run() {
                            hidepDialog();
                            showToast(R.string.set_success);
                            CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
                            VoiceManager.isWifiPasswordChange = true;
                            startActivity(MainTabActivity.class);
//                                                    }
//                                                }, 500);
//                                            }

//                                            @Override
//                                            public void error(String error) {
//                                                hidepDialog();
//                                                CameraUtils.sendCmd(Contacts.URL_RECONNECT_WIFI, null);
//                                                startActivity(MainTabActivity.class);
//                                            }
//                                        });
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
}
