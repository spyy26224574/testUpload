package com.adai.gkdnavi.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.adai.gkdnavi.R;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/9 10:44
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class NoticeDialog extends Dialog implements View.OnClickListener {
    private TextView mTitle;
    private TextView mTvNotice;
    private TextView mConfirm;
    private TextView mCancel;
    private TextView mTvNeutral;

    public interface OnClickListener {
        void onClick();
    }

    private OnClickListener mOnClickListener;//左边按钮点击
    private OnClickListener mOnClickListener1;//右边按钮点击
    private OnClickListener mOnClickListener2;//中间按钮点击事件

    private void assignViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mTvNotice = (TextView) findViewById(R.id.tv_notice);
        mConfirm = (TextView) findViewById(R.id.confirm);
        mCancel = (TextView) findViewById(R.id.cancel);
        mTvNeutral = (TextView) findViewById(R.id.tv_neutral);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mTvNeutral.setOnClickListener(this);
    }

    public NoticeDialog setTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    public NoticeDialog setMessage(String message) {
        mTvNotice.setText(message);
        return this;
    }

    public NoticeDialog setPositiveClickListener(String text, OnClickListener onClickListener) {
        mConfirm.setText(text);
        mOnClickListener = onClickListener;
        return this;
    }

    public NoticeDialog setNeutralClickListener(String text, OnClickListener onClickListener) {
        mOnClickListener2 = onClickListener;
        mTvNeutral.setVisibility(View.VISIBLE);
        mTvNeutral.setText(text);
        return this;
    }

    public NoticeDialog setNegativeClickListener(String text, OnClickListener onClickListener) {
        mCancel.setText(text);
        mOnClickListener1 = onClickListener;
        return this;
    }

    public NoticeDialog(Context context) {
        this(context, 0);
    }

    public NoticeDialog(Context context, int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_notice);
        initWidth();
        assignViews();
    }

    private void initWidth() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        DisplayMetrics outMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        float density = outMetrics.density;
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        int width = widthPixels > heightPixels ? heightPixels : widthPixels;
        attributes.width = (int) (width - 32 * density);
        window.setAttributes(attributes);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                if (mOnClickListener1 != null) {
                    mOnClickListener1.onClick();
                }
                dismiss();
                break;
            case R.id.confirm:
                if (mOnClickListener != null) {
                    mOnClickListener.onClick();
                }
                dismiss();
                break;
            case R.id.tv_neutral:
                if (mOnClickListener2 != null) {
                    mOnClickListener2.onClick();
                }
                dismiss();
                break;
        }
    }
}
