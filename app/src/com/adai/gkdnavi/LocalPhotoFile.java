package com.adai.gkdnavi;

import java.io.Serializable;

public class LocalPhotoFile implements Comparable<LocalPhotoFile>,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String path;
	private String time;
	private String size;
	
	public LocalPhotoFile(String name, String path, String time, String size) {
		this.time = time;
		this.path = path;
		this.name = name;
		this.size = size;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	@Override
	public int compareTo(LocalPhotoFile another) {
		return -(this.getName().compareTo(another.getName()));
	}
	
}
