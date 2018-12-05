package com.adai.gkd.bean.square;

import java.io.Serializable;

/**
 * Created by admin on 2016/8/10.
 */
public class LikeUserBean implements Serializable {
    public int resourceId;
    public int userId;
    /**
     * 头像地址
     */
    public String portrait;
    public String nickname;
    /**
     * 是否关注
     */
    public String isFocusOn;
    /**
     * 签名信息
     */
    public String signature;
}
