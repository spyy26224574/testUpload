package com.adai.gkdnavi.utils;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class SuperBaseAdapter<T> extends BaseAdapter {
	private static final String TAG = "SuperBaseAdapter";
	private List<T> mDatas;

	public SuperBaseAdapter(List<T> datas) {
		this.mDatas = datas;
	}

	@Override
	public int getCount() {
		if (mDatas != null) {
			return mDatas.size();
		}

		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mDatas != null) {
			return mDatas.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// ###1. 加载View ##########################
		BaseHolder<T> holder = null;
		if (convertView == null) {
			// 1. 创建holder
			holder = getItemHolder(position);// 去实现某一个holders
			// 2. 加载布局（TODO:
			convertView = holder.getRootView();
			// 3. 设置标记
			convertView.setTag(holder);
		} else {
			// 复用
			holder = (BaseHolder<T>) convertView.getTag();
		}
		// #######2.给view加载数据###################
		// 获取数据
		T data = mDatas.get(position);
		// 给view设置数据
		holder.setData(data);
		return convertView;
	}

	protected abstract BaseHolder<T> getItemHolder(int position);

}
