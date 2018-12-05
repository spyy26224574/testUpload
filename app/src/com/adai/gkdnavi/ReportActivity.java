package com.adai.gkdnavi;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;

/**
 * 举报页面
 */
public class ReportActivity extends BaseActivity implements View.OnClickListener {

    public static final String KEY_RESOURCE_ID="resouce_id";
    private TextView right_text;
    private RadioGroup resons;

    private int resourceid=-1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        right_text=(TextView)findViewById(R.id.right_text);
        right_text.setVisibility(View.VISIBLE);
        right_text.setOnClickListener(this);
        findViewById(R.id.right_img).setVisibility(View.GONE);
        resons=(RadioGroup)findViewById(R.id.resons);
    }

    @Override
    protected void init() {
        super.init();
        setTitle(R.string.select_reason);
        resourceid=getIntent().getIntExtra(KEY_RESOURCE_ID,-1);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.right_text:
                sendReport();
                break;
        }
    }

    private void sendReport(){
        int id=resons.getCheckedRadioButtonId();
        int resontype=-1;
        switch (id){
            case R.id.reson1:
                resontype=1;
                break;
            case R.id.reson2:
                resontype=2;
                break;
            case R.id.reson3:
                resontype=3;
                break;
            case R.id.reson4:
                resontype=4;
                break;
            case R.id.reson5:
                resontype=5;
                break;
            case R.id.reson6:
                resontype=6;
                break;
        }
        if(resontype==-1){
            showToast(R.string.select_report_reason);
            return;
        }
        RequestMethods_square.reportResource(resourceid, resontype, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if(result!=null){
                    switch (result.ret){
                        case 0:
                            showToast(R.string.report_success);
                            finish();
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
    }
}
