package com.adai.gkd.contacts;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.adai.gkd.bean.UserInfoBean;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.utils.VoiceManager;

public class CurrentUserInfo {

    public static int id;
    /**
     *
     */
    public static String username;
    public static String password;

    public static String email;
    /**
     *
     */
    public static String nickname;
    /**
     * 性别 S：保密；M:男；F:女
     */
    public static String sex;
    /**
     * 签名
     */
    public static String signature;
    /**
     * 头像
     */
    public static String portrait;
    /**
     * 环信id
     */
    public static String im_id;
    /**
     * 环信密码
     */
    public static String im_password;
    /**
     * access token
     */
    public static String access_token;
    /**
     * 频道
     */
    public static int channel;

    /**
     * 是否开放位置，1：开放；0：不开放
     */
    public static int is_opened;

    private static final String key_id = "key_id";
    private static final String key_username = "key_username";
    private static final String key_email = "key_email";
    private static final String key_nickname = "key_nickname";
    public static final String key_password = "key_password";
    private static final String key_signature = "key_signature";
    private static final String key_sex = "key_sex";
    private static final String key_portrait = "key_portrait";
    private static final String key_im_id = "key_im_id";
    private static final String key_im_password = "key_im_password";
    private static final String key_access_token = "key_access_token";
    private static final String key_is_open = "key_is_open";

    public static final String filename = "loinuserinfo";
    public static final String key_curUsername = "key_curusername";
    public static final String key_cur_password = "key_curpassword";
    public static final String key_cur_code = "key_curcode";

    /**
     * 保存用户信息
     *
     * @param context
     * @param info
     * @return
     */
    public static boolean saveUserinfo(Context context, UserInfoBean info) {
        if (context == null || info == null) return false;
        SharedPreferences pref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        Editor edit = pref.edit();
        edit.putInt(key_id, info.id);
        id = info.id;
        if (!TextUtils.isEmpty(info.username)) {
            edit.putString(key_username, info.username);
            username = info.username;
        }
        if (!TextUtils.isEmpty(info.email)) {
            edit.putString(key_email, info.email);
            email = info.email;
        }
        if (!TextUtils.isEmpty(info.nickname)) {
            edit.putString(key_nickname, info.nickname);
            nickname = info.nickname;
        }
        if (!TextUtils.isEmpty(info.sex)) {
            edit.putString(key_sex, info.sex);
            sex = info.sex;
        }
        if (!TextUtils.isEmpty(info.signature)) {
            edit.putString(key_signature, info.signature);
            signature = info.signature;
        }
        if (!TextUtils.isEmpty(info.portrait)) {
            edit.putString(key_portrait, info.portrait);
            portrait = info.portrait;
        }
        if (!TextUtils.isEmpty(info.im_id)) {
            edit.putString(key_im_id, info.im_id);
            im_id = info.im_id;
        }
        if (!TextUtils.isEmpty(info.im_password)) {
            edit.putString(key_im_password, info.im_password);
            im_password = info.im_password;
        }
        if (!TextUtils.isEmpty(info.access_token)) {
            edit.putString(key_access_token, info.access_token);
            access_token = info.access_token;
        }
        edit.putInt(key_is_open, info.is_opened);
//		loginIm();
        return edit.commit();
    }

    public static void clearUserinfo(Context context) {
        SharedPreferences pref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
//        context.getSharedPreferences("login_password", 0).edit().putString("currentPassword", "").apply();
        pref.edit().clear().apply();
        initUserinfo(context);
    }


    /**
     * 已登录后初始化用户信息
     *
     * @param context
     */
    public static void initUserinfo(final Context context) {
        if (context == null) return;
        SharedPreferences pref = context.getSharedPreferences(filename, Context.MODE_PRIVATE);
        id = pref.getInt(key_id, -1);
        username = pref.getString(key_username, null);
        password = pref.getString(key_password, "");
        email = pref.getString(key_email, null);
        nickname = pref.getString(key_nickname, null);
        signature = pref.getString(key_signature, null);
        portrait = pref.getString(key_portrait, null);
        im_id = pref.getString(key_im_id, null);
        im_password = pref.getString(key_im_password, null);
        access_token = pref.getString(key_access_token, null);
        is_opened = pref.getInt(key_is_open, 1);
        sex = pref.getString(key_sex, "");
        String curUsername = pref.getString(key_curUsername, null);
        String curPassword = pref.getString(key_cur_password, null);
        String curCode = pref.getString(key_cur_code, null);
        if (!TextUtils.isEmpty(curUsername) && !TextUtils.isEmpty(curPassword)) {
            login(context, curUsername, curPassword, curCode);
        }
        if (!TextUtils.isEmpty(access_token)) {
            VoiceManager.isLogin = true;
        }
    }

    private static void login(final Context context, final String curUsername, final String curPassword, final String areaCode) {
        RequestMethods.userLogin(curUsername, curPassword, areaCode,
                new HttpUtil.Callback<UserSingleupPagebean>() {
                    @Override
                    public void onCallback(UserSingleupPagebean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    SharedPreferences pref = context.getSharedPreferences(CurrentUserInfo.filename, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor edit = pref.edit();
                                    edit.putString(key_curUsername, curUsername);
                                    edit.putString(key_cur_password, curPassword);
                                    edit.putString(key_cur_code, areaCode);
                                    edit.apply();
                                    saveUserinfo(context.getApplicationContext(), result.data);
                                    VoiceManager.isLogin = true;
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                });
    }
}
