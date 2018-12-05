package com.adai.gkd.bean;

import java.io.Serializable;

public class UserInfoBean implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public int id;
    /**
     *
     */
    public String username;

    public String email;
    /**
     *
     */
    public String nickname;
    /**
     * 性别
     */
    public String sex;
    /**
     * 签名
     */
    public String signature;
    /**
     * 头像
     */
    public String portrait;
    /**
     * 环信id
     */
    public String im_id;
    /**
     * 环信密码
     */
    public String im_password;
    /**
     * access token
     */
    public String access_token;
    /**
     * 频道
     */
    public int channel;
    /**
     * 是否开放位置，1：开放；0：不开放
     */
    public int is_opened;
}
