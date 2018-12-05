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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.camera.CameraConstant;
import com.adai.camera.CameraFactory;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.gpsvideo.GpsInfoBean;
import com.adai.gkdnavi.utils.GpsUtil;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.WifiUtil;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.model.LatLng;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.ligo.medialib.PanoCamViewLocal;
import com.ligo.medialib.PanoCamViewOnline;
import com.ligo.medialib.opengl.VideoRenderHard;
import com.ligo.medialib.opengl.VideoRenderYuv;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.widget.piechart.ScreenUtils;

import java.io.Serializable;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;

import static android.net.NetworkInfo.State.CONNECTED;

public class PanoVideoPreviewActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, PanoCamViewLocal.OnChangeListener {
    public static final String TAG = "PanoVideoPreviewActivity";
    public static final String KEY_FILES = "KEY_FILES";
    public static final String KEY_POSTION = "KEY_POSTION";
    public static final String KEY_TYPE = "KEY_TYPE";
    private static final int SHOW_PROGRESS = 1;
    private static final int SHOW_GPS = 2;
    private static final int NO_DEVICE_CONNECT = 3;
    private static final int START_CHANGE_FILE = 4;
    private static final int STOP_CHANGE_FILE = 5;
    private boolean inFilechange = false;
    private RelativeLayout mIbVerticalGps;
    private RelativeLayout verticalmenu;
    //    private ImageView verticalprevious;
    private RelativeLayout mIvIllegalReport;
    private ImageView iv_illegal_report_land;
    private ImageView verticalplay;
    //    private ImageView verticalnext;
    private AppCompatSeekBar verticalseekbar;
    private TextView verticaltime;
    private ListView verticallist;
    private LinearLayout verticalframe;
    private ImageView horizontalmenu;
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
    private RelativeLayout activity_video_preview;
    private List<FileDomain> files = null;
    private FileDomain currentFile = null;
    private ListAdapter horizontal_adapter, vertical_adapter;
    private List<GpsInfoBean> _gpsinfos;
    private RelativeLayout rl_video_view;
    /**
     * 视频类型,本地还是摄像头，0为默认为摄像头，1为本地,2为网络
     */
    private int type = 0;

    private MapView mVerticalMapView;
    private Marker mVerticalMarker;
    private BitmapDescriptor mIcon;
    private ProgressBar mPbBuffer;
    private int mEncryptType;
    private PanoCamViewLocal mPanoCamViewLocal; //硬解码
    private PanoCamViewOnline mPanoCamViewOnline; //软解码
    private int currentPlayer = 1; //0：硬解码 1：软解码

    LinearLayout ll_iv_pano_type;
    ImageView iv_pano_type;
    ImageView iv_original, iv_front_back, iv_four_direct, iv_wide_single, iv_cylinder;
    ImageView iv_original_vertical, iv_front_back_vertical, iv_four_direct_vertical, iv_wide_single_vertical, iv_cylinder_vertical;
    LinearLayout ll_title_land;

    int panoDisplayType = 1; //全景视图方式
    public static final int VIDEO_SHOW_TYPE_SRC_CIRCLE = 1;
    public static final int VIDEO_SHOW_TYPE_2_SCREEN = 2;
    public static final int VIDEO_SHOW_TYPE_4_SCREEN = 3;
    public static final int VIDEO_SHOW_TYPE_HEMISPHERE = 4;
    public static final int VIDEO_SHOW_TYPE_CYLINDER = 5;

