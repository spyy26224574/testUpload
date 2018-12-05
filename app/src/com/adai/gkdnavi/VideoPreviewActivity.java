package com.adai.gkdnavi;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
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
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.gpsvideo.GpsInfoBean;
import com.adai.gkdnavi.gpsvideo.GpsParser;
import com.adai.gkdnavi.utils.GpsUtil;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.example.ipcamera.domain.FileDomain;
import com.ijk.media.widget.media.IjkVideoView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.widget.piechart.ScreenUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static android.net.NetworkInfo.State.CONNECTED;

public class VideoPreviewActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final String KEY_FILES = "KEY_FILES";
    public static final String KEY_POSTION = "KEY_POSTION";
    public static final String KEY_TYPE = "KEY_TYPE";
    private static final int SHOW_PROGRESS = 1;
    private static final int SHOW_GPS = 2;
    private static final int NO_DEVICE_CONNECT = 3;
    private IjkVideoView videoview;
    private ImageButton mIbHorizontalGps;
    private RelativeLayout mIbVerticalGps, verticalmenu;
    private ImageView verticalprevious;
    private RelativeLayout mIvIllegalReport;
    private ImageView verticalplay;
    private ImageView verticalnext;
    private AppCompatSeekBar verticalseekbar;
    private TextView verticaltime;
    private ListView verticallist;
    private LinearLayout verticalframe;
    private ImageButton horizontalmenu;
    private ImageView horizontalprevious;
    private ImageView horizontalplay;
    private ImageView horizontalnext;
    private ImageView mVideoFrame;
    private AppCompatSeekBar horizontalseekbar;
    private TextView horizontaltime;
    private LinearLayout horizontalbottom;
    private ListView horizontallist;
    private RelativeLayout horizontalframe;
    private View head_frame;
    private ImageView back, iv_fullscreen, iv_fullscreen_land;
    private TextView title;
    private RelativeLayout activityvideppreview;
    private List<FileDomain> files = null;
    private FileDomain currentFile = null;
    private ListAdapter horizontal_adapter, vertical_adapter;
    private List<GpsInfoBean> _gpsinfos;
    private LinearLayout mHorizontalMapFrame;
    private RelativeLayout rl_video_view;
//    private ImageView mIvFullscreen;
    /**
     * 视频类型,本地还是摄像头，0为默认为摄像头，1为本地,2为网络
     */
    private int type = 0;

    private MapView mVerticalMapView, mHorizontalMapView;
    private Marker mVerticalMarker, mHorizontalMarker;
    //    private WifiManager mWifiManager;
    private BitmapDescriptor mIcon;
    private ProgressBar mPbBuffer;
    private int mEncryptType;
    LinearLayout ll_title_land;

    private ImageView iv_illegal_report_land;
    private int mVideoWidth;
    private int mVideoHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_video_preview);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        ll_title_land = (LinearLayout) findViewById(R.id.ll_title_land);
        iv_illegal_report_land = (ImageView) findViewById(R.id.iv_illegal_report_land);
        iv_illegal_report_land.setOnClickListener(this);

        mPbBuffer = (ProgressBar) findViewById(R.id.pb_buffer);
        this.activityvideppreview = (RelativeLayout) findViewById(R.id.activity_video_preview);
        this.title = (TextView) findViewById(R.id.title);
        this.back = (ImageView) findViewById(R.id.back);
        this.horizontalframe = (RelativeLayout) findViewById(R.id.horizontal_frame);
        this.horizontallist = (ListView) findViewById(R.id.horizontal_list);
        this.horizontalbottom = (LinearLayout) findViewById(R.id.horizontal_bottom);
        this.horizontaltime = (TextView) findViewById(R.id.horizontal_time);
        this.horizontalseekbar = (AppCompatSeekBar) findViewById(R.id.horizontal_seekbar);
        this.horizontalnext = (ImageView) findViewById(R.id.horizontal_next);
        this.horizontalplay = (ImageView) findViewById(R.id.horizontal_play);
        this.horizontalprevious = (ImageView) findViewById(R.id.horizontal_previous);
        this.horizontalmenu = (ImageButton) findViewById(R.id.horizontal_menu);
        this.verticalframe = (LinearLayout) findViewById(R.id.vertical_frame);
        this.verticallist = (ListView) findViewById(R.id.vertical_list);
        this.verticaltime = (TextView) findViewById(R.id.vertical_time);
        this.verticalseekbar = (AppCompatSeekBar) findViewById(R.id.vertical_seekbar);
        this.verticalnext = (ImageView) findViewById(R.id.vertical_next);
        this.verticalplay = (ImageView) findViewById(R.id.vertical_play);
        this.verticalprevious = (ImageView) findViewById(R.id.vertical_previous);
        this.verticalmenu = (RelativeLayout) findViewById(R.id.vertical_menu);
        this.videoview = (IjkVideoView) findViewById(R.id.video_view);
        iv_fullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
        mVideoFrame = (ImageView) findViewById(R.id.iv_view_frame);
        rl_video_view = (RelativeLayout) findViewById(R.id.rl_video_view);
