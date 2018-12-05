package com.adai.camera.sunplus.filemanager.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.bean.SunplusMinuteFileDownLoadInfo;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.filemanager.SunplusFileFragment;
import com.adai.camera.sunplus.filemanager.SunplusPictureBrowseActivity;
import com.adai.camera.sunplus.filemanager.SunplusVideoPreviewActivity;
import com.adai.camera.sunplus.tool.SunplusImageLoadManager;
import com.adai.camera.sunplus.tool.SunplusMinuteFileDownloadManager;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.utils.DownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.view.ProgressCircleView;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFileType;

import org.videolan.vlc.util.Strings;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/9/19 10:11.
 */

public class SunplusFileAdapter extends RecyclerView.Adapter {
    private ArrayList<SunplusMinuteFile> mMinuteFiles;
    private Activity mActivity;
    private static final int TYPE_TITLE = 1 << 1;
    private static final int TYPE_CONTENT = 1 << 2;
    public static final String TAG_HASFILE = "hasFile";
    private EventListener mEventListener;

    public interface EventListener {
        void modeChange(boolean isEditMode);

        void hasFile(boolean hasFile);
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    private boolean isEditMode;

    public void setEditMode(boolean editMode) {
        if (isEditMode != editMode) {
            isEditMode = editMode;
            if (!isEditMode) {
                GlobalInfo.mSelectedMinuteFile.clear();
                //退出编辑模式的时候全部变成非选中状态
                if (mMinuteFiles != null && mMinuteFiles.size() > 0) {
                    for (SunplusMinuteFile minuteFile : mMinuteFiles) {
                        minuteFile.isChecked = false;
                        minuteFile.isTitleSelected = false;
                    }
                }
            }
        }
        if (mEventListener != null) {
            mEventListener.modeChange(editMode);
        }
    }

    public SunplusFileAdapter(Activity activity, ArrayList<SunplusMinuteFile> minuteFiles) {
        mActivity = activity;
        mMinuteFiles = minuteFiles;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (viewType) {
            case TYPE_TITLE:
                itemView = inflater.inflate(R.layout.item_file_gride_title, parent, false);
                return new TitleViewHolder(itemView);
            default:
                itemView = inflater.inflate(R.layout.item_ablum_grid, parent, false);
                return new ContentViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof TitleViewHolder) {
            TitleViewHolder titleViewHolder = (TitleViewHolder) holder;
            titleViewHolder.updateView(position);
        } else if (holder instanceof ContentViewHolder) {
            ContentViewHolder contentViewHolder = (ContentViewHolder) holder;
            contentViewHolder.updateView(position);
        }
    }

    private class ContentViewHolder extends RecyclerView.ViewHolder implements SunplusMinuteFileDownloadManager.DownloadObserver {
        private ImageView mImageView;
        private ImageView mIvIcon, mIvLock;
        private TextView mTvTime, mTvNum, mTvSize;
        private RelativeLayout mRlSelectMark, mRlBottom;
        private View mItemView;
        private ProgressCircleView mProgressCircleView;
        private SunplusMinuteFile mMinuteFile;
        private int mPosition;
        private ProgressBar mProgressBar;

        public ContentViewHolder(View itemView) {
            super(itemView);
            SunplusMinuteFileDownloadManager.getInstance().addObserver(this);
            mItemView = itemView;
            mProgressCircleView = (ProgressCircleView) itemView.findViewById(R.id.item_btn_download);
            mProgressCircleView.setVisibility(View.VISIBLE);
            mImageView = (ImageView) itemView.findViewById(R.id.iv);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_time);
            mTvNum = (TextView) itemView.findViewById(R.id.tv_num);
            mIvLock = (ImageView) itemView.findViewById(R.id.down_lock);
            mRlSelectMark = (RelativeLayout) itemView.findViewById(R.id.rl_select_mark);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            mRlBottom = (RelativeLayout) itemView.findViewById(R.id.rl_bottom);
            mTvSize = (TextView) itemView.findViewById(R.id.tv_size);
            initEvent();
        }

