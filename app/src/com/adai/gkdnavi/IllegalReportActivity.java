package com.adai.gkdnavi;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.IllegalTypeBean;
import com.adai.gkd.bean.LocationBean;
import com.adai.gkd.bean.params.IllegalReportParam;
import com.adai.gkd.bean.request.IdCardInfoBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.gpsvideo.GpsInfoBean;
import com.adai.gkdnavi.utils.GpsUtil;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UISwitchButton;
import com.adai.gkdnavi.utils.UIUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.adai.gkdnavi.widget.RichRadioButton;
import com.alibaba.sdk.android.common.utils.FileTypeUtil;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.example.ipcamera.application.VLCApplication;
import com.ijk.media.widget.media.IjkVideoView;
import com.oss.bean.OssUploadParam;
import com.oss.utils.OSSRequestUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class IllegalReportActivity extends BaseActivity implements View.OnClickListener, OnGetGeoCoderResultListener {
    private static final int REQUEST_CODE_VIDEO_FRAME0 = 10;
    private static final int REQUEST_CODE_VIDEO_FRAME1 = 12;
    private static final int REQUEST_CODE_VIDEO_FRAME2 = 14;
    private static final int REQUEST_CODE_LOCATION = 2;
    private static final int REQUEST_LOGIN_CODE = 4;
    private static final int REQUEST_CODE_BANK_INFO = 6;
    private static final int REQUEST_CODE_ILLEGAL_TYPE = 8;
    private static final int PLACE_PICKER_REQUEST = 16;
    private String video_path;
    private String video_logo_path;
    private int video_time;

    private List<Bitmap> mBitmaps = new ArrayList<>();
    private FFmpegMediaMetadataRetriever mFmmr;
    private static final int MAX_FRAMES = 3;
    private ImageView image0, image1, image2;
    private ArrayList<LocalFile> mLocalFiles = new ArrayList<>();
    private LinearLayout mActivityIllegalReport;
    private FrameLayout mFlVideoFrame;
    private IjkVideoView mVideoView;
    private TextView mVideoTime;
    private ImageView mVideoPlay;
    private EditText mTvIllegalTime;
    private LinearLayout mLlIllegalLocation;
    private TextView mTvLocation;
    private RadioGroup mRgCarType;
    private RichRadioButton mRbSmallCar;
    private RichRadioButton mRbLorry;
    private EditText mEtCarLicense;
    private EditText mShareText;
    private EditText mEtContactName;
    private EditText mEtPhoneNumber;
    private LinearLayout mLlHasBankInfo;
    private UISwitchButton mCbShowBankCard;
    private LinearLayout mLlBankInfo;
    private TextView mTvBankName;
    private TextView mTvBankOwnerName;
    private ImageView btn_right;
    private LocationBean mLocationBean;
    private TextView mBtnEditBankInfo;
    private TextView mTvBankIdCard, mTvBankCardNumber, mTvNoBankCardInfo;
    private IdCardInfoBean.IdCardInfoData mIdCardInfoData;
    private TextView mEtIllegalType, mTvOutTime;
    private IllegalTypeBean.DataBean.ItemsBean mIllegalTypeBean;
    private int mEncryptType;
    private ArrayList<GpsInfoBean> mGpsInfos;
    private GeoCoder mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_illegal_report);
        init();
        initView();
        initEvent();
        bindView();
        getDataFromServer();
    }


    @Override
    protected void init() {
        super.init();
        if (CurrentUserInfo.access_token == null) {
            CurrentUserInfo.initUserinfo(mContext);
        }
        Intent data = getIntent();
        video_path = data.getStringExtra("video_path");
        video_logo_path = data.getStringExtra("video_logo_path");
        video_time = data.getIntExtra("video_time", 0);
        mEncryptType = getIntent().getIntExtra("encryptType", 0);
        mGpsInfos = (ArrayList<GpsInfoBean>) getIntent().getSerializableExtra("gpsInfos");
        for (int i = 0; i < 3; i++) {
            LocalFile localFile = new LocalFile("illegalPicture" + i + ".png", VLCApplication.TEMP_PATH + "/illegalPicture" + i + ".png", "", "");
            mLocalFiles.add(localFile);
        }
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.illegal_report);
        showpDialog();
        mTvOutTime = (TextView) findViewById(R.id.tv_out_time);
        mEtIllegalType = (TextView) findViewById(R.id.et_illegal_type);
        mTvNoBankCardInfo = (TextView) findViewById(R.id.tv_no_bank_card_info);
        btn_right = (ImageView) findViewById(R.id.right_img);
        btn_right.setVisibility(View.VISIBLE);
        btn_right.setImageResource(R.drawable.icon_send);
        mActivityIllegalReport = (LinearLayout) findViewById(R.id.activity_illegal_report);
        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoTime = (TextView) findViewById(R.id.video_time);
        mVideoPlay = (ImageView) findViewById(R.id.video_play);
        image0 = (ImageView) findViewById(R.id.iv_image0);
        image1 = (ImageView) findViewById(R.id.iv_image1);
        image2 = (ImageView) findViewById(R.id.iv_image2);
        mTvIllegalTime = (EditText) findViewById(R.id.tv_illegal_time);
        mLlIllegalLocation = (LinearLayout) findViewById(R.id.ll_illegal_location);
        mTvLocation = (TextView) findViewById(R.id.tv_location);
        mRgCarType = (RadioGroup) findViewById(R.id.rg_car_type);
        mRbSmallCar = (RichRadioButton) findViewById(R.id.rb_small_car);
        mRbLorry = (RichRadioButton) findViewById(R.id.rb_lorry);
        mEtCarLicense = (EditText) findViewById(R.id.et_car_license);
        mShareText = (EditText) findViewById(R.id.share_text);
        mEtContactName = (EditText) findViewById(R.id.et_contact_name);
        mEtPhoneNumber = (EditText) findViewById(R.id.et_phone_number);
        mLlHasBankInfo = (LinearLayout) findViewById(R.id.ll_has_bank_info);
        mCbShowBankCard = (UISwitchButton) findViewById(R.id.cb_show_bank_card);
        mLlBankInfo = (LinearLayout) findViewById(R.id.ll_bank_info);
        mTvBankOwnerName = (TextView) findViewById(R.id.tv_bank_owner_name);
        mTvBankName = (TextView) findViewById(R.id.tv_bank_name);
        mBtnEditBankInfo = (TextView) findViewById(R.id.btn_edit_bank_card_info);
        mTvBankIdCard = (TextView) findViewById(R.id.tv_bank_id_card);
        mTvBankCardNumber = (TextView) findViewById(R.id.tv_bank_card_number);
    }

    private void initEvent() {
        mVideoPlay.setOnClickListener(this);
        findViewById(R.id.tv_change_picture0).setOnClickListener(this);
        findViewById(R.id.tv_change_picture1).setOnClickListener(this);
        findViewById(R.id.tv_change_picture2).setOnClickListener(this);
        findViewById(R.id.fl_video_frame).setOnClickListener(this);
        findViewById(R.id.rl_illegal_type).setOnClickListener(this);
        image0.setOnClickListener(this);
        image1.setOnClickListener(this);
        image2.setOnClickListener(this);
//        mLlIllegalLocation.setOnClickListener(this);
        mBtnEditBankInfo.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        mCbShowBankCard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mLlBankInfo.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void bindView() {
        mVideoView.setOnPreparedListener(preparedListener);
        mVideoView.setVideoPath(video_path);
        mVideoView.start();//为了一开始显示第一帧图
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
        try {
//            SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
//            format.applyPattern("yyyy_MMdd_HHmmss_SSS");
//            String substring = video_path.substring(video_path.lastIndexOf("/") + 1).substring(0, "yyyy_MMdd_HHmmss_SSS".length());
//            Date parseData = format.parse(substring);
//            SimpleDateFormat format1 = (SimpleDateFormat) SimpleDateFormat.getInstance();
//            format1.applyPattern("yyyy-MM-dd HH:mm:ss");
//            String illegalTime = format1.format(parseData);
//            mTvIllegalTime.setText(illegalTime);
//            Date date = new Date();
//            if (date.getTime() - parseData.getTime() > 3 * 24 * 3600 * 1000) {
//                //超过两天
//                mTvOutTime.setVisibility(View.VISIBLE);
//            }
            if (mGpsInfos != null && mGpsInfos.size() > 0) {
                GpsInfoBean gpsInfoBean = mGpsInfos.get(0);
                double[] doubles = GpsUtil.gps84_To_bd09(gpsInfoBean.latitude, gpsInfoBean.longitude);
                LatLng latLng = new LatLng(doubles[0], doubles[1]);
                mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latLng));
                mTvIllegalTime.setText(mGpsInfos.get(0).time);
                SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
                format.applyPattern("yyyy/MM/dd HH:mm:ss");
                Date illegalDate = format.parse(mGpsInfos.get(0).time);
                Date curDate = new Date();
                if (curDate.getTime() - illegalDate.getTime() > 3 * 24 * 3600 * 1000) {
                    mTvOutTime.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDataFromServer() {
        RequestMethods_square.getIdCardInfo(new HttpUtil.Callback<IdCardInfoBean>() {
            @Override
            public void onCallback(IdCardInfoBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (result.data != null) {
                                mIdCardInfoData = result.data;
                                mTvBankOwnerName.setText(result.data.the_name);
                                mTvBankIdCard.setText(result.data.identity_card);
                                mTvBankCardNumber.setText(result.data.bank_card_number);
                                mTvBankName.setText(result.data.bank_card_type);
                                mTvNoBankCardInfo.setVisibility(View.GONE);
                                mCbShowBankCard.setVisibility(View.VISIBLE);
                            } else {
                                mTvNoBankCardInfo.setVisibility(View.VISIBLE);
                                mCbShowBankCard.setVisibility(View.GONE);
                            }
                            break;
                        default:
                            if (!TextUtils.isEmpty(result.message))
                                showToast(result.message);
                            break;
                    }
                }
            }
        });
    }

    private IMediaPlayer.OnPreparedListener preparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            long duration = iMediaPlayer.getDuration();
            Log.e(_TAG_, "duration=" + duration);
            initFmmr(duration);
            mVideoTime.setText(String.format("%02d:%02d", duration / 60000, (duration / 1000) % 60));
        }
    };

    private void initFmmr(long duration) {
        if (mFmmr == null) {
            mFmmr = new FFmpegMediaMetadataRetriever();
        }
        mFmmr.setDataSource(video_path);
        mBitmaps.clear();
        getBitmaps(duration);
    }

    private void getBitmaps(final long duration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long sub = duration / MAX_FRAMES;
                for (int i = 0; i < 3; i++) {
                    int time = (int) (i * sub);
                    Bitmap bitmap = mFmmr.getScaledFrameAtTime(time * 1000, 640, 360);
                    if (bitmap != null) {
                        mBitmaps.add(bitmap);
                        try {
                            final String path = VLCApplication.TEMP_PATH + "/illegalPicture" + i + ".png";
                            FileOutputStream fos = new FileOutputStream(path);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                            fos.close();
                            final int finalI = i;
                            UIUtils.post(new Runnable() {
                                @Override
                                public void run() {
//                                    Glide.get(IllegalReportActivity.this).clearMemory();
                                    ImageLoaderUtil.getInstance().loadImageWithoutCache(IllegalReportActivity.this, path, finalI == 0 ? image0 : finalI == 1 ? image1 : image2);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
                UIUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        hidepDialog();
                    }
                });
                releaseMmr();
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
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.video_play:
                mVideoView.start();
                mVideoPlay.setVisibility(View.GONE);
                break;
            case R.id.fl_video_frame:
                mVideoView.pause();
                mVideoPlay.setVisibility(View.VISIBLE);
                break;
            case R.id.tv_change_picture0:
                intent.setClass(this, GetVideoFrameActivity.class);
                intent.putExtra("imagePath", mLocalFiles.get(0).getPath());
                intent.putExtra("video_path", video_path);
                startActivityForResult(intent, REQUEST_CODE_VIDEO_FRAME0);
                break;
            case R.id.tv_change_picture1:
                intent.setClass(this, GetVideoFrameActivity.class);
                intent.putExtra("imagePath", mLocalFiles.get(1).getPath());
                intent.putExtra("video_path", video_path);
                startActivityForResult(intent, REQUEST_CODE_VIDEO_FRAME1);
                break;
            case R.id.tv_change_picture2:
                intent.setClass(this, GetVideoFrameActivity.class);
                intent.putExtra("imagePath", mLocalFiles.get(2).getPath());
                intent.putExtra("video_path", video_path);
                startActivityForResult(intent, REQUEST_CODE_VIDEO_FRAME2);
                break;
            case R.id.iv_image0:
                intent.setClass(this, LocalPhotoPreviewActivity.class);
                bundle.putString("path", mLocalFiles.get(0).getPath());
                bundle.putSerializable("photos", mLocalFiles);
                bundle.putInt("position", 0);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.iv_image1:
                intent.setClass(this, LocalPhotoPreviewActivity.class);
                bundle.putString("path", mLocalFiles.get(1).getPath());
                bundle.putSerializable("photos", mLocalFiles);
                bundle.putInt("position", 1);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.iv_image2:
                intent.setClass(this, LocalPhotoPreviewActivity.class);
                bundle.putString("path", mLocalFiles.get(2).getPath());
                bundle.putSerializable("photos", mLocalFiles);
                bundle.putInt("position", 2);
                intent.putExtras(bundle);
                startActivity(intent);
                break;
            case R.id.ll_illegal_location:
                startActivityForResult(new Intent(this, ShareLocationActivity.class), REQUEST_CODE_LOCATION);
                break;
            case R.id.right_img:
                checkPostData();
                break;
            case R.id.btn_edit_bank_card_info:
                intent.setClass(this, EditBankInfoActivity.class);
                intent.putExtra("idCardData", mIdCardInfoData);
                startActivityForResult(intent, REQUEST_CODE_BANK_INFO);
                break;
            case R.id.rl_illegal_type:
                intent.setClass(this, IllegalTypeActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ILLEGAL_TYPE);
                break;
        }
    }

    private void askCommit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.commit_need_net);

        builder.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                btn_right.setClickable(false);
                postIllegalInfo();

            }
        });
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {

            }
        });

        builder.create().show(); //	Diglog的显示
    }

    private void postIllegalInfo() {
        showHpDialog(getString(R.string.committing));
        setDialogMax(100);
        final IllegalReportParam illegalReportParam = new IllegalReportParam();
        illegalReportParam.province = mLocationBean.province;
        illegalReportParam.city = mLocationBean.city;
        illegalReportParam.illegal_address = mLocationBean.address;
        illegalReportParam.des = mShareText.getText().toString();
        illegalReportParam.illegal_date = mTvIllegalTime.getText().toString();
        illegalReportParam.latitude = String.valueOf(mLocationBean.lat);
        illegalReportParam.longitude = String.valueOf(mLocationBean.lng);
        illegalReportParam.plate_number = mEtCarLicense.getText().toString();
        illegalReportParam.illegal_type = mIllegalTypeBean.violation_code;
        illegalReportParam.report_person = mEtContactName.getText().toString();
        illegalReportParam.videoTime = mVideoView.getDuration() / 1000;
//        illegalReportParam.car_type = mRgCarType.getCheckedRadioButtonId() == R.id.rb_lorry ? "c02" : "c01";
        List<OssUploadParam> ossUploadParams = new ArrayList<>();
        OssUploadParam picture0Param = new OssUploadParam(VLCApplication.TEMP_PATH + "/illegalPicture0.png");
        picture0Param.fileSize = new File(VLCApplication.TEMP_PATH + "/illegalPicture0.png").length();
        picture0Param.setFileType(FileTypeUtil.TYPE_RP_PIC);
        ossUploadParams.add(picture0Param);
        OssUploadParam picture1Param = new OssUploadParam(VLCApplication.TEMP_PATH + "/illegalPicture1.png");
        picture1Param.fileSize = new File(VLCApplication.TEMP_PATH + "/illegalPicture1.png").length();
        picture1Param.setFileType(FileTypeUtil.TYPE_RP_PIC);
        ossUploadParams.add(picture1Param);
        OssUploadParam picture2Param = new OssUploadParam(VLCApplication.TEMP_PATH + "/illegalPicture2.png");
        picture2Param.setFileType(FileTypeUtil.TYPE_RP_PIC);
        picture2Param.fileSize = new File(VLCApplication.TEMP_PATH + "/illegalPicture2.png").length();
        ossUploadParams.add(picture2Param);
        OssUploadParam videoParam = new OssUploadParam(video_path);
        videoParam.setFileType(FileTypeUtil.TYPE_RP_MOVIE);
        videoParam.fileSize = new File(video_path).length();
        ossUploadParams.add(videoParam);
        illegalReportParam.videoName = videoParam.partObject;
        List<String> photos = new ArrayList<>();
        photos.add(picture0Param.partObject);
        photos.add(picture1Param.partObject);
        photos.add(picture2Param.partObject);
        for (String photo : photos) {
            String[] splits = photo.split("/");
            String tempString = splits[splits.length - 2] + "/" + splits[splits.length - 1];
            illegalReportParam.pictureName += tempString + ";";
        }
        OSSRequestUtil.getInstance().postFile2OSS(ossUploadParams, new OSSRequestUtil.MultiUploadCallBack() {
            @Override
            public void onUploadComplete(List<String> objectKey) {
                RequestMethods_square.postIllegalReportInfo(illegalReportParam, new HttpUtil.Callback<BasePageBean>() {
                    @Override
                    public void onCallback(BasePageBean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    showToast(R.string.commit_succeeded);
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    }, 500);
                                    break;
                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        btn_right.setClickable(true);
                        hideHpDialog();
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
                btn_right.setClickable(true);
                showToast(R.string.upload_failed);
            }
        });
    }

    private void checkPostData() {
        if (TextUtils.isEmpty(mTvIllegalTime.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_time_violation));
            return;
        }
        if (TextUtils.isEmpty(mTvLocation.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.choose_location_violation));
            return;
        }
        if (TextUtils.isEmpty(mEtCarLicense.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_license_plate_number));
            return;
        }
        if (TextUtils.isEmpty(mEtContactName.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.please_input_name));
            return;
        }
        if (TextUtils.isEmpty(mEtPhoneNumber.getText().toString())) {
            ToastUtil.showShortToast(this, getString(R.string.enter_your_mobile_phone));
            return;
        }
        if (VoiceManager.isLogin) {
            askCommit();
        } else {
            Intent login = new Intent(mContext, LoginActivity.class);
            startActivityForResult(login, REQUEST_LOGIN_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case REQUEST_CODE_VIDEO_FRAME0:
//                Glide.get(IllegalReportActivity.this).clearMemory();
                ImageLoaderUtil.getInstance().loadImageWithoutCache(IllegalReportActivity.this, VLCApplication.TEMP_PATH + "/illegalPicture0.png", image0);
                break;
            case REQUEST_CODE_VIDEO_FRAME1:
                ImageLoaderUtil.getInstance().loadImageWithoutCache(IllegalReportActivity.this, VLCApplication.TEMP_PATH + "/illegalPicture1.png", image1);
                break;
            case REQUEST_CODE_VIDEO_FRAME2:
                ImageLoaderUtil.getInstance().loadImageWithoutCache(IllegalReportActivity.this, VLCApplication.TEMP_PATH + "/illegalPicture2.png", image2);
                break;
            case REQUEST_CODE_LOCATION:
                //返回的地址
//                if (data != null) {
//                    mLocationBean = (LocationBean) data.getSerializableExtra("location");
//                    double[] latLng = GpsUtil.bd09_To_Gcj02(mLocationBean.lat, mLocationBean.lng);
//                    mLocationBean.lat = latLng[0];
//                    mLocationBean.lng = latLng[1];
//                    mTvLocation.setText(mLocationBean.name);
//                    if (mLlHasBankInfo.getVisibility() != View.VISIBLE) {
//                        mLlHasBankInfo.setVisibility(View.VISIBLE);
//                    }
//                }
                break;
//            case REQUEST_LOGIN_CODE:
//                if (data.getBooleanExtra("islogin", false)) {
//                    checkPostData();
//                }
//                break;
            case REQUEST_CODE_BANK_INFO:
                if (data != null) {
                    mIdCardInfoData = (IdCardInfoBean.IdCardInfoData) data.getSerializableExtra("idCardInfo");
                    mTvBankOwnerName.setText(mIdCardInfoData.the_name);
                    mTvBankIdCard.setText(mIdCardInfoData.identity_card);
                    mTvBankCardNumber.setText(mIdCardInfoData.bank_card_number);
                    mTvBankName.setText(mIdCardInfoData.bank_card_type);
                    mTvNoBankCardInfo.setVisibility(View.GONE);
                    mCbShowBankCard.setVisibility(View.VISIBLE);
                }
                break;
            case REQUEST_CODE_ILLEGAL_TYPE:
                if (data != null) {
                    mIllegalTypeBean = (IllegalTypeBean.DataBean.ItemsBean) data.getSerializableExtra("selectedType");
                    mEtIllegalType.setText(mIllegalTypeBean.violation_name);
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        mVideoView.seekTo(0);
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
            mVideoPlay.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mVideoView != null) {
            mVideoView.seekTo(0);
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

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult.error != ReverseGeoCodeResult.ERRORNO.NO_ERROR) {
            return;
        }
        String address = reverseGeoCodeResult.getAddress();
        ReverseGeoCodeResult.AddressComponent addressDetail = reverseGeoCodeResult.getAddressDetail();
        if (mLocationBean == null) {
            mLocationBean = new LocationBean();
        }
        mLocationBean.lat = mGpsInfos.get(0).latitude;
        mLocationBean.lng = mGpsInfos.get(0).longitude;
        mLocationBean.address = address;
        mLocationBean.city = addressDetail.city;
        mLocationBean.district = addressDetail.street;
        mLocationBean.province = addressDetail.province;
        mLocationBean.name = address;
        mTvLocation.setText(address);
    }
}