//        mIvFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
//        mIvFullscreen.setOnClickListener(this);
        mIvIllegalReport = (RelativeLayout) findViewById(R.id.iv_illegal_report);
//        videoview.setAspectRatio(IRenderView.AR_MATCH_PARENT);
        videoview.setOnVideoSizeChangedListener(new IMediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(IMediaPlayer iMediaPlayer, int i, int i1, int i2, int i3) {
                mVideoWidth = iMediaPlayer.getVideoWidth();
                mVideoHeight = iMediaPlayer.getVideoHeight();
                toggleFrame(false);
            }
        });
        videoview.setKeepScreenOn(true);
        videoview.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                showTime(0, iMediaPlayer.getDuration());
                verticalseekbar.setMax((int) iMediaPlayer.getDuration());
                horizontalseekbar.setMax((int) iMediaPlayer.getDuration());
                changePlayState(true);
            }
        });
        videoview.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                changePlayState(false);
                setProgress();
            }
        });
        videoview.setRender(IjkVideoView.RENDER_SURFACE_VIEW);
        this.head_frame = findViewById(R.id.head_frame);
        mIbVerticalGps = (RelativeLayout) findViewById(R.id.ib_vertical_gps);
        mIbVerticalGps.setOnClickListener(this);
        mIbHorizontalGps = (ImageButton) findViewById(R.id.horizontal_ib_gps);
