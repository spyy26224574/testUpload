package com.adai.gkdnavi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.params.PhoneResetpwdParam;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.StringUtils;
import com.adai.gkdnavi.utils.WifiUtil;

import org.json.JSONObject;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class ResetPwdActivity extends BaseActivity implements OnClickListener {

    private static final int RESULT_COUNTRY_CODE = 1;
    private EditText code;
    private EditText email, mEtPhone;
    private EditText onePassWord;
    private EditText twicePassWord;
    private Button resetpwd;
    private Button register;
    private String strcode;
    private String stremail;
    private String strOnePass;
    private String strTwicePass;
    private ImageButton mImageButton;
    private Button sendcode, sendcode_email;
    private ProgressDialog pd;
    private LinearLayout mLLPhone, mLLEmail, mLLCountry;
    private int time = 60;
    private RadioGroup type_radio;
    private TextView type_title;
    /**
     * 找回密码方式，0为电话号码找回，1为邮箱找回
     */
    private int type = 0;
    private String mStrPhone;
    //    private ProgressDialog mPd;
    private static final String DEFAULT_COUNTRY_ID = "42";
    private String mCurrentId = DEFAULT_COUNTRY_ID;
    private String mCurrentCode;
    private TextView mTvCountry;
    private TextView mTvCountryCode;
    private String[] mLocalCountry;
    private static final int CODE_ERROR = 1;
    private static final int CODE_VERIFICATION_SUCCEED = 2;
    private static final int CODE_VERIFICATION_FAILED = 3;
    private static final int CODE_GET_VERIFICATION_CODE_SUCCEED = 4;
    private static final int CODE_GET_VERIFICATION_CODE = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hidepDialog();
            switch (msg.what) {
                case CODE_GET_VERIFICATION_CODE:
                    String phone = mEtPhone.getText().toString().trim().replaceAll("\\s*", "");
//                    String code = mTvCountryCode.getText().toString().trim();
                    SMSSDK.getVerificationCode(mCurrentCode, phone);
                    break;
                case CODE_ERROR:
                    String des = (String) msg.obj;
                    showToast(des);
                    break;
                case CODE_VERIFICATION_SUCCEED:
                    resetPwd();
                    break;
                case CODE_VERIFICATION_FAILED:
                    showToast(R.string.validation_failed);
                    break;
                case CODE_GET_VERIFICATION_CODE_SUCCEED:
                    postDelayed(new CountTimerRunnable(0, time), 1000);
                    sendcode.setEnabled(false);
                    break;
            }
        }
    };


    //    private Runnable timeRunnable = new Runnable() {
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            if (type == 0) {
//                time--;
//                if (time > 0) {
//                    sendcode.setText(String.format(getString(R.string.resendcode), time));
//                    mHandler.postDelayed(timeRunnable, 1000);
//                    sendcode.setEnabled(false);
//                } else {
//                    sendcode.setText(getString(R.string.resendtext));
//                    sendcode.setClickable(true);
//                    sendcode.setEnabled(true);
//                }
//            } else {
//                timeEmail--;
//                if (timeEmail > 0) {
//                    sendcode_email.setText(String.format(getString(R.string.resendcode), timeEmail));
//                    mHandler.postDelayed(timeRunnable, 1000);
//                    sendcode_email.setEnabled(false);
//                } else {
//                    sendcode_email.setText(getString(R.string.resendtext));
//                    sendcode_email.setClickable(true);
//                    sendcode_email.setEnabled(true);
//                }
//            }
//        }
//    };
    private class CountTimerRunnable implements Runnable {
        private int mType;
        private int mTime = 60;

        public CountTimerRunnable(int type, int time) {
            mType = type;
            mTime = time;
        }

        @Override
        public void run() {
            mTime--;
            switch (mType) {
                case 0:
                    if (mTime > 0) {
                        sendcode.setText(String.format(getString(R.string.resendcode), mTime));
                        mHandler.postDelayed(this, 1000);
                        sendcode.setEnabled(false);
                    } else {
                        sendcode.setText(getString(R.string.resendtext));
                        sendcode.setClickable(true);
                        sendcode.setEnabled(true);
                    }
                    break;
                case 1:
                    if (mTime > 0) {
                        sendcode_email.setText(String.format(getString(R.string.resendcode), mTime));
                        mHandler.postDelayed(this, 1000);
                        sendcode_email.setEnabled(false);
                    } else {
                        sendcode_email.setText(getString(R.string.resendtext));
                        sendcode_email.setClickable(true);
                        sendcode_email.setEnabled(true);
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_resetpwd);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        // TODO Auto-generated method stub
        code = (EditText) findViewById(R.id.code);
        email = (EditText) findViewById(R.id.email);
        mLLPhone = (LinearLayout) findViewById(R.id.ll_phone);
        mLLEmail = (LinearLayout) findViewById(R.id.ll_email);
        mTvCountryCode = (TextView) findViewById(R.id.tv_country_code);
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mEtPhone = (EditText) findViewById(R.id.et_phone_number);
        onePassWord = (EditText) findViewById(R.id.newPassWord);
        twicePassWord = (EditText) findViewById(R.id.twoPassWord);
        resetpwd = (Button) findViewById(R.id.resetpwd);
        findViewById(R.id.ll_country).setOnClickListener(this);
        mImageButton = (ImageButton) findViewById(R.id.logain_back);
        resetpwd.setOnClickListener(this);
        register = (Button) findViewById(R.id.register);
        register.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        sendcode = (Button) findViewById(R.id.sendcode);
        sendcode.setOnClickListener(this);
        sendcode_email = (Button) findViewById(R.id.sendcode_email);
        sendcode_email.setOnClickListener(this);
        type_radio = (RadioGroup) findViewById(R.id.type_radio);
        type_title = (TextView) findViewById(R.id.type_title);
        pd = new ProgressDialog(this);
        mLocalCountry = getCurrentCountry();
        if (mLocalCountry != null) {
            mCurrentCode = mLocalCountry[1];
            mTvCountryCode.setText("+" + mCurrentCode);
            mTvCountry.setText(mLocalCountry[0]);
        }
    }

    private String[] getCurrentCountry() {
        String mcc = getMCC();
        String[] country = null;
        if (!TextUtils.isEmpty(mcc)) {
            country = SMSSDK.getCountryByMCC(mcc);
        }
        if (country == null) {
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
    protected void init() {
        super.init();
        type_radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.by_phone:

//                        email.setHint(R.string.enter_phonenumber);
////                        email.setInputType(InputType.TYPE_CLASS_PHONE);
//                        email.setKeyListener(DigitsKeyListener.getInstance(getString(R.string.digits_phone)));
                        changeType(0);
                        break;
                    case R.id.by_email:

//                        email.setHint(R.string.enter_email);
//                        email.setKeyListener(DigitsKeyListener.getInstance(getString(R.string.digits_email)));
                        changeType(1);
                        break;
                }
            }
        });
        type_radio.check(R.id.by_phone);
        SMSSDK.registerEventHandler(mSMSEventHandler);
    }

    private void changeType(int type) {
        this.type = type;

        code.setText("");
        onePassWord.setText("");
        twicePassWord.setText("");
        switch (type) {
            case 0:
                type_title.setText(R.string.phonenumber);
                email.setText("");
                mLLEmail.setVisibility(View.GONE);
                mLLPhone.setVisibility(View.VISIBLE);
                mEtPhone.requestFocus();
                sendcode.setVisibility(View.VISIBLE);
                sendcode_email.setVisibility(View.GONE);
                break;
            case 1:
                type_title.setText(R.string.email);
                mEtPhone.setText("");
                mLLPhone.setVisibility(View.GONE);
                mLLEmail.setVisibility(View.VISIBLE);
                email.requestFocus();
                sendcode.setVisibility(View.GONE);
                sendcode_email.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 短信验证的回调监听
     */
    private EventHandler mSMSEventHandler = new EventHandler() {
        @Override
        public void beforeEvent(int i, Object o) {
            super.beforeEvent(i, o);
        }

        @Override
        public void afterEvent(int event, int result, Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) { //回调完成
                //提交验证码成功,如果验证成功会在data里返回数据。data数据类型为HashMap<number,code>
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    HashMap<String, Object> mData = (HashMap<String, Object>) data;
                    String phone = (String) mData.get("phone");//返回用户注册的手机号
                    if (phone.equals(mEtPhone.getText().toString().trim())) {
                        mHandler.sendEmptyMessage(CODE_VERIFICATION_SUCCEED);
                    } else {
                        mHandler.sendEmptyMessage(CODE_VERIFICATION_FAILED);
                    }
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//获取验证码成功
                    mHandler.sendEmptyMessage(CODE_GET_VERIFICATION_CODE_SUCCEED);
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {//返回支持发送验证码的国家列表

                }
            } else if (result == SMSSDK.RESULT_ERROR) {
                try {
                    Throwable throwable = (Throwable) data;
                    JSONObject object = new JSONObject(throwable.getMessage());
                    String des = object.optString("detail");//错误描述
                    if (!TextUtils.isEmpty(des)) {
                        Message message = mHandler.obtainMessage();
                        message.obj = des;
                        message.what = CODE_ERROR;
                        mHandler.sendMessage(message);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case RESULT_COUNTRY_CODE:
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
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.ll_country:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiUtil.pingNet()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivityForResult(new Intent(ResetPwdActivity.this, CountryCodeActivity.class), RESULT_COUNTRY_CODE);
                                }
                            });
                        } else {
                            Message message = mHandler.obtainMessage(CODE_ERROR);
                            message.obj = getString(R.string.nonetwork);
                            mHandler.sendMessage(message);
                        }
                    }
                }).start();
                break;
            case R.id.resetpwd:
                strcode = code.getText().toString().trim();
                stremail = email.getText().toString().trim();
                mStrPhone = mEtPhone.getText().toString().trim();
                strOnePass = onePassWord.getText().toString();
                strTwicePass = twicePassWord.getText().toString();
            /*
             * 分为注册成功和失败  1. 成功    返回到登录界面       2.失败     继续注册
			 */
                switch (type) {
                    case 0:
//                        if (!VoicePhone.isPhoneNumberValid(mStrPhone)) {
//                            Toast.makeText(this, getResources().getString(R.string.phonenum_error), Toast.LENGTH_SHORT).show();
//                            mEtPhone.requestFocus();
//                            return;
//                        }
                        break;
                    case 1:
                        if (!StringUtils.checkEmail(stremail)) {
                            Toast.makeText(this, getResources().getString(R.string.emailerror), Toast.LENGTH_SHORT).show();
                            email.requestFocus();
                            return;
                        }
                        break;
                }
                if (TextUtils.isEmpty(strcode)) {
                    Toast.makeText(this, getResources().getString(R.string.enter_code), Toast.LENGTH_SHORT).show();
                    code.requestFocus();
                    return;
                }
