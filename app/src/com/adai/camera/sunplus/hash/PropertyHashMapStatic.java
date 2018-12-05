package com.adai.camera.sunplus.hash;

import android.annotation.SuppressLint;
import android.util.Log;

import com.adai.camera.sunplus.bean.ItemInfo;
import com.adai.camera.sunplus.bean.SlowMotion;
import com.adai.camera.sunplus.bean.TimeLapseDuration;
import com.adai.camera.sunplus.bean.TimeLapseMode;
import com.adai.camera.sunplus.bean.Upside;
import com.adai.gkdnavi.R;
import com.icatch.wificam.customer.type.ICatchBurstNumber;
import com.icatch.wificam.customer.type.ICatchDateStamp;
import com.icatch.wificam.customer.type.ICatchLightFrequency;
import com.icatch.wificam.customer.type.ICatchWhiteBalance;

import java.util.HashMap;

@SuppressLint("UseSparseArrays")
public class PropertyHashMapStatic {
    private static final String TAG = "PropertyHashMapStatic";
    public static HashMap<Integer, ItemInfo> burstMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> whiteBalanceMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> electricityFrequencyMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> dateStampMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> timeLapseMode = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> timeLapseIntervalMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> timeLapseDurationMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> slowMotionMap = new HashMap<Integer, ItemInfo>();
    public static HashMap<Integer, ItemInfo> upsideMap = new HashMap<Integer, ItemInfo>();
    public static PropertyHashMapStatic propertyHashMap;

    public static PropertyHashMapStatic getInstance() {
        if (propertyHashMap == null) {
            propertyHashMap = new PropertyHashMapStatic();
        }
        return propertyHashMap;
    }

    public void initPropertyHashMap() {
        Log.e(TAG, "initPropertyHashMap:[Normal] -- PropertyHashMapStatic:Start initPropertyHashMap");
        initWhiteBalanceMap();
        initTimeLapseDuration();
        initSlowMotion();
        initUpside();
        initBurstMap();
        initElectricityFrequencyMap();
        initDateStampMap();
        ininTimeLapseMode();

        Log.e(TAG, "[Normal] -- PropertyHashMapStatic:End initPropertyHashMap");
    }

    private void ininTimeLapseMode() {
        timeLapseMode.put(TimeLapseMode.TIME_LAPSE_MODE_STILL, new ItemInfo(R.string.timeLapse_capture_mode, null, 0));
        timeLapseMode.put(TimeLapseMode.TIME_LAPSE_MODE_VIDEO, new ItemInfo(R.string.timeLapse_video_mode, null, 0));

    }

    public void initWhiteBalanceMap() {
        whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_AUTO, new ItemInfo(R.string.auto, null, R.drawable.wb_auto));
        whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_CLOUDY, new ItemInfo(R.string.wb_cloudy, null, R.drawable.wb_cloudy));
        whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_DAYLIGHT, new ItemInfo(R.string.daylight, null, R.drawable.wb_sunlight));
        whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_FLUORESCENT, new ItemInfo(R.string.fluoresent, null, R.drawable.wb_florescence));
        whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_TUNGSTEN, new ItemInfo(R.string.tungsten, null, R.drawable.wb_tungsten)); // whiteBalanceMap.put(ICatchWhiteBalance.ICH_WB_UNDEFINED,
    }

    private void initTimeLapseDuration() {
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_2MIN, new ItemInfo(R.string.setting_time_lapse_duration_2M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_5MIN, new ItemInfo(R.string.setting_time_lapse_duration_5M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_10MIN, new ItemInfo(R.string.setting_time_lapse_duration_10M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_15MIN, new ItemInfo(R.string.setting_time_lapse_duration_15M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_20MIN, new ItemInfo(R.string.setting_time_lapse_duration_20M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_30MIN, new ItemInfo(R.string.setting_time_lapse_duration_30M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_60MIN, new ItemInfo(R.string.setting_time_lapse_duration_60M, null, 0));
        timeLapseIntervalMap.put(TimeLapseDuration.TIME_LAPSE_DURATION_UNLIMITED, new ItemInfo(R.string.setting_time_lapse_duration_unlimit, null, 0));
    }

    private void initSlowMotion() {
        slowMotionMap.put(SlowMotion.SLOW_MOTION_OFF, new ItemInfo(R.string.close, null, 0));
        slowMotionMap.put(SlowMotion.SLOW_MOTION_ON, new ItemInfo(R.string.open, null, 0));
    }

    private void initUpside() {
        upsideMap.put(Upside.UPSIDE_OFF, new ItemInfo(R.string.close, null, 0));
        upsideMap.put(Upside.UPSIDE_ON, new ItemInfo(R.string.open, null, 0));
    }

    public void initBurstMap() {
        burstMap.put(ICatchBurstNumber.ICH_BURST_NUMBER_OFF, new ItemInfo(R.string.burst_off, null, 0));
        burstMap.put(ICatchBurstNumber.ICH_BURST_NUMBER_3, new ItemInfo(R.string.burst_3, null, 0));
        burstMap.put(ICatchBurstNumber.ICH_BURST_NUMBER_5, new ItemInfo(R.string.burst_5, null, 0));
        burstMap.put(ICatchBurstNumber.ICH_BURST_NUMBER_10, new ItemInfo(R.string.burst_10, null, 0));
        burstMap.put(ICatchBurstNumber.ICH_BURST_NUMBER_HS, new ItemInfo(R.string.burst_hs, null, 0));
    }

    public void initElectricityFrequencyMap() {
        electricityFrequencyMap.put(ICatchLightFrequency.ICH_LIGHT_FREQUENCY_50HZ, new ItemInfo(R.string.frequency_50HZ, null, 0));
        electricityFrequencyMap.put(ICatchLightFrequency.ICH_LIGHT_FREQUENCY_60HZ, new ItemInfo(R.string.frequency_60HZ, null, 0));
    }

    public void initDateStampMap() {
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_OFF, new ItemInfo(R.string.dateStamp_off, null, 0));
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_DATE, new ItemInfo(R.string.dateStamp_date, null, 0));
        dateStampMap.put(ICatchDateStamp.ICH_DATE_STAMP_DATE_TIME, new ItemInfo(R.string.dateStamp_date_and_time, null, 0));
    }
}