//        mIbHorizontalGps.setOnClickListener(this);
        mHorizontalMapView = (MapView) findViewById(R.id.horizontal_mapView);
        mHorizontalMapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));//设置定位模式以及一些marker的属性
        mHorizontalMapView.getMap().setMyLocationEnabled(true);
        mHorizontalMapView.getMap().setMaxAndMinZoomLevel(21, 10);
        mVerticalMapView = (MapView) findViewById(R.id.vertical_mapView);
        mVerticalMapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));//设置定位模式以及一些marker的属性
        // 开启定位图层
        mVerticalMapView.getMap().setMyLocationEnabled(true);
        mVerticalMapView.getMap().setMaxAndMinZoomLevel(21, 10);
        mHorizontalMapFrame = (LinearLayout) findViewById(R.id.horizontal_map_frame);
        horizontalplay.setOnClickListener(this);
        verticalplay.setOnClickListener(this);

        horizontalmenu.setOnClickListener(this);
        verticalmenu.setOnClickListener(this);

        horizontalprevious.setOnClickListener(this);
        verticalprevious.setOnClickListener(this);

        horizontalnext.setOnClickListener(this);
        verticalnext.setOnClickListener(this);

        activityvideppreview.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        iv_fullscreen_land.setOnClickListener(this);

        horizontalseekbar.setOnSeekBarChangeListener(this);
        verticalseekbar.setOnSeekBarChangeListener(this);
        mIvIllegalReport.setOnClickListener(this);
        findViewById(R.id.share_land).setOnClickListener(this);
        toggleFrame(false);
    }

    @Override
    protected void init() {
        super.init();
        Intent data = getIntent();
        if (data.hasExtra(KEY_FILES)) {
            files = (List<FileDomain>) data.getSerializableExtra(KEY_FILES);
        }
        if (files == null || files.size() <= 0) return;
        if (data.hasExtra(KEY_POSTION)) {
            int postion = data.getIntExtra(KEY_POSTION, -1);
            if (postion < 0) return;
            currentFile = files.get(postion);
        }
        type = data.getIntExtra(KEY_TYPE, 0);
        horizontal_adapter = new ListAdapter();
        vertical_adapter = new ListAdapter();
        horizontallist.setAdapter(horizontal_adapter);
        verticallist.setAdapter(vertical_adapter);

        horizontallist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parserVideo(files.get(position));
            }
        });
        verticallist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parserVideo(files.get(position));
            }
        });
        if (type == 0) {
            if (CameraFactory.PRODUCT == CameraFactory.ID_Novatek) {
                ImageLoader.getInstance().displayImage((Contacts.BASE_HTTP_IP + currentFile.getFpath().substring(currentFile.getFpath().indexOf(":") + 1)).replace("\\", "/") + Contacts.URL_GET_THUMBNAIL_END,
                        mVideoFrame);
            }
            ll_title_land.setVisibility(View.GONE);
        }
        parserVideo(currentFile);

    }

    private boolean isShowGps = false;

    private void parserVideo(final FileDomain fileDomain) {
        if (type == 1) {
            //本地视频才解析gps信息，有gps信息的视频才能举报
            GpsParser parser = new GpsParser();
            parser.setCallback(new GpsParser.GpsInfoCallback() {
                @Override
                public void onGpsInfo(List<GpsInfoBean> gpsinfos, int encryptType) {
                    int orientation = getResources().getConfiguration().orientation;
                    mEncryptType = encryptType;
                    _gpsinfos = gpsinfos;
                    if (gpsinfos != null && gpsinfos.size() > 1) {
                        mIbVerticalGps.setVisibility(View.VISIBLE);
                        isShowGps = true;
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                            mIbHorizontalGps.setVisibility(View.VISIBLE);
                        }
                        mVerticalMapView.getMap().clear();
                        mHorizontalMapView.getMap().clear();
                        if (mIcon == null) {
                            mIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark1);
                        }
                        double[] doubles = GpsUtil.gps84_To_bd09(_gpsinfos.get(0).latitude, _gpsinfos.get(0).longitude);
                        LatLng latLng = new LatLng(doubles[0], doubles[1]);
                        OverlayOptions maker = new MarkerOptions().position(latLng)
                                .icon(mIcon);
                        mVerticalMarker = (Marker) mVerticalMapView.getMap().addOverlay(maker);
                        mHorizontalMarker = (Marker) mHorizontalMapView.getMap().addOverlay(maker);
                        List<LatLng> tempGps = new ArrayList<>();
                        for (GpsInfoBean gpsInfoBean : _gpsinfos) {
                            double[] bdGps = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude);
                            LatLng latLng1 = new LatLng(bdGps[0], bdGps[1]);
                            tempGps.add(latLng1);
                        }
                        PolylineOptions points = new PolylineOptions().width(10)
                                .color(getResources().getColor(R.color.orange))
                                .points(tempGps);
                        mVerticalMapView.getMap().addOverlay(points);
                        mHorizontalMapView.getMap().addOverlay(points);
                        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
                        mVerticalMapView.getMap().setMapStatus(msu);
                        mHorizontalMapView.getMap().setMapStatus(msu);
                    } else {
                        isShowGps = false;
                        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                            mIbHorizontalGps.setVisibility(View.GONE);
                        }
                        mIbVerticalGps.setVisibility(View.GONE);
                        mVerticalMapView.setVisibility(View.GONE);
                    }
                    showVideo(fileDomain);
                    hidepDialog();
                }
            });
            showpDialog(getString(R.string.getting_gps_info));
            String video_path = fileDomain.getSmallpath();
            if (TextUtils.isEmpty(video_path)) {
                video_path = fileDomain.getFpath();
            }
