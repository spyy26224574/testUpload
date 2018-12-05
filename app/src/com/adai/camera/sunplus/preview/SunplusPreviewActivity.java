package com.adai.camera.sunplus.preview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.camera.FileManagerConstant;
import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.CameraAction;
import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.camera.sunplus.SDKAPI.CameraState;
import com.adai.camera.sunplus.SDKAPI.PreviewStream;
import com.adai.camera.sunplus.SDKAPI.SDKEvent;
import com.adai.camera.sunplus.adapter.SunplusResolutionAdapter;
import com.adai.camera.sunplus.bean.Tristate;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.filemanager.SunplusFileActivity;
import com.adai.camera.sunplus.setting.SunplusSettingActivity;
import com.adai.camera.sunplus.tool.ResolutionConvert;
import com.adai.camera.sunplus.tool.ScaleTool;
import com.adai.camera.sunplus.widget.PreviewH264;
import com.adai.camera.sunplus.widget.PreviewMjpg;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.icatch.wificam.customer.ICatchWificamConfig;
import com.icatch.wificam.customer.ICatchWificamPreview;
import com.icatch.wificam.customer.type.ICatchCodec;
import com.icatch.wificam.customer.type.ICatchCustomerStreamParam;
import com.icatch.wificam.customer.type.ICatchMJPGStreamParam;
import com.icatch.wificam.customer.type.ICatchMode;
import com.icatch.wificam.customer.type.ICatchPreviewMode;
import com.widget.piechart.ScreenUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author huangxy
 */
