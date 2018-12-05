package com.filepicker.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkdnavi.R;
import com.bumptech.glide.Glide;
import com.filepicker.models.PhotoDirectory;

import java.util.List;

/**
 * Created by admin on 2016/9/19.
 */
public class PhotoDirsPopAdapter extends BaseAdapter {

    private List<PhotoDirectory> dirs;

    public PhotoDirsPopAdapter(List<PhotoDirectory> dirs){
        this.dirs=dirs;
    }
    @Override
    public int getCount() {
        return dirs==null?0:dirs.size();
    }

    @Override
    public Object getItem(int position) {
        return dirs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView==null){
            convertView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_dir,null);
            holder=new ViewHolder();
            holder.icon_dir=(ImageView)convertView.findViewById(R.id.icon_dir);
            holder.name_dir=(TextView)convertView.findViewById(R.id.dir_name);
            holder.photos_num=(TextView)convertView.findViewById(R.id.photos_num);
            convertView.setTag(holder);
        }else{
            holder=(ViewHolder)convertView.getTag();
        }
        PhotoDirectory dir = dirs.get(position);
        if(dir.getPhotos().size()>0) {
            Glide.with(parent.getContext()).load(dir.getPhotos().get(0).getPath()).into(holder.icon_dir);
        }
        holder.photos_num.setText(String.format("%d张",dir.getPhotos().size()));
        holder.name_dir.setText(dir.getName());
        return convertView;
    }

    class ViewHolder{
        public ImageView icon_dir;
        public TextView name_dir;
        public TextView photos_num;
    }
}
