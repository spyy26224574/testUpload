/**
 * Added by zhangyanhu C01012,2014-8-20
 */
package com.adai.camera.sunplus.function;

import android.os.Handler;

import com.adai.camera.sunplus.SDKAPI.CameraAction;
import com.adai.camera.sunplus.data.GlobalInfo;


/**
 * Added by zhangyanhu C01012,2014-8-20
 */
public class FormatSDCard extends Thread {
	private Handler handler;
	public FormatSDCard(Handler handler){
		this.handler = handler;
	}
	@Override
	public void run() {
		handler.obtainMessage(GlobalInfo.MESSAGE_FORMAT_SD_START).sendToTarget();
		if(CameraAction.getInstance().formatStorage()){
			handler.obtainMessage(GlobalInfo.MESSAGE_FORMAT_SUCCESS).sendToTarget();
		}else{
			handler.obtainMessage(GlobalInfo.MESSAGE_FORMAT_FAILED).sendToTarget();
		}
	}

}
