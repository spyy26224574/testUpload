/**
 * Added by zhangyanhu C01012,2014-6-23
 */
package com.adai.camera.sunplus.SDKAPI;

import android.util.Log;

import com.adai.camera.CameraFactory;
import com.adai.camera.sunplus.data.FwToApp;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.data.PropertyId;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.ICatchWificamUtil;
import com.icatch.wificam.customer.exception.IchCameraModeException;
import com.icatch.wificam.customer.exception.IchDevicePropException;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.exception.IchNoSDCardException;
import com.icatch.wificam.customer.exception.IchSocketException;
import com.icatch.wificam.customer.type.ICatchCodec;
import com.icatch.wificam.customer.type.ICatchLightFrequency;
import com.icatch.wificam.customer.type.ICatchMode;
import com.icatch.wificam.customer.type.ICatchVideoFormat;
import com.icatch.wificam.customer.type.ICatchVideoSize;

import java.io.Serializable;
import java.util.List;

/**
 * @author huangxy
 */
public class CameraProperties implements Serializable {
    private static final String TAG = CameraProperties.class.getSimpleName();
    private static final long serialVersionUID = 4876146396113074804L;
    private List<Integer> fuction;
    // private PreviewStream previewStream = new PreviewStream();
    private List<ICatchMode> modeList;
    private static CameraProperties instance;
    private ICatchWificamProperty cameraConfiguration;
    private ICatchWificamControl cameraAction;

    public static CameraProperties getInstance() {
        if (instance == null) {
            instance = new CameraProperties();
        }
        return instance;
    }

    private CameraProperties() {

    }

    public void initCameraProperties() {
        cameraConfiguration = CameraFactory.getInstance().getSunplusCamera().getCameraPropertyClint();
        cameraAction = CameraFactory.getInstance().getSunplusCamera().getCameraActionClient();
    }

    public List<String> getSupportedStringPropertyValues(int propertyId) {
        List<String> supportedStringPropertyValues = null;
        try {
            supportedStringPropertyValues = cameraConfiguration.getSupportedStringPropertyValues(propertyId);
            if (supportedStringPropertyValues != null) {
                for (String s : supportedStringPropertyValues) {
                    Log.e(TAG, "getSupportedStringPropertyValues: propertyid =" + propertyId + "values" + s);
                }
            }
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        } catch (IchSocketException e) {
            e.printStackTrace();
        } catch (IchCameraModeException e) {
            e.printStackTrace();
        } catch (IchDevicePropException e) {
            e.printStackTrace();
        }
        return supportedStringPropertyValues;
    }

    public List<String> getSupportedImageSizes() {
        List<String> list = null;
        try {
            list = cameraConfiguration.getSupportedImageSizes();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");
            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");
            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");
            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");
            e.printStackTrace();
        }
        assert list != null;
        Log.e(TAG, "[Normal] -- CameraProperties: end getSupportedImageSizes list.size =" + list.size());
        return list;
    }

