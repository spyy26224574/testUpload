package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.adai.gkdnavi.R;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by admin on 2016/8/9.
 */
public class ImageGridAdapter extends BaseAdapter {
    private List<String> mValues;
    private Context mContext;
    private int dWidth;

    public ImageGridAdapter(Context context, List<String> data) {
        mValues = data;
        mContext = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        dWidth = (windowManager.getDefaultDisplay().getWidth() - context.getResources().getDimensionPixelOffset(R.dimen.left_margin) * 6) / 3;
    }

    @Override
    public int getCount() {
        return mValues == null ? 0 : mValues.size();
    }

    @Override
    public Object getItem(int position) {
        return mValues.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new ImageView(mContext);
//            ((ImageView)convertView).setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(dWidth, (dWidth * 3) / 4);
            ((ImageView) convertView).setScaleType(ImageView.ScaleType.CENTER_CROP);
            convertView.setLayoutParams(params);
        }
//        ImageLoader.getInstance().displayImage(mValues.get(position), (ImageView) convertView);
        Glide.with(mContext).load(mValues.get(position)).placeholder(R.drawable.default_image_holder).into((ImageView) convertView);
        return convertView;
    }
}
