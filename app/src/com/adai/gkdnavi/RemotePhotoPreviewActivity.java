package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.utils.DownLoadInfo;
import com.adai.gkdnavi.utils.DownloadManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.example.ipcamera.domain.FileDomain;
import com.example.photoviewer.PhotoView;

import java.util.ArrayList;


public class RemotePhotoPreviewActivity extends BaseActivity {
    private ViewPager mViewPager;
    private ArrayList<FileDomain> photos;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_pager);
        initData();
        bindViews();
        bindData();
    }

    private void initData() {
        Intent intent = getIntent();
        photos = (ArrayList<FileDomain>) intent.getSerializableExtra("photos");
        mPosition = intent.getIntExtra("position", 0);
    }


    private void bindViews() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
    }

    FileDomain mFileDomain;
//    ProgressCircleView mProgressBtn;

    private void bindData() {
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return photos == null ? 0 : photos.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View) object);
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView photoView = new PhotoView(RemotePhotoPreviewActivity.this);
                photoView.setScaleType(ImageView.ScaleType.FIT_XY);
                photoView.enable();
                mFileDomain = photos.get(position);
                String url;
                DownLoadInfo downloadInfo = DownloadManager.getInstance().getDownloadInfo(mFileDomain);
                if (downloadInfo.state == DownloadManager.STATE_DOWNLOADED) {
                    url = DownloadManager.getInstance().getCachePath(downloadInfo.fileName);
                } else {
//                    url = "http://192.168.1.254/CARDV/PHOTO/" + name + "?custom=1&cmd=4001";
                    url = Contacts.BASE_HTTP_IP + mFileDomain.getFpath().substring(mFileDomain.getFpath().indexOf(":") + 1).replace("\\", "/");
                }
//                BitmapHelper.display(photoView, url);
                ImageLoaderUtil.getInstance().loadImage(RemotePhotoPreviewActivity.this, url, R.drawable.showphoto, photoView);
                container.addView(photoView);
                return photoView;
            }
        });
        mViewPager.setCurrentItem(mPosition);
    }

}
