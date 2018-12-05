package com.adai.gkdnavi.gson;

public class Result {
	private String airQuality; // 空气质量
	private String sourceName; // 天气数据来源
	private String date;// 日期
	private String lastUpdateTime;// 数据最后更新时间
	private String city;// 城市
	private String wind;// 风向
	private String weather;// 天气
	private String tempRange;// 温度区间

	private String province;// 省份
	private int dateLong;//
	private int windLevel;// 风力大小

	private String stock_name;// 股票名称
	private String current_prices;// 当前价格
	private String change_amount;// 当前涨跌
	private String change_rate;// 当前涨跌比例
	private boolean isRise; // 涨跌标志

	public String getStock_name() {
		return stock_name;
	}

	public void setStock_name(String stock_name) {
		this.stock_name = stock_name;
	}

	public String getCurrent_prices() {
		return current_prices;
	}

	public void setCurrent_prices(String current_prices) {
		this.current_prices = current_prices;
	}

	public boolean isRise() {
		return isRise;
	}

	public void setRise(boolean isRise) {
		this.isRise = isRise;
	}

	public String getChange_amount() {
		return change_amount;
	}

	public void setChange_amount(String change_amount) {
		this.change_amount = change_amount;
	}

	public String getChange_rate() {
		return change_rate;
	}

	public void setChange_rate(String change_rate) {
		this.change_rate = change_rate;
	}

	public String getAirQuality() {
		return airQuality;
	}
	public void setAirQuality(String airQuality) {
		this.airQuality = airQuality;
	}
	public String getSourceName() {
		return sourceName;
	}
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getLastUpdateTime() {
		return lastUpdateTime;
	}
	public void setLastUpdateTime(String lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getWind() {
		return wind;
	}
	public void setWind(String wind) {
		this.wind = wind;
	}
	public String getWeather() {
		return weather;
	}
	public void setWeather(String weather) {
		this.weather = weather;
	}
	public String getTempRange() {
		return tempRange;
	}
	public void setTempRange(String tempRange) {
		this.tempRange = tempRange;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public int getDateLong() {
		return dateLong;
	}
	public void setDateLong(int dateLong) {
		this.dateLong = dateLong;
	}
	public int getWindLevel() {
		return windLevel;
	}
	public void setWindLevel(int windLevel) {
		this.windLevel = windLevel;
	}
	@Override
	public String toString() {
		return "Result [airQuality=" + airQuality + ", sourceName=" + sourceName + ", date=" + date
				+ ", lastUpdateTime=" + lastUpdateTime + ", city=" + city + ", wind=" + wind + ", weather=" + weather
				+ ", tempRange=" + tempRange + ", province=" + province + ", dateLong=" + dateLong + ", windLevel="
				+ windLevel + "]";
	}


}
