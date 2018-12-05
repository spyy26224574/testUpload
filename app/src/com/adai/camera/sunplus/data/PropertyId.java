package com.adai.camera.sunplus.data;

import com.icatch.wificam.customer.type.ICatchCameraProperty;

public class PropertyId {
	public final static int CAPTURE_DELAY = ICatchCameraProperty.ICH_CAP_CAPTURE_DELAY;
	public final static int BURST_NUMBER = ICatchCameraProperty.ICH_CAP_BURST_NUMBER;
	public final static int WHITE_BALANCE = ICatchCameraProperty.ICH_CAP_WHITE_BALANCE;
	public final static int LIGHT_FREQUENCY = ICatchCameraProperty.ICH_CAP_LIGHT_FREQUENCY;
	public final static int UP_SIDE = 0xd614;

	public final static int SLOW_MOTION = 0xd615;
	public final static int DATE_STAMP = ICatchCameraProperty.ICH_CAP_DATE_STAMP;
	public final static int IMAGE_SIZE = ICatchCameraProperty.ICH_CAP_IMAGE_SIZE;
	public final static int VIDEO_SIZE = ICatchCameraProperty.ICH_CAP_VIDEO_SIZE;
	public final static int ESSID_NAME = 0xd834;
	public final static int ESSID_PASSWORD = 0xd835;
	public final static int CAMERA_NAME = 0xd831;
	public final static int CAMERA_PASSWORD = 0xD83D;
	public final static int TIMELAPSE_MODE = 0xEE00;
	public final static int CAPTURE_DELAY_MODE = 0xD7F0;
	public final static int NOTIFY_FW_TO_SHARE_MODE = 0xD7FB;
	public final static int VIDEO_SIZE_FLOW = 0xD7FC;
	public final static int VIDEO_RECORDING_TIME = 0xD7FD;
	public final static int CAMERA_DATE = 0x5011;
	public final static int CAMERA_ESSID = 0xD83C;
	public final static int CAMERA_PASSWORD_NEW = 0xD832;
	public final static int SERVICE_ESSID = 0xD836;
	public final static int SERVICE_PASSWORD = 0xD837;
	public final static int CAMERA_CONNECT_CHANGE = 0xD7A1;
	// public static int WHITE_BALANCE =
	// ICatchCameraProperty.ICH_CAP_DATE_STAMP;
	// JIRA ICOM-2246
	public static final int TIMELAPSE_VIDEO_SIZE_LIST_MASK = 0xD7FB;
}
