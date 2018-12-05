package com.adai.gkd.bean.params;

/**
 * Created by admin on 2016/8/17.
 * 分享视频参数
 */
public class ShareVideoParam {
    public String videoName;
    public String pictureName;

//    /**
//     * 视频文件
//     */
//    public File file;
//    /**
//     * 封面文件
//     */
//    public File picture;
    /**
     * 文件类型
     */
    public String fileType="100";
    /**
     * 视频描述
     */
    public String des;
    /**
     * 视频长度
     */
    public int videoTime;
    /**
     * 地址名称
     */
    public String coordinate;
    /**
     *
     */
    public String longitude;
    /**
     *
     */
    public String latitude;
    /**
     * 是否公开
     */
    public String isOpen;
    /**
     * 是否允许评论
     */
    public String isReview;
    /**
     * 设备号，最近一次连接的设备号
     */
    public String deviceInfo;
    /**
     * 应用包名
     */
    public String packageName;

    /**
     * 语言
     */
    public String languageCode;

    /**
     * 视频类型（10:普通,20:360,30:720,40:全景）
     */
    public int videoType;
    /**
     * 镜头ID
     */
    public int fishEyeId;
    /**
     * 文件的宽
     */
    public int width;
    /**
     * 文件的高
     */
    public int height;
}
