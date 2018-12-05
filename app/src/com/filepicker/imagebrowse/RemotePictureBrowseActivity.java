package com.filepicker.imagebrowse;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.views.SmoothCheckBox;

import java.io.File;
import java.util.ArrayList;

public class RemotePictureBrowseActivity extends FragmentActivity implements View.OnClickListener {

    public static final String KEY_MODE = "key_mode";
    public static final String KEY_POSTION = "key_postion";
    public static final String KEY_TOTAL_LIST = "total_list";
    public static final String KEY_SELECT_LIST = "select_list";
    public static final int MODE_NOMAL = 0;
    public static final int MODE_SELECT = 1;
    public static final int MODE_NETWORK = 2;
    public static final int MODE_LOCAL = 3;
    ArrayList<String> selectedlist = new ArrayList<>();
    ArrayList<String> allList = new ArrayList<>();
    ImageViewPagerAdapter adapter;
    HackyViewPager pager;
    private int currentMode = 0;
    private int mPosition = 0;

    private View frame;
    private View left_back;
    private TextView title, right_text;
    private ImageView btn_right;
    private View main_view;

    private SmoothCheckBox checkBox;
    private ProgressBar downloading_progress;
    private boolean isDownloaded = false;
    private int initDataSize;
    private boolean downloadFile = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_picture_browse);
        Log.e("9527", "RemotePictureBrowseActivity");
        initview();
        init();
    }

    public void toggleFrame() {
        if (frame.getVisibility() == View.VISIBLE) {
            frame.setVisibility(View.GONE);
        } else {
            frame.setVisibility(View.VISIBLE);
        }
    }

    private void initview() {
        pager = (HackyViewPager) findViewById(R.id.pager);
        frame = findViewById(R.id.frame);
        left_back = findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
        btn_right = (ImageView) findViewById(R.id.right_img);
        main_view = findViewById(R.id.main_view);
        checkBox = (SmoothCheckBox) findViewById(R.id.checkbox);
        downloading_progress = (ProgressBar) findViewById(R.id.downloading_progress);
        right_text = (TextView) findViewById(R.id.right_text);
    }

    private void init() {
        currentMode = getIntent().getIntExtra(KEY_MODE, 0);
        mPosition = getIntent().getIntExtra(KEY_POSTION, 0);
        allList = getIntent().getStringArrayListExtra(KEY_TOTAL_LIST);
        initDataSize = allList.size();
        adapter = new ImageViewPagerAdapter(getSupportFragmentManager(), allList);
        pager.setAdapter(adapter);
        pager.setOnClickListener(this);
        left_back.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        main_view.setOnClickListener(this);
        right_text.setOnClickListener(this);
        right_text.setVisibility(View.GONE);
        if (currentMode == MODE_LOCAL) {
            btn_right.setVisibility(View.VISIBLE);
        } else if (currentMode == MODE_NETWORK) {
            btn_right.setBackgroundResource(R.drawable.bg_download_selector);
            btn_right.setVisibility(View.VISIBLE);
            isDownloaded = false;
        } else {
            btn_right.setVisibility(View.GONE);
        }
        if (currentMode == MODE_SELECT) {
            checkBox.setVisibility(View.VISIBLE);
            selectedlist = getIntent().getStringArrayListExtra(KEY_SELECT_LIST);
            checkBox.setOnClickListener(this);
            checkBox.setChecked(selectedlist.contains(allList.get(mPosition)));
        } else {
            checkBox.setVisibility(View.GONE);
        }
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (currentMode == MODE_SELECT) {
                    checkBox.setChecked(selectedlist.contains(allList.get(position)));
                } else if (currentMode == MODE_NETWORK) {
                    if (downloadingPath != null && downloadingPath.equals(allList.get(position))) {
                        downloading_progress.setVisibility(View.VISIBLE);
                    } else {
                        downloading_progress.setVisibility(View.INVISIBLE);
                    }
                    checkDownload(position);
                }
                String fileName = allList.get(position);
                fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
                title.setText(fileName);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        String fileName = allList.get(mPosition);
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
        title.setText(fileName);
        pager.setCurrentItem(mPosition);
        checkDownload(mPosition);
    }

    private void checkDownload(int position) {
        String currentPath = allList.get(position);
        String localpath = NetworkDownloadUtils.getLocalPath(currentPath);
        if (localpath != null) {
            File file = new File(localpath);
            if (file.exists()) {
                btn_right.setBackgroundResource(R.drawable.bg_share_normal);
                btn_right.setVisibility(View.GONE);
                right_text.setVisibility(View.VISIBLE);
                isDownloaded = true;
                if (currentMode != MODE_LOCAL) {
                    right_text.setVisibility(View.GONE);
                }
                //btn_right.setClickable(false);
                return;
            }
        }
        btn_right.setBackgroundResource(R.drawable.bg_download_selector);
        btn_right.setVisibility(View.VISIBLE);
        right_text.setVisibility(View.GONE);
        isDownloaded = false;
        //btn_right.setClickable(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBack() {
        if (currentMode == MODE_SELECT) {
            Intent data = new Intent();
            data.putStringArrayListExtra(KEY_SELECT_LIST, selectedlist);
            setResult(RESULT_OK, data);
        }
        if (initDataSize != allList.size() && allList.size() != 0 || downloadFile) {
            setResult(Activity.RESULT_OK);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBack();
                break;
            case R.id.main_view:

                break;
            case R.id.pager:

                break;
            case R.id.right_img:

                if (isDownloaded) {
                    ArrayList<String> paths = new ArrayList<>();
//                    ArrayList<Uri> photos = new ArrayList<>();
                    String name = allList.get(pager.getCurrentItem());
                    String localpath = VLCApplication.DOWNLOADPATH + "/" + name.substring(name.lastIndexOf("\\") + 1);
//                    Uri u = Uri.fromFile(new File(localpath));
//                    photos.add(u);
                    paths.add(localpath);
                    new ShareUtils().sharePhoto(this, paths);

                } else {


                    if (currentMode == MODE_LOCAL) {
//                    Intent share=new Intent(Intent.ACTION_SEND);
//                    share.setType("image/*");
//                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(allList.get(pager.getCurrentItem()))));
//                    share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(share);

//                        ArrayList<Uri> photos = new ArrayList<>();
                        ArrayList<String> paths = new ArrayList<>();
                        paths.add(allList.get(pager.getCurrentItem()));
//                        photos.add(Uri.fromFile(new File(allList.get(pager.getCurrentItem()))));
                        new ShareUtils().sharePhoto(this, paths);
                    } else if (currentMode == MODE_NETWORK) {
                        downloadFile(allList.get(pager.getCurrentItem()));
                    }
                }
                break;
            case R.id.checkbox:
                int item = pager.getCurrentItem();
                if (checkBox.isChecked()) {
                    selectedlist.remove(allList.get(item));
                } else {
                    selectedlist.add(allList.get(item));
                }
                checkBox.setChecked(!checkBox.isChecked());
                break;
            case R.id.right_text:
                if (isDownloaded) {
//                    ArrayList<Uri> photos = new ArrayList<>();
                    ArrayList<String> paths = new ArrayList<>();
                    String name = allList.get(pager.getCurrentItem());
                    String localpath = VLCApplication.DOWNLOADPATH + "/" + name.substring(name.length() - 25);
                    Uri u = Uri.fromFile(new File(localpath));
                    paths.add(localpath);
//                    photos.add(u);
                    new ShareUtils().sharePhoto(this, paths);
                } else {
                    if (currentMode == MODE_LOCAL) {
//                        ArrayList<Uri> photos = new ArrayList<>();
                        ArrayList<String> paths = new ArrayList<>();
                        paths.add(allList.get(pager.getCurrentItem()));
//                        photos.add(Uri.fromFile(new File(allList.get(pager.getCurrentItem()))));
                        new ShareUtils().sharePhoto(this, paths);
                    }
                }
                break;
        }
    }

    private String downloadingPath = null;

    private void downloadFile(String path) {
        if (downloadingPath != null) {
            ToastUtil.showShortToast(RemotePictureBrowseActivity.this, getString(R.string.downloading));
            return;
        }
        downloadFile = true;
        downloadingPath = path;
//        String[] strs = path.split("/");
//        String localpath= VLCApplication.DOWNLOADPATH+"/"+strs[strs.length-1];
        downloading_progress.setVisibility(View.VISIBLE);
        NetworkDownloadUtils.downloadFile(path, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                ToastUtil.showShortToast(RemotePictureBrowseActivity.this, getString(R.string.successfully_saved_to) + path);
                downloading_progress.setVisibility(View.INVISIBLE);
                if (downloadingPath.equals(allList.get(pager.getCurrentItem()))) {
                    btn_right.setBackgroundResource(R.drawable.bg_share_normal);
                    btn_right.setVisibility(View.GONE);
                    isDownloaded = true;
                    //btn_right.setClickable(false);
                }
                downloadingPath = null;
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownladFail() {
                ToastUtil.showShortToast(RemotePictureBrowseActivity.this, getString(R.string.download_error));
                downloading_progress.setVisibility(View.INVISIBLE);
                downloadingPath = null;
            }
        });
    }
}
