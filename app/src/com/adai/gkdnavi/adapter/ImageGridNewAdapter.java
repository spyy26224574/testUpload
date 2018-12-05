package com.adai.gkdnavi.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoPreviewActivity;
import com.adai.gkdnavi.fragment.FileGridNewFragment;
import com.adai.gkdnavi.fragment.FileGridNewFragment1;
import com.adai.gkdnavi.fragment.square.AlbumNewFragment;
import com.adai.gkdnavi.utils.DownloadManager;
import com.adai.gkdnavi.utils.MinuteFileDownloadInfo;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.view.ProgressCircleView;
import com.bumptech.glide.Glide;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.HourFile;
import com.example.ipcamera.domain.MinuteFile;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.filepicker.imagebrowse.RemotePictureBrowseActivity;

import java.util.ArrayList;

/**
 * Created by huangxy on 2016/12/5.
 */

public class ImageGridNewAdapter extends BaseAdapter {
    private final String TAG = this.getClass().getSimpleName();
    private Activity mContext;
    private int mWidth;
    private int mType;
    private AllFileSelectedListener mAllFileSelectedListener;
    private HourFile mHourFile;
    public static final String TAG_HAFILE = "hasFile";

    public interface AllFileSelectedListener {
        void selected(boolean isSelected);

    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
    }

    private boolean isEditMode;

    public ImageGridNewAdapter(Activity context, HourFile hourFile, int type, AllFileSelectedListener allFileSelectedListener) {
        mContext = context;
        mHourFile = hourFile;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWidth = (windowManager.getDefaultDisplay().getWidth() - context.getResources().getDimensionPixelOffset(R.dimen.dimen__5) * 6) / 3;
        mType = type;
        mAllFileSelectedListener = allFileSelectedListener;
    }

