package com.adai.gkdnavi;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.Hjni.HbxFishEye;
import com.adai.camera.CameraConstant;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.gpsvideo.GpsInfoBean;
import com.adai.gkdnavi.gpsvideo.GpsWriter;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.view.MaterialRangeSlider;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.example.ipcamera.application.VLCApplication;
import com.ffmpeg.ExecuteBinaryResponseHandler;
import com.ffmpeg.FFmpeg;
import com.ffmpeg.LoadBinaryResponseHandler;
import com.ffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.ffmpeg.exceptions.FFmpegNotSupportedException;
import com.ijk.media.widget.media.AndroidMediaController;
import com.ijk.media.widget.media.IRenderView;
import com.ijk.media.widget.media.IjkVideoView;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class EditVideoActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = EditVideoActivity.class.getSimpleName();
    public static final String IS_SUPPORT_REPORT_KEY = "is_support_report_key";
    private ArrayList<GpsInfoBean> mGpsInfos;
    //    private String mSilenceCutVideoPath;
    private int mEncryptType;

    public interface CmdCallback {
        void onSuccess();

        void onFail();
    }

    private AndroidMediaController mMediaController;
    private IjkVideoView videoView;
    private TextView video_time;
    private ImageView video_play;
    private ImageView video_frame;
    private TextView start_time, end_time, total_time;
    private ViewGroup seek_layout;
    private TextView cut_select, delete_select, voice;
    //    private RangeSeekBar<Integer> rangeSeekBar;
    private RadioGroup musics;
    private Uri video_uri;//播放视频的uri
    private FFmpeg fFmpeg;
    private ImageView right_img;
    private TextView right_text, mTvNotice;
    private String newPath;
    private String video_path;
    private String smallVideoPath;
    private String logo_path = VLCApplication.TEMP_PATH + "/logo.jpg";
    public static final int TYPE_SHARE = 0;
    public static final int TYPE_ILLEGAL_REPORT = 2;
    /**
     * 0为剪切选中，1为删除选中
     */
    private int type = 0;
    /**
     * 命令类型,0为视频编辑，1为截图,2为删除视频选中段，3为音频添加
     */
    private int cmdType = 0;
    /**
     * 步骤，当为
     */
    private int step = 0;
    /**
     * 视频类型，默认为本地视频1，0为摄像头视频
     */
    private int videoType = 1;
    /**
     * 最大允许的比特率
     */
    private final int MAX_BITRATE = 8000;
    /**
     * 转换使用比特率
     */
    private final int CONVERT_BITRATE = 2500;
    /**
     * 指令延迟执行时间
     */
    private final int DELAY_TIME = 500;

    private MaterialRangeSlider slider;
    private int mEditType = 0;
    private FFmpegMediaMetadataRetriever mFmmr;
    private static final int MAX_FRAMES = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        video_play = (ImageView) findViewById(R.id.video_play);
        videoView = (IjkVideoView) findViewById(R.id.video_view);
        video_frame = (ImageView) findViewById(R.id.video_frame);
        video_time = (TextView) findViewById(R.id.video_time);
        start_time = (TextView) findViewById(R.id.start_time);
        end_time = (TextView) findViewById(R.id.end_time);
        total_time = (TextView) findViewById(R.id.total_time);
        seek_layout = (ViewGroup) findViewById(R.id.seek_layout);
        cut_select = (TextView) findViewById(R.id.cut_select);
//        delete_select=(TextView)findViewById(R.id.delete_select);
        voice = (TextView) findViewById(R.id.voice);
        mTvNotice = (TextView) findViewById(R.id.tv_notice);
        videoView.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        videoView.setRender(IjkVideoView.RENDER_SURFACE_VIEW);
        //        rangeSeekBar=new RangeSeekBar<Integer>(0,100,this);
