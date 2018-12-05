package com.adai.gkd.bean.square;

import java.io.Serializable;

/**
 * Created by admin on 2016/8/10.
 * 用户评论实体
 */
public class ReviewBean implements Serializable {
    public int resourceId;
    public int userId;
    public String  nickname;
    public String portrait;
    public int level;
    public String reviewTime;
    public String message;
    public String replyUserName;

}