public class SunplusPreviewActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SunplusPreviewActivity.class.getSimpleName();
    private RelativeLayout mRlTitle;
    private TextView mBack;
    private TextView mHeadTitle, mTvResolution, mTvNoCardNotice, mTvTakePhoto, mTvRecordTime;
    private ImageView mIvSetting, mIvTakePhoto;
    private RelativeLayout mVideoFrame;
    private PreviewMjpg mPreviewMjpg;
    private PreviewH264 mPreviewH264;
    private ImageView mIvRecord;
    private ImageView mIvFullscreen;
    private RelativeLayout mRlBottom;
    private RecyclerView mRvResolution;
    private ImageView mIvPip;
    private LinearLayout mLlVideo, mLlResolution, mLlCurResolution;
    private LinearLayout mLlPhoto;
    private LinearLayout mLlPicture;
    private ImageView mIvHorizontalPip;
    private RadioGroup mRgMode;
    private RadioButton mRbVideoMode;
    private RadioButton mRbPhotoMode;
    private RelativeLayout.LayoutParams landscapeParams;
    private RelativeLayout.LayoutParams portraitParams;
    private RelativeLayout.LayoutParams mRecordWaitPortraitParams, mRecordWaitLandscapeParams;
    private RelativeLayout.LayoutParams mRecordPortraitParams, mRecordLandscapeParams;
    private RelativeLayout.LayoutParams mLlPicturePortraitParams, mLLPictureLandscapeParams;
    private boolean isPortrait = true;
    private ISunplusCamera mSunplusCamera;
    private CameraProperties mCameraProperties;
    private CameraAction mCameraAction;
    private CameraState mCameraState;
    private PreviewStream mPreviewStream;
    private ICatchWificamPreview mPreviewStreamClient;
    private boolean sdCardFullWarning;
    private int curMode;
    private int cacheTime;
    private boolean supportStreaming;
    private int currentCodec = ICatchCodec.ICH_CODEC_RGBA_8888;
    private SDKEvent mSDKEvent;
    private ExecutorService executor;
    private static final int MODE_CHANGE_SUCCESS = 1;
    private static final int MODE_CHANGE_FAILED = 2;
    private static final int ON_START = 3;
    private static final int APP_STATE_STILL_PREVIEW = 0x0001;//拍照模式
    private static final int APP_STATE_STILL_CAPTURE = 0x0002;
    private static final int APP_STATE_VIDEO_PREVIEW = 0x0003;//录制模式
    private static final int APP_STATE_VIDEO_CAPTURE = 0x0004;//正在录制
    private static final int APP_STATE_TIMELAPSE_VIDEO_CAPTURE = 0x0005;
    private static final int APP_STATE_TIMELAPSE_STILL_CAPTURE = 0x0006;
    private static final int APP_STATE_TIMELAPSE_PREVIEW_VIDEO = 0x0007;
    private static final int APP_STATE_TIMELAPSE_PREVIEW_STILL = 0x0008;
    private static final int APP_STATE_PLAYBACK = 0x0009;

    RelativeLayout rl_novatek_camera;
    RelativeLayout ll_record_wait;
    ImageView iv_record_wait;

    RelativeLayout horizontal_frame;
    ImageView iv_fullscreen_land;


    @SuppressLint("HandlerLeak")
    private Handler mCameraHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MODE_CHANGE_SUCCESS:
                    startPreview();
                    mSunplusCamera.resetVideoSize();
                    changeModeView();
                    break;
                case MODE_CHANGE_FAILED:
                    hidepDialog();
                    showToast(getString(R.string.set_failure));
                    hidepDialog();
                    break;
                case ON_START:
                    startPreview();
                    changeModeView();

                    break;
                case GlobalInfo.EVENT_BATTERY_ELETRIC_CHANGED:
                    LogUtils.e("handleMessage: EVENT_BATTERY_ELETRIC_CHANGED");
                    break;
                //拍照成功回调
                case GlobalInfo.EVENT_CAPTURE_COMPLETED:
                    LogUtils.e("handleMessage: EVENT_CAPTURE_COMPLETED");
                    executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            if (curMode == APP_STATE_STILL_CAPTURE) {

                                final Tristate ret = changeCameraMode(ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                                if (ret == Tristate.FALSE) {
                                    return;
                                }
                                curMode = APP_STATE_STILL_PREVIEW;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (ret == Tristate.NORMAL) {
                                            startPreview();
                                        }
                                        showToast(R.string.takephoto_sucess);
                                        hidepDialog();
                                    }
                                });
                                return;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    hidepDialog();
                                }
                            });
                        }
                    });

                    break;
                case GlobalInfo.EVENT_CAPTURE_START:
                    LogUtils.e("handleMessage: EVENT_CAPTURE_START");
                    if (curMode != APP_STATE_TIMELAPSE_STILL_CAPTURE) {
                        return;
                    }
                    break;
                case GlobalInfo.EVENT_CONNECTION_FAILURE:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_CONNECTION_FAILURE");
                    stopPreview();
                    finish();
                    break;
                case GlobalInfo.EVENT_FILE_ADDED:
                    LogUtils.e("handleMessage: EVENT_FILE_ADDED");
                    break;
                case GlobalInfo.EVENT_FILE_DOWNLOAD:
                    LogUtils.e("handleMessage: EVENT_FILE_DOWNLOAD");
                    break;
                case GlobalInfo.EVENT_FW_UPDATE_COMPLETED:
                    LogUtils.e("handleMessage: EVENT_FW_UPDATE_COMPLETED");
                    break;
                case GlobalInfo.EVENT_FW_UPDATE_POWEROFF:
                    LogUtils.e("handleMessage: EVENT_FW_UPDATE_POWEROFF");
                    break;
                case GlobalInfo.EVENT_NO_SD_CARD:
                    LogUtils.e("handleMessage: EVENT_NO_SD_CARD");
                    break;
                case GlobalInfo.EVENT_SD_CARD_FULL:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_SD_CARD_FULL");
                    if (curMode == APP_STATE_VIDEO_CAPTURE || curMode == APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                        showToast(R.string.wifi_camera_storage);
                    }
                    break;
                case GlobalInfo.EVENT_SERVER_STREAM_ERROR:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_SERVER_STREAM_ERROR");
                    break;
                case GlobalInfo.EVENT_TIME_LAPSE_STOP:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_TIME_LAPSE_STOP");
                    break;
                case GlobalInfo.EVENT_VIDEO_OFF:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_VIDEO_OFF");
                    if (curMode == APP_STATE_VIDEO_CAPTURE || curMode == APP_STATE_TIMELAPSE_VIDEO_CAPTURE) {
                        mCameraAction.stopVideoCapture();
                        curMode = APP_STATE_VIDEO_PREVIEW;
                        showRecordState(false);
                    }
                    break;
                case GlobalInfo.EVENT_VIDEO_ON:
                    hidepDialog();
                    LogUtils.e("handleMessage: EVENT_VIDEO_ON");
                    if (curMode == APP_STATE_VIDEO_PREVIEW) {
                        curMode = APP_STATE_VIDEO_CAPTURE;
                        showRecordState(true);
                    } else if (curMode == APP_STATE_TIMELAPSE_PREVIEW_VIDEO) {
                        curMode = APP_STATE_TIMELAPSE_VIDEO_CAPTURE;

                    }
                    break;
                case GlobalInfo.EVENT_VIDEO_RECORDING_TIME:
                    LogUtils.e("handleMessage: EVENT_VIDEO_RECORDING_TIME");
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunplus_preview);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        CameraFactory.getInstance().getSunplusCamera().initCamera();
        executor = Executors.newSingleThreadExecutor();
        mSunplusCamera = CameraFactory.getInstance().getSunplusCamera();
        mCameraProperties = CameraProperties.getInstance();
        mCameraAction = CameraAction.getInstance();
        mCameraState = CameraState.getInstance();
        mPreviewStream = PreviewStream.getInstance();
        mPreviewStreamClient = mSunplusCamera.getPreviewStreamClient();

        sdCardFullWarning = false;

        if (!mCameraProperties.cameraModeSupport(ICatchMode.ICH_MODE_VIDEO)) {
            curMode = APP_STATE_STILL_PREVIEW;
        } else {
            curMode = APP_STATE_VIDEO_PREVIEW;
        }

        cacheTime = mCameraProperties.getPreviewCacheTime();
        Log.e("1111", "-----------cacheTime =" + cacheTime);
        if (cacheTime < 200) {
            cacheTime = 200;
        }
        ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(SpUtils.getString(this, "SSID", ""));
        rl_novatek_camera = (RelativeLayout) findViewById(R.id.rl_novatek_camera);
        ll_record_wait = (RelativeLayout) findViewById(R.id.ll_record_wait);
        ll_record_wait.setOnClickListener(this);
        iv_record_wait = (ImageView) findViewById(R.id.iv_record_wait);

        horizontal_frame = (RelativeLayout) findViewById(R.id.horizontal_frame);
        iv_fullscreen_land = (ImageView) findViewById(R.id.iv_fullscreen_land);
        iv_fullscreen_land.setOnClickListener(this);

        mRlTitle = (RelativeLayout) findViewById(R.id.rl_title);
        mBack = (TextView) findViewById(R.id.back);
        mHeadTitle = (TextView) findViewById(R.id.head_title);
        mIvSetting = (ImageView) findViewById(R.id.iv_setting);
        mVideoFrame = (RelativeLayout) findViewById(R.id.video_frame);
        mPreviewMjpg = (PreviewMjpg) findViewById(R.id.preview_mjpg);
        mPreviewMjpg.setScaleType(ScaleTool.ScaleType.FIT_XY);
        mPreviewH264 = (PreviewH264) findViewById(R.id.preview_h264);
        mPreviewH264.setScaleType(ScaleTool.ScaleType.FIT_XY);
        mIvRecord = (ImageView) findViewById(R.id.iv_record);
        mIvFullscreen = (ImageView) findViewById(R.id.iv_fullscreen);
        mRlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
        mIvPip = (ImageView) findViewById(R.id.iv_pip);
        mLlVideo = (LinearLayout) findViewById(R.id.ll_video);
        mLlPhoto = (LinearLayout) findViewById(R.id.ll_photo);
        mLlPicture = (LinearLayout) findViewById(R.id.ll_picture);
        mIvHorizontalPip = (ImageView) findViewById(R.id.iv_horizontal_pip);
        mRgMode = (RadioGroup) findViewById(R.id.rg_mode);
        mRbVideoMode = (RadioButton) findViewById(R.id.rb_video_mode);
        mRbPhotoMode = (RadioButton) findViewById(R.id.rb_photo_mode);
        mLlResolution = (LinearLayout) findViewById(R.id.ll_resolution);
        mLlCurResolution = (LinearLayout) findViewById(R.id.ll_cur_resolution);
        mTvResolution = (TextView) findViewById(R.id.tv_cur_resolution);
        mRvResolution = (RecyclerView) findViewById(R.id.rv_resolution);
        mTvNoCardNotice = (TextView) findViewById(R.id.tv_no_card_notice);
        mTvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        mIvTakePhoto = (ImageView) findViewById(R.id.iv_take_photo);
        mTvRecordTime = (TextView) findViewById(R.id.tv_record_time);
        AnimationDrawable drawable = (AnimationDrawable) mIvRecord.getDrawable();
        drawable.start();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        initParams(dm);
        VoiceManager.setDefaultNetwork(mContext, true);
        autoHideView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        showpDialog();
        sdCardFullWarning = false;
        mSDKEvent.addEventListener();
        executor.submit(onStartThread);
    }

    private boolean hadStop;

    @Override
    protected void onStop() {
        super.onStop();
        if (!hadStop) {
            stopPreview();
            stopMediaStream();
        }
        hadStop = false;
        mSDKEvent.delEventListener();
    }

    private void autoHideView() {
        mCameraHandler.removeCallbacks(autoHideViewTask);
        mCameraHandler.postDelayed(autoHideViewTask, 3000);
    }

    private java.lang.Runnable autoHideViewTask = new Runnable() {
        @Override
        public void run() {
            mIvFullscreen.setVisibility(View.GONE);
            mLlResolution.setVisibility(View.GONE);
        }
    };

    private void initEvent() {
        mSDKEvent = new SDKEvent(mCameraHandler);
        mLlPicture.setOnClickListener(this);
        mIvSetting.setOnClickListener(this);
        mLlPhoto.setOnClickListener(this);
        mLlVideo.setOnClickListener(this);
        mRbPhotoMode.setOnClickListener(this);
        mRbVideoMode.setOnClickListener(this);
        mIvPip.setOnClickListener(this);
        mVideoFrame.setOnClickListener(this);
        mIvHorizontalPip.setOnClickListener(this);
        mIvFullscreen.setOnClickListener(this);
        mLlResolution.setOnClickListener(this);
        mRvResolution.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (RecyclerView.SCROLL_STATE_IDLE == newState) {
                    autoHideView();
                } else if (RecyclerView.SCROLL_STATE_DRAGGING == newState) {
                    mCameraHandler.removeCallbacks(autoHideViewTask);
                }
            }
        });
    }

    private void initParams(DisplayMetrics dm) {
        //宽度height = dm.heightPixels ;//高度
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        if (width > height) {
            width = width ^ height;
            height = width ^ height;
            width = width ^ height;
        }
//        landscapeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
//        landscapeParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        landscapeParams = new RelativeLayout.LayoutParams(height - ScreenUtils.dp2px(this, 110), width);
        landscapeParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        portraitParams = new RelativeLayout.LayoutParams(width, width * 9 / 16);
        portraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        portraitParams.addRule(RelativeLayout.BELOW, R.id.rl_title);

        mRecordWaitPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitPortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mRecordWaitPortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mRecordWaitPortraitParams.bottomMargin = ScreenUtils.dp2px(this, 10);

        mRecordWaitLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 30), ScreenUtils.dp2px(this, 30));
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mRecordWaitLandscapeParams.addRule(RelativeLayout.ABOVE, R.id.ll_picture);
        mRecordWaitLandscapeParams.bottomMargin = ScreenUtils.dp2px(this, 20);
        mRecordWaitLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 40);

        mLlPicturePortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLlPicturePortraitParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        mLlPicturePortraitParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mLlPicturePortraitParams.bottomMargin = ScreenUtils.dp2px(this, 50);
        mLLPictureLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 90), ScreenUtils.dp2px(this, 90));
        mLLPictureLandscapeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        mLLPictureLandscapeParams.addRule(RelativeLayout.CENTER_VERTICAL);
        mLLPictureLandscapeParams.rightMargin = ScreenUtils.dp2px(this, 10);

        mRecordPortraitParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
        mRecordPortraitParams.topMargin = ScreenUtils.dp2px(this, 10);
        mRecordPortraitParams.leftMargin = ScreenUtils.dp2px(this, 10);

        mRecordLandscapeParams = new RelativeLayout.LayoutParams(ScreenUtils.dp2px(this, 24), ScreenUtils.dp2px(this, 24));
        mRecordLandscapeParams.topMargin = ScreenUtils.dp2px(this, 40);
        mRecordLandscapeParams.leftMargin = ScreenUtils.dp2px(this, 20);
    }

    private void startPreview() {
        hidepDialog();
//        int type = SunplusResolutionAdapter.VIDEO_RESOLUTION;
//        if (curMode != APP_STATE_STILL_PREVIEW) {
//            //视频分辨率
//            mTvResolution.setText(mSunplusCamera.getVideoSize().getCurrentValue());
//        } else {
//            //图片分辨率
//            type = SunplusResolutionAdapter.PHOTO_RESOLUTION;
//            mTvResolution.setText(mSunplusCamera.getImageSize().getCurrentUiStringInSetting());
//        }
//        SunplusResolutionAdapter sunplusResolutionAdapter = new SunplusResolutionAdapter(type);
//        mRvResolution.setLayoutManager(new LinearLayoutManager(this));
//        mRvResolution.setAdapter(sunplusResolutionAdapter);
//        sunplusResolutionAdapter.setOnItemClickListener(new SunplusResolutionAdapter.ItemClickListener() {
//            @Override
//            public void onItemClick(int type, int position, String value) {
//                if (mTvResolution.getText().toString().equals(value)) {
//                    return;
//                }
//                if (type == SunplusResolutionAdapter.VIDEO_RESOLUTION) {
//                    stopPreview();
//                }
//                showpDialog();
//                SwitchResolution switchResolution = new SwitchResolution(type, position);
//                executor.submit(switchResolution);
//            }
//        });
        mTvNoCardNotice.setVisibility(View.GONE);
        if (!mCameraProperties.isSDCardExist()) {
            //没有插入sd卡
//                    showToast(R.string.wifi_sdcard);
            mTvNoCardNotice.setVisibility(View.VISIBLE);
        } else {
            if (curMode == APP_STATE_STILL_PREVIEW) {
                if (mCameraProperties.getRemainImageNum() < 1) {
                    //sd卡满了
                    showToast(R.string.wifi_camera_storage);
                }
            } else {
                if (mCameraProperties.getRecordingRemainTime() <= 0) {
                    //sd卡满了
                    showToast(R.string.wifi_camera_storage);
                }
            }
        }
        currentCodec = mPreviewStream.getCodec(mPreviewStreamClient);
        if (currentCodec == ICatchCodec.ICH_CODEC_RGBA_8888) {
            mPreviewMjpg.setVisibility(View.VISIBLE);
            mPreviewMjpg.start(CameraFactory.getInstance().getSunplusCamera());

            if (mPreviewH264 != null) {
                mPreviewH264.setVisibility(View.GONE);
            }
        } else if (currentCodec == ICatchCodec.ICH_CODEC_H264) {
            mPreviewH264.setVisibility(View.VISIBLE);
            mPreviewH264.start(CameraFactory.getInstance().getSunplusCamera());

            if (mPreviewMjpg != null) {
                mPreviewMjpg.setVisibility(View.GONE);
            }
        }
    }

    private boolean stopPreview() {
        boolean retValue = false;
        if (currentCodec == ICatchCodec.ICH_CODEC_RGBA_8888) {
            retValue = mPreviewMjpg.stop();
        } else if (currentCodec == ICatchCodec.ICH_CODEC_H264) {
            retValue = mPreviewH264.stop();
        }
        return retValue;
    }

    private boolean stopMediaStream() {
        return mPreviewStream.stopMediaStream(mPreviewStreamClient);
    }

    private int getResolutionWidth(String resolution) {
        // MJPG?W=720&H=400&BR=4000000&
        String temp = resolution;
        temp = temp.replace("MJPG?W=", "");
        temp = temp.replace("&H=", " ");
        temp = temp.replace("&BR=", " ");
        temp = temp.replace("&", " ");
        String[] tempArray = temp.split(" ");
        return Integer.parseInt(tempArray[0]);
    }

    private int getResolutionHeigth(String resolution) {
        String temp = resolution;
        temp = temp.replace("MJPG?W=", "");
        temp = temp.replace("&H=", " ");
        temp = temp.replace("&BR=", " ");
        temp = temp.replace("&", " ");
        String[] tempArray = temp.split(" ");
        return Integer.parseInt(tempArray[1]);
    }

    private int getResolutionBitrate(String resolution) {
        String temp = resolution;
        temp = temp.replace("MJPG?W=", "");
        temp = temp.replace("&H=", " ");
        temp = temp.replace("&BR=", " ");
        temp = temp.replace("&", " ");
        String[] tempArray = temp.split(" ");
        return Integer.parseInt(tempArray[2]);
    }

    private Tristate changeCameraMode(ICatchPreviewMode previewMode) {
        LogUtils.e("[Normal] -- Main: start changeCameraMode previewMode =" + previewMode);
        Tristate ret = Tristate.FALSE;
        String cmd = mCameraProperties.getCurrentStreamInfo();
        // String cmd = "H264?W=1280&H=720&BR=2000000&FPS=15&";
        LogUtils.e("[Normal] -- Main: start changeCameraMode cmd =" + cmd);
        if (cmd == null) {
            return changeCameraModeNormal(previewMode);
        }
        LogUtils.e("[Normal] -- Main: Resolution cmd = " + cmd);
        if (cmd.contains("MJPG")) {
            ICatchMJPGStreamParam param = new ICatchMJPGStreamParam(getResolutionWidth(cmd), getResolutionHeigth(cmd), getResolutionBitrate(cmd), 50);

            LogUtils.e("[Normal] -- Main: begin startMediaStream");

            ret = mPreviewStream.startMediaStream(mPreviewStreamClient, param, previewMode);

            LogUtils.e("[Normal] -- Main: end  startMediaStream ret = " + ret);
            // JIRA ICOM-1839 Start add by b.jiang 2015-08-13
            if (ret == Tristate.FALSE) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hidepDialog();
                        showToast(R.string.Check_connection);
                        finish();
                    }
                });

            }
            // JIRA ICOM-1839 End add by b.jiang 2015-08-13
            // JIRA ICOM-1787 Start add by b.jiang 2015-08-13
            else if (ret == Tristate.ABNORMAL) {
                supportStreaming = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreviewH264.setVisibility(View.GONE);
                        mPreviewMjpg.setVisibility(View.GONE);
                        showToast(R.string.Check_connection);
                        hidepDialog();
                        finish();
                    }
                });

            } else {
                supportStreaming = true;
            }
            // JIRA ICOM-1787 End add by b.jiang 2015-08-13
            return ret;
        } else {
            ICatchCustomerStreamParam param = null;
            if (GlobalInfo.enableSoftwareDecoder) {
                cmd = ResolutionConvert.convert(cmd);
            }
            param = new ICatchCustomerStreamParam(554, cmd);
            LogUtils.e("[Normal] -- Main: begin startMediaStream cmd=" + cmd);
            ret = mPreviewStream.startMediaStream(mPreviewStreamClient, param, previewMode);
            LogUtils.e("[Normal] -- Main: end  startMediaStream ret = " + ret);
            // JIRA ICOM-1839 Start add by b.jiang 2015-08-13
            if (ret == Tristate.FALSE) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        hidepDialog();
                        showToast(R.string.Check_connection);
                        finish();
                    }
                });

            } else if (ret == Tristate.ABNORMAL) {
                supportStreaming = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPreviewH264.setVisibility(View.GONE);
                        mPreviewMjpg.setVisibility(View.GONE);
                        hidepDialog();
                        showToast(R.string.Check_connection);
                        finish();
                    }
                });

            } else {
                supportStreaming = true;
            }

            // JIRA ICOM-1787 End add by b.jiang 2015-08-13
            return ret;
        }
    }

    private Tristate changeCameraModeNormal(ICatchPreviewMode previewMode) {
        LogUtils.e("[Normal] -- Main: changeCameraModeNormal previewMode =" + previewMode);
        ICatchMJPGStreamParam param = new ICatchMJPGStreamParam();
        Tristate ret = Tristate.FALSE;

        LogUtils.e("[Normal] -- Main: begin start media stream");
        ret = mPreviewStream.startMediaStream(mPreviewStreamClient, param, previewMode);
        LogUtils.e("[Normal] -- Main: end  changeCameraModeNormal ret = " + ret);
        // JIRA ICOM-1839 Start add by b.jiang 2015-08-13

        if (ret == Tristate.FALSE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hidepDialog();
                    showToast(R.string.Check_connection);
                    finish();
                }
            });
        }
        // JIRA ICOM-1839 End add by b.jiang 2015-08-13
        // JIRA ICOM-1787 Start add by b.jiang 2015-08-13
        else if (ret == Tristate.ABNORMAL) {
            supportStreaming = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPreviewMjpg.setVisibility(View.GONE);
                    mPreviewH264.setVisibility(View.GONE);
                    hidepDialog();
                    showToast(R.string.Check_connection);
                    finish();
                }
            });

        } else {
            supportStreaming = true;
        }
        // JIRA ICOM-1787 End add by b.jiang 2015-08-13
        return ret;

    }

    private class SwitchResolution implements Runnable {
        private int mType, mPosition;

        public SwitchResolution(int type, int position) {
            mType = type;
            mPosition = position;
        }

        @Override
        public void run() {
            boolean ret;
            switch (mType) {
                case SunplusResolutionAdapter.VIDEO_RESOLUTION:
                    if (curMode == APP_STATE_VIDEO_CAPTURE) {
                        if (mCameraAction.stopVideoCapture()) {
                            //停止录制成功
                            curMode = APP_STATE_VIDEO_PREVIEW;
                        } else {
                            //停止录制失败
                            showToastOnUiThread(R.string.stop_recording_failed);
                            return;
                        }
                    }
                    if (mCameraProperties.getVideoSizeFlow() == 1) {
                        stopMediaStream();
                        LogUtils.e("切换分辨率停止流");
                    }
                    List<String> valueList = CameraFactory.getInstance().getSunplusCamera().getVideoSize().getValueList();
                    ret = mCameraProperties.setVideoSize(valueList.get(mPosition));
                    mCameraProperties.getRecordingRemainTime();//没有这句开启录制后会导致机器死机
                    cacheTime = mCameraProperties.getPreviewCacheTime();
                    if (cacheTime < 200) {
                        cacheTime = 200;
                    }
                    final Tristate tristate = changeCameraMode(ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                    if (tristate == Tristate.FALSE) {
                        return;
                    }
                    ICatchWificamConfig.getInstance().setPreviewCacheParam(cacheTime, 200);
                    if (mCameraAction.startMovieRecord()) {
                        //开启录制成功
                        curMode = APP_STATE_VIDEO_CAPTURE;
                    } else {
                        //开启录制失败
                    }
                    final boolean tempRet1 = ret;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tristate == Tristate.NORMAL) {
                                startPreview();
                            }
                            if (tempRet1) {
                                showToastOnUiThread(R.string.set_success);
                            } else {
                                showToastOnUiThread(R.string.set_failure);
                            }
                            hidepDialog();
                        }
                    });

                    break;
                case SunplusResolutionAdapter.PHOTO_RESOLUTION:
                    ret = mSunplusCamera.getImageSize().setValueByPosition(mPosition);
                    mCameraProperties.getRemainImageNum();
                    final boolean tempRet = ret;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (supportStreaming) {
                                startPreview();
                            }
                            if (tempRet) {
                                showToastOnUiThread(R.string.set_success);
                            } else {
                                showToastOnUiThread(R.string.set_failure);
                            }
                            hidepDialog();
                        }
                    });

                    break;
                default:
                    break;
            }
        }
    }

    ;
    private Runnable switchModeThread = new Runnable() {
        @Override
        public void run() {
            Tristate ret;
            if (curMode == APP_STATE_VIDEO_CAPTURE) {
                //录制模式，录制状态
                if (mCameraAction.stopVideoCapture()) {
                    //停止录制成功
                    curMode = APP_STATE_VIDEO_PREVIEW;
                } else {
                    //停止录制失败
                    sendEmptyMessage(MODE_CHANGE_FAILED);
                    return;
                }
            }
            if (curMode == APP_STATE_STILL_PREVIEW || curMode == APP_STATE_TIMELAPSE_PREVIEW_STILL || curMode == APP_STATE_TIMELAPSE_PREVIEW_VIDEO) {
                stopMediaStream();
                ret = changeCameraMode(ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                if (ret == Tristate.FALSE) {
                    sendEmptyMessage(MODE_CHANGE_FAILED);
                    return;
                }
                curMode = APP_STATE_VIDEO_PREVIEW;
                //录制模式，非录制状态
                if (!mCameraProperties.isSDCardExist()) {
                    //没有插入sd卡
//                    showToast(R.string.wifi_sdcard);
                } else if (mCameraProperties.getRecordingRemainTime() <= 0) {
                    //sd卡满了
//                    showToast(R.string.wifi_camera_storage);
                } else {
                    if (mCameraAction.startMovieRecord()) {
                        //开启录制成功
                        curMode = APP_STATE_VIDEO_CAPTURE;
                    } else {
                        //开启录制失败
                        showToast(R.string.start_record_failed);
                    }
                }
                sendEmptyMessage(MODE_CHANGE_SUCCESS);
            }
            //切换成拍照模式
            else if (curMode == APP_STATE_VIDEO_PREVIEW) {
                stopMediaStream();
                ret = changeCameraMode(ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                if (ret == Tristate.FALSE) {
                    sendEmptyMessage(MODE_CHANGE_FAILED);
                    return;
                }
                curMode = APP_STATE_STILL_PREVIEW;
                sendEmptyMessage(MODE_CHANGE_SUCCESS);
            }
        }
    };

    private Runnable takePhotoThread = new Runnable() {
        @Override
        public void run() {
            stopPreview();
            mPreviewStream.stopMediaStream(mPreviewStreamClient);
            CameraAction.getInstance().capturePhoto();
        }
    };
    private Runnable onStartThread = new Runnable() {
        @Override
        public void run() {
            Tristate ret = Tristate.FALSE;
            mCameraProperties.getRemainImageNum();
            mCameraProperties.getRecordingRemainTime();
            if (mCameraState.isMovieRecording()) {
                LogUtils.e("onStart: [Normal] -- Main: restart recording");
                curMode = APP_STATE_VIDEO_CAPTURE;
                ret = changeCameraMode(ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                if (ret == Tristate.FALSE) {
                    return;
                }
            } else if (curMode == APP_STATE_VIDEO_PREVIEW) {
                LogUtils.e("[Normal] -- Main: curMode == APP_STATE_VIDEO_PREVIEW");
                ret = changeCameraMode(ICatchPreviewMode.ICH_VIDEO_PREVIEW_MODE);
                if (ret == Tristate.FALSE) {
                    return;
                }
                //录制模式，非录制状态
                if (!mCameraProperties.isSDCardExist()) {
                    //没有插入sd卡
//                    showToastOnUiThread(R.string.wifi_sdcard);
                } else if (mCameraProperties.getRecordingRemainTime() <= 0) {
                    //sd卡满了
                    showToastOnUiThread(R.string.wifi_camera_storage);
                } else {
                    if (mCameraAction.startMovieRecord()) {
                        //开启录制成功
                        curMode = APP_STATE_VIDEO_CAPTURE;
                    } else {
                        //开启录制失败
                        showToastOnUiThread(R.string.start_record_failed);
                    }
                }
            } else if (curMode == APP_STATE_STILL_PREVIEW) {
                LogUtils.e("[Normal] -- Main: curMode == ICH_STILL_PREVIEW_MODE");
                ret = changeCameraMode(ICatchPreviewMode.ICH_STILL_PREVIEW_MODE);
                if (ret == Tristate.FALSE) {
                    return;
                }
            }
            GlobalInfo.forbidAudioOutput = !mPreviewStream.supportAudio(mPreviewStreamClient);
//            if (curMode == APP_STATE_VIDEO_PREVIEW) {
//                //录制模式，非录制状态
//                if (!mCameraProperties.isSDCardExist()) {
//                    //没有插入sd卡
////                    showToastOnUiThread(R.string.wifi_sdcard);
//                } else if (mCameraProperties.getRecordingRemainTime() <= 0) {
//                    //sd卡满了
//                    showToastOnUiThread(R.string.wifi_camera_storage);
//                } else {
//                    if (mCameraAction.startMovieRecord()) {
//                        //开启录制成功
//                        curMode = APP_STATE_VIDEO_CAPTURE;
//                    } else {
//                        //开启录制失败
//                        showToastOnUiThread(R.string.wifi_camera_setting_fail);
//                    }
//                }
//            }
            if (ret == Tristate.NORMAL) {
                sendEmptyMessage(ON_START);
            }
        }
    };

    private void sendEmptyMessage(int what) {
        mCameraHandler.sendEmptyMessage(what);
    }

    public void changeModeView() {
//        mRgMode.check(curMode != APP_STATE_STILL_PREVIEW ? R.id.rb_video_mode : R.id.rb_photo_mode);
        mLlPicture.setBackgroundResource(curMode != APP_STATE_STILL_PREVIEW ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        if (curMode == APP_STATE_STILL_PREVIEW) {
            mLlPicture.setBackgroundResource(R.drawable.bg_circle);
        }
//        mLlPicture.setClickable(curMode == APP_STATE_STILL_PREVIEW);
        mTvTakePhoto.setText(curMode == APP_STATE_STILL_PREVIEW ? R.string.take_photo : R.string.record);
        mIvTakePhoto.setBackgroundResource(curMode != APP_STATE_STILL_PREVIEW ? R.drawable.record : R.drawable.preview_picture);
        if (curMode != APP_STATE_STILL_PREVIEW) {
            showRecordState(curMode == APP_STATE_VIDEO_CAPTURE);
        } else {
            mIvRecord.setVisibility(View.GONE);
            stopRecordTime();
        }
        mTvRecordTime.setVisibility(curMode == APP_STATE_STILL_PREVIEW ? View.GONE : View.VISIBLE);
        iv_record_wait.setBackgroundResource(curMode != APP_STATE_STILL_PREVIEW ? R.drawable.preview_picture_orange : R.drawable.record_orange);


    }

    public void showRecordState(boolean isRecord) {
        mIvRecord.setVisibility(isRecord ? View.VISIBLE : View.GONE);
        mLlPicture.setBackgroundResource(!isRecord ? R.drawable.bg_circle : R.drawable.bg_red_cicle);
        if (isRecord) {
            startRecordTime(mCameraProperties.getVideoRecordingTime());
        } else {
            stopRecordTime();
        }
    }

    private int recordTime;

    private void stopRecordTime() {
        mCameraHandler.removeCallbacks(recordTimer);
        recordTime = 0;
        mTvRecordTime.setText("00:00:00");
    }

    private void startRecordTime(int time) {
        recordTime = time;
        mCameraHandler.removeCallbacks(recordTimer);
        mCameraHandler.postDelayed(recordTimer, 1000);
    }

    private Runnable recordTimer = new Runnable() {
        @Override
        public void run() {
            if (recordTime % 30 == 0) {
                recordTime = mCameraProperties.getVideoRecordingTime();
                Log.e(TAG, "run: " + recordTime);
            }else{
                recordTime++;
            }
            int hour = recordTime / 3600;
            int minute = recordTime % 3600 / 60;
            int seconds = recordTime % 60;
            mTvRecordTime.setText(String.format("%02d", hour) + ":" + String.format("%02d", minute) + ":" + String.format("%02d", seconds));
            mCameraHandler.postDelayed(this, 1000);
        }
    };
    private boolean isClickable = true;

    @Override
    public void onClick(View v) {
        if (!isClickable) {
            return;
        }
        isClickable = false;
        switch (v.getId()) {
            case R.id.ll_record_wait:

                if (curMode == APP_STATE_STILL_PREVIEW || curMode == APP_STATE_TIMELAPSE_PREVIEW_STILL || curMode == APP_STATE_TIMELAPSE_PREVIEW_VIDEO) {
                    showpDialog();
                    if (!stopPreview()) {
                        showToast(getString(R.string.set_failure));
                        hidepDialog();
                        break;
                    }
                    executor.submit(switchModeThread);
                } else if (curMode == APP_STATE_VIDEO_PREVIEW || curMode == APP_STATE_VIDEO_CAPTURE) {
                    showpDialog();
                    if (!stopPreview()) {
                        showToast(getString(R.string.set_failure));
                        hidepDialog();
                        break;
                    }
                    executor.submit(switchModeThread);
                }


                break;
            case R.id.iv_fullscreen:
            case R.id.iv_fullscreen_land:
                setRequestedOrientation(isPortrait ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.iv_setting:
//                if (curMode == APP_STATE_VIDEO_CAPTURE) {
//                    if (mCameraAction.stopVideoCapture()) {
//                        //停止录制成功
//                        curMode = APP_STATE_VIDEO_PREVIEW;
//                    } else {
//                        //停止录制失败
//                        showToast(R.string.stop_recording_failed);
//                        break;
//                    }
//                }
//                stopPreview();
//                stopMediaStream();
//                hadStop = true;
//                Intent intent = new Intent(this, SunplusSettingActivity.class);
//                intent.putExtra(SunplusSettingActivity.IS_VIDEO_MODE, curMode == APP_STATE_VIDEO_PREVIEW);
//                startActivity(intent);
                if (curMode == APP_STATE_VIDEO_CAPTURE) {
                    showpDialog(R.string.msg_center_stop_recording);
                } else {
                    showpDialog();
                }
                executor.submit(gotoSettingThread);
                break;
            case R.id.ll_video:
//                if (curMode == APP_STATE_VIDEO_CAPTURE) {
//                    if (mCameraAction.stopVideoCapture()) {
//                        //停止录制成功
//                        curMode = APP_STATE_VIDEO_PREVIEW;
//                    } else {
//                        //停止录制失败
//                        showToast(R.string.stop_recording_failed);
//                        break;
//                    }
//                }
//                stopPreview();
//                stopMediaStream();
//                hadStop = true;
//                SunplusFileActivity.start(this, 2, FileManagerConstant.TYPE_REMOTE_VIDEO);
                new GoFileManagerTask(FileManagerConstant.TYPE_REMOTE_VIDEO).execute();
                break;
            case R.id.ll_photo:
//                if (curMode == APP_STATE_VIDEO_CAPTURE) {
//                    if (mCameraAction.stopVideoCapture()) {
//                        //停止录制成功
//                        curMode = APP_STATE_VIDEO_PREVIEW;
//                    } else {
//                        //停止录制失败
//                        showToast(R.string.stop_recording_failed);
//                        break;
//                    }
//                }
//                stopPreview();
//                stopMediaStream();
//                hadStop = true;
//                SunplusFileActivity.start(this, 3, FileManagerConstant.TYPE_REMOTE_PHOTO);
                new GoFileManagerTask(FileManagerConstant.TYPE_REMOTE_PHOTO).execute();

                break;
            case R.id.ll_picture:
//                if (curMode == APP_STATE_STILL_PREVIEW) {
//                    //拍照模式
//                    if (!mCameraProperties.isSDCardExist()) {
//                        //没有插入sd卡
//                        showToast(R.string.wifi_sdcard);
//                        break;
//                    } else if (mCameraProperties.getRemainImageNum() < 1) {
//                        //sd卡满了
//                        showToast(R.string.wifi_camera_storage);
//                        break;
//                    }
//                    showpDialog();
//                    curMode = APP_STATE_STILL_CAPTURE;
//                    executor.submit(takePhotoThread);
//                }
                if (curMode == APP_STATE_VIDEO_PREVIEW) {
                    //录制模式，非录制状态
                    if (!mCameraProperties.isSDCardExist()) {
                        //没有插入sd卡
                        showToast(R.string.wifi_sdcard);
                        break;
                    } else if (mCameraProperties.getRecordingRemainTime() <= 0) {
                        //sd卡满了
                        showToast(R.string.wifi_camera_storage);
                        break;
                    }
                    if (mCameraAction.startMovieRecord()) {
                        //开启录制成功
                        curMode = APP_STATE_VIDEO_CAPTURE;
                    } else {
                        //开启录制失败
                        showToast(R.string.set_failure);
                        break;
                    }
                    showRecordState(curMode == APP_STATE_VIDEO_CAPTURE);
                } else if (curMode == APP_STATE_VIDEO_CAPTURE) {
                    //录制模式，录制状态
                    if (mCameraAction.stopVideoCapture()) {
                        //停止录制成功
                        curMode = APP_STATE_VIDEO_PREVIEW;
                    } else {
                        //停止录制失败
                        showToast(R.string.set_failure);
                        break;
                    }
                    showRecordState(curMode == APP_STATE_VIDEO_CAPTURE);
                } else if (curMode == APP_STATE_STILL_PREVIEW) {

                    //拍照模式
                    if (!mCameraProperties.isSDCardExist()) {
                        //没有插入sd卡
                        showToast(R.string.wifi_sdcard);
                        break;
                    } else if (mCameraProperties.getRemainImageNum() < 1) {
                        //sd卡满了
                        showToast(R.string.wifi_camera_storage);
                        break;
                    }
                    showpDialog();
                    curMode = APP_STATE_STILL_CAPTURE;
                    executor.submit(takePhotoThread);
                }
                break;
            case R.id.rb_video_mode:
                if (curMode == APP_STATE_STILL_PREVIEW || curMode == APP_STATE_TIMELAPSE_PREVIEW_STILL || curMode == APP_STATE_TIMELAPSE_PREVIEW_VIDEO) {
                    showpDialog();
                    if (!stopPreview()) {
                        showToast(getString(R.string.set_failure));
                        hidepDialog();
                        break;
                    }
                    executor.submit(switchModeThread);
                }
                break;
            case R.id.rb_photo_mode:
                //切换成拍照模式
                if (curMode == APP_STATE_VIDEO_PREVIEW || curMode == APP_STATE_VIDEO_CAPTURE) {
                    showpDialog();
                    if (!stopPreview()) {
                        showToast(getString(R.string.set_failure));
                        hidepDialog();
                        break;
                    }
                    executor.submit(switchModeThread);
                }
                break;
            case R.id.iv_pip:
            case R.id.iv_horizontal_pip:
                break;
            case R.id.video_frame:
                if (mIvFullscreen.getVisibility() == View.VISIBLE) {
                    mIvFullscreen.setVisibility(View.GONE);
                    mLlResolution.setVisibility(View.GONE);
                    mCameraHandler.removeCallbacks(autoHideViewTask);
                } else {
                    mRvResolution.setVisibility(View.GONE);
                    mIvFullscreen.setVisibility(View.VISIBLE);
//                    mLlResolution.setVisibility(View.VISIBLE);
                    autoHideView();
                }
                break;
            case R.id.ll_resolution:
                mRvResolution.setVisibility(mRvResolution.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                mRvResolution.post(new Runnable() {
                    @Override
                    public void run() {
                        int height = mRvResolution.getHeight();
                        if (height > ScreenUtils.dp2px(SunplusPreviewActivity.this, 96)) {
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) mRvResolution.getLayoutParams();
                            layoutParams.height = ScreenUtils.dp2px(SunplusPreviewActivity.this, 96);
                            mRvResolution.setLayoutParams(layoutParams);
                        }
                    }
                });
                autoHideView();
                break;
            default:
                break;
        }
        isClickable = true;
    }

    private Runnable gotoSettingThread = new Runnable() {
        @Override
        public void run() {
            if (curMode == APP_STATE_VIDEO_CAPTURE) {
                if (mCameraAction.stopVideoCapture()) {
                    //停止录制成功
                    curMode = APP_STATE_VIDEO_PREVIEW;
                } else {
                    //停止录制失败
                    UIUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            hidepDialog();
                            showToast(R.string.stop_recording_failed);
                        }
                    });
                    return;
                }
            }
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    stopPreview();
                }
            });
            stopMediaStream();
            hadStop = true;
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    hidepDialog();
                    Intent intent = new Intent(SunplusPreviewActivity.this, SunplusSettingActivity.class);
                    intent.putExtra(SunplusSettingActivity.IS_VIDEO_MODE, curMode == APP_STATE_VIDEO_PREVIEW);
                    startActivity(intent);
                }
            });
        }
    };

    @SuppressLint("StaticFieldLeak")
    private class GoFileManagerTask extends AsyncTask<Void, Void, Boolean> {

        private final int mType;

        public GoFileManagerTask(int type) {
            mType = type;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (curMode == APP_STATE_VIDEO_CAPTURE) {
                showpDialog(R.string.msg_center_stop_recording);
            } else {
                showpDialog();
            }
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (curMode == APP_STATE_VIDEO_CAPTURE) {
                if (mCameraAction.stopVideoCapture()) {
                    //停止录制成功
                    curMode = APP_STATE_VIDEO_PREVIEW;
                } else {
                    //停止录制失败
                    return false;
                }
            }
            UIUtils.post(new Runnable() {
                @Override
                public void run() {
                    stopPreview();
                }
            });
            stopMediaStream();
            hadStop = true;
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aVoid) {
            super.onPostExecute(aVoid);
            hidepDialog();
            if (aVoid) {
                switch (mType) {
                    case FileManagerConstant.TYPE_REMOTE_VIDEO:
                        SunplusFileActivity.start(SunplusPreviewActivity.this, 2, FileManagerConstant.TYPE_REMOTE_VIDEO);
                        break;
                    case FileManagerConstant.TYPE_REMOTE_PHOTO:
                        SunplusFileActivity.start(SunplusPreviewActivity.this, 3, FileManagerConstant.TYPE_REMOTE_PHOTO);
                        break;
                    default:
                        break;
                }
            } else {
                showToast(R.string.stop_recording_failed);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int layoutDirection = newConfig.orientation;
        isPortrait = layoutDirection != Configuration.ORIENTATION_LANDSCAPE;
        changeOrientation(isPortrait);
    }

    public void changeOrientation(boolean isPortrait) {
        this.isPortrait = isPortrait;
//        horizontal_frame.setVisibility(isPortrait ? View.GONE : View.VISIBLE);
        mRlBottom.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mRlTitle.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
//        mRgMode.setVisibility(isPortrait ? View.VISIBLE : View.GONE);
        mIvFullscreen.setBackgroundResource(isPortrait ? R.drawable.selector_fullscreen : R.drawable.selector_exit_fullscreen);
        if (isPortrait) {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.white));
            mVideoFrame.setLayoutParams(portraitParams);
            final WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().setAttributes(attrs);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            ll_record_wait.setLayoutParams(mRecordWaitPortraitParams);
            mLlPicture.setLayoutParams(mLlPicturePortraitParams);
            mIvRecord.setLayoutParams(mRecordPortraitParams);

        } else {
            rl_novatek_camera.setBackgroundColor(getResources().getColor(R.color.dark_black));
            mVideoFrame.setLayoutParams(landscapeParams);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

            mLlPicture.setLayoutParams(mLLPictureLandscapeParams);
            ll_record_wait.setLayoutParams(mRecordWaitLandscapeParams);
            mIvRecord.setLayoutParams(mRecordLandscapeParams);
            mIvFullscreen.setVisibility(View.GONE);
        }
//        pictureVisible(curMode == APP_STATE_STILL_PREVIEW );
    }

    public void pictureVisible(boolean visible) {
        mLlPicture.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void goBack() {
        super.goBack();
        if (curMode == APP_STATE_VIDEO_CAPTURE) {
            if (mCameraAction.stopVideoCapture()) {
                //停止录制成功
                curMode = APP_STATE_VIDEO_PREVIEW;
            }
        }
        VoiceManager.isCameraBusy = true;

        new Thread(new Runnable() {
            @Override
            public void run() {

                mSunplusCamera.deInitCamera();
                VoiceManager.isCameraBusy = false;
                UIUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        WifiUtil.getInstance().checkAvailableNetwork(SunplusPreviewActivity.this);
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
