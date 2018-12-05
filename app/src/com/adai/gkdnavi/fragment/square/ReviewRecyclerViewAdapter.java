package com.adai.gkdnavi.fragment.square;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.camera.pano.PanoVideoActivity;
import com.adai.gkd.bean.square.ReviewBean;
import com.adai.gkd.bean.square.VideoDetailBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkdnavi.LikeUserListActivity;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.adapter.ImageGridAdapter;
import com.adai.gkdnavi.adapter.LikeGridAdapter;
import com.adai.gkdnavi.utils.TimeUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.bumptech.glide.Glide;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.ijk.media.activity.VideoActivity;
import com.ligo.medialib.PanoCamViewLocal;
import com.ligo.medialib.PanoCamViewOnline;
import com.ligo.medialib.opengl.VideoRenderHard;
import com.ligo.medialib.opengl.VideoRenderYuv;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;


/**
 * Created by admin on 2016/8/11.
 */
public class ReviewRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<ReviewBean> mValues;
    private VideoDetailBean data;
    private static final int TYPE_HEADER = 0X11;
    private static final int TYPE_SECOND = 0X12;
    private Context mContext;
    private OnReplyClick onReplyClick;
    private ImageView mVideo_logo;
    //    private RelativeLayout mBtn_status;
    private HeaderHolder lastPlay;
    private AppCompatSeekBar horizontal_seekbar;
