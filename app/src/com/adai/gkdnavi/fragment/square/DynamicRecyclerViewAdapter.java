package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.adapter.ImageGridAdapter;
import com.adai.gkdnavi.fragment.square.TypeVideoFragment.OnListFragmentInteractionListener;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.TimeUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.ijk.media.activity.VideoActivity;
import com.ijk.media.widget.media.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TypeVideoBean} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class DynamicRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0X11;
    private String TAG = this.getClass().getName();
    private Activity mContext;
    private final List<TypeVideoBean> mValues;
    private List<LikeUserBean> recommandDatas = new ArrayList<>();
    private final OnListFragmentInteractionListener mListener;

    private ViewHolder lastPlay;

    public ViewHolder getLastPlay() {
        return lastPlay;
    }

    public DynamicRecyclerViewAdapter(Activity context, List<TypeVideoBean> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    public void addRecommand(List<LikeUserBean> recommandDatas) {
        if (recommandDatas == null) return;
        this.recommandDatas.clear();
        this.recommandDatas.addAll(recommandDatas);
        notifyItemChanged(0);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dynamic_header, parent, false);
            return new HeaderViewholder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_typevideo_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mholder, int position) {

        if (mholder instanceof DynamicRecyclerViewAdapter.ViewHolder) {
            final DynamicRecyclerViewAdapter.ViewHolder holder = (ViewHolder) mholder;
            holder.mItem = mValues.get(position);
            // TODO: 分享功能
            holder.mIvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(holder.mItem.shareAddress)) {
                        new ShareUtils().showShareDialog(mContext, holder.mItem.shareAddress, holder.mItem.des, holder.mItem.des, holder.mItem.coverPicture);
                    }
                }
            });
            holder.share_location.setText(holder.mItem.coordinate);
            holder.userNickname.setText(holder.mItem.nickname);
            holder.browse_count.setText(String.valueOf(holder.mItem.browseCount));
            holder.like_count.setText(String.valueOf(holder.mItem.likeCount));
            holder.reply_count.setText(String.valueOf(holder.mItem.replyCount));
            String datetext = TimeUtils.getTimeStr(mContext, "yyyy-MM-dd kk:mm:ss", holder.mItem.uploadDate);
            holder.video_upload.setText(datetext);
            holder.video_title.setText(holder.mItem.des);
