package com.example.ipcamera.domain;
/**
 * @项目名称: GKDNavi
 * @包名: com.example.ipcamera.domain
 * @类名: MovieRecord.java
 * @作者: wyaz
 * @描述: 视频的分辨率
 * 
 */
public class MovieRecord {
	private String cmd;
	private String status;
	private String value;
	private String string;

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "MovieRecord{" +
				"cmd='" + cmd + '\'' +
				", status='" + status + '\'' +
				", value='" + value + '\'' +
				", string='" + string + '\'' +
				'}';
	}
}
