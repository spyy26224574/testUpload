package com.adai.gkd.contacts;

import android.text.TextUtils;

import com.adai.gkd.bean.AbnormalInfoBean;
import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.CustomInfoBean;
import com.adai.gkd.bean.LicenseBean;
import com.adai.gkd.bean.LogoBean;
import com.adai.gkd.bean.SingleInfoBean;
import com.adai.gkd.bean.SummaryInfoBean;
import com.adai.gkd.bean.UpdateCorrectBean;
import com.adai.gkd.bean.UserInfoBean;
import com.adai.gkd.bean.params.AbnormalParam;
import com.adai.gkd.bean.params.AdvertisementParam;
import com.adai.gkd.bean.params.AdvertisementRecordParam;
import com.adai.gkd.bean.params.CheckSessionParam;
import com.adai.gkd.bean.params.DeviceInfoParam;
import com.adai.gkd.bean.params.FriendAddParam;
import com.adai.gkd.bean.params.GetCameraInfoParam;
import com.adai.gkd.bean.params.GetCameraVersionParam;
import com.adai.gkd.bean.params.GetDeviceIdParam;
import com.adai.gkd.bean.params.GetLicenseParam;
import com.adai.gkd.bean.params.GetLocationParam;
import com.adai.gkd.bean.params.GetLogoParam;
import com.adai.gkd.bean.params.GetUpdateCorrectParam;
import com.adai.gkd.bean.params.PhoneAndCamParam;
import com.adai.gkd.bean.params.PhoneCodeParam;
import com.adai.gkd.bean.params.PhoneRegisterParam;
import com.adai.gkd.bean.params.PhoneResetpwdParam;
import com.adai.gkd.bean.params.RecordSingleParam;
import com.adai.gkd.bean.params.RecordSummaryParam;
import com.adai.gkd.bean.params.UpdateUserParams;
import com.adai.gkd.bean.params.UploadLocationParam;
import com.adai.gkd.bean.params.UserInfoParam;
import com.adai.gkd.bean.params.UserLoginParam;
import com.adai.gkd.bean.params.UserResetPwdParam;
import com.adai.gkd.bean.params.UserSearchParam;
import com.adai.gkd.bean.params.UserSendCodeParam;
import com.adai.gkd.bean.params.UserSingleupParam;
import com.adai.gkd.bean.request.AdvertisementPagebean;
import com.adai.gkd.bean.request.CameraVersionBean;
import com.adai.gkd.bean.request.CarInfoPagebean;
import com.adai.gkd.bean.request.CheckSessionBean;
import com.adai.gkd.bean.request.DeviceIdPagebean;
import com.adai.gkd.bean.request.DeviceInfoPageBean;
import com.adai.gkd.bean.request.PhoneAndCameraPageBean;
import com.adai.gkd.bean.request.RecordSinglePagebean;
import com.adai.gkd.bean.request.UserLocationPageBean;
import com.adai.gkd.bean.request.UserSearchPagebean;
import com.adai.gkd.bean.request.UserSingleupPagebean;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkd.httputils.HttpUtil.Callback;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.util.List;

/**
 * 网络请求方法类
 *
 * @author admin
 */
public class RequestMethods {

    /**
     * 用户注册
     *
     * @param username 用户名
     * @param pwd      用户密码
     * @param email    邮箱地址
     * @param callback
     */
    public static void userSingle(String username, String pwd, String email, Callback<UserSingleupPagebean> callback) {
        UserSingleupParam params = new UserSingleupParam();
        params.email = email;
        params.password = pwd;
        params.username = username;
        HttpUtil.getInstance().requestPost(Contacts.URL_USER_REGISTER, params, UserSingleupPagebean.class, callback);
    }

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param pwd      用户密码
     * @param callback
     */
    public static void userLogin(String username, String pwd, String areaCode, Callback<UserSingleupPagebean> callback) {
        UserLoginParam params = new UserLoginParam();
        params.username = username;
        params.password = pwd;
        params.areaCode = areaCode;
        HttpUtil.getInstance().requestPost(Contacts.URL_USER_LOGIN, params, UserSingleupPagebean.class, callback);
    }

