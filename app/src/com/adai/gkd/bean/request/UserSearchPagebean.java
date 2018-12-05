package com.adai.gkd.bean.request;

import java.util.List;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.UserInfoBean;
/**
 * 查找用户结果实体
 * @author admin
 *
 */
public class UserSearchPagebean extends BasePageBean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5753106448162524236L;

	/**
	 * 搜索结果
	 */
	public List<UserInfoBean> data;
}
