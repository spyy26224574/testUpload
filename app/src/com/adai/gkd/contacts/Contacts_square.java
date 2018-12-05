package com.adai.gkd.contacts;

/**
 * Created by admin on 2016/8/8.
 * 广场接口地址
 */
public class Contacts_square {
    //正式环境
    private static final String BaseUrl = "https://www.ligoor.com:8443";// "http://www.ligoor.com:8181";  //139.224.70.115

    //测试环境
//    private static final String BaseUrl = "http://192.168.1.10:8181";//局域网测试

    /**
     * 分享视频接口
     */
    public static final String URL_SHARE_VIDEO = BaseUrl + "/rest/upload/otherVideo";

    /**
     * 分享图片接口
     */
    public static final String URL_SHARE_PHOTO = BaseUrl + "/rest/upload/otherPicture";
    /**
     * 删除资源接口
     */
    public static final String URL_DELETE_FILE = BaseUrl + "/rest/file";
    /**
     * 广场主页所有分类视频获取接口
     */
    public static final String URL_CLASSIFY_VIDEO = BaseUrl + "/rest/file";
    /**
     * 获取指定分类视频
     */
    public static final String URL_TYPE_VIDEO = BaseUrl + "/rest/video/list";
    /**
     * 获取最新视频
     */
    public static final String URL_NEWST_VIDEO = BaseUrl + "/rest/otherBrand/newest";
    /**
     * 获取动态
     */
    public static final String URL_DYNAMIC_VIDEO = BaseUrl + "/rest/file/dynamic";
    /**
     * 获取推荐关注人
     */
    public static final String URL_PERSION_RECOMMEND = BaseUrl + "/rest/person/recommend";
    /**
     * 获取视频详情接口
     */
    public static final String URL_VIDEO_DETAIL = BaseUrl + "/rest/video/detail";
    /**
     * 获取图片资源详情
     */
    public static final String URL_PICTURE_DETAIL = BaseUrl + "/rest/picture/detail";
    /**
     * get方式为获取视频更多点赞，post方式为提交点赞,delete方式为取消占赞
     */
    public static final String URL_VIDEO_LIKE = BaseUrl + "/rest/like";
    /**
     * GET方式为获取更多评论，post方式为提交评论
     */
    public static final String URL_VIDEO_REVIEW = BaseUrl + "/rest/review";
    /**
     * post方式为添加关注，delete方式为取消关注
     */
    public static final String URL_ATTENTION_USER = BaseUrl + "/rest/attention";
    /**
     * post方式为添加收藏,delete方式为取消收藏
     */
    public static final String URL_FAVORITE_VIDEO = BaseUrl + "/rest/favorite";

    /**
     * post方式举报视频
     */
    public static final String URL_REPORT_VIDEO = BaseUrl + "/rest/report";
    /**
     * 获取个人资料接口
     */
    public static final String URL_GET_PERSONAL_INFO = BaseUrl + "/rest/person";
    /**
     * 获取个人分享
     */
    public static final String URL_GET_SHARED = BaseUrl + "/rest/video/share";
    /**
     * 获取个人举报
     */
    public static final String URL_GET_REPORT = BaseUrl + "/rest/ppcar/myIllegal";
    /**
     * 获取个人收藏
     */
    public static final String URL_GET_COLLECT = BaseUrl + "/rest/video/collect";
    /**
     * 查看关注及粉丝接口
     */
    public static final String URL_GET_ATTENTION = BaseUrl + "/rest/attention";

    /**
     * 意见反馈
     */
    public static final String URL_FEED_BACK = BaseUrl + "/rest/feedBack";

    /**
     * 违章举报
     */
    public static final String URL_ILLEGAL_REPORT = BaseUrl + "/rest/upload/hudIllegal";
    /**
     * 获取身份证信息接口
     */
    public static final String URL_GET_IDCARD = BaseUrl + "/rest/illegal/getIdCard";
    /**
     * 身份证信息上传接口
     */
    public static final String URL_POST_IDCARD = BaseUrl + "/rest/illegal/addIdCard";
    /**
     * 获取违章信息接口
     */
    public static final String URL_GET_ILLEGAL_TYPE = BaseUrl + "/rest/illegal/violationType";

    /**
     * 查看违章详情接口
     */
    public static final String URL_GET_ILLEGAL_DETAIL = BaseUrl + "/rest/illegal/detail";
    /**
     * 获取服务器摄像头信息
     */
    public static final String URL_GET_CAMERA_INFO = BaseUrl + "/rest/firmwareUpdate";
    /**
     * 上传手机和摄像头信息
     */
    public static final String URL_PHONE_AND_CAM_INFO = BaseUrl + "/rest/publicService/uploadPhoneInfo";
    /**
     * 获取启动logo
     */
    public static final String URL_GET_LOGO = BaseUrl + "/rest/publicService/startLogo";
    /**
     * 获取路拍授权
     */
    public static final String URL_GET_LICENSE = BaseUrl + "/rest/camera/authorize2";
    /**
     * 获取路拍全景顶点数据
     */
    public static final String URL_GET_UPDATE_CORRECT = BaseUrl + "/rest/camera/updateCorrectFile";

    public static final String UTL_CHECK_SESSION = BaseUrl + "/rest/person/checkIsValid";
}
