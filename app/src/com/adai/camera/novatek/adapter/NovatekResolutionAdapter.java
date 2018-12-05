package com.adai.camera.novatek.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.gkdnavi.R;

import java.util.ArrayList;

/**
 * @author huangxy
 * @date 2017/11/11 16:45
 */

public class NovatekResolutionAdapter extends RecyclerView.Adapter<NovatekResolutionAdapter.ViewHolder> {
    private final ArrayList<Integer> mKeyList;
    private int mCmdId;
    private SparseArray<String> mMenuItem;

    public interface ItemClickListener {
        /**
         * item点击事件的回调
         *
         * @param cmd 命令
         * @param key 设置指定的key
         */
        void onItemClick(int cmd, int key);
    }

    private ItemClickListener mItemClickListener;

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        mItemClickListener = onItemClickListener;
    }

    public NovatekResolutionAdapter(int cmdId) {
        mCmdId = cmdId;
        mMenuItem = NovatekRepository.getInstance().getMenuItem(mCmdId);
        mKeyList = new ArrayList<>();
        for (int i = 0; i < mMenuItem.size(); i++) {
            mKeyList.add(mMenuItem.keyAt(i));
        }
    }

    @Override
    public NovatekResolutionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resolution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final NovatekResolutionAdapter.ViewHolder holder, int position) {
        holder.mTvResolution.setText(mMenuItem.get(mKeyList.get(position)));
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mCmdId, mKeyList.get(holder.getAdapterPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMenuItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvResolution;
        private View mItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvResolution = (TextView) itemView.findViewById(R.id.tv_resolution);
            mItemView = itemView;
        }

    }
}
