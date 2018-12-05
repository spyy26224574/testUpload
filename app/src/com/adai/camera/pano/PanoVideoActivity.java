package com.adai.camera.pano;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;
import com.ligo.medialib.PanoCamViewLocal;
import com.ligo.medialib.PanoCamViewOnline;
import com.ligo.medialib.opengl.VideoRenderHard;
import com.ligo.medialib.opengl.VideoRenderYuv;
import com.widget.piechart.ScreenUtils;

import java.io.File;

import tv.danmaku.ijk.media.player.IMediaPlayer;

public class PanoVideoActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, PanoCamViewLocal.OnChangeListener {
    public static final String TAG = "PanoVideoActivity";
    public static final String KEY_FILES = "KEY_FILES";
    public static final String KEY_POSTION = "KEY_POSTION";
    public static final String KEY_TYPE = "KEY_TYPE";
    private static final int SHOW_PROGRESS = 1;
    private static final int NO_DEVICE_CONNECT = 3;
    private AppCompatSeekBar horizontalseekbar;
    private TextView horizontaltime;
    private LinearLayout horizontalbottom;
    private RelativeLayout horizontalframe;
    private View head_frame;
    private ImageView back;
    private TextView title;
    private RelativeLayout activityvideppreview;
//    private RelativeLayout rl_video_view;
    /**
     * 视频类型,本地还是摄像头，0为默认为摄像头，1为本地,2为网络
     */
    private int type = 0;
    private ProgressBar mPbBuffer;
    private PanoCamViewLocal mPanoCamViewLocal; //硬解码
    private PanoCamViewOnline mPanoCamViewOnline; //软解码

    ImageView iv_original_land;
    ImageView iv_front_back_land;
    ImageView iv_four_direct_land;
    ImageView iv_wide_single_land;
    ImageView iv_cylinder_land;

    int panoDisplayType = 1; //全景视图方式
    public static final int VIDEO_SHOW_TYPE_SRC_CIRCLE = 1;
    public static final int VIDEO_SHOW_TYPE_2_SCREEN = 2;
    public static final int VIDEO_SHOW_TYPE_4_SCREEN = 3;
    public static final int VIDEO_SHOW_TYPE_PLANE1 = 4;
    public static final int VIDEO_SHOW_TYPE_CYLINDER = 5;

    private String mVideoPath;
    private int videoType;
    private int fishEyeId;
    private int width;
    private int height;

    //    private long postion = 0;
    private ImageView verticalplay;
    private boolean isDownload = false;
    ImageView iv_download, iv_download_progress;

    private int currentPlayer = 0; //0：硬解码 1：软解码

    ImageView iv_pano_type;
    LinearLayout ll_iv_pano_type;
    boolean slideByUser = false;
//    int[] ints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("9999", "onCreate");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_pano_player);
        mVideoPath = getIntent().getStringExtra("videoPath");
        videoType = getIntent().getIntExtra("videoType", 0);
        fishEyeId = getIntent().getIntExtra("fishEyeId", 0);
        width = getIntent().getIntExtra("width", 0);
        height = getIntent().getIntExtra("height", 0);

        type = getIntent().getIntExtra("type", 0);
        if (type == 2) {
            checkDownload();
        }

        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        mPanoCamViewLocal = (PanoCamViewLocal) findViewById(R.id.pv_video);
        mPanoCamViewLocal.setOnChangeListener(this);

        mPanoCamViewOnline = (PanoCamViewOnline) findViewById(R.id.pv_video_soft);
        mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);

        iv_pano_type = (ImageView) findViewById(R.id.iv_pano_type);
        iv_pano_type.setOnClickListener(this);
        iv_pano_type.setVisibility(View.GONE);
        ll_iv_pano_type = (LinearLayout) findViewById(R.id.ll_iv_pano_type);
        RelativeLayout.LayoutParams horizontalTitleLayoutParams = (RelativeLayout.LayoutParams) ll_iv_pano_type.getLayoutParams();
        horizontalTitleLayoutParams.width = ScreenUtils.getScreenH(this);
        ll_iv_pano_type.setLayoutParams(horizontalTitleLayoutParams);

        iv_original_land = (ImageView) findViewById(R.id.iv_original_land);
        iv_front_back_land = (ImageView) findViewById(R.id.iv_front_back_land);
        iv_four_direct_land = (ImageView) findViewById(R.id.iv_four_direct_land);
        iv_wide_single_land = (ImageView) findViewById(R.id.iv_wide_single_land);
        iv_cylinder_land = (ImageView) findViewById(R.id.iv_cylinder_land);

        iv_original_land.setOnClickListener(this);
        iv_front_back_land.setOnClickListener(this);
        iv_four_direct_land.setOnClickListener(this);
        iv_wide_single_land.setOnClickListener(this);
        iv_cylinder_land.setOnClickListener(this);