    int[] ints; //视频文件ID
    private Button btn_decode;
    private int mCurrent;
    private int mCurrentPanoType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_pano_video_preview);
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

        ll_iv_pano_type = (LinearLayout) findViewById(R.id.ll_iv_pano_type);
        RelativeLayout.LayoutParams horizontalTitleLayoutParams = (RelativeLayout.LayoutParams) ll_iv_pano_type.getLayoutParams();
        horizontalTitleLayoutParams.width = ScreenUtils.getScreenW(this);
        ll_iv_pano_type.setLayoutParams(horizontalTitleLayoutParams);
        iv_pano_type = (ImageView) findViewById(R.id.iv_pano_type);
        iv_pano_type.setOnClickListener(this);
        ll_title_land = (LinearLayout) findViewById(R.id.ll_title_land);
        iv_original = (ImageView) findViewById(R.id.iv_original);
        iv_front_back = (ImageView) findViewById(R.id.iv_front_back);
        iv_four_direct = (ImageView) findViewById(R.id.iv_four_direct);
        iv_wide_single = (ImageView) findViewById(R.id.iv_wide_single);
        iv_cylinder = (ImageView) findViewById(R.id.iv_cylinder);

        iv_original.setOnClickListener(this);
        iv_front_back.setOnClickListener(this);
        iv_four_direct.setOnClickListener(this);
        iv_wide_single.setOnClickListener(this);
        iv_cylinder.setOnClickListener(this);

        iv_original_vertical = (ImageView) findViewById(R.id.iv_original_vertical);
        iv_front_back_vertical = (ImageView) findViewById(R.id.iv_front_back_vertical);
        iv_four_direct_vertical = (ImageView) findViewById(R.id.iv_four_direct_vertical);
        iv_wide_single_vertical = (ImageView) findViewById(R.id.iv_wide_single_vertical);
        iv_cylinder_vertical = (ImageView) findViewById(R.id.iv_cylinder_vertical);

        iv_original_vertical.setOnClickListener(this);
        iv_front_back_vertical.setOnClickListener(this);
        iv_four_direct_vertical.setOnClickListener(this);
        iv_wide_single_vertical.setOnClickListener(this);
        iv_cylinder_vertical.setOnClickListener(this);
        mPbBuffer = (ProgressBar) findViewById(R.id.pb_buffer);
        this.activity_video_preview = (RelativeLayout) findViewById(R.id.activity_video_preview);
        this.btn_decode = (Button) findViewById(R.id.btn_decode);
        btn_decode.setOnClickListener(this);
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
        this.horizontalmenu = (ImageView) findViewById(R.id.horizontal_menu);
        this.verticalframe = (LinearLayout) findViewById(R.id.vertical_frame);
        this.verticallist = (ListView) findViewById(R.id.vertical_list);
        this.verticaltime = (TextView) findViewById(R.id.vertical_time);
        this.verticalseekbar = (AppCompatSeekBar) findViewById(R.id.vertical_seekbar);
//        this.verticalnext = (ImageView) findViewById(R.id.vertical_next);
        this.verticalplay = (ImageView) findViewById(R.id.vertical_play);
//        this.verticalprevious = (ImageView) findViewById(R.id.vertical_previous);
        this.verticalmenu = (RelativeLayout) findViewById(R.id.vertical_menu);
        iv_fullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
        mVideoFrame = (ImageView) findViewById(R.id.iv_view_frame);
        rl_video_view = (RelativeLayout) findViewById(R.id.rl_video_view);
        mIvIllegalReport = (RelativeLayout) findViewById(R.id.iv_illegal_report);
        iv_illegal_report_land = (ImageView) findViewById(R.id.iv_illegal_report_land);
        this.head_frame = findViewById(R.id.head_frame);
        mIbVerticalGps = (RelativeLayout) findViewById(R.id.ib_vertical_gps);
        mIbVerticalGps.setOnClickListener(this);
        mVerticalMapView = (MapView) findViewById(R.id.vertical_mapView);
        mVerticalMapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));//设置定位模式以及一些marker的属性

        // 开启定位图层
        mVerticalMapView.getMap().setMyLocationEnabled(true);
        mVerticalMapView.getMap().setMaxAndMinZoomLevel(21, 10);
        horizontalplay.setOnClickListener(this);
        verticalplay.setOnClickListener(this);
        horizontalmenu.setOnClickListener(this);
        verticalmenu.setOnClickListener(this);
        horizontalprevious.setOnClickListener(this);
//        verticalprevious.setOnClickListener(this);
        horizontalnext.setOnClickListener(this);
