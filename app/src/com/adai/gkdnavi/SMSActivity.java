package com.adai.gkdnavi;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkdnavi.utils.WifiUtil;

import org.json.JSONObject;

import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.utils.SMSLog;

public class SMSActivity extends BaseActivity implements View.OnClickListener {
    private static final int RESULT_COUNTRY_CODE = 1;
    // 默认使用中国区号
    private static final String DEFAULT_COUNTRY_ID = "42";
    private static final int CODE_ERROR = 1;
    private static final int CODE_VERIFICATION_SUCCEED = 2;
    private static final int CODE_VERIFICATION_FAILED = 3;
    private static final int CODE_GET_VERIFICATION_CODE_SUCCEED = 4;
    private static final int CODE_GET_VERIFICATION_CODE = 5;
    private String mCurrentId = DEFAULT_COUNTRY_ID;
    private String mCurrentCode = "86";
    private LinearLayout mLlCountry;
    private TextView mTvCountry;
    private TextView mTvCountryCode;
    private EditText mEtPhoneNumber;
    private EditText mCode;
    private Button mSendcode;
    private Button mBtnNext;
    private int time = 60;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            hidepDialog();
            switch (msg.what) {
                case CODE_GET_VERIFICATION_CODE:
                    String phone = mEtPhoneNumber.getText().toString().trim().replaceAll("\\s*", "");
//                    String code = mTvCountryCode.getText().toString().trim();
                    SMSSDK.getVerificationCode(mCurrentCode, phone);
                    break;
                case CODE_ERROR:
                    String des = (String) msg.obj;
                    showToast(des);
                    break;
                case CODE_VERIFICATION_SUCCEED:
                    registerEmail();
                    break;
                case CODE_VERIFICATION_FAILED:
                    showToast(R.string.validation_failed);
                    break;
                case CODE_GET_VERIFICATION_CODE_SUCCEED:
                    time = 60;
                    postDelayed(timeRunnable, 1000);
                    mSendcode.setEnabled(false);
                    break;
            }
        }
    };

    private Runnable timeRunnable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            time--;
            if (time > 0) {
                mSendcode.setText(String.format(getString(R.string.resendcode), time));
                mHandler.postDelayed(timeRunnable, 1000);
            } else {
                mSendcode.setText(getString(R.string.resendtext));
                mSendcode.setTextColor(getResources().getColor(R.color.white));
                mSendcode.setEnabled(true);
            }
        }
    };
    private String[] mLocalCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        SMSSDK.registerEventHandler(mSMSEventHandler);

    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.register);
        mLlCountry = (LinearLayout) findViewById(R.id.ll_country);
        mTvCountry = (TextView) findViewById(R.id.tv_country);
        mTvCountryCode = (TextView) findViewById(R.id.tv_country_code);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);
        mCode = (EditText) findViewById(R.id.code);
        mSendcode = (Button) findViewById(R.id.sendcode);
        mBtnNext = (Button) findViewById(R.id.btn_next);
        mLocalCountry = getCurrentCountry();
        if (mLocalCountry != null) {
            mCurrentCode = mLocalCountry[1];
            mTvCountryCode.setText("+" + mCurrentCode);
            mTvCountry.setText(mLocalCountry[0]);
        }
    }


    private void initEvent() {
        mLlCountry.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mSendcode.setOnClickListener(this);
    }

    /**
     * 短信验证的回调监听
     */
    private EventHandler mSMSEventHandler = new EventHandler() {

        @Override
        public void afterEvent(int event, int result, Object data) {
            Log.e(_TAG_, "afterEvent: " + event + " result=" + result);
            if (result == SMSSDK.RESULT_COMPLETE) { //回调完成
                //提交验证码成功,如果验证成功会在data里返回数据。data数据类型为HashMap<number,code>
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    HashMap<String, Object> mData = (HashMap<String, Object>) data;
                    String phone = (String) mData.get("phone");//返回用户注册的手机号
                    if (phone.equals(mEtPhoneNumber.getText().toString().trim())) {
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

    private String[] getCurrentCountry() {
        String mcc = getMCC();
        String[] country = null;
        if (!TextUtils.isEmpty(mcc)) {
            country = SMSSDK.getCountryByMCC(mcc);
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

    /**
     * 获取验证码
     */
    public void getSecurity() {
        String phone = mEtPhoneNumber.getText().toString().trim().replaceAll("\\s*", "");
        if (TextUtils.isEmpty(phone)) {
            showToast(R.string.phone_not_empty);
            mEtPhoneNumber.requestFocus();
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
        final String security = mCode.getText().toString().trim();
        final String phone = mEtPhoneNumber.getText().toString().trim();
        if (TextUtils.isEmpty(phone)) {
            showToast(R.string.phone_not_empty);
            mEtPhoneNumber.requestFocus();
            return;
        }
        if (!TextUtils.isEmpty(security)) {
            //提交短信验证码
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (WifiUtil.pingNet()) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SMSSDK.submitVerificationCode(mCurrentCode, phone, security);//国家号，手机号码，验证码
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
            mCode.requestFocus();
        }
    }

    private void registerEmail() {
        Intent intent = new Intent(this, RegisterEmailActivity.class);
        intent.putExtra("code", mCurrentCode);
        intent.putExtra("phone", mEtPhoneNumber.getText().toString().trim());
        intent.putExtra("localCountry", mLocalCountry);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_country://获取国家码
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiUtil.pingNet()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivityForResult(new Intent(SMSActivity.this, CountryCodeActivity.class), RESULT_COUNTRY_CODE);
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
            case R.id.sendcode://发送验证码
                mCode.requestFocus();
                getSecurity();
                break;
            case R.id.btn_next://进入注册页面
                submitVerificationCode();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mSMSEventHandler);
    }
}
