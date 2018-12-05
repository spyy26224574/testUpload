package com.adai.camera.mstar.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adai.camera.mstar.data.MstarRepository;
import com.adai.gkdnavi.R;

import java.util.List;

/**
 * @author huangxy
 * @date 2017/11/24 15:15.
 */

public class MstarResolutionAdapter extends RecyclerView.Adapter<MstarResolutionAdapter.ViewHolder> {

    private int mMenuId;
    private List<String> mMenuItem;

    public interface ItemClickListener {
        void onItemClick(int menuId, String param);
    }

    private ItemClickListener mItemClickListener;

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        mItemClickListener = onItemClickListener;
    }

    public MstarResolutionAdapter(int menuId) {
        mMenuId = menuId;
        MstarRepository.Menu menu = MstarRepository.getInstance().GetAutoMenu(menuId);
        if (menu != null) {
            mMenuItem = menu.GetMenuItemIdList();
        }
    }

    @Override
    public MstarResolutionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resolution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MstarResolutionAdapter.ViewHolder holder, int position) {
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mMenuId, mMenuItem.get(holder.getLayoutPosition()));
                }
            }
        });
        holder.mTvResolution.setText(mMenuItem.get(position));
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
