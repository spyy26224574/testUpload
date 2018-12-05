package com.adai.gkd.bean.params;

import java.io.Serializable;

/**
 * Created by huangxy on 2017/1/17.
 */

public class IllegalReportParam implements Serializable {
    private static final long serialVersionUID = -3537602952981950653L;
    /**
     * 文件类型（‘100’:代表视频,’200’:代表图片, ’300’:代表违章）
     */
    public String fileType = "300";
    /**
     * 违章描述
     */
    public String des;
    /**
     * 视频时长
     */
    public int videoTime;
    /**
     * 省份
     */
    public String province;
    /**
     * 市
     */
    public String city;
    /**
     * 违章车牌
     */
    public String plate_number;
    /**
     * 举报人
     */
    public String report_person;
    /**
     * 违章类型
     */
    public String illegal_type;
    /**
     * 违章地点
     */
    public String illegal_address;
    /**
     * 经度
     */
    public String longitude;
    /**
     * 纬度
     */
    public String latitude;
    /**
     * 违章日期
     */
    public String illegal_date;
    /**
     * 车辆类型
     */
//    public String car_type;
    public String languageCode;
    public String packageName;
    public String videoName="";
    public String pictureName="";
}
