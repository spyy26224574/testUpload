package com.adai.gkd.bean.params;

import java.io.Serializable;

/**
 * Created by admin on 2016/8/11.
 * 添加评论参数实体
 */
public class AddReviewParam implements Serializable {
    public int resourceId;
    public String message;

    public int replyUserId;
}