    /**
     * 获取用户信息
     *
     * @param id       用户id
     * @param callback
     */
    public static void userInfo(int id, Callback<UserSingleupPagebean> callback) {
        UserInfoParam params = new UserInfoParam();
        params.id = id;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_GETUSER_INFO, params, UserSingleupPagebean.class, callback);
    }

    /**
     * 获取当前用户信息
     *
     * @param callback
     */
    public static void userCurrent(Callback<UserSingleupPagebean> callback) {
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_GET_CURRENTUSER, null, UserSingleupPagebean.class, callback);
    }

    /**
     * 更新用户信息
     *
     * @param headpath 头像图片地址
     * @param nickname 昵称
     * @param callback
     */
    public static void userUpdate(String headpath, String nickname, String sex, String signature, Callback<UserSingleupPagebean> callback) {

        UpdateUserParams params = new UpdateUserParams();
        if (!TextUtils.isEmpty(headpath)) {
            File head = new File(headpath);
            params.image = head;
        }
        params.nickname = nickname;
        params.signature = signature;
        params.sex = sex;
        HttpUtil.getInstance().requestPostWithFile(Contacts.URL_USER_UPDATE, params, UserSingleupPagebean.class, callback);
    }

    /**
     * 位置共享开关
     *
     * @param is_open  是否开放位置，1：开放；0：不开放
     * @param callback
     */
    public static void userLocationShare(int is_open, Callback<UserSingleupPagebean> callback) {
        UpdateUserParams params = new UpdateUserParams();
        params.is_opened = is_open;
        HttpUtil.getInstance().requestPostWithFile(Contacts.URL_USER_UPDATE, params, UserSingleupPagebean.class, callback);
    }

    /**
     * 发送验证码
     *
     * @param email    邮箱地址
     * @param callback
     */
    public static void userSendCode(String email, Callback<BasePageBean> callback) {
        UserSendCodeParam params = new UserSendCodeParam();
        params.email = email;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_SEND_CAPTCHA, params, BasePageBean.class, callback);
    }

    /**
     * 重置密码
     *
     * @param email    邮箱
     * @param code     验正码
     * @param pwd      新密码
     * @param callback
     */
    public static void userResetpwd(String email, String code, String pwd, Callback<BasePageBean> callback) {
        UserResetPwdParam params = new UserResetPwdParam();
        params.code = code;
        params.email = email;
        params.password = pwd;
        HttpUtil.getInstance().requestPost(Contacts.URL_USER_RESET_PASWORD, params, BasePageBean.class, callback);
    }

    /**
     * 查找用户
     *
     * @param key
     * @param callback
     */
    public static void userSearch(String key, Callback<UserSearchPagebean> callback) {
        UserSearchParam params = new UserSearchParam();
        params.key = key;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_SEARCH, params, UserSearchPagebean.class, callback);
    }

    /**
     * 上传位置信息
     *
     * @param lon
     * @param lat
     * @param callback
     */
    public static void userUploadLocation(double lon, double lat, Callback<BasePageBean> callback) {
        UploadLocationParam params = new UploadLocationParam();
        params.latitude = lat;
        params.longitude = lon;
        HttpUtil.getInstance().requestPost(Contacts.URL_USER_UPLOAD_LOCATION, params, BasePageBean.class, callback);
    }

    /**
     * 获取位置信息
     *
     * @param callback
     * @param usernames 用户名列表
     */
    public static void userGetLocation(Callback<UserLocationPageBean> callback, String... usernames) {
        if (usernames == null) return;
        StringBuffer idsb = new StringBuffer();
        for (String id : usernames) {
            idsb.append(id).append(",");
        }
        GetLocationParam params = new GetLocationParam();
        params.ids = idsb.toString();
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_GET_LOCATION, params, UserLocationPageBean.class, callback);
    }

    /**
     * 根据设备号获取设备信息
     *
     * @param deviceid
     * @param callback
     */
    public static void userDeviceInfo(String deviceid, Callback<DeviceInfoPageBean> callback) {
        if (deviceid == null) return;
        DeviceInfoParam params = new DeviceInfoParam();
        params.deviceid = deviceid;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_DEVICE_INFO, params, DeviceInfoPageBean.class, callback);
    }

    /**
     * 添加好友
     *
     * @param friend_id 用户id
     * @param callback
     */
    public static void friendAdd(int friend_id, Callback<BasePageBean> callback) {
        FriendAddParam params = new FriendAddParam();
        params.friend_id = friend_id;
        HttpUtil.getInstance().requestPost(Contacts.URL_FRIEND_ADD, params, BasePageBean.class, callback);
    }

    /**
     * 根据用户名添加好友
     *
     * @param username 用户名
     * @param callback
     */
    public static void friendAdd(final String username, final Callback<BasePageBean> callback) {
        UserSearchParam params = new UserSearchParam();
        params.key = username;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_SEARCH, params, UserSearchPagebean.class, new Callback<UserSearchPagebean>() {

            @Override
            public void onCallback(UserSearchPagebean result) {
                if (result != null && result.ret == 0 && result.data != null) {
                    for (UserInfoBean info : result.data) {
                        if (username.equals(info.username)) {
                            FriendAddParam params = new FriendAddParam();
                            params.friend_id = info.id;
                            HttpUtil.getInstance().requestPost(Contacts.URL_FRIEND_ADD, params, BasePageBean.class, callback);
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * 删除好友
     *
     * @param friend_id 好友用户id
     * @param callback
     */
    public static void friendDelete(int friend_id, Callback<BasePageBean> callback) {
        FriendAddParam params = new FriendAddParam();
        params.friend_id = friend_id;
        HttpUtil.getInstance().requestPost(Contacts.URL_FRIEND_DELETE, params, BasePageBean.class, callback);
    }

    /**
     * 根据用户名删除好友
     *
     * @param username
     * @param callback
     */
    public static void friendDelete(final String username, final Callback<BasePageBean> callback) {
        UserSearchParam params = new UserSearchParam();
        params.key = username;
        HttpUtil.getInstance().requestGet(Contacts.URL_USER_SEARCH, params, UserSearchPagebean.class, new Callback<UserSearchPagebean>() {

            @Override
            public void onCallback(UserSearchPagebean result) {
                if (result != null && result.ret == 0 && result.data != null) {
                    for (UserInfoBean info : result.data) {
                        if (username.equals(info.username)) {
                            FriendAddParam params = new FriendAddParam();
                            params.friend_id = info.id;
                            HttpUtil.getInstance().requestPost(Contacts.URL_FRIEND_DELETE, params, BasePageBean.class, callback);
                            break;
                        }
                    }
                }
            }
        });
    }


    /**
     * 行车实时数据上传
     *
     * @param data
     * @param callback
     */
    public static void recordSingle(List<SingleInfoBean> data, Callback<RecordSinglePagebean> callback) {
        if (data == null || data.size() == 0) return;
        RecordSingleParam params = new RecordSingleParam();
        params.data = data;
        HttpUtil.getInstance().requestPost(Contacts.URL_RECORD_SINGLE, params, RecordSinglePagebean.class, callback);
    }

    /**
     * 驾驶习惯数据上传
     *
     * @param data
     * @param callback
     */
    public static void recordCustom(List<CustomInfoBean> data, Callback<RecordSinglePagebean> callback) {
        if (data == null || data.size() == 0) return;
        RecordSingleParam params = new RecordSingleParam();
//		params.data=new Gson().toJson(data);
        HttpUtil.getInstance().requestPost(Contacts.URL_RECORD_CUSTOM, params, RecordSinglePagebean.class, callback);
    }

    /**
     * 行车总结数据上传
     *
     * @param data
     * @param callback
     */
    public static void recordSummary(List<SummaryInfoBean> data, Callback<RecordSinglePagebean> callback) {
        if (data == null || data.size() == 0) return;
        RecordSummaryParam params = new RecordSummaryParam();
//		params.data=new Gson().toJson(data);
        params.data = data;
        HttpUtil.getInstance().requestPost(Contacts.URL_RECORD_SUMMARY, params, RecordSinglePagebean.class, callback);
    }

    /**
     * 异常数据上传
     *
     * @param data
     * @param callback
     */
    public static void recordAbnormal(List<AbnormalInfoBean> data, Callback<RecordSinglePagebean> callback) {
        if (data == null || data.size() == 0) return;
        AbnormalParam params = new AbnormalParam();
        params.data = data;
        HttpUtil.getInstance().requestPost(Contacts.URL_RECORD_ABNORMAL, params, RecordSinglePagebean.class, callback);
    }

    /**
     * 获取车辆信息
     *
     * @param callback
     */
    public static void carInfo(Callback<CarInfoPagebean> callback) {
        HttpUtil.getInstance().requestGet(Contacts.URL_CAR_INFO, null, CarInfoPagebean.class, callback);
    }

    /**
     * 获取广告
     *
     * @param type        广告类型，1:引导页广告，2：导航栏广告
     * @param arrangement 广告序号
     * @param callback
     */
    public static void advertisementGet(int type, int arrangement, Callback<AdvertisementPagebean> callback) {
        AdvertisementParam params = new AdvertisementParam();
        params.arrangement = arrangement;
        params.type = type;
        params.version_category = VLCApplication.getAppContext().getPackageName();
        HttpUtil.getInstance().requestGet(Contacts.URL_ADV_GET, params, AdvertisementPagebean.class, callback);
    }

    /**
     * 上传广告点击记录
     *
     * @param advertisement_id 广告id
     * @param user_id
     * @param deviceid
     * @param callback
     */
    public static void advertisementRecord(int advertisement_id, int user_id, String deviceid, Callback<BasePageBean> callback) {
        AdvertisementRecordParam params = new AdvertisementRecordParam();
        params.advertisement_id = advertisement_id;
        params.user_id = user_id;
        params.deviceid = deviceid;
        params.version_category = "02";//01：hud，02：cam
        HttpUtil.getInstance().requestPost(Contacts.URL_ADV_RECORD, params, BasePageBean.class, callback);
    }


    /**
     * 获取device_id等信息
     */
    public static void getDeviceId(String device_id, Callback<DeviceIdPagebean> callback) {
        GetDeviceIdParam deviceidParam = new GetDeviceIdParam();
        deviceidParam.deviceid = device_id;
        HttpUtil.getInstance().requestGet(Contacts.URL_DEV_GET, deviceidParam, DeviceIdPagebean.class, callback);
    }

    /**
     * 获取camera信息
     */
    public static void getCameraVersion(String factory_model, Callback<CameraVersionBean> callBack) {
        GetCameraVersionParam cameraVersionParam = new GetCameraVersionParam();
        cameraVersionParam.factory_model = factory_model;
        HttpUtil.getInstance().requestGet(Contacts.URL_CAMERA_VERSION, cameraVersionParam, CameraVersionBean.class, callBack);
    }

    public static void getCameraInfo(String deviceInfo, Callback<CameraVersionBean> callback) {
        GetCameraInfoParam getCameraInfoParam = new GetCameraInfoParam();
        getCameraInfoParam.deviceInfo = deviceInfo;
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_CAMERA_INFO, getCameraInfoParam, CameraVersionBean.class, callback);
    }

    public static void getLogo(String packageName, String languageCode, String identity, Callback<LogoBean> callback) {
        GetLogoParam getLogoParam = new GetLogoParam();
        getLogoParam.packageName = packageName;
        getLogoParam.languageCode = languageCode;
        getLogoParam.identity = identity;
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_LOGO, getLogoParam, LogoBean.class, callback);
    }

    public static void getLicense(String mac, String package_name, Callback<LicenseBean> callback) {
        GetLicenseParam getLicenseParam = new GetLicenseParam();
        getLicenseParam.mac = mac;
        getLicenseParam.packageName = package_name;
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_LICENSE, getLicenseParam, LicenseBean.class, callback);
    }

    public static void getUpdateCorrect(String soft_version, Callback<UpdateCorrectBean> callback) {
        GetUpdateCorrectParam getUpdateCorrectParam = new GetUpdateCorrectParam();
        getUpdateCorrectParam.soft_version = soft_version;
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_UPDATE_CORRECT, getUpdateCorrectParam, UpdateCorrectBean.class, callback);
    }

    /**
     * 上传手机和摄像头信息
     *
     * @param phoneAndCamParam 参数
     * @param callBack         回调
     */
    public static void postPhoneAndCamInfo(PhoneAndCamParam phoneAndCamParam, Callback<PhoneAndCameraPageBean> callBack) {
        HttpUtil.getInstance().requestPost(Contacts_square.URL_PHONE_AND_CAM_INFO, phoneAndCamParam, PhoneAndCameraPageBean.class, callBack);
    }

    /**
     * 获取手机验证码
     *
     * @param phone
     * @param callback
     */
    public static void requestPhoneCode(String phone, Callback<BasePageBean> callback) {
        PhoneCodeParam param = new PhoneCodeParam();
        param.phone = phone;
        HttpUtil.getInstance().requestGet(Contacts.URL_REQUEST_CODE, param, BasePageBean.class, callback);
    }

    /**
     * 手机号注册
     *
     * @param param
     * @param callback
     */
    public static void registerByPhone(PhoneRegisterParam param, Callback<UserSingleupPagebean> callback) {
        HttpUtil.getInstance().requestPost(Contacts.URL_REGISTER_PHONE, param, UserSingleupPagebean.class, callback);
    }

    /**
     * 手机号码找回密码
     *
     * @param param
     * @param callback
     */
    public static void resetpwdByPhone(PhoneResetpwdParam param, Callback<BasePageBean> callback) {
        HttpUtil.getInstance().requestPost(Contacts.URL_RESET_PWD_PHONE, param, BasePageBean.class, callback);
    }

    /**
     * 检测token是否过期
     *
     * @param userId 用户id
     */
    public static void checkSessionValid(int userId, Callback<CheckSessionBean> callback) {
        CheckSessionParam checkSessionParam = new CheckSessionParam();
        checkSessionParam.userId = userId;
        HttpUtil.getInstance().requestGet(Contacts_square.UTL_CHECK_SESSION, checkSessionParam, CheckSessionBean.class, callback);
    }
}
