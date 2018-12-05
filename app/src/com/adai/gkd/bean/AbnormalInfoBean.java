package com.adai.gkd.bean;

import java.io.Serializable;
/**
 * 故障信息实体
 * @author admin
 *
 */
public class AbnormalInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String deviceid;
	/**
	 * 故障ID,1胎压异常,2冷却液异常
	 */
	public int abnormal_id;
	/**
	 * 故障详情
	 */
	public String detail;
	/**
	 * 时间，格式:"2016-01-01 00:00:00"
	 */
	public String happen_time;
}
