package com.adai.gkd.bean.params;

import java.io.Serializable;

/**
 * Created by admin on 2016/9/6.
 */
public class PhoneRegisterParam implements Serializable {
    private static final long serialVersionUID = -8033591296255913524L;
    /**
     * 电话号码
     */
    public String phone;
    /**
     * 密码
     */
    public String password;
    /**
     * 邮箱地址
     */
    public String email;
    public String languageCode;
    public String areaCode;
    public String packageName = "";
//    /**
//     * 验证码
//     */
//    public String code;
}
