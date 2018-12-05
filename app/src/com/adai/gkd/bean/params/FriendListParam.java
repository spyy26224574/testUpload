package com.adai.gkd.bean.params;

import java.io.Serializable;
/**
 * 获取好友列表参数类
 * @author admin
 *
 */
public class FriendListParam implements Serializable {

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
