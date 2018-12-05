package com.adai.gkdnavi.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

public class MyNumberPicker extends NumberPicker {

	public MyNumberPicker(Context context) {
		super(context);
	}
	public MyNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyNumberPicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
    public void addView(View child) {
        super.addView(child);
        updateView(child);
    }
 
    @Override
    public void addView(View child, int index,
                        android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        updateView(child);
    }
 
    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        updateView(child);
    }
 
    public void updateView(View view) {
        if (view instanceof EditText) {
            //这里修改字体的属性
            ((EditText) view).setTextColor(Color.parseColor("#FE982C"));
//            view.setBackgroundColor(Color.parseColor("#FE9424"));
//            ((EditText) view).setTextSize();
        }
    }
}
