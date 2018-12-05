package com.adai.gkdnavi;

import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Locale;

public class UserAgreementActivity extends BaseActivity {

    TextView busDetailContent;
    Context mContext;
    ImageButton logain_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_user_agreement);
        initView();
        mContext = this;
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.software_protocol);
        Locale curLanguage = getCurrentLocale();
        String abc = "";
        if (Locale.SIMPLIFIED_CHINESE.equals(curLanguage)) {
            abc = getFromAssets("useragreement-zh-CN");
        } else if (Locale.TAIWAN.equals(curLanguage)) {
            abc = getFromAssets("useragreement-zh-TW");
        } else {
            abc = getFromAssets("useragreement.txt");
        }
        busDetailContent = (TextView) findViewById(R.id.bus_detail_content);
        busDetailContent.setText(Html.fromHtml(abc));
    }

    @Override
    protected void onStart() {
        super.onStart();
//        busDetailContent.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public String getFromAssets(String fileName) {
        try {
            InputStreamReader inputReader = new InputStreamReader(getResources().getAssets().open(fileName), "utf-8");

            BufferedReader bufReader = new BufferedReader(inputReader);
            String line;
            String Result = "";
            while ((line = bufReader.readLine()) != null) {
                Result = Result + line;
            }

            return Result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
