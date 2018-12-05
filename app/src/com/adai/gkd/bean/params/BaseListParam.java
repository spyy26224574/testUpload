package com.adai.gkd.bean.params;

import java.io.Serializable;

public class BaseListParam implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 页码
	 */
	public int page;
	/**
	 * 分页大小
	 */
	public int per_page;
}
