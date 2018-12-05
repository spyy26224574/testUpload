package com.adai.gkdnavi.adapter;

import java.util.List;

import com.adai.gkdnavi.R;
import com.example.ipcamera.domain.CallRecord;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class RecordAdapter extends BaseAdapter {

	private Context context;
	private List<CallRecord> list;
	private LayoutInflater inflater;
	
	public RecordAdapter(Context context, List<CallRecord> list) {

		this.context = context;
		this.list = list;
		this.inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	/*
	 * 明天从这里开始写
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.phonerecord, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.phoneName);
			holder.number = (TextView) convertView.findViewById(R.id.phoneNumber);
			holder.time = (TextView) convertView.findViewById(R.id.phoneTime);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		CallRecord callRecord = list.get(position);
		holder.name.setText(callRecord.getName());
		holder.number.setText(callRecord.getNumber());
		holder.time.setText(callRecord.getDate());
		
		return convertView;
	}

	private static class ViewHolder {
		TextView name;
		TextView number;
		TextView time;
	}
}
