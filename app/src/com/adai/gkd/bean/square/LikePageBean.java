package com.adai.gkd.bean.square;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.PageInfoBean;

import java.util.List;

/**
 * Created by admin on 2016/8/11.
 */
public class LikePageBean extends BasePageBean {
    public LikeListBean data;

    public class LikeListBean{
        public List<LikeUserBean>items;
        public PageInfoBean _meta;
    }
}
