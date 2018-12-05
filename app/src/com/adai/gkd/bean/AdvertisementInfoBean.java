package com.adai.gkd.bean;

import java.io.Serializable;
/**
 * 广告信息实体
 * @author admin
 *
 */
public class AdvertisementInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int id;
	
	public String title;
	/**
	 * 图片
	 */
	public String cover;
	/**
	 * 跳转链接
	 */
	public String link;
}
