package com.adai.gkd.bean;

import java.io.Serializable;

public class MessageBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int id;
	/**
	 * 1小力拍照，2异常信息报警，3推送消息
	 */
	public int type;
	public String content;
	public String deviceid;
	public String starttime;
	public String endtime;
	public String url;
	/**
	 * 0未读，1已读
	 */
	public int isread=0;
	public String title;
	public String createtime;
	public String errorcode;
}