//        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
//            @Override
//            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
//                Log.i(_TAG_, "User selected new range values: MIN=" + minValue + ", MAX=" + maxValue);
//                long duration=videoView.getDuration();
//                long start=duration*minValue/100;
//                long end=duration*maxValue/100;
//                long total=duration*(maxValue-minValue)/100;
//                start_time.setText(String.format("%02d:%02d:%02d",start/60000,(start/1000)%60,start%1000));
//                end_time.setText(String.format("%02d:%02d:%02d",end/60000,(end/1000)%60,end%1000));
//                total_time.setText(String.format("%02d:%02d:%02d",total/60000,(total/1000)%60,total%1000));
//            }
//        });
//        seek_layout.addView(rangeSeekBar);
//        right_img = (ImageView) findViewById(R.id.right_img);
//        right_img.setVisibility(View.GONE);
//        right_img.setBackgroundResource(R.drawable.setting_arrow);
//        right_img.setOnClickListener(this);
        right_text = (TextView) findViewById(R.id.right_text);
        right_text.setVisibility(View.VISIBLE);
        right_text.setText(getString(R.string.nextstep));
        right_text.setOnClickListener(this);
        musics = (RadioGroup) findViewById(R.id.music);
        musics.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                playMusic(checkedId);
            }
        });
        slider = (MaterialRangeSlider) findViewById(R.id.seek_slide);
        slider.setRangeSliderListener(new MaterialRangeSlider.RangeSliderListener() {
            @Override
            public void onMaxChanged(int newValue) {
                long duration = videoView.getDuration();//duration是毫秒级的，+500好四舍五入
                int minValue = slider.getSelectedMin();
                long start = duration * minValue / 100;
                long end = duration * newValue / 100;
                long total = duration * (newValue - minValue) / 100;
//                start_time.setText(String.format("%02d:%02d:%02d",start/60000,(start/1000)%60,start%1000));
                end_time.setText(String.format("%02d:%02d", end / 60000, (end / 1000) % 60));
                total_time.setText(String.format("%02d:%02d", total / 60000, (total / 1000) % 60));
//                if ((end - start) / 1000 >= 30 && end >= 30000) {
//                    slider.setSelectedMin((int) ((end - 30000) / duration * 100));
//                }
            }

            @Override
            public void onMinChanged(int newValue) {
                long duration = videoView.getDuration();
                int maxValue = slider.getSelectedMax();
                long start = duration * newValue / 100;
                long end = duration * maxValue / 100;
                long total = duration * (maxValue - newValue) / 100;
                start_time.setText(String.format("%02d:%02d", start / 60000, (start / 1000) % 60));
//                end_time.setText(String.format("%02d:%02d:%02d",end/60000,(end/1000)%60,end%1000));
                total_time.setText(String.format("%02d:%02d", total / 60000, (total / 1000) % 60));
                videoView.seekTo(((int) (start / 1000)) * 1000);
//                if ((end - start) / 1000 >= 30 && end <= duration - 30000) {
//                    slider.setSelectedMax((int) ((start + 30000) / duration * 100));
//                }
            }
        });
        slider.setBitmapList(mBitmaps);
    }

    private void initFmmr(long duration) {
        if (mFmmr == null) {
            mFmmr = new FFmpegMediaMetadataRetriever();
        }
        try {
            mFmmr.setDataSource(TextUtils.isEmpty(smallVideoPath) ? video_path : smallVideoPath);
            mBitmaps.clear();
            slider.setBitmapList(mBitmaps);
//        mFmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ALBUM);
//        mFmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_ARTIST);
            getBitmaps(duration);
        } catch (IllegalArgumentException e) {
            hidepDialog();
        }
    }

    //    private ImageView testimage;
    private List<Bitmap> mBitmaps = new ArrayList<>();

    private void getBitmaps(final long duration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap logo = mFmmr.getScaledFrameAtTime(0, FFmpegMediaMetadataRetriever.OPTION_CLOSEST_SYNC, 640, 360);
                if (logo != null) {
                    try {
                        FileOutputStream fos = new FileOutputStream(logo_path);
                        logo.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                        fos.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                long sub = duration / MAX_FRAMES;
                for (int i = 0; i < MAX_FRAMES; i++) {
                    int time = (int) (i * sub);
                    if (mFmmr == null) break;
                    if (isDestroy) break;
                    Bitmap bitmap = mFmmr.getScaledFrameAtTime(time * 1000000, 64, 36);
                    if (bitmap != null) {
                        mBitmaps.add(bitmap);
                        slider.postInvalidate();
                    }
                }
                releaseMmr();
                videoView.start();
            }
        }).start();
    }

    private void releaseMmr() {
        if (mFmmr != null) {
            mFmmr.release();
            mFmmr = null;
        }
    }

    private MediaPlayer player;

    private void playMusic(int checkedid) {
        String musicpath = getMusicPath(checkedid);
        if (!TextUtils.isEmpty(musicpath)) {
            if (player == null) {
                player = new MediaPlayer();
                player.setLooping(false);
            }
            try {
                if (player.isPlaying()) {
                    player.stop();
                }
                player.reset();
                player.setDataSource(mContext, Uri.parse(musicpath));
                player.prepare();
                player.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (player != null && player.isPlaying()) {
                player.stop();
            }
        }
    }

    private void stopPlayMusic() {
        if (player != null && player.isPlaying()) {
            player.stop();
            player.release();
            player = null;
        }
    }

    @Override
    protected void init() {
        super.init();
        setTitle(getString(R.string.share_video_title));
        fFmpeg = FFmpeg.getInstance(this);
        try {
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
//                    showUnsupportedExceptionDialog();
                }
            });
            checkMusic();
        } catch (FFmpegNotSupportedException e) {
//            showUnsupportedExceptionDialog();
        }
        final Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);//有大档就大档，没有就小档，用于剪辑,也有可能是本地视频
        final Uri smallUri = getIntent().getParcelableExtra("smallUri");//小档，用于播放
        videoType = getIntent().getIntExtra("videoType", 1);
        mEditType = getIntent().getIntExtra("editType", TYPE_SHARE);
        mEncryptType = getIntent().getIntExtra("encryptType", 0);
        mGpsInfos = (ArrayList<GpsInfoBean>) getIntent().getSerializableExtra("gpsInfos");
