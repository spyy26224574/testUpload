package com.adai.camera.sunplus.bean;

import android.content.Context;
import android.content.res.Resources;

import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.data.PropertyId;
import com.adai.camera.sunplus.hash.PropertyHashMapDynamic;
import com.adai.gkdnavi.utils.LogUtils;
import com.icatch.wificam.customer.type.ICatchCameraProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PropertyTypeInteger {

    private HashMap<Integer, ItemInfo> hashMap;
    private int propertyId;
    private String[] valueListString;
    private List<Integer> valueListInt;
    private Context context;
    private Resources res;

    public PropertyTypeInteger(HashMap<Integer, ItemInfo> hashMap, int propertyId, Context context) {
        this.hashMap = hashMap;
        this.propertyId = propertyId;
        this.context = context;
        initItem();
    }

    public PropertyTypeInteger(int propertyId, Context context) {
        this.propertyId = propertyId;
        this.context = context;
        initItem();
    }

    public void initItem() {
        // TODO Auto-generated method stub
        if (hashMap == null) {
            hashMap = PropertyHashMapDynamic.getInstance().getDynamicHashInt(propertyId);
        }
        List<String> supportedStringPropertyValues = CameraProperties.getInstance().getSupportedStringPropertyValues(propertyId);
        LogUtils.e(supportedStringPropertyValues.toString());
        res = context.getResources();

        switch (propertyId) {
            case PropertyId.WHITE_BALANCE:
                valueListInt = CameraProperties.getInstance().getSupportedWhiteBalances();
                break;
            case PropertyId.CAPTURE_DELAY:
                valueListInt = CameraProperties.getInstance().getSupportedCaptureDelays();
                break;
            case PropertyId.BURST_NUMBER:

                valueListInt = CameraProperties.getInstance().getsupportedBurstNums();
                break;
            case PropertyId.LIGHT_FREQUENCY:
                valueListInt = CameraProperties.getInstance().getSupportedLightFrequencys();
                break;
            case PropertyId.DATE_STAMP:
                valueListInt = CameraProperties.getInstance().getsupportedDateStamps();
                break;
            case PropertyId.UP_SIDE:
                valueListInt = new ArrayList<Integer>();
                valueListInt.add(Upside.UPSIDE_OFF);
                valueListInt.add(Upside.UPSIDE_ON);
                break;
            case PropertyId.SLOW_MOTION:
                valueListInt = new ArrayList<Integer>();
                valueListInt.add(SlowMotion.SLOW_MOTION_OFF);
                valueListInt.add(SlowMotion.SLOW_MOTION_ON);
                break;
            case PropertyId.TIMELAPSE_MODE:
                valueListInt = new ArrayList<Integer>();
                valueListInt.add(TimeLapseMode.TIME_LAPSE_MODE_STILL);
                valueListInt.add(TimeLapseMode.TIME_LAPSE_MODE_VIDEO);
                break;
            default:
                valueListInt = CameraProperties.getInstance().getSupportedPropertyValues(propertyId);
                break;
        }
        valueListString = new String[valueListInt.size()];
        if (valueListInt != null) {
            for (int ii = 0; ii < valueListInt.size(); ii++) {
                if (propertyId == ICatchCameraProperty.ICH_CAP_CAPTURE_DELAY) {
                    ItemInfo itemInfo = hashMap.get(valueListInt.get(ii));
                    if (itemInfo != null) {
                        valueListString[ii] = itemInfo.uiStringInSettingString;
                    } else {
                        valueListString[ii] = "";
                    }
                } else {
                    ItemInfo itemInfo = hashMap.get(valueListInt.get(ii));
                    if (itemInfo != null) {
                        valueListString[ii] = res.getString(itemInfo.uiStringInSetting);
                    } else {
                        valueListString[ii] = "";
                    }
                }

            }
        }

    }

    public int getCurrentValue() {
        // TODO Auto-generated method stub
        int retValue;
        switch (propertyId) {
            case PropertyId.WHITE_BALANCE:
                retValue = CameraProperties.getInstance().getCurrentWhiteBalance();
                break;
            case PropertyId.CAPTURE_DELAY:
                retValue = CameraProperties.getInstance().getCurrentCaptureDelay();
                break;
            case PropertyId.BURST_NUMBER:
                retValue = CameraProperties.getInstance().getCurrentBurstNum();
                break;
            case PropertyId.LIGHT_FREQUENCY:
                retValue = CameraProperties.getInstance().getCurrentLightFrequency();
                break;
            case PropertyId.DATE_STAMP:
                retValue = CameraProperties.getInstance().getCurrentDateStamp();
                break;
            case PropertyId.UP_SIDE:
                retValue = CameraProperties.getInstance().getCurrentUpsideDown();
                break;
            case PropertyId.SLOW_MOTION:
                retValue = CameraProperties.getInstance().getCurrentSlowMotion();
                break;
            case PropertyId.TIMELAPSE_MODE:
                retValue = AppProperties.getInstanse().getTimeLapseMode();
                break;
            default:
                retValue = CameraProperties.getInstance().getCurrentPropertyValue(propertyId);
                break;
        }
        return retValue;
    }

    public String getCurrentUiStringInSetting() {
        // TODO Auto-generated method stub
        ItemInfo itemInfo = hashMap.get(getCurrentValue());
        String ret = null;
        if (itemInfo == null) {
            ret = "";
        } else {
            ret = res.getString(itemInfo.uiStringInSetting);
        }

        return ret;
    }

    public String getCurrentUiStringInPreview() {
        ItemInfo itemInfo = hashMap.get(getCurrentValue());
        String ret = null;
        if (itemInfo == null) {
            ret = "";
        } else {
            ret = itemInfo.uiStringInPreview;
        }
        // TODO Auto-generated method stub
        return ret;
    }

    public String getCurrentUiStringInSetting(int position) {
        // TODO Auto-generated method stub
        return valueListString[position];
    }

    public int getCurrentIcon() {
        // TODO Auto-generated method stub
        return hashMap.get(getCurrentValue()).iconID;
    }

    public String[] getValueList() {
        // TODO Auto-generated method stub
        return valueListString;
    }

    public Boolean setValue(int value) {
        // TODO Auto-generated method stub
        boolean retValue;
        switch (propertyId) {
            case PropertyId.WHITE_BALANCE:
                retValue = CameraProperties.getInstance().setWhiteBalance(value);
                break;
            case PropertyId.CAPTURE_DELAY:
                retValue = CameraProperties.getInstance().setCaptureDelay(value);
                break;
            case PropertyId.BURST_NUMBER:
                retValue = CameraProperties.getInstance().setCurrentBurst(value);
                break;
            case PropertyId.LIGHT_FREQUENCY:
                retValue = CameraProperties.getInstance().setLightFrequency(value);
                break;
            case PropertyId.DATE_STAMP:
                retValue = CameraProperties.getInstance().setDateStamp(value);
                break;
            default:
                retValue = CameraProperties.getInstance().setPropertyValue(propertyId, value);
                break;
        }
        return retValue;
    }

    public Boolean setValueByPosition(int position) {
        // TODO Auto-generated method stub

        boolean retValue;
        switch (propertyId) {
            case PropertyId.WHITE_BALANCE:
                retValue = CameraProperties.getInstance().setWhiteBalance(valueListInt.get(position));
                break;
            case PropertyId.CAPTURE_DELAY:
                retValue = CameraProperties.getInstance().setCaptureDelay(valueListInt.get(position));
                break;
            case PropertyId.BURST_NUMBER:
                retValue = CameraProperties.getInstance().setCurrentBurst(valueListInt.get(position));
                break;
            case PropertyId.LIGHT_FREQUENCY:
                retValue = CameraProperties.getInstance().setLightFrequency(valueListInt.get(position));
                break;
            case PropertyId.DATE_STAMP:
                retValue = CameraProperties.getInstance().setDateStamp(valueListInt.get(position));
                break;
            case PropertyId.UP_SIDE:
                retValue = CameraProperties.getInstance().setUpsideDown(valueListInt.get(position));
                break;
            case PropertyId.SLOW_MOTION:
                retValue = CameraProperties.getInstance().setSlowMotion(valueListInt.get(position));
                break;
            default:
                retValue = CameraProperties.getInstance().setPropertyValue(propertyId, valueListInt.get(position));
                break;
        }
        return retValue;
    }

}
