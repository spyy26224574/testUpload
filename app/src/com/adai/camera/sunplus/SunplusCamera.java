package com.adai.camera.sunplus;

import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.CameraAction;
import com.adai.camera.sunplus.SDKAPI.CameraFixedInfo;
import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.SDKAPI.CameraState;
import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.SDKAPI.SunplusSession;
import com.adai.camera.sunplus.SDKAPI.VideoPlayback;
import com.adai.camera.sunplus.bean.PropertyTypeInteger;
import com.adai.camera.sunplus.bean.PropertyTypeString;
import com.adai.camera.sunplus.bean.StreamResolution;
import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.bean.TimeLapseDuration;
import com.adai.camera.sunplus.bean.TimeLapseInterval;
import com.adai.camera.sunplus.data.PropertyId;
import com.adai.camera.sunplus.hash.PropertyHashMapStatic;
import com.adai.gkdnavi.utils.UIUtils;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.ICatchWificamSession;
import com.icatch.wificam.customer.ICatchWificamState;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;
import com.icatch.wificam.customer.exception.IchInvalidSessionException;
import com.icatch.wificam.customer.type.ICatchCameraProperty;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by huangxy on 2017/4/5 20:00.
 */

public class SunplusCamera implements ISunplusCamera {
    private SunplusSession mSunplusSession;
    private ICatchWificamPlayback photoPlayback;
    private ICatchWificamControl cameraAction;
    private ICatchWificamVideoPlayback videoPlayback;
    private ICatchWificamPreview previewStream;
    private ICatchWificamInfo cameraInfo;
    private ICatchWificamProperty cameraProperty;
    private ICatchWificamState cameraState;
    private ICatchWificamAssist cameraAssist;

    private PropertyTypeInteger whiteBalance, burst, electricityFrequency, dateStamp, slowMotion, upside, captureDelay;
    private PropertyTypeString videoSize, imageSize;
    private StreamResolution streamResolution;
    private TimeLapseInterval timeLapseInterval;
    private TimeLapseDuration timeLapseDuration;
    private PropertyTypeInteger timeLapseMode;
    public List<SunplusMinuteFile> mSelectedMinuteFile = new ArrayList<>();

    public SunplusCamera() {
        mSunplusSession = new SunplusSession();
    }

    @Override
    public boolean prepareSession() {
        return mSunplusSession.prepareSession();
    }

    @Override
    public boolean destroySession() {
        return mSunplusSession.destroySession();
    }

    @Override
    public boolean initCamera() {
        boolean retValue = false;
        try {
            ICatchWificamSession sdkSession = mSunplusSession.getSDKSession();
            if (sdkSession == null) {
                return false;
            }
            photoPlayback = sdkSession.getPlaybackClient();
            cameraAction = mSunplusSession.getSDKSession().getControlClient();
            previewStream = mSunplusSession.getSDKSession().getPreviewClient();
            videoPlayback = mSunplusSession.getSDKSession().getVideoPlaybackClient();
            cameraProperty = mSunplusSession.getSDKSession().getPropertyClient();
            cameraInfo = mSunplusSession.getSDKSession().getInfoClient();
            cameraState = mSunplusSession.getSDKSession().getStateClient();
            cameraAssist = ICatchWificamAssist.getInstance();
            retValue = true;
        } catch (IchInvalidSessionException e) {
            e.printStackTrace();
        }
        CameraAction.getInstance().initCameraAction();
        CameraFixedInfo.getInstance().initCameraFixedInfo();
        CameraProperties.getInstance().initCameraProperties();
        CameraState.getInstance().initCameraState();
        FileOperation.getInstance().initICatchWificamPlayback();
        VideoPlayback.getInstance().initVideoPlayback();
        PropertyHashMapStatic.getInstance().initPropertyHashMap();
        initProperty();
//		uid = mSDKSession.getUId();
        //同步日期
        if (CameraProperties.getInstance().hasFuction(PropertyId.CAMERA_DATE)) {
            long time = System.currentTimeMillis();
            Date date = new Date(time);
            SimpleDateFormat myFmt = new SimpleDateFormat("yyyyMMdd HHmmss");
            String temp = myFmt.format(date);
            temp = temp.replaceAll(" ", "T");
            temp = temp + ".0";
            CameraProperties.getInstance().setCameraDate(temp);
        }
        return retValue;
    }

    @Override
    public boolean deInitCamera() {
        return destroySession();
    }

    @Override
    public SunplusSession getSunplusSession() {
        return mSunplusSession;
    }

