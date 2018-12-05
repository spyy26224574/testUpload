package com.adai.gkdnavi.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by admin on 2016/8/23.
 */
public class BottomRefreshRecycleView extends RecyclerView {
    private int mScrollY = 0;

    public interface OnRefreshListener {
        public void onRefresh();
    }

    private int lastVisibleItem = 0;
    private OnRefreshListener refreshListener;

    public void setRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    private OnScrollListener mScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            if (newState == RecyclerView.SCROLL_STATE_IDLE
                    && getAdapter() != null && lastVisibleItem + 1 == getAdapter().getItemCount() && mScrollY > 0) {
                if (refreshListener != null) {
                    refreshListener.onRefresh();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            mScrollY = dy;
            LayoutManager manager = getLayoutManager();
            if (manager instanceof LinearLayoutManager) {
                lastVisibleItem = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
            } else if (manager instanceof GridLayoutManager) {
                lastVisibleItem = ((GridLayoutManager) getLayoutManager()).findLastVisibleItemPosition();
            }
        }
    };

    public BottomRefreshRecycleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BottomRefreshRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BottomRefreshRecycleView(Context context) {
        super(context);
        init();
    }

    private void init() {
        addOnScrollListener(mScrollListener);
    }
}
