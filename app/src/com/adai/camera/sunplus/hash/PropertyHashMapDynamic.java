package com.adai.camera.sunplus.hash;

import android.util.Log;

import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.bean.ItemInfo;
import com.adai.camera.sunplus.data.PropertyId;
import com.icatch.wificam.customer.ICatchWificamUtil;
import com.icatch.wificam.customer.exception.IchInvalidArgumentException;
import com.icatch.wificam.customer.type.ICatchImageSize;
import com.icatch.wificam.customer.type.ICatchVideoSize;

import java.util.HashMap;
import java.util.List;

public class PropertyHashMapDynamic {
    // public static HashMap<String, ItemInfo> imageSizeMap = new

    private static final String TAG = "PropertyHashMapDynamic";
    // HashMap<String, ItemInfo>();

    private static PropertyHashMapDynamic propertyHashMap;

    public static PropertyHashMapDynamic getInstance() {
        if (propertyHashMap == null) {
            propertyHashMap = new PropertyHashMapDynamic();
        }
        return propertyHashMap;
    }

    public HashMap<Integer, ItemInfo> getDynamicHashInt(int propertyId) {
        switch (propertyId) {
            case PropertyId.CAPTURE_DELAY:
                return getCaptureDelayMap();

            default:
                return null;
        }
    }

    public HashMap<String, ItemInfo> getDynamicHashString(int propertyId) {
        switch (propertyId) {
            case PropertyId.IMAGE_SIZE:
                return getImageSizeMap();
            case PropertyId.VIDEO_SIZE:
                return getVideoSizeMap();
            default:
                return null;
        }
    }
 
    private HashMap<Integer, ItemInfo> getCaptureDelayMap() {
        HashMap<Integer, ItemInfo> captureDelayMap = new HashMap<>();
        List<Integer> delyaList = CameraProperties.getInstance().getSupportedPropertyValues(PropertyId.CAPTURE_DELAY);
        String temp;
        for (int ii = 0; ii < delyaList.size(); ii++) {
            if (delyaList.get(ii) == 0) {
                temp = "OFF";
            } else {
                temp = delyaList.get(ii) / 1000 + "S";
            }
            Log.e(TAG, "delyaList.get(ii) ==" + delyaList.get(ii));
            captureDelayMap.put(delyaList.get(ii), new ItemInfo(temp, temp, 0));
        }
        return captureDelayMap;
    }

    private HashMap<String, ItemInfo> getImageSizeMap() {
        Log.e(TAG, "getImageSizeMap: [Normal] -- SDKReflectToUI: begin initImageSizeMap");
        HashMap<String, ItemInfo> imageSizeMap = new HashMap<String, ItemInfo>();
        List<String> imageSizeList = null;
        imageSizeList = CameraProperties.getInstance().getSupportedImageSizes();
        List<Integer> convertImageSizeList = null;
        try {
            convertImageSizeList = ICatchWificamUtil.convertImageSizes(imageSizeList);
        } catch (IchInvalidArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String temp = "Undefined";
        String temp1 = "Undefined";
        for (int ii = 0; ii < imageSizeList.size(); ii++) {
            if (convertImageSizeList.get(ii) == ICatchImageSize.ICH_IMAGE_SIZE_VGA) {
                temp = "VGA" + "(" + imageSizeList.get(ii) + ")";
                imageSizeMap.put(imageSizeList.get(ii), new ItemInfo(temp, "VGA", 0));
            } else {
                temp = convertImageSizeList.get(ii) + "M" + "(" + imageSizeList.get(ii) + ")";
                temp1 = convertImageSizeList.get(ii) + "M";
                imageSizeMap.put(imageSizeList.get(ii), new ItemInfo(temp, temp1, 0));
            }
            Log.e(TAG, "[Normal] -- SDKReflectToUI: imageSize =" + temp);
        }
        Log.e(TAG, "[Normal] -- SDKReflectToUI: end initImageSizeMap imageSizeMap =" + imageSizeMap.size());
        return imageSizeMap;
    }

    private HashMap<String, ItemInfo> getVideoSizeMap() {
        Log.e(TAG, "[Normal] -- SDKReflectToUI: begin initVideoSizeMap");
        HashMap<String, ItemInfo> videoSizeMap = new HashMap<>();
        List<String> videoSizeList = CameraProperties.getInstance().getSupportedVideoSizes();
        List<ICatchVideoSize> convertVideoSizeList = null;
        try {
            convertVideoSizeList = ICatchWificamUtil.convertVideoSizes(videoSizeList);
        } catch (IchInvalidArgumentException e) {
            e.printStackTrace();
        }
        // videoSizeArray = new String[convertVideoSizeList.size()];
        assert convertVideoSizeList != null;
        for (int ii = 0; ii < convertVideoSizeList.size(); ii++) {
            Log.e(TAG, "[Normal] -- SDKReflectToUI: videoSizeList_" + ii + " = " + videoSizeList.get(ii));
            if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_1080P_WITH_30FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1080", "30fpsFHD30", 0));
                // cs[1] = "FHD";
            } else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_1080P_WITH_60FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1080", "60fpsFHD60", 0));
                // cs[1] = "FHD";
            } else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_1440P_30FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1440", "30fps1440P", 0));
                // cs[1] = "FHD";
            } else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_720P_120FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x720", "120fpsHD120", 0));
                // cs[1] = "HD";
            } else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_720P_WITH_30FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x720", "30fpsHD30", 0));
                // cs[1] = "HD";
            } else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_720P_WITH_60FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x720", "60fpsHD60", 0));
                // cs[1] = "HD";
            } else if (videoSizeList.get(ii).equals("1280x720 50")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x720", "50fpsHD50", 0));
                // cs[1] = "HD";
            } else if (videoSizeList.get(ii).equals("1280x720 25")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x720", "25fpsHD25", 0));
                // cs[1] = "HD";
            }
            /*
             * else if (videoSizeList.get(ii).equals("1280x720 12")) {
			 * videoSizeMap.put(videoSizeList.get(ii), new
			 * ItemInfo("1280x720 12fpsHD12", 0)); }
			 */

            else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_960P_60FPS) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x960", "60fps960P", 0));
            } else if (videoSizeList.get(ii).equals("1280x960 120")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x960", "120fps960P", 0));
            } else if (videoSizeList.get(ii).equals("1280x960 30")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1280x960", "30fps960P", 0));
            }

