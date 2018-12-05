package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.sms.utils.SearchEngine;
import com.sms.widget.CountryListView;
import com.sms.widget.GroupListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;

public class CountryCodeActivity extends BaseActivity implements GroupListView.OnItemClickListener{
    private CountryListView mCountryListView;
//    private EditText mEtSearch;
    private SearchView mSearchView;
    private HashMap<String, String> countryRules;
    private static final int MSG_PREPARED = 1;
    private static final int CODE_ERROR = 2;
    public static final String COUNTRY_ID = "country_id";
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_PREPARED:
                    //初始化搜索引擎成功
                    if (countryRules == null || countryRules.size() <= 0) {
                        setContentView(R.layout.activity_country_code);
                        initView();
                        initEvent();
                        // 获取国家列表
                        SMSSDK.getSupportedCountries();
                    }
                    break;
                case CODE_ERROR:
                    hidepDialog();
                    String des = (String) msg.obj;
                    showToast(des);
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

    }

    @Override
    protected void init() {
        super.init();
        SMSSDK.registerEventHandler(mSMSEventHandler);
        showpDialog();
        // 初始化搜索引擎
        SearchEngine.prepare(this, new Runnable() {
            public void run() {
                mHandler.sendEmptyMessage(MSG_PREPARED);
            }
        });
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
                } else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {//获取验证码成功
                } else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {//返回支持发送验证码的国家列表
                    onCountryListGot((ArrayList<HashMap<String, Object>>) data);
                }
            } else if (result == SMSSDK.RESULT_ERROR) {
                try {
                    Throwable throwable = (Throwable) data;
                    Log.e(_TAG_, "afterEvent: " + throwable.getMessage());
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

    private void onCountryListGot(ArrayList<HashMap<String, Object>> countries) {
        // 解析国家列表
        for (HashMap<String, Object> country : countries) {
            String code = (String) country.get("zone");
            String rule = (String) country.get("rule");
            if (TextUtils.isEmpty(code) || TextUtils.isEmpty(rule)) {
                continue;
            }

            if (countryRules == null) {
                countryRules = new HashMap<>();
            }
            countryRules.put(code, rule);
        }
        hidepDialog();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.choose_country);
        mCountryListView = (CountryListView) findViewById(R.id.clv);
//        mEtSearch = (EditText) findViewById(R.id.et_search);
        mSearchView = (SearchView) findViewById(R.id.sv);
    }

    private void initEvent() {
        mCountryListView.setOnItemClickListener(this);
//        mEtSearch.addTextChangedListener(this);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mCountryListView.onSearch(newText);
                return false;
            }
        });
    }

    @Override
    public void onItemClick(GroupListView parent, View view, int group, int position) {
        if(position >= 0){
            String[] country = mCountryListView.getCountry(group, position);
            if (countryRules != null && countryRules.containsKey(country[1])) {
                Intent data = new Intent();
                data.putExtra(COUNTRY_ID, country[2]);
                setResult(RESULT_OK,data);
//                mEtSearch.setFocusable(false);
                finish();
            } else {
                showToast(R.string.country_not_support_currently);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SMSSDK.unregisterEventHandler(mSMSEventHandler);
    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//    }
//
//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//        mCountryListView.onSearch(s.toString().toLowerCase());
//    }
}
