package com.adai.gkd.bean;

import java.io.Serializable;

public class PageInfoBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int totalCount;
	public int pageCount;
	public int currentPage;
	public int perPage;
}
