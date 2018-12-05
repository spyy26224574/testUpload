package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.adai.gkd.bean.IllegalTypeBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.adapter.IllegalTYpeAdapter;

import java.util.ArrayList;

public class IllegalTypeActivity extends BaseActivity implements View.OnClickListener {
    private ListView mLvIllegalType;
    private ArrayList<IllegalTypeBean.DataBean.ItemsBean> mItemsBeens;
    private IllegalTYpeAdapter mIllegalTYpeAdapter;
    private boolean isShowPrice = true;
    private TextView mRightText;
    private IllegalTypeBean.DataBean.ItemsBean mSelectedItemsBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illegal_type);
        init();
        initView();
        getDataFromServer();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
    }

    private void getDataFromServer() {
        RequestMethods_square.getIllegalType(new HttpUtil.Callback<IllegalTypeBean>() {
            @Override
            public void onCallback(IllegalTypeBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (result.data != null && result.data.items != null && result.data.items.size() > 0) {
                                mItemsBeens = result.data.items;
                                mIllegalTYpeAdapter = new IllegalTYpeAdapter(IllegalTypeActivity.this, mItemsBeens, isShowPrice);
                                mLvIllegalType.setAdapter(mIllegalTYpeAdapter);
                            }
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }

                }
            }
        });
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.illegal_type);
        mRightText = (TextView) findViewById(R.id.right_text);
        mRightText.setVisibility(View.VISIBLE);
        mRightText.setText(R.string.ok);
        mLvIllegalType = (ListView) findViewById(R.id.lv_illegal_type);
    }

    private void initEvent() {
        mRightText.setOnClickListener(this);
        mLvIllegalType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedItemsBean = mItemsBeens.get(position);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
                if (mSelectedItemsBean != null) {
                    Intent intent = getIntent();
                    intent.putExtra("selectedType", mSelectedItemsBean);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
        }
    }
}
