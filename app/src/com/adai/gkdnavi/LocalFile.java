package com.adai.gkdnavi;

import java.io.Serializable;

public class LocalFile implements Comparable<LocalFile>,Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2L;
	String name;
	String path;
	String time;
	String size;

	public LocalFile(String name, String path, String time, String size) {
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
	public int compareTo(LocalFile another) {
		return -(this.getName().compareTo(another.getName()));
	}

}
