package com.adai.gkdnavi.utils;

public class DownLoadInfo {

	public String fileName;
	public String downloadUrl; // 下载的网络地址
	public String savePath; // 本地存放路径
	public int state = DownloadManager.STATE_NONE;
	public long currentProgress; // 当前进度
	public long size; // 应用程序的大小
	public Runnable task; // 下载的任务

	@Override
	public String toString() {
		return "DownloadInfo [fileName=" + fileName + ", downloadUrl="
				+ downloadUrl + ", savePath=" + savePath + ", state=" + state
				+ ", currentProgress=" + currentProgress + ", size=" + size
				+ ", task=" + task + "]";
	}
}
