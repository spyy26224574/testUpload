package com.adai.gkd.bean.square;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.PageInfoBean;

import java.util.List;

/**
 * Created by admin on 2016/8/11.
 */
public class ReviewPageBean extends BasePageBean {
    public ReviewListBean data;

    public class ReviewListBean{
        public List<ReviewBean> items;
        public PageInfoBean _meta;
    }
}