//        verticalnext.setOnClickListener(this);
        activity_video_preview.setOnClickListener(this);
        iv_fullscreen.setOnClickListener(this);
        iv_fullscreen_land.setOnClickListener(this);
        horizontalseekbar.setOnSeekBarChangeListener(this);
        verticalseekbar.setOnSeekBarChangeListener(this);
        mIvIllegalReport.setOnClickListener(this);
        iv_illegal_report_land.setOnClickListener(this);
        findViewById(R.id.share).setOnClickListener(this);
        findViewById(R.id.share_land).setOnClickListener(this);
        this.back.setOnClickListener(this);
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
        if (type == 0) {
            ll_title_land.setVisibility(View.GONE);
        }
        horizontal_adapter = new ListAdapter();
        vertical_adapter = new ListAdapter();
        horizontallist.setAdapter(horizontal_adapter);
        verticallist.setAdapter(vertical_adapter);

        horizontallist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.stopPlay();
                    mPanoCamViewLocal.changePlayer();
                } else {
                    mPanoCamViewOnline.stopPlay();
                    mPanoCamViewOnline.changePlayer();
                }
                parserVideo(files.get(position));
            }
        });
        verticallist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                ll_iv_pano_type.setVisibility(View.GONE);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.stopPlay();
                    mPanoCamViewLocal.changePlayer();
                } else {
                    mPanoCamViewOnline.stopPlay();
                    mPanoCamViewOnline.changePlayer();
                }
                handler.removeMessages(SHOW_PROGRESS);
                parserVideo(files.get(position));

            }
        });
        if (type == 0) {
            if (CameraFactory.PRODUCT == CameraFactory.ID_Novatek) {
                ImageLoader.getInstance().displayImage((Contacts.BASE_HTTP_IP + currentFile.getFpath().substring(currentFile.getFpath().indexOf(":") + 1)).replace("\\", "/") + Contacts.URL_GET_THUMBNAIL_END,
                        mVideoFrame);
            }
        }
        parserVideo(currentFile);
    }

    private boolean isShowGps = false;

    private void parserVideo(final FileDomain fileDomain) {
        Log.e("9997", "parserVideo type =" + type);
        mCurrent = 0;
//        currentPlayer = 0;
//        if (type == 1) {
//            //本地视频才解析gps信息，有gps信息的视频才能举报
//            GpsParser parser = new GpsParser();
//            parser.setCallback(new GpsParser.GpsInfoCallback() {
//                @Override
//                public void onGpsInfo(List<GpsInfoBean> gpsinfos, int encryptType) {
//                    Log.e("9997", "gpsinfos =" + gpsinfos);
//                    mEncryptType = encryptType;
//                    _gpsinfos = gpsinfos;
//                    if (gpsinfos != null && gpsinfos.size() > 1) {
//                        mIbVerticalGps.setVisibility(View.VISIBLE);
//                        isShowGps = true;
//
//                        mVerticalMapView.getMap().clear();
//                        if (mIcon == null) {
//                            mIcon = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark1);
//                        }
//                        double[] doubles = GpsUtil.gps84_To_bd09(_gpsinfos.get(0).latitude, _gpsinfos.get(0).longitude);
//                        LatLng latLng = new LatLng(doubles[0], doubles[1]);
//                        OverlayOptions maker = new MarkerOptions().position(latLng)
//                                .icon(mIcon);
//                        mVerticalMarker = (Marker) mVerticalMapView.getMap().addOverlay(maker);
//                        List<LatLng> tempGps = new ArrayList<>();
//                        for (GpsInfoBean gpsInfoBean : _gpsinfos) {
//                            double[] bdGps = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude);
//                            LatLng latLng1 = new LatLng(bdGps[0], bdGps[1]);
//                            tempGps.add(latLng1);
//                        }
//                        PolylineOptions points = new PolylineOptions().width(10)
//                                .color(getResources().getColor(R.color.orange))
//                                .points(tempGps);
//                        mVerticalMapView.getMap().addOverlay(points);
//                        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
//                        mVerticalMapView.getMap().setMapStatus(msu);
//                    } else {
//                        isShowGps = false;
//
//                        mIbVerticalGps.setVisibility(View.GONE);
//                        mVerticalMapView.setVisibility(View.GONE);
//                    }
//                    showVideo(fileDomain);
//                    hidepDialog();
//                }
//            });
//            showpDialog(getString(R.string.getting_gps_info));
//            String video_path = fileDomain.getSmallpath();
//            if (TextUtils.isEmpty(video_path)) {
//                video_path = fileDomain.getFpath();
//            }
////            if (type == 0) {
////                video_path = Contacts.BASE_HTTP_IP + video_path.substring(video_path.indexOf(":") + 1);
////                video_path = video_path.replace("\\", "/");
////            }
//            parser.parseFile(video_path);
//        } else {
        showVideo(fileDomain);
//        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
//                    Log.e("9527", "SHOW_PROGRESS");
                    int pos = setProgress();
                    if (mPanoCamViewLocal.isPlaying()) {
                        long delayMillis = 1000 - (pos % 1000);
                        sendEmptyMessageDelayed(SHOW_PROGRESS, delayMillis);
                    }
                    break;
                case SHOW_GPS:
                    int pos1 = updateMaplocation();
                    if (currentPlayer == 0) {
                        if (mPanoCamViewLocal.isPlaying()) {
                            sendEmptyMessageDelayed(SHOW_GPS, 1000 - (pos1 % 1000));
                        }
                    } else {
                        if (mPanoCamViewOnline.isPlaying()) {
                            sendEmptyMessageDelayed(SHOW_GPS, 1000 - (pos1 % 1000));
                        }
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
                case START_CHANGE_FILE:
                    inFilechange = true;
                    horizontalnext.setClickable(false);
                    horizontalplay.setClickable(false);
                    horizontalprevious.setClickable(false);

                    verticalplay.setClickable(false);

                    horizontalseekbar.setClickable(false);
                    verticalseekbar.setClickable(false);
                    break;
                case STOP_CHANGE_FILE:
                    inFilechange = false;
                    horizontalnext.setClickable(true);
                    horizontalplay.setClickable(true);
                    horizontalprevious.setClickable(true);

                    verticalplay.setClickable(true);

                    horizontalseekbar.setClickable(true);
                    verticalseekbar.setClickable(true);
                    break;

            }
        }
    };

    private int updateMaplocation() {
        if (_gpsinfos == null || _gpsinfos.size() <= 0) return 0;
//        int pos = mPanoCamViewLocal.getCurrent();
//        int duration = mPanoCamViewLocal.getDuration();
        int pos;
        int duration;
        if (currentPlayer == 0) {
            pos = mPanoCamViewLocal.getCurrent();
            duration = mPanoCamViewLocal.getDuration();
        } else {
            pos = mPanoCamViewOnline.getCurrent();
            duration = mPanoCamViewOnline.getDuration();
        }
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
//        ToastUtil.showShortToast(PanoVideoPreviewActivity.this, "index = " + index + "pos = " + pos + ",duration = " + duration
//                + "doubles[0] = " + doubles[0] + ",doubles[1] = " + doubles[1]);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mVerticalMapView.getMap().setMapStatus(msu);
        mVerticalMapView.getMap().animateMapStatus(msu);
        mVerticalMarker.setPosition(latLng);
        return pos;
    }

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

        if (verticalseekbar != null) {
            verticalseekbar.setMax(duration);
            verticalseekbar.setProgress(current);
        }

        showTime(current, duration);
        return current;

    }

    private void showVideo(FileDomain file) {
        if (file == null) return;
        mCurrentPanoType = 1;
        currentFile = file;
        String video_path = getVideoPath(file);
        String name;

        name = file.getSmallname();
        if (TextUtils.isEmpty(name)) {
            name = file.getName();
        }
        Log.e("ryujin", "showVideo: " + video_path);
        title.setText(name);
        if (currentPlayer == 0) {
            mPanoCamViewOnline.setVisibility(View.GONE);
            mPanoCamViewLocal.setVisibility(View.VISIBLE);
            if (!mPanoCamViewLocal.isInit) {
                mPanoCamViewLocal.setOnChangeListener(this);
                mPanoCamViewLocal.reInit(PanoVideoPreviewActivity.this);
            }
            mPanoCamViewLocal.startPlay(video_path);
        } else {
            mPanoCamViewLocal.setVisibility(View.GONE);
            mPanoCamViewOnline.setVisibility(View.VISIBLE);
            if (!mPanoCamViewOnline.isInit) {
                mPanoCamViewOnline.reInit();
                mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
            }
            showVideoSoft(currentFile);
        }

//        mPanoCamViewLocal.setVisibility(View.VISIBLE);
//        mPanoCamViewOnline.setVisibility(View.GONE);
//        mPanoCamViewLocal.setVisibility(View.INVISIBLE);
//        mPanoCamViewLocal.stopPlay();
//        mPanoCamViewLocal.changePlayer();

//        currentPlayer = 1;

//        mPanoCamViewOnline.setVisibility(View.VISIBLE);
//        mPanoCamViewOnline.reInit();
//        mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);


        mIvIllegalReport.setVisibility(View.GONE);
        iv_illegal_report_land.setVisibility(View.GONE);
        boolean aBoolean = SpUtils.getBoolean(this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, false);
        // FIXME: 2017/5/26 测试用，暂时显示违章举报
        mIvIllegalReport.setVisibility(aBoolean && isShowGps ? View.VISIBLE : View.GONE);

//        ints = HbxFishEye.GetId(video_path);
        ints = new int[2];
//        String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
//        if (product_model.equals("100") && (video_path.contains("http") || (video_path.contains("rtsp")))) {
        ints[0] = 3;
        ints[1] = 1;
//        }
//        mPanoCamViewLocal.startPlay(video_path, ints[0], ints[1]);
        horizontal_adapter.notifyDataSetChanged();
        vertical_adapter.notifyDataSetChanged();
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
//        Log.e("9527", "play_time = " + play_time + " total_time = " + total_time + " text = " + text);
        horizontaltime.setText(text);
        verticaltime.setText(text);
    }

    private void play() {
        if (currentPlayer == 0) {
            if (!mPanoCamViewLocal.isPlaying()) {
                mPanoCamViewLocal.resume();
            }
        } else {
            int decoding_type;
            if (video_path.contains("http")) {
                decoding_type = 1;
            } else {
                decoding_type = 3;
            }
            mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
            mPanoCamViewOnline.startPlay(decoding_type);
        }
        changePlayState(true);
    }

    private void changePlayState(boolean isPlaying) {
        Log.e("9527", "changePlayState isPlaying = " + isPlaying);
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
        play();
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
        handler.sendEmptyMessage(SHOW_GPS);
        Log.e("9999", "resume");
        if (currentPlayer == 0) {
            mPanoCamViewLocal.resume();
        } else {
            mPanoCamViewOnline.resume();
        }
        changePlayState(true);
    }


    private void playPrevious() {
        if (files == null || files.size() <= 1) return;
        int postion = files.indexOf(currentFile) - 1;
        if (postion < 0) {
            postion = files.size() - 1;
        } else if (postion >= files.size()) {
            postion = 0;
        }
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
        parserVideo(files.get(postion));
    }

    private int currentDecodeType = 0;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_decode:
                if (currentFile == null) {
                    return;
                }
                if (currentPlayer == 1) {
                    mCurrent = 0;
                    if (mPanoCamViewOnline.isPause() || mPanoCamViewOnline.isPlaying()) {
                        mCurrent = mPanoCamViewOnline.getCurrent();

                    }
                    mPanoCamViewOnline.changePlayer();
                    if (!mPanoCamViewLocal.isInit) {
                        mPanoCamViewLocal.reInit(this);
                    }
                    mPanoCamViewOnline.setVisibility(View.GONE);
                    mPanoCamViewLocal.setVisibility(View.VISIBLE);
                    mPanoCamViewLocal.startPlay(video_path);
                } else {
                    if (mPanoCamViewLocal.isPause() || mPanoCamViewLocal.isPlaying()) {
                        mCurrent = mPanoCamViewLocal.getCurrent();
                    }
                    mPanoCamViewLocal.changePlayer();
                    if (!mPanoCamViewOnline.isInit) {
                        mPanoCamViewOnline.reInit();
                    }
                    mPanoCamViewLocal.setVisibility(View.GONE);
                    mPanoCamViewOnline.setVisibility(View.VISIBLE);
                    mPanoCamViewOnline.setUrl(video_path);
                    int decoding_type;
                    if (video_path.contains("http")) {
                        decoding_type = 1;
                    } else {
                        decoding_type = 3;
                    }
                    mPanoCamViewOnline.startPlay(decoding_type);
                    mPanoCamViewOnline.seek(mCurrent);
                    mPanoCamViewOnline.onChangeShowType(mCurrentPanoType);
                }
                currentPlayer ^= 1;
