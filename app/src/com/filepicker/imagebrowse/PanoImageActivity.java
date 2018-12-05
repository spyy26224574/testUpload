package com.filepicker.imagebrowse;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.views.SmoothCheckBox;
import com.ligo.medialib.PanoImageView;
import com.ligo.medialib.opengl.ImageRender;

import java.io.File;
import java.util.ArrayList;

public class PanoImageActivity extends BaseActivity implements View.OnClickListener {

    public static final int MODE_LOCAL = 3;
    public static final int MODE_NETWORK = 0;
    private TextView title;
    private ImageView btn_right;
    private TextView right_text;
    private SmoothCheckBox checkBox;
    private ProgressBar downloading_progress;
    private PanoImageView mPanoImageView;
    private ImageView iv_original, iv_front_back, iv_four_direct, iv_wide_single, iv_cylinder;
    private String mPath;
    private int mType;

    public static void newInstance(Context context, String path, int type) {
        Intent intent = new Intent(context, PanoImageActivity.class);
        intent.putExtra("path", path);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pano_image);
        initView();
        initEvent();
    }

    private void initEvent() {
        iv_original.setOnClickListener(this);
        iv_front_back.setOnClickListener(this);
        iv_four_direct.setOnClickListener(this);
        iv_wide_single.setOnClickListener(this);
        iv_cylinder.setOnClickListener(this);
        right_text.setOnClickListener(this);
        btn_right.setOnClickListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        title = (TextView) findViewById(R.id.title);
        btn_right = (ImageView) findViewById(R.id.right_img);
        right_text = (TextView) findViewById(R.id.right_text);
        checkBox = (SmoothCheckBox) findViewById(R.id.checkbox);
        downloading_progress = (ProgressBar) findViewById(R.id.downloading_progress);
        mPanoImageView = (PanoImageView) findViewById(R.id.piv);
        iv_original = (ImageView) findViewById(R.id.iv_original);
        iv_front_back = (ImageView) findViewById(R.id.iv_front_back);
        iv_four_direct = (ImageView) findViewById(R.id.iv_four_direct);
        iv_wide_single = (ImageView) findViewById(R.id.iv_wide_single);
        iv_cylinder = (ImageView) findViewById(R.id.iv_cylinder);
        mPath = getIntent().getStringExtra("path");
        mType = getIntent().getIntExtra("type", 3);
        Glide.with(this).load(mPath).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                mPanoImageView.setBitmap(resource);
                setPanoType(ImageRender.TYPE_CIRCLE);
            }
        });
        String fileName = mPath.substring(mPath.lastIndexOf("/") + 1);
        title.setText(fileName);
        checkDownload(mPath);
        if (mType != 3) {
            right_text.setVisibility(View.GONE);
        }
    }

    private void checkDownload(String path) {
        String localpath = NetworkDownloadUtils.getLocalPath(path);
        if (localpath != null) {
            File file = new File(localpath);
            if (file.exists()) {
                btn_right.setVisibility(View.GONE);
                right_text.setVisibility(View.VISIBLE);
            } else {
                btn_right.setVisibility(View.VISIBLE);
                right_text.setVisibility(View.GONE);
            }
        }
        btn_right.setBackgroundResource(R.drawable.bg_download_selector);
        btn_right.setClickable(true);

    }

    public void setPanoType(int panoType) {
        mPanoImageView.onChangeShowType(panoType);
        iv_original.setSelected(panoType == ImageRender.TYPE_CIRCLE);
        iv_front_back.setSelected(panoType == ImageRender.TYPE_2_SCREEN);
        iv_four_direct.setSelected(panoType == ImageRender.TYPE_4_SCREEN);
        iv_wide_single.setSelected(panoType == ImageRender.TYPE_HEMISPHERE);
        iv_cylinder.setSelected(panoType == ImageRender.TYPE_CYLINDER);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_original:
                setPanoType(ImageRender.TYPE_CIRCLE);
                break;
            case R.id.iv_front_back:
                setPanoType(ImageRender.TYPE_2_SCREEN);
                break;
            case R.id.iv_four_direct:
                setPanoType(ImageRender.TYPE_4_SCREEN);
                break;
            case R.id.iv_wide_single:
                setPanoType(ImageRender.TYPE_HEMISPHERE);
                break;
            case R.id.iv_cylinder:
                setPanoType(ImageRender.TYPE_CYLINDER);
                break;
            case R.id.right_text:
//                ArrayList<Uri> photos = new ArrayList<>();
                String localpath = NetworkDownloadUtils.getLocalPath(mPath);
//                if (localpath != null) {
//                    File file = new File(localpath);
//                    if (file.exists()) {
//                        photos.add(Uri.fromFile(file));
//                    }
//                }
                ArrayList<String> paths = new ArrayList<>();
                if (localpath != null) {
                    File file = new File(localpath);
                    if (file.exists()) {
                        paths.add(localpath);
                    }
                }
//                if (photos.size() > 0)
                if (paths.size() > 0) {
                    new ShareUtils().sharePhoto(this, paths);
                }
                break;
            case R.id.right_img:
                downloadFile(mPath);
                break;
        }
    }

    private String downloadingPath = null;

    private void downloadFile(String path) {
        if (downloadingPath != null) {
            ToastUtil.showShortToast(this, getString(R.string.downloading));
            return;
        }
        downloadingPath = path;
//        String[] strs = path.split("/");
//        String localpath= VLCApplication.DOWNLOADPATH+"/"+strs[strs.length-1];
        downloading_progress.setVisibility(View.VISIBLE);
        NetworkDownloadUtils.downloadFile(path, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                ToastUtil.showShortToast(PanoImageActivity.this, getString(R.string.successfully_saved_to) + path);
                downloading_progress.setVisibility(View.INVISIBLE);
                btn_right.setBackgroundResource(R.drawable.bg_download_complete);
                btn_right.setClickable(false);
//                right_text.setVisibility(View.VISIBLE);
                downloadingPath = null;

                File file = new File(path);
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
                intent.putExtra("isVideo", false);
                VLCApplication.getAppContext().sendBroadcast(intent);
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownladFail() {
                ToastUtil.showShortToast(PanoImageActivity.this, getString(R.string.download_error));
                downloading_progress.setVisibility(View.INVISIBLE);
                downloadingPath = null;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPanoImageView.release();
    }
}