//    private TextView horizontal_time;

    boolean slideByUser = false;
    int panoDisplayType = 0; //全景显示效果类型
    public static final int VIDEO_SHOW_TYPE_SRC_CIRCLE = 1;
    public static final int VIDEO_SHOW_TYPE_2_SCREEN = 2;
    public static final int VIDEO_SHOW_TYPE_4_SCREEN = 3;
    public static final int VIDEO_SHOW_TYPE_PLANE1 = 4;
    public static final int VIDEO_SHOW_TYPE_CYLINDER = 5;
    public int mtype = 0; //0，普通文件，1为360度文件，2位720度文件
    public int fishEyeId = 0; // 镜头ID
    public int width = 0; // 文件的宽
    public int height = 0; // 文件的高

    private static final int SHOW_PROGRESS = 1;
    private static final int SHOW_PROGRESS_SOFT = 2;

    private PanoCamViewLocal mPanoCamViewLocalView;
    private PanoCamViewOnline mPanoCamViewOnlineView;
    int currentPlayer = 0;

    public void setOnReplyClick(OnReplyClick onReplyClick) {
        this.onReplyClick = onReplyClick;
    }

    public ReviewRecyclerViewAdapter(Context context, VideoDetailBean data) {
        this.data = data;
        this.mContext = context;
        mValues = data.reviewList;
        mtype = data.videoType;
        fishEyeId = data.fishEyeId;
        width = data.width;
        height = data.height;
    }

    public void addReview(List<ReviewBean> values) {
        if (mValues != null && values != null) {
            mValues.addAll(values);
        }
    }

    public void onActivityDestroy() {
        Log.e("9527", "onActivityDestroy");
        destroy();
    }

    public void onActivityStop() {
        Log.e("9527", "onActivityStop");
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
//                    Log.e("9527", "SHOW_PROGRESS");
                    int pos = setProgress();
                    if (mPanoCamViewLocalView.isPlaying()) {
                        long delayMillis = 1000 - (pos % 1000);
                        sendEmptyMessageDelayed(SHOW_PROGRESS, delayMillis);
                    }
                    break;
                case SHOW_PROGRESS_SOFT:

                    lastPlay.iv_original.setClickable(true);
                    lastPlay.iv_front_back.setClickable(true);
                    lastPlay.iv_four_direct.setClickable(true);
                    lastPlay.iv_wide_single.setClickable(true);
                    lastPlay.iv_cylinder.setClickable(true);

                    lastPlay.mPbBuffer.setVisibility(View.GONE);
                    int current = mPanoCamViewOnlineView.getCurrent();
                    int duration = mPanoCamViewOnlineView.getDuration();
                    Log.e("9527", "SHOW_PROGRESS_SOFT current = " + current + ",duration = " + duration);
                    if (!slideByUser) {
                        if (current >= 0 && duration - current > 0) {
                            setProgress();
                        }
                        if (current == -1) {
                            if (horizontal_seekbar != null) {
                                horizontal_seekbar.setProgress(duration);
                            }

                            showTime(duration, duration);
                            lastPlay.changePlayState(false);
                        }
                    }
                    break;

            }
        }
    };

    public void destroy() {
        if (mPanoCamViewLocalView != null) {
            mPanoCamViewLocalView.stopPlay();
            mPanoCamViewLocalView.release();
        }

        if (mPanoCamViewOnlineView != null) {
            mPanoCamViewOnlineView.stopPlay();
            mPanoCamViewOnlineView.setInfoCallback(null);
            mPanoCamViewOnlineView.release();
        }
    }

    public void stop() {
        if (lastPlay != null && lastPlay.vertical_play != null && lastPlay.horizontal_seekbar != null) {
            lastPlay.vertical_play.setBackgroundResource(R.drawable.play_play_selector);
            lastPlay.horizontal_seekbar.setProgress(0);
        }

        if (mPanoCamViewLocalView != null) {
            mPanoCamViewLocalView.pause();
        }

        if (mPanoCamViewOnlineView != null) {
            mPanoCamViewOnlineView.stopPlay();
        }
    }


    public void onActivityResume() {
        Log.e("9527", "onActivityResume");
//        if (mPanoCamViewLocalView != null) {
////            if (mPanoCamViewLocalView.isStop()) {
////                lastPlay.vertical_play.setBackgroundResource(R.drawable.play_play_selector);
//////                mPanoCamViewLocal = new PanoCamViewLocal(mContext);
////                mPanoCamViewLocalView.setListener();
////                mPanoCamViewLocalView.setInfoCallback(myinfoCallback);
//////                showVideo(data.videoUrl);
////            }
//
//        }

//        if (!mPanoCamViewLocalView.isInit) {
//            mPanoCamViewLocalView.setVisibility(View.VISIBLE);
//            mPanoCamViewLocalView.reInit(mContext);
//            mPanoCamViewLocalView.setOnChangeListener(lastPlay);
//        }

    }

    public void onActivityPause() {
        Log.e("9527", "onActivityPause");

        if (mPanoCamViewLocalView != null) {
            if (mPanoCamViewLocalView.isPlaying()) {
                lastPlay.pause();
            }
        }
        if (mPanoCamViewOnlineView != null) {
            mPanoCamViewOnlineView.stopPlay();
        }
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

    public void onScrollStateChanged() {
//        if (mAndroidMediaController != null) {
//            mAndroidMediaController.hide();
//        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (TYPE_HEADER == viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_detail_header, null);
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
//            ImageLoader.getInstance().displayImage(data.portrait,mholder.user_logo);
//            ImageLoadHelper.getInstance().displayImage(data.portrait,mholder.user_logo,R.drawable.default_header_img);
//            Glide.with(mContext).load(data.portrait).placeholder(R.drawable.default_header_img).into(mholder.user_logo);
            ImageLoaderUtil.getInstance().loadRoundImage(mContext, data.portrait, R.drawable.default_header_img, mholder.user_logo);
            mholder.user_nickname.setText(data.nickname);
            mholder.share_location.setText(data.coordinate);
            mholder.video_des.setText(data.des);
            if (data.uploadDate != null && data.uploadDate.length() > 10) {
                mholder.upload_time.setText(data.uploadDate.substring(0, data.uploadDate.length() - 2));
            }
            if (VoiceManager.isLogin && CurrentUserInfo.id == data.userId) {
                mholder.concern.setVisibility(View.GONE);
            } else {
                if ("Y".equals(data.isFocus)) {
                    mholder.concern.setSelected(true);
                    mholder.concern.setText(mContext.getString(R.string.already_attention));
                } else {
                    mholder.concern.setSelected(false);
                    mholder.concern.setText(mContext.getString(R.string.add_attention));
                }
            }
            mholder.concern.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onReplyClick != null) {
                        onReplyClick.onAttention(v, data.userId);
                    }
                }
            });
            mholder.user_logo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent userPage = new Intent(mContext, PersonalPageActivity.class);
                    userPage.putExtra("userid", data.userId);
                    mContext.startActivity(userPage);
                }
            });
            if ("100".equals(data.fileType)) { //视频
//                mholder.mPanoCamViewLocal.setVisibility(View.VISIBLE);
                mholder.fullscreen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stop();
                        Intent intent = null;
                        Log.e("9527", "mtype = " + mtype);
                        if (mtype == 1) {
                            intent = new Intent(mContext, PanoVideoActivity.class);
                        } else if (mtype == 0) {
                            intent = new Intent(mContext, VideoActivity.class);
                        }

                        intent.putExtra("videoType", mtype);
                        intent.putExtra("fishEyeId", fishEyeId);
                        intent.putExtra("width", width);
                        intent.putExtra("height", height);

                        intent.putExtra("type", 2);
                        intent.putExtra("videoPath", data.videoUrl);
                        mContext.startActivity(intent);
//                        if (holder instanceof HeaderHolder) {
//                            if (((HeaderHolder) holder).videoView.isPlaying()) {
//                                ((HeaderHolder) holder).videoView.pause();
//                                mVideo_logo.setVisibility(View.VISIBLE);
//                                mBtn_status.setVisibility(View.VISIBLE);
//                            }
//                        }
                    }
                });

