package com.adai.camera.product;

import com.adai.camera.sunplus.SDKAPI.SunplusSession;
import com.adai.camera.sunplus.bean.PropertyTypeInteger;
import com.adai.camera.sunplus.bean.PropertyTypeString;
import com.adai.camera.sunplus.bean.StreamResolution;
import com.adai.camera.sunplus.bean.TimeLapseDuration;
import com.adai.camera.sunplus.bean.TimeLapseInterval;
import com.icatch.wificam.customer.ICatchWificamAssist;
import com.icatch.wificam.customer.ICatchWificamControl;
import com.icatch.wificam.customer.ICatchWificamInfo;
import com.icatch.wificam.customer.ICatchWificamPlayback;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.ICatchWificamProperty;
import com.icatch.wificam.customer.ICatchWificamState;
import com.icatch.wificam.customer.ICatchWificamVideoPlayback;

/**
 * Created by huangxy on 2017/3/27.
 */

public interface ISunplusCamera {

    boolean prepareSession();

    boolean destroySession();

    boolean initCamera();

    boolean deInitCamera();

    SunplusSession getSunplusSession();

    ICatchWificamPreview getPreviewStreamClient();

    ICatchWificamProperty getCameraPropertyClint();

    ICatchWificamControl getCameraActionClient();

    ICatchWificamAssist getCameraAssistClint();

    ICatchWificamInfo getCameraInfoClint();

    ICatchWificamState getCameraStateClint();

    ICatchWificamPlayback getPlaybackClient();

    ICatchWificamVideoPlayback getVideoPlaybackClint();

    PropertyTypeInteger getWhiteBalance();

    PropertyTypeInteger getBurst();

    PropertyTypeInteger getElectricityFrequency();

    PropertyTypeInteger getDateStamp();

    PropertyTypeInteger getSlowMotion();

    PropertyTypeInteger getUpside();

    PropertyTypeInteger getCaptureDelay();

    PropertyTypeString getVideoSize();

    PropertyTypeString getImageSize();

    StreamResolution getStreamResolution();

    TimeLapseInterval getTimeLapseInterval();

    TimeLapseDuration getTimeLapseDuration();

    PropertyTypeInteger getTimeLapseMode();

    void resetTimeLapseVideoSize();

    void resetVideoSize();

    void _initMenuItem();//测试用
}
