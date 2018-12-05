package com.adai.gkdnavi.utils;

import com.adai.gkdnavi.utils.DownloadManager.DownloadObserver;
import com.example.ipcamera.domain.FileDomain;

import java.util.LinkedList;
import java.util.List;

public class AppListAdapter extends SuperBaseAdapter<FileDomain> implements DownloadObserver {
    /**
     * 用来记录有几个holder显示
     */
    private List<AppItemHolder> mHolders = new LinkedList<>();

    public AppListAdapter(List<FileDomain> datas) {
        super(datas);
    }

    @Override
    protected BaseHolder<FileDomain> getItemHolder(int position) {
        AppItemHolder holder = new AppItemHolder();

        if (!mHolders.contains(holder)) {
            mHolders.add(holder);
        }

        return holder;
    }

    /**
     * 用来控制开始监听
     */
    public void startObserver() {
        // 将holder加到观察者中
        DownloadManager.getInstance().addObserver(this);

        for (AppItemHolder holder : mHolders) {
            holder.checkState();
        }
    }

    /**
     * 停止开始监听
     */
    public void stopObserver() {
        DownloadManager.getInstance().deleteObserver(this);
    }

    @Override
    public synchronized void onDownloadStateChanged(DownLoadInfo info) {
        for (AppItemHolder holder : mHolders) {
            holder.checkState();
        }
    }

}
