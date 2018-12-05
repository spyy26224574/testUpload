/**
 * Added by zhangyanhu C01012,2014-8-8
 */
package com.adai.camera.sunplus.data;

import android.annotation.SuppressLint;

import java.util.HashMap;

/**
 * Added by zhangyanhu C01012,2014-8-8
 */
public class FwToApp {
	@SuppressLint("UseSparseArrays")
	private HashMap<Integer, Integer> burstMap = new HashMap<Integer, Integer>();
	private static FwToApp fwToApp;

	public static FwToApp getInstance() {
		if (fwToApp == null) {
			fwToApp = new FwToApp();
		}
		return fwToApp;
	}

	public FwToApp() {
		initBurstMap();
	}

	/**
	 * Added by zhangyanhu C01012,2014-8-8
	 */
	private void initBurstMap() {
		burstMap.put(0, 0);
		burstMap.put(1, 1);
		burstMap.put(2, 3);
		burstMap.put(3, 5);
		burstMap.put(4, 10);
	}

	public int getAppBurstNum(int fwValue) {
		if (fwValue >= 0 && fwValue <= 4) {
			return burstMap.get(fwValue);
		}
		return 0;
	}
}