//                mPanoCamViewOnline.changeDecodec(currentDecodeType);
                btn_decode.setText(currentPlayer == 1 ? R.string.soft_decode : R.string.hard_decode);
                break;
            case R.id.back:
                PanoVideoPreviewActivity.this.finish();
                break;
            case R.id.iv_pano_type: {
                if (ll_iv_pano_type.getVisibility() == View.VISIBLE) {
                    ll_iv_pano_type.setVisibility(View.GONE);
                } else {
                    ll_iv_pano_type.setVisibility(View.VISIBLE);
                }
            }
            break;
            case R.id.iv_original_vertical:
            case R.id.iv_original:
                panoDisplayType = 1;
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
                setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CIRCLE);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CIRCLE);
                }
                break;
            case R.id.iv_front_back_vertical:
            case R.id.iv_front_back:
                panoDisplayType = 2;
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_front_back);
                setPanoType(VIDEO_SHOW_TYPE_2_SCREEN);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_2_SCREEN);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_2_SCREEN);
                }
                break;
            case R.id.iv_four_direct_vertical:
            case R.id.iv_four_direct:
                panoDisplayType = 3;
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_four_direct);
                setPanoType(VIDEO_SHOW_TYPE_4_SCREEN);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_4_SCREEN);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_4_SCREEN);
                }
                break;
            case R.id.iv_wide_single_vertical:
            case R.id.iv_wide_single:
                panoDisplayType = 4;
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_hemisphere);
                setPanoType(VIDEO_SHOW_TYPE_HEMISPHERE);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_HEMISPHERE);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_HEMISPHERE);
                }
                break;
            case R.id.iv_cylinder_vertical:
            case R.id.iv_cylinder:
                panoDisplayType = 5;
                iv_pano_type.setBackgroundResource(R.drawable.selector_iv_cylinder);
                setPanoType(VIDEO_SHOW_TYPE_CYLINDER);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.onChangeShowType(VideoRenderHard.TYPE_CYLINDER);
                } else {
                    mPanoCamViewOnline.onChangeShowType(VideoRenderYuv.TYPE_CYLINDER);
                }
                break;
            case R.id.iv_fullscreen:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case R.id.iv_fullscreen_land:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.vertical_play:
            case R.id.horizontal_play:
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
//                 else if (mPanoCamViewLocal.isStop()) {
//                } else {
//
//                    mPanoCamViewLocal.startPlay(decoding_type);
//                    changePlayState(true);
//                }
                break;
            case R.id.horizontal_previous:
                handler.sendEmptyMessage(START_CHANGE_FILE);
