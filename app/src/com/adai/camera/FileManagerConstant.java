package com.adai.camera;

/**
 * Created by huangxy on 2017/8/28 11:35.
 */

public class FileManagerConstant {
    public static final int TYPE_LOCAL_PICTURE = 0x01 << 1;//下载图片
    public static final int TYPE_LOCAL_VIDEO = 0x01 << 2;//下载视频
    public static final int TYPE_REMOTE_NORMAL_VIDEO = 0x01 << 3;//普通视频
    public static final int TYPE_REMOTE_URGENCY_VIDEO = 0x01 << 4;//紧急视频
    public static final int TYPE_REMOTE_PHOTO = 0x01 << 5;//记录仪图片
    public static final int TYPE_REMOTE_MONITOR_VIDEO = 0x01 << 6;//停车视频
    public static final int TYPE_REMOTE_ALL = TYPE_REMOTE_NORMAL_VIDEO | TYPE_REMOTE_PHOTO | TYPE_REMOTE_URGENCY_VIDEO | TYPE_REMOTE_MONITOR_VIDEO;
    public static final int TYPE_REMOTE_VIDEO = TYPE_REMOTE_NORMAL_VIDEO | TYPE_REMOTE_URGENCY_VIDEO | TYPE_REMOTE_MONITOR_VIDEO;
    public static final String ACTION_EDIT_MODE_CHANGE = "com.adai.camera.FileManagerConstant.editModeChange";
    public static final String ACTION_SELECTED_FILE = "com.adai.camera.FileManagerConstant.selected_file";
}
