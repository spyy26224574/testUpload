package com.adai.gkdnavi.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.adai.gkdnavi.LoginActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.widget.LoadingLayout;

/**
 * Created by admin on 2016/8/8.
 */
public class BaseFragment extends Fragment {

    protected final String _TAG_ = this.getClass().getSimpleName();
    private ProgressDialog pd;
    private ProgressDialog hPd;
    protected Activity mContext;
    protected LoadingLayout mLoadingLayout;
    private boolean isViewCreated, isFirstLoad = true;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    protected void wrap() {
        mLoadingLayout = LoadingLayout.wrap(this);
        mLoadingLayout.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRetry();
            }
        });
        showContent();
    }

    protected void onRetry() {
        showLoading();
    }

    protected void showLoading() {
        if (mLoadingLayout != null) {
            mLoadingLayout.showLoading();
        }
    }

    protected void showEmptyContent() {
        if (mLoadingLayout != null) {
            mLoadingLayout.showEmpty();
        }
    }

    protected void showContent() {
        if (mLoadingLayout != null) {
            mLoadingLayout.showContent();
        }
    }

    protected void showError() {
        if (mLoadingLayout != null) {
            mLoadingLayout.showError();
        }
    }

    protected void showToast(@StringRes int res) {
        showToast(getString(res));
    }

    protected void showToast(String str) {
        if (!TextUtils.isEmpty(str) && getActivity() != null) {
            Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean checkLogin() {
        if (!VoiceManager.isLogin) {
            Intent login = new Intent(getActivity(), LoginActivity.class);
            startActivity(login);
            return false;
        } else {
            return true;
        }
    }

    protected void init() {
        pd = new ProgressDialog(mContext);
        pd.setCancelable(false);
        hPd = new ProgressDialog(mContext, R.style.AppTheme);
        hPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        hPd.setCancelable(false);
    }

    protected void showpDialog() {
        if (pd == null) {
            return;
        }
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
    }

    public void setpDialogCancelable(boolean cancelable) {
        if (pd != null) {
            pd.setCancelable(cancelable);
        }
    }

    public void setpDialogDismissListener(DialogInterface.OnDismissListener listener) {
        if (pd != null) {
            pd.setOnDismissListener(listener);
        }
    }

    protected void showpDialog(@StringRes int resId) {
        showpDialog(getString(resId));
    }

    protected void showpDialog(String message) {
        if (pd == null) {
            return;
        }
        pd.setMessage(message);
        pd.show();
    }

    protected void hidepDialog() {
        if (pd == null) {
            return;
        }
        pd.dismiss();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        if (getUserVisibleHint() && isFirstLoad) {
            isFirstLoad = false;
            lazyLoadData();
        }
    }

    protected void onUserVisible(boolean isUserVisible) {

    }

    /**
     * viewpager中嵌套viewpager，内部viewpager的第一个fragment不可见isVisibleToUser也会返回true
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.e(_TAG_, "setUserVisibleHint: " + isVisibleToUser);
        if (isViewCreated && isFirstLoad) {
            isFirstLoad = false;
            lazyLoadData();
        }
        if (isViewCreated) {
            onUserVisible(isVisibleToUser);
        }
    }

    /**
     * 此方法对嵌套的viewpager的第一个fragment无效
     */
    protected void lazyLoadData() {

    }

    /**
     * 更改app类型，0为Hud版，1为camera版
     *
     * @param mode
     */
    protected void onAppmodeChange(int mode) {

    }

    /**
     * 获取当前app类型
     *
     * @return
     */
    public int getCurrentAppmode() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
//		return shareprefrence.getInt("appmode", 0);
        return 0;
    }

    /**
     * 设置appmode
     *
     * @param mode 0 hud,1 camera
     */
    public void setAppmode(int mode) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("appmode", mode).commit();
    }

    /**
     * 设置是否允许语音唤醒
     *
     * @param allow
     */
    public void setAllowWakeup(boolean allow) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putBoolean("isallowwakeup", allow).commit();
    }

    /**
     * 获取是否允许语音唤醒
     *
     * @return
     */
    public boolean getAllowWakeup() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getBoolean("isallowwakeup", false);
    }

    /**
     * 设置地图类型
     *
     * @param model 0为百度导航，1为高德,2百度地图
     */
    public void setMapModel(int model) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor edit = shareprefrence.edit();
        edit.putInt("mapmode", model);
        edit.commit();
    }

    /**
     * 获取地图类型
     *
     * @return
     */
    public int getCurrentMapModel() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getInt("mapmode", 1);
    }

    /**
     * 设置胎压单位 0为bar,1为psi
     *
     * @param type
     */
    public void setTirePresureUnit(int type) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("TirePresureUnit", type).commit();
    }

    /**
     * 获取胎压单位类型
     *
     * @return
     */
    public int getTirePresureUnit() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getInt("TirePresureUnit", 0);
    }

    /**
     * 设置轮胎温度显示类型，0为℃，1为℉ ℉=32+1.8×℃
     *
     * @param type
     */
    public void setTireTempratureUnit(int type) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("TireTempratureUnit", type).commit();
    }

    /**
     * 获取轮胎温度类型
     *
     * @return
     */
    public int getTireTempratureUnit() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getInt("TireTempratureUnit", 0);
    }

    /**
     * 设置与摄像头连接类型
     *
     * @param model 0为摄像头起AP，1为手机端起AP
     */
    public void setNetModel(int model) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("netmode", model).commit();
    }

    /**
     * 获取与摄像头连接类型
     * 0为摄像头起AP，1为手机端起AP
     *
     * @return
     */
    public int getCurrentNetModel() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getInt("netmode", 0);
    }

    /**
     * 设置是否开启软件电子狗
     *
     * @param aimless
     */
    public void setAimless(boolean aimless) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putBoolean("aimless", aimless).commit();
    }

    /**
     * 获取是否允开启软件电子狗
     *
     * @return
     */
    public boolean getAimless() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return shareprefrence.getBoolean("aimless", false);
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
