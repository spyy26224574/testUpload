package com.adai.gkd.bean.params;

import java.io.Serializable;

public class UserResetPwdParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2597369217285847866L;

	public String email;
	/**
	 * 验证码
	 */
	public String code;
	
	public String password;
}
