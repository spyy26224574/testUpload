package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.params.PhoneRegisterParam;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends BaseActivity implements OnClickListener {

    private EditText username;
    private EditText email;
    private EditText onePassWord;
    private EditText twicePassWord;
    private Button register;
    private String strusername;
    private String stremail;
    private String code;
    private String strOnePass;
    private String strTwicePass;
    private ImageButton mImageButton;

    private EditText et_code;
    private Button sendcode;

    private Button btn_protocol;
    private CheckBox cb_protocol;
//    private boolean agree_protocol = false;

    private int time = 60;
    private Handler timeHandler = new Handler();
    private Runnable timeRunable = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            time--;
            if (time > 0) {
                sendcode.setText(String.format(getString(R.string.resendcode), time));
                timeHandler.postDelayed(timeRunable, 1000);
            } else {
                sendcode.setText(getString(R.string.resendtext));
                sendcode.setTextColor(getResources().getColor(R.color.white));
                sendcode.setEnabled(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register);
        initView();
        init();
    }

    @Override
    protected void initView() {
        // TODO Auto-generated method stub
        username = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        onePassWord = (EditText) findViewById(R.id.newPassWord);
        twicePassWord = (EditText) findViewById(R.id.twoPassWord);
        register = (Button) findViewById(R.id.quickRegister);
        mImageButton = (ImageButton) findViewById(R.id.register_back);
        register.setOnClickListener(this);
        mImageButton.setOnClickListener(this);
        et_code = (EditText) findViewById(R.id.code);
        sendcode = (Button) findViewById(R.id.sendcode);
        sendcode.setOnClickListener(this);
        btn_protocol = (Button) findViewById(R.id.btn_protocol);
        btn_protocol.setOnClickListener(this);
        cb_protocol = (CheckBox) findViewById(R.id.cb_protocol);

//        cb_protocol.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                agree_protocol = isChecked;
//                Log.e("9527", "agree_protocol = " + agree_protocol);
//            }
//        });

    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.quickRegister:
                strusername = username.getText().toString().trim();
                code = et_code.getText().toString().trim();
                stremail = email.getText().toString().trim();
                strOnePass = onePassWord.getText().toString();
                strTwicePass = twicePassWord.getText().toString();
            /*
             * 分为注册成功和失败  1. 成功    返回到登录界面       2.失败     继续注册
			 */
                if (TextUtils.isEmpty(strusername)) {
//					Toast.makeText(this, getResources().getString(R.string.User_name_cannot_be_empty), Toast.LENGTH_SHORT).show();
                    showToast(R.string.phonenum_error);
                    username.requestFocus();
                    return;
                } else if (!Pattern.matches(getString(R.string.pattern_username), strusername)) {
                    Toast.makeText(this, getString(R.string.only_letters_numbers), Toast.LENGTH_SHORT).show();
                    username.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(code)) {
                    Toast.makeText(this, getResources().getString(R.string.enter_code), Toast.LENGTH_SHORT).show();
                    et_code.requestFocus();
                    return;
                } else if (TextUtils.isEmpty(strOnePass) || strOnePass.length() < 6) {
                    Toast.makeText(this, getResources().getString(R.string.password_least_six), Toast.LENGTH_SHORT).show();
                    onePassWord.requestFocus();
                    return;
                } else if (!strOnePass.equals(strTwicePass)) {
                    Toast.makeText(this, getResources().getString(R.string.Two_input_password_error), Toast.LENGTH_SHORT).show();
                    return;
                } else if (isContainChinese(strusername)) {
                    Toast.makeText(this, getResources().getString(R.string.input_error), Toast.LENGTH_SHORT).show();
                    return;
                } else if (!cb_protocol.isChecked()) {
                    Toast.makeText(this, getString(R.string.read_user_service), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!TextUtils.isEmpty(strusername) && !TextUtils.isEmpty(strOnePass)) {
//					final ProgressDialog pd = new ProgressDialog(this);
//					pd.setMessage(getResources().getString(R.string.Is_the_registered));
//					pd.show();

//					new Thread(new Runnable() {
//						public void run() {
//							try {
//								// 调用sdk注册方法
//								EMClient.getInstance().createAccount(strusername, strOnePass);
//								runOnUiThread(new Runnable() {
//									public void run() {
//										if (!RegisterActivity.this.isFinishing())
//											pd.dismiss();
//										Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registered_successfully), 0).show();
//										finish();
//									}
//								});
//							} catch (final HyphenateException e) {
//								runOnUiThread(new Runnable() {
//									public void run() {
//										if (!RegisterActivity.this.isFinishing())
//											pd.dismiss();
//										int errorCode=e.getErrorCode();
//										if(errorCode==EMError.NETWORK_ERROR){
//											Toast.makeText(getApplicationContext(), getResources().getString(R.string.network_anomalies), Toast.LENGTH_SHORT).show();
//										}else if(errorCode == EMError.USER_ALREADY_EXIST){
//											Toast.makeText(getApplicationContext(), getResources().getString(R.string.User_already_exists), Toast.LENGTH_SHORT).show();
//										}else if(errorCode == EMError.USER_AUTHENTICATION_FAILED){
//											Toast.makeText(getApplicationContext(), getResources().getString(R.string.registration_failed_without_permission), Toast.LENGTH_SHORT).show();
//										}else if(errorCode == EMError.USER_ILLEGAL_ARGUMENT){
//										    Toast.makeText(getApplicationContext(), getResources().getString(R.string.illegal_user_name),Toast.LENGTH_SHORT).show();
//										}else{
//											Toast.makeText(getApplicationContext(), getResources().getString(R.string.Registration_failed) + e.getMessage(), Toast.LENGTH_SHORT).show();
//										}
//									}
//								});
//							}
//						}
//					}).start();
//					RequestMethods.userSingle(strusername.toLowerCase(), strOnePass, stremail, new HttpUtil.Callback<UserSingleupPagebean>() {
//
//						@Override
//						public void onCallback(UserSingleupPagebean result) {
//							// TODO Auto-generated method stub
//							if(result!=null){
//								switch (result.ret) {
//								case 0:
//									showToast(getString(R.string.Registered_successfully));
//									CurrentUserInfo.saveUserinfo(getApplicationContext(), result.data);
//									Intent data=new Intent();
//									Bundle bundle=new Bundle();
//									bundle.putSerializable("userinfo", result.data);
//									bundle.putBoolean("isRegister", true);
//									data.putExtras(bundle);
//									setResult(RESULT_OK,data);
//									finish();
//									break;
//								case 10101:
//									showToast(getString(R.string.hasregister));
//									break;
//
//								default:
//									showToast(result.message);
//									break;
//								}
//							}
//							pd.dismiss();
//							pd.cancel();
//						}
//					});
                    onRegister();
                } else {
                    showToast(R.string.password_inconsistent);
                }

                break;
            case R.id.register_back:
                finish();
                break;
            case R.id.sendcode:
                onSendCode();
                break;
            case R.id.btn_protocol:
                Intent reset = new Intent(RegisterActivity.this, UserAgreementActivity.class);
                startActivity(reset);
                break;

        }

    }

    private void onRegister() {
        showpDialog();
        PhoneRegisterParam params = new PhoneRegisterParam();
//        params.code = code;
        params.password = strOnePass;
        params.email = stremail;
        params.phone = strusername;
        RequestMethods.registerByPhone(params, new HttpUtil.Callback<UserSingleupPagebean>() {
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
        view.findViewById(R.id.confirm).setOnClickListener(new OnClickListener() {
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

    private void onSendCode() {
        String phoneNum = username.getText().toString();
        if (VoicePhone.isPhoneNumberValid(phoneNum)) {
            showpDialog();
            RequestMethods.requestPhoneCode(phoneNum, new HttpUtil.Callback<BasePageBean>() {
                @Override
                public void onCallback(BasePageBean result) {
                    if (result != null) {
                        switch (result.ret) {
                            case 0:
                                time = 60;
                                timeHandler.postDelayed(timeRunable, 1000);
                                sendcode.setTextColor(getResources().getColor(R.color.black));
                                sendcode.setEnabled(false);
                                break;
                            default:
                                showToast(result.message);
                                break;
                        }
                    }
                    hidepDialog();
                }
            });
        } else {
            showToast(getString(R.string.phonenum_error));
        }
    }

    private boolean isContainChinese(String str) {

        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }
}
