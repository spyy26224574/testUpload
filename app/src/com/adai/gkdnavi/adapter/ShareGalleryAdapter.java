package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.ShareLayoutActivity;

import java.util.List;

/**
 * Created by admin on 2016/8/29.
 */
public class ShareGalleryAdapter extends BaseAdapter {
    private List<ShareLayoutActivity.Appinfo> datas;
    private Context context;

    public ShareGalleryAdapter(Context context, List<ShareLayoutActivity.Appinfo> datas){
        this.context=context;
        this.datas=datas;
    }
    @Override
    public int getCount() {
        return datas==null?0:datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView= LayoutInflater.from(context).inflate(R.layout.item_share_layout,null);
        }
        TextView title=(TextView)convertView.findViewById(R.id.title);
        ImageView icon=(ImageView)convertView.findViewById(R.id.icon);
        title.setText(datas.get(position).title);
        icon.setImageDrawable(datas.get(position).icon);
        return convertView;
    }
}
