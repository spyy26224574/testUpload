package com.adai.gkd.bean;

public class LicenseBean extends BasePageBean {
    private static final long serialVersionUID = 1L;

    public DataBean data;

    public static class DataBean {
        /**
         * statusCode 状态码(‘101’:正常认证,’102’:没有认证)
         */
        public String statusCode;
    }
}
