/**
 * Added by zhangyanhu C01012,2014-6-27
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.CameraFactory;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchCaptureImageException;
import com.icatch.wificam.customer.exception.IchDeviceException;
import com.icatch.wificam.customer.exception.IchDevicePropException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchListenerExistsException;
import com.icatch.wificam.customer.exception.IchListenerNotExistsException;
import com.icatch.wificam.customer.exception.IchNotSupportedException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.exception.IchStorageFormatException;
import com.icatch.wificam.customer.exception.IchTimeOutException;
import com.icatch.wificam.customer.type.ICatchEventID;

/**
 * Added by zhangyanhu C01012,2014-6-27
 */
public class CameraAction {
    private static final String TAG = "CameraAction";
    private static CameraAction instance;
    private ICatchWificamControl cameraAction;
    public ICatchWificamAssist cameraAssist;

    public static CameraAction getInstance() {
        if (instance == null) {
            instance = new CameraAction();
        }
        return instance;
    }

    private CameraAction() {

    }

    public void initCameraAction() {
        cameraAction = CameraFactory.getInstance().getSunplusCamera().getCameraActionClient();
        cameraAssist = CameraFactory.getInstance().getSunplusCamera().getCameraAssistClint();
    }

    //用于本地vidoe播放对cameraAction的初始化;
    public void initCameraAction(ICatchWificamControl myWificamControl) {
        this.cameraAction = myWificamControl;
    }