//            case R.id.vertical_previous:
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.stopPlay();
                } else {
                    mPanoCamViewOnline.stopPlay();
                }
                playPrevious();
                break;
            case R.id.horizontal_next:
//            case R.id.vertical_next:
                handler.sendEmptyMessage(START_CHANGE_FILE);
                if (currentPlayer == 0) {
                    mPanoCamViewLocal.stopPlay();
                } else {
                    mPanoCamViewOnline.stopPlay();
                }
                playNext();
                break;
//            case R.id.horizontal_menu:
//                if (mHorizontalMapFrame.getVisibility() == View.VISIBLE) {
//                    horizontallist.setVisibility(View.VISIBLE);
//                    mHorizontalMapFrame.setVisibility(View.GONE);
//                } else {
//                    horizontallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
//                }
//                break;
            case R.id.vertical_menu:
                if (mVerticalMapView.getVisibility() == View.VISIBLE) {
                    verticallist.setVisibility(View.VISIBLE);
                    mVerticalMapView.setVisibility(View.GONE);
                } else {
                    verticallist.setVisibility(verticallist.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                }
                break;
//            case R.id.activity_video_preview:
//                toggleFrame();
//                break;
            case R.id.share:
            case R.id.share_land:
                new ShareUtils().shareVideo(this, currentFile.fpath);
                //                gotoEdit(EditVideoActivity.TYPE_SHARE);
                break;
            case R.id.ib_vertical_gps:
                mVerticalMapView.setVisibility(mVerticalMapView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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

    private boolean isShowingFrame() {
        return head_frame.getVisibility() == View.VISIBLE;
    }

    private void toggleFrame() {
        toggleFrame(true);
    }

    private void toggleFrame(boolean change) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        int screenW = ScreenUtils.getScreenW(this);
        int screenH = ScreenUtils.getScreenH(this);
        if (screenW > screenH) {
            screenW = screenH;
        }
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {//横屏
            ll_iv_pano_type.setVisibility(View.GONE);
//            ll_title_land.setVisibility(View.VISIBLE);
            head_frame.setBackgroundColor(getResources().getColor(R.color.transparent_));
            horizontalframe.setVisibility(isShowingFrame() ? View.VISIBLE : View.GONE);
            verticalframe.setVisibility(View.GONE);
            rl_video_view.setLayoutParams(layoutParams);
            iv_pano_type.setVisibility(View.VISIBLE);
        } else { //竖屏
//            ll_title_land.setVisibility(View.GONE);
            head_frame.setBackgroundColor(getResources().getColor(R.color.dark_black));
            ll_iv_pano_type.setVisibility(View.GONE);
            verticalframe.setVisibility(View.VISIBLE);
            horizontalframe.setVisibility(View.GONE);
            iv_pano_type.setVisibility(View.GONE);
            verticallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
            layoutParams.height = screenW;
            layoutParams.addRule(RelativeLayout.BELOW, R.id.head_frame);
            rl_video_view.setLayoutParams(layoutParams);

        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
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
        handler.sendEmptyMessage(START_CHANGE_FILE);
        mPbBuffer.setVisibility(View.VISIBLE);
        int progress = seekBar.getProgress();
        Log.e(_TAG_, "9527 onStopTrackingTouch: progress=" + progress);
        if (progress == seekBar.getMax()) {
            progress = progress - 1;
        }
        if (currentPlayer == 0) {
            mPanoCamViewLocal.seek(progress);
        } else {
            mPanoCamViewOnline.seek(progress);
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                slideByUser = false;
            }
        }, 200);
        handler.sendEmptyMessageDelayed(SHOW_PROGRESS, 500);
    }

    String video_path;

    public String getVideoPath(FileDomain file) {
        video_path = file.baseUrl + file.getSmallpath();
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
        mPanoCamViewLocal.stopPlay();
        changePlayState(false);

        Intent share = new Intent(Intent.ACTION_SEND);
        String path = getVideoPath(currentFile);
        String smallPath = getSmallVideoPath(currentFile);

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
        startActivity(share);
    }

    @Override
    public void onLoadComplete(int ret) {
        handler.sendEmptyMessage(STOP_CHANGE_FILE);
        verticalseekbar.setMax(mPanoCamViewLocal.getDuration());
        horizontalseekbar.setMax(mPanoCamViewLocal.getDuration());
        verticalseekbar.setProgress(mCurrent);
        horizontalseekbar.setProgress(mCurrent);
        changePlayState(true);
//        iv_pano_type.setVisibility(View.VISIBLE);
        iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
        setPanoType(mCurrentPanoType);
        mPanoCamViewLocal.onChangeShowType(mCurrentPanoType);
        mPanoCamViewLocal.seek(mCurrent);
        handler.sendEmptyMessage(SHOW_GPS);
    }

    @Override
    public void onBuffering(int percent) {
        Log.e("9999", "percent = " + percent);

    }

    @Override
    public void onSeekComplete() {
        handler.sendEmptyMessage(STOP_CHANGE_FILE);
        mPbBuffer.setVisibility(View.GONE);
    }

    @Override
    public void onError(String errorMessage) {
        Log.e("9527", "onError errorMessage = " + errorMessage);
        if (!errorMessage.equals("-38")) {
//            Log.e("9527", "onError -38 = ");
            mPanoCamViewLocal.setVisibility(View.GONE);
            mPanoCamViewLocal.stopPlay();
            mPanoCamViewLocal.changePlayer();
            mPbBuffer.setVisibility(View.GONE);
            //            currentPlayer = 1;
//
//            mPanoCamViewOnline.setVisibility(View.VISIBLE);
//            mPanoCamViewOnline.reInit();
//            mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
//            showVideoSoft(currentFile);
        }

    }

    @Override
    public void onEnd() {
        Log.e("9527", "onEnd ");
        int duration = mPanoCamViewLocal.getDuration();
        if (horizontalseekbar != null) {
            horizontalseekbar.setProgress(duration);
        }
        if (verticalseekbar != null) {
            verticalseekbar.setProgress(duration);
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
//                mPanoCamViewLocal.setVisibility(View.INVISIBLE);
//                mPanoCamViewLocal.stopPlay();
//                mPanoCamViewLocal.changePlayer();
//
//                currentPlayer = 1;
//
//                mPanoCamViewOnline.setVisibility(View.VISIBLE);
//                mPanoCamViewOnline.reInit();
//                mPanoCamViewOnline.setInfoCallback(mMediaInfoCallback);
//                showVideoSoft(currentFile);
                break;
        }


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
        if (mPanoCamViewLocal != null && mPanoCamViewLocal.isPlaying()) {
            pause();
        }
        if (mPanoCamViewOnline != null) {
            mPanoCamViewOnline.stopPlay();
            mPanoCamViewOnline.setInfoCallback(null);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("9527", "onDestroy");
        unRegisterWifiReceiver();
        handler.removeMessages(NO_DEVICE_CONNECT);

        if (mVerticalMapView != null) {
            mVerticalMapView.onDestroy();
        }
        if (mPanoCamViewLocal != null) {
            mPanoCamViewLocal.stopPlay();
            mPanoCamViewLocal.release();
        }

        if (mPanoCamViewOnline != null) {
            mPanoCamViewOnline.stopPlay();
            mPanoCamViewOnline.setInfoCallback(null);
            mPanoCamViewOnline.release();
        }


    }

    boolean slideByUser = false;
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
                    if (inFilechange) {
                        handler.sendEmptyMessage(STOP_CHANGE_FILE);
                    }
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
                            if (verticalseekbar != null) {
                                verticalseekbar.setProgress(duration);
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

//    int cut = 100;


    @Override
    public void onBackPressed() {
        Log.e("9999", "onBackPressed");
        super.onBackPressed();
    }

    private void showVideoSoft(FileDomain file) {
        if (file == null) return;
        currentFile = file;

        String video_path = getVideoPath(file);
        Log.e("9527", "showVideoSoft: video_path = " + video_path);

        ints = new int[2];
        String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
//        if (product_model.equals("100") && (video_path.contains("http") || (video_path.contains("rtsp")))) {
        ints[0] = 3;
        ints[1] = 1;
//        }
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

        if ((ints[0] & ints[1]) == 0) {
            iv_pano_type.setVisibility(View.GONE);
            mPanoCamViewOnline.onChangeShowType(mCurrentPanoType);
        } else {
//            iv_pano_type.setVisibility(View.VISIBLE);
            iv_pano_type.setBackgroundResource(R.drawable.selector_iv_original);
            setPanoType(VIDEO_SHOW_TYPE_SRC_CIRCLE);
            mPanoCamViewOnline.onChangeShowType(mCurrentPanoType);
        }

        handler.sendEmptyMessage(SHOW_GPS);
        horizontal_adapter.notifyDataSetChanged();
        vertical_adapter.notifyDataSetChanged();
    }

    public void setPanoType(int panoType) {
        iv_original.setSelected(panoType == VIDEO_SHOW_TYPE_SRC_CIRCLE);
        iv_front_back.setSelected(panoType == VIDEO_SHOW_TYPE_2_SCREEN);
        iv_four_direct.setSelected(panoType == VIDEO_SHOW_TYPE_4_SCREEN);
        iv_wide_single.setSelected(panoType == VIDEO_SHOW_TYPE_HEMISPHERE);
        iv_cylinder.setSelected(panoType == VIDEO_SHOW_TYPE_CYLINDER);
        iv_original_vertical.setSelected(panoType == VIDEO_SHOW_TYPE_SRC_CIRCLE);
        iv_front_back_vertical.setSelected(panoType == VIDEO_SHOW_TYPE_2_SCREEN);
        iv_four_direct_vertical.setSelected(panoType == VIDEO_SHOW_TYPE_4_SCREEN);
        iv_wide_single_vertical.setSelected(panoType == VIDEO_SHOW_TYPE_HEMISPHERE);
        iv_cylinder_vertical.setSelected(panoType == VIDEO_SHOW_TYPE_CYLINDER);
        title.setLines(1);
        mCurrentPanoType = panoType;
    }
}
