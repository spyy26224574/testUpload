package com.adai.gkd.bean;

import java.io.Serializable;
/**
 * 驾驶习惯数据实体
 * @author admin
 *
 */
public class CustomInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 设备id
	 */
	public String deviceid;
	/**
	 * 点火次数
	 */
	public int ignite_count;
	/**
	 * 总行驶时间
	 */
	public float total_travel_time; 
	/**
	 * 平均热车时间
	 */
	public float average_hot_time;
	/**
	 * 平均行驶车速
	 */
	public float average_speed;
	/**
	 * 最高转速
	 */
	public float max_rotate_speed;
	/**
	 * 急加速次数
	 */
	public int accelerate_count;
	/**
	 * 急减速次数
	 */
	public int scram_count;
	/**
	 * 时间，格式:"2016-01-01 00:00:00"
	 */
	public String happen_time;
}
