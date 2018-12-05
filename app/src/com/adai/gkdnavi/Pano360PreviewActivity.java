package com.adai.gkdnavi;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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

import com.Hjni.HbxFishEye;
import com.adai.camera.CameraConstant;
import com.adai.camera.CameraFactory;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.gpsvideo.GpsInfoBean;
import com.adai.gkdnavi.gpsvideo.GpsParser;
import com.adai.gkdnavi.utils.GpsUtil;
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
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.ligo.medialib.PanoCamViewLocal;
import com.ligo.medialib.opengl.VideoRenderYuv;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.widget.piechart.ScreenUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author huangxy
 */
public class Pano360PreviewActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, PanoCamViewLocal.OnChangeListener {
    public static final String KEY_FILES = "KEY_FILES";
    public static final String KEY_POSTION = "KEY_POSTION";
    public static final String KEY_TYPE = "KEY_TYPE";
    private static final int SHOW_PROGRESS = 1;
    private static final int SHOW_GPS = 2;
    private static final int NO_DEVICE_CONNECT = 3;
    //    private IjkVideoView videoview;
    private ImageButton verticalmenu, mIbVerticalGps, mIbHorizontalGps;
    private ImageView verticalprevious;
    private ImageView mIvIllegalReport;
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
    private ImageView back, iv_fullscreen;
    private TextView title;
    private RelativeLayout activityvideppreview;
    private List<FileDomain> files = null;
    private FileDomain currentFile = null;
    private ListAdapter horizontal_adapter, vertical_adapter;
    private List<GpsInfoBean> _gpsinfos;
    private LinearLayout mHorizontalMapFrame;
    private RelativeLayout rl_video_view;
    //    private ImageView mIvFullscreen;
    private PanoCamViewLocal mPlayerView;
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


    ImageView iv_original, iv_original_land;
    ImageView iv_front_back, iv_front_back_land;
    ImageView iv_four_direct, iv_four_direct_land;
    ImageView iv_wide_single, iv_wide_single_land;
    ImageView iv_cylinder, iv_cylinder_land;
    //    LinearLayout ll_video_frame;
    LinearLayout ll_pano_type_land;
    ImageView iv_pano_type_land;
    int modetype = 1;
    boolean outOfChina = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_pano360_preview);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        mPlayerView = (PanoCamViewLocal) findViewById(R.id.pv_video);
        mPlayerView.setOnChangeListener(this);

        iv_original = (ImageView) findViewById(R.id.iv_original);
        iv_front_back = (ImageView) findViewById(R.id.iv_front_back);
        iv_four_direct = (ImageView) findViewById(R.id.iv_four_direct);
        iv_wide_single = (ImageView) findViewById(R.id.iv_wide_single);
        iv_cylinder = (ImageView) findViewById(R.id.iv_cylinder);
        iv_original_land = (ImageView) findViewById(R.id.iv_original_land);
        iv_front_back_land = (ImageView) findViewById(R.id.iv_front_back_land);
        iv_four_direct_land = (ImageView) findViewById(R.id.iv_four_direct_land);
        iv_wide_single_land = (ImageView) findViewById(R.id.iv_wide_single_land);
        iv_cylinder_land = (ImageView) findViewById(R.id.iv_cylinder_land);
        iv_original.setOnClickListener(this);
        iv_front_back.setOnClickListener(this);
        iv_four_direct.setOnClickListener(this);
        iv_wide_single.setOnClickListener(this);
        iv_cylinder.setOnClickListener(this);
        iv_original_land.setOnClickListener(this);
        iv_front_back_land.setOnClickListener(this);
        iv_four_direct_land.setOnClickListener(this);
        iv_wide_single_land.setOnClickListener(this);
        iv_cylinder_land.setOnClickListener(this);

        ll_pano_type_land = (LinearLayout) findViewById(R.id.ll_pano_type_land);
        iv_pano_type_land = (ImageView) findViewById(R.id.iv_pano_type_land);
        iv_pano_type_land.setOnClickListener(this);

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
        this.verticalmenu = (ImageButton) findViewById(R.id.vertical_menu);
