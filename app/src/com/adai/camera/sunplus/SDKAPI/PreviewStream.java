/**
 * Added by zhangyanhu C01012,2014-6-27
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.sunplus.bean.Tristate;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchStreamNotSupportException;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.exception.IchVideoStreamClosedException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchCustomerStreamParam;
import com.icatch.wificam.customer.type.ICatchFileStreamParam;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;
import com.icatch.wificam.customer.type.ICatchH264StreamParam;
import com.icatch.wificam.customer.type.ICatchMJPGStreamParam;
import com.icatch.wificam.customer.type.ICatchPreviewMode;


public class PreviewStream {
	private static final String TAG = "";
	private static PreviewStream instance;

	// private ICatchWificamPreview previewStreamControl;
	//
	public static PreviewStream getInstance() {
		if (instance == null) {
			instance = new PreviewStream();
		}
		return instance;
	}

	//
	// private PreviewStream(){
	//
	// }
	//
	// public void initPreviewStream(){
	// previewStreamControl =
	// GlobalInfo.getInstance().getCurrentCamera().getpreviewStreamClient();
	// }

	public boolean stopMediaStream(ICatchWificamPreview previewStreamControl) {
		Log.e(TAG, "stopMediaStream: [Normal] -- PreviewStream: begin stopMediaStream");
		boolean retValue = false;
		try {
			retValue = previewStreamControl.stop();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		}
		try {
			Thread.currentThread().sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- PreviewStreamend stopMediaStream =" + retValue);
		return retValue;
	}

	public boolean supportAudio(ICatchWificamPreview previewStreamControl) {
		Log.e(TAG, "[Normal] -- PreviewStream: begin supportAudio");
		boolean retValue = false;
		try {
			retValue = previewStreamControl.containsAudioStream();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotRunningException");
			
			e.printStackTrace();
		}
		// JIRA ICOM-1844 Start delete by b.jiang 2015-08-14
		// Log.e(TAG, "[Normal] -- PreviewStream: ",
		// "end containsAudioStream retValue =" + retValue);
		// //retValue = false;
		// retValue = retValue & (!GlobalInfo.forbidAudioOutput);
		// JIRA ICOM-1844 End delete by b.jiang 2015-08-14
		Log.e(TAG, "[Normal] -- PreviewStream: end supportAudio retValue =" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public boolean getNextVideoFrame(ICatchFrameBuffer buffer, ICatchWificamPreview previewStreamControl) {
		// Log.e(TAG, "[Normal] -- PreviewStream: ",
		// "begin getNextVideoFrame");
		boolean retValue = false;
		// Log.d("tigertiger","previewStream = "+previewStream);
		try {
			retValue = previewStreamControl.getNextVideoFrame(buffer);
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			// need to close preview get next video frame
			
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchBufferTooSmallException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchTryAgainException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchTryAgainException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotRunningException");
			
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");
			
			e.printStackTrace();
		} catch (IchVideoStreamClosedException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchVideoStreamClosedException");
			
			e.printStackTrace();
		}

		// Log.e(TAG, "[Normal] -- PreviewStream: ",
		// "end getNextVideoFrame retValue =" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public boolean getNextAudioFrame(ICatchWificamPreview previewStreamControl, ICatchFrameBuffer icatchBuffer) {
		// Log.e(TAG, "[Normal] -- PreviewStream: ",
		// "begin getNextAudioFrame");
		boolean retValue = false;
		try {
			retValue = previewStreamControl.getNextAudioFrame(icatchBuffer);
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchBufferTooSmallException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchTryAgainException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchTryAgainException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotRunningException");
			
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");
			
			e.printStackTrace();
		} catch (IchAudioStreamClosedException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchAudioStreamClosedException");
			
			e.printStackTrace();
		}
		// Log.e(TAG, "[Normal] -- PreviewStream: ",
		// "end getNextAudioFrame retValue =" + retValue);
		return retValue;
	}

	public Tristate startMediaStream(ICatchWificamPreview previewStreamControl, ICatchCustomerStreamParam param, ICatchPreviewMode previewMode) {

		Log.e(TAG, "[Normal] -- PreviewStream: begin startMediaStream");
		boolean temp = false;
		// JIRA ICOM-1705 Start add by b.jiang 2015-08-06
		Tristate retValue = Tristate.FALSE;
		try {
			temp = previewStreamControl.start(param, previewMode, GlobalInfo.forbidAudioOutput);
			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			if(temp){
				retValue = Tristate.NORMAL;
			}else{
				retValue = Tristate.FALSE;
			}
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");

			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");

			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");

			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");

			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");

			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			retValue = Tristate.ABNORMAL;
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13
			e.printStackTrace();
		}
		// JIRA ICOM-1705 End add by b.jiang 2015-08-06
		Log.e(TAG, "[Normal] -- PreviewStream: end startMediaStream retValue =" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public Tristate startMediaStream(ICatchWificamPreview previewStreamControl, ICatchMJPGStreamParam param, ICatchPreviewMode previewMode) {

		Log.e(TAG, "[Normal] -- PreviewStream: begin startMediaStream");
		boolean temp = false;
		Tristate retValue = Tristate.FALSE;
		try {
			temp = previewStreamControl.start(param, previewMode, GlobalInfo.forbidAudioOutput);
			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			if(temp){
				retValue = Tristate.NORMAL;
			}else{
				retValue = Tristate.FALSE;
			}
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");

			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");

			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");

			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");

			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			retValue = Tristate.ABNORMAL;
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13

			e.printStackTrace();
		}
		// JIRA ICOM-1705 End add by b.jiang 2015-08-06
		Log.e(TAG, "[Normal] -- PreviewStream: param = " + param);
		Log.e(TAG, "[Normal] -- PreviewStream: previewMode = " + previewMode);
		Log.e(TAG, "[Normal] -- PreviewStream: end startMediaStream retValue =" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public Tristate startMediaStream(ICatchWificamPreview previewStreamControl, ICatchH264StreamParam param, ICatchPreviewMode previewMode) {
		
		Log.e(TAG, "[Normal] -- PreviewStream: begin startMediaStream");
		boolean temp = false;
		// JIRA ICOM-1705 Start add by b.jiang 2015-08-06
		Tristate retValue = Tristate.FALSE;
		try {
			temp = previewStreamControl.start(param, previewMode, GlobalInfo.forbidAudioOutput);
			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			if(temp){
				retValue = Tristate.NORMAL;
			}else{
				retValue = Tristate.FALSE;
			}
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");
			
			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			
			//JIRA ICOM-1705 Start add by b.jiang 2015-08-13
			retValue = Tristate.ABNORMAL;
			//JIRA ICOM-1705 End add by b.jiang 2015-08-13
			e.printStackTrace();
		}
		// JIRA ICOM-1705 End add by b.jiang 2015-08-06
		Log.e(TAG, "[Normal] -- PreviewStream: end startMediaStream retValue =" + retValue);
		return retValue;
	}

	public boolean changePreviewMode(ICatchWificamPreview previewStreamControl, ICatchPreviewMode previewMode) {
		Log.e(TAG, "[Normal] -- PreviewStream: begin changePreviewMode");
		boolean retValue = false;
		try {
			retValue = previewStreamControl.changePreviewMode(previewMode);
		} catch (IchSocketException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- PreviewStream: end changePreviewMode");
		return retValue;

	}

	public int getVideoWidth(ICatchWificamPreview previewStreamControl) {
		
		Log.e(TAG, "[Normal] -- CameraProperties: begin getVideoWidth");
		int retValue = 0;

		try {
			retValue = previewStreamControl.getVideoFormat().getVideoW();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchStreamNotRunningException");
			
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraProperties: end getVideoWidth retValue =" + retValue);
		return retValue;
	}

	public int getVideoHeigth(ICatchWificamPreview previewStreamControl) {
		
		Log.e(TAG, "[Normal] -- CameraProperties: begin getVideoHeigth");
		int retValue = 0;

		try {
			retValue = previewStreamControl.getVideoFormat().getVideoH();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG, "[Error] -- CameraProperties: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraProperties: end getVideoHeigth retValue =" + retValue);
		return retValue;
	}

	public int getCodec(ICatchWificamPreview previewStreamControl) {
		
		Log.e(TAG, "[Normal] -- CameraProperties: begin getCodec previewStreamControl =" + previewStreamControl);
		int retValue = 0;

		try {
			retValue = previewStreamControl.getVideoFormat().getCodec();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG, "[Error] -- CameraProperties: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraProperties: end getCodec retValue =" + retValue);
		return retValue;
	}

	public int getBitrate(ICatchWificamPreview previewStreamControl) {
		
		Log.e(TAG, "[Normal] -- CameraProperties: begin getBitrate");
		int retValue = 0;

		try {
			retValue = previewStreamControl.getVideoFormat().getBitrate();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG, "[Error] -- CameraProperties: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraProperties: end getBitrate retValue =" + retValue);
		return retValue;
	}

	public ICatchAudioFormat getAudioFormat(ICatchWificamPreview previewStreamControl) {
		
		Log.e(TAG, "[Normal] -- CameraProperties: begin getAudioFormat");
		ICatchAudioFormat retValue = null;

		try {
			retValue = previewStreamControl.getAudioFormat();
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG, "[Error] -- CameraProperties: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- CameraProperties: end getAudioFormat retValue =" + retValue);
		return retValue;
	}

	public boolean startMediaStream(ICatchWificamPreview previewStreamControl, ICatchFileStreamParam param, ICatchPreviewMode previewMode) {
		
		Log.e(TAG, "[Normal] -- PreviewStream: begin startMediaStream previewStreamControl="+ previewStreamControl);
		boolean temp = false;
		try {
			temp = previewStreamControl.start(param, previewMode);
		} catch (IchSocketException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidArgumentException");
			
			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			
			e.printStackTrace();
		}
		// JIRA ICOM-1705 End add by b.jiang 2015-08-06
		Log.e(TAG, "[Normal] -- PreviewStream: param = " + param);
		Log.e(TAG, "[Normal] -- PreviewStream: end startMediaStream temp =" + temp);
		return temp;
	}


	//start add by b.jiang 20160108
	public boolean enableAudio(ICatchWificamPreview previewStreamControl){
		Log.e(TAG, "[Normal] -- PreviewStream: start enableAudio");
		boolean value = false;
		try {
			value = previewStreamControl.enableAudio();
		} catch (IchSocketException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- PreviewStream: end enableAudio value = " + value);
		return value;
	}
	//end by b.jiang 20160108

	//start add by b.jiang 20160108
	public boolean disableAudio(ICatchWificamPreview previewStreamControl){
		Log.e(TAG, "[Normal] -- PreviewStream: start disableAudio");
		boolean value = false;
		try {
			value = previewStreamControl.disableAudio();
		} catch (IchSocketException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchInvalidSessionException");
			e.printStackTrace();
		} catch (IchStreamNotSupportException e) {
			
			Log.e(TAG, "[Error] -- PreviewStream: IchStreamNotSupportException");
			e.printStackTrace();
		}
		Log.e(TAG, "[Normal] -- PreviewStream: end disableAudio value = " + value);
		return value;
	}
	//end by b.jiang 20160108

}
