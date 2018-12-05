package com.adai.gkd.bean.request;

import com.adai.gkd.bean.BasePageBean;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/8/22 20:38
 * @updateAuthor $Author$
 * @updateDate $Date$
 */
public class CameraVersionBean extends BasePageBean {
    private static final long serialVersionUID = 3389456510185813475L;
    public CameraVersionData data;

    public static class CameraVersionData {
        public String cam_url;
        public String cam_version;
        public String cam_md5;
        public String logo_image;
        public String logo_update_time;
        public String factory_name;
        public int if_ota;
        public int if_support_report;
        public String machine_model;

        @Override
        public String toString() {
            return "CameraVersionData{" +
                    "cam_url='" + cam_url + '\'' +
                    ", cam_version='" + cam_version + '\'' +
                    ", cam_md5='" + cam_md5 + '\'' +
                    ", logo_image='" + logo_image + '\'' +
                    ", logo_update_time='" + logo_update_time + '\'' +
                    ", factory_name='" + factory_name + '\'' +
                    ", if_ota=" + if_ota +
                    ", if_support_report=" + if_support_report +
                    ", machine_model='" + machine_model + '\'' +
                    '}';
        }
    }

}
