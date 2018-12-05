package com.adai.gkd.bean.request;

import com.adai.gkd.bean.BasePageBean;

/**
 * @author huangxy
 * @date 2018/7/20 16:30.
 */
public class CheckSessionBean extends BasePageBean {
    public Data data;

    public class Data {
        public int isValid;
    }
}
