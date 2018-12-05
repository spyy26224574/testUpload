package com.adai.camera.mstar.setting.subsetting;

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

import com.adai.camera.mstar.CameraCommand;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.MainTabActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.SpUtils;


public class MstarPasswordSettingActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEtPassword;
    private CheckBox mCbShowPassword;
    private TextView mTvComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_password_setting);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
                checkCameraSetting();
                break;
        }
    }

    private void checkCameraSetting() {
        final String password = mEtPassword.getText().toString();
        if (TextUtils.isEmpty(password) || password.length() < 8) {//WIFI密码为空
            Toast.makeText(this, getString(R.string.password_too_short), Toast.LENGTH_SHORT)
                    .show();
        } else {//发送命令
            CameraCommand.asynSendRequest(CameraCommand.commandUpdateUrl(null, password), new CameraCommand.RequestListener() {
                @Override
                public void onResponse(String result) {
                    if (CameraCommand.checkResponse(result)) {
                        CameraCommand.asynSendRequest(CameraCommand.commandReactivateUrl(), new CameraCommand.RequestListener() {
                            @Override
                            public void onResponse(String response) {
                                if (CameraCommand.checkResponse(response)) {
                                    showToast(R.string.set_success);
                                    SpUtils.putString(MstarPasswordSettingActivity.this, "pwd", mEtPassword.getText().toString());
                                    startActivity(MainTabActivity.class);
                                } else {
                                    showToast(R.string.set_failure);
                                }
                            }

                            @Override
                            public void onErrorResponse(String message) {
                                showToast(R.string.set_failure);
                            }
                        });
                    } else {
                        showToast(R.string.set_failure);
                    }
                }

                @Override
                public void onErrorResponse(String message) {
                    showToast(R.string.set_failure);
                }
            });
        }
    }

}
