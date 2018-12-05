package com.adai.gkd.contacts;

import android.support.annotation.NonNull;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.IllegalTypeBean;
import com.adai.gkd.bean.params.AddReviewParam;
import com.adai.gkd.bean.params.AttentionParam;
import com.adai.gkd.bean.params.BaseListParam;
import com.adai.gkd.bean.params.BaseResourceParam;
import com.adai.gkd.bean.params.BaseUserResourceListParam;
import com.adai.gkd.bean.params.ClassifyVideoParam;
import com.adai.gkd.bean.params.FeedbackParam;
import com.adai.gkd.bean.params.GetIdCardInfoParam;
import com.adai.gkd.bean.params.GetIllegalDetailParam;
import com.adai.gkd.bean.params.IllegalReportParam;
import com.adai.gkd.bean.params.NewsListParam;
import com.adai.gkd.bean.params.PersonalInfoParam;
import com.adai.gkd.bean.params.PostIdCardInfoParam;
import com.adai.gkd.bean.params.ReprotParam;
import com.adai.gkd.bean.params.RequestUserParam;
import com.adai.gkd.bean.params.ResorceInfoParam;
import com.adai.gkd.bean.params.SharePhotoParam;
import com.adai.gkd.bean.params.ShareVideoParam;
import com.adai.gkd.bean.params.TypeVideoParam;
import com.adai.gkd.bean.params.VideoDetailParam;
import com.adai.gkd.bean.request.IdCardInfoBean;
import com.adai.gkd.bean.square.ClassifyVideoPageBean;
import com.adai.gkd.bean.square.LikePageBean;
import com.adai.gkd.bean.square.PersonalInfoPageBean;
import com.adai.gkd.bean.square.RecommanPageBean;
import com.adai.gkd.bean.square.ReviewPageBean;
import com.adai.gkd.bean.square.ShareVideoPageBean;
import com.adai.gkd.bean.square.TypeVideoListPageBean;
import com.adai.gkd.bean.square.VideoDetailPageBean;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.BuildConfig;
import com.adai.camera.CameraConstant;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.SystemUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by admin on 2016/8/8.
 */
public class RequestMethods_square {