//        ll_pano_type_land = (LinearLayout) findViewById(R.id.ll_pano_type_land);

        verticalplay = (ImageView) findViewById(R.id.vertical_play);
        verticalplay.setOnClickListener(this);

        mPbBuffer = (ProgressBar) findViewById(R.id.pb_buffer);
        this.activityvideppreview = (RelativeLayout) findViewById(R.id.activity_video_preview);
        this.title = (TextView) findViewById(R.id.title);
        this.back = (ImageView) findViewById(R.id.back);
        this.horizontalframe = (RelativeLayout) findViewById(R.id.horizontal_frame);
        this.horizontalbottom = (LinearLayout) findViewById(R.id.horizontal_bottom);
        this.horizontaltime = (TextView) findViewById(R.id.horizontal_time);
        this.horizontalseekbar = (AppCompatSeekBar) findViewById(R.id.horizontal_seekbar);

//        mVideoFrame = (ImageView) findViewById(R.id.iv_view_frame);
//        rl_video_view = (RelativeLayout) findViewById(R.id.rl_video_view);

        this.head_frame = findViewById(R.id.head_frame);

        activityvideppreview.setOnClickListener(this);
        horizontalseekbar.setOnSeekBarChangeListener(this);
        iv_download = (ImageView) findViewById(R.id.iv_download);
        iv_download.setOnClickListener(this);
        iv_download_progress = (ImageView) findViewById(R.id.iv_download_progress);

//        findViewById(R.id.iv_exit_fullscreen).setOnClickListener(this);

    }

    private void showVideo(String video_path) {
        if (mVideoPath == null) return;
        title.setText("roadcam");
//        mPbBuffer.setVisibility(View.GONE);

        if (!mPanoCamViewLocal.isInit) {
            mPanoCamViewOnline.setVisibility(View.GONE);
            mPanoCamViewOnline.stopPlay();
            mPanoCamViewOnline.changePlayer();

            mPanoCamViewLocal.setVisibility(View.VISIBLE);
            mPanoCamViewLocal.reInit(PanoVideoActivity.this);
            mPanoCamViewLocal.setOnChangeListener(this);
        }

//        ints = HbxFishEye.GetId(video_path);
//        String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
//        if (product_model.equals("100") && (video_path.contains("http") || (video_path.contains("rtsp")))) {
//            ints[0] = 1;
//            ints[1] = 1;
//        }
        mPanoCamViewLocal.startPlay(video_path);

    }


    @Override
    protected void init() {
        super.init();
//        if (getIntent().hasExtra("position")) {
//            postion = getIntent().getIntExtra("position", 0);
//        }
        showVideo(mVideoPath);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    int pos = setProgress();
                    if (mPanoCamViewLocal.isPlaying()) {
                        long delayMillis = 1000 - (pos % 1000);
                        sendEmptyMessageDelayed(SHOW_PROGRESS, delayMillis);
                    }
                    break;
                case NO_DEVICE_CONNECT:
                    hidepDialog();
                    if (isFinishing()) return;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new AlertDialog.Builder(mContext).setTitle(R.string.notice).setMessage(R.string.wifi_checkmessage)
                                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        WifiUtil.getInstance().gotoWifiSetting(mContext);
                                                    }
                                                }).setNegativeButton(R.string.cancel, null).create().show();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }).start();
                    break;
            }
        }
    };

    private int setProgress() {
        int current;
        int duration;
        if (currentPlayer == 0) {
            current = mPanoCamViewLocal.getCurrent();
            duration = mPanoCamViewLocal.getDuration();
        } else {
            current = mPanoCamViewOnline.getCurrent();
            duration = mPanoCamViewOnline.getDuration();
        }
//        Log.w("9527", "currentPlayer = " + currentPlayer + "duration = " + duration + " current = " + current);
        if (duration < 0)
            return 0;
        if (horizontalseekbar != null) {
            horizontalseekbar.setMax(duration);
            horizontalseekbar.setProgress(current);
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
        horizontaltime.setText(text);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPanoCamViewOnline != null) {
            mPanoCamViewOnline.stopPlay();
        }
    }

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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
//            case R.id.iv_exit_fullscreen:
//                destroy();
//                finish();
//                break;
            case R.id.iv_pano_type: {
                if (ll_iv_pano_type.getVisibility() == View.VISIBLE) {
                    ll_iv_pano_type.setVisibility(View.GONE);
                } else {
                    ll_iv_pano_type.setVisibility(View.VISIBLE);
                }
            }
            break;
            case R.id.iv_original_land:
                panoDisplayType = 1;
                setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CIRCLE);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderHard.TYPE_CIRCLE);
                }
                break;
            case R.id.iv_front_back_land:
                panoDisplayType = 2;
                setPanoType(VIDEO_SHOW_TYPE_2_SCREEN);
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_front_back);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_2_SCREEN);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderHard.TYPE_2_SCREEN);
                }
                break;
            case R.id.iv_four_direct_land:
                panoDisplayType = 3;
                setPanoType(VIDEO_SHOW_TYPE_4_SCREEN);
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_four_direct);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_4_SCREEN);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_4_SCREEN);
                }
                break;
            case R.id.iv_wide_single_land:
                panoDisplayType = 4;
                setPanoType(VIDEO_SHOW_TYPE_PLANE1);
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_hemisphere);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_HEMISPHERE);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_HEMISPHERE);
                }
                break;
            case R.id.iv_cylinder_land:
                panoDisplayType = 5;
                setPanoType(VIDEO_SHOW_TYPE_CYLINDER);
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_cylinder);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CYLINDER);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CYLINDER);
                }
                break;
            case R.id.iv_download:
                downLoad(mVideoPath);
            case R.id.vertical_play:
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

        }
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        Log.e(_TAG_, " 9527 onProgressChanged:progress = " + progress + " fromUser = " + fromUser);
        if (!fromUser) return;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(_TAG_, "9527 onStartTrackingTouch: ");
        slideByUser = true;
        handler.removeMessages(SHOW_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == seekBar.getMax()) {
            progress = progress - 1;
        }
        Log.e(_TAG_, "9527 onStopTrackingTouch: progress=" + progress);
        if (currentPlayer == 0) {
            mPanoCamViewLocal.seek(progress);
        } else {
            mPanoCamViewOnline.seek(progress);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                slideByUser = false;
            }
        }, 200);
        handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("9527", "onDestroy");
