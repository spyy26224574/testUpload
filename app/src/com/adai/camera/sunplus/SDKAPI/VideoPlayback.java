/**
 * Added by zhangyanhu C01012,2014-6-27
 */
package com.adai.camera.sunplus.SDKAPI;

import com.adai.camera.CameraFactory;
import com.baidu.android.common.logging.Log;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.exception.IchAudioStreamClosedException;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchNoSuchFileException;
import com.icatch.wificam.customer.exception.IchPauseFailedException;
import com.icatch.wificam.customer.exception.IchPbStreamPausedException;
import com.icatch.wificam.customer.exception.IchResumeFailedException;
import com.icatch.wificam.customer.exception.IchSeekFailedException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStreamNotRunningException;
import com.icatch.wificam.customer.exception.IchTryAgainException;
import com.icatch.wificam.customer.exception.IchVideoStreamClosedException;
import com.icatch.wificam.customer.type.ICatchAudioFormat;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;
import com.icatch.wificam.customer.type.ICatchVideoFormat;

public class VideoPlayback {
	private static final String TAG = "VideoPlayback";
	private static VideoPlayback instance;
	private ICatchWificamVideoPlayback videoPlayback;

	public static VideoPlayback getInstance() {
		if (instance == null) {
			instance = new VideoPlayback();
		}
		return instance;
	}

	private VideoPlayback() {

	}

	public void initVideoPlayback() {
		videoPlayback = CameraFactory.getInstance().getSunplusCamera().getVideoPlaybackClint();
	}
	//用于本地video播放时对vidoePlayback的初始化;
	public void initVideoPlayback(ICatchWificamVideoPlayback videoPlayback) {
		this.videoPlayback = videoPlayback;
	}

	public boolean stopPlaybackStream() {
		Log.e(TAG,"[Normal] -- VideoPlayback: start stopPlaybackStream ");
		if (videoPlayback == null) {
			return true;
		}
		boolean retValue = false;
		try {
			retValue = videoPlayback.stop();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: stopPlaybackStream =" + retValue);
		return retValue;
	}

	public boolean startPlaybackStream(ICatchFile file) {
		boolean retValue = false;
		Log.e(TAG,"[Normal] -- VideoPlayback: begin startPlaybackStream");
		try {
			retValue = videoPlayback.play(file);
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			e.printStackTrace();
		} catch (IchNoSuchFileException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchNoSuchFileException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: -----------end startPlaybackStream retValue =" + retValue);
		return retValue;
	}
	
	public boolean startPlaybackStream(String fileName) {
		boolean retValue = false;
		ICatchFile icathfile = new ICatchFile(33, ICatchFileType.ICH_TYPE_VIDEO, fileName,"", 0);
		Log.e(TAG,"[Normal] -- VideoPlayback: begin startPlaybackStream file=" + fileName);
		try {
			retValue = videoPlayback.play(icathfile, false, false);
//			retValue = videoPlayback.play(icathfile);
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchNoSuchFileException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchNoSuchFileException");
			
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: -----------end startPlaybackStream retValue =" + retValue);
		return retValue;
	}

	public boolean pausePlayback() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin pausePlayback");
		boolean retValue = false;
		try {
			retValue = videoPlayback.pause();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchPauseFailedException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchPauseFailedException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end pausePlayback =" + retValue);
		return retValue;
	}

	/**
	 * 
	 * Added by zhangyanhu C01012,2014-3-20
	 */
	public boolean resumePlayback() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin resumePlayback");
		boolean retValue = false;
		try {
			retValue = videoPlayback.resume();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchResumeFailedException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchResumeFailedException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end resumePlayback retValue=" + retValue);
		return retValue;
	}

	public int getVideoDuration() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin getVideoDuration");
		double temp = 0;
		try {
			temp = videoPlayback.getLength();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end getVideoDuration temp =" + temp);
		Log.e(TAG,"[Normal] -- VideoPlayback: end getVideoDuration length =" + Double.valueOf(temp * 100).intValue());
		return (Double.valueOf(temp * 100).intValue());
	}

	public boolean videoSeek(double position) {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin videoSeek position = " + position);
		boolean retValue = false;
		try {
			retValue = videoPlayback.seek(position);
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchSeekFailedException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSeekFailedException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end videoSeek retValue=" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public boolean getNextVideoFrame(ICatchFrameBuffer buffer) {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin getNextVideoFrame");
		boolean retValue = false;
		try {
			retValue = videoPlayback.getNextVideoFrame(buffer);
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchBufferTooSmallException");
			e.printStackTrace();
		} catch (IchTryAgainException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchTryAgainException");
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidArgumentException");
			e.printStackTrace();
		} catch (IchVideoStreamClosedException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchVideoStreamClosedException");
			e.printStackTrace();
		} catch (IchPbStreamPausedException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchPbStreamPausedException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end getNextVideoFrame  retValue= " + retValue);
		return retValue;
	}

	public boolean getNextAudioFrame(ICatchFrameBuffer buffer) {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin getNextAudioFrame");
		boolean retValue = false;
		try {
			retValue = videoPlayback.getNextAudioFrame(buffer);
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchStreamNotRunningException");
			
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchBufferTooSmallException");
			e.printStackTrace();
		} catch (IchTryAgainException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchTryAgainException");
			e.printStackTrace();
		} catch (IchInvalidArgumentException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchInvalidArgumentException");
			e.printStackTrace();
		} catch (IchAudioStreamClosedException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchAudioStreamClosedException");
			
			e.printStackTrace();
		} catch (IchPbStreamPausedException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: getNextAudioFrame IchPbStreamPausedException");
			
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end getNextAudioFrame  retValue= " + retValue);
		return retValue;
	}

	public boolean containsAudioStream() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin containsAudioStream");
		boolean retValue = false;
		try {
			retValue = videoPlayback.containsAudioStream();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end containsAudioStream  retValue= " + retValue);
		return retValue;
	}

	public ICatchAudioFormat getAudioFormat() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin getAudioFormat");
		ICatchAudioFormat retValue = null;
		try {
			retValue = videoPlayback.getAudioFormat();

		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- VideoPlayback: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			Log.e(TAG,"[Error] -- VideoPlayback: IchStreamNotRunningException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- VideoPlayback: end getAudioFormat  retValue= " + retValue);
		return retValue;
	}

	public ICatchVideoFormat getVideoFormat() {
		Log.e(TAG,"[Normal] -- VideoPlayback: begin getVideoFormat");
		ICatchVideoFormat retValue = null;
		try {
			retValue = videoPlayback.getVideoFormat();
		} catch (IchSocketException e) {
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			e.printStackTrace();
		} catch (IchStreamNotRunningException e) {
			
			e.printStackTrace();
		}

		Log.e(TAG,"[Normal] -- VideoPlayback: end getVideoFormat  retValue= " + retValue);
		return retValue;
	}

}
