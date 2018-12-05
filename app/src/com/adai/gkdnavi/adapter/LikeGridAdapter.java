package com.adai.gkdnavi.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;

import java.util.List;

/**
 * Created by admin on 2016/8/12.
 */
public class LikeGridAdapter extends BaseAdapter {
    private List<LikeUserBean> datas;

    public LikeGridAdapter(List<LikeUserBean> datas) {
        this.datas = datas;
    }

    @Override
    public int getCount() {
        return datas == null ? 0 : datas.size();
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
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.like_grid_item, null);
//            ((ImageView)convertView).setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
//            ((ImageView)convertView).setImageResource(R.drawable.icon_friend_normal);
//            ((ImageView)convertView).setScaleType(ImageView.ScaleType.FIT_XY);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
        LikeUserBean item = datas.get(position);
//        ImageLoader.getInstance().displayImage(item.portrait, (ImageView) convertView);
//        ImageLoadHelper.getInstance().displayImage(item.portrait, imageView,R.drawable.icon_friend_normal);
        ImageLoaderUtil.getInstance().loadRoundImage(parent.getContext(), item.portrait, R.drawable.icon_friend_normal, imageView);
        return convertView;
    }
}