//        mTvNotice.setText(mEditType == TYPE_SHARE ? R.string.only_support_5_30 : R.string.only_support_5_20);
        mTvNotice.setText(R.string.only_support_5_15);

        // updateSize player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
//        showpDialog();
        if (uri != null) {
            initData(uri, smallUri);
        }
    }

    private void initData(Uri uri, Uri smallUri) {
        Log.e("ryujin", "initData: uri:" + uri + "\nsmallUri:" + smallUri);
        Log.e(_TAG_, uri.getPath());
        //获取剪辑用的路径
        if (uri.toString().contains("http://")) {
            video_path = uri.toString();
        } else {
            video_path = uri.getPath();
        }
        //获取播放用的路径
        if (smallUri != null) {
            if (smallUri.toString().contains("http://")) {
                smallVideoPath = smallUri.toString();
            } else {
                smallVideoPath = smallUri.getPath();
            }
        }
        Log.e("ryujin", "video_path:" + video_path + "\nsmallVideoPath:" + smallVideoPath);
//        rangeSeekBar.setSelectedMinValue(0);
//        rangeSeekBar.setSelectedMaxValue(100);
        showpDialog();
        slider.setMax(100);
        slider.setMin(0);
        slider.reset();
        video_uri = uri;
        videoView.setOnPreparedListener(preparedListener);
        mMediaController = new AndroidMediaController(this, false);
//        videoView.setVideoURI(uri);
        videoView.setVideoURI(smallUri == null ? uri : smallUri);//用小档播放
//        AsyncTaskImageLoad async = new AsyncTaskImageLoad(video_frame);
//        async.execute(uri.getPath());
//        if(videoType==1) {
//            getVideoFrame(uri.getPath(), "640*360", 0);
//        }else if(videoType==0){
//            getVideoFrame(uri.toString(),"640*360", 0);
//        }
        video_play.setOnClickListener(this);
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        if (videoView.isPlaying()) {
                            //刚进来的时候先播放视频然后马上暂停以显示第一帧
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    videoView.pause();
                                }
                            }, 500);
                            hidepDialog();
                            videoView.setOnInfoListener(null);
                            //防止一进来就显示Controller
                            videoView.setMediaController(mMediaController);
                        }
                        break;
                }
                return false;
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                hidepDialog();
                showAlertMessage(R.string.VideoView_error_text_unknown);
                return true;
            }
        });
        cut_select.setOnClickListener(this);
