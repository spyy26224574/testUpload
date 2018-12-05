package com.adai.camera.mstar.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.novatek.filemanager.NovatekFileManagerFragment;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoPreviewActivity;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.fragment.FileGridNewFragment;
import com.adai.gkdnavi.utils.DownloadManager;
import com.adai.gkdnavi.utils.MinuteFileDownloadInfo;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.adai.gkdnavi.view.ProgressCircleView;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.filepicker.imagebrowse.RemotePictureBrowseActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import org.videolan.vlc.util.Strings;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/10/16 16:24.
 */

public class MstarFileAdapter extends RecyclerView.Adapter {
    private ArrayList<MinuteFile> mMinuteFiles;
    private Activity mActivity;
    private int mFileType;
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
                VoiceManager.selectedMinuteFile.clear();
                //退出编辑模式的时候全部变成非选中状态
                if (mMinuteFiles != null && mMinuteFiles.size() > 0) {
                    for (MinuteFile minuteFile : mMinuteFiles) {
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

    public MstarFileAdapter(Activity activity, ArrayList<MinuteFile> minuteFiles, int type) {
        mActivity = activity;
        mMinuteFiles = minuteFiles;
        mFileType = type;
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

    private DisplayImageOptions mDisplayImageOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_image_holder)
            .showImageForEmptyUri(R.drawable.default_image_holder)
            .showImageOnFail(R.drawable.default_image_holder)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .build();
    private DisplayImageOptions mDisplayVideoOptions = new DisplayImageOptions.Builder()
            .showImageOnLoading(R.drawable.default_video_holder)
            .showImageForEmptyUri(R.drawable.default_video_holder)
            .showImageOnFail(R.drawable.default_video_holder)
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .bitmapConfig(Bitmap.Config.RGB_565)
            .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
            .build();

    private class ContentViewHolder extends RecyclerView.ViewHolder implements MinuteFileDownloadManager.DownloadObserver {
        private ImageView mImageView;
        private ImageView mIvIcon, mIvLock;
        private TextView mTvTime, mTvNum, mTvSize;
        private RelativeLayout mRlSelectMark, mRlBottom;
        private View mItemView;
        private ProgressCircleView mProgressCircleView;
        private MinuteFile mMinuteFile;
        private int mPosition;

        public ContentViewHolder(View itemView) {
            super(itemView);
            MinuteFileDownloadManager.getInstance().addObserver(this);
            mItemView = itemView;
            mProgressCircleView = (ProgressCircleView) itemView.findViewById(R.id.item_btn_download);
            mProgressCircleView.setVisibility(mFileType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO ||
                    mFileType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO ? View.VISIBLE : View.GONE);
            mImageView = (ImageView) itemView.findViewById(R.id.iv);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_time);
            mTvNum = (TextView) itemView.findViewById(R.id.tv_num);
            mIvLock = (ImageView) itemView.findViewById(R.id.down_lock);
            mRlSelectMark = (RelativeLayout) itemView.findViewById(R.id.rl_select_mark);
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
//                    if (mFileType == NovatekFileManagerContract.Presenter.TYPE_REMOTE_URGENCY_VIDEO) {
//                        ToastUtil.showShortToast(mActivity, mActivity.getString(R.string.emergency_video_cannot_be_deleted));
//                        return true;
//                    }
                    if (!isEditMode) {
                        boolean allChecked = false;
                        mMinuteFile.isChecked = true;
                        VoiceManager.selectedMinuteFile.add(mMinuteFile);
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
//                        Intent intent = new Intent(FileGridNewFragment1.ACTION_SELECTED_FILE);
//                        intent.putExtra(TAG_HASFILE, true);
//                        mActivity.sendBroadcast(intent);
//                        Intent intent1 = new Intent(FileGridNewFragment1.ACTION_EDIT_MODE_CHANGE);
//                        mActivity.sendBroadcast(intent1);
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
                            VoiceManager.selectedMinuteFile.add(mMinuteFile);
                            if (VoiceManager.selectedMinuteFile.size() == 1) {
//                                Intent intent = new Intent(FileGridNewFragment1.ACTION_SELECTED_FILE);
//                                intent.putExtra(TAG_HASFILE, true);
//                                mActivity.sendBroadcast(intent);
                                if (mEventListener != null) {
                                    mEventListener.hasFile(true);
                                }
                            }
                        } else {
                            VoiceManager.selectedMinuteFile.remove(mMinuteFile);
                            if (VoiceManager.selectedMinuteFile.size() == 0) {
//                                Intent intent = new Intent(FileGridNewFragment1.ACTION_SELECTED_FILE);
//                                intent.putExtra(TAG_HASFILE, false);
//                                mActivity.sendBroadcast(intent);
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
                        switch (mFileType) {
                            case FileManagerConstant.TYPE_LOCAL_PICTURE:
                            case FileManagerConstant.TYPE_LOCAL_VIDEO://本地文件浏览
                                if (mMinuteFile.fileDomains.get(0).isPicture) {
                                    Intent intent = new Intent();
                                    intent.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_LOCAL);
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
                                    ArrayList<String> values = new ArrayList<>();
                                    for (int i = titlePosition + 1; i < nextTitlePosition; i++) {
                                        if (mMinuteFiles.get(i).fileDomains.get(0).isPicture) {
                                            values.add(mMinuteFiles.get(i).fileDomains.get(0).fpath);
                                        }
                                    }
                                    int position = values.indexOf(mMinuteFile.fileDomains.get(0).fpath);
                                    intent.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, values);
                                    intent.putExtra(PictureBrowseActivity.KEY_POSTION, position);
                                    intent.setClass(mActivity, PictureBrowseActivity.class);
                                    mActivity.startActivityForResult(intent, NovatekFileManagerFragment.REQUEST_FILE_DELETE);
                                } else {
                                    Intent intent = new Intent();
                                    ArrayList<FileDomain> videoDomains = new ArrayList<>();
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
                                        if (!mMinuteFiles.get(i).fileDomains.get(0).isPicture) {
                                            videoDomains.add(mMinuteFiles.get(i).fileDomains.get(0));
                                        }
                                    }
                                    int position = videoDomains.indexOf(mMinuteFile.fileDomains.get(0));
                                    intent.putExtra(VideoPreviewActivity.KEY_POSTION, position);
                                    intent.putExtra(VideoPreviewActivity.KEY_FILES, videoDomains);
                                    intent.putExtra(VideoPreviewActivity.KEY_TYPE, 1);
                                    intent.setClass(mActivity, VideoPreviewActivity.class);
                                    mActivity.startActivity(intent);
                                }
                                break;
                            case FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO://记录仪文件浏览
                            case FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO:
                            case FileManagerConstant.TYPE_REMOTE_PHOTO:
                            case FileManagerConstant.TYPE_REMOTE_ALL:
                                if (MinuteFileDownloadManager.isDownloading) {
                                    ToastUtil.showShortToast(mActivity, mActivity.getString(R.string.please_stop_download));
                                    return;
                                }
                                if (mMinuteFile.fileDomains.get(0).isPicture) {
                                    Intent picture = new Intent(mActivity, RemotePictureBrowseActivity.class);
                                    picture.putExtra(RemotePictureBrowseActivity.KEY_MODE, RemotePictureBrowseActivity.MODE_NETWORK);
                                    ArrayList<String> fileList = new ArrayList<>();
                                    for (FileDomain fileDomain : mMinuteFile.fileDomains) {
//                                        fileList.add(Contacts.BASE_HTTP_IP + fileDomain.fpath.substring(fileDomain.fpath.indexOf(":") + 1).replace("\\", "/"));
                                        fileList.add(fileDomain.baseUrl + fileDomain.getFpath());
                                    }
                                    picture.putStringArrayListExtra(RemotePictureBrowseActivity.KEY_TOTAL_LIST, fileList);
                                    picture.putExtra(RemotePictureBrowseActivity.KEY_POSTION, 0);
                                    mActivity.startActivityForResult(picture, FileGridNewFragment.REQUEST_FILE_DELETE);
                                } else {
                                    Intent intent = new Intent();
                                    ArrayList<FileDomain> videoDomains = new ArrayList<>();
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
                                        if (!mMinuteFiles.get(i).fileDomains.get(0).isPicture) {
                                            videoDomains.add(mMinuteFiles.get(i).fileDomains.get(0));
                                        }
                                    }
                                    int position = videoDomains.indexOf(mMinuteFile.fileDomains.get(0));
                                    intent.putExtra(VideoPreviewActivity.KEY_POSTION, position);
                                    intent.putExtra(VideoPreviewActivity.KEY_FILES, videoDomains);
                                    intent.putExtra(VideoPreviewActivity.KEY_TYPE, 0);
                                    intent.setClass(mActivity, VideoPreviewActivity.class);
                                    mActivity.startActivity(intent);
                                }
                                break;
                        }
                    }
                }
            });
        }

        void updateView(int position) {
            mPosition = position;
            this.mMinuteFile = mMinuteFiles.get(position);
//            mIvIcon.setVisibility(mMinuteFile.fileDomains.get(0).isPicture ? View.GONE : View.VISIBLE);
//            mIvLock.setVisibility(mMinuteFile.fileDomains.get(0).attr == 33 ? View.VISIBLE : View.GONE);
            if (mFileType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO || mFileType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO) {
                mTvTime.setText(mMinuteFile.time.substring(11, 19));
                mRlBottom.setVisibility(View.VISIBLE);
                mTvSize.setText(Strings.readableFileSize(mMinuteFile.fileDomains.get(0).getSize()));
            } else {
                try {
                    if (!mMinuteFile.fileDomains.get(0).isPicture) {
                        mTvTime.setText(mMinuteFile.fileDomains.get(0).time.substring(11));
                        mRlBottom.setVisibility(View.VISIBLE);
//                        LogUtils.e("fileSize = " + mMinuteFile.fileDomains.get(0).getSize());
                        mTvSize.setText(Strings.readableFileSize(mMinuteFile.fileDomains.get(0).getSize()));
                    } else {
                        mTvTime.setVisibility(View.GONE);
                        mRlBottom.setVisibility(View.GONE);
                    }
                } catch (Exception ignored) {
                }
            }
//            mTvNum.setText("" + mMinuteFile.fileDomains.size());
//            mTvNum.setVisibility(mMinuteFile.fileDomains.size() == 1 ? View.GONE : View.VISIBLE);
            mRlSelectMark.setVisibility(mMinuteFile.isChecked ? View.VISIBLE : View.GONE);
            String path = this.mMinuteFile.fileDomains.get(0).getFpath();
            int placeholder = R.drawable.default_video_holder;
            if (mMinuteFiles.get(position).fileDomains.get(0).isPicture) {
                placeholder = R.drawable.default_image_holder;
            }
            if ((mFileType & FileManagerConstant.TYPE_REMOTE_ALL) == mFileType) {
//                path = (Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1)).replace("\\", "/") + Contacts.URL_GET_THUMBNAIL_END;
                if (TextUtils.isEmpty(MstarCamera.CAM_IP)) {
                    MstarCamera.CAM_IP = CameraCommand.getCameraIp();
                }
//                if (path.indexOf("SD/") < 0) {//action do not has "SD/", CarDV has "SD/" in path
//                    url = new URL("http://" + CameraCommand.getCameraIp() + "/thumb"
//                            + path);
//                }else
//                    url = new URL("http://" + CameraCommand.getCameraIp() + "/thumb"
//                            + params[0].mFileNode.mName.substring(params[0].mFileNode.mName.lastIndexOf("SD") + 2));
                String fpath = mMinuteFiles.get(position).fileDomains.get(0).getFpath();
                if (TextUtils.isEmpty(MstarCamera.CAM_IP)) {
                    MstarCamera.CAM_IP = CameraCommand.getCameraIp();
                }
                if (!fpath.contains("SD/")) {
                    path = "http://" + MstarCamera.CAM_IP + "/thumb" + fpath;
                } else {
                    path = "http://" + MstarCamera.CAM_IP + "/thumb" + fpath.substring(fpath.lastIndexOf("SD") + 2);
                }
                ImageLoader.getInstance().displayImage(path, mImageView, mDisplayImageOptions);
                //                if (mMinuteFiles.get(position).fileDomains.get(0).isPicture) {
////                    path = (Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1)).replace("\\", "/");
////                } else {
//                    ImageLoader.getInstance().displayImage(path, mImageView, mDisplayImageOptions);
//                } else {
//                    ImageLoader.getInstance().displayImage(path, mImageView, mDisplayVideoOptions);
//                }
            } else {
                if (path.endsWith("head.jpg") || path.endsWith("logo.jpg")) {
                    ImageLoaderUtil.getInstance().loadImageWithoutCache(mActivity, path, placeholder, mImageView);
                } else {
                    ImageLoaderUtil.getInstance().loadImage(mActivity, path, placeholder, mImageView);
                }
            }
            if ((mFileType & FileManagerConstant.TYPE_REMOTE_ALL) == mFileType) {
                MinuteFileDownloadManager.getInstance().addInfo(mMinuteFile);
                MinuteFileDownloadInfo minuteFileDownloadInfo = MinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(mMinuteFile);
                saveUpdate(minuteFileDownloadInfo);
            }
        }

        @Override
        public void onDownloadStateChanged(MinuteFileDownloadInfo info) {
            if (mMinuteFile == null) return;
            if (mMinuteFile.hashCode() == info.key) {
                saveUpdate(info);
            }
        }

        private void saveUpdate(final MinuteFileDownloadInfo info) {
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    updateState(info);
                }
            });
        }

        private void updateState(MinuteFileDownloadInfo info) {
            if (mMinuteFile.hashCode() != info.key) return;
            int state = info.state;
            mProgressCircleView.setProgressEnable(false);
            mProgressCircleView.setVisibility(View.VISIBLE);
            mProgressCircleView.setClickable(true);
            switch (state) {
                case DownloadManager.STATE_NONE:
                    mProgressCircleView.setVisibility(View.GONE);
//                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.not_download));
//                    mProgressCircleView.setIcon(R.drawable.ic_download);
                    break;
                case DownloadManager.STATE_WAITTING:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_waitdownload));
                    mProgressCircleView.setIcon(R.drawable.ic_pause);
                    break;
                case DownloadManager.STATE_DOWNLOADING:
                    mProgressCircleView.setIcon(R.drawable.ic_pause);
                    mProgressCircleView.setProgressEnable(true);
                    int progress = info.progress;
                    mProgressCircleView.setProgress(progress);
                    mProgressCircleView.setText(progress + "%");// 百分比
                    break;
                case DownloadManager.STATE_PAUSE:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_continuedownload));
                    mProgressCircleView.setIcon(R.drawable.ic_resume);
                    break;
                case DownloadManager.STATE_DOWNLOADED:
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
                case DownloadManager.STATE_FAILED:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.wifi_retrydownload));
                    mProgressCircleView.setIcon(R.drawable.ic_redownload);
                    break;
                default:
                    break;
            }
        }

        // 下载按钮点击事件
        private void performProgressClick() {

            MinuteFileDownloadInfo minuteFileDownloadInfo = MinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(mMinuteFile);
            int state = minuteFileDownloadInfo.state;
            switch (state) {
                case DownloadManager.STATE_NONE:
                    MinuteFileDownloadManager.getInstance().download(mMinuteFile);
                    break;
                case DownloadManager.STATE_WAITTING:
                    MinuteFileDownloadManager.getInstance().cancel(mMinuteFile);
                    break;
                case DownloadManager.STATE_DOWNLOADING:
                    MinuteFileDownloadManager.getInstance().pause(mMinuteFile);
                    break;
                case DownloadManager.STATE_PAUSE:
                    MinuteFileDownloadManager.getInstance().download(mMinuteFile);
                    break;
                case DownloadManager.STATE_DOWNLOADED:
                    break;
                case DownloadManager.STATE_FAILED:
                    MinuteFileDownloadManager.getInstance().download(mMinuteFile);
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
                                VoiceManager.selectedMinuteFile.remove(mMinuteFiles.get(i));
                            }
                        }
                        boolean curHasFile = VoiceManager.selectedMinuteFile.size() > 0;
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
                                VoiceManager.selectedMinuteFile.add(mMinuteFiles.get(i));
                            }

                        }
                        boolean curHasFile = VoiceManager.selectedMinuteFile.size() > 0;
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
//            mTvSelect.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
//            mTvSelect.setText(mMinuteFiles.get(position).isTitleSelected ? mActivity.getString(R.string.cancel_select) : mActivity.getString(R.string.select));
            if ((mFileType & FileManagerConstant.TYPE_REMOTE_ALL) == mFileType) {
                mTime.setText(mMinuteFiles.get(position).parentTime);
            } else {
                mTime.setText(mMinuteFiles.get(position).hourTime);
            }
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
