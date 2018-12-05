package com.adai.gkdnavi;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.umeng.analytics.MobclickAgent;

import java.lang.reflect.Field;
import java.util.Locale;

public class BaseActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected final String _TAG_ = this.getClass().getSimpleName();

    protected Context mContext;
    private TextView title;
    private ImageView back, right;
    private ProgressDialog pd;
    private ProgressDialog hPd;
    private AlertDialog mAlertDialog;
    private View mDecorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCurrentLanguage();
        mContext = this;
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        hidepDialog();
        hideAlertMessage();
        PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
        hideSoftInput();
        fixInputMethodManagerLeak(this);
        super.onDestroy();
    }

    public static void fixInputMethodManagerLeak(Context destContext) {
        if (destContext == null) {
            return;
        }

        InputMethodManager inputMethodManager = (InputMethodManager) destContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager == null) {
            return;
        }

        String[] viewArray = new String[]{"mCurRootView", "mServedView", "mNextServedView"};
        Field filed;
        Object filedObject;

        for (String view : viewArray) {
            try {
                filed = inputMethodManager.getClass().getDeclaredField(view);
                if (!filed.isAccessible()) {
                    filed.setAccessible(true);
                }
                filedObject = filed.get(inputMethodManager);
                if (filedObject != null && filedObject instanceof View) {
                    View fileView = (View) filedObject;
                    if (fileView.getContext() == destContext) { // 被InputMethodManager持有引用的context是想要目标销毁的
                        filed.set(inputMethodManager, null); // 置空，破坏掉path to gc节点
                    } else {
                        break;// 不是想要目标销毁的，即为又进了另一层界面了，不要处理，避免影响原逻辑,也就不用继续for循环了
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    protected void setCurrentLanguage() {
        int language = SpUtils.getInt(this, "language", -1);
        if (language == -1) {
            return;
        }
        switchLanguage(language);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setCurrentLanguage();
    }

    protected String getLanguage() {
        Locale locale = getCurrentLocale();
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    protected Locale getCurrentLocale() {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList locales = getResources().getConfiguration().getLocales();
            locale = locales.get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        return locale;
    }

    public void hideSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && mDecorView != null) {
            inputMethodManager.hideSoftInputFromWindow(mDecorView.getWindowToken(), 0);
        }
    }

    public void toggleSoftInput() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (inputMethodManager != null) {
            inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    protected boolean isSimplifiedChinese() {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList locales = getResources().getConfiguration().getLocales();
            locale = locales.get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        return Locale.SIMPLIFIED_CHINESE.getLanguage().equals(locale.getLanguage());
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        ImageLoader.getInstance().pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        ImageLoader.getInstance().resume();
    }

    /**
     * 设置语言
     *
     * @param language
     */
    private void switchLanguage(int language) {
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        switch (language) {
            case 0:
                config.locale = Locale.SIMPLIFIED_CHINESE;
                break;
            case 1:
                config.locale = Locale.TAIWAN;
                break;
            case 2:
                config.locale = Locale.ENGLISH;
                break;
        }
        resources.updateConfiguration(config, metrics);
        SpUtils.putInt(this, "language", language);
    }

    /**
     * 隐藏虚拟按键
     */
    protected void hideNavigation() {
        if (Build.VERSION.SDK_INT >= 14) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    protected void initView() {
        initTitle();
    }

    protected void init() {
        mDecorView = getWindow().getDecorView();
        pd = new ProgressDialog(mContext);
        hPd = new ProgressDialog(mContext);
        hPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    protected void showAlertMessage(@StringRes int messageId) {
        showAlertMessage(getString(messageId));
    }

    protected void showAlertMessage(String message) {
        showAlertDialog(message, null, null);
    }

    protected void showAlertDialog(String message, DialogInterface.OnClickListener positiveClick, DialogInterface.OnClickListener negativeClick) {
        try {
            if (mAlertDialog == null) {
                mAlertDialog = new AlertDialog.Builder(this)
                        .setMessage(message)
                        .setPositiveButton(R.string.ok, positiveClick)
                        .setNegativeButton(R.string.no, negativeClick)
                        .show();
            } else {
                if (!mAlertDialog.isShowing()) {
                    mAlertDialog.setMessage(message);
                    mAlertDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void hideAlertMessage() {
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }

    protected void showpDialog() {
        showpDialog(getString(R.string.please_wait));
    }

    protected void showCancelablepDialog(String message) {
        if (pd == null) {
            return;
        }
        try {
            pd.setCancelable(true);
            pd.setMessage(message);
            pd.show();
        } catch (Exception ignore) {

        }
    }

    protected void showCancelablepDialog(@StringRes int resId) {
        showCancelablepDialog(getString(resId));
    }

    protected void showpDialog(String message) {
        if (pd == null) {
            return;
        }
        try {
            pd.setCancelable(false);
            pd.setMessage(message);
            pd.show();
        } catch (Exception ignore) {

        }
    }

    protected void showpDialog(@StringRes int resId) {
        showpDialog(getString(resId));
    }

    protected void hidepDialog() {
        if (pd == null) {
            return;
        }
        pd.dismiss();
    }

    protected void showHpDialog() {
        showHpDialog(getString(R.string.crop__wait));
    }

    protected void showHpDialog(String message) {
        if (hPd == null) {
            return;
        }
        try {
            hPd.setMessage(message);
            hPd.show();
        } catch (Exception ignore) {

        }
    }

    protected void hideHpDialog() {
        if (hPd == null) {
            return;
        }
        hPd.dismiss();
    }

    protected void setDialogProgress(int progress) {
        if (hPd == null) {
            return;
        }
        hPd.setProgress(progress);
    }

    protected void setDialogMax(int max) {
        if (hPd == null) {
            return;
        }
        hPd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        hPd.setMax(max);
    }

    protected void setHpDialogCancelable(boolean cancelable) {
        if (hPd == null) {
            return;
        }
        hPd.setCancelable(cancelable);
    }

    private void initTitle() {
        View titletemp = findViewById(R.id.head_title);
        if (titletemp != null) {
            title = (TextView) titletemp;
        }
        View backTemp = findViewById(R.id.back);
        if (backTemp != null) {
            backTemp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goBack();
                }
            });
        }
    }

    protected void setTitle(String titlestr) {
        if (title != null) {
            title.setText(titlestr);
        }
    }

    @Override
    public void setTitle(@StringRes int id) {
        setTitle(getString(id));
    }

    /**
     * 后退
     */
    protected void goBack() {
        finish();
    }

    protected void showToastOnUiThread(@StringRes final int res) {
        showToastOnUiThread(mContext.getString(res));
    }

    protected void showToastOnUiThread(final String message) {
        UIUtils.post(new Runnable() {
            @Override
            public void run() {
                showToast(message);
            }
        });
    }

    protected void showToast(int resid) {
        showToast(mContext.getString(resid));
    }

    protected void showToast(String str) {
        if (TextUtils.isEmpty(str)) {
            return;
        }
        ToastUtil.showShortToast(mContext, str);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            goBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void startActivity(Class<?> cls) {
        Intent it = new Intent(mContext, cls);
        mContext.startActivity(it);
    }

    /**
     * 更改app类型，0为Hud版，1为camera版
     *
     * @param mode
     */
    protected void onAppmodeChange(int mode) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if ("appmode".equals(key)) {
            onAppmodeChange(sharedPreferences.getInt(key, 0));
        } else if ("isallowwakeup".equals(key)) {
            onAllowWakeup(sharedPreferences.getBoolean(key, true));
        }
    }

    /**
     * 获取当前app类型
     *
     * @return
     */
    public int getCurrentAppmode() {
        //SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        return 1;
    }

    /**
     * 设置appmode
     *
     * @param mode 0hud,1camera
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

    protected void onAllowWakeup(boolean allowwakeup) {

    }

    /**
     * 设置地图类型
     *
     * @param model 0为百度，1为高德
     */
    public void setMapModel(int model) {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(mContext);
        shareprefrence.edit().putInt("mapmode", model).commit();
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