//        delete_select.setOnClickListener(this);
        voice.setOnClickListener(this);
        timerHandler.removeMessages(0);
        timerHandler.sendEmptyMessageDelayed(0, 500);
    }

    private Handler timerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (videoView.isPlaying()) {
                int currentPosition = videoView.getCurrentPosition();
                int duration = videoView.getDuration();
                int percent = slider.getSelectedMax();
                int selectedMin = slider.getSelectedMin();
                if (currentPosition >= duration * percent / 100) {
                    videoView.pause();
                    videoView.seekTo(selectedMin * duration / 100);
                }
            }
            timerHandler.sendEmptyMessageDelayed(0, 500);
        }
    };

    /**
     * 比特率，kb/s
     */
    private int bitrate = 0;
    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            if (voice.isSelected()) {
                iMediaPlayer.setVolume(1, 1);
            } else {
                iMediaPlayer.setVolume(0, 0);
            }
            ITrackInfo[] infos = iMediaPlayer.getTrackInfo();
            if (infos != null && infos.length > 0)
                for (ITrackInfo info : infos) {
                    Log.e(_TAG_, "trackinfo=" + info.getInfoInline());
                    if (info.getTrackType() == ITrackInfo.MEDIA_TRACK_TYPE_VIDEO) {
                        String lineinfo = info.getInfoInline();
                        try {
                            String[] lineinfos = lineinfo.split(",");
                            if (lineinfos != null && lineinfos.length > 2) {
                                String bitrateinfo = lineinfos[2];
                                String[] bitrateinfos = bitrateinfo.trim().split(" ");
                                bitrate = Integer.parseInt(bitrateinfos[0]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            long duration = iMediaPlayer.getDuration();
            Log.e(_TAG_, "duration=" + duration);
            video_time.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
            start_time.setText(String.format("%02d:%02d", 0, 0));
            end_time.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
            total_time.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
//            slider.setSelectedMax((int) (30*1000*duration/100));
//            getVideoFrame(video_path, "640*360", 0);
            slider.setDuration(duration);
            videoView.seekTo(0);
            initFmmr(duration / 1000);
        }
    };

//    public class AsyncTaskImageLoad extends AsyncTask<String, Integer, Bitmap> {
//        private ImageView Image = null;
//
//        public AsyncTaskImageLoad(ImageView img) {
//            Image = img;
//        }
//
//        protected Bitmap doInBackground(String... params) {
//            try {
////                Log.e("info", "doInBackground params[0] = " + params[0]);
//                Bitmap comp = getVideoThumbnail(params[0]);
//                return comp;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        protected void onPostExecute(Bitmap result) {
//            if (Image != null && result != null) {
//                Image.setImageBitmap(result);
//            }
//            super.onPostExecute(result);
//        }
//    }

//    /**
//     * 获取视频的第一帧
//     *
//     * @param path
//     * @param widthAndHeight
//     * @param cuttime
//     */
//    private void getVideoFrame(String path, String widthAndHeight, int cuttime) {
//        logo_path = VLCApplication.TEMP_PATH + "/logo.jpg";
//        String baseCmd = "-i %s -y -f image2 -ss %d -t 0.001 -s %s %s";
//        String cmd = String.format(baseCmd, path, cuttime, widthAndHeight, logo_path);
//        Log.e(_TAG_, "getVideoFrame cmd=" + cmd);
//        String[] command = cmd.split(" ");
//        cmdType = 1;
//        if (command.length != 0) {
//            execFFmpegBinary(command, new CmdCallback() {
//                @Override
//                public void onSuccess() {
////                    video_frame.setImageURI(Uri.parse(logo_path));
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        }
//    }

//    /**
//     * 降低比特率
//     *
//     * @param inpath
//     */
//    private void convertVideo(String inpath) {
//        String basecmd = "-y -i %s -b:v %dk -acodec copy -s 1280*720 %s";
//        newPath = getTemppath();
//        String cmd = String.format(basecmd, inpath, CONVERT_BITRATE, newPath);
//        Log.e(_TAG_, "convertVideo cmd=" + cmd);
//        String[] command = cmd.split(" ");
//        if (command.length != 0) {
//            execFFmpegBinary(command, new CmdCallback() {
//                @Override
//                public void onSuccess() {
//                    video_path = newPath;
//                    nextStep();
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        }
//    }

    private String dialog_text;

    /**
     * 裁剪视频
     *
     * @param starttime
     * @param length
     * @param outpath
     * @param callback
     */
    private void cutVideo(int starttime, int length, String outpath, CmdCallback callback) {
        String startStr = String.format("%02d:%02d:%02d", starttime / 3600, (starttime % 3600) / 60, starttime % 60);
        String lengthStr = String.format("%02d:%02d:%02d", length / 3600, (length % 3600) / 60, length % 60);
        String cmd = "";
//        String basecmd="-ss %s -t %s -i %s -vcodec copy -acodec copy %s";

//        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
//        String filename=format.format(new Date())+".mov";
//        newPath= VLCApplication.TEMP_PATH+"/"+filename;
        String inpath = video_uri.getPath();
        if (videoType == 0) {
            inpath = video_uri.toString();
        }
        Log.e("9999", "bitrate = " + bitrate + " , MAX_BITRATE = " + MAX_BITRATE);
//        if (bitrate > MAX_BITRATE) {
//            String basecmd = "-ss %s -t %s -i %s -b:v %dk -acodec copy -s 1920x1080 %s";
////            String basecmd = "-ss %s -t %s -i %s -b:v %dk -acodec copy -r 25 -s 1280x720 -vf \"movie=%s,scale=60:60 [watermark];[in][watermark] overlay=20:20[out]\" %s";
//            cmd = String.format(basecmd, startStr, lengthStr, inpath, CONVERT_BITRATE, outpath);
//        } else {
        String basecmd = "-ss %s -t %s -i %s -vcodec copy -acodec copy %s";
        cmd = String.format(basecmd, startStr, lengthStr, inpath, outpath);
//        }
//        String basecmd = "-ss %s -t %s -i %s -vcodec copy -acodec copy %s";
//        cmd = String.format(basecmd, startStr, lengthStr, inpath, outpath);
        Log.e(_TAG_, "cutVideo cmd=" + cmd);
        String[] command = cmd.split(" ");
        cmdType = 0;
        if (command.length != 0) {
            dialog_text = getString(R.string.cut_warning_text);
            execFFmpegBinary(command, callback);
        }
    }

//    private void mergeVideo(String path1, String path2) {
//        String textfile = VLCApplication.TEMP_PATH + "/mergetext.txt";
//        try {
//            FileOutputStream fos = new FileOutputStream(textfile);
//            String text = String.format("file '%s'", path1) + "\n" + String.format("file '%s'", path2);
//            fos.write(text.getBytes());
//            fos.flush();
//            fos.close();
//            File textt = new File(textfile);
//            if (!textt.canExecute()) {
//                textt.setExecutable(true);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        String baseCmd = "-f concat -i %s -c copy %s";
//        newPath = getTemppath();
//        String cmd = String.format(baseCmd, textfile, newPath);
//        String[] command = cmd.split(" ");
//        if (command.length != 0) {
//            execFFmpegBinary(command, new CmdCallback() {
//                @Override
//                public void onSuccess() {
//                    initData(Uri.parse(newPath), Uri.parse(newPath));
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        }
//    }

    private void seperateVideo(String video) {
        newPath = getTemppath();//VLCApplication.TEMP_PATH+"/test.mov";
        String cmd = "";
//        if (bitrate <= MAX_BITRATE) {
        String baseCmd = "-i %s -vcodec copy -an %s";
        cmd = String.format(baseCmd, video, newPath);
//        } else {
//            String baseCmd = "-i %s -b:v %dk -an %s";
//            cmd = String.format(baseCmd, video, CONVERT_BITRATE, newPath);
//            bitrate = CONVERT_BITRATE;
//        }
        Log.e(_TAG_, "seperateVideo cmd=" + cmd);
        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command, new CmdCallback() {
                @Override
                public void onSuccess() {
//                    initData(Uri.parse(newPath));
                    video_path = newPath;
                    if (musics.getCheckedRadioButtonId() == R.id.music_none) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                writeGps2File();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        nextStep();
                                    }
                                });
                            }
                        }).start();
                    } else {
                        delayHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
//                        mergeAudio(newPath,getMusicPath(musics.getCheckedRadioButtonId()));
                    }
                }

                @Override
                public void onFail() {

                }
            });
        }
    }

    private void mergeAudio(String video, String audio) {
        SystemClock.sleep(1000);
        newPath = getTemppath();//VLCApplication.TEMP_PATH+"/mergeAudio.mov";
//        String baseCmd="-i %s -i %s -vcodec copy -acodec copy %s";
        int length = videoView.getDuration() / 1000;
        String lengthStr = String.format("%d:%d:%d", length / 3600, (length % 3600) / 60, length % 60);
        String temp = VLCApplication.TEMP_PATH + "/tt.mov";
        String cmd;
//        if (bitrate <= MAX_BITRATE) {
//            String baseCmd = "-i %s -ss 0:0:00 -t %s -i %s -vcodec copy -acodec copy %s";
        String baseCmd = "-y -i %s -ss 0:0:00 -t %s -i %s -vcodec copy -acodec copy %s";
        cmd = String.format(baseCmd, video, lengthStr, audio, newPath);
//        } else {
//            String baseCmd = "-y -i %s -ss 0:0:00 -t %s -i %s -b:v %dk -acodec copy %s";
//            cmd = String.format(baseCmd, video, lengthStr, audio, CONVERT_BITRATE, newPath);
//            bitrate = CONVERT_BITRATE;
//        }
        Log.e(_TAG_, "mergeaudio cmd=" + cmd);
        String[] command = cmd.split(" ");
        if (command.length != 0) {
            execFFmpegBinary(command, new CmdCallback() {
                @Override
                public void onSuccess() {
//                    initData(Uri.parse(newPath));
                    video_path = newPath;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            writeGps2File();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    nextStep();
                                }
                            });
                        }
                    }).start();
                }

                @Override
                public void onFail() {

                }
            });
        }
    }

    private Handler delayHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    String audioPath = getMusicPath(musics.getCheckedRadioButtonId());
                    mergeAudio(video_path, audioPath);
                    break;
                case 1:
                    seperateVideo(video_path);
                    break;
            }
        }
    };

    private String getTemppath() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
        int position = video_path.lastIndexOf(".");
        String extern = video_path.substring(position);
        String filename = format.format(new Date()) + extern;
        return VLCApplication.TEMP_PATH + "/" + filename;
    }

    private String getCutPath() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
        int position = video_path.lastIndexOf(".");
        String extern = video_path.substring(position);
        String filename = format.format(new Date()) + extern;
        return VLCApplication.DOWNLOADPATH + "/" + filename;
    }

