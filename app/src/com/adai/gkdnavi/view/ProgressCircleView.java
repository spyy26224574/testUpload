package com.adai.gkdnavi.view;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkdnavi.R;

/**
 * 
 */
public class ProgressCircleView extends LinearLayout
{
	private ImageView	mIvIcon;
	private TextView	mTvText;
	private boolean		mProgressEnable;
	private int			mProgress;
	private int			mMax;
	private RectF		mOval;

	private Paint		mPaint	= new Paint();

	public ProgressCircleView(Context context) {
		this(context, null);
	}

	public ProgressCircleView(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 将xml和class 进行绑定
		View.inflate(getContext(), R.layout.progress_circle_view, this);

        mIvIcon = (ImageView) findViewById(R.id.progress_circle_iv_icon);
        mTvText = (TextView) findViewById(R.id.progress_circle_tv_text);
        mTvText.setVisibility(View.GONE);
        initPaint();
    }

    private void initPaint() {
        mPaint.setColor(getResources().getColor(R.color.orange));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setAntiAlias(true);
    }

    /**
     * 设置进度是否可见
     *
     * @param enable
     */
    public void setProgressEnable(boolean enable) {
        this.mProgressEnable = enable;
    }

    /**
     * 设置文本显示
     */
    public void setText(String text) {
        mTvText.setText(text);
    }

    /**
     * 图片的图标
     *
     * @param res
     */
    public void setIcon(int res) {
        mIvIcon.setImageResource(res);
    }

    /**
     * 设置进度显示
     *
     * @param progress
     */
    public void setProgress(int progress) {
        this.mProgress = progress;

        invalidate();
    }

    /**
     * 设置进度的最大值
     *
     * @param max
     */
    public void setMax(int max) {
        this.mMax = max;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mProgressEnable) {
            // 进度是否可见

            if (mOval == null) {
                int left = mIvIcon.getLeft();
                int top = mIvIcon.getTop();
                int right = mIvIcon.getRight();
                int bottom = mIvIcon.getBottom();
                mOval = new RectF(left + 1, top + 1, right - 1, bottom - 1);
            }

            float startAngle = -90;
            float sweepAngle = 0;
            if (mMax == 0) {
                mMax = 100;
            }
            sweepAngle = mProgress * 360f / mMax;
            canvas.drawArc(mOval, startAngle, sweepAngle, false, mPaint);
        }
    }

}
