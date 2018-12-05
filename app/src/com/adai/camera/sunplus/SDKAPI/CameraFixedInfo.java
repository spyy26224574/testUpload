/**
 * Added by zhangyanhu C01012,2014-6-27
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.CameraFactory;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;

/**
 * Added by zhangyanhu C01012,2014-6-27
 */
public class CameraFixedInfo {
	private static final String TAG = "CameraFixedInfo";
	private static CameraFixedInfo instance;
	private ICatchWificamInfo cameraFixedInfo;

	public static CameraFixedInfo getInstance() {
		if (instance == null) {
			instance = new CameraFixedInfo();
		}
		return instance;
	}

	private CameraFixedInfo() {

	}

	public void initCameraFixedInfo() {
		cameraFixedInfo = CameraFactory.getInstance().getSunplusCamera().getCameraInfoClint();
	}

	public String getCameraName() {
		Log.e(TAG, "getCameraName: [Normal] -- CameraFixedInfo: begin getCameraName");
		String name = "";
		try {
			name = cameraFixedInfo.getCameraProductName();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- CameraFixedInfo: IchInvalidSessionException");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- CameraFixedInfo: end getCameraName name =" + name);
		return name;
	}

	public String getCameraVersion() {
		Log.e(TAG,"[Normal] -- CameraFixedInfo: begin getCameraVersion");
		String version = "";
		try {
			version = cameraFixedInfo.getCameraFWVersion();
		} catch (IchInvalidSessionException e) {
			// TODO Auto-generated catch block
			Log.e(TAG,"[Error] -- CameraFixedInfo: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- CameraFixedInfo: end getCameraVersion version =" + version);
		return version;
	}

}
