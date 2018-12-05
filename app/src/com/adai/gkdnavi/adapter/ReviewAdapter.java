package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.bean.square.ReviewBean;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.TimeUtils;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by admin on 2016/8/10.
 */
public class ReviewAdapter extends BaseAdapter {
    private List<ReviewBean> mValues;
    private Context mContext;

    public ReviewAdapter(Context context,List<ReviewBean> values){
        this.mValues=values;
        this.mContext=context;
    }
    @Override
    public int getCount() {
        return mValues==null?0:mValues.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;
        if(convertView==null){
            convertView= LayoutInflater.from(mContext).inflate(R.layout.item_video_reply,parent);
            holder=new ViewHolder();
            holder.user_head=(ImageView)convertView.findViewById(R.id.user_head);
            holder.user_nickname=(TextView)convertView.findViewById(R.id.user_nickname);
            holder.level_time=(TextView)convertView.findViewById(R.id.level_time);
            holder.btn_reply=(TextView)convertView.findViewById(R.id.btn_reply);
            holder.reply_message=(TextView)convertView.findViewById(R.id.reply_message);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        ReviewBean item = mValues.get(position);
        ImageLoader.getInstance().displayImage(item.portrait,holder.user_head);
        holder.user_nickname.setText(item.nickname);
        String level_text=String.format("%d%s",item.level,mContext.getResources().getString(R.string.floor))+"  "+ TimeUtils.getTimeStr(mContext,"yyyy-MM-dd kk:mm:ss",item.reviewTime);
        holder.level_time.setText(level_text);
        if(!TextUtils.isEmpty(item.replyUserName)){
            String text="回复 <span color='red'>"+item.replyUserName+"</span> "+item.message;
            holder.reply_message.setText(Html.fromHtml(text));
        }else{
            holder.reply_message.setText(item.message);
        }
        return convertView;
    }

    class ViewHolder{
        public ImageView user_head;
        public TextView user_nickname;
        public TextView level_time;
        public TextView btn_reply;
        public TextView reply_message;
    }
}
