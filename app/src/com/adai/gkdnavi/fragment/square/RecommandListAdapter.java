package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by admin on 2016/9/8.
 */
public class RecommandListAdapter extends BaseAdapter {

    private final List<LikeUserBean> mValues;
    private Activity mContext;
    public RecommandListAdapter(Activity context, List<LikeUserBean> items){
        mValues = items;
        mContext=context;
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
        if(convertView==null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_likeuser, parent, false);
        }
        ImageView head_img = (ImageView) convertView.findViewById(R.id.head_img);
        TextView nickname = (TextView) convertView.findViewById(R.id.nickname);
        TextView sign = (TextView) convertView.findViewById(R.id.sign);
        TextView add_attention = (TextView) convertView.findViewById(R.id.add_attention);
        final LikeUserBean mItem=mValues.get(position);
        nickname.setText(mItem.nickname);
        sign.setText(mItem.signature);
        Glide.with(mContext).load(mItem.portrait).into(head_img);
        if("Y".equals(mItem.isFocusOn)){
            add_attention.setText(mContext.getString(R.string.already_attention));
            add_attention.setSelected(true);
        }else{
            add_attention.setText(mContext.getString(R.string.add_attention));
            add_attention.setSelected(false);
        }
        add_attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestMethods_square.addAttention(mItem.userId, new HttpUtil.Callback<BasePageBean>() {
                    @Override
                    public void onCallback(BasePageBean result) {
                        if(result!=null){
                            switch (result.ret){
                                case 0:
                                    notifyDataSetChanged();
                                    break;
                                default:
                                    Toast.makeText(mContext,result.message,Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }
                });
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personal=new Intent(mContext, PersonalPageActivity.class);
                personal.putExtra("userid",mItem.userId);
                mContext.startActivity(personal);
            }
        });
        return convertView;
    }
}
