package com.adai.camera.sunplus.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.filepicker.imagebrowse.HackyViewPager;
import com.filepicker.views.SmoothCheckBox;
import com.icatch.wificam.customer.type.ICatchFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class SunplusPictureBrowseActivity extends BaseActivity implements View.OnClickListener {
    private View frame;
    private View left_back;
    private TextView title, right_text;
    private ImageView btn_right;
    private View main_view;
    HackyViewPager pager;
    public static final String KEY_MODE = "key_mode";
    public static final String KEY_POSTION = "key_postion";
    public static final String KEY_TOTAL_LIST = "total_list";
    public static final String KEY_SELECT_LIST = "select_list";
    private SmoothCheckBox checkBox;
    private ProgressBar downloading_progress;
    private int initDataSize;
    private ImagePagerAdapter mImagePagerAdapter;
    private int mPosition;
    private boolean isDownloaded = false;
    private String downloadingPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_sunplus_picture_browse);
        mPosition = getIntent().getIntExtra(KEY_POSTION, 0);
        initView();
        init();
    }

    @Override
    public void initView() {
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

    @Override
    public void init() {
        initDataSize = GlobalInfo.previewFileList.size();
        mImagePagerAdapter = new ImagePagerAdapter(getSupportFragmentManager(), GlobalInfo.previewFileList);
        pager.setAdapter(mImagePagerAdapter);
        pager.setOnClickListener(this);
        left_back.setOnClickListener(this);
        btn_right.setOnClickListener(this);
        main_view.setOnClickListener(this);
        right_text.setOnClickListener(this);
        right_text.setVisibility(View.GONE);
        btn_right.setVisibility(View.GONE);
        checkBox.setVisibility(View.GONE);
        btn_right.setBackgroundResource(R.drawable.bg_download_selector);
        btn_right.setVisibility(View.VISIBLE);
        isDownloaded = false;
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (downloadingPath != null && downloadingPath.equals(GlobalInfo.previewFileList.get(position).getFileName())) {
                    downloading_progress.setVisibility(View.VISIBLE);
                } else {
                    downloading_progress.setVisibility(View.INVISIBLE);
                }
                ICatchFile iCatchFile = GlobalInfo.previewFileList.get(position);
                title.setText(iCatchFile.getFileName());
                checkDownload(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ICatchFile iCatchFile = GlobalInfo.previewFileList.get(mPosition);
        title.setText(iCatchFile.getFileName());
        pager.setCurrentItem(mPosition);
        checkDownload(mPosition);

    }

    private void checkDownload(int position) {
        String currentPath = GlobalInfo.previewFileList.get(position).getFileName();
        String localpath = NetworkDownloadUtils.getLocalPath(currentPath);
        if (localpath != null) {
            File file = new File(localpath);
            if (file.exists()) {
                btn_right.setBackgroundResource(R.drawable.bg_share_normal);
                btn_right.setVisibility(View.GONE);
                right_text.setVisibility(View.VISIBLE);
                isDownloaded = true;
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
                downloadFile(GlobalInfo.previewFileList.get(pager.getCurrentItem()));
                break;
            case R.id.checkbox:
                break;
            case R.id.right_text:
//                ArrayList<Uri> photos = new ArrayList<>();
                ArrayList<String> paths = new ArrayList<>();
                String name = GlobalInfo.previewFileList.get(pager.getCurrentItem()).getFileName();
                String localpath = NetworkDownloadUtils.getLocalPath(name);
//                Uri u = Uri.fromFile(new File(localpath));
//                photos.add(u);
                paths.add(localpath);
                new ShareUtils().sharePhoto(this, paths);
                break;
        }
    }

    private void downloadFile(final ICatchFile iCatchFile) {
        if (downloadingPath != null) {
            ToastUtil.showShortToast(this, getString(R.string.downloading));
            return;
        }
        downloadingPath = iCatchFile.getFileName();
        downloading_progress.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean ret = FileOperation.getInstance().downloadFile(iCatchFile, NetworkDownloadUtils.getLocalPath(iCatchFile.getFileName()));
                if (ret) {
                    UIUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShortToast(SunplusPictureBrowseActivity.this, getString(R.string.successfully_saved_to) + NetworkDownloadUtils.getLocalPath(iCatchFile.getFileName()));
                            downloading_progress.setVisibility(View.INVISIBLE);
                            if (downloadingPath.equals(GlobalInfo.previewFileList.get(pager.getCurrentItem()).getFileName())) {
                                btn_right.setBackgroundResource(R.drawable.bg_share_normal);
                                btn_right.setVisibility(View.GONE);
                                right_text.setVisibility(View.VISIBLE);
                                isDownloaded = true;
                                //btn_right.setClickable(false);
                            }
                            downloadingPath = null;
                        }
                    });
                } else {
                    //下载失败
                    UIUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showShortToast(SunplusPictureBrowseActivity.this, getString(R.string.download_error));
                            downloading_progress.setVisibility(View.INVISIBLE);
                            downloadingPath = null;
                        }
                    });
                }
            }
        }).start();
    }

    public void toggleFrame() {
        if (frame.getVisibility() == View.VISIBLE) {
            frame.setVisibility(View.GONE);
        } else {
            frame.setVisibility(View.VISIBLE);
        }
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
        if (initDataSize != GlobalInfo.previewFileList.size() && GlobalInfo.previewFileList.size() != 0) {
            setResult(Activity.RESULT_OK);
        }
        finish();
    }

    class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private List<ICatchFile> mData;

        ImagePagerAdapter(FragmentManager fm, List<ICatchFile> sunplusFileDomains) {
            super(fm);
            mData = sunplusFileDomains;
        }

        @Override
        public Fragment getItem(int position) {
            ICatchFile iCatchFile = mData.get(position);
            SunplusImageFragment fragment = new SunplusImageFragment(iCatchFile);
            fragment.setAdapter(this);
            return fragment;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
