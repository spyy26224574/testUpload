package com.adai.gkdnavi.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.adai.gkdnavi.R;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/9 19:32
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class SeekBarDialog extends Dialog implements View.OnClickListener {
    private TextView mTitle;
    private LinearLayout mLlTempTime;
    private ImageView mIvSub;
    private SeekBar mSbTempTime;
    private ImageView mIvAdd;
    private TextView mConfirm;
    private TextView mCancel;
    private int mValue;

    public interface OnPositiveClickListener {
        void onclick(int value);
    }

    private OnPositiveClickListener mOnPositiveClickListener;

    private void assignViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mLlTempTime = (LinearLayout) findViewById(R.id.ll_temp_time);
        mIvSub = (ImageView) findViewById(R.id.iv_sub);
        mSbTempTime = (SeekBar) findViewById(R.id.sb_temp_time);
        mIvAdd = (ImageView) findViewById(R.id.iv_add);
        mConfirm = (TextView) findViewById(R.id.confirm);
        mCancel = (TextView) findViewById(R.id.cancel);
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
        mIvSub.setOnClickListener(this);
        mIvAdd.setOnClickListener(this);
        mSbTempTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mValue = progress / 50 * 50 + 200;
                mTitle.setText(String.valueOf(mValue));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress() / 50 * 50;
                mSbTempTime.setProgress(progress);
                mValue = progress + 200;
                mTitle.setText(String.valueOf(mValue));
            }
        });
    }

    public SeekBarDialog(Context context) {
        this(context, 0);
    }

    public SeekBarDialog(Context context, int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_seekbar);
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

    public SeekBarDialog setTitle(String title) {
//        mTitle.setText(title);
        return this;
    }

    public SeekBarDialog setSeekBarContent(int progress) {
        mSbTempTime.setProgress(progress - 200);
        mValue = mSbTempTime.getProgress() / 50 * 50 + 200;
        mTitle.setText(String.valueOf(progress));
        return this;
    }

    public SeekBarDialog setPositiveClickListener(OnPositiveClickListener positiveClickListener) {
        mOnPositiveClickListener = positiveClickListener;
        return this;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel:
                dismiss();
                break;
            case R.id.confirm:
                if (mOnPositiveClickListener != null) {
                    mOnPositiveClickListener.onclick(mValue);
                }
                dismiss();
                break;
            case R.id.iv_sub:
                mSbTempTime.setProgress(mSbTempTime.getProgress() - 50);
                mValue = mSbTempTime.getProgress() / 50 * 50 + 200;
                mTitle.setText(String.valueOf(mValue));
                break;
            case R.id.iv_add:
                mSbTempTime.setProgress(mSbTempTime.getProgress() + 50);
                mValue = mSbTempTime.getProgress() / 50 * 50 + 200;
                mTitle.setText(String.valueOf(mValue));
                break;
        }
    }
}