//    private void deleteVideo(int starttime, final int endtime) {
//        final int totallength = videoView.getDuration() / 1000;
//        if (starttime < 1) {
//            final String path = getTemppath();
//            cutVideo(endtime, totallength - endtime, path, new CmdCallback() {
//                @Override
//                public void onSuccess() {
//                    initData(Uri.parse(path), Uri.parse(path));
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        } else {
//            final String temppath1 = VLCApplication.TEMP_PATH + "/temp1.mov";
//            final String temppath2 = VLCApplication.TEMP_PATH + "/temp2.mov";
//            File tempfile1 = new File(temppath1);
//            File tempfile2 = new File(temppath2);
//            if (tempfile1.exists()) {
//                tempfile1.delete();
//            }
//            if (tempfile2.exists()) {
//                tempfile2.delete();
//            }
//            cutVideo(starttime, endtime - starttime, temppath1, new CmdCallback() {
//                @Override
//                public void onSuccess() {
//                    File tempfile2 = new File(temppath2);
//                    if (tempfile2.exists()) {
//                        mergeVideo(temppath1, temppath2);
//                    } else {
//                        cutVideo(endtime, totallength - endtime, temppath2, this);
//                    }
//                }
//
//                @Override
//                public void onFail() {
//
//                }
//            });
//        }
//    }

    private void execFFmpegBinary(final String[] command, final CmdCallback callback) {
        try {
            fFmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
//                    addTextViewToLayout("FAILED with output : "+s);
                    if (callback != null) {
                        callback.onFail();
                    }
                    Log.e(_TAG_, s);
                    hidepDialog();
                }

                @Override
                public void onSuccess(String s) {
//                    addTextViewToLayout("SUCCESS with output : "+s);
//                    showToast(s);
//                    switch (cmdType){
//                        case 0:
//                            initData(Uri.parse(newPath));
//                            break;
//                        case 1:
//                            video_frame.setImageURI(Uri.parse(logo_path));
//                            break;
//                    }
                    if (callback != null) {
                        callback.onSuccess();
                    }
                }

                @Override
                public void onProgress(String s) {
                    Log.d(_TAG_, "Started command : ffmpeg " + command);
//                    addTextViewToLayout("progress : "+s);
//                    progressDialog.setMessage("Processing\n"+s);
                    if (!TextUtils.isEmpty(dialog_text)) {
                        showpDialog(dialog_text);
                    } else {
                        showpDialog();
                    }
                }

                @Override
                public void onStart() {
//                    outputLayout.removeAllViews();
//
//                    Log.d(TAG, "Started command : ffmpeg " + command);
//                    progressDialog.setMessage("Processing...");
//                    progressDialog.show();
                    if (!TextUtils.isEmpty(dialog_text)) {
                        showpDialog(dialog_text);
                    } else {
                        showpDialog();
                    }
                }

                @Override
                public void onFinish() {
//                    Log.d(TAG, "Finished command : ffmpeg "+command);
//                    progressDialog.dismiss();
//                    hidepDialog();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
            if (callback != null) {
                callback.onFail();
            }
            hidepDialog();
        }
    }

//    public Bitmap getVideoThumbnail(String filePath) {
//        Bitmap bitmap = null;
//        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//        try {
//            retriever.setDataSource(filePath);
//            // 取得视频的长度(单位为毫秒)
//            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
//            // 取得视频的长度(单位为秒)
//            int seconds = Integer.valueOf(time) / 1000;
//            if (seconds >= 1) {
//                bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
//            } else {
//                bitmap = retriever.getFrameAtTime();
//            }
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//        } catch (RuntimeException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                retriever.release();
//            } catch (RuntimeException e) {
//                e.printStackTrace();
//            }
//        }
//        return bitmap;
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_play:
//                video_frame.setVisibility(View.GONE);
                video_play.setVisibility(View.GONE);
                videoView.start();
                break;
            case R.id.cut_select:
                onCutVideo();
                break;
//            case R.id.delete_select:
////                onDeleteVideo();
//                mergeAudio(video_path,VLCApplication.TEMP_PATH+"/tkzc.mp3");
////                seperateVideo(video_path);
//                break;
            case R.id.right_img:
//                nextStep();
//                checkShareData();
//                convertVideo(video_path);
                break;
            case R.id.voice:
                IMediaPlayer mediaPlayer = videoView.getMediaPlayer();
                voice.setSelected(!voice.isSelected());
                if (mediaPlayer != null) {
                    if (!voice.isSelected()) {
                        mediaPlayer.setVolume(0, 0);
                    } else {
                        mediaPlayer.setVolume(1, 1);
                    }
                }
                break;
            case R.id.right_text:
                checkShareData();
                stopPlayMusic();
                break;
            default:
                break;
        }
    }

