package com.adai.gkdnavi;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.adai.gkdnavi.fragment.square.ClassifyVideoFragment;

public class SquareActivity extends FragmentActivity {

	ClassifyVideoFragment classfyfragment;
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_square);
		classfyfragment=new ClassifyVideoFragment();
		FragmentManager fragmentmanager = getSupportFragmentManager();
		fragmentmanager.beginTransaction().replace(R.id.content, classfyfragment).commit();
	}
	
}