//			else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_VGA_120FPS) {
//				videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x480 120fpsVGA120", 0));
//				// cs[1] = "VGA";
//			} else if (convertVideoSizeList.get(ii) == ICatchVideoSize.ICH_VIDEO_SIZE_640_360_240FPS) {
//				videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x480 240fpsVGA240", 0));
//				// cs[1] = "VGA";
//			}

            //start add by b.jiang 20160106
            else if (videoSizeList.get(ii).equals("640x480 240")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x480", "240fpsVGA240", 0));
            } else if (videoSizeList.get(ii).equals("640x480 120")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x480", "120fpsVGA120", 0));
            } else if (videoSizeList.get(ii).equals("640x360 240")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x360", "240fpsVGA240", 0));
            } else if (videoSizeList.get(ii).equals("640x360 120")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("640x360", "120fpsVGA120", 0));
            }

            //end add by b.jiang 20160106


            else if (videoSizeList.get(ii).equals("1920x1080 24")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1080", "24fpsFHD24", 0));
            } else if (videoSizeList.get(ii).equals("1920x1080 50")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1080", "50fpsFHD50", 0));
            } else if (videoSizeList.get(ii).equals("1920x1080 25")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("1920x1080", "25fpsFHD25", 0));
            }
            //Start add by b.jiang 2016-01-15
            else if (videoSizeList.get(ii).equals("3840x2160 60")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "60fps4K60", 0));

            } else if (videoSizeList.get(ii).equals("3840x2160 50")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "50fps4K50", 0));

            } else if (videoSizeList.get(ii).equals("3840x2160 25")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "25fps4K25", 0));

            } else if (videoSizeList.get(ii).equals("3840x2160 24")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "24fps4K24", 0));

            }
            //end add by b.jiang 2016-01-15

            else if (videoSizeList.get(ii).equals("3840x2160 30")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "30fps4K30", 0));

            } else if (videoSizeList.get(ii).equals("3840x2160 15")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160", "15fps4K15", 0));

            } else if (videoSizeList.get(ii).equals("3840x2160 10")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("3840x2160","10fps4K10", 0));

            }

            //ICOM-2442 Start add by b.jiang 2015-12-14
            else if (videoSizeList.get(ii).equals("2704x1524 30")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","30fps2.7K30", 0));
            } else if (videoSizeList.get(ii).equals("2704x1524 15")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","15fps2.7K15", 0));
            }
            //ICOM-2442 End add by b.jiang 2015-12-14

            //Start add by b.jiang 2016-01-15
            else if (videoSizeList.get(ii).equals("2704x1524 60")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","60fps2.7K60", 0));
            } else if (videoSizeList.get(ii).equals("2704x1524 50")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","50fps2.7K50", 0));
            } else if (videoSizeList.get(ii).equals("2704x1524 25")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","25fps2.7K25", 0));
            } else if (videoSizeList.get(ii).equals("2704x1524 24")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("2704x1524","24fps2.7K24", 0));
            }
            //end add by b.jiang 2016-01-15

            // ICOM-1902 Start add by zhangyanhu C01012 2015-8-17
            else if (videoSizeList.get(ii).equals("848x480 240")) {
                videoSizeMap.put(videoSizeList.get(ii), new ItemInfo("848x480","240fpsWVGA", 0));
            }
            // ICOM-1902 End add by zhangyanhu C01012 2015-8-17
        }
        Log.e(TAG, "[Normal] -- SDKReflectToUI: end initVideoSizeMap videoSizeList =" + videoSizeList.size());
        return videoSizeMap;
    }
}