//                if (mtype == 0) {
////                    mholder.videoView.setVisibility(View.VISIBLE);
////                    mholder.ll_video_frame.setVisibility(View.GONE);
//                    mholder.ll_pano_type.setVisibility(View.GONE);
////                    mholder.iv_decoding_type.setVisibility(View.GONE);
//                    mholder.horizontal_bottom.setVisibility(View.GONE);
//                    mholder.vertical_play.setVisibility(View.GONE);
//
//                    mholder.videoView.setVideoPath(data.videoUrl);
//                    mholder.video_time.setText(String.format("%02d:%02d", data.videoTime / 60, data.videoTime % 60));
//                    mAndroidMediaController = new AndroidMediaController(mContext, false);
//                    mholder.videoView.setMediaController(mAndroidMediaController);
//                    playview = mholder.videoView;
//                    lastPlay = mholder;
//                    if (holder instanceof HeaderHolder) {
//                        mVideo_logo = ((HeaderHolder) holder).video_logo;
//                        mBtn_status = ((HeaderHolder) holder).btn_status;
//                    }
//                    Glide.with(mContext).load(data.coverPicture).placeholder(R.drawable.default_image_holder).into(mholder.video_logo);
//
//
//                    mholder.btn_status.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ((HeaderHolder) holder).videoView.start();
//                            ((HeaderHolder) holder).btn_status.setVisibility(View.GONE);
//                            ((HeaderHolder) holder).video_logo.setVisibility(View.GONE);
//                            playview = ((HeaderHolder) holder).videoView;
//                        }
//                    });
//
//                } else if (mtype == 1) {

                if ((mtype & fishEyeId) == 0) {
                    mholder.ll_pano_type.setVisibility(View.GONE);
                } else {
                    mholder.ll_pano_type.setVisibility(View.VISIBLE);
                }

                mholder.video_time.setText(String.format("%02d:%02d", data.videoTime / 60, data.videoTime % 60));
                mPanoCamViewLocalView = mholder.mPanoCamViewLocal;
                mPanoCamViewOnlineView = mholder.mPanoCamViewOnline;
                horizontal_seekbar = mholder.horizontal_seekbar;
//                horizontal_time = mholder.horizontal_time;

                horizontal_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                        Log.e("9527", "onProgressChanged:progress = " + progress + " fromUser = " + fromUser);
                        if (!fromUser) return;

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        Log.e("9999", "onStartTrackingTouch: ");
                        slideByUser = true;

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        int progress = seekBar.getProgress();
                        Log.e("9527", "onStopTrackingTouch: progress=" + progress);
                        if (currentPlayer == 0) {
                            mPanoCamViewLocalView.seek(progress);
                        } else {
                            mPanoCamViewOnlineView.seek(progress);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                slideByUser = false;
                            }
                        }, 200);

                    }
                });


