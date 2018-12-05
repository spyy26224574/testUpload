package com.adai.gkdnavi;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.example.ipcamera.application.VLCApplication;
import com.example.photoviewer.PhotoView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PicturePreviewActivity extends Activity {
	private static final String fileEndingPhoto = "JPG";
	private static final String TAG = "PicturePreviewActivity";
	private static ArrayList<String> imgsId = null;
	private ViewPager mPager;
	private int position;
	private String path;
	private List<LocalPhotoFile> list = new ArrayList<LocalPhotoFile>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_view_pager);

		createFolderDispList();
		initializeByIntent();

	}

	private void createFolderDispList() {
		File filePath;
		imgsId = new ArrayList<String>();
		String filefirstPathText = VLCApplication.DOWNLOADPATH;
		filePath = new File(filefirstPathText);

		File[] fileList = filePath.listFiles();
		if (fileList != null && filePath.isDirectory()) {
			for (File currenFile : fileList) {
				String fileName = currenFile.getName();
				int indexPoint = fileName.lastIndexOf('.');
				if (indexPoint > 0 && currenFile.isFile()) {
					String fileEnd = fileName.substring(indexPoint + 1);
					if (fileEnd.equalsIgnoreCase(fileEndingPhoto)) {
						imgsId.add(currenFile.getPath());
					}

				}
			}

		}

	}

	private void initializeByIntent() {
		Bundle bundle = this.getIntent().getExtras();
		position = bundle.getInt("position");
		path = bundle.getString("path");
		ArrayList<LocalPhotoFile> listTemp = (ArrayList<LocalPhotoFile>) bundle.getSerializable("photos");
		if(listTemp!=null&&listTemp.size()>0){
			list.clear();
			list.addAll(listTemp);
		}
		Log.e(TAG, "position= " + position);
		Log.e(TAG, "path= " + path);

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setPageMargin((int) (getResources().getDisplayMetrics().density * 15));
		mPager.setAdapter(new PagerAdapter() {
			@Override
			public int getCount() {
				//return imgsId.size();
				return list.size();
			}

			@Override
			public boolean isViewFromObject(View view, Object object) {
				return view == object;
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				PhotoView view = new PhotoView(PicturePreviewActivity.this);
				view.enable();
				//view.setImageResource(imgsId.get(position));
				LocalPhotoFile localPhotoFile = list.get(position);
				view.setImageResource(localPhotoFile.getPath());
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

	}

}