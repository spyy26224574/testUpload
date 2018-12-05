package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.adai.gkdnavi.utils.ToastUtil;

public class SetSignatureActivity extends BaseActivity {

    private EditText edit_signature;
    private TextView save;
    private int maxLen = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_set_signature);
        initView();
        init();
    }

    @Override
    protected void init() {
        super.init();
        setTitle(R.string.set_signature);
        String sign = getIntent().getStringExtra("sign");
        if (!TextUtils.isEmpty(sign)) {
            edit_signature.setText(sign);
        }
    }

    @Override
    protected void initView() {
        super.initView();
        edit_signature = (EditText) findViewById(R.id.edit_signature);
        edit_signature.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Editable editable = edit_signature.getText();
                int len = editable.length();
                if (len > maxLen) {
                    ToastUtil.showShortToast(SetSignatureActivity.this, SetSignatureActivity.this.getString(R.string.beyond_maximum));
                    int selEndIndex = Selection.getSelectionEnd(editable);
                    String str = editable.toString();
                    //截取新字符串
                    String newStr = str.substring(0, maxLen);
                    edit_signature.setText(newStr);
                    editable = edit_signature.getText();
                    //新字符串的长度
                    int newLen = editable.length();
                    //旧光标位置超过字符串长度
                    if (selEndIndex > newLen) {
                        selEndIndex = editable.length();
                    }
                    //设置新光标所在的位置
                    Selection.setSelection(editable, selEndIndex);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        save = (TextView) findViewById(R.id.right_text);
        save.setVisibility(View.VISIBLE);
        save.setText(getString(R.string.save));
        save.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInfo();
            }
        });

    }

    private void saveInfo() {
        showpDialog();
        RequestMethods.userUpdate(null, null, null,edit_signature.getText().toString(), new HttpUtil.Callback<UserSingleupPagebean>() {

            @Override
            public void onCallback(UserSingleupPagebean result) {
                // TODO Auto-generated method stub
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(R.string.Modify_success);

                            UserInfoBean userinfo = result.data;
                            if (userinfo != null)
                                userinfo.signature = edit_signature.getText().toString();
                            CurrentUserInfo.saveUserinfo(mContext, userinfo);

                            String signature = edit_signature.getText().toString();
                            Intent intent = new Intent();
                            Bundle bundle = new Bundle();
                            bundle.putString("signature", signature);
                            intent.putExtras(bundle);
                            SetSignatureActivity.this.setResult(RESULT_OK, intent);
                            SetSignatureActivity.this.finish();

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
