package com.adai.gkd.bean.request;

import com.adai.gkd.bean.BasePageBean;

import java.io.Serializable;

/**
 * Created by huangxy on 2017/1/17.
 */

public class IdCardInfoBean extends BasePageBean {

    private static final long serialVersionUID = 930575780962953933L;
    public IdCardInfoData data;

    public static class IdCardInfoData implements Serializable{
        private static final long serialVersionUID = -7533494361066874322L;
        public String the_name;
        public String identity_card;
        public String bank_card_number;
        public String bank_card_type;
    }
}
