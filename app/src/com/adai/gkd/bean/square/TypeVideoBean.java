package com.adai.gkd.bean.square;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2016/8/9.
 */
public class TypeVideoBean extends VideoGridBean implements Serializable {
    private static final long serialVersionUID = -2279303886885909085L;
    //    public int resourceId;
//    public int userId;
//    public String fileType;
//    public String coverPicture;
//    public String des;
//    public int browseCount;
//    public String uploadDate;
    public String pictureUrl;

    public String videoUrl;
    public int videoTime;
    public int videoType;
    public int fishEyeId;
    public int width;
    public int height;
    /**
     * 地址名称
     */
    public String coordinate;
    public String longitude;
    public String latitude;
    public String isOpen;
    public String isReview;
    public String nickname;
    public String portrait;
    public int replyCount;
    public String shareAddress;
    public ArrayList<String> pictureList;
    public List<String> thumbnailList;
    public int likeCount;

    /**
     * 是否点赞
     */
    public String isLike;
    /**
     * 是否关注
     */
    public String isFocus;
    /**
     * 是否收藏
     */
    public String isCollect;
    /**
     * 是否可删除，Y为可删除，N为不能删除
     */
    public String isDelete;
    public String illegalAddress = "";
    public String illegalType = "";
    public String reportPerson = "";
    public String illegalDate;

}
