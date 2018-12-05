package com.adai.gkdnavi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkdnavi.utils.VoiceManager;

/**
 * Created by admin on 2016/8/10.
 */
public class BaseFragmentActivity extends FragmentActivity {

    protected Context mContext;
    private TextView title;
    private ImageView back,right;
    private ProgressDialog pd;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext=this;
    }

    protected void initView(){
        initTitle();
    }
    protected void init() {
        pd=new ProgressDialog(mContext);
    }

    protected void showpDialog() {
        if(pd==null)return;
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
    }
    protected void showpDialog(String message) {
        if(pd==null)return;
        pd.setMessage(message);
        pd.show();
    }
    protected void showpDialog(int resId) {
        if(pd==null)return;
        pd.setMessage(getString(resId));
        pd.show();
    }
    protected void hidepDialog() {
        if(pd==null)return;
        pd.dismiss();
    }
    private void initTitle() {
        View titletemp=findViewById(R.id.head_title);
        if(titletemp!=null){
            title=(TextView)titletemp;
        }
        View backTemp=findViewById(R.id.back);
        if(backTemp!=null){
            back=(ImageView)backTemp;
            back.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    goBack();
                }
            });
        }
    }

    public void setTitle(@StringRes int id){
        setTitle(mContext.getString(id));
    }
    protected void setTitle(String titlestr) {
        if(title!=null){
            title.setText(titlestr);
        }
    }
    /**
     * 后退
     */
    protected void goBack() {
        finish();
    }
    protected void  showToast(int resid) {
        showToast(mContext.getString(resid));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            goBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void showToast(String str){
        if(TextUtils.isEmpty(str))return;
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    public boolean checkLogin(){
        if(!VoiceManager.isLogin){
            Intent login=new Intent(mContext, LoginActivity.class);
            startActivity(login);
            return false;
        }else {
            return true;
        }
    }

    /**
     * 设置全景视频解码方式
     *
     * @param decoding_type 0:软解码 1：硬解码
     */
    public void setDecodingType(int decoding_type) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("decoding_type", decoding_type).commit();
    }

    /**
     * 获取全景视频解码方式
     *
     * @return 0:软解码 1：硬解码
     */
    public int getDecodingType() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getInt("decoding_type", 0);
    }
}
