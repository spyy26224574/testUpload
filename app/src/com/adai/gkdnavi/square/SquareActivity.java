package com.adai.gkdnavi.square;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.square.ClassifyVideoFragment;
import com.adai.gkdnavi.fragment.square.DynamicFragment;
import com.adai.gkdnavi.fragment.square.NewestVideoFragment;
import com.filepicker.adapters.SectionsPagerAdapter;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class SquareActivity extends FragmentActivity{

    //	ClassifyVideoFragment classfyfragment;
    private android.support.v4.view.ViewPager viewpager;
    private SectionsPagerAdapter adapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle arg0) {
        // TODO Auto-generated method stub
        super.onCreate(arg0);
        setContentView(R.layout.activity_square);
        this.viewpager = (ViewPager) findViewById(R.id.viewpager);
//		classfyfragment=new ClassifyVideoFragment();
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        adapter = new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ClassifyVideoFragment(), getString(R.string.square));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.square), true);
        adapter.addFragment(new DynamicFragment(), getString(R.string.dynamic));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.dynamic));
        adapter.addFragment(new NewestVideoFragment(), getString(R.string.newest));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.newest));
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        viewpager.setAdapter(adapter);
//		viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//			@Override
//			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//			}
//
//			@Override
//			public void onPageSelected(int position) {
//				changeTitle(position);
//			}
//
//			@Override
//			public void onPageScrollStateChanged(int state) {
//
//			}
//		});
        viewpager.setOffscreenPageLimit(3);
//		FragmentManager fragmentmanager = getSupportFragmentManager();
//		fragmentmanager.beginTransaction().replace(R.id.content, classfyfragment).commit();
//		titlenewest.setOnClickListener(this);
//		titledynamic.setOnClickListener(this);
//		titlesquare.setOnClickListener(this);
        // updateSize player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");
//		changeTitle(0);
    }


}