    public List<String> getSupportedVideoSizes() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedVideoSizes");
        List<String> list = null;
        try {
            list = cameraConfiguration.getSupportedVideoSizes();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getSupportedWhiteBalances() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedWhiteBalances");
        List<Integer> list = null;
        try {
            list = cameraConfiguration.getSupportedWhiteBalances();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getSupportedCaptureDelays() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedCaptureDelays");
        List<Integer> list = null;
        try {
            list = cameraConfiguration.getSupportedCaptureDelays();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getSupportedLightFrequencys() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedLightFrequencys");
        List<Integer> list = null;

        try {
            list = cameraConfiguration.getSupportedLightFrequencies();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        // delete LIGHT_FREQUENCY_AUTO because UI don't need this option
        assert list != null;
        for (int ii = 0; ii < list.size(); ii++) {
            if (list.get(ii) == ICatchLightFrequency.ICH_LIGHT_FREQUENCY_AUTO) {
                list.remove(ii);
            }
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getSupportedLightFrequencys list.size() =" + list.size());
        return list;
    }

    public boolean setImageSize(String value) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setImageSize set value =" + value);
        boolean retVal = false;

        try {
            retVal = cameraConfiguration.setImageSize(value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setImageSize retVal=" + retVal);
        return retVal;
    }

    public boolean setVideoSize(String value) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setVideoSize set value =" + value);
        boolean retVal = false;

        try {
            retVal = cameraConfiguration.setVideoSize(value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setVideoSize retVal=" + retVal);
        return retVal;
    }

    public boolean setWhiteBalance(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setWhiteBalanceset value =" + value);
        boolean retVal = false;
        if (value < 0 || value == 0xff) {
            return false;
        }
        try {
            retVal = cameraConfiguration.setWhiteBalance(value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setWhiteBalance retVal=" + retVal);
        return retVal;
    }

    public boolean setLightFrequency(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setLightFrequency set value =" + value);
        boolean retVal = false;
        if (value < 0 || value == 0xff) {
            return false;
        }
        try {
            retVal = cameraConfiguration.setLightFrequency(value);    //.setLightFrequency(value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setLightFrequency retVal=" + retVal);
        return retVal;
    }

    public String getCurrentImageSize() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentImageSize");
        String value = "unknown";

        try {
            value = cameraConfiguration.getCurrentImageSize();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentImageSize value =" + value);
        return value;
    }

    public String getCurrentVideoSize() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentVideoSize");
        String value = "unknown";

        try {
            value = cameraConfiguration.getCurrentVideoSize();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentVideoSize value =" + value);
        return value;
    }

    public int getCurrentWhiteBalance() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentWhiteBalance");
        int value = 0xff;
        try {
            Log.e(TAG, "[Normal] -- CameraProperties: ******value=   " + value);
            value = cameraConfiguration.getCurrentWhiteBalance();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentWhiteBalance retvalue =" + value);
        return value;
    }

    public int getCurrentLightFrequency() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentLightFrequency");
        int value = 0xff;
        try {
            value = cameraConfiguration.getCurrentLightFrequency();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentLightFrequency value =" + value);
        return value;
    }

    public boolean setCaptureDelay(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setCaptureDelay set value =" + value);
        boolean retVal = false;

        try {
            Log.e(TAG, "[Normal] -- CameraProperties: start setCaptureDelay ");
            retVal = cameraConfiguration.setCaptureDelay(value);
            Log.e(TAG, "[Normal] -- CameraProperties: end setCaptureDelay ");
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCaptureDelay retVal =" + retVal);
        return retVal;
    }

    public int getCurrentCaptureDelay() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentCaptureDelay");
        int retVal = 0;

        try {
            retVal = cameraConfiguration.getCurrentCaptureDelay();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentCaptureDelay retVal =" + retVal);
        return retVal;
    }

    public int getCurrentDateStamp() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentDateStampType");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentDateStamp();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: getCurrentDateStampType retValue =" + retValue);
        return retValue;
    }

    /**
     * Added by zhangyanhu C01012,2014-4-1
     */
    public boolean setDateStamp(int dateStamp) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setDateStampType set value = " + dateStamp);
        Boolean retValue = false;
        try {
            retValue = cameraConfiguration.setDateStamp(dateStamp);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentVideoSize retValue =" + retValue);
        return retValue;
    }

    /**
     * Added by zhangyanhu C01012,2014-4-1
     */
    public List<Integer> getDateStampList() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getDateStampList");
        List<Integer> list = null;
        try {
            list = cameraConfiguration.getSupportedDateStamps();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getSupportFuction() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportFuction");
        List<Integer> fuction = null;
        // List<Integer> temp = null;
        try {
            fuction = cameraConfiguration.getSupportedProperties();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return fuction;
    }

    /**
     * to prase the burst number Added by zhangyanhu C01012,2014-2-10
     */
    public int getCurrentBurstNum() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentBurstNum");
        int number = 0xff;
        try {
            number = cameraConfiguration.getCurrentBurstNumber();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: getCurrentBurstNum num =" + number);
        return number;
    }

    public int getCurrentAppBurstNum() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentAppBurstNum");
        int number = 0xff;
        try {
            number = cameraConfiguration.getCurrentBurstNumber();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        number = FwToApp.getInstance().getAppBurstNum(number);
        Log.e(TAG, "[Normal] -- CameraProperties: getCurrentAppBurstNum num =" + number);
        return number;
    }

    public boolean setCurrentBurst(int burstNum) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setCurrentBurst set value = " + burstNum);
        if (burstNum < 0 || burstNum == 0xff) {
            return false;
        }
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setBurstNumber(burstNum);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCurrentBurst retValue =" + retValue);
        return retValue;
    }

    public int getRemainImageNum() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getRemainImageNum");
        int num = 0;
        try {
            num = cameraAction.getFreeSpaceInImages();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        } catch (IchNoSDCardException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchNoSDCardException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getRemainImageNum num =" + num);
        return num;
    }

    public int getRecordingRemainTime() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getRecordingRemainTimeInt");
        int recordingTime = 0;

        try {
            recordingTime = cameraAction.getRemainRecordingTime();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        } catch (IchNoSDCardException e) {

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getRecordingRemainTimeInt recordingTime =" + recordingTime);
        return recordingTime;
    }

    public boolean isSDCardExist() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin isSDCardExist GlobalInfo.isSdCardExist =" + GlobalInfo.isSdCardExist);
        Boolean isReady = false;
        // JIRA ICOM-1577 Begin:Modify by b.jiang C01063 2015-07-17

        // try {
        // isReady = cameraAction.isSDCardExist();
        // } catch (IchSocketException e) {
        // Log.e(TAG,"[Error] -- CameraProperties: ",
        // "IchSocketException");
        //
        // e.printStackTrace();
        // } catch (IchCameraModeException e) {
        // Log.e(TAG,"[Error] -- CameraProperties: ",
        // "IchCameraModeException");
        //
        // e.printStackTrace();
        // } catch (IchInvalidSessionException e) {
        // Log.e(TAG,"[Error] -- CameraProperties: ",
        // "IchInvalidSessionException");
        //
        // e.printStackTrace();
        // }
        // Log.e(TAG,"[Normal] -- CameraProperties: ",
        // "end isSDCardExist isReady =" + isReady);
        // return isReady;

        return GlobalInfo.isSdCardExist;
        // JIRA ICOM-1577 End:Modify by b.jiang C01063 2015-07-17
    }

    public int getBatteryElectric() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getBatteryElectric");
        int electric = 0;
        try {
            electric = cameraAction.getCurrentBatteryLevel();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getBatteryElectric electric =" + electric);
        return electric;
    }

    public boolean supportVideoPlayback() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin hasVideoPlaybackFuction");
        boolean retValue = false;
        try {
            retValue = cameraAction.supportVideoPlayback();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        } catch (IchNoSDCardException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchNoSDCardException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: hasVideoPlaybackFuction retValue =" + retValue);
        return retValue;
        // return false;
    }

    public boolean cameraModeSupport(ICatchMode mode) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin cameraModeSupport  mode=" + mode);
        Boolean retValue = false;
        if (modeList == null) {
            modeList = getSupportedModes();
        }
        if (modeList == null) {
            return false;
        }
        if (modeList.contains(mode)) {
            retValue = true;
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end cameraModeSupport retValue =" + retValue);
        return retValue;
    }

    public String getCameraMacAddress() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCameraMacAddress macAddress macAddress ");
        String macAddress = cameraAction.getMacAddress();
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraMacAddress macAddress =" + macAddress);
        return macAddress;
    }

    public List<Integer> getConvertSupportedImageSizes() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getConvertSupportedImageSizes");
        List<String> list = getSupportedImageSizes();
        List<Integer> convertImageSizeList = null;
        // ICatchWificamUtil sizeAss = new ICatchWificamUtil();
        try {
            convertImageSizeList = ICatchWificamUtil.convertImageSizes(list);
        } catch (IchInvalidArgumentException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        }
        return convertImageSizeList;
    }

    public List<ICatchVideoSize> getConvertSupportedVideoSizes() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getConvertSupportedVideoSizes");
        List<String> list = getSupportedVideoSizes();
        List<ICatchVideoSize> convertVideoSizeList = null;
        // ICatchWificamUtil sizeAss = new ICatchWificamUtil();
        try {
            convertVideoSizeList = ICatchWificamUtil.convertVideoSizes(list);
        } catch (IchInvalidArgumentException e) {

            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidArgumentException");
            e.printStackTrace();
        }
        assert convertVideoSizeList != null;
        return convertVideoSizeList;
    }

    public boolean hasFuction(int fuc) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin hasFuction query fuction = " + fuc);
        if (fuction == null) {
            fuction = getSupportFuction();
        }
        Boolean retValue = false;
        if (fuction.contains(fuc)) {
            retValue = true;
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end hasFuction retValue =" + retValue);
        return retValue;
    }

    /**
     * Added by zhangyanhu C01012,2014-7-4
     */
    public List<Integer> getsupportedDateStamps() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getsupportedDateStamps");
        List<Integer> list = null;

        try {
            list = cameraConfiguration.getSupportedDateStamps();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getsupportedBurstNums() {
        // TODO Auto-generated method stub
        Log.e(TAG, "[Normal] -- CameraProperties: begin getsupportedBurstNums");
        List<Integer> list = null;

        try {
            list = cameraConfiguration.getSupportedBurstNumbers();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    /**
     * Added by zhangyanhu C01012,2014-7-4
     */
    public List<Integer> getSupportedFrequencies() {
        // TODO Auto-generated method stub
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedFrequencies");
        List<Integer> list = null;
        try {
            list = cameraConfiguration.getSupportedLightFrequencies();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    /**
     * Added by zhangyanhu C01012,2014-8-21
     *
     * @return
     */
    public List<ICatchMode> getSupportedModes() {
        Log.e(TAG, "[Normal] -- CameraAction: begin getSupportedModes");

        List<ICatchMode> list = null;
        try {
            list = cameraAction.getSupportedModes();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraAction: IchInvalidSessionException");
            e.printStackTrace();
        } catch (Exception ignore) {

        }
        return list;
    }

    // public List<Integer> getSupportedTimeLapseStillDurations() {
    // Log.e(TAG,"[Normal] -- CameraProperties: ",
    // "begin getSupportedTimeLapseStillDurations");
    // List<Integer> list = null;
    // //boolean retValue = false;
    // try {
    // list = cameraConfiguration.getSupportedTimeLapseStillDurations();
    // } catch (IchSocketException e) {
    //
    // e.printStackTrace();
    // } catch (IchCameraModeException e) {
    //
    // e.printStackTrace();
    // } catch (IchDevicePropException e) {
    //
    // e.printStackTrace();
    // } catch (IchInvalidSessionException e) {
    //
    // e.printStackTrace();
    // }
    // Log.e(TAG,"[Normal] -- CameraProperties: ",
    // "end getSupportedTimeLapseStillDurations list =" + list.size());
    // return list;
    // }
    //
    // public List<Integer> getSupportedTimeLapseStillintervals() {
    // Log.e(TAG,"[Normal] -- CameraProperties: ",
    // "begin getSupportedTimeLapseStillintervals");
    // List<Integer> list = null;
    // //boolean retValue = false;
    // try {
    // list = cameraConfiguration.getSupportedTimeLapseStillIntervals();
    // } catch (IchSocketException e) {
    //
    // e.printStackTrace();
    // } catch (IchCameraModeException e) {
    //
    // e.printStackTrace();
    // } catch (IchDevicePropException e) {
    //
    // e.printStackTrace();
    // } catch (IchInvalidSessionException e) {
    //
    // e.printStackTrace();
    // }
    // Log.e(TAG,"[Normal] -- CameraProperties: ",
    // "end getSupportedTimeLapseStillintervals list =" + list.size());
    // return list;
    // }

    public List<Integer> getSupportedTimeLapseDurations() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedTimeLapseDurations");
        List<Integer> list = null;
        // boolean retValue = false;
        try {
            list = cameraConfiguration.getSupportedTimeLapseDurations();
        } catch (IchSocketException e) {

            e.printStackTrace();
        } catch (IchCameraModeException e) {

            e.printStackTrace();
        } catch (IchDevicePropException e) {

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            e.printStackTrace();
        }
        return list;
    }

    public List<Integer> getSupportedTimeLapseIntervals() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedTimeLapseIntervals");
        List<Integer> list = null;
        // boolean retValue = false;
        try {
            list = cameraConfiguration.getSupportedTimeLapseIntervals();
        } catch (IchSocketException e) {

            e.printStackTrace();
        } catch (IchCameraModeException e) {

            e.printStackTrace();
        } catch (IchDevicePropException e) {

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {

            e.printStackTrace();
        }
        return list;
    }

    public boolean setTimeLapseDuration(int timeDuration) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setTimeLapseDuration videoDuration =" + timeDuration);
        boolean retVal = false;
//		if (timeDuration < 0 || timeDuration == 0xff) {
//			return false;
//		}
        try {
            retVal = cameraConfiguration.setTimeLapseDuration(timeDuration);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setTimeLapseDuration retVal=" + retVal);
        return retVal;
    }

    public int getCurrentTimeLapseDuration() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentTimeLapseDuration");
        int retVal = 0xff;
        try {
            retVal = cameraConfiguration.getCurrentTimeLapseDuration();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentTimeLapseDuration retVal=" + retVal);
        return retVal;
    }

    public boolean setTimeLapseInterval(int timeInterval) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin setTimeLapseInterval videoDuration =" + timeInterval);
        boolean retVal = false;
        //JIRA ICOM-1764 Start modify by b.jiang 20160126
//		if (timeInterval < 0 || timeInterval == 0xff) {
//			return false;
//		}
        //JIRA ICOM-1764 Start modify by b.jiang 20160126
        Log.e(TAG, "[Normal] -- CameraProperties: timeInterval=" + timeInterval);

        try {
            retVal = cameraConfiguration.setTimeLapseInterval(timeInterval);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setTimeLapseInterval retVal=" + retVal);
        return retVal;
    }

    public int getCurrentTimeLapseInterval() {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getCurrentTimeLapseInterval");
        int retVal = 0xff;
        try {
            retVal = cameraConfiguration.getCurrentTimeLapseInterval();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentTimeLapseInterval retVal=" + retVal);
        return retVal;
    }

    public int getMaxZoomRatio() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getMaxZoomRatio");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getMaxZoomRatio();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getMaxZoomRatio retValue =" + retValue);
        return retValue;
    }

    public int getCurrentZoomRatio() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentZoomRatio");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentZoomRatio();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentZoomRatio retValue =" + retValue);
        return retValue;
    }

    public int getCurrentUpsideDown() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentUpsideDown");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentUpsideDown();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentUpsideDown retValue =" + retValue);
        return retValue;
    }

    public boolean setUpsideDown(int upside) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setUpsideDown upside = " + upside);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setUpsideDown(upside);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setUpsideDown retValue =" + retValue);
        return retValue;
    }

    public int getCurrentSlowMotion() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentSlowMotion");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentSlowMotion();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentSlowMotion retValue =" + retValue);
        return retValue;
    }

    public boolean setSlowMotion(int slowMotion) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setSlowMotion slowMotion = " + slowMotion);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setSlowMotion(slowMotion);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setSlowMotion retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraDate(String date) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCameraDate date = " + date);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.CAMERA_DATE, date);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraDate retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraEssidName(String ssidName) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCameraEssidName date = " + ssidName);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.ESSID_NAME, ssidName);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraEssidName retValue =" + retValue);
        return retValue;
    }

    public String getCameraEssidName() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraEssidName");
        String retValue = "";
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.ESSID_NAME);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraEssidName retValue =" + retValue);
        return retValue;
    }

    public String getCameraEssidPassword() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraEssidPassword");
        String retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.ESSID_PASSWORD);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraEssidPassword retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraEssidPassword(String ssidPassword) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setStringPropertyValue date = " + ssidPassword);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.ESSID_PASSWORD, ssidPassword);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraSsid retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraSsid(String ssid) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCameraSsid date = " + ssid);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.CAMERA_ESSID, ssid);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraSsid retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraName(String cameraName) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setStringPropertyValue cameraName = " + cameraName);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.CAMERA_NAME, cameraName);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setStringPropertyValue retValue =" + retValue);
        return retValue;
    }

    public String getCameraName() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraName");
        String retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.CAMERA_NAME);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraName retValue =" + retValue);
        return retValue;
    }

    public String getCameraName(ICatchWificamProperty cameraConfiguration1) {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraName");
        String retValue = null;
        try {
            retValue = cameraConfiguration1.getCurrentStringPropertyValue(PropertyId.CAMERA_NAME);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraName retValue =" + retValue);
        return retValue;
    }

    public String getCameraPasswordNew() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraPassword");
        String retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.CAMERA_PASSWORD_NEW);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraPassword retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraPasswordNew(String cameraNamePassword) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCameraPasswordNew cameraName = " + cameraNamePassword);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.CAMERA_PASSWORD_NEW, cameraNamePassword);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraPasswordNew retValue =" + retValue);
        return retValue;
    }

    public String getCameraSsid() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraSsid date = ");
        String retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.CAMERA_ESSID);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraSsid retValue =" + retValue);
        return retValue;
    }

    public boolean setCameraPassword(String password) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCameraSsid date = " + password);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.CAMERA_PASSWORD, password);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCameraSsid retValue =" + retValue);
        return retValue;
    }

    public String getCameraPassword() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCameraPassword date = ");
        String retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(PropertyId.CAMERA_PASSWORD);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCameraPassword retValue =" + retValue);
        return retValue;
    }

    public boolean setCaptureDelayMode(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setCaptureDelayMode value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setPropertyValue(PropertyId.CAPTURE_DELAY_MODE, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setCaptureDelayMode retValue =" + retValue);
        return retValue;
    }

    public int getCurrentCaptureDelayMode() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentCaptureDelayMode");
        int retValue = -1;
        try {
            retValue = cameraConfiguration.getCurrentPropertyValue(PropertyId.CAPTURE_DELAY_MODE);
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentCaptureDelayMode retValue =" + retValue);
        return retValue;
    }

    public int getVideoRecordingTime() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getRecordingTime");
        int retValue = 0;
        try {
            // JIRA ICOM-1608 Begin:Modify by b.jiang C01063 2015-07-20
            // 0xD7F0 -> PropertyId.VIDEO_RECORDING_TIME
            // retValue = cameraConfiguration.getCurrentPropertyValue(0xD7F0);
            retValue = cameraConfiguration.getCurrentPropertyValue(PropertyId.VIDEO_RECORDING_TIME);
            // JIRA ICOM-1608 End:Modify by b.jiang C01063 2015-07-20
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getRecordingTime retValue =" + retValue);
        return retValue;
    }

    public boolean setServiceEssid(String value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setServiceEssid value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.SERVICE_ESSID, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setServiceEssid retValue =" + retValue);
        return retValue;
    }

    public boolean setServicePassword(String value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setServicePassword value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(PropertyId.SERVICE_PASSWORD, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setServicePassword retValue =" + retValue);
        return retValue;
    }

    public boolean notifyFwToShareMode(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start notifyFwToShareMode value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setPropertyValue(PropertyId.NOTIFY_FW_TO_SHARE_MODE, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end notifyFwToShareMode retValue =" + retValue);
        return retValue;
    }

    public List<Integer> getSupportedPropertyValues(int propertyId) {
        Log.e(TAG, "[Normal] -- CameraProperties: begin getSupportedPropertyValues propertyId =" + propertyId);
        List<Integer> list = null;
        try {
            list = cameraConfiguration.getSupportedPropertyValues(propertyId);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return list;
    }

    public int getCurrentPropertyValue(int propertyId) {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentPropertyValue propertyId = " + propertyId);
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentPropertyValue(propertyId);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentPropertyValue retValue =" + retValue);
        return retValue;
    }

    public String getCurrentStringPropertyValue(int propertyId) {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentStringPropertyValue propertyId = " + propertyId);
        String retValue = "";
        try {
            retValue = cameraConfiguration.getCurrentStringPropertyValue(propertyId);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentStringPropertyValue retValue =" + retValue);
        return retValue;
    }

    public boolean setPropertyValue(int propertyId, int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setPropertyValue value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setPropertyValue(propertyId, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setPropertyValue retValue =" + retValue);
        return retValue;
    }

    public boolean setStringPropertyValue(int propertyId, String value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setStringPropertyValue value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStringPropertyValue(propertyId, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setStringPropertyValue retValue =" + retValue);
        return retValue;
    }

    public int getVideoSizeFlow() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getVideoSizeFlow");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentPropertyValue(PropertyId.VIDEO_SIZE_FLOW);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getVideoSizeFlow retValue =" + retValue);
        return retValue;
    }

    public boolean notifyCameraConnectChnage(int value) {
        Log.e(TAG, "[Normal] -- CameraProperties: start notifyCameraConnectChnage value = " + value);
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setPropertyValue(PropertyId.CAMERA_CONNECT_CHANGE, value);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end notifyCameraConnectChnage retValue =" + retValue);
        return retValue;
    }

    public List<ICatchVideoFormat> getResolutionList() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getResolution");
        List<ICatchVideoFormat> retList = null;
        try {
            retList = cameraConfiguration.getSupportedStreamingInfos();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return retList;
    }

    public String getBestResolution() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getBestResolution");
        String bestResolution = null;

        List<ICatchVideoFormat> tempList = getResolutionList(cameraConfiguration);
        if (tempList == null || tempList.size() == 0) {
            return null;
        }
        Log.e(TAG, "1111getResolutionList() tempList.size() = " + tempList.size());
        int tempWidth = 0;
        int tempHeigth = 0;

        ICatchVideoFormat temp;

        for (int ii = 0; ii < tempList.size(); ii++) {
            temp = tempList.get(ii);
            if (temp.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                if (bestResolution == null) {
                    bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps() + "&";
                }

                if (temp.getVideoW() == 640 && temp.getVideoH() == 360) {
                    bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps() + "&";
                    return bestResolution;
                } else if (temp.getVideoW() == 640 && temp.getVideoH() == 480) {
                    if (tempWidth != 640) {
                        bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps()
                                + "&";
                        tempWidth = 640;
                        tempHeigth = 480;
                    }
                } else if (temp.getVideoW() == 720) {
                    if (tempWidth != 640) {
                        if (temp.getVideoW() * 9 == temp.getVideoH() * 16)// 16:9优先
                        {
                            bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                    + temp.getFps() + "&";
                            tempWidth = 720;
                            tempHeigth = temp.getVideoH();
                        } else if (temp.getVideoW() * 3 == temp.getVideoH() * 4)// 4:3
                        {
                            if (tempWidth != 720)
                                bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                        + temp.getFps() + "&";
                            tempWidth = 720;
                            tempHeigth = temp.getVideoH();
                        }
                    }
                } else if (temp.getVideoW() < tempWidth) {
                    if (temp.getVideoW() * 9 == temp.getVideoH() * 16)// 16:9优先
                    {
                        bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps()
                                + "&";
                        tempWidth = temp.getVideoW();
                        tempHeigth = temp.getVideoH();
                    } else if (temp.getVideoW() * 3 == temp.getVideoH() * 4)// 4:3
                    {
                        if (tempWidth != temp.getVideoW())
                            bestResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                    + temp.getFps() + "&";
                        tempWidth = temp.getVideoW();
                        tempHeigth = temp.getVideoH();
                    }
                }
            }
        }
        if (bestResolution != null) {
            return bestResolution;
        }
        for (int ii = 0; ii < tempList.size(); ii++) {
            temp = tempList.get(ii);
            if (temp.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                if (bestResolution == null) {
                    bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps() + "&";
                }

                if (temp.getVideoW() == 640 && temp.getVideoH() == 360) {
                    bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps() + "&";
                    return bestResolution;
                } else if (temp.getVideoW() == 640 && temp.getVideoH() == 480) {
                    if (tempWidth != 640) {
                        bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps()
                                + "&";
                        tempWidth = 640;
                        tempHeigth = 480;
                    }
                } else if (temp.getVideoW() == 720) {
                    if (tempWidth != 640) {
                        if (temp.getVideoW() * 9 == temp.getVideoH() * 16)// 16:9优先
                        {
                            bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                    + temp.getFps() + "&";
                            tempWidth = 720;
                            tempHeigth = temp.getVideoH();
                        } else if (temp.getVideoW() * 3 == temp.getVideoH() * 4)// 4:3
                        {
                            if (tempWidth != 720)
                                bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                        + temp.getFps() + "&";
                            tempWidth = 720;
                            tempHeigth = temp.getVideoH();
                        }
                    }
                } else if (temp.getVideoW() < tempWidth) {
                    if (temp.getVideoW() * 9 == temp.getVideoH() * 16)// 16:9优先
                    {
                        bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps()
                                + "&";
                        tempWidth = temp.getVideoW();
                        tempHeigth = temp.getVideoH();
                    } else if (temp.getVideoW() * 3 == temp.getVideoH() * 4)// 4:3
                    {
                        if (tempWidth != temp.getVideoW())
                            bestResolution = "MJPG?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS="
                                    + temp.getFps() + "&";
                        tempWidth = temp.getVideoW();
                        tempHeigth = temp.getVideoH();
                    }
                }
            }
        }

        Log.e(TAG, "[Normal] -- CameraProperties: end getBestResolution");
        return bestResolution;

    }

    public List<ICatchVideoFormat> getResolutionList(ICatchWificamProperty cameraConfiguration) {
        Log.e(TAG, "[Normal] -- CameraProperties: start getResolutionList");
        List<ICatchVideoFormat> retList = null;
        try {
            retList = cameraConfiguration.getSupportedStreamingInfos();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        return retList;
    }

    public String getAppDefaultResolution() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getAppDefaultResolution");
        String appDefaultResolution = "";

        List<ICatchVideoFormat> tempList = getResolutionList(cameraConfiguration);
        if (tempList == null || tempList.size() == 0) {
            return null;
        }
        Log.e(TAG, "1111getResolutionList() tempList.size() = " + tempList.size());

        ICatchVideoFormat temp;

        for (int ii = 0; ii < tempList.size(); ii++) {
            temp = tempList.get(ii);

            if (temp.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                if (temp.getVideoW() == 1280 && temp.getVideoH() == 720 && temp.getBitrate() == 500000) {
                    appDefaultResolution = "H264?" + "W=" + temp.getVideoW() + "&H=" + temp.getVideoH() + "&BR=" + temp.getBitrate() + "&FPS=" + temp.getFps()
                            + "&";
                    return appDefaultResolution;
                }
            }
        }

        Log.e(TAG, "[Normal] -- CameraProperties: end getAppDefaultResolution");
        return appDefaultResolution;

    }

    public String getFWDefaultResolution() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getFWDefaultResolution");
        String resolution = null;
        ICatchVideoFormat retValue = null;
        try {
            retValue = cameraConfiguration.getCurrentStreamingInfo();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        if (retValue != null) {
            if (retValue.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                resolution = "H264?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate() + "&FPS="
                        + retValue.getFps() + "&";
            } else if (retValue.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                resolution = "MJPG?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate() + "&FPS="
                        + retValue.getFps() + "&";
            }
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getFWDefaultResolution");
        return resolution;

    }

    public boolean setStreamingInfo(ICatchVideoFormat iCatchVideoFormat) {
        Log.e(TAG, "[Normal] -- CameraProperties: start setStreamingInfo");
        boolean retValue = false;
        try {
            retValue = cameraConfiguration.setStreamingInfo(iCatchVideoFormat);
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end setStreamingInfo");
        return retValue;

    }

    public boolean setStreamInfo(ICatchVideoFormat iCatchVideoFormat) {
        Log.e(TAG, "[Normal] -- CameraProperties:  setStreamInfo");

        try {
            return cameraConfiguration.setStreamingInfo(iCatchVideoFormat);
        } catch (IchSocketException | IchInvalidSessionException | IchCameraModeException | IchDevicePropException e) {
            e.printStackTrace();
        }
        return false;
    }

    public ICatchVideoFormat getStreamInfo() {
        try {
            return cameraConfiguration.getCurrentStreamingInfo();
        } catch (IchSocketException e) {
            e.printStackTrace();
        } catch (IchCameraModeException e) {
            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        } catch (IchDevicePropException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getCurrentStreamInfo() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getCurrentStreamInfo");

        ICatchVideoFormat retValue = null;
        String bestResolution = null;
        try {
            retValue = cameraConfiguration.getCurrentStreamingInfo();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        if (retValue == null) {
            Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentStreamInfo retValue = " + retValue);
            return null;
        }
        //JIRA ICOM-2520 Start add by b.jiang 2015-12-23
        //判断是否支援property code 0XD7AE，如果支援送带FPS的，不支援就不送带fps的
        if (hasFuction(0xd7ae)) {
            if (retValue.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                bestResolution = "H264?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate() + "&FPS="
                        + retValue.getFps() + "&";
            } else if (retValue.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                bestResolution = "MJPG?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate() + "&FPS="
                        + retValue.getFps() + "&";
            }
        } else {
            if (retValue.getCodec() == ICatchCodec.ICH_CODEC_H264) {
                bestResolution = "H264?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate();
            } else if (retValue.getCodec() == ICatchCodec.ICH_CODEC_JPEG) {
                bestResolution = "MJPG?" + "W=" + retValue.getVideoW() + "&H=" + retValue.getVideoH() + "&BR=" + retValue.getBitrate();
            }
        }
        //JIRA ICOM-2520 End add by b.jiang 2015-12-23

        Log.e(TAG, "[Normal] -- CameraProperties: end getCurrentStreamInfo bestResolution =" + bestResolution);
        return bestResolution;
    }

    public int getPreviewCacheTime() {
        Log.e(TAG, "[Normal] -- CameraProperties: start getPreviewCacheTime");
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getPreviewCacheTime();
        } catch (IchSocketException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchSocketException");

            e.printStackTrace();
        } catch (IchCameraModeException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchCameraModeException");

            e.printStackTrace();
        } catch (IchInvalidSessionException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchInvalidSessionException");

            e.printStackTrace();
        } catch (IchDevicePropException e) {
            Log.e(TAG, "[Error] -- CameraProperties: IchDevicePropException");

            e.printStackTrace();
        }
        Log.e(TAG, "[Normal] -- CameraProperties: end getPreviewCacheTime retValue =" + retValue);
        return retValue;
    }

    public int getCameraTimeLapseVideoSizeListMask() {
        int retValue = 0;
        try {
            retValue = cameraConfiguration.getCurrentPropertyValue(PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK);
        } catch (IchInvalidSessionException | IchSocketException | IchCameraModeException | IchDevicePropException e) {

            e.printStackTrace();
        }
        return retValue;
    }

}
