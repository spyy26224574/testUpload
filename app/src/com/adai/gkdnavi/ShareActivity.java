package com.adai.gkdnavi;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.Hjni.HbxFishEye;
import com.adai.gkd.bean.LocationBean;
import com.adai.gkd.bean.request.CheckSessionBean;
import com.adai.gkd.bean.square.ShareVideoBean;
import com.adai.gkd.bean.square.ShareVideoPageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.adapter.SharePhotoRecyclerAdapter;
import com.adai.gkdnavi.utils.GpsUtil;
import com.adai.gkdnavi.utils.ImageUriUtil;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.UISwitchButton;
import com.adai.gkdnavi.utils.VoiceManager;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.FilePickerConst;
import com.oss.bean.OssUploadParam;
import com.oss.utils.OSSRequestUtil;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.preview.PhotoPreviewActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ShareActivity extends BaseActivity implements View.OnClickListener {

    private static final int REQUEST_LOGIN_CODE = 1;
    public static final int REQUESE_SELECT_PHOTO_CODE = 2;
    public static final int REQUEST_PICKPHOTO_CODE = 3;
    public static final int REQUEST_LOCATION_CODE = 4;
    /**
     * 0为视频分享，1为图片分享
     */
    private int shareType = 0;
    private String video_path;
    private String video_logo_path;
    private int video_time = 0;
    private int videoType = 0;
    private int fishEyeId = 0;
    private int width = 0;
    private int height = 0;

    private EditText share_content;
    private ImageView video_logo;
    private TextView share_location;
    private UISwitchButton ispublic, isreview, isShowLocation;

    private RecyclerView photo_grid;
    private String photo_path;
    private ArrayList<String> shareImages = new ArrayList<>();
    private SharePhotoRecyclerAdapter adapter;

    //定位相关
    private LocationClient mLocClient;
    private BDLocation location;
    private boolean afterfisetloction = false;
    private ImageView right_img;
    private int mShareAppTag;
    private LocationBean mLocationBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        initView();
        init();
        initLocation();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkConnectChangedReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkConnectChangedReceiver);
        super.onDestroy();
    }

    private void shareUrl2Other(ShareVideoBean data) {
        finish();
        if (mShareAppTag == ShareUtils.Appinfo.RIDERS) {
            showToast(R.string.share_success);
        }
        if (data != null && !TextUtils.isEmpty(data.share_address)) {
            new ShareUtils().shareToApp(mShareAppTag, ShareActivity.this, data.share_address, data.title, data.title, data.cover_picture);
        }
    }

    private final BroadcastReceiver mNetworkConnectChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (afterfisetloction) {
                if (location != null && location.getAddrStr() != null) {
                    //已有数据，无需操作
                } else {
                    if (mLocClient != null) {
                        if (!mLocClient.isStarted()) {
                            mLocClient.start();
                        }
                    }
                }
            }
        }
    };

    private void initLocation() {
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(listener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setIsNeedAddress(true);
        option.setCoorType("gcj02"); // 设置坐标类型
        option.setScanSpan(1000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    private void showLocation() {
        if (isShowLocation.isChecked()) {
            if (location != null && location.getAddrStr() != null) {
                share_location.setText(location.getAddrStr());
                Log.e(_TAG_, "showLocation: " + location.getBuildingName());
            } else {
                share_location.setText(getString(R.string.navi_locationFail));
            }
        } else {
            share_location.setText(getString(R.string.show_location));
        }
    }

    private BDLocationListener listener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            afterfisetloction = true;
            if (bdLocation != null) {
                mLocClient.stop();
                location = bdLocation;
                if (mLocationBean == null) {
                    mLocationBean = new LocationBean();
                }
                mLocationBean.address = TextUtils.isEmpty(location.getBuildingName()) ? location.getAddrStr() : location.getBuildingName();
                double[] latLon = GpsUtil.bd09_To_gps84(location.getLatitude(), location.getLongitude());
                mLocationBean.lat = latLon[0];
                mLocationBean.lng = latLon[1];
                showLocation();
            }
        }
    };

    @Override
    protected void initView() {
        super.initView();
        share_content = (EditText) findViewById(R.id.share_text);
        video_logo = (ImageView) findViewById(R.id.video_logo);
        share_location = (TextView) findViewById(R.id.location);
        ispublic = (UISwitchButton) findViewById(R.id.ispublic);
        ispublic.setChecked(true);
        isreview = (UISwitchButton) findViewById(R.id.isReview);
        isreview.setChecked(true);
        isShowLocation = (UISwitchButton) findViewById(R.id.isShowLocation);
        isShowLocation.setChecked(true);
        photo_grid = (RecyclerView) findViewById(R.id.photo_grid);
        right_img = (ImageView) findViewById(R.id.right_img);
        right_img.setImageResource(R.drawable.bg_share_orange_selector);
        right_img.setVisibility(View.GONE);
        right_img.setOnClickListener(this);
        TextView righttext = (TextView) findViewById(R.id.right_text);
        righttext.setVisibility(View.VISIBLE);
        righttext.setText(R.string.share);
        righttext.setOnClickListener(this);
        findViewById(R.id.share_location_line).setOnClickListener(this);
    }

    @Override
    protected void init() {
        super.init();
        if (CurrentUserInfo.access_token == null) {
            CurrentUserInfo.initUserinfo(mContext);
        }
        Intent data = getIntent();
        String title = getIntent().getStringExtra("title");
        setTitle(getString(R.string.share_to, title));
        mShareAppTag = data.getIntExtra("app", ShareUtils.Appinfo.RIDERS);
        if (data.hasExtra("shareType")) {
            shareType = data.getIntExtra("shareType", 0);
            video_path = data.getStringExtra("video_path");
            video_logo_path = data.getStringExtra("video_logo_path");
            video_time = data.getIntExtra("video_time", 0);
            Log.e("9529", "video_path = " + video_path);
            int[] ints = HbxFishEye.GetId(video_path);
            videoType = ints[0];
            fishEyeId = ints[1];
            Log.e("9529", "videoType = " + videoType + "fishEyeId = " + fishEyeId + "width = " + width + "height = " + height);

        } else {
            shareType = 1;
            if (Intent.ACTION_SEND.equals(data.getAction())) {
                Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null) {
                    shareImages.add(ImageUriUtil.getImageAbsolutePath(this, uri));
                }
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(data.getAction())) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    ClipData clipdata = getIntent().getClipData();
                    if (clipdata != null) {
                        int max = clipdata.getItemCount() > VLCApplication.MAX_PHOTO_NUM ? VLCApplication.MAX_PHOTO_NUM : clipdata.getItemCount();
                        for (int i = 0; i < max; i++) {
                            ClipData.Item item = clipdata.getItemAt(i);
                            String path = ImageUriUtil.getImageAbsolutePath(this, item.getUri());
                            shareImages.add(path);
                            Log.e(_TAG_, "path=" + path);
                        }
                    }
                }
                if (shareImages.size() <= 0) {
                    ArrayList<Uri> uris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    if (uris != null) {
                        int max = uris.size() > VLCApplication.MAX_PHOTO_NUM ? VLCApplication.MAX_PHOTO_NUM : uris.size();
                        for (int i = 0; i < max; i++) {
                            String path = ImageUriUtil.getImageAbsolutePath(this, uris.get(i));
                            shareImages.add(path);
                            Log.e(_TAG_, "url=" + path);
                        }
                    }
                }
            }
        }
        switch (shareType) {
            case 0:
                initVideoShare();
                break;
            case 1:
                initPhotoShare();
                break;
            default:
                break;
        }
    }

    private void initVideoShare() {
        video_logo.setImageURI(Uri.parse(video_logo_path));
    }

    private void initPhotoShare() {
        video_logo.setVisibility(View.GONE);
        photo_grid.setVisibility(View.VISIBLE);
        adapter = new SharePhotoRecyclerAdapter(this, 9);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setSmoothScrollbarEnabled(true);
        gridLayoutManager.setAutoMeasureEnabled(true);
        photo_grid.setLayoutManager(gridLayoutManager);
        photo_grid.setHasFixedSize(true);
        photo_grid.setNestedScrollingEnabled(false);
        photo_grid.setAdapter(adapter);
        if (shareImages != null) {
//            ArrayList<String> temp = new ArrayList<String>();
//            temp.add(photo_path);
            adapter.addPhotos(shareImages);
        }

    }

    private void onShareVideo() {
        final String des = share_content.getText().toString();
        if (TextUtils.isEmpty(des)) {
            showToast(R.string.input_description);
            return;
        }
//        String address = location != null ? (TextUtils.isEmpty(location.getBuildingName()) ? location.getAddrStr() : location.getBuildingName()) : "";
        final String address = mLocationBean != null ? (TextUtils.isEmpty(mLocationBean.address) ? "" : mLocationBean.address) : "";
        final double lon = location == null ? 0 : location.getLongitude();
        final double lat = location == null ? 0 : location.getLatitude();
        final String isOpen = ispublic.isChecked() ? "Y" : "N";
        final String review = isreview.isChecked() ? "Y" : "N";
        setHpDialogCancelable(false);
        showHpDialog(getString(R.string.sharing));
        setDialogMax(100);
        List<OssUploadParam> ossUploadParams = new ArrayList<>();
        final OssUploadParam videoParam = new OssUploadParam(video_path);
        videoParam.fileSize = new File(video_path).length();
        ossUploadParams.add(videoParam);
        final OssUploadParam videoLogParam = new OssUploadParam(video_logo_path);
        if (!TextUtils.isEmpty(video_logo_path)) {
            videoLogParam.setFileType(FileTypeUtil.TYPE_THUMBNAIL);
            videoLogParam.fileSize = new File(video_logo_path).length();
            ossUploadParams.add(videoLogParam);
        }
        OSSRequestUtil.getInstance().postFile2OSS(ossUploadParams, new OSSRequestUtil.MultiUploadCallBack() {
            @Override
            public void onUploadComplete(List<String> objectKey) {
                RequestMethods_square.shareVideo(videoParam.partObject, videoLogParam.partObject, des, video_time, isShowLocation.isChecked() ? address : "", String.valueOf(lat), String.valueOf(lon), videoType, fishEyeId, width, height, isOpen, review, new HttpUtil.Callback<ShareVideoPageBean>() {
                    @Override
                    public void onCallback(ShareVideoPageBean result) {
                        hideHpDialog();
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
//                                    onShareToOther(result.data);
                                    shareUrl2Other(result.data);
                                    break;
                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        right_img.setClickable(true);
                    }
                });
            }

            @Override
            public void onUploading(int progress) {
                setDialogProgress(progress);
            }

            @Override
            public void onUploadFail() {
                hideHpDialog();
                showToast(R.string.upload_failed);
            }
        });
    }

    private void onsharePhoto() {
        if (adapter == null) {
            return;
        }
        if (adapter.getPhotos().size() <= 0) {
            showToast(R.string.select_share_picture);
            return;
        }

        final String des = share_content.getText().toString();
        if (TextUtils.isEmpty(des)) {
            showToast(R.string.input_description);
            return;
        }
//        String address = location != null ? (TextUtils.isEmpty(location.getBuildingName()) ? location.getAddrStr() : location.getBuildingName()) : "";
        final String address = mLocationBean != null ? (TextUtils.isEmpty(mLocationBean.address) ? "" : mLocationBean.address) : "";
        final double lon = mLocationBean == null ? 0 : mLocationBean.lng;
        final double lat = mLocationBean == null ? 0 : mLocationBean.lat;
        final String isOpen = ispublic.isChecked() ? "Y" : "N";
        final String review = isreview.isChecked() ? "Y" : "N";
        setHpDialogCancelable(false);
        showHpDialog(getString(R.string.sharing));
        setDialogMax(100);
        List<OssUploadParam> ossUploadParams = new ArrayList<>();
        for (String photo : adapter.getPhotos()) {
            OssUploadParam ossUploadParam = new OssUploadParam(photo);
            ossUploadParam.fileSize = new File(photo).length();
            ossUploadParams.add(ossUploadParam);
        }
        OSSRequestUtil.getInstance().postFile2OSS(ossUploadParams, new OSSRequestUtil.MultiUploadCallBack() {
            @Override
            public void onUploadComplete(List<String> objectKey) {
                RequestMethods_square.sharePhoto(objectKey, des, isShowLocation.isChecked() ? address : "", String.valueOf(lat), String.valueOf(lon), isOpen, review, new HttpUtil.Callback<ShareVideoPageBean>() {
                    @Override
                    public void onCallback(ShareVideoPageBean result) {
                        hideHpDialog();
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
//                                    onShareToOther(result.data);
                                    shareUrl2Other(result.data);
                                    break;
                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        right_img.setClickable(true);
                    }
                });
            }

            @Override
            public void onUploading(int progress) {
                setDialogProgress(progress);
            }

            @Override
            public void onUploadFail() {
                hideHpDialog();
                showToast(R.string.upload_failed);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_text:
            case R.id.right_img:
                if (VoiceManager.isLogin) {
                    askShare();
                } else {
                    Intent login = new Intent(mContext, LoginActivity.class);
                    startActivityForResult(login, REQUEST_LOGIN_CODE);
                    return;
                }
                break;
            case R.id.share_location_line:
//                showShareDialog();
                if (isShowLocation.isChecked()) {
                    startActivityForResult(new Intent(this, ShareLocationActivity.class), REQUEST_LOCATION_CODE);
                }
                break;
            default:
                break;
        }
    }

    private void askShare() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ShareActivity.this);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.share_need_net);

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                RequestMethods.checkSessionValid(CurrentUserInfo.id, new HttpUtil.Callback<CheckSessionBean>() {
                    @Override
                    public void onCallback(CheckSessionBean result) {
                        if (result.ret != -1) {
                            if (result.data != null && result.data.isValid == 1) {
                                right_img.setClickable(false);
                                if (shareType == 0) {
                                    onShareVideo();
                                } else if (shareType == 1) {
                                    onsharePhoto();
                                }
                            } else {
                                Intent login = new Intent(mContext, LoginActivity.class);
                                startActivityForResult(login, REQUEST_LOGIN_CODE);
                            }
                        } else {
                            showToast(result.message);
                        }
                    }
                });
            }
        });
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        builder.create().show(); //	Diglog的显示
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case REQUEST_LOGIN_CODE:
                if (data.getBooleanExtra("islogin", false)) {
                    if (shareType == 0) {
                        onShareVideo();
                    } else if (shareType == 1) {
                        onsharePhoto();
                    }
                }
                break;
            case FilePickerConst.REQUEST_CODE:
                if (data != null) {
                    ArrayList<String> filePaths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS);
                    adapter.addPhotos(filePaths);
                }
                break;
            case REQUESE_SELECT_PHOTO_CODE:
                if (data != null) {
                    ArrayList<String> select = data.getStringArrayListExtra(PhotoPreviewActivity.KEY_SELECT_LIST);
                    adapter.addPhotos(select);
                }
                break;
            case REQUEST_PICKPHOTO_CODE:
                if (data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                    adapter.addPhotos(result);
                }
                break;
            case REQUEST_LOCATION_CODE:
                //返回的地址
                if (data != null) {
                    mLocationBean = (LocationBean) data.getSerializableExtra("location");
                    location.setAddrStr(mLocationBean.address);
                    location.setLatitude(mLocationBean.lat);
                    location.setLongitude(mLocationBean.lng);
                    location.setBuildingName(mLocationBean.name);
                    share_location.setText(mLocationBean.name);
                    double[] latLon = GpsUtil.bd09_To_gps84(mLocationBean.lat, mLocationBean.lng);
                    mLocationBean.lat = latLon[0];
                    mLocationBean.lng = latLon[1];
                }
                break;
            default:
                break;
        }
    }
}
