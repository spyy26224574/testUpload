package com.adai.gkdnavi.gson;

public class EndLoc {
	private String type; 
	//基础地点  LOC_BASIC  道路 LOC_STREET  交叉路口 LOC_CROSS 区域 LOC_REGION 位置点LOC_POI
	private String country;//国家简称
	private String province;//省全称
	private String provinceAddr;//省简称
	private String city;//市全称
	private String cityAddr;//市简称
	private String area;//县区
	private String areaAddr;//县区简称
	
	private String street;//道路名称
	private String streets;//交口的另一道路名称
	private String region;//区域名称
	private String poi;//机构等名称,CURRENT_POI 表示当前地点
	private String keyword;// 地点其他关键词

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getProvinceAddr() {
		return provinceAddr;
	}

	public void setProvinceAddr(String provinceAddr) {
		this.provinceAddr = provinceAddr;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCityAddr() {
		return cityAddr;
	}

	public void setCityAddr(String cityAddr) {
		this.cityAddr = cityAddr;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getAreaAddr() {
		return areaAddr;
	}

	public void setAreaAddr(String areaAddr) {
		this.areaAddr = areaAddr;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getStreets() {
		return streets;
	}

	public void setStreets(String streets) {
		this.streets = streets;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getPoi() {
		return poi;
	}

	public void setPoi(String poi) {
		this.poi = poi;
	}
	

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Override
	public String toString() {
		return "EndLoc [type=" + type + ", country=" + country + ", province=" + province + ", provinceAddr="
				+ provinceAddr + ", city=" + city + ", cityAddr=" + cityAddr + ", area=" + area + ", areaAddr="
				+ areaAddr + ", street=" + street + ", streets=" + streets + ", region=" + region + ", poi=" + poi
				+ ", keyword=" + keyword + "]";
	}
	
}
