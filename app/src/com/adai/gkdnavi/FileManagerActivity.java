package com.adai.gkdnavi;

import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;

@SuppressWarnings("deprecation")
public class FileManagerActivity extends ActivityGroup {//TabHost

	private TabHost mTabHost = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_activity);

		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mTabHost = (TabHost) findViewById(R.id.maintabhost);
		mTabHost.setup();

		mTabHost.setup(this.getLocalActivityManager());

		mTabHost.addTab(mTabHost
				.newTabSpec("t1")
				.setIndicator("",getResources().getDrawable(R.drawable.local_tab))
				.setContent(new Intent(this, LocalActivity.class)));
		mTabHost.addTab(mTabHost
				.newTabSpec("t2")
				.setIndicator("",getResources().getDrawable(R.drawable.remote_tab))
				.setContent(new Intent(this, RemoteActivity.class)));

	}

}