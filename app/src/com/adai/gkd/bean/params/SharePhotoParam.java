package com.adai.gkd.bean.params;

/**
 * Created by admin on 2016/8/18.
 */
public class SharePhotoParam {
    //    public List<File> file=new ArrayList<File>();
    public String pictureName="";
    /**
     * 类型为图片
     */
    public String fileType = "200";
    /**
     * 描述
     */
    public String des;
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
}
