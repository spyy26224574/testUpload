package com.adai.gkdnavi.gpsvideo;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.ijk.media.widget.media.AndroidMediaController;
import com.ijk.media.widget.media.IjkVideoView;

import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;


public class GpsVideoActivity extends BaseActivity {

    private static final int SHOW_GPS = 1;
    private MapView mapView;
    private Marker marker;
    private IjkVideoView videoView;
    //    private String url;
    private List<GpsInfoBean> _gpsinfos;
    private String mVideoPath;
    private Uri mVideoUri;
    private View mHead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_gps_video);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        mHead = findViewById(R.id.layout_head);
        mapView = (MapView) findViewById(R.id.mapview);
        videoView = (IjkVideoView) findViewById(R.id.videoview);
        videoView.setMediaController(new AndroidMediaController(this, false));
        mapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, null));//设置定位模式以及一些marker的属性
        // 开启定位图层
        mapView.getMap().setMyLocationEnabled(true);
        mapView.getMap().setMaxAndMinZoomLevel(21, 10);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_GPS:
                    int pos = updateMaplocation();
                    if (videoView.isPlaying()) {
                        sendEmptyMessageDelayed(SHOW_GPS, 1000 - (pos % 1000));
                    }
                    break;
            }
        }
    };

    @Override
    protected void init() {
        super.init();
        setTitle(getString(R.string.video_play));
//        url="http://192.168.1.39/versionupdate/20141207_104224.MP4";
//        url= Environment.getExternalStorageDirectory().getAbsolutePath()+"/ligo/20141207_104224.MP4";
        Intent intent = getIntent();
        mVideoPath = intent.getStringExtra("videoPath");
        if (TextUtils.isEmpty(mVideoPath)) {
            Toast.makeText(mContext, getString(R.string.location_error), Toast.LENGTH_SHORT).show();
            return;
        }
        GpsParser parser = new GpsParser();
//        parser.setCallback(new GpsParser.GpsinfoCallback() {
//            @Override
//            public void onGpsinfo(List<GpsInfoBean> gpsinfos) {
//                if(gpsinfos!=null){
//                    _gpsinfos=gpsinfos;
//                }else{
//                    showToast(getString(R.string.get_gps_failed));
//                    mapView.setVisibility(View.GONE);
//                }
//                playVideo();
//                hidepDialog();
//            }
//        });
        showpDialog(getString(R.string.getting_gps_info));
        parser.parseFile(mVideoPath);
    }

    private void playVideo() {
        videoView.setVideoPath(mVideoPath);
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
                switch (i) {
                    case IjkVideoView.MEDIA_INFO_START_PLAYING:
                        handler.sendEmptyMessage(SHOW_GPS);
                        break;
                }
                return false;
            }
        });
        videoView.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int orientation = newConfig.orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            mHead.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.VISIBLE);
            full(false);
        } else {
            mHead.setVisibility(View.GONE);
            mapView.setVisibility(View.GONE);
            full(true);
        }
    }

    private void full(boolean enable) {
        if (enable) {
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(lp);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attr);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
    }

    private int updateMaplocation() {
        if (_gpsinfos == null || _gpsinfos.size() <= 0) return 0;
        int pos = videoView.getCurrentPosition();
        int duration = videoView.getDuration();
        if (duration < 1000) return 0;
        int framesize = _gpsinfos.size() / (duration / 1000);
        if (framesize < 1) {
            framesize = 1;
        }
        int index = (pos / 1000) * framesize;
        if (index >= _gpsinfos.size()) {
            index = _gpsinfos.size() - 1;
        }
//        LatLng latLng=_gpsinfos.get(index).latLng;
//        if(marker==null) {
//            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.icon_mark1);
//            OverlayOptions maker = new MarkerOptions().position(latLng)
//                    .icon(icon);
//            marker = (Marker) mapView.getMap().addOverlay(maker);
//        }else {
//            marker.setPosition(latLng);
//        }
//        MapStatusUpdate msu= MapStatusUpdateFactory.newLatLngZoom(latLng, 16);
//        mapView.getMap().setMapStatus(msu);
//        MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(latLng);
//        mapView.getMap().animateMapStatus(u);
        return pos;
    }

    @Override
    protected void onStop() {
        videoView.stopPlayback();
        videoView.release(true);
        videoView.stopBackgroundPlay();
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
