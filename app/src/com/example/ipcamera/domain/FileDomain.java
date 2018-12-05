package com.example.ipcamera.domain;


import java.io.Serializable;
import java.util.Calendar;


public class FileDomain implements Serializable {

    private static final long serialVersionUID = -7094082608795551758L;
    public String baseUrl = "";
    public String name;
    private String smallname;
    public String fpath = "";
    private String smallpath = "";
    private String downloadPath = "";
    public long timeCode;
    public String time;
    public String upTime;
    public int attr;//33紧急视频
    //    private Bitmap bitmap;
    public boolean isPicture = true;
    public boolean isCheck = false;
    private String mThumbnailUrl = "";
    public long size;
    public int type;

    public String createTime;
    public int duration;
    public String cameraType = "0"; //0: front view video 1: rear view video
    public Calendar startTime;
    public Calendar endTime;

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }


    public void setDownloadPath(String downloadPath) {
        this.downloadPath = downloadPath;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        mThumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "FileDomain{" +
                "name='" + name + '\'' +
                ", smallname='" + smallname + '\'' +
                ", fpath='" + fpath + '\'' +
                ", smallpath='" + smallpath + '\'' +
                ", size=" + size +
                ", timeCode=" + timeCode +
                ", time='" + time + '\'' +
                ", attr=" + attr +
                ", isPicture=" + isPicture +
                ", isCheck=" + isCheck +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFpath() {
        return fpath;
    }

    public void setFpath(String fpath) {
        this.fpath = fpath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getTimeCode() {
        return timeCode;
    }

    public void setTimeCode(long timeCode) {
        this.timeCode = timeCode;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getCameraType() {
        return cameraType;
    }

    public void setCameraType(String cameraType) {
        this.cameraType = cameraType;
    }

    public int getAttr() {
        return attr;
    }

    public void setAttr(int attr) {
        this.attr = attr;
    }

    public String getSmallpath() {
        return smallpath;
    }

    public void setSmallpath(String smallpath) {
        this.smallpath = smallpath;
    }

    public String getSmallname() {
        return smallname;
    }

    public void setSmallname(String smallname) {
        this.smallname = smallname;
    }

}
