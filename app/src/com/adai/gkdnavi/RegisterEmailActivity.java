package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.adai.gkd.bean.params.PhoneRegisterParam;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.StringUtils;

public class RegisterEmailActivity extends BaseActivity implements View.OnClickListener {
    private EditText mEmail;
    private EditText mNewPassWord;
    private EditText mTwoPassWord;
    private Button mBtnRegister;
    private String mAreaCode;
    private String mPhone;
    private String mLocalCountry;
    private Button btn_protocol;
    private CheckBox cb_protocol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_email);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        Intent intent = getIntent();
        mAreaCode = intent.getStringExtra("code");
        mPhone = intent.getStringExtra("phone");
        mLocalCountry = intent.getStringExtra("localCountry");
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.register);
        mEmail = (EditText) findViewById(R.id.email);
        mNewPassWord = (EditText) findViewById(R.id.newPassWord);
        mTwoPassWord = (EditText) findViewById(R.id.twoPassWord);
        mBtnRegister = (Button) findViewById(R.id.btn_register);
        cb_protocol = (CheckBox) findViewById(R.id.cb_protocol);
        btn_protocol = (Button) findViewById(R.id.btn_protocol);
    }

    private void initEvent() {
        mBtnRegister.setOnClickListener(this);
        btn_protocol.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_register:
                register();
                break;
            case R.id.btn_protocol:
                Intent reset = new Intent(this, UserAgreementActivity.class);
                startActivity(reset);
                break;
        }
    }

    private void register() {
        String newPassword = mNewPassWord.getText().toString().trim();
        String twoPassword = mTwoPassWord.getText().toString().trim();
        if (TextUtils.isEmpty(newPassword) || newPassword.length() < 6) {
            Toast.makeText(this, getResources().getString(R.string.password_least_six), Toast.LENGTH_SHORT).show();
            mNewPassWord.requestFocus();
            return;
        } else if (!newPassword.equals(twoPassword)) {
            Toast.makeText(this, getResources().getString(R.string.Two_input_password_error), Toast.LENGTH_SHORT).show();
            return;
        } else if (!cb_protocol.isChecked()) {
            Toast.makeText(this, getString(R.string.read_user_service), Toast.LENGTH_SHORT).show();
            return;
        }
        String emailStr = mEmail.getText().toString().trim();
        if (!TextUtils.isEmpty(emailStr) && !StringUtils.checkEmail(emailStr)) {
            showToast(getString(R.string.emailerror));
            mEmail.requestFocus();
            return;
        }
        showpDialog();
        PhoneRegisterParam param = new PhoneRegisterParam();
        param.areaCode = mAreaCode;
        param.languageCode = mLocalCountry;
        param.password = newPassword;
        param.email = emailStr;
        param.phone = mPhone;
        param.packageName = BuildConfig.APPLICATION_ID;
        RequestMethods.registerByPhone(param, new HttpUtil.Callback<UserSingleupPagebean>() {
            @Override
            public void onCallback(UserSingleupPagebean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(getString(R.string.action_Registered_successfully));
                            CurrentUserInfo.saveUserinfo(getApplicationContext(), result.data);
                            Intent data = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("userinfo", result.data);
                            bundle.putBoolean("isRegister", true);
                            data.putExtras(bundle);
                            setResult(RESULT_OK, data);
//							finish();
                            showModifyNickname();
                            break;
                        case 10101:
                            showToast(R.string.hasregister);
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
                hidepDialog();
            }
        });
    }

    private EditText et_nickname;
    private AlertDialog dialog;

    private void showModifyNickname() {
        View view = View.inflate(mContext, R.layout.notice_dialog, null);
        et_nickname = (EditText) view.findViewById(R.id.et_nickname);
        et_nickname.setText(CurrentUserInfo.nickname);
        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkNickname();
            }
        });
        dialog = new AlertDialog.Builder(mContext).setView(view).create();
        dialog.show();
    }

    private void checkNickname() {
        if (et_nickname == null) return;
        String nickname = et_nickname.getText().toString();

        int len = 0;
        String Reg = "([\u4e00-\u9fa5])";
        for (int i = 0; i < nickname.length(); i++) {
            String b = Character.toString(nickname.charAt(i));
            if (b.matches(Reg)) {
                len++;
            }
            len++;
        }

        if (TextUtils.isEmpty(nickname)) {
            showToast(R.string.notnon_nickname);
            return;
        } else if (len > 16) {
            showToast(R.string.nickname_too_long);
            return;
        }
        changeNickname(nickname);
    }

    private void changeNickname(String nickname) {
        showpDialog();
        RequestMethods.userUpdate(null, nickname, null, null, new HttpUtil.Callback<UserSingleupPagebean>() {
            @Override
            public void onCallback(UserSingleupPagebean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (result.data != null)
                                CurrentUserInfo.saveUserinfo(mContext, result.data);
                            finish();
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
                hidepDialog();
            }
        });
    }
}
