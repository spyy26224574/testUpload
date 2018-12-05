package com.adai.gkd.bean;

import java.io.Serializable;

public class SummaryInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String deviceid;
	/**
	 * 热车时长
	 */
	public float hot_time;
	/**
	 * 怠速时长
	 */
	public float idling_time;
	/**
	 * 行驶时长
	 */
	public float travel_time;
	/**
	 * 怠速油耗
	 */
	public float idling_fuel;
	/**
	 * 最高转速
	 */
	public float max_rotate_speed;
	/**
	 * 最高行驶速度
	 */
	public float max_speed;
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
