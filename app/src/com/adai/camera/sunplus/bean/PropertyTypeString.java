package com.adai.camera.sunplus.bean;

import android.content.Context;
import android.util.Log;

import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.data.PropertyId;
import com.adai.camera.sunplus.hash.PropertyHashMapDynamic;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class PropertyTypeString {

	private static final String TAG = "PropertyTypeString";
	private int propertyId;
	private List<String> valueListString;
	private List<String> valueListStringUI;
	private HashMap<String, ItemInfo> hashMap;
	private String[] valueArrayString;

	public PropertyTypeString(int propertyId, Context context) {
		this.propertyId = propertyId;
		initItem();
	}
	//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
	private Boolean checkWithTimeLapseMask(int TimeLapseMaskValue, int index) {
		int intShiftValue = 0x1;
		int intMatchValue = 0 ;
		Log.e(TAG, "checkWithTimeLapseMask: [Normal] -- PropertyTypeString: start ceckWithTimeLapseMask");
		Log.e(TAG,"[Normal] -- PropertyTypeString: TimeLapseMaskValue = " + TimeLapseMaskValue);
		Log.e(TAG,"[Normal] -- PropertyTypeString: index = " + index);
		if ( index > 0) {
			intShiftValue = intShiftValue << index;			
		}  
		Log.e(TAG,"[Normal] -- PropertyTypeString: intShiftValue = " + intShiftValue);
		intMatchValue = TimeLapseMaskValue & intShiftValue; 
		Log.e(TAG,"[Normal] -- PropertyTypeString: intMatchValue = " + intMatchValue);
		if (intMatchValue > 0) {			
			Log.e(TAG,"[Normal] -- PropertyTypeString: end ceckWithTimeLapseMask = true ");
			return true;
		} else {
			Log.e(TAG,"[Normal] -- PropertyTypeString: end ceckWithTimeLapseMask = faluse ");
			return false;
		}		
	}
	//JIRA ICOM-2246 End Add by b.jiang 2015-12-04
	public void initItem() {
		if (hashMap == null) {
			//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
			if (propertyId == PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) {
				Log.e(TAG,"[Normal] -- PropertyTypeString: start initItem in PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK");
				hashMap = PropertyHashMapDynamic.getInstance().getDynamicHashString(PropertyId.VIDEO_SIZE);
			} else {
				Log.e(TAG,"[Normal] -- PropertyTypeString: start initItem in " + propertyId);				
				hashMap = PropertyHashMapDynamic.getInstance().getDynamicHashString(propertyId);	
			} 
			
			//JIRA ICOM-2246 End Add by b.jiang 2015-12-04
			
			
			//hashMap = PropertyHashMapDynamic.getInstance().getDynamicHashString(propertyId);
		}
		if (propertyId == PropertyId.IMAGE_SIZE) {
			valueListString = CameraProperties.getInstance().getSupportedImageSizes();
		}
		if (propertyId == PropertyId.VIDEO_SIZE) {
			valueListString = CameraProperties.getInstance().getSupportedVideoSizes();
		}
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
		if (propertyId == PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) {
			valueListString = CameraProperties.getInstance().getSupportedVideoSizes();
			Log.e(TAG,"[Normal] -- PropertyTypeString - initItem :  before valueListString.size = " + valueListString.size());
			Log.d("TigerTiger" ,"before valueListString.size = " + valueListString.size());
			// Check support property : TIMELAPSE_VIDEO_SIZE_LIST_MASK
			int intTimeLapseVideoSizeListMask = 0;
			if (CameraProperties.getInstance().hasFuction(PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) == true) {
				intTimeLapseVideoSizeListMask = CameraProperties.getInstance().getCameraTimeLapseVideoSizeListMask();
				Log.e(TAG,"[Normal] -- PropertyTypeString - initItem :  getCameraTimeLapseVideoSizeListMask = " + intTimeLapseVideoSizeListMask);
				for (int ii = 0; ii < valueListString.size(); ii++) {
					if (checkWithTimeLapseMask(intTimeLapseVideoSizeListMask, ii) == false) {
						valueListString.remove(ii);
						ii--;
						intTimeLapseVideoSizeListMask = intTimeLapseVideoSizeListMask >> 1 ;
					} 
				}		
			}			
		}
		
		//JIRA ICOM-2246 End Add by b.jiang 2015-12-04	
		Log.e(TAG,"[Normal] -- PropertyTypeString - initItem :  after valueListString.size = " + valueListString.size());
		Log.d("TigerTiger" ,"after valueListString.size = " + valueListString.size());
		
		for (int ii = 0; ii < valueListString.size(); ii++) {
			if (hashMap.containsKey(valueListString.get(ii)) == false) {
				valueListString.remove(ii);
				ii--;
			}
		}
		valueListStringUI = new LinkedList<String>();
		valueArrayString = new String[valueListString.size()];
		if (valueListString != null) {
			for (int ii = 0; ii < valueListString.size(); ii++) {
				valueListStringUI.add(ii, hashMap.get(valueListString.get(ii)).uiStringInSettingString);
				valueArrayString[ii] = hashMap.get(valueListString.get(ii)).uiStringInSettingString;
			}
		}

	}

	public String getCurrentValue() {
		
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
		if (propertyId == PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) {
			return CameraProperties.getInstance().getCurrentStringPropertyValue( PropertyId.VIDEO_SIZE);
		} else { 
			return CameraProperties.getInstance().getCurrentStringPropertyValue( propertyId);
		}
		//return CameraProperties.getInstance().getCurrentStringPropertyValue(propertyId);
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
		
	}

	public String getCurrentUiStringInSetting() {
		ItemInfo itemInfo = hashMap.get(getCurrentValue());
		String ret = null;
		if (itemInfo == null) {
			ret = "";
		} else {
			ret = itemInfo.uiStringInSettingString;
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
		return ret;
	}

	public String getCurrentUiStringInSetting(int position) {
		
		return valueListString.get(position);
	}

	public List<String> getValueList() {
		
		return valueListString;
	}

	public List<String> getValueListUI() {
		
		return valueListString;
	}

	public Boolean setValue(String value) {
		
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
		if (propertyId == PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) {
			return CameraProperties.getInstance().setStringPropertyValue( PropertyId.VIDEO_SIZE, value);
		} else { 
			return CameraProperties.getInstance().setStringPropertyValue( propertyId, value);	
		}
		//return CameraProperties.getInstance().setStringPropertyValue( propertyId, value);
		//JIRA ICOM-2246 End Add by b.jiang 2015-12-04
		
	}

	public boolean setValueByPosition(int position) {
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04
		if (propertyId == PropertyId.TIMELAPSE_VIDEO_SIZE_LIST_MASK) {
			return CameraProperties.getInstance().setStringPropertyValue( PropertyId.VIDEO_SIZE,
					valueListString.get(position));
		} else {
			return CameraProperties.getInstance().setStringPropertyValue( propertyId,
					valueListString.get(position));	
		}
		//return CameraProperties.getInstance().setStringPropertyValue( propertyId,valueListString.get(position));
		//JIRA ICOM-2246 Begin Add by b.jiang 2015-12-04

		
	}

	public String[] getValueArrayString() {
		return valueArrayString;
	}
}
