package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.bean.square.VideoGridBean;
import com.adai.gkdnavi.R;
import com.bumptech.glide.Glide;

import java.util.List;

public class ClassifyVideoGridAdapter extends BaseAdapter {

	private List<VideoGridBean> data;
	Context context;
	
	public ClassifyVideoGridAdapter(Context context,List<VideoGridBean> data) {
		// TODO Auto-generated constructor stub
		this.data=data;
		this.context=context;
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data==null?0:data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(convertView==null){
			convertView=LayoutInflater.from(context).inflate(R.layout.item_classify_video_grid, null);
		}
		ImageView logo=(ImageView)convertView.findViewById(R.id.video_logo);
		ImageView type=(ImageView)convertView.findViewById(R.id.item_type);
		TextView see_nums=(TextView)convertView.findViewById(R.id.see_nums);
		TextView title=(TextView)convertView.findViewById(R.id.video_grid_title);
		VideoGridBean item = data.get(position);
//		ImageLoader.getInstance().displayImage(item.coverPicture, logo);
		Glide.with(context).load(item.coverPicture).placeholder(R.drawable.default_video_holder).into(logo);
		see_nums.setText(String.valueOf(item.browseCount));
		if("100".equals(item.fileType)){
			type.setBackgroundResource(R.drawable.bg_video_transport);
		}else{
			type.setBackgroundResource(R.drawable.bg_image_transport);
		}
		title.setText(item.des);
		return convertView;
	}

}
