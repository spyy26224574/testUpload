/**
 * Added by zhangyanhu C01012,2014-6-27
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.CameraFactory;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.exception.IchBufferTooSmallException;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchDeviceException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchNoSuchFileException;
import com.icatch.wificam.customer.exception.IchNoSuchPathException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;

import java.util.List;

/**
 * Added by zhangyanhu C01012,2014-6-27
 */
public class FileOperation {
	private static final String TAG = "FileOperation";
	private static FileOperation instance;
	private ICatchWificamPlayback cameraPlayback;

	public static FileOperation getInstance() {
		if (instance == null) {
			instance = new FileOperation();
		}
		return instance;
	}

	private FileOperation() {

	}

	public void initICatchWificamPlayback() {
		cameraPlayback = CameraFactory.getInstance().getSunplusCamera().getPlaybackClient();
	}
	
	public void initFileOperation(ICatchWificamPlayback playback) {
		this.cameraPlayback = playback;
	}

	public boolean cancelDownload() {
		Log.e(TAG, "cancelDownload: [Normal] -- FileOperation: begin cancelDownload");
		if(cameraPlayback == null){
			return true;
		}
		boolean retValue = false;
		try {
			retValue = cameraPlayback.cancelFileDownload();
		} catch (IchSocketException e) {
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
			
			e.printStackTrace();
		} catch (IchDeviceException e) {
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			
			e.printStackTrace();
		}
		// }
		Log.e(TAG,"[Normal] -- FileOperation: end cancelDownload retValue =" + retValue);
		return retValue;
	}

	public List<ICatchFile> getFileList(ICatchFileType type) {
		Log.e(TAG,"[Normal] -- FileOperation: begin getFileList");
		List<ICatchFile> list = null;
		try {
			//Log.d("1111start listFiles cameraPlayback=" + cameraPlayback);
			list = cameraPlayback.listFiles(type);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchNoSuchPathException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchPathException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end getFileList list=" + list);
		return list;
	}

	public boolean deleteFile(ICatchFile file) {
		Log.e(TAG,"[Normal] -- FileOperation: begin deleteFile filename =" + file.getFileName());
		boolean retValue = false;
		try {
			retValue = cameraPlayback.deleteFile(file);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end deleteFile retValue=" + retValue);
		return retValue;
	}

	public boolean downloadFile(ICatchFile file, String path) {
		Log.e(TAG,"[Normal] -- FileOperation: begin downloadFile filename =" + file.getFileName());
		Log.e(TAG,"[Normal] -- FileOperation: begin downloadFile path =" + path);
		boolean retValue = false;
		try {
			retValue = cameraPlayback.downloadFile(file, path);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end downloadFile retValue =" + retValue);
		return retValue;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public ICatchFrameBuffer downloadFile(ICatchFile curFile) {
		Log.e(TAG,"[Normal] -- FileOperation: begin downloadFile for buffer filename =" + curFile.getFileName());
		ICatchFrameBuffer buffer = null;
		try {
			buffer = cameraPlayback.downloadFile(curFile);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchBufferTooSmallException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end downloadFile for buffer, buffer =" + buffer);
		return buffer;
	}

	/**
	 * 
	 * Added by zhangyanhu C01012,2014-10-28
	 */

	public ICatchFrameBuffer getQuickview(ICatchFile curFile) {
		Log.e(TAG,"[Normal] -- FileOperation: begin getQuickview for buffer filename =" + curFile.getFileName());
		ICatchFrameBuffer buffer = null;
		try {
			buffer = cameraPlayback.getQuickview(curFile);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchBufferTooSmallException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end getQuickview for buffer, buffer =" + buffer);
		return buffer;
	}

	/**
	 * Added by zhangyanhu C01012,2014-7-2
	 */
	public ICatchFrameBuffer getThumbnail(ICatchFile file) {
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail");
		// TODO Auto-generated method stub
		ICatchFrameBuffer frameBuffer = null;
		try {
			Log.e(TAG,"1111start cameraPlayback.getThumbnail(file) cameraPlayback=" + cameraPlayback);
			frameBuffer = cameraPlayback.getThumbnail(file);
			Log.e(TAG,"1111end cameraPlayback.getThumbnail(file)");
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchBufferTooSmallException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end getThumbnail frameBuffer=" + frameBuffer);
		return frameBuffer;
	}
	
	public ICatchFrameBuffer getThumbnail(String filePath) {
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail");
		// TODO Auto-generated method stub
		ICatchFile icathfile = new ICatchFile(33, ICatchFileType.ICH_TYPE_VIDEO, filePath,"", 0);
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail file=" + filePath);
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail cameraPlayback=" + cameraPlayback);
		
		
		ICatchFrameBuffer frameBuffer = null;
		try {
			Log.e(TAG,"teststart cameraPlayback.getThumbnail(file) cameraPlayback=" + cameraPlayback);
			Log.e(TAG,"1111start cameraPlayback.getThumbnail(file) cameraPlayback=" + cameraPlayback);
			frameBuffer = cameraPlayback.getThumbnail(icathfile);
			Log.e(TAG,"1111end cameraPlayback.getThumbnail(file)");
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchBufferTooSmallException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end getThumbnail frameBuffer=" + frameBuffer);
		return frameBuffer;
	}
	
	public ICatchFrameBuffer getThumbnail(ICatchWificamPlayback wificamPlayback, String filePath) {
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail");
		ICatchFile icathfile = new ICatchFile(33, ICatchFileType.ICH_TYPE_VIDEO, filePath,"", 0);
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail file=" + filePath);
		Log.e(TAG,"[Normal] -- FileOperation: begin getThumbnail cameraPlayback=" + wificamPlayback);
		ICatchFrameBuffer frameBuffer = null;
		try {
			Log.e(TAG,"teststart cameraPlayback.getThumbnail(file) cameraPlayback=" + wificamPlayback);
			frameBuffer = wificamPlayback.getThumbnail(icathfile);
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		} catch (IchBufferTooSmallException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchBufferTooSmallException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end getThumbnail frameBuffer=" + frameBuffer);
		return frameBuffer;
	}
	//20160608 add 
	public boolean uploadFile(String localfilepath, String remotefilepath) {  // remote path need exist 
		Log.e(TAG,"[Normal] -- FileOperation: begin uploadFile localpath =" + localfilepath + " remotepath="+remotefilepath );
		boolean retValue = false;
		try {
			retValue = cameraPlayback.uploadFile(localfilepath,remotefilepath );
		} catch (IchSocketException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchSocketException");
			e.printStackTrace();
		} catch (IchCameraModeException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchCameraModeException");
			e.printStackTrace();
		} catch (IchInvalidSessionException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchInvalidSessionException");
		} catch (IchNoSuchFileException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchNoSuchFileException");
			e.printStackTrace();
		} catch (IchDeviceException e) {
			
			Log.e(TAG,"[Error] -- FileOperation: IchDeviceException");
			e.printStackTrace();
		}
		Log.e(TAG,"[Normal] -- FileOperation: end uploadFile retValue=" + retValue);
		return retValue;
	}

}
