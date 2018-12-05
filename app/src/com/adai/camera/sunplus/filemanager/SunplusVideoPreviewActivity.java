package com.adai.camera.sunplus.filemanager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.camera.sunplus.SDKAPI.CameraAction;
import com.adai.camera.sunplus.SDKAPI.VideoPlayback;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.widget.VideoPbMjpg;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.TimeUtils;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.type.ICatchEvent;
import com.icatch.wificam.customer.type.ICatchEventID;
import com.icatch.wificam.customer.type.ICatchFile;
import com.widget.piechart.ScreenUtils;


public class SunplusVideoPreviewActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    public static final String KEY_POSTION = "KEY_POSTION";
    private static final int EVENT_VIDEO_PLAY_COMPLETED = 1;
    private static final int EVENT_CACHE_STATE_CHANGED = 2;
    private static final int EVENT_CACHE_PROGRESS_NOTIFY = 3;
    private static final int EVENT_MEDIA_STREAM_CLOSE = 4;
    private static final String TAG = "SunplusVideoPreview";

    private RelativeLayout mActivityVideoPreview, rl_video_view;
    private VideoPbMjpg mVideoView;
    private LinearLayout mVerticalFrame;
    private ImageButton mVerticalMenu;
    private ImageButton mIbVerticalGps;
    private ImageView mVerticalPrevious;
    private ImageView mVerticalPlay;
    private ImageView mVerticalNext;
    private AppCompatSeekBar mVerticalSeekbar;
    private TextView mVerticalTime;
    private ListView mVerticalList;
    private LinearLayout mHeadFrame;
    private TextView mTitle;
    private ImageButton mHorizontalIbGps;
    private ImageView mIvIllegalReport;
    private ImageView mVideoCut;
    private RelativeLayout mHorizontalFrame;
    private LinearLayout mHorizontalBottom;
    private ImageButton mHorizontalMenu;
    private ImageView mHorizontalPrevious;
    private ImageView mHorizontalPlay;
    private ImageView mHorizontalNext;
    private AppCompatSeekBar mHorizontalSeekbar;
    private TextView mHorizontalTime;
    private ListView mHorizontalList;
    private ICatchFile currentFile;
    private ListAdapter horizontal_adapter;
    private ListAdapter vertical_adapter;
    private VideoPlayback mVideoPlayback = VideoPlayback.getInstance();
    private int curMode;
    private final int MODE_VIDEO_STOP = 0;
    private final int MODE_VIDEO_PLAYING = 1;
    private final int MODE_VIDEO_PAUSE = 2;
    private VideoEventListener mVideoEventListener;
    private ProgressBar mProgressBar;
    private TextView mTvLoadPercent;
    private double mCurrentTime;
    private int lastSeekBarPosition;
    private boolean needUpdateSeekBar = true;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GlobalInfo.MESSAGE_UPDATE_VIDEOPB_BAR:
                    Log.e(TAG, "handleMessage: progress = " + msg.arg1);
                    mVerticalSeekbar.setProgress(msg.arg1);
                    mHorizontalSeekbar.setProgress(msg.arg1);
                    mVerticalTime.setText(TimeUtils.secondsToMinutes(msg.arg1 / 100) + "/" + TimeUtils.secondsToMinutes(mVideoPlayback.getVideoDuration() / 100));
                    mHorizontalTime.setText(TimeUtils.secondsToMinutes(msg.arg1 / 100) + "/" + TimeUtils.secondsToMinutes(mVideoPlayback.getVideoDuration() / 100));
                    break;
                case EVENT_CACHE_STATE_CHANGED:
                    Log.e(TAG, "handleMessage: showLoading?" + msg.arg1);
                    if (msg.arg1 == 1) {
                        showLoading(true);
                    } else if (msg.arg1 == 2) {
                        showLoading(false);
                    }
                    break;
                case EVENT_CACHE_PROGRESS_NOTIFY:
                    Log.e(TAG, "handleMessage: cache progress = " + msg.arg1 + "  cacheSeekbar=" + msg.arg2);
