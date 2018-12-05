package com.adai.gkdnavi.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;
/**
 * 处理click和touch事件拦截事件的自定义控件
 * @author Administrator
 *
 */
public class InterceptLinearlayout extends LinearLayout {

	public InterceptLinearlayout(Context context) {
		super(context,null);
	}
	
	public InterceptLinearlayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	
	public InterceptLinearlayout(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		return true;
	}
	
	
}
