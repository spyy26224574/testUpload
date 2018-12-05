package com.adai.gkdnavi.utils;

import java.text.DecimalFormat;

/**
 * @author huangxy
 * @date 2018/10/24 9:34.
 */
public class FileSizeUtils {
    /**
     * 转换文件大小
     *
     * @param size
     * @return
     */
    public static String FormetFileSize(long size) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (size == 0) {
            return wrongSize;
        }
        if (size < 1024) {
            fileSizeString = df.format((double) size) + "B";
        } else if (size < 1048576) {
            fileSizeString = df.format((double) size / 1024) + "KB";
        } else if (size < 1073741824) {
            fileSizeString = df.format((double) size / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) size / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}
