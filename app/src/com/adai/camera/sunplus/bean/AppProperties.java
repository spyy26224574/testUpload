/**
 * Added by zhangyanhu C01012,2014-8-28
 */
package com.adai.camera.sunplus.bean;

/**
 * Added by zhangyanhu C01012,2014-8-28
 */
public class AppProperties {
	private int timeLapseMode = -1;
	public static AppProperties appProperties;

	public static AppProperties getInstanse() {
		if (appProperties == null) {
			appProperties = new AppProperties();
		}
		return appProperties;
	}

	public int getTimeLapseMode() {
		return timeLapseMode;
	}

	public void setTimeLapseMode(int timeLapseMode) {
		this.timeLapseMode = timeLapseMode;
	}
}
