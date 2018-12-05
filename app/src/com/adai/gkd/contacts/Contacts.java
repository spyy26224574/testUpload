package com.adai.gkd.contacts;

public class Contacts {

    public static final String BaseUrl = "https://www.ligoor.com:443";//"http://www.ligoor.com:8102";  //正式139.224.70.115
//    public static final String BaseUrl = "http://192.168.1.10:8102";    //局域网测试

    //"http://139.224.70.115:8102";//"http://192.168.1.10:8102";//
    /**
     * 升级
     */
    public static final String VERSION_UPDATE = "http://www.ligoor.com/upload_cam/appota/roadcam.xml";
    /**
     * 升级测试
     */
    public static final String BETA_VERSION_UPDATE = "http://www.ligoor.com/upload_cam/appota_beta/roadcam.xml";
    //_________用户________________
    /**
     * 登录url
     */
    public static final String URL_USER_LOGIN = BaseUrl + "/user/login";
    /**
     * 注册
     */
    public static final String URL_USER_REGISTER = BaseUrl + "/user/signup";
    /**
     * 获取用户信息
     */
    public static final String URL_USER_GETUSER_INFO = BaseUrl + "/user/info";
    /**
     * 获取当前用户信息
     */
    public static final String URL_USER_GET_CURRENTUSER = BaseUrl + "/user/view";
    /**
     * 更新用户信息
     */
    public static final String URL_USER_UPDATE = BaseUrl + "/user/update";
    /**
     * 发送验证码
     */
    public static final String URL_USER_SEND_CAPTCHA = BaseUrl + "/user/captcha";
    /**
     * 重置密码
     */
    public static final String URL_USER_RESET_PASWORD = BaseUrl + "/user/resetpwd";
    /**
     * 查找用户
     */
    public static final String URL_USER_SEARCH = BaseUrl + "/user/search";
    /**
     * 上传位置信息
     */
    public static final String URL_USER_UPLOAD_LOCATION = BaseUrl + "/user/location";
    /**
     * 获取位置信息
     */
    public static final String URL_USER_GET_LOCATION = BaseUrl + "/user/location";

    public static final String URL_USER_DEVICE_INFO = BaseUrl + "/device";
    //______________好友_______________________
    /**
     * 添加好友
     */
    public static final String URL_FRIEND_ADD = BaseUrl + "/friend/add";
    /**
     * 删除好友
     */
    public static final String URL_FRIEND_DELETE = BaseUrl + "/friend/delete";
    /**
     * 获取好友列表
     */
    public static final String URL_FRIEND_LIST = BaseUrl + "/friend/list";

    //------------群组------------------
    /**
     * 创建群组
     */
    public static final String URL_GROUP_CREATE = BaseUrl + "/group/create";
    /**
     * 删除群组
     */
    public static final String URL_GROUP_DELETE = BaseUrl + "/group/delete";
    /**
     * 获取群组列表
     */
    public static final String URL_GROUP_LIST = BaseUrl + "/group/list";
    /**
     * 加入群组
     */
    public static final String URL_GROUP_ADD = BaseUrl + "/group/add";
    /**
     * 退出群组
     */
    public static final String URL_GROUP_EXIT = BaseUrl + "/group/exit";

    //-----------行车记录数据----------------
    /**
     * 行车数据上传
     */
    public static final String URL_RECORD_SINGLE = BaseUrl + "/record/single";
    /**
     * 驾始数据上传
     */
    public static final String URL_RECORD_CUSTOM = BaseUrl + "/record/custom";
    /**
     * 行车总结数据上传
     */
    public static final String URL_RECORD_SUMMARY = BaseUrl + "/record/summary";
    /**
     * 异常数据上传
     */
    public static final String URL_RECORD_ABNORMAL = BaseUrl + "/record/abnormal";
    /**
     * 获取车辆信息
     */
    public static final String URL_CAR_INFO = BaseUrl + "/car";
    //--------------广告--------------------------
    /**
     * 点击广告记录
     */
    public static final String URL_ADV_RECORD = BaseUrl + "/advertisement/record";
    /**
     * 获取广告
     */
    public static final String URL_ADV_GET = BaseUrl + "/advertisement";
    /**
     * 上传胎压id
     */
    public static final String URL_RECORD_TIRE = BaseUrl + "/tire/add";

    /**
     * 获取胎压Id列表信息s
     */
    public static final String URL_GET_TIRE = BaseUrl + "/tire/list";
    /**
     * 删除胎压列表信息
     */
    public static final String URL_DELETE_TIRE = BaseUrl + "/tire/delete";
    /**
     * 获取LOGO
     */
    public static final String URL_DEV_GET = BaseUrl + "/factory/logo";
    /**
     * 根据行车记录仪的版本号得到对应的固件版本信息
     */
    public static final String URL_CAMERA_VERSION = BaseUrl + "/factory/model";
    /**
     * 手机号注册接口地址
     */
    public static final String URL_REGISTER_PHONE = BaseUrl + "/user/phoneregister";
    /**
     * 发送手机短信验证码
     */
    public static final String URL_REQUEST_CODE = BaseUrl + "/user/telcaptcha";
    /**
     * 知信找回密码
     */
    public static final String URL_RESET_PWD_PHONE = BaseUrl + "/user/resetpwdnocode";
}
