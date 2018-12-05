package com.adai.gkdnavi;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.example.photoviewer.PhotoView;

import java.util.ArrayList;
import java.util.List;

public class LocalPhotoPreviewActivity extends BaseActivity {
    private static final String fileEndingPhoto = "JPG";
    private static final String TAG = "LocalPhotoPreviewActivity";
    //    private static ArrayList<String> imgsId = null;
    private ViewPager mPager;
    private int position;
    private String path;
    private List<LocalFile> list = new ArrayList<>();
    private View mHeadView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_view_pager);

        initView();
        initializeByIntent();
    }

    @Override
    protected void initView() {
        super.initView();
        mHeadView = findViewById(R.id.layout_head);
    }

    private void toggleHead() {
        mHeadView.setVisibility(mHeadView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void initializeByIntent() {
        Bundle bundle = this.getIntent().getExtras();
        position = bundle.getInt("position");
        path = bundle.getString("path");
        ArrayList<LocalFile> listTemp = (ArrayList<LocalFile>) bundle.getSerializable("photos");
        if (listTemp != null && listTemp.size() > 0) {
            list.clear();
            list.addAll(listTemp);
        }
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
        mPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                PhotoView view = new PhotoView(LocalPhotoPreviewActivity.this);
                view.enable();
//                view.setImageResource(list.get(position).getPath());
//                setTitle(list.get(position).getName());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        toggleHead();
                    }
                });
                ImageLoaderUtil.getInstance().loadImageWithoutCache(LocalPhotoPreviewActivity.this, list.get(position).getPath(), R.drawable.showphoto, view);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(ViewGroup container, int position,
                                    Object object) {
                container.removeView((View) object);
            }
        });

        mPager.setCurrentItem(position);
        setTitle(list.get(position).getName());
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setTitle(list.get(position).getName());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

}