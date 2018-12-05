package com.adai.gkdnavi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

public class FocusedtrueTextView extends TextView {

	public FocusedtrueTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean isFocused() {
		// TODO Auto-generated method stub
		return true;
	}

}
