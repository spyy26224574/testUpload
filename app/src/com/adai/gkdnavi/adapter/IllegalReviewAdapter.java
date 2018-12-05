package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkd.bean.square.ReviewBean;
import com.adai.gkd.bean.square.VideoDetailBean;
import com.adai.gkdnavi.LikeUserListActivity;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.GpsUtil;
import com.adai.gkdnavi.utils.TimeUtils;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.TextureMapView;
import com.baidu.mapapi.model.LatLng;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.ijk.media.activity.VideoActivity;
import com.ijk.media.widget.media.IjkVideoView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by huangxy on 2017/1/18.
 */

public class IllegalReviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = IllegalReviewAdapter.class.getSimpleName();
    private final List<ReviewBean> mValues;
    private VideoDetailBean data;
    private Context mContext;
    private static final int TYPE_HEADER = 0X11;
    private static final int TYPE_SECOND = 0X12;
    private OnReplyClick onReplyClick;
    private IjkVideoView playview;
    private TextureMapView mMapView;
    private ImageView mVideoLogo;
    private LinearLayout mBtnStatus;

    public void setOnReplyClick(OnReplyClick onReplyClick) {
        this.onReplyClick = onReplyClick;
    }

    public IllegalReviewAdapter(Context context, VideoDetailBean videoDetailBean) {

        mContext = context;
        data = videoDetailBean;
        mValues = videoDetailBean.reviewList;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.illegal_detail_head, parent, false);
            return new HeaderHolder(view);
        } else if (TYPE_SECOND == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_detail_second, null);
            return new SecondHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video_reply, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderHolder) {
            final HeaderHolder mholder = (HeaderHolder) holder;
            ImageLoaderUtil.getInstance().loadRoundImage(mContext, data.portrait, mholder.mUserLogo);
            if (data.pictureList != null && data.pictureList.size() > 2) {
                ImageLoaderUtil.getInstance().loadImage(mContext, data.pictureList.get(0), mholder.mIvIllegalPicture0);
                ImageLoaderUtil.getInstance().loadImage(mContext, data.pictureList.get(1), mholder.mIvIllegalPicture1);
                ImageLoaderUtil.getInstance().loadImage(mContext, data.pictureList.get(2), mholder.mIvIllegalPicture2);

                mholder.mIvIllegalPicture0.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pictureBrowse(0);
                    }
                });
                mholder.mIvIllegalPicture1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pictureBrowse(1);
                    }
                });
                mholder.mIvIllegalPicture2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pictureBrowse(2);
                    }
                });
            }
            ImageLoaderUtil.getInstance().loadImage(mContext, data.coverPicture, R.drawable.default_image_holder, mholder.mVideoLogo);
            mholder.mUserNickname.setText(data.nickname);
            mholder.mVideoDes.setText(data.des);
            mholder.mVideoView.setVideoPath(data.videoUrl);
            mholder.mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mholder.mVideoView.isPlaying()) {
                        mholder.mVideoView.pause();
                        mholder.mBtnStatus.setVisibility(View.VISIBLE);
                    }
                    return true;
                }
            });
            playview = mholder.mVideoView;
            mVideoLogo = mholder.mVideoLogo;
            mBtnStatus = mholder.mBtnStatus;
            mholder.mVideoTime.setText(String.format("%02d:%02d", data.videoTime / 60, data.videoTime % 60));
            mholder.mBtnStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mholder.mVideoView.start();
                    mholder.mBtnStatus.setVisibility(View.GONE);
                    mholder.mVideoLogo.setVisibility(View.GONE);
                    playview = mholder.mVideoView;
                }
            });
            mholder.mFullscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, VideoActivity.class);
                    intent.putExtra("type", 2);
                    intent.putExtra("videoPath", data.videoUrl);
                    intent.putExtra("position", mholder.mVideoView.getCurrentPosition());
                    mContext.startActivity(intent);
                    if (mholder.mVideoView.isPlaying()) {
                        mholder.mVideoView.pause();
                        mholder.mVideoLogo.setVisibility(View.VISIBLE);
                        mholder.mBtnStatus.setVisibility(View.VISIBLE);
                    }
                }
            });
            mholder.mTvCarLicense.setText(data.plateNumber);
            mholder.mTvIllegalLocation.setText(String.format("%s: %s", mContext.getString(R.string.illegal_location), data.illegalAddress));
            mholder.mTvIllegalTime.setText(String.format("%s: %s", mContext.getString(R.string.illegal_time), data.illegalDate));
            mholder.mUploadTime.setText(data.uploadDate);
            mholder.mTvIllegalType.setText(data.illegalType);
            final BaiduMap map = mholder.mBMapView.getMap();
            double[] doubles = GpsUtil.gps84_To_bd09(Double.valueOf(data.latitude), Double.valueOf(data.longitude));
            final LatLng latLng = new LatLng(doubles[0], doubles[1]);
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark_default);
            OverlayOptions overlayOptions = new MarkerOptions()
                    .position(latLng)
                    .icon(bitmap);
            map.addOverlay(overlayOptions);
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
            map.animateMapStatus(mapStatusUpdate);
            mholder.mIvMove2Location.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                    map.animateMapStatus(mapStatusUpdate);
                }
            });
            String approveState = mContext.getString(R.string.wait_check);
            switch (data.approveState) {
                case 10:
                    approveState = mContext.getString(R.string.wait_check);
                    break;
                case 20:
                    approveState = mContext.getString(R.string.checking);
                    break;
                case 30:
                case 50:
                    approveState = mContext.getString(R.string.check_rejected);

                    break;
                case 40:
                    approveState = mContext.getString(R.string.check_pass);
                    break;
            }
            mholder.mTvApproveState.setText(approveState);
        } else if (holder instanceof SecondHolder) {
            SecondHolder sHolder = (SecondHolder) holder;
            String browsetext = mContext.getResources().getString(R.string.format_browsenum, data.browseCount);
            sHolder.browse_count.setText(browsetext);
            String replytext = mContext.getResources().getString(R.string.format_reviewnum, data.replyCount);
            sHolder.reply_count.setText(replytext);
            sHolder.btn_like.setSelected("Y".equals(data.isLike));
            sHolder.btn_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onReplyClick != null) {
                        onReplyClick.onLike(data.resourceId);
                    }
                }
            });
            if (data.likeList != null && data.likeList.size() > 0) {
                sHolder.adapter = new LikeGridAdapter(data.likeList);
                sHolder.like_grid.setAdapter(sHolder.adapter);
                sHolder.like_count.setText(String.valueOf(data.likeCount));
                sHolder.like_grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent userPage = new Intent(mContext, PersonalPageActivity.class);
                        userPage.putExtra("userid", data.likeList.get(position).userId);
                        mContext.startActivity(userPage);
                    }
                });
            } else {
                sHolder.like_layout.setVisibility(View.GONE);
            }
            sHolder.like_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent likemore = new Intent(mContext, LikeUserListActivity.class);
                    likemore.putExtra("resourceid", data.resourceId);
                    mContext.startActivity(likemore);
                }
            });
        } else if (holder instanceof ViewHolder) {
            ViewHolder mholder = (ViewHolder) holder;
            mholder.mItem = mValues.get(position - 2);
            ImageLoader.getInstance().displayImage(mholder.mItem.portrait, mholder.user_head);
            mholder.user_nickname.setText(mholder.mItem.nickname);
            String level_text = String.format("%d%s", mholder.mItem.level, mContext.getResources().getString(R.string.floor)) + "  " + TimeUtils.getTimeStr(mContext, "yyyy-MM-dd kk:mm:ss", mholder.mItem.reviewTime);
            mholder.level_time.setText(level_text);
            if (!TextUtils.isEmpty(mholder.mItem.replyUserName)) {
                String text = "<html>" + mContext.getString(R.string.reply) + " <font color='red'>" + mholder.mItem.replyUserName + "</font> " + mholder.mItem.message + "</html>";
                mholder.reply_message.setText(Html.fromHtml(text));
            } else {
                mholder.reply_message.setText(mholder.mItem.message);
            }
            if (!"Y".equals(data.isReview)) {
                mholder.btn_reply.setVisibility(View.GONE);
            }
            mholder.btn_reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onReplyClick != null) {
                        ReviewBean item = ((ViewHolder) holder).mItem;
                        onReplyClick.onReply(item.userId, item.nickname);
                    }
                }
            });
        }
    }

    private void pictureBrowse(int position) {
        Intent picture = new Intent(mContext, PictureBrowseActivity.class);
        picture.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_NETWORK);
        picture.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, data.pictureList);
        picture.putExtra(PictureBrowseActivity.KEY_POSTION, position);
        mContext.startActivity(picture);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else if (position == 1) {
            return TYPE_SECOND;
        } else {
            return RecyclerView.INVALID_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        int count = mValues == null ? 0 : mValues.size();
        count += 2;
        return count;
    }

    public void addReview(List<ReviewBean> values) {
        if (mValues != null && values != null) {
            mValues.addAll(values);
        }
    }

    public void onActivityDestroy() {
        if (playview != null) {
            playview.stopPlayback();
            playview.release(true);
            playview.stopBackgroundPlay();
        }
    }

    public void onActivityPause() {
        if (playview != null) {
            if (playview.isPlaying()) {
                playview.pause();
            }
        }
        if (mVideoLogo != null) {
            mBtnStatus.setVisibility(View.VISIBLE);
        }
    }

    public interface OnReplyClick {
        void onReply(int toUserid, String nickname);

        void onLike(int resourceid);

        void onAttention(View v, int userid);
    }

    class HeaderHolder extends RecyclerView.ViewHolder {

        private ImageView mUserLogo;
        private TextView mUserNickname;
        private TextView mUploadTime;
        private TextView mTvCarLicense;
        private TextView mTvIllegalLocation;
        private TextView mTvIllegalTime;
        private TextView mVideoDes;
        private TextureMapView mBMapView;
        private IjkVideoView mVideoView;
        private ImageView mVideoLogo;
        private LinearLayout mBtnStatus;
        private ImageView mFullscreen;
        private TextView mVideoTime;
        private ImageView mIvIllegalPicture0;
        private ImageView mIvIllegalPicture1;
        private ImageView mIvIllegalPicture2;
        private TextView mTvIllegalType;
        private ImageView mIvMove2Location;
        private TextView mTvApproveState;

        public HeaderHolder(View itemView) {
            super(itemView);
            mTvApproveState = (TextView) itemView.findViewById(R.id.tv_approve_state);
            mUserLogo = (ImageView) itemView.findViewById(R.id.user_logo);
            mUserNickname = (TextView) itemView.findViewById(R.id.user_nickname);
            mUploadTime = (TextView) itemView.findViewById(R.id.upload_time);
            mTvCarLicense = (TextView) itemView.findViewById(R.id.tv_car_license);
            mTvIllegalLocation = (TextView) itemView.findViewById(R.id.tv_illegal_location);
            mTvIllegalTime = (TextView) itemView.findViewById(R.id.tv_illegal_time);
            mVideoDes = (TextView) itemView.findViewById(R.id.video_des);
            mBMapView = (TextureMapView) itemView.findViewById(R.id.bMapView);
            mMapView = mBMapView;
            mMapView.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(event.getAction() == MotionEvent.ACTION_UP){
                        mBMapView.getParent().requestDisallowInterceptTouchEvent(false);
                    }else{
                        mBMapView.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return false;
                }
            });
            mVideoView = (IjkVideoView) itemView.findViewById(R.id.video_view);
            mVideoLogo = (ImageView) itemView.findViewById(R.id.video_logo);
            mBtnStatus = (LinearLayout) itemView.findViewById(R.id.btn_status);
            mFullscreen = (ImageView) itemView.findViewById(R.id.fullscreen);
            mVideoTime = (TextView) itemView.findViewById(R.id.video_time);
            mIvIllegalPicture0 = (ImageView) itemView.findViewById(R.id.iv_illegal_picture0);
            mIvIllegalPicture1 = (ImageView) itemView.findViewById(R.id.iv_illegal_picture1);
            mIvIllegalPicture2 = (ImageView) itemView.findViewById(R.id.iv_illegal_picture2);
            mTvIllegalType = (TextView) itemView.findViewById(R.id.tv_illegal_type);
            mIvMove2Location = (ImageView) itemView.findViewById(R.id.move2location);
        }
    }

    class SecondHolder extends RecyclerView.ViewHolder {

        public final TextView browse_count;
        public final TextView reply_count;
        public final ImageView btn_like;
        public final GridView like_grid;
        public final TextView like_count;
        public final View like_layout;
        public final ImageView like_more;
        public LikeGridAdapter adapter;

        public SecondHolder(View itemView) {
            super(itemView);
            browse_count = (TextView) itemView.findViewById(R.id.browse_count);
            reply_count = (TextView) itemView.findViewById(R.id.reply_count);
            btn_like = (ImageView) itemView.findViewById(R.id.btn_like);
            like_grid = (GridView) itemView.findViewById(R.id.like_grid);
            like_count = (TextView) itemView.findViewById(R.id.likeNumber);
            like_layout = itemView.findViewById(R.id.more_like_layout);
            like_more = (ImageView) itemView.findViewById(R.id.like_more);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ReviewBean mItem;
        public final ImageView user_head;
        public final TextView user_nickname;
        public final TextView level_time;
        public final TextView btn_reply;
        public final TextView reply_message;

        public ViewHolder(View itemView) {
            super(itemView);
            user_head = (ImageView) itemView.findViewById(R.id.user_head);
            user_nickname = (TextView) itemView.findViewById(R.id.user_nickname);
            level_time = (TextView) itemView.findViewById(R.id.level_time);
            btn_reply = (TextView) itemView.findViewById(R.id.btn_reply);
            reply_message = (TextView) itemView.findViewById(R.id.reply_message);
        }
    }

    public TextureMapView getMapView() {
        return mMapView;
    }
}