//            if (type == 0) {
//                video_path = Contacts.BASE_HTTP_IP + video_path.substring(video_path.indexOf(":") + 1);
//                video_path = video_path.replace("\\", "/");
//            }
            parser.parseFile(video_path);
        } else {
            showVideo(fileDomain);
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
                    int pos = setProgress();
                    if (videoview.isPlaying()) {
                        sendEmptyMessageDelayed(SHOW_PROGRESS, 1000 - (pos % 1000));
                    }
                    break;
                case SHOW_GPS:
                    int pos1 = updateMaplocation();
                    if (videoview.isPlaying()) {
                        sendEmptyMessageDelayed(SHOW_GPS, 1000 - (pos1 % 1000));
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

    private int updateMaplocation() {
        if (_gpsinfos == null || _gpsinfos.size() <= 0) return 0;
        int pos = videoview.getCurrentPosition();
        int duration = videoview.getDuration();
        if (duration < 1000) return 0;
        int framesize = _gpsinfos.size() / (duration / 1000);
        if (framesize < 1) {
            framesize = 1;
        }
        int index = (pos / 1000) * framesize;
        if (index >= _gpsinfos.size()) {
            index = _gpsinfos.size() - 1;
        }
        GpsInfoBean gpsInfoBean = _gpsinfos.get(index);
        double[] doubles = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude);
        LatLng latLng = new LatLng(doubles[0], doubles[1]);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mVerticalMapView.getMap().setMapStatus(msu);
        mHorizontalMapView.getMap().setMapStatus(msu);
        mVerticalMapView.getMap().animateMapStatus(msu);
        mHorizontalMapView.getMap().animateMapStatus(msu);
        mVerticalMarker.setPosition(latLng);
        mHorizontalMarker.setPosition(latLng);
        return pos;
    }

    private int setProgress() {
        int position = videoview.getCurrentPosition();
        int duration = videoview.getDuration();
        int bufferPercentage = videoview.getBufferPercentage();

        int secondaryProgress = bufferPercentage * duration / 100;
        if (Math.abs(secondaryProgress - duration) < 1000) {
            secondaryProgress = duration;
        }
        if (horizontalseekbar != null) {
//            horizontalseekbar.setMax(duration);
            horizontalseekbar.setProgress(position);
            horizontalseekbar.setSecondaryProgress(secondaryProgress);
        }
        if (verticalseekbar != null) {
//            verticalseekbar.setMax(duration);
            verticalseekbar.setProgress(position);
            verticalseekbar.setSecondaryProgress(secondaryProgress);
        }

        showTime(position, duration);

        return position;
    }

    private void showVideo(FileDomain file) {
        if (file == null) return;
        currentFile = file;
        mIvIllegalReport.setVisibility(View.GONE);
        iv_illegal_report_land.setVisibility(View.GONE);
//        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
//        format.applyPattern("yyyy_MMdd_HHmmss_SSS");
        String name;
//        try {
//            String substring = name.substring(0, "yyyy_MMdd_HHmmss_SSS".length());
//            if (substring.indexOf("_") == 4) {
//                format.parse(substring);//如果不满足格式将会抛出异常，将不会走下面的代码
        //走到这里说明是camera文件
        boolean aBoolean = SpUtils.getBoolean(this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, false);
        // FIXME: 2017/5/26 测试用，暂时显示违章举报
        mIvIllegalReport.setVisibility(aBoolean && isShowGps ? View.VISIBLE : View.GONE);
        iv_illegal_report_land.setVisibility(aBoolean && isShowGps ? View.VISIBLE : View.GONE);
//        mIvIllegalReport.setVisibility(View.GONE);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        String video_path = getVideoPath(file);
        name = file.getSmallname();
        if (TextUtils.isEmpty(name)) {
            name = file.getName();
        }
        Log.e("ryujin", "showVideo: " + video_path);
        title.setText(name);
        videoview.setVideoPath(video_path);
        horizontal_adapter.notifyDataSetChanged();
        vertical_adapter.notifyDataSetChanged();
        play();
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
        verticaltime.setText(text);
    }

    private void play() {
        videoview.setOnErrorListener(null);
        videoview.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i) {
                    case IjkVideoView.MEDIA_INFO_START_PLAYING:
                        mPbBuffer.setVisibility(View.GONE);
                        mVideoFrame.setVisibility(View.GONE);
                        handler.sendEmptyMessage(SHOW_GPS);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        mPbBuffer.setVisibility(View.VISIBLE);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        mPbBuffer.setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        });
        videoview.start();
        changePlayState(true);
    }

    private void changePlayState(boolean isPlaying) {
        handler.removeMessages(SHOW_PROGRESS);
        if (isPlaying) {
            horizontalplay.setBackgroundResource(R.drawable.play_pause_selector);
            verticalplay.setBackgroundResource(R.drawable.play_pause_selector);
            handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
        } else {
            horizontalplay.setBackgroundResource(R.drawable.play_play_selector);
            verticalplay.setBackgroundResource(R.drawable.play_play_selector);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        if (type == 0) {
//            //摄像头
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    final boolean b = WifiUtil.checkNetwork(mContext, 0);
//                    Log.e(_TAG_, "run: 是否连接到摄像头=" + b);
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (b) {
//                                play();
//                            } else {
//                                showpDialog(R.string.connecting_camera);
//                                int netWordId = SpUtils.getInt(VideoPreviewActivity.this, "netWorkId", -1);
//                                Log.e(_TAG_, "run: netWorkId" + netWordId);
//                                mWifiManager.disconnect();
//                                connectWifiByReflectMethod(netWordId);
//                                setWifiBroadcast();
//                                checkWifiConnect();
//                            }
//                        }
//                    });
//                }
//            }).start();
//
//        } else {
        play();
//        }
    }

    private void setWifiBroadcast() {
        IntentFilter mWifiStateFilter = new IntentFilter();
        mWifiStateFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiStateReceiver, mWifiStateFilter);
    }

    private void unRegisterWifiReceiver() {
        if (mWifiStateReceiver != null) {
            try {
                unregisterReceiver(mWifiStateReceiver);
            } catch (Exception ignored) {
            }
        }
    }

    private BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
        private boolean hadDisconnected;

        @Override
        public void onReceive(Context context, Intent intent) {
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    //如果当前的网络连接成功并且网络连接可用
                    if (info.getType() == ConnectivityManager.TYPE_WIFI)
                        if (CONNECTED == info.getState() && info.isAvailable()) {
                            Log.e(_TAG_, "onReceive: ");
                            if (hadDisconnected) {
                                hadDisconnected = false;
                                unRegisterWifiReceiver();
                                handler.removeMessages(NO_DEVICE_CONNECT);
                                hidepDialog();
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        play();
                                    }
                                }, 1000);
                            }
                        } else {
                            hadDisconnected = true;
                        }
                }
            }

        }
    };