    /**
     * 分享视频接口封装方法
     *
     * @param videoPath  视频本地路径
     * @param logoPath   封面路径
     * @param des        视频描述
     * @param videoTime  视频时长
     * @param coordinate 地址名称
     * @param lat
     * @param lon
     * @param isopen     是否公开
     * @param isReview   是否允许评论
     * @param callback
     */
    public static void shareVideo(@NonNull String videoPath, @NonNull String logoPath, String des,
                                  int videoTime, String coordinate, String lat, String lon, int videoType, int fishEyeId, int width, int height,
                                  String isopen, String isReview, HttpUtil.Callback<ShareVideoPageBean> callback) {
        ShareVideoParam params = new ShareVideoParam();
//        params.file = new File(videoPath);
//        params.picture = new File(logoPath);
        String[] videoSplits = videoPath.split("/");
        String videoString = videoSplits[videoSplits.length - 2] + "/" + videoSplits[videoSplits.length - 1];
        String[] logoSplits = logoPath.split("/");
        String logoString = logoSplits[logoSplits.length - 2] + "/" + logoSplits[logoSplits.length - 1];
        params.videoName = videoString;
        params.pictureName = logoString;
        params.coordinate = coordinate;
        params.des = des;
        params.latitude = lat;
        params.longitude = lon;
        params.isOpen = isopen;
        params.isReview = isReview;
        params.videoTime = videoTime;
        params.packageName = BuildConfig.APPLICATION_ID;
        params.deviceInfo = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_VERSION_CURRENT, "");
        params.languageCode = VLCApplication.getInstance().getLanguage();
        params.videoType = videoType;
        params.fishEyeId = fishEyeId;
        params.width = width;
        params.height = height;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_SHARE_VIDEO, params, ShareVideoPageBean.class, callback);
    }

    public static void sharePhoto(@NonNull List<String> photos, String des, String coordinate, String lat, String lon,
                                  String isopen, String isReview, HttpUtil.Callback<ShareVideoPageBean> callback) {
        SharePhotoParam params = new SharePhotoParam();
//        for (String photo : photos) {
//            params.file.add(new File(photo));
//        }
        for (String photo : photos) {
            String[] splits = photo.split("/");
            String tempString = splits[splits.length - 2] + "/" + splits[splits.length - 1];
            params.pictureName += tempString + ";";
        }
        params.languageCode = VLCApplication.getInstance().getLanguage();
        params.coordinate = coordinate;
        params.des = des;
        params.latitude = lat;
        params.longitude = lon;
        params.isOpen = isopen;
        params.isReview = isReview;
        params.packageName = BuildConfig.APPLICATION_ID;
        params.deviceInfo = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_VERSION_CURRENT, "");
        HttpUtil.getInstance().requestPost(Contacts_square.URL_SHARE_PHOTO, params, ShareVideoPageBean.class, callback);
    }

    /**
     * 获取所有视频分类
     *
     * @param callback
     */
    public static void getClassifyVideo(HttpUtil.Callback<ClassifyVideoPageBean> callback) {
        ClassifyVideoParam classifyVideoParam = new ClassifyVideoParam();
        classifyVideoParam.packageName = BuildConfig.APPLICATION_ID;
        classifyVideoParam.languageCode = VLCApplication.getInstance().getLanguage();
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_CLASSIFY_VIDEO, classifyVideoParam, ClassifyVideoPageBean.class, callback);
    }

    /**
     * 获取单个分类视频集合`
     *
     * @param typeid
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getTypeVideo(int typeid, int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        TypeVideoParam params = new TypeVideoParam();
        params.typeId = typeid;
        params.page = page;
        params.per_page = pagesize;
        params.packageName = BuildConfig.APPLICATION_ID;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_TYPE_VIDEO, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取最新视频
     *
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getNewstVideo(int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        NewsListParam params = new NewsListParam();
        params.page = page;
        params.per_page = pagesize;
        try {
            params.deviceInfo = URLEncoder.encode(SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_VERSION_CURRENT, ""), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        params.packageName = BuildConfig.APPLICATION_ID;
        params.languageCode = VLCApplication.getInstance().getLanguage();
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_NEWST_VIDEO, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取动态
     *
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getDynamicVideo(int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        BaseListParam params = new BaseListParam();
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_DYNAMIC_VIDEO, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取推荐关注人
     *
     * @param callback
     */
    public static void getReconmendPerson(HttpUtil.Callback<RecommanPageBean> callback) {
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_PERSION_RECOMMEND, null, RecommanPageBean.class, callback);
    }

    /**
     * 获取资源详情
     *
     * @param fileType   资源类型,"100"为视频，"200"为图片
     * @param resourceId
     * @param userid
     * @param callback
     */
    public static void getResourceDetail(String fileType, int resourceId, int userid, HttpUtil.Callback<VideoDetailPageBean> callback) {
        VideoDetailParam params = new VideoDetailParam();
        params.resourceId = resourceId;
        params.userId = userid;
        String url = Contacts_square.URL_VIDEO_DETAIL;
        if ("200".equals(fileType)) {
            url = Contacts_square.URL_PICTURE_DETAIL;
        }
        HttpUtil.getInstance().requestGetWithoutContenttype(url, params, VideoDetailPageBean.class, callback);
    }

    /**
     * 获取图片详情
     *
     * @param resourceId
     * @param callback
     */
    public static void getPictureDetail(int resourceId, HttpUtil.Callback<VideoDetailPageBean> callback) {
        VideoDetailParam params = new VideoDetailParam();
        params.resourceId = resourceId;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_PICTURE_DETAIL, params, VideoDetailPageBean.class, callback);
    }

    /**
     * 获取更多点赞
     *
     * @param resourceid
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getMoreLike(int resourceid, int page, int pagesize, HttpUtil.Callback<LikePageBean> callback) {
        ResorceInfoParam params = new ResorceInfoParam();
        params.resourceId = resourceid;
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_VIDEO_LIKE, params, LikePageBean.class, callback);
    }

    /**
     * 获取更多评论
     *
     * @param resourceid
     * @param page
     * @param pagesize
     */
    public static void getMoreReview(int resourceid, int page, int pagesize, HttpUtil.Callback<ReviewPageBean> callback) {
        ResorceInfoParam params = new ResorceInfoParam();
        params.resourceId = resourceid;
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_VIDEO_REVIEW, params, ReviewPageBean.class, callback);
    }

    /**
     * 添加评论或回复
     *
     * @param resourceid
     * @param replyUserId
     * @param message
     * @param callback
     */
    public static void addReview(int resourceid, int replyUserId, String message, HttpUtil.Callback<BasePageBean> callback) {
        AddReviewParam param = new AddReviewParam();
        param.message = message;
        if (replyUserId != -1)
            param.replyUserId = replyUserId;
        param.resourceId = resourceid;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_VIDEO_REVIEW, param, BasePageBean.class, callback);
    }

    /**
     * 添加赞
     *
     * @param resourceid
     * @param callback
     */
    public static void addLike(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_VIDEO_LIKE, params, BasePageBean.class, callback);
    }

    /**
     * 取消赞
     *
     * @param resourceid
     * @param callback
     */
    public static void deleteLike(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestDelete(Contacts_square.URL_VIDEO_LIKE, params, BasePageBean.class, callback);
    }

    /**
     * 添加用户关注
     *
     * @param userid
     * @param callback
     */
    public static void addAttention(int userid, HttpUtil.Callback<BasePageBean> callback) {
        RequestUserParam params = new RequestUserParam();
        params.byUserId = userid;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_ATTENTION_USER, params, BasePageBean.class, callback);
    }

    /**
     * 取消关注
     *
     * @param userid
     * @param callback
     */
    public static void deleteAttention(int userid, HttpUtil.Callback<BasePageBean> callback) {
        RequestUserParam params = new RequestUserParam();
        params.byUserId = userid;
        HttpUtil.getInstance().requestDelete(Contacts_square.URL_ATTENTION_USER, params, BasePageBean.class, callback);
    }

    /**
     * 添加收藏
     *
     * @param resourceid
     * @param callback
     */
    public static void addFavorite(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_FAVORITE_VIDEO, params, BasePageBean.class, callback);
    }

    /**
     * 取消收藏
     *
     * @param resourceid
     * @param callback
     */
    public static void deleteFavorite(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestDelete(Contacts_square.URL_FAVORITE_VIDEO, params, BasePageBean.class, callback);
    }

    /**
     * 获取个人信息
     *
     * @param userid
     * @param callback
     */
    public static void getPersonalInfo(int userid, HttpUtil.Callback<PersonalInfoPageBean> callback) {
        PersonalInfoParam param = new PersonalInfoParam();
        param.userId = userid;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_GET_PERSONAL_INFO, param, PersonalInfoPageBean.class, callback);
    }

    /**
     * 获取用户分享的视频
     *
     * @param userid
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getShare(int userid, int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        BaseUserResourceListParam params = new BaseUserResourceListParam();
        params.userId = userid;
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_GET_SHARED, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取用户收藏
     *
     * @param userid
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getCollect(int userid, int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        BaseUserResourceListParam params = new BaseUserResourceListParam();
        params.userId = userid;
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_GET_COLLECT, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取用户举报
     *
     * @param userid
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getReport(int userid, int page, int pagesize, HttpUtil.Callback<TypeVideoListPageBean> callback) {
        BaseUserResourceListParam params = new BaseUserResourceListParam();
        params.userId = userid;
        params.page = page;
        params.per_page = pagesize;
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_GET_REPORT, params, TypeVideoListPageBean.class, callback);
    }

    /**
     * 获取 关注及粉丝列表
     *
     * @param userid
     * @param type     类形，0为关注 ，1为粉丝
     * @param page
     * @param pagesize
     * @param callback
     */
    public static void getAttention(int userid, int type, int page, int pagesize, HttpUtil.Callback<LikePageBean> callback) {
        AttentionParam params = new AttentionParam();
        params.userId = userid;
        params.page = page;
        params.per_page = pagesize;
        params.type = String.valueOf(type);
        HttpUtil.getInstance().requestGetWithoutContenttype(Contacts_square.URL_GET_ATTENTION, params, LikePageBean.class, callback);
    }

    /**
     * 举报视频
     *
     * @param resourceid
     * @param resonid
     * @param callback
     */
    public static void reportResource(int resourceid, int resonid, HttpUtil.Callback<BasePageBean> callback) {
        ReprotParam params = new ReprotParam();
        params.resourceId = resonid;
        params.reportWhyCode = String.valueOf(resonid);
        HttpUtil.getInstance().requestPost(Contacts_square.URL_REPORT_VIDEO, params, BasePageBean.class, callback);
    }

    /**
     * 删除资源
     *
     * @param resourceid
     * @param callback
     */
    public static void deleteResource(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestDelete(Contacts_square.URL_DELETE_FILE, params, BasePageBean.class, callback);
    }

    /**
     * 增加浏览次数
     *
     * @param resourceid
     * @param callback
     */
    public static void addSeeResource(int resourceid, HttpUtil.Callback<BasePageBean> callback) {
        BaseResourceParam params = new BaseResourceParam();
        params.resourceId = resourceid;
        HttpUtil.getInstance().requestPut(Contacts_square.URL_DELETE_FILE, params, BasePageBean.class, callback);
    }

    public static void feedBack(int type, String des, char isReportError, List<File> errors, char isNeedContact, String phoneNum, String email, List<File> image,
                                HttpUtil.Callback<BasePageBean> callback) {
        FeedbackParam param = new FeedbackParam();
        param.type = type;
        param.description = des;
        param.isReportError = isReportError;
        param.isNeedContact = isNeedContact;
        param.log = errors;
        param.phoneNum = phoneNum;
        param.email = email;
        param.image = image;

        param.versionCategory = VLCApplication.getAppContext().getPackageName();

        param.softVersion = SystemUtil.getAppversion(VLCApplication.getAppContext());
        param.deviceid = SpUtils.getString(VLCApplication.getAppContext(), "xuliehao", null);
        param.hudVersion = SpUtils.getString(VLCApplication.getAppContext(), "gujianVersion", null);
        param.cameraVersion = SpUtils.getString(VLCApplication.getAppContext(), "CAMERA_VERSION", "");
        param.obdVersion = VoiceManager.obdversion;
        HttpUtil.getInstance().requestPostWithFile(Contacts_square.URL_FEED_BACK, param, BasePageBean.class, callback);
    }

    /**
     * 违章举报
     *
     * @param illegalReportParam
     * @param callback
     */
    public static void postIllegalReportInfo(IllegalReportParam illegalReportParam, HttpUtil.Callback<BasePageBean> callback) {
        String videoName = illegalReportParam.videoName;
        String[] videoSplit = videoName.split("/");
        if (videoSplit.length >= 2) {
            illegalReportParam.videoName = videoSplit[videoSplit.length - 2] + "/" + videoSplit[videoSplit.length - 1];
        }
        illegalReportParam.languageCode = VLCApplication.getInstance().getLanguage();
        illegalReportParam.packageName = BuildConfig.APPLICATION_ID;
        HttpUtil.getInstance().requestPost(Contacts_square.URL_ILLEGAL_REPORT, illegalReportParam, BasePageBean.class, callback);
    }

    /**
     * 获取身份证信息
     *
     * @param callback
     */
    public static void getIdCardInfo(HttpUtil.Callback<IdCardInfoBean> callback) {
        GetIdCardInfoParam params = new GetIdCardInfoParam();
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_IDCARD, params, IdCardInfoBean.class, callback);
    }

    /**
     * 保存身份证信息到服务器
     *
     * @param postIdCardInfoParam
     * @param callback
     */
    public static void postIdCardInfo(PostIdCardInfoParam postIdCardInfoParam, HttpUtil.Callback<BasePageBean> callback) {
        HttpUtil.getInstance().requestPost(Contacts_square.URL_POST_IDCARD, postIdCardInfoParam, BasePageBean.class, callback);
    }

    /**
     * 获取违章类型列表
     *
     * @param callback
     */
    public static void getIllegalType(HttpUtil.Callback<IllegalTypeBean> callback) {
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_ILLEGAL_TYPE, null, IllegalTypeBean.class, callback);
    }

    /**
     * 获取违章详情
     *
     * @param getIllegalDetailParam
     * @param callback
     */
    public static void getIllegalDetail(GetIllegalDetailParam getIllegalDetailParam, HttpUtil.Callback<VideoDetailPageBean> callback) {
        HttpUtil.getInstance().requestGet(Contacts_square.URL_GET_ILLEGAL_DETAIL, getIllegalDetailParam, VideoDetailPageBean.class, callback);
    }

}
