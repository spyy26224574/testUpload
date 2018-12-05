package com.adai.gkd.bean.request;

import java.util.List;

import com.adai.gkd.bean.AdvertisementInfoBean;
import com.adai.gkd.bean.BasePageBean;
/**
 * 广告请求结果数据实体
 * @author admin
 *
 */
public class AdvertisementPagebean extends BasePageBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 广告数据
	 */
	public List<AdvertisementInfoBean> data;
}
