package com.adai.gkdnavi.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/8/12 10:33
 * @updateAuthor $Author$
 * @updateDate $Date$
 * @des 点击相当于点击返回键的ImageButton
 */
public class BackImageButton extends ImageButton {
    private Activity mActivity;

    public BackImageButton(Context context) {
        this(context, null);
    }

    public BackImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity = (Activity) context;
        init();
    }

    private void init() {
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.onBackPressed();
            }
        });
    }
}
