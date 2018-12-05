package com.adai.gkdnavi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.Button;

public class ProgressButton extends Button {
	private Drawable mProgressDrawable;
	private boolean mProgressEnable;
	private int mMax;
	private int mProgress;

	public ProgressButton(Context context) {
		this(context, null);
	}

	public ProgressButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setProgressEnable(boolean enable) {
		this.mProgressEnable = enable;
	}

	public void setProgressBackground(Drawable drawable) {
		this.mProgressDrawable = drawable;
	}

	public void setProgress(int progress) {
		this.mProgress = progress;
		invalidate();
	}

	public void setMax(int max) {
		this.mMax = max;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mProgressEnable) {
			// 进度条的绘制
			int width = getMeasuredWidth();
			if (mMax <= 0) {
				mMax = 100;
			}
			int right = (int) (width * mProgress * 1f / mMax + 0.5f);
			mProgressDrawable.setBounds(0, 0, right, getMeasuredHeight());
			mProgressDrawable.draw(canvas);// 将Drawable画到画布上
		}
		super.onDraw(canvas);// 文本的绘制
	}

}
