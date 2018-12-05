package com.adai.gkd.bean;

import java.io.Serializable;
/**
 * 行车实时数据实体
 * @author admin
 *
 */
public class SingleInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7190181058088422344L;

	public String deviceid;
	/**
	 * 电瓶电压
	 */
	public float battery_voltage;
	/**
	 * 发动机转速
	 */
	public float engine_speed;
	/**
	 * 车速
	 */
	public float speed;
	/**
	 * 节气门开度
	 */
	public float tap; 
	/**
	 * 冷却液温度
	 */
	public float thw;
	/**
	 * 瞬时油耗
	 */
	public float dynamical_fuel;
	/**
	 * 油耗
	 */
	public float fuel;
	/**
	 * 累积油耗
	 */
	public float total_fuel;
	/**
	 * 故障次数
	 */
	public int error_count;
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
