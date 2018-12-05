package com.adai.gkd.bean.params;

import java.io.Serializable;
/**
 * 请求广告记录参数
 * @author admin
 *
 */
public class AdvertisementRecordParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 广告ID
	 */
	public int advertisement_id;
	/**
	 * 用户ID
	 */
	public int user_id ;
	/**
	 * 设备ID
	 */
	public String deviceid;
	public String version_category;
}
