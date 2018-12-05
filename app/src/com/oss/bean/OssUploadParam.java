package com.oss.bean;

import com.adai.gkd.contacts.CurrentUserInfo;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by huangxy on 2017/6/5 15:36.
 */

public class OssUploadParam {
    public long fileSize;
    /**
     * 本地文件路径
     */
    public String uploadFilePath;
    /**
     * 部分objectKey
     */
    public String partObject;
    /**
     * 文件类型
     */
    private int fileType;
    private Calendar mCalendar;
    private SimpleDateFormat mSimpleDateFormat;
    private SimpleDateFormat mSimpleDateFormat1;

    public OssUploadParam(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
        mCalendar = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat("yyyyMMdd", Locale.SIMPLIFIED_CHINESE);
        mSimpleDateFormat1 = new SimpleDateFormat("HHmmss", Locale.SIMPLIFIED_CHINESE);
        fileType = FileTypeUtil.getFileType(uploadFilePath);
        makePartObjectByType(fileType);
    }

    public int getFileType() {
        return fileType;
    }

    /**
     * 针对缩略图写的方法，缩略图类型{@link FileTypeUtil#TYPE_THUMBNAIL}
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
        makePartObjectByType(fileType);
    }

    private void makePartObjectByType(int fileType) {
        switch (fileType) {
            case FileTypeUtil.TYPE_IMG:
                partObject = CurrentUserInfo.username + "/" + "pic/" + mSimpleDateFormat.format(mCalendar.getTime()) + "/" + mSimpleDateFormat1.format(mCalendar.getTime()) + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                break;
            case FileTypeUtil.TYPE_VIDEO:
                partObject = CurrentUserInfo.username + "/" + "move/" + mSimpleDateFormat.format(mCalendar.getTime()) + "/" + mSimpleDateFormat1.format(mCalendar.getTime()) + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                break;
            case FileTypeUtil.TYPE_THUMBNAIL:
                partObject = CurrentUserInfo.username + "/" + "thumbnail/" + mSimpleDateFormat.format(mCalendar.getTime()) + "/" + mSimpleDateFormat1.format(mCalendar.getTime()) + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                break;
            case FileTypeUtil.TYPE_RP_MOVIE:
                partObject = CurrentUserInfo.username + "/" + "rp_movie/" + mSimpleDateFormat.format(mCalendar.getTime()) + "/" + mSimpleDateFormat1.format(mCalendar.getTime()) + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                break;
            case FileTypeUtil.TYPE_RP_PIC:
                partObject = CurrentUserInfo.username + "/" + "rp_pic/" + mSimpleDateFormat.format(mCalendar.getTime()) + "/" + mSimpleDateFormat1.format(mCalendar.getTime()) + uploadFilePath.substring(uploadFilePath.lastIndexOf("/") + 1);
                break;
            default:
                break;
        }
    }
}