//                    mholder.videoView.setVideoPath(data.videoUrl);
//                    mholder.video_time.setText(String.format("%02d:%02d", data.videoTime / 60, data.videoTime % 60));
//                    mAndroidMediaController = new AndroidMediaController(mContext, false);
//                    mholder.videoView.setMediaController(mAndroidMediaController);
//                    playview = mholder.videoView;
                lastPlay = mholder;
                if (holder instanceof HeaderHolder) {
                    mVideo_logo = ((HeaderHolder) holder).video_logo;
//                    mBtn_status = ((HeaderHolder) holder).btn_status;
                }
//                ImageLoader.getInstance().displayImage(data.coverPicture,mholder.video_logo);
                Glide.with(mContext).load(data.coverPicture).placeholder(R.drawable.default_image_holder).into(mholder.video_logo);

                mholder.btn_status.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((HeaderHolder) holder).btn_status.setVisibility(View.GONE);
                        ((HeaderHolder) holder).video_logo.setVisibility(View.GONE);
                        mholder.horizontal_bottom.setVisibility(View.VISIBLE);
//                            mPanoCamViewLocal.setInfoCallback(myinfoCallback);
                        mholder.mPbBuffer.setVisibility(View.VISIBLE);
                        showVideo(data.videoUrl);
                    }
                });
//                }
            } else if ("200".equals(data.fileType)) { //图片
                mholder.fullscreen.setVisibility(View.GONE);
                mholder.mPanoCamViewLocal.setVisibility(View.GONE);
                mholder.mPanoCamViewOnline.setVisibility(View.GONE);
                mholder.ll_pano_type.setVisibility(View.GONE);
//                mholder.ll_video_frame.setVisibility(View.GONE);
                mholder.video_logo.setVisibility(View.GONE);
//                mholder.videoView.setVisibility(View.GONE);
                mholder.video_time.setVisibility(View.GONE);
                mholder.btn_status.setVisibility(View.GONE);
                mholder.mPbBuffer.setVisibility(View.GONE);
                mholder.imageslistgrid.setVisibility(View.VISIBLE);
                if (data.thumbnailList != null) {
                    mholder.imageslistgrid.setAdapter(new ImageGridAdapter(mContext, data.thumbnailList));
                } else {
                    mholder.imageslistgrid.setAdapter(new ImageGridAdapter(mContext, data.pictureList));
                }
                mholder.imageslistgrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent picture = new Intent(mContext, PictureBrowseActivity.class);
                        picture.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_NETWORK);
                        picture.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, data.pictureList);
                        picture.putExtra(PictureBrowseActivity.KEY_POSTION, position);
                        mContext.startActivity(picture);
                    }
                });
//                mholder.fullscreen.setVisibility(View.GONE);
                mholder.horizontal_bottom.setVisibility(View.GONE);
            }
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
//            if(headView!=null){
//                mholder.mItem=mValues.get(position-1);
//            }else {
//                mholder.mItem =mValues.get(position);
//            }
            mholder.mItem = mValues.get(position - 2);
