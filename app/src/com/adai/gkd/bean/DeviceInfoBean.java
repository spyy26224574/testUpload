package com.adai.gkd.bean;

import java.io.Serializable;
/**
 * 设备信息实体
 * @author admin
 *
 */
public class DeviceInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String deviceid;
	public String address;
	public String type;
	public int status;
	public String factory_name;
	/**
	 * 是否为内测机
	 */
	public int is_beta;

	public String obd_version;

	public String obd_url;
	public String obd_md5;
}
