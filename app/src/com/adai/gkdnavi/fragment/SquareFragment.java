package com.adai.gkdnavi.fragment;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.camera.CameraConstant;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.square.ClassifyVideoFragment;
import com.adai.gkdnavi.fragment.square.NewestVideoFragment;
import com.adai.gkdnavi.fragment.square.TypeVideoRecyclerViewAdapter;
import com.adai.gkdnavi.utils.SpUtils;
import com.filepicker.adapters.SectionsPagerAdapter;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/10/26 17:08
 * @updateAuthor $Author$
 * @updateDate $Date$
 */
public class SquareFragment extends BaseFragment {
    private android.support.v4.view.ViewPager viewpager;
    private SectionsPagerAdapter adapter;
    private TabLayout tabLayout;
    private ClassifyVideoFragment mClassifyVideoFragment;
    //    private DynamicFragment mDynamicFragment;
    private NewestVideoFragment mNewestVideoFragment;
    private String mNews;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_square, container, false);
        viewpager = (ViewPager) rootView.findViewById(R.id.viewpager);
        tabLayout = (TabLayout) rootView.findViewById(R.id.tabs);

        adapter = new SectionsPagerAdapter(getChildFragmentManager());
        if (mClassifyVideoFragment == null) {
            mClassifyVideoFragment = new ClassifyVideoFragment();
        }
        adapter.addFragment(mClassifyVideoFragment, getString(R.string.recommend));
//        if (mDynamicFragment == null)
//            mDynamicFragment = new DynamicFragment();
//        adapter.addFragment(mDynamicFragment, getString(R.string.dynamic));
        if (mNewestVideoFragment == null) {
            mNewestVideoFragment = new NewestVideoFragment();
        }
//        String news= PreferenceManager.getDefaultSharedPreferences(getContext()).getString("factory_name",null);
        String news = SpUtils.getString(mContext, CameraConstant.CAMERA_FACTORY, "");

        if (TextUtils.isEmpty(mNews)) {
            mNews = getString(R.string.newest);
        }
        adapter.addFragment(mNewestVideoFragment, mNews);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.square), true);
//        tabLayout.addTab(tabLayout.newTab().setText(R.string.dynamic));

        tabLayout.addTab(tabLayout.newTab().setText(mNews));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewpager.setOffscreenPageLimit(1);
        viewpager.setAdapter(adapter);
//        tabLayout.setupWithViewPager(viewpager);

        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        return rootView;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            if (mNewestVideoFragment != null) {
                TypeVideoRecyclerViewAdapter adapter = mNewestVideoFragment.getAdapter();
                if (adapter != null) {
                    adapter.onPause();
                }
            }
//            if (mDynamicFragment != null) {
//                DynamicRecyclerViewAdapter dynamicRecyclerViewAdapter = mDynamicFragment.getAdapter();
//                if (dynamicRecyclerViewAdapter != null) {
//                    dynamicRecyclerViewAdapter.onPause();
//                }
//            }
        }
    }

    public void loadFirstPage() {
        try {
            String news = SpUtils.getString(mContext, CameraConstant.CAMERA_FACTORY, "");
            if (TextUtils.isEmpty(news)) {
                news = getString(R.string.newest);
            }
            if (!news.equals(mNews)) {
                mNews = news;
                if (tabLayout != null) {
                    TabLayout.Tab tabAt = tabLayout.getTabAt(1);
                    if (tabAt != null) {
                        tabAt.setText(mNews);
                    }
                    if (mNewestVideoFragment != null) {
                        mNewestVideoFragment.reFactoryData();
                    }
                }
            }
        } catch (Exception ignored) {

        }
        if (mClassifyVideoFragment != null) {
            mClassifyVideoFragment.firstLoad();
        }
    }

}