    public boolean capturePhoto() {
        Log.e(TAG, "capturePhoto: [Normal] -- CameraAction: begin doStillCapture");
        boolean ret = false;
        try {
            ret = cameraAction.capturePhoto();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchCaptureImageException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCaptureImageException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end doStillCapture ret = " + ret);
        return ret;
    }

    public boolean triggerCapturePhoto() {
        Log.e(TAG, "[Normal] -- CameraAction: begin triggerCapturePhoto");
        boolean ret = false;
        try {
            ret = cameraAction.triggerCapturePhoto();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchCaptureImageException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCaptureImageException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end triggerCapturePhoto ret = " + ret);
        return ret;
    }

    public boolean startMovieRecord() {
        Log.e(TAG, "[Normal] -- CameraAction: begin startVideoCapture");
        boolean ret = false;

        try {
            ret = cameraAction.startMovieRecord();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end startVideoCapture ret =" + ret);
        return ret;
    }

    public boolean startTimeLapse() {
        Log.e(TAG, "[Normal] -- CameraAction: begin startTimeLapse");
        boolean ret = false;

        try {
            ret = cameraAction.startTimeLapse();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end startTimeLapse ret =" + ret);
        return ret;
    }

    public boolean stopTimeLapse() {
        Log.e(TAG, "[Normal] -- CameraAction: begin stopMovieRecordTimeLapse");
        boolean ret = false;

        try {
            ret = cameraAction.stopTimeLapse();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end stopMovieRecordTimeLapse ret =" + ret);
        return ret;
    }

    public boolean stopVideoCapture() {
        Log.e(TAG, "[Normal] -- CameraAction: begin stopVideoCapture");
        boolean ret = false;

        try {
            ret = cameraAction.stopMovieRecord();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end stopVideoCapture ret =" + ret);
        return ret;
    }

    public boolean formatStorage() {
        Log.e(TAG, "[Normal] -- CameraAction: begin formatSD");
        boolean retVal = false;

        try {
            retVal = cameraAction.formatStorage();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchStorageFormatException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchStorageFormatException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: begin formatSD retVal =" + retVal);
        return retVal;
    }

    public boolean sleepCamera() {
        Log.e(TAG, "[Normal] -- CameraAction: begin sleepCamera");
        boolean retValue = false;
        try {
            try {
                retValue = cameraAction.toStandbyMode();
            } catch (IchDeviceException e) {

                Log.e(TAG, "[Error] -- CameraAction: IchDeviceException");
                e.printStackTrace();
            } catch (IchInvalidSessionException e) {
                Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");

                e.printStackTrace();
            }
        } catch (IchSocketException e) {

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end sleepCamera retValue =" + retValue);
        return retValue;
    }

    public boolean addCustomEventListener(int eventID, ICatchWificamListener listener) {
        Log.e(TAG, "[Normal] -- CameraAction: begin addEventListener eventID=" + eventID);
        boolean retValue = false;
        try {
            retValue = cameraAction.addCustomEventListener(eventID, listener);
        } catch (IchListenerExistsException e) {

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            e.printStackTrace();
        }

        Log.e(TAG, "[Normal] -- CameraAction: end addEventListener retValue = " + retValue);
        return retValue;
    }

    public boolean delCustomEventListener(int eventID, ICatchWificamListener listener) {
        Log.e(TAG, "[Normal] -- CameraAction: begin delEventListener eventID=" + eventID);
        boolean retValue = false;
        try {
            retValue = cameraAction.delCustomEventListener(eventID, listener);
        } catch (IchListenerNotExistsException e) {

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end delEventListener retValue = " + retValue);
        return retValue;
    }

    public boolean addEventListener(int eventID, ICatchWificamListener listener) {
        Log.e(TAG, "[Normal] -- CameraAction: begin addEventListener eventID=" + eventID);

        boolean retValue = false;
        try {

            retValue = cameraAction.addEventListener(eventID, listener);

        } catch (IchListenerExistsException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchListenerExistsException");
            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");
            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end addEventListener retValue = " + retValue);
        return retValue;
    }

    public boolean delEventListener(int eventID, ICatchWificamListener listener) {
        Log.e(TAG, "[Normal] -- CameraAction: begin delEventListener eventID=" + eventID);
        boolean retValue = false;
        try {
            retValue = cameraAction.delEventListener(eventID, listener);
        } catch (IchListenerNotExistsException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchListenerExistsException");
            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");
            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end delEventListener retValue = " + retValue);
        return retValue;
    }

    public static boolean addScanEventListener(ICatchWificamListener listener) {
        boolean retValue = false;
        try {
            retValue = ICatchWificamSession.addEventListener(ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD, listener);
        } catch (IchListenerExistsException e) {

            e.printStackTrace();
        }
        return retValue;
    }

    public static boolean delScanEventListener(ICatchWificamListener listener) {
        boolean retValue = false;
        try {
            //retValue = ICatchWificamSession.delEventListener(ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD, listener);
            retValue = ICatchWificamSession.delEventListener(ICatchEventID.ICATCH_EVENT_DEVICE_SCAN_ADD, listener);
        } catch (IchListenerNotExistsException e) {

            e.printStackTrace();
        }
        return retValue;
    }

    /**
     * Added by zhangyanhu C01012,2014-7-2
     */
    public String getCameraMacAddress() {
        // TODO Auto-generated method stub
        String macAddress = "";
        macAddress = cameraAction.getMacAddress();
        return macAddress;
    }

    public boolean zoomIn() {
        Log.e(TAG, "[Normal] -- CameraAction: begin zoomIn");
        boolean retValue = false;
        try {
            retValue = cameraAction.zoomIn();
        } catch (IchSocketException e) {

            e.printStackTrace();
        } catch (IchCameraModeException e) {

            e.printStackTrace();
        } catch (IchStorageFormatException e) {

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end zoomIn retValue = " + retValue);
        return retValue;
    }

    public boolean zoomOut() {
        Log.e(TAG, "[Normal] -- CameraAction: begin zoomOut");
        boolean retValue = false;
        try {
            retValue = cameraAction.zoomOut();
        } catch (IchSocketException | IchCameraModeException | IchStorageFormatException | IchInvalidSessionException e) {

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end zoomOut retValue = " + retValue);
        return retValue;
    }

    public boolean updateFW() {
        boolean ret = false;
        Log.e(TAG, "[Normal] -- CameraAction: begin update FW");
        SunplusSession mSDKSession = CameraFactory.getInstance().getSunplusCamera().getSunplusSession();
        try {
            ret = cameraAssist.updateFw(mSDKSession.getSDKSession(), GlobalInfo.UPDATEFW_FILENAME);
        } catch (IchInvalidSessionException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");
            e.printStackTrace();
        } catch (IchSocketException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");
            e.printStackTrace();
        } catch (IchCameraModeException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");
            e.printStackTrace();
        } catch (IchDevicePropException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchDevicePropException");
            e.printStackTrace();
        } catch (IchTimeOutException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchTimeOutException");
            e.printStackTrace();
        } catch (IchDeviceException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchDeviceException");
            e.printStackTrace();
        } catch (IchNotSupportedException e) {

            Log.e(TAG, "[Error] -- CameraAction: IchNotSupportedException");
            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraAction: end updateFW");
        return ret;
    }

    // JIRA ICOM-1577 End:Add by b.jiang C01063 2015-07-17
    public static boolean addStaicEventListener(int eventId, ICatchWificamListener listener) {
        boolean retValue = false;
        try {
            retValue = ICatchWificamSession.addEventListener(eventId, listener);
        } catch (IchListenerExistsException e) {

            e.printStackTrace();
        }
        return retValue;
    }

    public static boolean delStaicEventListener(int eventId, ICatchWificamListener listener) {
        boolean retValue = false;
        try {
            retValue = ICatchWificamSession.delEventListener(eventId, listener);
        } catch (IchListenerNotExistsException e) {

            e.printStackTrace();
        }
        return retValue;
    }

    // JIRA ICOM-1577 End:Add by b.jiang C01063 2015-07-17

    public boolean previewMove(int xshift, int yshfit) {
        Log.e(TAG, "[Normal] -- CameraAction: begin previewMove");
        boolean ret = false;
        ret = cameraAction.pan(xshift, yshfit);
        Log.e(TAG, "[Normal] -- CameraAction: end previewMove ret = " + ret);
        return ret;
        //return true;
    }

    public boolean resetPreviewMove() {
        Log.e(TAG, "[Normal] -- CameraAction: begin resetPreviewMove");
        boolean ret = false;
        ret = cameraAction.panReset();
        Log.e(TAG, "[Normal] -- CameraAction: end resetPreviewMove ret = " + ret);
        return ret;
        //return true;
    }
}
