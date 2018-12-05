package com.adai.gkd.bean.params;

import java.io.Serializable;
/**
 * 请求广告参数
 * @author admin
 *
 */
public class AdvertisementParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 广告类型，1:引导页广告，2：导航栏广告
	 */
	public int type;
	/**
	 * 广告序号
	 */
	public int arrangement;
	/**
	 * 软件类型，01为hud,02为cam
	 */
	public String version_category="01";
}
