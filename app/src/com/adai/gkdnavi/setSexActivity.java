package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.adai.gkd.bean.UserInfoBean;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;

public class setSexActivity extends BaseActivity {
    private AppCompatRadioButton mRbSecrety, mRbMan, mRbFemale;
    private RadioGroup mRgSex;
    private TextView save;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_sex);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.set_sex);
        mRgSex = (RadioGroup) findViewById(R.id.rg_sex);
        mRbSecrety = (AppCompatRadioButton) findViewById(R.id.rb_secret);
        mRbMan = (AppCompatRadioButton) findViewById(R.id.rb_man);
        mRbFemale = (AppCompatRadioButton) findViewById(R.id.rb_female);
        save = (TextView) findViewById(R.id.right_text);
        save.setVisibility(View.VISIBLE);
        save.setText(getString(R.string.save));
        if ("M".equals(CurrentUserInfo.sex)) {
            mRgSex.check(R.id.rb_man);
        } else if ("F".equals(CurrentUserInfo.sex)) {
            mRgSex.check(R.id.rb_female);
        } else {
            mRgSex.check(R.id.rb_secret);
        }
    }

    private void initEvent() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sexTag = "";
                int checkedRadioButtonId = mRgSex.getCheckedRadioButtonId();
                switch (checkedRadioButtonId) {
                    case R.id.rb_secret:
                        sexTag = "S";
                        break;
                    case R.id.rb_man:
                        sexTag = "M";
                        break;
                    case R.id.rb_female:
                        sexTag = "F";
                        break;
                }
                if (sexTag.equals(CurrentUserInfo.sex)) {
                    finish();
                    return;
                }
                showpDialog();
                RequestMethods.userUpdate(null, null, sexTag, null, new HttpUtil.Callback<UserSingleupPagebean>() {

                    @Override
                    public void onCallback(UserSingleupPagebean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    showToast(R.string.Modify_success);

                                    UserInfoBean userinfo = result.data;
                                    if (userinfo != null) {
                                        CurrentUserInfo.saveUserinfo(mContext, userinfo);
                                        Intent intent = new Intent();
                                        Bundle bundle = new Bundle();
                                        bundle.putString("sex", userinfo.sex);
                                        intent.putExtras(bundle);
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    } else {
                                        showToast(TextUtils.isEmpty(result.message) ? getString(R.string.Modify_failure) : result.message);
                                    }
                                    break;

                                default:
                                    showToast(TextUtils.isEmpty(result.message) ? getString(R.string.Modify_failure) : result.message);
                                    break;
                            }
                        }
                        hidepDialog();
                    }
                });
            }
        });
    }
}