//			else if(!StringUtils.checkEmail(stremail)){
//					Toast.makeText(this, getResources().getString(R.string.emailerror), Toast.LENGTH_SHORT).show();
//					email.requestFocus();
//					return;
//				}
                else if (TextUtils.isEmpty(strOnePass) || strOnePass.length() < 6) {
                    showToast(getResources().getString(R.string.password_least_six));
                    onePassWord.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(strTwicePass) || strTwicePass.length() < 6) {
                    Toast.makeText(this, getResources().getString(R.string.password_least_six), Toast.LENGTH_SHORT).show();
                    twicePassWord.requestFocus();
                    return;
                } else if (!strOnePass.equals(strTwicePass)) {
                    Toast.makeText(this, getResources().getString(R.string.Two_input_password_error), Toast.LENGTH_SHORT).show();
                    return;
                }
//                mPd = new ProgressDialog(this);
//                mPd.setMessage(getResources().getString(R.string.navi_plslater));
//                mPd.show();
                showpDialog();
                if (type == 1) {
                    if (!StringUtils.checkEmail(stremail)) {
                        showToast(getString(R.string.emailerror));
                        email.requestFocus();
//                        mPd.dismiss();
                        hidepDialog();
                        return;
                    }
                    RequestMethods.userResetpwd(stremail, strcode, strOnePass, new HttpUtil.Callback<BasePageBean>() {

                        @Override
                        public void onCallback(BasePageBean result) {
                            // TODO Auto-generated method stub
                            if (result != null) {
                                switch (result.ret) {
                                    case 0:
                                        Intent data = new Intent();
                                        data.putExtra("hasreset", true);
                                        setResult(RESULT_OK, data);
                                        finish();
                                        break;
                                    case 10108:
                                        showToast(R.string.codetimeout);
                                        break;
                                    case 10109:
                                        showToast(R.string.codeerror);
                                        break;

                                    default:
                                        showToast(result.message);
                                        break;
                                }
                            }
//                            mPd.dismiss();
                            hidepDialog();
                        }
                    });
                } else if (type == 0) {
//                    if (!VoicePhone.isPhoneNumberValid(mStrPhone)) {
//                        showToast(getString(R.string.phonenum_error));
////                        mPd.dismiss();
//                        hidepDialog();
//                        return;
//                    }
//                    PhoneResetpwdParam param = new PhoneResetpwdParam();
//                    param.phone = mStrPhone;
//                    param.code = strcode;
//                    param.password = strOnePass;
//                    RequestMethods.resetpwdByPhone(param, new HttpUtil.Callback<BasePageBean>() {
//                        @Override
//                        public void onCallback(BasePageBean result) {
//                            if (result != null) {
//                                switch (result.ret) {
//                                    case 0:
//                                        showToast(getString(R.string.Modify_success));
//                                        Intent data = new Intent();
//                                        data.putExtra("hasreset", true);
//                                        setResult(RESULT_OK, data);
//                                        finish();
//                                        break;
//                                    case 10108:
//                                        showToast(R.string.codetimeout);
//                                        break;
//                                    case 10109:
//                                        showToast(R.string.codeerror);
//                                        break;
//
//                                    default:
//                                        showToast(result.message);
//                                        break;
//                                }
//                            }
////                            mPd.dismiss();
//                            hidepDialog();
//                        }
//                    });
                    submitVerificationCode();
                }

                break;
            case R.id.register:
                Intent registerIntent = new Intent(ResetPwdActivity.this, SMSActivity.class);
                startActivity(registerIntent);
                finish();
                break;
            case R.id.logain_back:
                finish();
                break;
            case R.id.sendcode_email:
                code.requestFocus();
                stremail = email.getText().toString().trim();
                if (TextUtils.isEmpty(stremail)) {
                    showToast(R.string.enter_email);
                    return;
                }
                if (!StringUtils.checkEmail(stremail)) {
                    showToast(getString(R.string.emailerror));
//                        mPd.dismiss();
                    email.requestFocus();
                    hidepDialog();
                    return;
                }
                showpDialog();
                RequestMethods.userSendCode(stremail, new HttpUtil.Callback<BasePageBean>() {

                    @Override
                    public void onCallback(BasePageBean result) {
                        // TODO Auto-generated method stub
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    showToast(String.format(getString(R.string.codenotifysucess), stremail));
                                    time = 60;
                                    mHandler.postDelayed(new CountTimerRunnable(1, time), 1000);
                                    sendcode_email.setClickable(false);
                                    break;
                                case 10107:
                                    showToast(R.string.usernotfound);
                                    break;

                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
//                            mPd.dismiss();
                        hidepDialog();
                    }
                });
                break;
            case R.id.sendcode:
                code.requestFocus();
                mStrPhone = mEtPhone.getText().toString().trim();
                if (TextUtils.isEmpty(mStrPhone)) {
                    showToast(R.string.enter_phonenumber);
                    return;
                }
                showpDialog();
                //获取验证码
                getSecurity();
                break;
        }

    }

    private void resetPwd() {
        PhoneResetpwdParam param = new PhoneResetpwdParam();
        param.phone = mStrPhone;
//        param.code = strcode;
        param.areaCode = mCurrentCode;
        param.password = strOnePass;
        RequestMethods.resetpwdByPhone(param, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                hidepDialog();
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(getString(R.string.Modify_success));
                            Intent data = new Intent();
                            data.putExtra("hasreset", true);
                            setResult(RESULT_OK, data);
                            finish();
                            break;
                        case 10108:
                            showToast(R.string.codetimeout);
                            break;
                        case 10109:
                            showToast(R.string.codeerror);
                            break;

                        default:
                            showToast(result.message);
                            break;
                    }
                }