        private void initEvent() {
            mProgressCircleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performProgressClick();
                }
            });
            mItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isEditMode) {
                        boolean allChecked = false;
                        mMinuteFile.isChecked = true;
                        GlobalInfo.mSelectedMinuteFile.add(mMinuteFile);
                        int titlePosition = mPosition;
                        int nextTitlePosition = mPosition;
                        for (; !mMinuteFiles.get(titlePosition).isTitle; titlePosition--) {

                        }
                        for (; !mMinuteFiles.get(nextTitlePosition).isTitle; nextTitlePosition++) {
                            if (nextTitlePosition == mMinuteFiles.size() - 1) {
                                //如果是最后一条，那么就没有下一个title了
                                nextTitlePosition = mMinuteFiles.size();
                                break;
                            }
                        }
                        for (int i = titlePosition + 1; i < nextTitlePosition; i++) {
                            if (!mMinuteFiles.get(i).isChecked) {
                                allChecked = false;
                                break;
                            } else {
                                allChecked = true;
                            }
                        }
                        if (mMinuteFiles.get(titlePosition).isTitleSelected != allChecked) {
                            mMinuteFiles.get(titlePosition).isTitleSelected = allChecked;
                        }
                        mRlSelectMark.setVisibility(View.VISIBLE);
                        if (mEventListener != null) {
                            mEventListener.hasFile(true);
                            isEditMode = true;
                            mEventListener.modeChange(true);
                        }
                    }
                    return true;
                }
            });
            mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isEditMode) {
                        //编辑模式，点击改变选中状态
                        boolean allChecked = false;
                        mMinuteFile.isChecked = !mMinuteFile.isChecked;
                        mRlSelectMark.setVisibility(mMinuteFile.isChecked ? View.VISIBLE : View.GONE);
                        if (mMinuteFile.isChecked) {
                            GlobalInfo.mSelectedMinuteFile.add(mMinuteFile);
                            if (GlobalInfo.mSelectedMinuteFile.size() == 1) {
                                if (mEventListener != null) {
                                    mEventListener.hasFile(true);
                                }
                            }
                        } else {
                            GlobalInfo.mSelectedMinuteFile.remove(mMinuteFile);
                            if (GlobalInfo.mSelectedMinuteFile.size() == 0) {
                                if (mEventListener != null) {
                                    mEventListener.hasFile(false);
                                }
                            }
                        }
                        int titlePosition = mPosition;
                        int nextTitlePosition = mPosition;
                        for (; !mMinuteFiles.get(titlePosition).isTitle; titlePosition--) {

                        }
                        for (; !mMinuteFiles.get(nextTitlePosition).isTitle; nextTitlePosition++) {
                            if (nextTitlePosition == mMinuteFiles.size() - 1) {
                                //如果是最后一条，那么就没有下一个title了
                                nextTitlePosition = mMinuteFiles.size();
                                break;
                            }
                        }
                        for (int i = titlePosition + 1; i < nextTitlePosition; i++) {
                            if (!mMinuteFiles.get(i).isChecked) {
                                allChecked = false;
                                break;
                            } else {
                                allChecked = true;
                            }
                        }
                        if (mMinuteFiles.get(titlePosition).isTitleSelected != allChecked) {
                            mMinuteFiles.get(titlePosition).isTitleSelected = allChecked;
//                            notifyItemChanged(titlePosition);
                        }
                    } else {
                        if (SunplusMinuteFileDownloadManager.isDownloading) {
                            ToastUtil.showShortToast(mActivity, mActivity.getString(R.string.please_stop_download));
                            return;
                        }
                        if (mMinuteFile.fileDomains.get(0).getFileType() == ICatchFileType.ICH_TYPE_IMAGE) {
                            Intent pictureIntent = new Intent(mActivity, SunplusPictureBrowseActivity.class);
                            pictureIntent.putExtra(SunplusPictureBrowseActivity.KEY_POSTION, 0);
                            GlobalInfo.previewFileList.clear();
                            GlobalInfo.previewFileList.addAll(mMinuteFile.fileDomains);
                            mActivity.startActivityForResult(pictureIntent, SunplusFileFragment.REQUEST_FILE_DELETE);
                        } else {
                            Intent videoIntent = new Intent(mActivity, SunplusVideoPreviewActivity.class);
                            GlobalInfo.previewFileList.clear();
                            int titlePosition = mPosition;
                            int nextTitlePosition = mPosition;
                            for (; !mMinuteFiles.get(titlePosition).isTitle; titlePosition--) {

                            }
                            for (; !mMinuteFiles.get(nextTitlePosition).isTitle; nextTitlePosition++) {
                                if (nextTitlePosition == mMinuteFiles.size() - 1) {
                                    //如果是最后一条，那么就没有下一个title了
                                    nextTitlePosition++;
                                    break;
                                }
                            }
                            for (int i = titlePosition + 1; i < nextTitlePosition; i++) {
                                if (mMinuteFiles.get(i).fileDomains.get(0).getFileType() != ICatchFileType.ICH_TYPE_IMAGE) {
                                    GlobalInfo.previewFileList.add(mMinuteFiles.get(i).fileDomains.get(0));
                                }
                            }
                            int position = GlobalInfo.previewFileList.indexOf(mMinuteFile.fileDomains.get(0));
                            videoIntent.putExtra(SunplusVideoPreviewActivity.KEY_POSTION, position);
                            mActivity.startActivity(videoIntent);
                        }
                    }
                }
            });
        }

        void updateView(int position) {
            mPosition = position;
            this.mMinuteFile = mMinuteFiles.get(position);
            mTvTime.setText(mMinuteFile.time.substring(11, 19));
            ICatchFile iCatchFile = mMinuteFile.fileDomains.get(0);
            if (iCatchFile.getFileType() == ICatchFileType.ICH_TYPE_IMAGE) {
                SunplusImageLoadManager.getInstance().loadImage(iCatchFile, R.drawable.default_image_holder, mImageView, true);
            } else {
                SunplusImageLoadManager.getInstance().loadImage(iCatchFile, R.drawable.default_video_holder, mImageView, true);
                mRlBottom.setVisibility(View.VISIBLE);
            }
            mTvSize.setText(Strings.readableFileSize(iCatchFile.getFileSize()));
            mRlSelectMark.setVisibility(mMinuteFile.isChecked ? View.VISIBLE : View.GONE);
            SunplusMinuteFileDownloadManager.getInstance().addInfo(mMinuteFile);
            SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = SunplusMinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(mMinuteFile);
            saveUpdate(minuteFileDownloadInfo);
        }

        @Override
        public void onDownloadStateChanged(SunplusMinuteFileDownLoadInfo info) {
            if (mMinuteFile == null) {
                return;
            }
            if (mMinuteFile.hashCode() == info.key) {
                saveUpdate(info);
            }
        }

        private void saveUpdate(final SunplusMinuteFileDownLoadInfo info) {
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    updateState(info);
                }
            });
        }

        private void updateState(SunplusMinuteFileDownLoadInfo info) {
            if (mMinuteFile.hashCode() != info.key) {
                return;
            }
            int state = info.state;
            mProgressCircleView.setProgressEnable(false);
            mProgressCircleView.setVisibility(View.VISIBLE);
            mProgressCircleView.setClickable(true);
            mProgressBar.setVisibility(View.GONE);
            switch (state) {
                case SunplusMinuteFileDownloadManager.STATE_NONE:
                    mProgressCircleView.setVisibility(View.GONE);
                    break;
                case SunplusMinuteFileDownloadManager.STATE_WAITTING:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_waitdownload));
                    mProgressCircleView.setIcon(R.drawable.ic_pause);
                    break;
                case SunplusMinuteFileDownloadManager.STATE_DOWNLOADING:
                   /* mProgressCircleView.setIcon(R.drawable.ic_pause);
                    mProgressCircleView.setProgressEnable(true);
                    int progress = info.progress;
                    mProgressCircleView.setProgress(progress);
                    mProgressCircleView.setText(progress + "%");// 百分比*/
                    mProgressCircleView.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case SunplusMinuteFileDownloadManager.STATE_PAUSE:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_continuedownload));
                    mProgressCircleView.setIcon(R.drawable.ic_download);
                    break;
                case SunplusMinuteFileDownloadManager.STATE_DOWNLOADED:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.download_cuccess));
                    mProgressCircleView.setIcon(R.drawable.downloaded);
                    mProgressCircleView.setClickable(false);
                    try {
                        Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
                        intent.putExtra("isVideo", FileTypeUtil.getFileType(info.allDownloadInfos.get(0).savePath) == FileTypeUtil.TYPE_VIDEO);
                        VLCApplication.getAppContext().sendBroadcast(intent);
                    } catch (Exception ignore) {

                    }
                    break;
                case SunplusMinuteFileDownloadManager.STATE_FAILED:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_retrydownload));
                    mProgressCircleView.setIcon(R.drawable.ic_redownload);
                    break;
                default:
                    break;
            }
        }

        // 下载按钮点击事件
        private void performProgressClick() {

            SunplusMinuteFileDownLoadInfo minuteFileDownloadInfo = SunplusMinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(mMinuteFile);
            int state = minuteFileDownloadInfo.state;
            switch (state) {
                case DownloadManager.STATE_NONE:
                    SunplusMinuteFileDownloadManager.getInstance().download(mMinuteFile);
                    break;
                case DownloadManager.STATE_WAITTING:
                    SunplusMinuteFileDownloadManager.getInstance().cancel(mMinuteFile);
                    break;
                case DownloadManager.STATE_DOWNLOADING:
                    SunplusMinuteFileDownloadManager.getInstance().pause(mMinuteFile);
                    break;
                case DownloadManager.STATE_PAUSE:
                    SunplusMinuteFileDownloadManager.getInstance().download(mMinuteFile);
                    break;
                case DownloadManager.STATE_DOWNLOADED:
                    break;
                case DownloadManager.STATE_FAILED:
                    SunplusMinuteFileDownloadManager.getInstance().download(mMinuteFile);
                    break;
                default:
                    break;
            }
        }
    }

    private class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView mTime;
        private TextView mTvSelect;
        private int mPosition;

        public TitleViewHolder(View itemView) {
            super(itemView);
            mTime = (TextView) itemView.findViewById(R.id.tv_time);
            mTvSelect = (TextView) itemView.findViewById(R.id.tv_select);
            initEvent();
        }

        private void initEvent() {
            mTvSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int nextTitlePosition = mPosition + 1;
                    for (; !mMinuteFiles.get(nextTitlePosition).isTitle; nextTitlePosition++) {
                        if (nextTitlePosition == mMinuteFiles.size() - 1) {
                            //如果是最后一个title，那么就没有下一个title了
                            nextTitlePosition = mMinuteFiles.size();
                            break;
                        }
                    }

                    if (mMinuteFiles.get(mPosition).isTitleSelected) {
                        //取消选中当前小时下的所有文件
                        mMinuteFiles.get(mPosition).isTitleSelected = false;
                        for (int i = mPosition; i < nextTitlePosition; i++) {
                            if (mMinuteFiles.get(i).isChecked) {
                                mMinuteFiles.get(i).isChecked = false;
                                GlobalInfo.mSelectedMinuteFile.remove(mMinuteFiles.get(i));
                            }
                        }
                        boolean curHasFile = GlobalInfo.mSelectedMinuteFile.size() > 0;
                        if (!curHasFile) {
                            Intent intent = new Intent(FileManagerConstant.ACTION_SELECTED_FILE);
                            intent.putExtra(TAG_HASFILE, false);
                            mActivity.sendBroadcast(intent);
                        }
                    } else {
                        //选择当前小时下的所有文件
                        mMinuteFiles.get(mPosition).isTitleSelected = true;
                        for (int i = mPosition + 1; i < nextTitlePosition; i++) {
                            if (!mMinuteFiles.get(i).isChecked) {
                                mMinuteFiles.get(i).isChecked = true;
                                GlobalInfo.mSelectedMinuteFile.add(mMinuteFiles.get(i));
                            }

                        }
                        boolean curHasFile = GlobalInfo.mSelectedMinuteFile.size() > 0;
                        if (curHasFile) {
                            Intent intent = new Intent(FileManagerConstant.ACTION_SELECTED_FILE);
                            intent.putExtra(TAG_HASFILE, true);
                            mActivity.sendBroadcast(intent);
                        }
                    }
                    for (int i = mPosition; i < nextTitlePosition; i++) {
                        notifyItemChanged(i);
                    }
                }
            });
        }

        public void updateView(int position) {
            mPosition = position;
            mTime.setText(mMinuteFiles.get(position).hourTime+":00");
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (mMinuteFiles.size() == 0) {
            return TYPE_TITLE;
        }
        return mMinuteFiles.get(position).isTitle ? TYPE_TITLE : TYPE_CONTENT;
    }


    @Override
    public int getItemCount() {
        return mMinuteFiles == null ? 0 : mMinuteFiles.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return getItemViewType(position) == TYPE_TITLE ? ((GridLayoutManager) layoutManager).getSpanCount() : 1;
                }
            });
        }
    }
}
