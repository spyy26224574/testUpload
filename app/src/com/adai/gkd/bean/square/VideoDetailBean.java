package com.adai.gkd.bean.square;

import java.util.List;

/**
 * Created by admin on 2016/8/10.
 */
public class VideoDetailBean extends TypeVideoBean {
    private static final long serialVersionUID = -137202494441198835L;
    public int id;
    public String videoPicture;
    public String province;
    public String city;
    public String plateNumber;
    public String createDate;
    public List<ReviewBean> reviewList;
    public List<LikeUserBean> likeList;
}
