package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.adai.gkd.bean.LocationBean;
import com.adai.gkdnavi.R;

import java.util.List;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/24 17:28
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class LocationSearchAdapter extends RecyclerView.Adapter<LocationSearchAdapter.ViewHolder> {
    private List<LocationBean> mData;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    public interface OnItemClickListener{
        void onClick(LocationBean locationBean);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public LocationSearchAdapter(Context context, List<LocationBean> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public LocationSearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.item_location, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final LocationSearchAdapter.ViewHolder holder, int position) {
        holder.mAddress.setText(mData.get(position).address);
        holder.mName.setText(mData.get(position).name);
        if (mData.get(position).isCheck) {
            holder.mCb.setChecked(true);
            holder.mCb.setVisibility(View.VISIBLE);
        }else{
            holder.mCb.setChecked(false);
            holder.mCb.setVisibility(View.INVISIBLE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (LocationBean locationBean : mData) {
                    locationBean.isCheck = false;
                }
                mData.get(holder.getAdapterPosition()).isCheck = true;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onClick(mData.get(holder.getAdapterPosition()));
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mAddress;
        CheckBox mCb;
        TextView mName;
        public ViewHolder(View itemView) {
            super(itemView);
            mAddress = (TextView) itemView.findViewById(R.id.tv_address);
            mCb = (CheckBox) itemView.findViewById(R.id.cb);
            mName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
}