//                            mPd.dismiss();
            }
        });
    }

    /**
     * 获取验证码
     */
    public void getSecurity() {
        String phone = mEtPhone.getText().toString().trim().replaceAll("\\s*", "");
        if (TextUtils.isEmpty(phone)) {
            showToast(R.string.phone_not_empty);
            mEtPhone.requestFocus();
        }
//        else if (!Pattern.matches(getString(R.string.pattern_phone), strusername)) {
//            showToast(getString(R.string.phonenum_error));
//        }
        else {
//            showpDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (WifiUtil.pingNet()) {
                        mHandler.sendEmptyMessage(CODE_GET_VERIFICATION_CODE);
                    } else {
                        Message message = mHandler.obtainMessage(CODE_ERROR);
                        message.obj = getString(R.string.nonetwork);
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        }
    }

    /**
     * 向服务器提交验证码，在监听回调中判断是否通过验证
     */
    public void submitVerificationCode() {
        if (!TextUtils.isEmpty(strcode)) {
            //提交短信验证码
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (WifiUtil.pingNet()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SMSSDK.submitVerificationCode(mCurrentCode, mStrPhone, strcode);//国家号，手机号码，验证码
                            }
                        });
                    } else {
                        Message message = mHandler.obtainMessage(CODE_ERROR);
                        message.obj = getString(R.string.nonetwork);
                        mHandler.sendMessage(message);
                    }
                }
            }).start();
        } else {
            showToast(R.string.enter_code);
            code.requestFocus();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mSMSEventHandler);
    }
}
