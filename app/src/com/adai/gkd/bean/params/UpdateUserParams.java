package com.adai.gkd.bean.params;

import java.io.File;
import java.io.Serializable;

public class UpdateUserParams implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String nickname;
	/**
	 * 头像
	 */
	public File image;
	/**
	 * 是否开放位置共享
	 */
	public int is_opened;
	/**
	 * 个性签名
	 */
	public String signature;
	/**
	 * 性别
	 */
	public String sex;
}