    private void initProperty() {
        whiteBalance = new PropertyTypeInteger(PropertyHashMapStatic.whiteBalanceMap, PropertyId.WHITE_BALANCE, UIUtils.getContext());
        burst = new PropertyTypeInteger(PropertyHashMapStatic.burstMap, ICatchCameraProperty.ICH_CAP_BURST_NUMBER, UIUtils.getContext());
        dateStamp = new PropertyTypeInteger(PropertyHashMapStatic.dateStampMap, PropertyId.DATE_STAMP, UIUtils.getContext());
        slowMotion = new PropertyTypeInteger(PropertyHashMapStatic.slowMotionMap, PropertyId.SLOW_MOTION, UIUtils.getContext());
        upside = new PropertyTypeInteger(PropertyHashMapStatic.upsideMap, PropertyId.UP_SIDE, UIUtils.getContext());

        electricityFrequency = new PropertyTypeInteger(PropertyHashMapStatic.electricityFrequencyMap, PropertyId.LIGHT_FREQUENCY,
                UIUtils.getContext());

        captureDelay = new PropertyTypeInteger(PropertyId.CAPTURE_DELAY, UIUtils.getContext());
        videoSize = new PropertyTypeString(PropertyId.VIDEO_SIZE, UIUtils.getContext());
        imageSize = new PropertyTypeString(PropertyId.IMAGE_SIZE, UIUtils.getContext());
        streamResolution = new StreamResolution();
        timeLapseInterval = new TimeLapseInterval();
        timeLapseDuration = new TimeLapseDuration();
        timeLapseMode = new PropertyTypeInteger(PropertyHashMapStatic.timeLapseMode, PropertyId.TIMELAPSE_MODE, UIUtils.getContext());
    }

    @Override
    public ICatchWificamPreview getPreviewStreamClient() {
        return previewStream;
    }

    @Override
    public ICatchWificamProperty getCameraPropertyClint() {
        return cameraProperty;
    }

    @Override
    public ICatchWificamControl getCameraActionClient() {
        return cameraAction;
    }

    @Override
    public ICatchWificamAssist getCameraAssistClint() {
        return cameraAssist;
    }

    @Override
    public ICatchWificamInfo getCameraInfoClint() {
        return cameraInfo;
    }

    @Override
    public ICatchWificamState getCameraStateClint() {
        return cameraState;
    }

    @Override
    public ICatchWificamPlayback getPlaybackClient() {
        return photoPlayback;
    }

    @Override
    public ICatchWificamVideoPlayback getVideoPlaybackClint() {
        return videoPlayback;
    }

    @Override
    public PropertyTypeInteger getWhiteBalance() {
        return whiteBalance;
    }

    @Override
    public PropertyTypeInteger getBurst() {
        return burst;
    }

    @Override
    public PropertyTypeInteger getElectricityFrequency() {
        return electricityFrequency;
    }

    @Override
    public PropertyTypeInteger getDateStamp() {
        return dateStamp;
    }

    @Override
    public PropertyTypeInteger getSlowMotion() {
        return slowMotion;
    }

    @Override
    public PropertyTypeInteger getUpside() {
        return upside;
    }

    @Override
    public PropertyTypeInteger getCaptureDelay() {
        return captureDelay;
    }

    @Override
    public PropertyTypeString getVideoSize() {
        return videoSize;
    }

    @Override
    public PropertyTypeString getImageSize() {
        return imageSize;
    }

    @Override
    public StreamResolution getStreamResolution() {
        return streamResolution;
    }

    @Override
    public TimeLapseInterval getTimeLapseInterval() {
        return timeLapseInterval;
    }

    @Override
    public TimeLapseDuration getTimeLapseDuration() {
        return timeLapseDuration;
    }

    @Override
    public PropertyTypeInteger getTimeLapseMode() {
        return timeLapseMode;
    }

    @Override
    public void resetTimeLapseVideoSize() {
        videoSize = new PropertyTypeString(PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK, UIUtils.getContext());
    }

    @Override
    public void resetVideoSize() {
        videoSize = new PropertyTypeString(PropertyId.VIDEO_SIZE, UIUtils.getContext());
    }

    @Override
    public void _initMenuItem() {
        //测试用
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_BATTERY_LEVEL);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_MOVIE_REC);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_WHITE_BALANCE);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_CAPTURE_DELAY);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_IMAGE_SIZE);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_VIDEO_SIZE);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_LIGHT_FREQUENCY);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_BATTERY_LEVEL);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_PRODUCT_NAME);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_FW_VERSION);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_BURST_NUMBER);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_DATE_STAMP);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_UPSIDE_DOWN);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_SLOW_MOTION);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_DIGITAL_ZOOM);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_TIMELAPSE_STILL);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_TIMELAPSE_VIDEO);
        CameraProperties.getInstance().getSupportedStringPropertyValues(ICatchCameraProperty.ICH_CAP_UNDEFINED);
    }
}
