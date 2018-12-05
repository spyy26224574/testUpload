/**
 * Added by zhangyanhu C01012,2014-7-1
 */
package com.adai.camera.sunplus.function;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.adai.camera.CameraFactory;
import com.adai.camera.sunplus.SDKAPI.CameraAction;
import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.icatch.wificam.customer.type.ICatchCameraProperty;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Added by zhangyanhu C01012,2014-7-1
 */
public class CameraCaptureThread implements Runnable {
	private static final String TAG = "CameraCaptureThread";
	private Context context;
//	private MediaPlayer stillCaptureStartBeep;
//	private MediaPlayer delayBeep;
//	private MediaPlayer continuousCaptureBeep;
	// private CameraProperties cameraProperties = new CameraProperties();
	// private CameraAction cameraAction = new CameraAction();
	private Handler handler;

	public CameraCaptureThread(Context context, Handler handler) {
		this.context = context;
		this.handler = handler;
//		stillCaptureStartBeep = MediaPlayer.create(context, R.raw.captureshutter);
//		delayBeep = MediaPlayer.create(context, R.raw.delay_beep);
//		continuousCaptureBeep = MediaPlayer.create(context, R.raw.captureburst);
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// photo capture
		long lastTime = 0;
		Log.e(TAG, "run: [Normal] -- CameraCaptureThread: start CameraCaptureThread");
		Log.e("tigertiger", "start CameraCaptureThread.....");
		int delayTime = CameraFactory.getInstance().getSunplusCamera().getCaptureDelay().getCurrentValue();
		int remainBurstNumer = 1;
		if (CameraProperties.getInstance().hasFuction(ICatchCameraProperty.ICH_CAP_BURST_NUMBER)) {
			remainBurstNumer = CameraProperties.getInstance().getCurrentAppBurstNum();
		} else {
			remainBurstNumer = 1;
			// stillCaptureStartBeep.start();
		}
		// JIRA ICOM-1633 Begin:Add by b.jiang C01063 2015-07-17
		if (remainBurstNumer == 1) {
			CaptureAudioTask captureAudioTask = new CaptureAudioTask(remainBurstNumer,0);
			Timer captureAudioTimer = new Timer(true);
			captureAudioTimer.schedule(captureAudioTask, delayTime, 200);
		}else{
			CaptureAudioTask captureAudioTask = new CaptureAudioTask(remainBurstNumer,1);
			Timer captureAudioTimer = new Timer(true);
			captureAudioTimer.schedule(captureAudioTask, delayTime, 200);
		}

		int count = delayTime / 1000;
		int timerDelay = 0;
		if (delayTime >= 5000) {
			Timer delayTimer = new Timer(true);
			DelayTimerTask delayTimerTask = new DelayTimerTask(count / 2,delayTimer);
			delayTimer.schedule(delayTimerTask, 0, 1000);
			timerDelay = delayTime;
		} else {
			timerDelay = 0;
			count = delayTime / 500;
		}
		if (delayTime >= 3000) {
			Timer delayTimer1 = new Timer(true);
			DelayTimerTask delayTimerTask1 = new DelayTimerTask(count/2,delayTimer1);
			delayTimer1.schedule(delayTimerTask1, timerDelay / 2, 500);
			timerDelay = delayTime;
		} else {
			timerDelay = 0;
			count = delayTime / 250;
		}
		Timer delayTimer2 = new Timer(true);
		DelayTimerTask delayTimerTask2 = new DelayTimerTask(count, delayTimer2);
		delayTimer2.schedule(delayTimerTask2, timerDelay - timerDelay / 4, 250);
		// JIRA ICOM-1633 End:Add by b.jiang C01063 2015-07-17
		CameraAction.getInstance().triggerCapturePhoto();
		Log.e(TAG, "run: [Normal] -- CameraCaptureThread: delayTime = " + delayTime + " remainBurstNumer="+ remainBurstNumer);
		Log.e(TAG, "run: [Normal] -- CameraCaptureThread: end CameraCaptureThread");
	}

	private class CaptureAudioTask extends TimerTask {
		private int burstNumber;
		private int type = 0;

		public CaptureAudioTask(int burstNumber,int type) {
			this.burstNumber = burstNumber;
			this.type = type;
		}

		@Override
		public void run() {
			if(type == 0){
				if (burstNumber > 0) {
					Log.e(TAG, "run: [Normal] -- CameraCaptureThread: CaptureAudioTask remainBurstNumer =" + burstNumber);
//					stillCaptureStartBeep.start();
					burstNumber--;
				} else {
					cancel();
				}
			}else{
				if (burstNumber > 0) {
					Log.e(TAG, "run: [Normal] -- CameraCaptureThread: CaptureAudioTask remainBurstNumer =" + burstNumber);
//					continuousCaptureBeep.start();
					burstNumber--;
				} else {
					cancel();
				}
			}
			
		}
	}

	private class DelayTimerTask extends TimerTask {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.TimerTask#run()
		 */
		private int count;
		private Timer timer;

		public DelayTimerTask(int count, Timer timer) {
			this.count = count;
			this.timer = timer;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (count-- > 0) {
//				delayBeep.start();
			} else {
				if (timer != null) {
					timer.cancel();
				}
			}
		}

	}
}