//        this.videoview = (IjkVideoView) findViewById(R.id.video_view);
//        ll_video_frame = (LinearLayout) findViewById(R.id.ll_video_frame);

        iv_fullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        mVideoFrame = (ImageView) findViewById(R.id.iv_view_frame);
        rl_video_view = (RelativeLayout) findViewById(R.id.rl_video_view);
//        mIvFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
//        mIvFullscreen.setOnClickListener(this);
        mIvIllegalReport = (ImageView) findViewById(R.id.iv_illegal_report);
        this.head_frame = findViewById(R.id.head_frame);
        mIbVerticalGps = (ImageButton) findViewById(R.id.ib_vertical_gps);
        mIbVerticalGps.setOnClickListener(this);
        mIbHorizontalGps = (ImageButton) findViewById(R.id.horizontal_ib_gps);
        mIbHorizontalGps.setOnClickListener(this);
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

        horizontalseekbar.setOnSeekBarChangeListener(this);
        verticalseekbar.setOnSeekBarChangeListener(this);
        mIvIllegalReport.setOnClickListener(this);
        findViewById(R.id.video_cut).setOnClickListener(this);
        toggleFrame(false);


    }

    @Override
    protected void init() {
        super.init();
        Intent data = getIntent();
        if (data.hasExtra(KEY_FILES)) {
            files = (List<FileDomain>) data.getSerializableExtra(KEY_FILES);
        }
        if (files == null || files.size() <= 0) {
            return;
        }
        if (data.hasExtra(KEY_POSTION)) {
            int postion = data.getIntExtra(KEY_POSTION, -1);
            if (postion < 0) {
                return;
            }
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
                mPlayerView.stopPlay();
                parserVideo(files.get(position));
            }
        });
        verticallist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                mPlayerView.stopPlay();
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
                        outOfChina = GpsUtil.outOfChina(_gpsinfos.get(0).latitude, _gpsinfos.get(0).longitude);

                        double[] doubles = GpsUtil.gps84_To_bd09(_gpsinfos.get(0).latitude, _gpsinfos.get(0).longitude, outOfChina);
                        LatLng latLng = new LatLng(doubles[0], doubles[1]);
                        OverlayOptions maker = new MarkerOptions().position(latLng)
                                .icon(mIcon);
                        mVerticalMarker = (Marker) mVerticalMapView.getMap().addOverlay(maker);
                        mHorizontalMarker = (Marker) mHorizontalMapView.getMap().addOverlay(maker);
                        List<LatLng> tempGps = new ArrayList<>();
                        for (GpsInfoBean gpsInfoBean : _gpsinfos) {
                            double[] bdGps = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude, outOfChina);
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
            parser.parseFile(video_path);
        } else {
            showVideo(fileDomain);
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_PROGRESS:
//                    int pos = setProgress();
//                    if (mPlayerView.isPlaying()) {
//                        sendEmptyMessageDelayed(SHOW_PROGRESS, 1000 - (pos % 1000));
//                    }
                    break;
                case SHOW_GPS:
                    int pos1 = updateMaplocation();
                    if (mPlayerView.isPlaying()) {
                        sendEmptyMessageDelayed(SHOW_GPS, 1000 - (pos1 % 1000));
                    }
                    break;
                case NO_DEVICE_CONNECT:
                    hidepDialog();
                    if (isFinishing()) {
                        return;
                    }
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
                default:
                    break;
            }
        }
    };

    private int updateMaplocation() {
        if (_gpsinfos == null || _gpsinfos.size() <= 0) return 0;
        int pos = mPlayerView.getCurrent();
        int duration = mPlayerView.getDuration();
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
        double[] doubles = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude, outOfChina);
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

    private void setProgress(int current, int duration) {
        if (horizontalseekbar != null) {
            horizontalseekbar.setMax(duration);
            horizontalseekbar.setProgress(current);
        }
        if (verticalseekbar != null) {
            verticalseekbar.setMax(duration);
            verticalseekbar.setProgress(current);
        }

        showTime(current, duration);

    }

    private void showVideo(FileDomain file) {
        if (file == null) {
            return;
        }
        currentFile = file;
        mIvIllegalReport.setVisibility(View.GONE);
        String name;
        boolean aBoolean = SpUtils.getBoolean(this, EditVideoActivity.IS_SUPPORT_REPORT_KEY, false);
        // FIXME: 2017/5/26 测试用，暂时显示违章举报
        mIvIllegalReport.setVisibility(aBoolean && isShowGps ? View.VISIBLE : View.GONE);
        String video_path = getVideoPath(file);
        name = file.getSmallname();
        if (TextUtils.isEmpty(name)) {
            name = file.getName();
        }
        Log.e("ryujin", "showVideo: " + video_path);
        title.setText(name);

        int[] ints = HbxFishEye.GetId(video_path);
        String product_model = SpUtils.getString(VLCApplication.getAppContext(), CameraConstant.CAMERA_PRODUCT_MODEL, "");
        if (product_model.equals("100") && (video_path.contains("http") || (video_path.contains("rtsp")))) {
            ints[0] = 1;
            ints[1] = 1;
        }
        mPlayerView.startPlay(video_path);
//        mPlayerView.startPlay(1);

//        if ((ints[0] & ints[1]) == 0) {
//            mPlayerView.onChangeShowType(VideoRender.VIDEO_SHOW_TYPE_SRC_PLANE);
//        } else {
//            mPlayerView.onChangeShowType(VideoRender.VIDEO_SHOW_TYPE_SRC_CIRCLE);
//        }

        mPbBuffer.setVisibility(View.GONE);
        changePlayState(true);
        horizontal_adapter.notifyDataSetChanged();
        vertical_adapter.notifyDataSetChanged();
    }

    private void showTime(long play_time, long total_time) {
        if (total_time <= 0) {
            return;
        }
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
        if (!mPlayerView.isPlaying()) {
            mPlayerView.resume();
        }
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
        play();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void pause() {
        Log.e("9999", "pause");
        mPlayerView.pause();
        changePlayState(false);
    }

    private void resume() {
        Log.e("9999", "resume");
        mPlayerView.resume();
        changePlayState(true);
    }


    private void playPrevious() {
        if (files == null || files.size() <= 1) {
            return;
        }
        int postion = files.indexOf(currentFile) - 1;
        if (postion < 0) {
            postion = files.size() - 1;
        } else if (postion >= files.size()) {
            postion = 0;
        }
        mPlayerView.stopPlay();
        parserVideo(files.get(postion));
    }

    private void playNext() {
        if (files == null || files.size() <= 1) {
            return;
        }
        int postion = files.indexOf(currentFile) + 1;
        if (postion < 0) {
            postion = files.size() - 1;
        } else if (postion >= files.size()) {
            postion = 0;
        }
        mPlayerView.stopPlay();
        parserVideo(files.get(postion));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_original:
            case R.id.iv_original_land:
                modetype = 1;
                iv_original.setSelected(true);
                iv_front_back.setSelected(false);
                iv_four_direct.setSelected(false);
                iv_wide_single.setSelected(false);
                iv_cylinder_land.setSelected(false);
                iv_original_land.setSelected(true);
                iv_front_back_land.setSelected(false);
                iv_four_direct_land.setSelected(false);
                iv_wide_single_land.setSelected(false);
                iv_cylinder.setSelected(false);
                mPlayerView.onChangeShowType(VideoRenderYuv.TYPE_CIRCLE);
                break;
            case R.id.iv_front_back:
            case R.id.iv_front_back_land:
                modetype = 2;
                iv_original.setSelected(false);
                iv_front_back.setSelected(true);
                iv_four_direct.setSelected(false);
                iv_wide_single.setSelected(false);
                iv_cylinder.setSelected(false);
                iv_original_land.setSelected(false);
                iv_front_back_land.setSelected(true);
                iv_four_direct_land.setSelected(false);
                iv_wide_single_land.setSelected(false);
                iv_cylinder_land.setSelected(false);
                mPlayerView.onChangeShowType(VideoRenderYuv.TYPE_2_SCREEN);
                break;
            case R.id.iv_four_direct:
            case R.id.iv_four_direct_land:
                modetype = 3;
                iv_original.setSelected(false);
                iv_front_back.setSelected(false);
                iv_four_direct.setSelected(true);
                iv_wide_single.setSelected(false);
                iv_cylinder.setSelected(false);
                iv_original_land.setSelected(false);
                iv_front_back_land.setSelected(false);
                iv_four_direct_land.setSelected(true);
                iv_wide_single_land.setSelected(false);
                iv_cylinder_land.setSelected(false);
                mPlayerView.onChangeShowType(VideoRenderYuv.TYPE_4_SCREEN);
                break;
            case R.id.iv_wide_single:
            case R.id.iv_wide_single_land:
                modetype = 4;
                iv_original.setSelected(false);
                iv_front_back.setSelected(false);
                iv_four_direct.setSelected(false);
                iv_wide_single.setSelected(true);
                iv_cylinder.setSelected(false);
                iv_original_land.setSelected(false);
                iv_front_back_land.setSelected(false);
                iv_four_direct_land.setSelected(false);
                iv_wide_single_land.setSelected(true);
                iv_cylinder_land.setSelected(false);
                mPlayerView.onChangeShowType(VideoRenderYuv.TYPE_SRC);
                break;
            case R.id.iv_cylinder:
            case R.id.iv_cylinder_land:
                modetype = 5;
                iv_original.setSelected(false);
                iv_front_back.setSelected(false);
                iv_four_direct.setSelected(false);
                iv_wide_single.setSelected(false);
                iv_cylinder.setSelected(true);
                iv_original_land.setSelected(false);
                iv_front_back_land.setSelected(false);
                iv_four_direct_land.setSelected(false);
                iv_wide_single_land.setSelected(false);
                iv_cylinder_land.setSelected(true);
                mPlayerView.onChangeShowType(VideoRenderYuv.TYPE_CYLINDER);
                break;
            case R.id.iv_pano_type_land:
                if (ll_pano_type_land.getVisibility() == View.VISIBLE) {
                    iv_pano_type_land.setBackgroundResource(R.drawable.pano_up_selector);
                    ll_pano_type_land.setVisibility(View.GONE);
                } else {
                    iv_pano_type_land.setBackgroundResource(R.drawable.pano_down_selector);
                    ll_pano_type_land.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.iv_fullscreen:
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                break;
            case R.id.vertical_play:
            case R.id.horizontal_play:
                if (mPlayerView.isPlaying()) {
                    pause();
                } else if (mPlayerView.isPause()) {
                    resume();
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
                    horizontallist.setVisibility(View.VISIBLE);
                    mHorizontalMapFrame.setVisibility(View.GONE);
                } else {
                    horizontallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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
//            case R.id.activity_video_preview:
//                toggleFrame();
//                break;
            case R.id.video_cut:
                gotoEdit(EditVideoActivity.TYPE_SHARE);
                break;
            case R.id.ib_vertical_gps:
                mVerticalMapView.setVisibility(mVerticalMapView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.horizontal_ib_gps:
                mHorizontalMapFrame.setVisibility(mHorizontalMapFrame.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                break;
            case R.id.iv_illegal_report:
                gotoEdit(EditVideoActivity.TYPE_ILLEGAL_REPORT);
                break;
            default:
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
        Log.e("9999", "toggleFrame  change = " + change);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        int screenW = ScreenUtils.getScreenW(this);
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            iv_fullscreen.setBackgroundResource(R.drawable.selector_exit_fullscreen);
            if (change) {
                boolean showingFrame = isShowingFrame();
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
//                layoutParams.addRule(RelativeLayout.BELOW, R.id.head_frame);
                rl_video_view.setLayoutParams(layoutParams);
            }
        } else {
            iv_fullscreen.setBackgroundResource(R.drawable.selector_fullscreen);
            if (change) {
//                head_frame.setVisibility(isShowingFrame() ? View.GONE : View.VISIBLE);
            } else {
                //屏幕旋转
//                mIbHorizontalGps.setVisibility(View.GONE);
                verticalframe.setVisibility(View.VISIBLE);
                horizontalframe.setVisibility(View.GONE);
//                mVerticalMapView.setVisibility(mHorizontalMapFrame.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                verticallist.setVisibility(horizontallist.getVisibility() == View.VISIBLE ? View.VISIBLE : View.GONE);
                layoutParams.height = 9 * screenW / 16;
//                mVideoFrame.setLayoutParams(layoutParams);
//                videoview.setLayoutParams(layoutParams);
                layoutParams.addRule(RelativeLayout.BELOW, R.id.head_frame);
                rl_video_view.setLayoutParams(layoutParams);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        Log.e(_TAG_, " 9527 onProgressChanged:progress = " + progress + " fromUser = " + fromUser);
        if (!fromUser) {
            return;
        }
//        int postion = progress * videoview.getDuration() / 100;
//        videoview.seekTo(postion);
//        showTime(videoview.getCurrentPosition(), videoview.getDuration());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.e(_TAG_, "9527 onStartTrackingTouch: ");
        slideByUser = true;
        handler.removeMessages(SHOW_PROGRESS);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.e(_TAG_, "9527 onStopTrackingTouch: ");
        int progress = seekBar.getProgress();
        Log.e(_TAG_, "9527 onStopTrackingTouch: progress=" + progress);
        if (progress == seekBar.getMax()) {
            progress = progress - 1;
        }
        mPlayerView.seek(progress);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                slideByUser = false;
            }
        }, 200);
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
    public void onLoadComplete(int ret) {//加载完成 -1不支持展开

    }

    @Override
    public void onBuffering(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(String errorMessage) {

    }

    @Override
    public void onEnd() {

    }

    @Override
    public void onInfo(int what) {//701:开始缓存 702：缓存结束 开始播放


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
        if (mPlayerView.isPlaying()) {
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("9527", "release Decoder");
        handler.removeMessages(NO_DEVICE_CONNECT);
        mPlayerView.stopPlay();
        mPlayerView.release();
        if (mHorizontalMapView != null) {
            mHorizontalMapView.onDestroy();
        }
        if (mVerticalMapView != null) {
            mVerticalMapView.onDestroy();
        }

    }

    boolean slideByUser = false;

//    private PanoCamViewLocal.MediaInfoCallback myinfoCallback = new PanoCamViewLocal.MediaInfoCallback() {
//        @Override
//        public void onInfo(PanoCamViewLocal.States state, String info) {
//            Log.e("9999", "onInfo state = " + state);
//            switch (state) {
//                case STATUS_STOP:
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mPlayerView.stopPlay();
//                        }
//                    }).start();
//                    break;
//                case STATUS_PLAY:
//                case STATUS_PAUSE:
//                case STATUS_ERROR:
//                    break;
//                default:
//                    break;
//            }
//        }
//
//
//        @Override
//        public void onUpdateFrame(final byte[] data, final int width, final int height, final int type) {
////            Log.e("9999", "onUpdateFrame  type = " + type);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    int current = mPlayerView.getCurrent();
//                    int duration = mPlayerView.getDuration();
//
//                    Log.w("9527", "duration = " + duration + " current = " + current + ", type = " + type);
//
//                    if (!slideByUser) {
//                        if (current == 0 && mPlayerView.isPlaying()) {
//                            showTime(0, duration);
//                            verticalseekbar.setMax(duration);
//                            horizontalseekbar.setMax(duration);
//                        }
//                        if (current >= 0 && duration - current > 0) {
//                            setProgress(current, duration);
//                        } else if (current == -1) {
//                            setProgress(verticalseekbar.getMax(), verticalseekbar.getMax());
//                            changePlayState(false);
//                        }
//                    }
//
//                }
//            });
//
//        }
//
//        @Override
//        public void onScreenShot(boolean sucess, String url) {
//        }
//
//    };


}
