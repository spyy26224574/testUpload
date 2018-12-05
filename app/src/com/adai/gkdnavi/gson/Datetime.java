package com.adai.gkdnavi.gson;

public class Datetime {
	private String date; //日期  格式为YYYY-MM-DD,缺省值为”CURRENT_DAY”
	private String type; //时间类型：DT_BASIC
	private String dateOrig; //date 的原始字串

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDateOrig() {
		return dateOrig;
	}

	public void setDateOrig(String dateOrig) {
		this.dateOrig = dateOrig;
	}

	@Override
	public String toString() {
		return "Datetime [date=" + date + ", type=" + type + ", dateOrig="
				+ dateOrig + "]";
	}

}
