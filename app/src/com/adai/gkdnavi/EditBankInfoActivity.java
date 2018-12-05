package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.params.PostIdCardInfoParam;
import com.adai.gkd.bean.request.IdCardInfoBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;

public class EditBankInfoActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout mActivityEditBankInfo;
    private EditText mEtName;
    private EditText mEtIdentityCard;
    private EditText mEtBankCard;
    private EditText mEtBank;
    private IdCardInfoBean.IdCardInfoData mIdCardInfoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_bank_info);
        init();
        initView();
        initEvent();
        bindView();
    }

    @Override
    protected void init() {
        super.init();
        mIdCardInfoData = (IdCardInfoBean.IdCardInfoData) getIntent().getSerializableExtra("idCardData");
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.bank_card_info);
        mActivityEditBankInfo = (LinearLayout) findViewById(R.id.activity_edit_bank_info);
        mEtName = (EditText) findViewById(R.id.et_name);
        mEtIdentityCard = (EditText) findViewById(R.id.et_identity_card);
        mEtBankCard = (EditText) findViewById(R.id.et_bank_card);
        mEtBank = (EditText) findViewById(R.id.et_bank);
    }

    private void initEvent() {
        findViewById(R.id.btn_save).setOnClickListener(this);
    }

    private void bindView() {
        if (mIdCardInfoData != null) {
            mEtName.setText(mIdCardInfoData.the_name);
            mEtIdentityCard.setText(mIdCardInfoData.identity_card);
            mEtBank.setText(mIdCardInfoData.bank_card_type);
            mEtBankCard.setText(mIdCardInfoData.bank_card_number);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                postIdCardInfo();
                break;
        }
    }

    private void postIdCardInfo() {
        if (TextUtils.isEmpty(mEtName.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_name_bank_card));
            return;
        }
        if (TextUtils.isEmpty(mEtIdentityCard.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_id_number));
            return;
        }
        if (TextUtils.isEmpty(mEtBankCard.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_bank_card_number));
            return;
        }
        if (TextUtils.isEmpty(mEtBank.getText().toString())) {
            ToastUtil.showShortToast(this, "请输入开户的银行");
            return;
        }
        if (VoiceManager.isLogin) {

            PostIdCardInfoParam postIdCardInfoParam = new PostIdCardInfoParam();
            postIdCardInfoParam.bank_card_number = mEtBankCard.getText().toString();
            postIdCardInfoParam.bank_card_type = mEtBank.getText().toString();
            postIdCardInfoParam.identity_card = mEtIdentityCard.getText().toString();
            postIdCardInfoParam.the_name = mEtName.getText().toString();
            showpDialog();
            RequestMethods_square.postIdCardInfo(postIdCardInfoParam, new HttpUtil.Callback<BasePageBean>() {
                @Override
                public void onCallback(BasePageBean result) {
                    if (result != null) {
                        switch (result.ret) {
                            case 0:
                                hidepDialog();
                                showToast(R.string.saved_successfully);
                                IdCardInfoBean.IdCardInfoData idCardInfoData = new IdCardInfoBean.IdCardInfoData();
                                idCardInfoData.the_name = mEtName.getText().toString();
                                idCardInfoData.bank_card_number = mEtBankCard.getText().toString();
                                idCardInfoData.bank_card_type = mEtBank.getText().toString();
                                idCardInfoData.identity_card = mEtIdentityCard.getText().toString();
                                Intent intent = getIntent();
                                intent.putExtra("idCardInfo", idCardInfoData);
                                setResult(RESULT_OK, intent);
                                finish();
                                break;
                            default:
                                hidepDialog();
                                showToast(result.message);
                                break;
                        }
                    }
                }
            });
        } else {
            Intent login = new Intent(mContext, LoginActivity.class);
            startActivity(login);
        }
    }
}
