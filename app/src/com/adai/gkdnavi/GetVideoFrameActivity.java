package com.adai.gkdnavi;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.adai.gkdnavi.view.CustomSeekBar;
import com.ijk.media.widget.media.IjkVideoView;
import com.ijk.media.widget.media.TextureRenderView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class GetVideoFrameActivity extends BaseActivity implements View.OnClickListener {
    private IjkVideoView mVideoView;
    private ImageView mVideoFrame;
    private String mImagePath;
    private TextView right_text, mTvTime;
    private CustomSeekBar mCustomSeekBar;
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private FFmpegMediaMetadataRetriever mFmmr;
    private String video_path;
    private static final int MAX_FRAMES = 10;
    private long mDuration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_video_frame);
        init();
        initView();
        initEvent();
        bindView();
    }

    @Override
    protected void init() {
        super.init();
        Intent intent = getIntent();
        mImagePath = intent.getStringExtra("imagePath");
        video_path = intent.getStringExtra("video_path");
    }

    protected void initView() {
        super.initView();
        setTitle(R.string.change_picture);
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoFrame = (ImageView) findViewById(R.id.video_frame);
        right_text = (TextView) findViewById(R.id.right_text);
        right_text.setVisibility(View.VISIBLE);
        mTvTime = (TextView) findViewById(R.id.tv_time);
        mCustomSeekBar = (CustomSeekBar) findViewById(R.id.customSeekBar);
        mCustomSeekBar.setBitmapList(mBitmaps);
    }

    private void initEvent() {
        right_text.setOnClickListener(this);
        mVideoView.setVideoPath(video_path);
        mVideoView.setOnPreparedListener(preparedListener);
        mVideoView.start();
        mVideoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if (mVideoView.isPlaying()) {
                            //刚进来的时候先播放视频然后马上暂停以显示第一帧
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mVideoView.pause();
                                }
                            }, 200);
                            mVideoView.setOnInfoListener(null);
                        }
                        break;
                }
                return false;
            }
        });
        mCustomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int currentTime = (int) (progress * mDuration / 100 + .5f);
                mTvTime.setText(String.format("%02d:%02d", currentTime / 60000, currentTime / 1000 % 60));
                mVideoView.seekTo(currentTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mVideoFrame.setVisibility(View.GONE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void bindView() {
//        Glide.get(this).clearMemory();
        ImageLoaderUtil.getInstance().loadImageWithoutCache(this, mImagePath, mVideoFrame);
        right_text.setText(R.string.ok);
    }

    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mDuration = iMediaPlayer.getDuration();
            initFmmr();
            getBitmaps(mDuration);
        }
    };

    private void initFmmr() {
        if (mFmmr == null) {
            mFmmr = new FFmpegMediaMetadataRetriever();
        }
        mFmmr.setDataSource(video_path);
    }

    private void getBitmaps(final long duration) {
        mBitmaps.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                long sub = duration / MAX_FRAMES;
                for (int i = 0; i < MAX_FRAMES; i++) {
                    int time = (int) (i * sub);
                    Bitmap bitmap = mFmmr.getScaledFrameAtTime(time * 1000, 64, 36);
                    if (bitmap != null) {
                        mBitmaps.add(bitmap);
                        mCustomSeekBar.postInvalidate();
                    }
                }
                releaseMmr();
                initFmmr();
            }
        }).start();
    }

    private void releaseMmr() {
        if (mFmmr != null) {
            mFmmr.release();
            mFmmr = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
                if (mVideoView.getRenderView() instanceof TextureRenderView) {
                    TextureRenderView renderView = (TextureRenderView) mVideoView.getRenderView();
                    TextureView tv = (TextureView) renderView.getView();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        Bitmap bitmap = tv.getBitmap();
                        try {
                            FileOutputStream fos = new FileOutputStream(mImagePath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        setResult(RESULT_OK);
                        finish();
                        return;
                    }
                }
                showpDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        int progress = mCustomSeekBar.getProgress();
//                        int currentTime = (int) (progress * mDuration / 100 + .5f);
//                        final Bitmap bitmap = mFmmr.getScaledFrameAtTime(currentTime * 1000, 640, 360);
                        Bitmap bitmap = null;
                        int currentpostion = mVideoView.getCurrentPosition() - 800;
                        bitmap = mFmmr.getScaledFrameAtTime(currentpostion * 1000, 640, 360);
                        try {
                            FileOutputStream fos = new FileOutputStream(mImagePath);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        UIUtils.post(new Runnable() {
                            @Override
                            public void run() {
                                hidepDialog();
                                setResult(RESULT_OK);
                                finish();
                            }
                        });

                    }
                }).start();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVideoView.stopPlayback();
        mVideoView.release(true);
        mVideoView.stopBackgroundPlay();
        releaseMmr();
    }
}
