package com.adai.gkdnavi.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.adai.gkdnavi.R;

/**
 * @author huangxy
 * @date 2018/6/22 11:33.
 */
public class BackgroundStyleView extends LinearLayout {
    GradientDrawable gradientDrawable;
    private int mode = 0; //0 边框模式；1 填充模式
    //按下去的颜色
    private int pressedColor;
    //正常颜色
    private int normalColor;
    //边框宽度
    private float strokeWidth;
    //圆角
    private float radius;

    public BackgroundStyleView(Context context) {
        this(context, null);
    }

    public BackgroundStyleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackgroundStyleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.BackgroundStyleView);
            normalColor = typedArray.getColor(R.styleable.BackgroundStyleView_normal_color, Color.TRANSPARENT);
            pressedColor = typedArray.getColor(R.styleable.BackgroundStyleView_press_color, Color.TRANSPARENT);
            strokeWidth = typedArray.getDimension(R.styleable.BackgroundStyleView_stroke_width, 0);
            radius = typedArray.getDimension(R.styleable.BackgroundStyleView_corner, 0);
            mode = typedArray.getInt(R.styleable.BackgroundStyleView_type, 0);
            typedArray.recycle();
        }
    }

    private void init() {
        gradientDrawable = new GradientDrawable();
        switch (mode) {
            case 0:
                gradientDrawable.setColor(Color.TRANSPARENT);
                gradientDrawable.setStroke((int) strokeWidth, normalColor);
                break;
            case 1:
                gradientDrawable.setColor(normalColor);
                break;
        }
        gradientDrawable.setCornerRadius(radius);
        setBackgroundDrawable(gradientDrawable);
    }

    public void setNormalColor(int color) {
        normalColor = color;
        switch (mode) {
            case 0:
                gradientDrawable.setStroke((int) strokeWidth, normalColor);
                break;
            case 1:
                gradientDrawable.setColor(normalColor);
                break;
        }
    }
}
