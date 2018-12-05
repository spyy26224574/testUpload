package com.adai.gkd.bean;

public class UpdateCorrectBean extends BasePageBean {
    private static final long serialVersionUID = 1L;

    public DataBean data;

    public static class DataBean {

        public int is_upgrade;   //是否升级  0：无需升级 1：需要升级
        public String soft_version;  //软件版本
        public String file_url;    //文件地址
        public String file_md5;    //文件md5


    }
}
