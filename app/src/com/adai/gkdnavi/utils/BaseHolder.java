package com.adai.gkdnavi.utils;

import android.view.View;

public abstract class BaseHolder<T> {

	protected View mRootView;
	protected T mData;

	public BaseHolder() {
		mRootView = initView();
	}

	/**
	 * 初始化view
	 * 
	 * @return
	 */
	protected abstract View initView();

	protected abstract void refreshUI(T data);

	public View getRootView() {
		return mRootView;
	}

	public void setData(T data) {
		this.mData = data;
		// 根据数据改变UI
		refreshUI(data);
	}

}