//                    setLoadingPercent(msg.arg1);

                    mHorizontalSeekbar.setSecondaryProgress(msg.arg2);
                    mVerticalSeekbar.setSecondaryProgress(msg.arg2);
                    break;
                case EVENT_VIDEO_PLAY_COMPLETED:
                    changePlayState(false);
                    Log.e(TAG, "handleMessage: +video play completed");
                    if (curMode != MODE_VIDEO_STOP) {
                        showLoading(false);
                        stopVideo();
                        curMode = MODE_VIDEO_STOP;
                        mHorizontalSeekbar.setProgress(0);
                        mVerticalSeekbar.setProgress(0);
                        mHorizontalSeekbar.setSecondaryProgress(0);
                        mVerticalSeekbar.setSecondaryProgress(0);
                    }
                    break;
                case EVENT_MEDIA_STREAM_CLOSE:
                    Log.e(TAG, "handleMessage: stopVideo");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunplus_video_preview);
        initView();
        init();
        initEvent();
    }

    private void initEvent() {
        mVideoView.addVideoPbUpdateBarLitener(new VideoPbMjpg.VideoPbUpdateBarLitener() {
            @Override
            public void updateBar(double pts) {
                if (curMode != MODE_VIDEO_PLAYING || !needUpdateSeekBar) {
                    return;
                }
                mCurrentTime = pts;
                int temp = Double.valueOf(mCurrentTime * 100).intValue();
                mHandler.obtainMessage(GlobalInfo.MESSAGE_UPDATE_VIDEOPB_BAR, temp, 0).sendToTarget();
            }
        });
        mHorizontalPlay.setOnClickListener(this);
        mVerticalPlay.setOnClickListener(this);
        mHorizontalPrevious.setOnClickListener(this);
        mVerticalPrevious.setOnClickListener(this);
        mHorizontalNext.setOnClickListener(this);
        mVerticalNext.setOnClickListener(this);
        mHorizontalMenu.setOnClickListener(this);
        mVerticalMenu.setOnClickListener(this);
        mVideoView.setOnClickListener(this);
        mVerticalSeekbar.setOnSeekBarChangeListener(this);
        mHorizontalSeekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        addListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        delListener();
    }

    private void addListener() {
        CameraAction.getInstance().addEventListener(ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED, mVideoEventListener);
        CameraAction.getInstance().addEventListener(ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS, mVideoEventListener);
        CameraAction.getInstance().addEventListener(ICatchEventID.ICH_EVENT_VIDEO_STREAM_PLAYING_ENDED, mVideoEventListener);
        CameraAction.getInstance().addEventListener(ICatchEventID.ICH_EVENT_MEDIA_STREAM_CLOSED, mVideoEventListener);
    }

    private void delListener() {
        CameraAction.getInstance().delEventListener(ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED, mVideoEventListener);
        CameraAction.getInstance().delEventListener(ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS, mVideoEventListener);
        CameraAction.getInstance().delEventListener(ICatchEventID.ICH_EVENT_VIDEO_STREAM_PLAYING_ENDED, mVideoEventListener);
        CameraAction.getInstance().delEventListener(ICatchEventID.ICH_EVENT_MEDIA_STREAM_CLOSED, mVideoEventListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_view:
                Log.e(TAG, "onClick: videoView");
                toggleFrame(true);
                break;
            case R.id.vertical_play:
            case R.id.horizontal_play:
                switch (curMode) {
                    case MODE_VIDEO_STOP:
                        startVideo(currentFile);
                        break;
                    case MODE_VIDEO_PAUSE:
                        resumeVideo();
                        break;
                    case MODE_VIDEO_PLAYING:
                        pauseVideo();
                        break;
                    default:
                        break;
                }
                break;
            case R.id.horizontal_previous:
            case R.id.vertical_previous:
                playPrevious();
                break;
            case R.id.horizontal_next:
            case R.id.vertical_next:
                playNext();
                break;
            case R.id.horizontal_menu:
                mHorizontalList.setVisibility(mHorizontalList.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.vertical_menu:
                mVerticalList.setVisibility(mVerticalList.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.video_cut:
                break;
            case R.id.iv_illegal_report:
                break;
            default:
                break;
        }
    }

    private void changePlayState(boolean isPlaying) {
        if (isPlaying) {
            mHorizontalPlay.setBackgroundResource(R.drawable.play_pause_selector);
            mVerticalPlay.setBackgroundResource(R.drawable.play_pause_selector);
        } else {
            mHorizontalPlay.setBackgroundResource(R.drawable.play_play_selector);
            mVerticalPlay.setBackgroundResource(R.drawable.play_play_selector);
        }
    }


    /*--------------- OnSeekBarChangeListener ---------------*/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        lastSeekBarPosition = seekBar.getProgress();
        needUpdateSeekBar = false;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (curMode == MODE_VIDEO_STOP) {
            seekBar.setProgress(lastSeekBarPosition);
        } else {
            if (!mVideoPlayback.videoSeek(seekBar.getProgress() / 100.0)) {
                seekBar.setProgress(lastSeekBarPosition);
            }
            needUpdateSeekBar = true;
        }
    }


    public class VideoEventListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent iCatchEvent) {
            switch (iCatchEvent.getEventID()) {
                case ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_CHANGED:
                    mHandler.obtainMessage(EVENT_CACHE_STATE_CHANGED, iCatchEvent.getIntValue1(), 0).sendToTarget();
                    break;
                case ICatchEventID.ICH_EVENT_VIDEO_PLAYBACK_CACHING_PROGRESS:
                    int temp = Double.valueOf(iCatchEvent.getDoubleValue1() * 100).intValue();
                    mHandler.obtainMessage(EVENT_CACHE_PROGRESS_NOTIFY, iCatchEvent.getIntValue1(), temp).sendToTarget();
                    break;
                case ICatchEventID.ICH_EVENT_VIDEO_STREAM_PLAYING_ENDED:
                    mHandler.obtainMessage(EVENT_VIDEO_PLAY_COMPLETED, 0, 0).sendToTarget();
                    break;
                case ICatchEventID.ICH_EVENT_MEDIA_STREAM_CLOSED:
                    mHandler.obtainMessage(EVENT_MEDIA_STREAM_CLOSE);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();
        mActivityVideoPreview = (RelativeLayout) findViewById(R.id.activity_video_preview);
        rl_video_view = (RelativeLayout) findViewById(R.id.rl_video_view);
        mVideoView = (VideoPbMjpg) findViewById(R.id.video_view);
        mVerticalFrame = (LinearLayout) findViewById(R.id.vertical_frame);
        mVerticalMenu = (ImageButton) findViewById(R.id.vertical_menu);
        mIbVerticalGps = (ImageButton) findViewById(R.id.ib_vertical_gps);
        mVerticalPrevious = (ImageView) findViewById(R.id.vertical_previous);
        mVerticalPlay = (ImageView) findViewById(R.id.vertical_play);
        mVerticalNext = (ImageView) findViewById(R.id.vertical_next);
        mVerticalSeekbar = (AppCompatSeekBar) findViewById(R.id.vertical_seekbar);
        mVerticalTime = (TextView) findViewById(R.id.vertical_time);
        mVerticalList = (ListView) findViewById(R.id.vertical_list);
        mHeadFrame = (LinearLayout) findViewById(R.id.head_frame);
        mTitle = (TextView) findViewById(R.id.title);
        mHorizontalIbGps = (ImageButton) findViewById(R.id.horizontal_ib_gps);
        mIvIllegalReport = (ImageView) findViewById(R.id.iv_illegal_report);
        mVideoCut = (ImageView) findViewById(R.id.video_cut);
        mHorizontalFrame = (RelativeLayout) findViewById(R.id.horizontal_frame);
        mHorizontalBottom = (LinearLayout) findViewById(R.id.horizontal_bottom);
        mHorizontalMenu = (ImageButton) findViewById(R.id.horizontal_menu);
        mHorizontalPrevious = (ImageView) findViewById(R.id.horizontal_previous);
        mHorizontalPlay = (ImageView) findViewById(R.id.horizontal_play);
        mHorizontalNext = (ImageView) findViewById(R.id.horizontal_next);
        mHorizontalSeekbar = (AppCompatSeekBar) findViewById(R.id.horizontal_seekbar);
        mHorizontalTime = (TextView) findViewById(R.id.horizontal_time);
        mHorizontalList = (ListView) findViewById(R.id.horizontal_list);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mTvLoadPercent = (TextView) findViewById(R.id.loadPercent);
        toggleFrame(false);
    }

    @Override
    protected void init() {
        super.init();
        Intent data = getIntent();
        if (data.hasExtra(KEY_POSTION)) {
            int postion = data.getIntExtra(KEY_POSTION, -1);
            if (postion < 0) {
                return;
            }
            currentFile = GlobalInfo.previewFileList.get(postion);
        }
        horizontal_adapter = new ListAdapter();
        vertical_adapter = new ListAdapter();
        mVideoEventListener = new VideoEventListener();
        mHorizontalList.setAdapter(horizontal_adapter);
        mVerticalList.setAdapter(vertical_adapter);
        mVerticalSeekbar.setMax(0);
        mHorizontalSeekbar.setMax(0);
        addListener();
        startVideo(currentFile);
        mHorizontalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startVideo(GlobalInfo.previewFileList.get(position));
            }
        });
        mVerticalList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startVideo(GlobalInfo.previewFileList.get(position));
            }
        });
    }

    private void startVideo(ICatchFile iCatchFile) {
        if (iCatchFile == null) {
            return;
        }
        if (curMode != MODE_VIDEO_STOP) {
            stopVideo();
        }
        showLoading(true);
        currentFile = iCatchFile;
        if (!mVideoPlayback.startPlaybackStream(iCatchFile)) {
            //打开文件流失败
            showAlertMessage(R.string.VideoView_error_text_unknown);
        } else {
            if (!mVideoView.start(CameraFactory.getInstance().getSunplusCamera(), mHandler)) {
                showAlertMessage(R.string.VideoView_error_text_unknown);
            } else {
                mHorizontalSeekbar.setProgress(0);
                mVerticalSeekbar.setProgress(0);
                mHorizontalSeekbar.setSecondaryProgress(0);
                mVerticalSeekbar.setSecondaryProgress(0);
                int videoDuration = mVideoPlayback.getVideoDuration();
                mHorizontalSeekbar.setMax(videoDuration);
                mVerticalSeekbar.setMax(videoDuration);
                mVerticalTime.setText(TimeUtils.secondsToMinutes(0) + "/" + TimeUtils.secondsToMinutes(videoDuration / 100));
                mHorizontalTime.setText(TimeUtils.secondsToMinutes(0) + "/" + TimeUtils.secondsToMinutes(videoDuration / 100));
                curMode = MODE_VIDEO_PLAYING;
                changePlayState(true);
                mTitle.setText(currentFile.getFileName());
                horizontal_adapter.notifyDataSetChanged();
                vertical_adapter.notifyDataSetChanged();
            }
        }
    }

    private void resumeVideo() {
        if (!mVideoPlayback.resumePlayback()) {
            showAlertMessage(R.string.VideoView_error_text_unknown);
        } else {
            curMode = MODE_VIDEO_PLAYING;
            changePlayState(true);
        }
    }

    private void pauseVideo() {
        if (!mVideoPlayback.pausePlayback()) {
            showAlertMessage(R.string.VideoView_error_text_unknown);
        } else {
            curMode = MODE_VIDEO_PAUSE;
            changePlayState(false);
        }
    }

    private boolean stopVideo() {
        boolean ret = true;
        if (curMode != MODE_VIDEO_STOP) {
            mVideoView.stop();
            ret = mVideoPlayback.stopPlaybackStream();
            if (ret) {
                mHorizontalSeekbar.setProgress(0);
                mVerticalSeekbar.setProgress(0);
                mVerticalSeekbar.setSecondaryProgress(0);
                mHorizontalSeekbar.setSecondaryProgress(0);
                changePlayState(false);
                curMode = MODE_VIDEO_STOP;
            }
        }
        return ret;
    }

    private void playPrevious() {
        if (GlobalInfo.previewFileList == null) {
            return;
        }
        int position = GlobalInfo.previewFileList.indexOf(currentFile) - 1;
        if (position < 0) {
            position = GlobalInfo.previewFileList.size() - 1;
        }
        startVideo(GlobalInfo.previewFileList.get(position));
    }

    private void playNext() {
        if (GlobalInfo.previewFileList == null) {
            return;
        }
        int position = GlobalInfo.previewFileList.indexOf(currentFile) + 1;
        if (position >= GlobalInfo.previewFileList.size()) {
            position = 0;
        }
        startVideo(GlobalInfo.previewFileList.get(position));
    }

    private void setLoadingPercent(int percentage) {
        String temp = percentage + "%";
        mTvLoadPercent.setText(temp);
    }

    private void showLoading(boolean showFlag) {
        mTvLoadPercent.setText("0%");
        mProgressBar.setVisibility(showFlag ? View.VISIBLE : View.GONE);
//        mTvLoadPercent.setVisibility(showFlag ? View.VISIBLE : View.GONE);
    }

    private void toggleFrame(boolean change) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        int screenW = ScreenUtils.getScreenW(this);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (change) {
                boolean showingFrame = isShowingFrame();
                mHeadFrame.setVisibility(showingFrame ? View.GONE : View.VISIBLE);
                mHorizontalFrame.setVisibility(showingFrame ? View.GONE : View.VISIBLE);
            } else {
                //屏幕旋转
                mHorizontalFrame.setVisibility(isShowingFrame() ? View.VISIBLE : View.GONE);
                mVerticalFrame.setVisibility(View.GONE);
                mHorizontalList.setVisibility(mVerticalList.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                rl_video_view.setLayoutParams(layoutParams);
            }
        } else {
            if (change) {
                mHeadFrame.setVisibility(isShowingFrame() ? View.GONE : View.VISIBLE);
            } else {
                //屏幕旋转
                mVerticalFrame.setVisibility(View.VISIBLE);
                mHorizontalFrame.setVisibility(View.GONE);
                mVerticalList.setVisibility(mHorizontalList.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                layoutParams.height = 9 * screenW / 16;
                rl_video_view.setLayoutParams(layoutParams);
            }
        }
    }

    private boolean isShowingFrame() {
        return mHeadFrame.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleFrame(false);
    }

    private class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return GlobalInfo.previewFileList == null ? 0 : GlobalInfo.previewFileList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.simple_list_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            ICatchFile iCatchFile = GlobalInfo.previewFileList.get(position);
            textView.setText(iCatchFile.getFileName());
            if (iCatchFile == currentFile) {
                textView.setTextColor(getResources().getColor(R.color.main_color));
            } else {
                textView.setTextColor(getResources().getColor(R.color.white));
            }
            return convertView;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopVideo();
    }
}
