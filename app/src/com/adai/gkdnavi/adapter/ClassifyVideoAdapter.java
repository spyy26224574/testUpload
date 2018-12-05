package com.adai.gkdnavi.adapter;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkd.bean.square.ClassifyVideoBean;
import com.adai.gkd.bean.square.VideoGridBean;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.TypeVideoActivity;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.fragment.square.TypeVideoRecyclerViewAdapter;

import java.util.List;

/**
 * 广场页面分类显示
 * @author admin
 *
 */
public class ClassifyVideoAdapter extends RecyclerView.Adapter<CalssifyVideoHolder>{

	Fragment context;
	private List<ClassifyVideoBean> data;
	
	public ClassifyVideoAdapter(Fragment context,List<ClassifyVideoBean> data) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.data=data;
	}
	@Override
	public int getItemCount() {
		// TODO Auto-generated method stub
		return data==null?0:data.size();
	}

	@Override
	public void onBindViewHolder(final CalssifyVideoHolder vh, int position) {
		// TODO Auto-generated method stub
		ClassifyVideoBean item=data.get(position);
		vh.mItem=item;
		vh.classify_name.setText(item.typeName);
		vh.classify_des.setText(item.typeDescribe);
		vh.adapter=new ClassifyVideoGridAdapter(context.getActivity(), item.squareColle);
		vh.line_show_all.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent type=new Intent(context.getActivity(), TypeVideoActivity.class);
				type.putExtra("typeid",vh.mItem.typeId);
				type.putExtra("typename",vh.mItem.typeName);
				context.startActivity(type);
			}
		});
		vh.video_grid.setAdapter(vh.adapter);
		vh.video_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				VideoGridBean colle = vh.mItem.squareColle.get(position);
				Intent intent=new Intent(context.getActivity(), VideoDetailActivity.class);
				intent.putExtra("resourceid",colle.resourceId);
				intent.putExtra("fileType",colle.fileType);
				context.startActivityForResult(intent, TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE);
			}
		});
	}

	@Override
	public CalssifyVideoHolder onCreateViewHolder(ViewGroup arg0, int position) {
		// TODO Auto-generated method stub
		View v=LayoutInflater.from(context.getActivity()).inflate(R.layout.item_classify_video, null);
		return new CalssifyVideoHolder(v);
	}

}

class CalssifyVideoHolder extends RecyclerView.ViewHolder{

	public ClassifyVideoBean mItem;
	public final TextView classify_name,classify_des;
	public final LinearLayout line_show_all;
	public final GridView video_grid;
	public ClassifyVideoGridAdapter adapter;
	
	public CalssifyVideoHolder(View itemView) {
		super(itemView);
		// TODO Auto-generated constructor stub
		classify_name=(TextView)itemView.findViewById(R.id.classify_name);
		classify_des=(TextView)itemView.findViewById(R.id.classify_des);
		line_show_all=(LinearLayout)itemView.findViewById(R.id.line_show_all);
		video_grid=(GridView)itemView.findViewById(R.id.videolistgrid);
	}
	
}
