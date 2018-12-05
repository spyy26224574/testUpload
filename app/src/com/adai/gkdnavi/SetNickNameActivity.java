package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.adai.gkd.bean.UserInfoBean;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;

public class SetNickNameActivity extends BaseActivity {

    private EditText edit_nickname;
    private TextView save;
    private int maxLen = 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_nickname);
        initView();
        init();
    }


    @Override
    protected void init() {
        super.init();
        setTitle(R.string.set_nickname);
        String nickname = getIntent().getStringExtra("nickname");
        if (!TextUtils.isEmpty(nickname)) {
            edit_nickname.setText(nickname);
        }
    }

    @Override
    protected void initView() {
        super.initView();
          edit_nickname = (EditText) findViewById(R.id.edit_nickname);
//        edit_nickname.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Editable editable = edit_nickname.getText();
//                //int len = editable.length();
//                int len = 0;
//                String Reg = "([\u4e00-\u9fa5])";
//                for (int i = 0; i < editable.length(); i++) {
//                    String b = Character.toString(editable.charAt(i));
//                    if (b.matches(Reg)) {
//                        len++;
//                    }
//                    len++;
//                }
//
//                if (len > maxLen) {
//                    int selEndIndex = Selection.getSelectionEnd(editable);
//                    String str = editable.toString();
//                    //截取新字符串
//                    String newStr = str.substring(0, maxLen);
//                    edit_nickname.setText(newStr);
//                    editable = edit_nickname.getText();
//                    //新字符串的长度
//                    int newLen = editable.length();
//                    //旧光标位置超过字符串长度
//                    if (selEndIndex > newLen) {
//                        selEndIndex = editable.length();
//                    }
//                    //设置新光标所在的位置
//                    Selection.setSelection(editable, selEndIndex);
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
        save = (TextView) findViewById(R.id.right_text);
        save.setVisibility(View.VISIBLE);
        save.setText(getString(R.string.save));
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edit_nickname == null) return;
                String nickname = edit_nickname.getText().toString();

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
                saveInfo();
            }
        });

    }

    private void saveInfo() {
        showpDialog();
        RequestMethods.userUpdate(null, edit_nickname.getText().toString(),null, null, new HttpUtil.Callback<UserSingleupPagebean>() {

            @Override
            public void onCallback(UserSingleupPagebean result) {
                // TODO Auto-generated method stub
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(R.string.Modify_success);

                            UserInfoBean userinfo = result.data;
                            if (userinfo != null)
                                userinfo.nickname = edit_nickname.getText().toString();
                            CurrentUserInfo.saveUserinfo(mContext, userinfo);

                            String nickname = edit_nickname.getText().toString();
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("nickname", nickname);
                            intent.putExtras(bundle);
                            SetNickNameActivity.this.setResult(RESULT_OK, intent);
                            SetNickNameActivity.this.finish();


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

}
