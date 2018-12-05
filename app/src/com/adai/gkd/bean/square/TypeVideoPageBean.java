package com.adai.gkd.bean.square;

import com.adai.gkd.bean.PageInfoBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by admin on 2016/8/9.
 */
public class TypeVideoPageBean implements Serializable {
    private static final long serialVersionUID = 8948514186279383633L;
    public List<TypeVideoBean> items;
    public PageInfoBean _meta;
}
