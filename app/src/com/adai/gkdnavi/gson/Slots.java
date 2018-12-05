package com.adai.gkdnavi.gson;

public class Slots {
	private String name;   //拨打电话的名字
	private String code;   //拨打电话的号码
	private String distance;		//导航的距离参数
	private Location location;    //查询天气的地址对象
	private Datetime datetime;    //查询天气的时间对象
	private StartLoc startLoc;    //导航的起点对象
	private EndLoc endLoc;		//导航的终点对象

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Datetime getDatetime() {
		return datetime;
	}

	public void setDatetime(Datetime datetime) {
		this.datetime = datetime;
	}

	public StartLoc getStartLoc() {
		return startLoc;
	}

	public void setStartLoc(StartLoc startLoc) {
		this.startLoc = startLoc;
	}

	public EndLoc getEndLoc() {
		return endLoc;
	}

	public void setEndLoc(EndLoc endLoc) {
		this.endLoc = endLoc;
	}

	public String getDistance() {
		return distance;
	}

	public void setDistance(String distance) {
		this.distance = distance;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "Slots [name=" + name + ", code=" + code + ", distance=" + distance + ", location=" + location
				+ ", datetime=" + datetime + ", startLoc=" + startLoc + ", endLoc=" + endLoc + "]";
	}

}
