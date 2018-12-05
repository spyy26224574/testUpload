package com.adai.gkd.bean;

/**
 * @author huangxy
 * @date 2017/12/15 16:52.
 */

public class LogoBean extends BasePageBean {
    private static final long serialVersionUID = -5675173769971720699L;

    /**
     * data : {"logo_image":"http://192.168.1.10:8181/upload/police_icoin.png","update_time":"2017-12-12 10:49:44"}
     */

    public DataBean data;

    public static class DataBean {
        /**
         * logo_image : http://192.168.1.10:8181/upload/police_icoin.png
         * update_time : 2017-12-12 10:49:44
         */
        public String logo_image;
        public String update_time;
    }
}
