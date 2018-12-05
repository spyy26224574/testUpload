package com.adai.gkdnavi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by admin on 2016/9/8.
 */
public class NoscrollListview extends ListView {
    public NoscrollListview(Context context) {
        super(context);
    }

    public NoscrollListview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoscrollListview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
