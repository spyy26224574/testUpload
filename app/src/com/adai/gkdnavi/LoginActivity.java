package com.adai.gkdnavi;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;

import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

public class LoginActivity extends BaseActivity implements OnClickListener {
    public static final int REQ_CODE_LOGIN = 0XA1;
    private static final int RESULT_COUNTRY_CODE = 1;
    private EditText etUserName;
    private EditText etPassWord;
    private Button login;
    private TextView register;
    private String currentUsername;
    private String currentPassword;
    private ImageButton mImageButton;
    private static final String DEFAULT_COUNTRY_ID = "42";
    private String mCurrentId = DEFAULT_COUNTRY_ID;
    private String mCurrentCode;
    private String[] mLocalCountry;
    private TextView mTvCountry;
    private TextView mTvCountryCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 如果登录成功过，直接进入主页面
        setContentView(R.layout.activity_login);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        etUserName = (EditText) findViewById(R.id.et_userName);
        etUserName.setText(getSharedPreferences("login_name", 0).getString("currentUsername", ""));
        etPassWord = (EditText) findViewById(R.id.et_passWord);
        login = (Button) findViewById(R.id.login);
        mImageButton = (ImageButton) findViewById(R.id.logain_back);
        register = (TextView) findViewById(R.id.register);
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mTvCountryCode = (TextView) findViewById(R.id.tv_country_code);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        findViewById(R.id.resetpassword).setOnClickListener(this);
        findViewById(R.id.ll_country).setOnClickListener(this);
        mLocalCountry = getCurrentCountry();
        if (mLocalCountry != null) {
            mCurrentCode = mLocalCountry[1];
            mTvCountry.setText(mLocalCountry[0]);
            mTvCountryCode.setText("+" + mCurrentCode);
        }
    }

    private String[] getCurrentCountry() {
        String mcc = getMCC();
        String[] country = null;
        if (!TextUtils.isEmpty(mcc)) {
            try {
                country = SMSSDK.getCountryByMCC(mcc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (country == null) {
            SMSLog.getInstance().d("no country found by MCC: " + mcc);
            country = SMSSDK.getCountry(DEFAULT_COUNTRY_ID);
        }
        return country;
    }

    private String getMCC() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        // 返回当前手机注册的网络运营商所在国家的MCC+MNC. 如果没注册到网络就为空.
        String networkOperator = tm.getNetworkOperator();
        if (!TextUtils.isEmpty(networkOperator)) {
            return networkOperator;
        }
        // 返回SIM卡运营商所在国家的MCC+MNC. 5位或6位. 如果没有SIM卡返回空
        return tm.getSimOperator();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ll_country:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiUtil.pingNet()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivityForResult(new Intent(LoginActivity.this, CountryCodeActivity.class), RESULT_COUNTRY_CODE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showToast(R.string.nonetwork);
                                }
                            });
                        }
                    }
                }).start();
                break;
            case R.id.login:
                currentUsername = etUserName.getText().toString().trim();
                currentPassword = etPassWord.getText().toString().trim();
                if (TextUtils.isEmpty(currentUsername) || TextUtils.isEmpty(currentPassword)) {
                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.account_password_notification), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentUsername.length() < 2 || currentUsername.length() > 16) {
                    showToast(R.string.username_2_16_char);
                    return;
                }
                showpDialog(R.string.is_loging);
                RequestMethods.userLogin(currentUsername, currentPassword, mCurrentCode, new HttpUtil.Callback<UserSingleupPagebean>() {

                    @Override
                    public void onCallback(UserSingleupPagebean result) {

                        boolean ishide = true;
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
//							//保存用户名和密码用于自动登陆
                                    SharedPreferences.Editor editor = getSharedPreferences("login_name", 0).edit();
                                    editor.putString("currentUsername", currentUsername);
                                    editor.commit();
                                    SharedPreferences pref = getSharedPreferences(CurrentUserInfo.filename, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor edit = pref.edit();
                                    edit.putString(CurrentUserInfo.key_cur_code, mCurrentCode);
                                    edit.putString(CurrentUserInfo.key_curUsername, currentUsername);
                                    edit.putString(CurrentUserInfo.key_cur_password, currentPassword);
                                    edit.apply();
                                    CurrentUserInfo.saveUserinfo(getApplicationContext(), result.data);
                                    Intent data = new Intent();
                                    data.putExtra("islogin", true);
                                    VoiceManager.isLogin = true;
                                    setResult(RESULT_OK, data);
                                    finish();
                                    ishide = false;
//                                    loginIm(result);
                                    break;
                                case 10105:
                                    showToast(R.string.usernotfound);
                                    break;
                                case 10104:
                                    showToast(R.string.userorpwderror);
                                    break;

                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        if (ishide)
                            hidepDialog();
//                            pd.dismiss();
                    }
                });

                break;

            case R.id.register:
//                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
//                startActivity(registerIntent);
                startActivity(SMSActivity.class);
                break;
            case R.id.logain_back:
                finish();
                break;
            case R.id.resetpassword:
                Intent reset = new Intent(LoginActivity.this, ResetPwdActivity.class);
                startActivity(reset);
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case RESULT_COUNTRY_CODE://选择国家吗返回
                if (data != null) {
                    mCurrentId = data.getStringExtra(CountryCodeActivity.COUNTRY_ID);
                    String[] country = SMSSDK.getCountry(mCurrentId);
                    if (country != null) {
                        mCurrentCode = country[1];
                        mTvCountryCode.setText("+" + mCurrentCode);
                        mTvCountry.setText(country[0]);
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
