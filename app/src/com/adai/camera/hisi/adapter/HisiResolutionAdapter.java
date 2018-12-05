package com.adai.camera.hisi.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adai.gkdnavi.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author huangxy
 * @date 2017/11/21 16:48.
 */

public class HisiResolutionAdapter extends RecyclerView.Adapter<HisiResolutionAdapter.ViewHolder> {
    private List<String> mMenuItem = new ArrayList<>();
    private int mMode;
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        void onItemClick(int mode, String value);
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        mItemClickListener = onItemClickListener;
    }

    public HisiResolutionAdapter(int mode, List<String> menuItem) {
        mMode = mode;
        mMenuItem = menuItem;
    }

    @Override
    public HisiResolutionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resolution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final HisiResolutionAdapter.ViewHolder holder, int position) {
        holder.mTvResolution.setText(mMenuItem.get(position));
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mMode,mMenuItem.get(holder.getLayoutPosition()));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mMenuItem == null ? 0 : mMenuItem.size();
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