//        unRegisterWifiReceiver();
//        handler.removeMessages(NO_DEVICE_CONNECT);
//
//        if (mPanoCamViewLocal != null) {
//            mPanoCamViewLocal.stopPlay();
//        }
//
//        if (mPanoCamViewOnline != null) {
//            mPanoCamViewOnline.stopPlay();
//            mPanoCamViewOnline.setInfoCallback(null);
//        }
        if (mPanoCamViewLocal != null) {
            mPanoCamViewLocal.release();
        }

        if (mPanoCamViewOnline != null) {
            mPanoCamViewOnline.release();
        }


    }

//    public void destroy() {
//        handler.removeMessages(NO_DEVICE_CONNECT);
//
//        mPlayerView.stopPlay();
////        mPlayerView.setInfoCallback(null);
//        mPlayerView.release();
//
//    }


    private String checkDownload() {
        String local = NetworkDownloadUtils.getLocalPath(mVideoPath);
        File file = new File(local);
        if (file.exists()) {
            isDownload = true;
            findViewById(R.id.iv_download).setBackgroundResource(R.drawable.bg_download_complete);
            return local;
        } else {
            isDownload = false;
            return null;
        }
    }

    private void downLoad(String path) {
        if (isDownload) return;

        iv_download_progress.setVisibility(View.VISIBLE);
        final Animation operatingAnim = AnimationUtils.loadAnimation(this, R.anim.down_anim);
        LinearInterpolator lin = new LinearInterpolator();
        operatingAnim.setInterpolator(lin);
        iv_download_progress.startAnimation(operatingAnim);

        NetworkDownloadUtils.downloadFile(path, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                Log.e(TAG, "onDownloadComplete");
                if (!isFinishing()) {
                    Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
                    intent.putExtra("isVideo", true);
                    VLCApplication.getAppContext().sendBroadcast(intent);
                    ToastUtil.showShortToast(PanoVideoActivity.this, getString(R.string.successfully_saved_to) + path);
                    ToastUtil.showShortToast(PanoVideoActivity.this, getString(R.string.successfully_saved_to) + path);

                    iv_download_progress.clearAnimation();
                    iv_download_progress.setVisibility(View.GONE);
                    iv_download.setBackgroundResource(R.drawable.bg_download_complete);

                    isDownload = true;
                }
            }

            @Override
            public void onDownloading(int progress) {
            }

            @Override
            public void onDownladFail() {
                Log.e(TAG, "onDownladFail");
                if (!isFinishing()) {
                    ToastUtil.showShortToast(PanoVideoActivity.this, getString(R.string.download_error));
                    iv_download_progress.clearAnimation();
                    iv_download_progress.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //按下的如果是BACK，同时没有重复
            Log.e("9528", "back");
            destroy();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void goBack() {
        super.goBack();
        destroy();
        finish();
    }

    private void destroy() {
        Log.e("9999", "destroy");
        handler.removeMessages(NO_DEVICE_CONNECT);

        if (mPanoCamViewLocal != null) {
            mPanoCamViewLocal.stopPlay();
//            mPanoCamViewLocal.release();
        }

        if (mPanoCamViewOnline != null) {
            mPanoCamViewOnline.stopPlay();
            mPanoCamViewOnline.setInfoCallback(null);
//            mPanoCamViewOnline.release();
        }

    }

    @Override
    public void onLoadComplete(int ret) {
        horizontalseekbar.setProgress(0);
        horizontalseekbar.setMax(mPanoCamViewLocal.getDuration());
        changePlayState(true);

        if ((videoType & fishEyeId) == 0) {
            iv_pano_type.setVisibility(View.GONE);
            mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_SRC);
        } else {
            iv_pano_type.setVisibility(View.VISIBLE);
            iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
            setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
            mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CIRCLE);
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
        Log.e("9999", "onError");
        mPanoCamViewLocal.setVisibility(View.INVISIBLE);
        mPanoCamViewLocal.stopPlay();
        mPanoCamViewLocal.changePlayer();

        currentPlayer = 1;

        mPanoCamViewOnline.setVisibility(View.VISIBLE);
        mPanoCamViewOnline.reInit();
        mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
        showVideoSoft(mVideoPath);

    }

    @Override
    public void onEnd() {
        Log.e("9527", "onEnd ");
        int duration = mPanoCamViewLocal.getDuration();
        if (horizontalseekbar != null) {
            horizontalseekbar.setProgress(duration);
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
                mPanoCamViewLocal.setVisibility(View.INVISIBLE);
                mPanoCamViewLocal.stopPlay();
                mPanoCamViewLocal.changePlayer();

                currentPlayer = 1;

                mPanoCamViewOnline.setVisibility(View.VISIBLE);
                mPanoCamViewOnline.reInit();
                mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
                showVideoSoft(mVideoPath);
                break;
        }


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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPbBuffer.setVisibility(View.GONE);
                    int current = mPanoCamViewOnline.getCurrent();
                    int duration = mPanoCamViewOnline.getDuration();
//                    Log.w("9527", "33333 current = " + current + " ,duration = " + duration);
                    if (!slideByUser) {
                        if (current >= 0 && duration - current > 0) {
                            setProgress();
                        }
                        if (current == -1) {
                            if (horizontalseekbar != null) {
                                horizontalseekbar.setProgress(duration);
                            }
                            showTime(duration, duration);
                            changePlayState(false);
                        }
                    }

                }
            });

        }

        @Override
        public void onScreenShot(boolean sucess, String url) {
        }

    };

    private void changePlayState(boolean isPlaying) {
        Log.e("9527", "changePlayState isPlaying = " + isPlaying);
        handler.removeMessages(SHOW_PROGRESS);
        if (isPlaying) {
            verticalplay.setBackgroundResource(R.drawable.play_pause_selector);
            handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
        } else {
            verticalplay.setBackgroundResource(R.drawable.play_play_selector);
        }
    }

    private void showVideoSoft(String video_path) {
        if (mVideoPath == null) return;
        title.setText("roadcam");

        mPanoCamViewOnline.setUrl(video_path);
        mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
        changePlayState(true);
        int decoding_type;
        if (video_path.contains("http")) {
            decoding_type = 1;
        } else {
            decoding_type = 3;
        }
        mPanoCamViewOnline.startPlay(decoding_type);

        if ((videoType & fishEyeId) == 0) {
            iv_pano_type.setVisibility(View.GONE);
            mPanoCamViewOnline.onChangeShowType(VideoRenderHard.TYPE_SRC);
        } else {
            iv_pano_type.setVisibility(View.VISIBLE);
            iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
            setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
            mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CIRCLE);
        }

    }

    public void setPanoType(int panoType) {
        iv_original_land.setSelected(panoType == VIDEO_SHOW_TYPE_SRC_CIRCLE);
        iv_front_back_land.setSelected(panoType == VIDEO_SHOW_TYPE_2_SCREEN);
        iv_four_direct_land.setSelected(panoType == VIDEO_SHOW_TYPE_4_SCREEN);
        iv_wide_single_land.setSelected(panoType == VIDEO_SHOW_TYPE_PLANE1);
        iv_cylinder_land.setSelected(panoType == VIDEO_SHOW_TYPE_CYLINDER);
    }


}