//    private void checkWifiConnect() {
//        handler.sendEmptyMessageDelayed(NO_DEVICE_CONNECT, 15000);
//    }
//
//    private Method connectWifiByReflectMethod(int netId) {
//        Method connectMethod = null;
//        if (mWifiManager == null) {
//            mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            // 反射方法： connect(int, listener) , 4.2 <= phone's android version
//            for (Method methodSub : mWifiManager.getClass()
//                    .getDeclaredMethods()) {
//                if ("connect".equalsIgnoreCase(methodSub.getName())) {
//                    Class<?>[] types = methodSub.getParameterTypes();
//                    if (types != null && types.length > 0) {
//                        if ("int".equalsIgnoreCase(types[0].getName())) {
//                            connectMethod = methodSub;
//                        }
//                    }
//                }
//            }
//            if (connectMethod != null) {
//                try {
//                    connectMethod.invoke(mWifiManager, netId, null);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.JELLY_BEAN) {
//            // 反射方法: connect(Channel c, int networkId, ActionListener listener)
//            // 暂时不处理4.1的情况 , 4.1 == phone's android version
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
//                && Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//            // 反射方法：connectNetwork(int networkId) ,
//            // 4.0 <= phone's android version < 4.1
//            for (Method methodSub : mWifiManager.getClass()
//                    .getDeclaredMethods()) {
//                if ("connectNetwork".equalsIgnoreCase(methodSub.getName())) {
//                    Class<?>[] types = methodSub.getParameterTypes();
//                    if (types != null && types.length > 0) {
//                        if ("int".equalsIgnoreCase(types[0].getName())) {
//                            connectMethod = methodSub;
//                        }
//                    }
//                }
//            }
//            if (connectMethod != null) {
//                try {
//                    connectMethod.invoke(mWifiManager, netId);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return null;
//                }
//            }
//        } else {
//            // < android 4.0
//            return null;
//        }
//        return connectMethod;
//    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    private void pause() {
        videoview.pause();
        changePlayState(false);
    }

    private void playPrevious() {
        if (files == null || files.size() <= 1) return;
        int postion = files.indexOf(currentFile) - 1;
        if (postion < 0) {
            postion = files.size() - 1;
        } else if (postion >= files.size()) {
            postion = 0;
        }
//        showVideo(files.get(postion));
        parserVideo(files.get(postion));
    }

    private void playNext() {
        if (files == null || files.size() <= 1) return;
        int postion = files.indexOf(currentFile) + 1;
        if (postion < 0) {
            postion = files.size() - 1;
        } else if (postion >= files.size()) {
            postion = 0;
        }
//        showVideo(files.get(postion));
        parserVideo(files.get(postion));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_fullscreen:
            case R.id.iv_fullscreen_land:
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case R.id.vertical_play:
            case R.id.horizontal_play:
                if (videoview.isPlaying()) {
                    pause();
                } else {
                    play();
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
                if (mHorizontalMapFrame.getVisibility() == View.VISIBLE) {
//                    horizontallist.setVisibility(View.VISIBLE);
                    mHorizontalMapFrame.setVisibility(View.GONE);
                } else {
//                    horizontallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                break;
            case R.id.vertical_menu:
                if (mVerticalMapView.getVisibility() == View.VISIBLE) {
                    verticallist.setVisibility(View.VISIBLE);
                    mVerticalMapView.setVisibility(View.GONE);
                } else {
                    verticallist.setVisibility(verticallist.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                break;
            case R.id.activity_video_preview:
                toggleFrame();
                break;
            case R.id.share_land:
                new ShareUtils().shareVideo(this, currentFile.fpath);
                //                gotoEdit(EditVideoActivity.TYPE_SHARE);
                break;
            case R.id.ib_vertical_gps:
                mVerticalMapView.setVisibility(mVerticalMapView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.horizontal_ib_gps:
                mHorizontalMapFrame.setVisibility(mHorizontalMapFrame.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.iv_illegal_report:
            case R.id.iv_illegal_report_land:
                gotoEdit(EditVideoActivity.TYPE_ILLEGAL_REPORT);
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggleFrame(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        toggleFrame();
        return super.onTouchEvent(event);
    }

    private boolean isShowingFrame() {
        return head_frame.getVisibility() == View.VISIBLE;
    }

    private void toggleFrame() {
        toggleFrame(true);
    }

    private void toggleFrame(boolean change) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        int screenW = ScreenUtils.getScreenW(this);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            iv_fullscreen.setBackgroundResource(R.drawable.selector_exit_fullscreen);
//            iv_fullscreen.setVisibility(View.GONE);
//            ll_title_land.setVisibility(View.VISIBLE);
            if (change) {
//                boolean showingFrame = isShowingFrame();
                boolean showingFrame = (horizontalframe.getVisibility() == View.VISIBLE);
//                head_frame.setVisibility(showingFrame ? View.GONE : View.VISIBLE);
                horizontalframe.setVisibility(showingFrame ? View.GONE : View.VISIBLE);

            } else {
                //屏幕旋转
//                mIbHorizontalGps.setVisibility(isShowGps ? View.VISIBLE : View.GONE);
                horizontalframe.setVisibility(isShowingFrame() ? View.VISIBLE : View.GONE);
                verticalframe.setVisibility(View.GONE);
//                horizontallist.setVisibility(verticallist.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
//                mHorizontalMapFrame.setVisibility(mVerticalMapView.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
//                mVideoFrame.setLayoutParams(layoutParams);
//                videoview.setLayoutParams(layoutParams);
                rl_video_view.setLayoutParams(layoutParams);
            }
        } else {
            horizontalframe.setVisibility(View.GONE);
//            ll_title_land.setVisibility(View.GONE);
//            iv_fullscreen.setVisibility(View.VISIBLE);
//            iv_fullscreen.setBackgroundResource(R.drawable.selector_fullscreen);
            if (change) {
//                head_frame.setVisibility(isShowingFrame() ? View.GONE : View.VISIBLE);

//                head_frame.setVisibility(showingFrame ? View.GONE : View.VISIBLE);

            } else {
                //屏幕旋转
//                mIbHorizontalGps.setVisibility(View.GONE);
                verticalframe.setVisibility(View.VISIBLE);
                horizontalframe.setVisibility(View.GONE);
//                mVerticalMapView.setVisibility(mHorizontalMapFrame.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
//                verticallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                if (mVideoHeight != 0 && mVideoWidth != 0) {
                    layoutParams.height = mVideoHeight * screenW / mVideoWidth;
                } else {
                    layoutParams.height = 9 * screenW / 16;
                }
//                mVideoFrame.setLayoutParams(layoutParams);
//                videoview.setLayoutParams(layoutParams);
                layoutParams.addRule(RelativeLayout.BELOW, R.id.head_frame);
                rl_video_view.setLayoutParams(layoutParams);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
//        int postion = progress * videoview.getDuration() / 100;
//        videoview.seekTo(postion);
//        showTime(videoview.getCurrentPosition(), videoview.getDuration());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        handler.removeMessages(SHOW_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(_TAG_, "onStopTrackingTouch: ");
        int progress = seekBar.getProgress();
//        int postion = progress * videoview.getDuration() / 100;
        videoview.seekTo(progress);
        handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
    }

    public String getVideoPath(FileDomain file) {
        String video_path = file.baseUrl + file.getSmallpath();
        if (TextUtils.isEmpty(file.getSmallpath())) {
            video_path = file.baseUrl + file.getFpath();
        }
        if (type == 0) {
            if (CameraFactory.PRODUCT == CameraFactory.ID_Novatek) {
                video_path = Contacts.BASE_HTTP_IP + video_path.substring(video_path.indexOf(":") + 1);
            }
            video_path = video_path.replace("\\", "/");
        }
        return video_path;
    }

    public String getSmallVideoPath(FileDomain file) {
        if (TextUtils.isEmpty(file.getSmallpath())) {
            return null;
        }
        String smallVideoPath = file.baseUrl + file.getSmallpath();
        if (type == 0) {
            if (CameraFactory.PRODUCT == CameraFactory.ID_Novatek) {
                smallVideoPath = Contacts.BASE_HTTP_IP + smallVideoPath.substring(smallVideoPath.indexOf(":") + 1);
                smallVideoPath = smallVideoPath.replace("\\", "/");
            }
        }
        return smallVideoPath;
    }

    private void gotoEdit(int editType) {
        Intent share = new Intent(Intent.ACTION_SEND);
        String path = getVideoPath(currentFile);
        String smallPath = getSmallVideoPath(currentFile);
//        if (type == 0) {
//            if (!TextUtils.isEmpty(smallPath)) {
//                smallPath = Contacts.BASE_HTTP_IP + smallPath.substring(smallPath.indexOf(":") + 1);
//                smallPath = smallPath.replace("\\", "/");
//            }
//            path = Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1);
//            path = path.replace("\\", "/");
//        }
        Log.e("9527", " path = " + path);
        share.setType("video/*");
        share.setComponent(new ComponentName(getPackageName(), "com.adai.gkdnavi.EditVideoActivity"));
        share.putExtra("videoType", type);
        share.putExtra("editType", editType);
        share.putExtra("encryptType", mEncryptType);
        share.putExtra("gpsInfos", (Serializable) _gpsinfos);
        Log.e("9527", " Uri.parse(path) = " + Uri.parse(path));
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        if (!TextUtils.isEmpty(smallPath)) {
            share.putExtra("smallUri", Uri.parse(smallPath));
        }
        //摄像头文件只支持4个同时的操作，为了避免剪辑视频失败，跳到剪辑页面前释放视频
        videoview.stopPlayback();
        videoview.stopBackgroundPlay();
        videoview.release(true);
        //为了解决从编辑页面返回并没有连接摄像头时莫名提示加载出错的问题
        videoview.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                return true;
            }
        });
        startActivity(share);
    }

    class ListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return files == null ? 0 : files.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.simple_list_item, null);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            FileDomain fileDomain = files.get(position);
            String name = fileDomain.getSmallname();
            if (TextUtils.isEmpty(name)) {
                name = fileDomain.getName();
            }
            textView.setText(name);
            if (fileDomain == currentFile) {
                textView.setTextColor(getResources().getColor(R.color.orange));
            } else {
                textView.setTextColor(getResources().getColor(R.color.white));
            }
            return convertView;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (videoview.isPlaying()) {
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegisterWifiReceiver();
        handler.removeMessages(NO_DEVICE_CONNECT);
        videoview.stopPlayback();
        videoview.release(true);
        videoview.stopBackgroundPlay();
    }
}
