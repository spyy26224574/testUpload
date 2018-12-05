package com.alibaba.sdk.android.common.utils;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by huangxy on 2017/6/5 14:20.
 */

public class FileTypeUtil {
    private static final String videos[] = {"mp4", "avi", "mov", "wmv", "asf", "navi", "3gp", "mkv", "f4v", "rmvb", "webm","ts"};
    private static final String imgs[] = {"bmp", "jpg", "jpeg", "png", "tiff", "gif", "pcx", "tga", "exif", "fpx", "svg", "psd",
            "cdr", "pcd", "dxf", "ufo", "eps", "ai", "raw", "wmf"};
    private static final String documents[] = {"txt", "doc", "docx", "xls", "htm", "html", "jsp", "rtf", "wpd", "pdf", "ppt"};
    private static final String musics[] = {"mp3", "wma", "wav", "mod", "ra", "cd", "md", "asf", "aac", "vqf", "ape", "mid", "ogg",
            "m4a", "vqf"};
    public static final int TYPE_VIDEO = 1;
    public static final int TYPE_IMG = 2;
    public static final int TYPE_DOCUMENT = 3;
    public static final int TYPE_MUSIC = 4;
    public static final int TYPE_THUMBNAIL = 5;
    public static final int TYPE_RP_MOVIE = 6;
    public static final int TYPE_RP_PIC = 7;
    public static final int TYPE_OTHER = -1;


    public static int getFileType(@NonNull String fileName) {
        if (!TextUtils.isEmpty(fileName)) {
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            for (String video : videos) {
                if (video.equals(fileType)) {
                    return TYPE_VIDEO;
                }
            }
            for (String img : imgs) {
                if (img.equals(fileType)) {
                    return TYPE_IMG;
                }
            }
            for (String document : documents) {
                if (document.equals(fileType)) {
                    return TYPE_DOCUMENT;
                }
            }
            for (String music : musics) {
                if (music.equals(fileType)) {
                    return TYPE_MUSIC;
                }
            }
        }
        return TYPE_OTHER;
    }
}
