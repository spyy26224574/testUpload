package com.adai.gkd.bean.square;

import java.io.Serializable;

/**
 * Created by admin on 2016/8/19.
 */
public class PersonalInfoBean implements Serializable {
    public int userId;
    public String nickname;
    public String portrait;
    public int focusCount;
    public int fansCount;
    /**
     * 签名
     */
    public String signature;
    /**
     * 收藏个数
     */
    public int collectCount;
    /**
     * 是否关注
     */
    public String isFocus;
}