//    private void onDeleteVideo() {
//        int minValue = rangeSeekBar.getSelectedMinValue();
//        int maxvalue = rangeSeekBar.getSelectedMaxValue();
//        int totaltime = videoView.getDuration() / 1000;
//        int start = totaltime * minValue / 100;
//        int end = totaltime * maxvalue / 100;
//        deleteVideo(start, end);
//    }

    private void onCutVideo() {
//        showpDialog();
        int minValue = slider.getSelectedMin();//rangeSeekBar.getSelectedMinValue();
        int maxvalue = slider.getSelectedMax();//rangeSeekBar.getSelectedMaxValue();
        int totaltime = videoView.getDuration() / 1000;
        int start = totaltime * minValue / 100;
        int length = totaltime * (maxvalue - minValue) / 100;
        if (length < 1) { //(length < 5 || length > 16)
            showToast(R.string.only_support_1);
            return;
        }
//        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss");
//
//        String[] temps=video_path.split(".");
//        String extern=temps[temps.length-1];
//        String filename=format.format(new Date())+extern;
//        if (!TextUtils.isEmpty(mSilenceCutVideoPath)) {
//            File file = new File(mSilenceCutVideoPath);
//            if (file.isFile()) {
//                file.delete();
//            }
//        }
        newPath = getCutPath();//getTemppath();
        if (videoView.isPlaying()) {
            videoView.pause();
        }
        if (mEditType == TYPE_ILLEGAL_REPORT) {
            //获取剪辑片段的gps信息
            if (mGpsInfos != null && mGpsInfos.size() > 0) {
                int frameSize = mGpsInfos.size() / totaltime;
                if (frameSize < 1) {
                    frameSize = 1;
                }
                int startIndex = start * frameSize;
                if (startIndex >= mGpsInfos.size()) {
                    startIndex = mGpsInfos.size() - 1;
                }
                int endIndex = totaltime * maxvalue / 100 * frameSize;
                if (endIndex > mGpsInfos.size()) {
                    endIndex = mGpsInfos.size();
                }
                int size = mGpsInfos.size();
                for (int i = size - 1; i >= endIndex; i--) {
                    mGpsInfos.remove(i);
                }
                if (startIndex > 0) {
                    for (int i = startIndex - 1; i >= 0; i--) {
                        mGpsInfos.remove(i);
                    }
                }
            }
        }
        Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
        intent.putExtra("isVideo", true);
        VLCApplication.getAppContext().sendBroadcast(intent);
        cutVideo(start, length, newPath, new CmdCallback() {
            @Override
            public void onSuccess() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        mSilenceCutVideoPath = newPath;
                        //写gps信息到文件
                        writeGps2File();

                        if (videoType == 1) {//本地
                            String inpath = video_uri.getPath();
                            int[] ints = HbxFishEye.GetId(inpath);
//                            if ((ints[0] & ints[1]) != 0) { //全景文件
                            HbxFishEye.SaveId2File(newPath, ints[0], ints[1]);
//                                Log.e("9999", "是全景文件");
//                            } else {
//                                Log.e("9999", "不是全景文件");
//                            }
                        } else if (videoType == 0) {//需要判断当前是否360摄像头网络
//                            String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
//                            if (product_model.equals("100")) {
                            HbxFishEye.SaveId2File(newPath, 3, 1);
//                                Log.e("9999", "是全景文件");
//                            } else {
//                                Log.e("9999", "不是全景文件");
//                            }
                        }

//                        moveFile(newPath, mSilenceCutVideoPath);
                        dialog_text = "";
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initData(Uri.parse(newPath), Uri.parse(newPath));
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFail() {
                dialog_text = "";
            }
        });
    }

    private void writeGps2File() {
        new GpsWriter().writeGps2mp4(newPath, mGpsInfos, mEncryptType);
    }

    /**
     * 检查数据
     */
    private void checkShareData() {
        if (video_path.startsWith("http://")) {
            showToast(R.string.cut_video_share);
            return;
        }
        int duration = videoView.getDuration();
        if (mEditType == TYPE_SHARE) {
            if (duration < 1000) {
                showToast(R.string.only_support_1);
                return;
            }
//            if (duration > 16000) {
//                showToast(R.string.cut_video_first);
//                return;
//            }
        } else {
            if (duration < 1000) {
                showToast(R.string.only_support_1);
                return;
            }
//            if (duration > 16000) {
//                showToast(R.string.cut_video_first);
//                return;
//            }
        }
        if (voice.isSelected() && musics.getCheckedRadioButtonId() == R.id.music_none) {
//            if (bitrate > MAX_BITRATE) {
//                convertVideo(video_path);
//            } else {
            nextStep();
//            }
        } else {
            if (!voice.isSelected()) {
                delayHandler.sendEmptyMessageDelayed(1, DELAY_TIME);
//                seperateVideo(video_path);
            } else {
                delayHandler.sendEmptyMessageDelayed(0, DELAY_TIME);
//                String audioPath= getMusicPath(musics.getCheckedRadioButtonId());
//                mergeAudio(video_path,audioPath);
            }
        }
    }

    private String getMusicPath(int checkedid) {
        String musicpath = "";
        switch (checkedid) {
            case R.id.music_none:

                break;
            case R.id.music_1:
                musicpath = VLCApplication.MUSIC_PATH + "/m1.mp3";
                break;
            case R.id.music_2:
                musicpath = VLCApplication.MUSIC_PATH + "/m2.mp3";
                break;
            case R.id.music_3:
                musicpath = VLCApplication.MUSIC_PATH + "/m3.mp3";
                break;
            case R.id.music_4:
                musicpath = VLCApplication.MUSIC_PATH + "/m4.mp3";
                break;
            case R.id.music_5:
                musicpath = VLCApplication.MUSIC_PATH + "/m5.mp3";
                break;
            case R.id.music_6:
                musicpath = VLCApplication.MUSIC_PATH + "/m6.mp3";
                break;
            case R.id.music_7:
                musicpath = VLCApplication.MUSIC_PATH + "/m7.mp3";
                break;
            case R.id.music_8:
                musicpath = VLCApplication.MUSIC_PATH + "/m8.mp3";
                break;
            case R.id.music_9:
                musicpath = VLCApplication.MUSIC_PATH + "/m9.mp3";
                break;
            case R.id.music_10:
                musicpath = VLCApplication.MUSIC_PATH + "/m10.mp3";
                break;
            default:
                break;
        }
        return musicpath;
    }

    private void nextStep() {
        hidepDialog();
        String savepath = video_path.replace("temp", "downloads");
        if (!savepath.equals(video_path)) {
            moveFile(video_path, savepath);
        }
        Intent broadIntent = new Intent(AlbumFragment.ACTION_FRESH);
        broadIntent.putExtra("isVideo", true);
        VLCApplication.getAppContext().sendBroadcast(broadIntent);
        int duration = videoView.getDuration();
        Intent intent = new Intent();
        if (mEditType == TYPE_SHARE) {
            new ShareUtils().shareVideo((Activity) mContext, savepath, logo_path, duration / 1000);
        } else {
            if (!VoiceManager.isLogin) {
                Intent login = new Intent(mContext, LoginActivity.class);
                startActivity(login);
                return;
            } else {
                intent.setClass(mContext, IllegalReportActivity.class);
                intent.putExtra("video_path", savepath);
                intent.putExtra("video_logo_path", logo_path);
                intent.putExtra("video_time", duration / 1000);
                intent.putExtra("encryptType", mEncryptType);
                intent.putExtra("gpsInfos", mGpsInfos);
                startActivity(intent);
                finish();
            }
            File files[] = new File(VLCApplication.TEMP_PATH).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (FileTypeUtil.TYPE_VIDEO == FileTypeUtil.getFileType(f.getPath())) {
                        f.delete();
                    }
                }
            }
        }

    }

    private void checkMusic() {
        try {
            String[] musics = getAssets().list("music");
            File music_path = new File(VLCApplication.MUSIC_PATH);
            if (!music_path.exists()) {
                music_path.mkdirs();
            }
            for (String music : musics) {
                File file = new File(VLCApplication.MUSIC_PATH + "/" + music);
                if (!file.exists()) {
                    copyFile("music/" + music, file.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFile(String source, String path) {
        Log.e("9527", "copyFile source = " + source);
        Log.e("9527", "copyFile path = " + path);
        try {
            InputStream inputStream = getAssets().open(source);
            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int moveFile(String fromFile, String toFile) {
        Log.e("9527", "moveFile fromFile = " + fromFile);
        Log.e("9527", "moveFile toFile = " + toFile);
        try {
            InputStream fosfrom = new FileInputStream(fromFile);
            OutputStream fosto = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosfrom.read(bt)) > 0) {
                fosto.write(bt, 0, c);
            }
            fosfrom.close();
            fosto.close();

            if (videoType == 1) {//本地
                String inpath = video_uri.getPath();
                int[] ints = HbxFishEye.GetId(inpath);
                if ((ints[0] & ints[1]) != 0) { //全景文件
                    HbxFishEye.SaveId2File(toFile, ints[0], ints[1]);
                    Log.e(TAG, "是全景文件");
                } else {
                    Log.e(TAG, "非全景文件");
                }
            } else if (videoType == 0) {//需要判断当前是否360摄像头网络
                WifiManager mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                String curSSID = mWifiManager.getConnectionInfo().getSSID();
                if (curSSID.startsWith("\"") && curSSID.endsWith("\"")) {
                    curSSID = curSSID.substring(1, curSSID.length() - 1);
                }
                if(curSSID.startsWith("X215")){
                    HbxFishEye.SaveId2File(toFile, 3, 1);
                    Log.e(TAG, "是全景文件");
                } else {
                    Log.e(TAG, "非全景文件");
                }

//                String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
//                if (product_model.equals("100")) {
//                    HbxFishEye.SaveId2File(toFile, 3, 1);
//                    Log.e(TAG, "是全景文件");
//                } else {
//                    Log.e(TAG, "非全景文件");
//                }
            }

            return 0;

        } catch (Exception ex) {
            return -1;
        }
    }

    private boolean isDestroy;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        Log.e(TAG, "onDestroy: ");
//        releaseMmr();
        videoView.stopPlayback();
        Log.e(TAG, "onDestroy: stopPlayback");
        videoView.release(true);
        Log.e(TAG, "onDestroy: videoView.release(true)");
        videoView.stopBackgroundPlay();
        Log.e(TAG, "onDestroy: videoView.stopBackgroundPlay()");
        timerHandler.removeMessages(0);
        IjkMediaPlayer.native_profileEnd();
        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }
    }
}