    @Override
    public int getCount() {
        return mHourFile.minuteFiles == null ? 0 : mHourFile.minuteFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mHourFile.minuteFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.item_ablum_grid, null);
            holder = new ViewHolder(convertView);
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(mWidth, mWidth);
            convertView.setLayoutParams(layoutParams);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        int size = VoiceManager.selectedMinuteFile.size();
        for (MinuteFile minuteFile : mHourFile.minuteFiles) {
            if (minuteFile.isChecked) {
                VoiceManager.selectedMinuteFile.add(minuteFile);
            }else{
                VoiceManager.selectedMinuteFile.remove(minuteFile);
            }
        }
        if (size == 0 && VoiceManager.selectedMinuteFile.size() > 0) {
            Intent intent = new Intent(FileGridNewFragment1.ACTION_SELECTED_FILE);
            intent.putExtra(TAG_HAFILE, true);
            mContext.sendBroadcast(intent);
        }
        if (size > 0 && VoiceManager.selectedMinuteFile.size() == 0) {
            Intent intent = new Intent(FileGridNewFragment1.ACTION_SELECTED_FILE);
            intent.putExtra(TAG_HAFILE, false);
            mContext.sendBroadcast(intent);
        }
        holder.updateItem(mHourFile.minuteFiles.get(position));
        return convertView;
    }

    class ViewHolder implements MinuteFileDownloadManager.DownloadObserver {
        private ImageView mImageView;
        private ImageView mIvIcon, mIvLock;
        private TextView mTvTime, mTvNum;
        private RelativeLayout mRlSelectMark;
        private View mItemView;
        private MinuteFile mMinuteFile;
        private ProgressCircleView mProgressCircleView;

        public ViewHolder(View itemView) {
            MinuteFileDownloadManager.getInstance().addObserver(this);
            mItemView = itemView;
            mProgressCircleView = (ProgressCircleView) itemView.findViewById(R.id.item_btn_download);
            mProgressCircleView.setVisibility(mType == AlbumNewFragment.ALBUM_RECORDER ? View.VISIBLE : View.GONE);
            mImageView = (ImageView) itemView.findViewById(R.id.iv);
            mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mIvIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            mTvTime = (TextView) itemView.findViewById(R.id.tv_time);
            mTvNum = (TextView) itemView.findViewById(R.id.tv_num);
            mIvLock = (ImageView) itemView.findViewById(R.id.down_lock);
            mRlSelectMark = (RelativeLayout) itemView.findViewById(R.id.rl_select_mark);
        }

        void updateItem(MinuteFile minuteFile) {
            mMinuteFile = minuteFile;
            mIvIcon.setVisibility(mMinuteFile.fileDomains.get(0).isPicture ? View.GONE : View.VISIBLE);
            mIvLock.setVisibility(mMinuteFile.fileDomains.get(0).attr == 33 ? View.VISIBLE : View.GONE);
            if (mType == AlbumNewFragment.ALBUM_RECORDER) {
                mTvTime.setText(mMinuteFile.time.substring(11));
            } else {
                try {
                    mTvTime.setText(mMinuteFile.fileDomains.get(0).time.substring(11, 16));
                } catch (Exception ignored) {

                }
            }
            mTvNum.setText("" + mMinuteFile.fileDomains.size());
            mTvNum.setVisibility(mMinuteFile.fileDomains.size() == 1 ? View.GONE : View.VISIBLE);
            mRlSelectMark.setVisibility(mMinuteFile.isChecked ? View.VISIBLE : View.GONE);
            String path = mMinuteFile.fileDomains.get(0).getFpath();
            mItemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isEditMode) {
                        mMinuteFile.isChecked = true;
                        mAllFileSelectedListener.selected(false);
                        Intent intent = new Intent(FileGridNewFragment.ACTION_EDIT_MODE_CHANGE);
                        mContext.sendBroadcast(intent);
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
                                Intent intent = new Intent(FileGridNewFragment.ACTION_SELECTED_FILE);
                                intent.putExtra("hasFile", true);
                                mContext.sendBroadcast(intent);
                            }
                        } else {
                            VoiceManager.selectedMinuteFile.remove(mMinuteFile);
                            if (VoiceManager.selectedMinuteFile.size() == 0) {
                                Intent intent = new Intent(FileGridNewFragment.ACTION_SELECTED_FILE);
                                intent.putExtra("hasFile", false);
                                mContext.sendBroadcast(intent);
                            }
                        }

                        for (MinuteFile minuteFile : mHourFile.minuteFiles) {
                            if (!minuteFile.isChecked) {
                                allChecked = false;
                                break;
                            } else {
                                allChecked = true;
                            }
                        }
                        if (mHourFile.isChecked != allChecked) {
                            mAllFileSelectedListener.selected(allChecked);
                            mHourFile.isChecked = allChecked;
                        }
                    } else {
                        switch (mType) {
                            case AlbumNewFragment.ALBUM_LOCAL_FILE:
                            case AlbumNewFragment.ALBUM_PHONE://本地文件浏览
                                if (mMinuteFile.fileDomains.get(0).isPicture) {
                                    Intent intent = new Intent();
                                    intent.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_LOCAL);
                                    ArrayList<String> values = new ArrayList<>();
                                    for (FileDomain localPhotoFile : mMinuteFile.fileDomains) {
                                        values.add(localPhotoFile.fpath);
                                    }
                                    intent.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, values);
                                    intent.putExtra(PictureBrowseActivity.KEY_POSTION, 0);
                                    intent.setClass(mContext, PictureBrowseActivity.class);
                                    mContext.startActivityForResult(intent, FileGridNewFragment.REQUEST_FILE_DELETE);
                                } else {
                                    Intent intent = new Intent();
                                    ArrayList<FileDomain> videoDomains = new ArrayList<>();
                                    for (MinuteFile minuteFile : mHourFile.minuteFiles) {
                                        if (!minuteFile.fileDomains.get(0).isPicture) {
                                            videoDomains.add(minuteFile.fileDomains.get(0));
                                        }
                                    }
                                    int position = videoDomains.indexOf(mMinuteFile.fileDomains.get(0));
                                    intent.putExtra(VideoPreviewActivity.KEY_POSTION, position);
                                    intent.putExtra(VideoPreviewActivity.KEY_FILES, videoDomains);
                                    intent.putExtra(VideoPreviewActivity.KEY_TYPE, 1);
                                    intent.setClass(mContext, VideoPreviewActivity.class);
                                    mContext.startActivity(intent);
                                }
                                break;
                            case AlbumNewFragment.ALBUM_RECORDER://记录仪文件浏览
                                if (mMinuteFile.fileDomains.get(0).isPicture) {
                                    Intent picture = new Intent(mContext, RemotePictureBrowseActivity.class);
                                    picture.putExtra(RemotePictureBrowseActivity.KEY_MODE, RemotePictureBrowseActivity.MODE_NETWORK);
                                    ArrayList<String> fileList = new ArrayList<>();
                                    for (FileDomain fileDomain : mMinuteFile.fileDomains) {
                                        fileList.add(Contacts.BASE_HTTP_IP + fileDomain.fpath.substring(fileDomain.fpath.indexOf(":") + 1).replace("\\", "/"));
                                    }
                                    picture.putStringArrayListExtra(RemotePictureBrowseActivity.KEY_TOTAL_LIST, fileList);
                                    picture.putExtra(RemotePictureBrowseActivity.KEY_POSTION, 0);
                                    mContext.startActivityForResult(picture, FileGridNewFragment.REQUEST_FILE_DELETE);
                                } else {
                                    Intent intent = new Intent();
                                    ArrayList<FileDomain> videoDomains = new ArrayList<>();
                                    for (MinuteFile minuteFile : mHourFile.minuteFiles) {
                                        if (!minuteFile.fileDomains.get(0).isPicture) {
                                            //视频文件
                                            videoDomains.add(minuteFile.fileDomains.get(0));
                                        }
                                    }
                                    int position = videoDomains.indexOf(mMinuteFile.fileDomains.get(0));
                                    intent.putExtra(VideoPreviewActivity.KEY_POSTION, position);
                                    intent.putExtra(VideoPreviewActivity.KEY_FILES, videoDomains);
                                    intent.putExtra(VideoPreviewActivity.KEY_TYPE, 0);
                                    intent.setClass(mContext, VideoPreviewActivity.class);
                                    mContext.startActivity(intent);
                                }
                                break;
                        }
                    }
                }
            });
            mProgressCircleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    performProgressClick();
                }
            });
            if (mType == AlbumNewFragment.ALBUM_RECORDER) {
                if (mMinuteFile.fileDomains.get(0).isPicture) {
                    path = (Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1)).replace("\\", "/");
                } else {
                    path = (Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1)).replace("\\", "/") + Contacts.URL_GET_THUMBNAIL_END;
                }
            }
            Glide.with(mContext).load(path).dontAnimate().placeholder(R.drawable.default_image_holder).into(mImageView);
            if (mType == AlbumNewFragment.ALBUM_RECORDER) {
                MinuteFileDownloadManager.getInstance().addInfo(mMinuteFile);
                MinuteFileDownloadInfo minuteFileDownloadInfo = MinuteFileDownloadManager.getInstance().getMinuteFileDownloadInfo(mMinuteFile);
                saveUpdate(minuteFileDownloadInfo);
            }
        }

        @Override
        public void onDownloadStateChanged(MinuteFileDownloadInfo info) {
            if (mMinuteFile.hashCode() == info.key) {
                Log.e(TAG, "onDownloadStateChanged: progress:" + info.progress);
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
            int state = info.state;
            mProgressCircleView.setProgressEnable(false);
            switch (state) {
                case DownloadManager.STATE_NONE:
                    mProgressCircleView.setText(UIUtils.getContext().getString(R.string.action_not_download));
                    mProgressCircleView.setIcon(R.drawable.ic_download);
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
                    mProgressCircleView.setIcon(R.drawable.ic_downloadsuccess);
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
}
