package com.adai.gkdnavi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.SeekBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2017/1/16.
 */

public class CustomSeekBar extends SeekBar {
    private List<Bitmap> mBitmaps = new ArrayList<>();
    private int mMaxBitmapNum = 10;

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBitmapList(@NonNull List<Bitmap> bitmapList) {
        mBitmaps = bitmapList;
        while (mBitmaps.size() > mMaxBitmapNum) {
            mBitmaps.remove(mBitmaps.size() - 1);
        }
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBitmaps(canvas);
        super.onDraw(canvas);
    }

    private void drawBitmaps(Canvas canvas) {
        if (mBitmaps == null || mBitmaps.size() == 0) return;
        int perBitmapWidth = getWidth() / mMaxBitmapNum;
        for (int position = 0; position < mBitmaps.size(); position++) {
            Rect dst = new Rect();
            if (position == mMaxBitmapNum-1) {
                dst.set(perBitmapWidth * position, dp2px(5), getWidth(), getHeight() - dp2px(5));
            }else{
                dst.set(perBitmapWidth * position, dp2px(5), perBitmapWidth * (position + 1), getHeight() - dp2px(5));
            }
            canvas.drawBitmap(mBitmaps.get(position), null, dst, null);
        }
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
}