//            ImageLoader.getInstance().displayImage(mholder.mItem.portrait,mholder.user_head);
            ImageLoaderUtil.getInstance().loadRoundImage(mContext, mholder.mItem.portrait, R.drawable.default_header_img, mholder.user_head);
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

    @Override
    public int getItemCount() {
        int count = mValues == null ? 0 : mValues.size();
        count += 2;
        return count;
    }

    class HeaderHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PanoCamViewLocal.OnChangeListener {

        public final ImageView user_logo;
        public final TextView user_nickname;
        public final TextView share_location;
        public final GridView imageslistgrid;
        public final TextView concern;
        public final TextView video_des;
        public final ImageView video_logo;
        public final TextView video_time;
        public final ImageView btn_status;
        public final ProgressBar mPbBuffer;
        public final ImageView fullscreen;
        public final TextView upload_time;

        public final PanoCamViewLocal mPanoCamViewLocal;
        public final PanoCamViewOnline mPanoCamViewOnline;
        public final LinearLayout ll_pano_type;
        public final ImageView iv_original;
        public final ImageView iv_front_back;
        public final ImageView iv_four_direct;
        public final ImageView iv_wide_single;
        public final ImageView iv_cylinder;
        public final LinearLayout horizontal_bottom;
        public final ImageView vertical_play;
        public final AppCompatSeekBar horizontal_seekbar;
        public final TextView horizontal_time;


        public HeaderHolder(View itemView) {
            super(itemView);
            user_logo = (ImageView) itemView.findViewById(R.id.user_logo);
            user_nickname = (TextView) itemView.findViewById(R.id.user_nickname);
            share_location = (TextView) itemView.findViewById(R.id.share_location);
            concern = (TextView) itemView.findViewById(R.id.concern);
            video_des = (TextView) itemView.findViewById(R.id.video_des);
//            videoView = (IjkVideoView) itemView.findViewById(R.id.video_view);
            video_logo = (ImageView) itemView.findViewById(R.id.video_logo);
            video_time = (TextView) itemView.findViewById(R.id.video_time);
            btn_status = (ImageView) itemView.findViewById(R.id.btn_status);
            mPbBuffer = (ProgressBar) itemView.findViewById(R.id.pb_buffer);
            fullscreen = (ImageView) itemView.findViewById(R.id.fullscreen);

            imageslistgrid = (GridView) itemView.findViewById(R.id.imageslistgrid);
            upload_time = (TextView) itemView.findViewById(R.id.upload_time);

            mPanoCamViewLocal = (PanoCamViewLocal) itemView.findViewById(R.id.pv_video);
            mPanoCamViewLocal.setOnChangeListener(this);

            mPanoCamViewOnline = (PanoCamViewOnline) itemView.findViewById(R.id.pv_video_soft);
            mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);


            ll_pano_type = (LinearLayout) itemView.findViewById(R.id.ll_pano_type);

            iv_original = (ImageView) itemView.findViewById(R.id.iv_original);
            iv_front_back = (ImageView) itemView.findViewById(R.id.iv_front_back);
            iv_four_direct = (ImageView) itemView.findViewById(R.id.iv_four_direct);
            iv_wide_single = (ImageView) itemView.findViewById(R.id.iv_wide_single);
            iv_cylinder = (ImageView) itemView.findViewById(R.id.iv_cylinder);
            iv_original.setOnClickListener(this);
            iv_front_back.setOnClickListener(this);
            iv_four_direct.setOnClickListener(this);
            iv_wide_single.setOnClickListener(this);
            iv_cylinder.setOnClickListener(this);

            iv_original.setClickable(false);
            iv_front_back.setClickable(false);
            iv_four_direct.setClickable(false);
            iv_wide_single.setClickable(false);
            iv_cylinder.setClickable(false);

            vertical_play = (ImageView) itemView.findViewById(R.id.vertical_play);
            vertical_play.setOnClickListener(this);
            horizontal_bottom = (LinearLayout) itemView.findViewById(R.id.horizontal_bottom);
            horizontal_seekbar = (AppCompatSeekBar) itemView.findViewById(R.id.horizontal_seekbar);
            horizontal_time = (TextView) itemView.findViewById(R.id.horizontal_time);
        }

        private PanoCamViewOnline.MediaInfoCallback mMediaInfoCallback = new PanoCamViewOnline.MediaInfoCallback() {
            @Override
            public void onInfo(PanoCamViewOnline.States state, String info) {
                Log.e("9999", "onInfo state = " + state);
                switch (state) {
                    case STATUS_STOP:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                mPanoCamViewOnline.stopPlay();
                            }
                        }).start();
                        break;
                    case STATUS_PLAY:
                    case STATUS_PAUSE:
                    case STATUS_ERROR:
                        break;
                    default:
                        break;
                }
            }


            @Override
            public void onUpdateFrame(final byte[] data, final int width, final int height, final int type) {
//            Log.e("9999", "onUpdateFrame  type = " + type);

                handler.sendEmptyMessage(SHOW_PROGRESS_SOFT);

            }

            @Override
            public void onScreenShot(boolean sucess, String url) {
            }

        };


        private void pause() {
            Log.e("9999", "pause");
            if (currentPlayer == 0) {
                mPanoCamViewLocal.pause();
            } else {
                mPanoCamViewOnline.pause();
            }
            changePlayState(false);
        }

        private void resume() {
            Log.e("9999", "resume");
            if (currentPlayer == 0) {
                mPanoCamViewLocal.resume();
            } else {
                mPanoCamViewOnline.resume();
            }
            changePlayState(true);
        }

        private void play() {
            if (currentPlayer == 0) {
                if (!mPanoCamViewLocal.isPlaying()) {
                    mPanoCamViewLocal.resume();
                }
            } else {
                mPanoCamViewOnline.startPlay(1);
            }
        }

        public void setPanoType(int panoType) {
            iv_original.setSelected(panoType == VIDEO_SHOW_TYPE_SRC_CIRCLE);
            iv_front_back.setSelected(panoType == VIDEO_SHOW_TYPE_2_SCREEN);
            iv_four_direct.setSelected(panoType == VIDEO_SHOW_TYPE_4_SCREEN);
            iv_wide_single.setSelected(panoType == VIDEO_SHOW_TYPE_PLANE1);
            iv_cylinder.setSelected(panoType == VIDEO_SHOW_TYPE_CYLINDER);
        }


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.vertical_play:
                    Log.e("9999", "isPlaying = " + mPanoCamViewLocal.isPlaying());
                    if (currentPlayer == 0) {
                        if (mPanoCamViewLocal.isPlaying()) {
                            pause();
                        } else {
                            resume();
                        }
                    } else {
                        if (mPanoCamViewOnline.isPlaying()) {
                            pause();
                        } else if (mPanoCamViewOnline.isPause()) {
                            resume();
                        } else {
                            play();
                        }

                    }
                    break;
                case R.id.iv_original:
                    panoDisplayType = 1;
                    setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
                    if (currentPlayer == 0) {
                        mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CIRCLE);
                    } else {
                        mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CIRCLE);
                    }
                    break;
                case R.id.iv_front_back:
                    panoDisplayType = 2;
                    setPanoType(VIDEO_SHOW_TYPE_2_SCREEN);
                    if (currentPlayer == 0) {
                        mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_2_SCREEN);
                    } else {
                        mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_2_SCREEN);
                    }
                    break;
                case R.id.iv_four_direct:
                    panoDisplayType = 3;
                    setPanoType(VIDEO_SHOW_TYPE_4_SCREEN);
                    if (currentPlayer == 0) {
                        mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_4_SCREEN);
                    } else {
                        mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_4_SCREEN);
                    }
                    break;
                case R.id.iv_wide_single:
                    panoDisplayType = 4;
                    setPanoType(VIDEO_SHOW_TYPE_PLANE1);
                    if (currentPlayer == 0) {
                        mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_SRC);
                    } else {
                        mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_SRC);
                    }
                    break;
                case R.id.iv_cylinder:
                    panoDisplayType = 5;
                    setPanoType(VIDEO_SHOW_TYPE_CYLINDER);
                    if (currentPlayer == 0) {
                        mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CYLINDER);
                    } else {
                        mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CYLINDER);
                    }
                    break;
            }
        }

        @Override
        public void onLoadComplete(int ret) {
            horizontal_seekbar.setProgress(0);
            horizontal_seekbar.setMax(mPanoCamViewLocal.getDuration());
            changePlayState(true);
            iv_original.setClickable(true);
            iv_front_back.setClickable(true);
            iv_four_direct.setClickable(true);
            iv_wide_single.setClickable(true);
            iv_cylinder.setClickable(true);

            if ((mtype & fishEyeId) == 0) {
                mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_SRC);
            } else {
                mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_SRC);
                lastPlay.setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
            }

        }

        @Override
        public void onBuffering(int percent) {
            Log.e("9999", "percent = " + percent);

        }

        @Override
        public void onSeekComplete() {
            mPbBuffer.setVisibility(View.GONE);


        }

        @Override
        public void onError(String errorMessage) {
            Log.e("9527", "onError ");
            mPanoCamViewLocal.setVisibility(View.GONE);
            mPanoCamViewLocal.stopPlay();
            mPanoCamViewLocal.changePlayer();

            iv_original.setClickable(false);
            iv_front_back.setClickable(false);
            iv_four_direct.setClickable(false);
            iv_wide_single.setClickable(false);
            iv_cylinder.setClickable(false);

            currentPlayer = 1;

            mPanoCamViewOnline.setVisibility(View.VISIBLE);
            mPanoCamViewOnline.reInit();
            mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
            showVideoSoft(data.videoUrl);

        }

        @Override
        public void onEnd() {
            Log.e("9527", "onEnd ");
            int duration = mPanoCamViewLocal.getDuration();
            if (horizontal_seekbar != null) {
                horizontal_seekbar.setProgress(duration);
            }
            showTime(duration, duration);
            changePlayState(false);

        }

        @Override
        public void onInfo(int what) {
            Log.e("9999", "what = " + what);
            switch (what) {
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    mPbBuffer.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    mPbBuffer.setVisibility(View.VISIBLE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    mPbBuffer.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                    break;
                default://不支持硬解码 切换为软解码重新播放
                    mPanoCamViewLocal.setVisibility(View.GONE);
                    mPanoCamViewLocal.stopPlay();
                    mPanoCamViewLocal.changePlayer();

                    iv_original.setClickable(false);
                    iv_front_back.setClickable(false);
                    iv_four_direct.setClickable(false);
                    iv_wide_single.setClickable(false);
                    iv_cylinder.setClickable(false);

                    currentPlayer = 1;

                    mPanoCamViewOnline.setVisibility(View.VISIBLE);
                    mPanoCamViewOnline.reInit();
                    mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
                    showVideoSoft(data.videoUrl);
                    break;
            }


        }

        private void changePlayState(boolean isPlaying) {
            Log.e("9527", "changePlayState isPlaying = " + isPlaying);
            handler.removeMessages(SHOW_PROGRESS);
            if (isPlaying) {
                vertical_play.setBackgroundResource(R.drawable.play_pause_selector);
                handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
            } else {
                vertical_play.setBackgroundResource(R.drawable.play_play_selector);
            }
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

    public interface OnReplyClick {
        void onReply(int toUserid, String nickname);

        void onLike(int resourceid);

        void onAttention(View v, int userid);
    }

    private int setProgress() {
        int current;
        int duration;
        if (currentPlayer == 0) {
            current = mPanoCamViewLocalView.getCurrent();
            duration = mPanoCamViewLocalView.getDuration();
        } else {
            current = mPanoCamViewOnlineView.getCurrent();
            duration = mPanoCamViewOnlineView.getDuration();
        }
//        Log.w("9527", "currentPlayer = " + currentPlayer + "duration = " + duration + " current = " + current);
        if (duration < 0)
            return 0;
        if (horizontal_seekbar != null) {
            horizontal_seekbar.setMax(duration);
            horizontal_seekbar.setProgress(current);
        }

        showTime(current, duration);
        return current;

    }

    private void showTime(long play_time, long total_time) {
        if (total_time <= 0) return;
        if (play_time > total_time) {
            play_time = total_time;
        }
        int play_hour = (int) (play_time / 1000 / 60 / 60);
        int play_minitu = (int) ((play_time / (60 * 1000)) % 60);
        int play_seconds = (int) ((play_time / 1000) % 60);
        int total_hour = (int) (total_time / 1000 / 60 / 60);
        int total_minitu = (int) ((total_time / (60 * 1000)) % 60);
        int total_seconds = (int) ((total_time / 1000) % 60);
        String text = String.format("%02d:%02d/%02d:%02d", play_minitu, play_seconds, total_minitu, total_seconds);
        lastPlay.horizontal_time.setText(text);
    }

    private void showVideo(String video_path) {
        if (video_path == null) return;
        mPanoCamViewLocalView.startPlay(video_path);

    }

    private void showVideoSoft(String video_path) {
        if (video_path == null) return;
        mPanoCamViewOnlineView.setUrl(video_path);
        mPanoCamViewOnlineView.setInfoCallback(lastPlay.mMediaInfoCallback);
        lastPlay.changePlayState(true);

        mPanoCamViewOnlineView.startPlay(1);

        if ((mtype & fishEyeId) == 0) {
            lastPlay.ll_pano_type.setVisibility(View.GONE);
            mPanoCamViewOnlineView.onChangeShowType(VideoRenderYuv.TYPE_SRC);
        } else {
            lastPlay.ll_pano_type.setVisibility(View.VISIBLE);
            lastPlay.setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
            mPanoCamViewOnlineView.onChangeShowType(VideoRenderYuv.TYPE_CIRCLE);
        }

    }


}
