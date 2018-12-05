package com.adai.gkdnavi.utils;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.DownloadManager.DownloadObserver;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.adai.gkdnavi.view.ProgressCircleView;
import com.example.ipcamera.domain.FileDomain;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

public class AppItemHolder extends BaseHolder<FileDomain> implements
        OnClickListener, DownloadObserver {

    private static final String TAG = "AppItemHolder";

    @ViewInject(R.id.unload_name)
    private TextView mTvTitle;

    @ViewInject(R.id.item_tv_size)
    private TextView mTvSize;

    @ViewInject(R.id.unload_time)
    private TextView mTvDes;

    @ViewInject(R.id.imgView)
    private ImageView mIvIcon;

    @ViewInject(R.id.item_btn_download)
    private ProgressCircleView mProgressBtn;

    @ViewInject(R.id.down_lock)
    private ImageView lock;

    @Override
    protected View initView() {

        View view = View.inflate(UIUtils.getContext(), R.layout.itemdownload, null);
        // 注入
        ViewUtils.inject(this, view);
        mProgressBtn.setOnClickListener(this);
        return view;
    }

    @Override
    protected void refreshUI(final FileDomain data) {
        mTvDes.setText(data.time);
        mTvTitle.setText(data.name);
        mTvSize.setText(StringUtils.formatFileSize(data.size, false));
        // 设置图片
        //mIvIcon.setImageBitmap(data.getBitmap());
        if (data.attr == 32) {
            lock.setVisibility(View.GONE);
        } else {
            lock.setVisibility(View.VISIBLE);
        }
        String name = data.name;
        String fpath = data.fpath;
        String url = null;
        // 获取当前下载的状态
        DownLoadInfo info = DownloadManager.getInstance().getDownloadInfo(data);
        safeUpdateState(info);
        // FIXME: 2016/10/28 暂时修改规则
//        if (name.contains("MOV")) {
//            mIvIcon.setBackgroundResource(R.drawable.video_default);
//            if (fpath.contains("RO")) {
//                url = Contacts.URL_GET_THUMBNAIL_HEAD_RO + name + Contacts.URL_GET_THUMBNAIL_END;
//            } else {
//                url = Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE + name + Contacts.URL_GET_THUMBNAIL_END;
//            }
////			BitmapHelper.display(mIvIcon,url);
//        } else {
//            if (info.state == DownloadManager.STATE_DOWNLOADED) {
//                url = DownloadManager.getInstance().getCachePath(info.fileName);
//            } else {
//                url = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO + name;
//            }
//        }
        if (fpath.toLowerCase().contains("\\movie\\")) {
            mIvIcon.setBackgroundResource(R.drawable.video_default);
            url = (Contacts.BASE_HTTP_IP + fpath.substring(fpath.indexOf(":") + 1)).replace("\\", "/") + Contacts.URL_GET_THUMBNAIL_END;
            BitmapHelper.display(mIvIcon, url);
        } else {
            if (info.state == DownloadManager.STATE_DOWNLOADED) {
                url = DownloadManager.getInstance().getCachePath(info.fileName);
            } else {
                url = (Contacts.BASE_HTTP_IP + fpath.substring(fpath.indexOf(":") + 1)).replace("\\", "/");
            }
            ImageLoaderUtil.getInstance().loadImage(UIUtils.getContext(), url, mIvIcon);
        }
        //System.out.println("刷新UI:" + info);

    }

    @Override
    public void onClick(View v) {
        if (v == mProgressBtn) {
            performProgressClick();
        }
    }

    public void checkState() {
        DownLoadInfo info = DownloadManager.getInstance()
                .getDownloadInfo(mData);
        safeUpdateState(info);
    }

    private void safeUpdateState(final DownLoadInfo info) {
        UIUtils.post(new Runnable() {

            @Override
            public void run() {
                updateState(info);
            }
        });
    }

    private void updateState(DownLoadInfo info) {
        int state = info.state;
        mProgressBtn.setProgressEnable(false);
        switch (state) {
            case DownloadManager.STATE_NONE:
                mProgressBtn.setText(UIUtils.getContext().getString(R.string.wifi_downloading));
                mProgressBtn.setIcon(R.drawable.ic_download);
                break;
            case DownloadManager.STATE_WAITTING:
                mProgressBtn.setText(UIUtils.getContext().getString(R.string.wifi_waitdownload));
                mProgressBtn.setIcon(R.drawable.ic_pause);
                break;
            case DownloadManager.STATE_DOWNLOADING:
                mProgressBtn.setIcon(R.drawable.ic_pause);
                mProgressBtn.setProgressEnable(true);
                int progress = (int) (info.currentProgress * 100f / info.size + 0.5f);
                mProgressBtn.setProgress(progress);
                Log.e(TAG, "progress = " + progress + " ,info.currentProgress=" + info.currentProgress);
                mProgressBtn.setText(progress + "%");// 百分比
                break;
            case DownloadManager.STATE_PAUSE:
                mProgressBtn.setText(UIUtils.getContext().getString(R.string.wifi_continuedownload));
                mProgressBtn.setIcon(R.drawable.ic_resume);
                break;
            case DownloadManager.STATE_DOWNLOADED:
                mProgressBtn.setText(UIUtils.getContext().getString(R.string.download_cuccess));
                mProgressBtn.setIcon(R.drawable.ic_downloadsuccess);
                break;
            case DownloadManager.STATE_FAILED:
                mProgressBtn.setText(UIUtils.getContext().getString(R.string.wifi_retrydownload));
                mProgressBtn.setIcon(R.drawable.ic_redownload);
                break;
            default:
                break;
        }
    }

    // 行为操作
    private void performProgressClick() {

        DownLoadInfo info = DownloadManager.getInstance()
                .getDownloadInfo(mData);
        int state = info.state;
        //System.out.println("点击下载  : " + state);

        switch (state) {
            case DownloadManager.STATE_NONE:
                download();
                break;
            case DownloadManager.STATE_WAITTING:
                cancel();
                break;
            case DownloadManager.STATE_DOWNLOADING:
                pause();
//			cancel();
                break;
            case DownloadManager.STATE_PAUSE:
                download();
                break;
            case DownloadManager.STATE_DOWNLOADED:
                break;
            case DownloadManager.STATE_FAILED:
                download();
                break;
            default:
                break;
        }
    }

    private void download() {
        DownloadManager.getInstance().download(mData);
    }

    private void cancel() {
        Toast.makeText(UIUtils.getContext(), UIUtils.getContext().getString(R.string.cancel), Toast.LENGTH_SHORT).show();
        DownloadManager.getInstance().cancel(mData);
    }

    private void pause() {
        Toast.makeText(UIUtils.getContext(), UIUtils.getContext().getString(R.string.wifi_pausedownload), Toast.LENGTH_SHORT).show();
        DownloadManager.getInstance().pause(mData);
    }

    @Override
    public void onDownloadStateChanged(DownLoadInfo info) {
        // 子线程中执行的
        if (info.fileName.equals(mData.name)) {
            // UI更新
            safeUpdateState(info);
        }
    }

}