//            ImageLoader.getInstance().displayImage(holder.mItem.portrait, holder.userLogo);
//            Glide.with(mContext).load(holder.mItem.portrait).placeholder(R.drawable.default_header_img).dontAnimate().into(holder.userLogo);
            ImageLoaderUtil.getInstance().loadRoundImage(mContext, holder.mItem.portrait, R.drawable.default_header_img, holder.userLogo);
            holder.video_progress.setVisibility(View.GONE);
            holder.userLogo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userPage = new Intent(mContext, PersonalPageActivity.class);
                    userPage.putExtra("userid", holder.mItem.userId);
                    mContext.startActivity(userPage);
                }
            });
            if ("100".equals(holder.mItem.fileType)) {
                holder.imageslistgrid.setVisibility(View.GONE);
                holder.video_time.setVisibility(View.VISIBLE);
                holder.video_view.setVisibility(View.VISIBLE);
                holder.video_logo.setVisibility(View.VISIBLE);
                holder.btn_status.setVisibility(View.VISIBLE);
                holder.fullscreen.setVisibility(View.VISIBLE);
//                ImageLoader.getInstance().displayImage(holder.mItem.coverPicture, holder.video_logo);
//                Glide.with(mContext).load(holder.mItem.coverPicture).placeholder(R.drawable.default_image_holder).into(holder.video_logo);
                ImageLoaderUtil.getInstance().loadImage(mContext, holder.mItem.coverPicture, R.drawable.default_image_holder, holder.video_logo);
//                holder.video_view.setVideoPath(holder.mItem.videoUrl);
                if (holder.video_view.isPlaying()) {
                    holder.video_view.pause();
                }
                holder.video_view.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(IMediaPlayer iMediaPlayer) {
                        holder.video_progress.setVisibility(View.GONE);
                    }
                });
                holder.video_view.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                        return true;
                    }
                });
                holder.video_view.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Log.e(DynamicRecyclerViewAdapter.class.getName(), "ontouch...");
                        if (holder.video_view.isPlaying()) {
                            holder.video_view.pause();
                            holder.btn_status.setVisibility(View.VISIBLE);
                            holder.video_progress.setVisibility(View.GONE);
                        }
                        return true;
                    }
                });
                holder.video_view.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                        switch (i) {
                            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                                Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                                holder.video_progress.setVisibility(View.VISIBLE);
                                break;
                            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                                Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                                holder.video_progress.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                });
                holder.video_view.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(IMediaPlayer iMediaPlayer) {
                        holder.btn_status.setVisibility(View.VISIBLE);
                    }
                });
                //去掉加载出错的提示

                holder.video_view.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                        holder.btn_status.setVisibility(View.VISIBLE);
                        holder.video_progress.setVisibility(View.GONE);
                        holder.video_logo.setVisibility(View.VISIBLE);
                        holder.video_view.release(true);
                        return true;
                    }
                });
                holder.video_time.setText(String.format("%02d:%02d", holder.mItem.videoTime / 60, holder.mItem.videoTime % 60));
                holder.btn_status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (lastPlay != null) {
                            if (lastPlay.video_view.getCurrentState() == IjkVideoView.STATE_PREPARING) {
                                ToastUtil.showShortToast(mContext, mContext.getString(R.string.player_is_prepare));
                                return;
                            }
                        }
                        if (!holder.video_view.isPlaying()) {
                            if (holder.video_view.getMediaPlayer() == null || !holder.mItem.videoUrl.equals(holder.video_view.getVideoPath())) {
                                holder.video_view.setVideoPath(holder.mItem.videoUrl);
                                holder.video_progress.setVisibility(View.VISIBLE);
                            }
                            holder.video_view.start();
                            holder.video_logo.setVisibility(View.GONE);
                            holder.btn_status.setVisibility(View.GONE);
                            if (lastPlay != null) {
                                if (!lastPlay.equals(holder)) {
                                    if (lastPlay.video_view.isPlaying()) {
                                        lastPlay.video_view.pause();
                                        lastPlay.btn_status.setVisibility(View.VISIBLE);
                                        lastPlay.video_view.stopPlayback();
                                        lastPlay.video_view.release(true);
                                        lastPlay.video_view.stopBackgroundPlay();
                                    }
                                }
                            }
                            lastPlay = holder;
                            RequestMethods_square.addSeeResource(((ViewHolder) mholder).mItem.resourceId, null);
                        }
                    }
                });
                holder.fullscreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mContext, VideoActivity.class);
                        intent.putExtra("videoPath", holder.mItem.videoUrl);
                        intent.putExtra("type", 2);
                        intent.putExtra("position", holder.video_view.getCurrentPosition());
                        if (holder.video_view.isPlaying()) {
                            holder.video_view.pause();
                            holder.btn_status.setVisibility(View.VISIBLE);
                            holder.video_logo.setVisibility(View.VISIBLE);
                        }
                        mContext.startActivity(intent);
                        if (holder.video_view.isPlaying()) {
                            holder.video_view.pause();
                        }
                    }
                });
            } else {
                holder.imageslistgrid.setVisibility(View.VISIBLE);
                holder.video_time.setVisibility(View.GONE);
                holder.video_view.setVisibility(View.GONE);
                holder.video_logo.setVisibility(View.GONE);
                holder.btn_status.setVisibility(View.GONE);
                holder.fullscreen.setVisibility(View.GONE);
                if (holder.mItem.thumbnailList != null) {
                    holder.gridAdapter = new ImageGridAdapter(holder.mView.getContext(), holder.mItem.thumbnailList);
                } else {
                    holder.gridAdapter = new ImageGridAdapter(holder.mView.getContext(), holder.mItem.pictureList);
                }
                holder.imageslistgrid.setAdapter(holder.gridAdapter);
                holder.imageslistgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent picture = new Intent(mContext, PictureBrowseActivity.class);
                        picture.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_NETWORK);
                        picture.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, holder.mItem.pictureList);
                        picture.putExtra(PictureBrowseActivity.KEY_POSTION, position);
                        mContext.startActivity(picture);
                        RequestMethods_square.addSeeResource(((ViewHolder) mholder).mItem.resourceId, null);
                    }
                });
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onListFragmentInteraction(holder.mItem);
                    }
                    Intent detail = new Intent(mContext, VideoDetailActivity.class);
                    detail.putExtra("resourceid", holder.mItem.resourceId);
                    detail.putExtra("fileType", holder.mItem.fileType);
                    mContext.startActivity(detail);
                }
            });
        } else if (mholder instanceof HeaderViewholder) {
            HeaderViewholder holder = (HeaderViewholder) mholder;
            holder.mView.setVisibility(View.GONE);
            holder.adapter = new RecommandListAdapter(mContext, recommandDatas);
            holder.recommandlist.setAdapter(holder.adapter);
        }
    }

    public void onPause() {
        if (lastPlay != null) {
            if (lastPlay.video_view.isPlaying()) {
                lastPlay.video_logo.setVisibility(View.VISIBLE);
                lastPlay.video_view.pause();
                lastPlay.btn_status.setVisibility(View.VISIBLE);
                lastPlay.video_progress.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 销毁播放,页面销毁时调用
     */
    public void onDestroy() {
        if (lastPlay != null) {
            if (lastPlay.video_view.isPlaying()) {
                lastPlay.video_view.stopPlayback();
            }
            lastPlay.video_view.release(true);
            lastPlay.video_view.stopBackgroundPlay();
            lastPlay.btn_status.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    @Override
    public int getItemViewType(int position) {
//        if(position==0){
//            return TYPE_HEADER;
//        }
        return super.getItemViewType(position);
    }

    public class HeaderViewholder extends RecyclerView.ViewHolder {

        public final View mView;
        public final ListView recommandlist;
        public RecommandListAdapter adapter;

        public HeaderViewholder(View itemView) {
            super(itemView);
            this.mView = itemView;
            recommandlist = (ListView) itemView.findViewById(R.id.recommandlist);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView mIvShare;
        public final View mView;
        public final ImageView userLogo;
        public final TextView userNickname;
        public final TextView share_location;
        public final TextView video_time;
        public final TextView video_upload;
        public final TextView video_title;
        public final IjkVideoView video_view;
        public final ImageView video_logo;
        public final ImageView btn_status;
        public final TextView browse_count;
        public final TextView like_count;
        public final TextView reply_count;
        public final TextView btn_reply;
        public final GridView imageslistgrid;
        public final ImageView fullscreen;
        public final ProgressBar video_progress;
        public TypeVideoBean mItem;
        public ImageGridAdapter gridAdapter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIvShare = (ImageView) view.findViewById(R.id.iv_share);
            userLogo = (ImageView) view.findViewById(R.id.userLogo);
            userNickname = (TextView) view.findViewById(R.id.userNickname);
            share_location = (TextView) view.findViewById(R.id.share_location);
            video_time = (TextView) view.findViewById(R.id.video_time);
            video_upload = (TextView) view.findViewById(R.id.video_upload);
            video_title = (TextView) view.findViewById(R.id.video_title);
            video_view = (IjkVideoView) view.findViewById(R.id.video_view);
            video_logo = (ImageView) view.findViewById(R.id.video_logo);
            btn_status = (ImageView) view.findViewById(R.id.btn_status);
            browse_count = (TextView) view.findViewById(R.id.browse_count);
            like_count = (TextView) view.findViewById(R.id.like_count);
            reply_count = (TextView) view.findViewById(R.id.reply_count);
            btn_reply = (TextView) view.findViewById(R.id.btn_reply);
            imageslistgrid = (GridView) view.findViewById(R.id.imageslistgrid);
            fullscreen = (ImageView) view.findViewById(R.id.fullscreen);
            video_progress = (ProgressBar) view.findViewById(R.id.video_progress);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + userNickname.getText() + "'";
        }
    }
}
