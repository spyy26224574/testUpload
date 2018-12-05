/**
 * Added by zhangyanhu C01012,2014-7-2
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.CameraFactory;
import com.icatch.wificam.customer.ICatchWificamState;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;

/**
 * Added by zhangyanhu C01012,2014-7-2
 */
public class CameraState {
	private static final String TAG = "CameraState";
	private static CameraState instance;
	private ICatchWificamState cameraState;

	public static CameraState getInstance() {
		if (instance == null) {
			instance = new CameraState();
		}
		return instance;
	}

	private CameraState() {

	}

	public void initCameraState() {
		cameraState = CameraFactory.getInstance().getSunplusCamera().getCameraStateClint();
	}

	public boolean isMovieRecording() {
		Log.e(TAG, "isMovieRecording: [Normal] -- CameraState: begin isMovieRecording");
		boolean retValue = false;
		try {
			retValue = cameraState.isMovieRecording();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- CameraState: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraState: end isMovieRecording retValue=" + retValue);
		return retValue;
	}

	public boolean isTimeLapseVideoOn() {
		Log.e(TAG, "[Normal] -- CameraState: begin isTimeLapseVideoOn");
		boolean retValue = false;
		try {
			retValue = cameraState.isTimeLapseVideoOn();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- CameraState: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraState: end isTimeLapseVideoOn retValue=" + retValue);
		return retValue;
	}

	public boolean isTimeLapseStillOn() {
		Log.e(TAG, "[Normal] -- CameraState: begin isTimeLapseStillOn");
		boolean retValue = false;
		try {
			retValue = cameraState.isTimeLapseStillOn();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- CameraState: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraState: end isTimeLapseStillOn retValue=" + retValue);
		return retValue;
	}

	public boolean isSupportImageAutoDownload() {
		Log.e(TAG, "[Normal] -- CameraState: begin isSupportImageAutoDownload");
		boolean retValue = false;
		try {
			retValue = cameraState.supportImageAutoDownload();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraState: IchInvalidSessionException");
			
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraState: end isSupportImageAutoDownload = " + retValue);
		return retValue;

	}

	public boolean isStreaming() {
		Log.e(TAG, "[Normal] -- CameraState: begin isStreaming");
		boolean retValue = false;
		try {
			retValue = cameraState.isStreaming();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- CameraState: IchInvalidSessionException");
			e.printStackTrace();
		}

		Log.e(TAG, "[Normal] -- CameraState: end isStreaming retValue=" + retValue);
		return retValue;
	}
}
