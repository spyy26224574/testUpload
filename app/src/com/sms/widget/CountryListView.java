/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，
 * 也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2014年 mob.com. All rights reserved.
 */
package com.sms.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.gkdnavi.R;
import com.sms.adapter.CountryAdapter;
import com.sms.utils.SizeHelper;


/**
 * 自定义国家列表控件listview
 */
public class CountryListView extends RelativeLayout implements OnTouchListener {
    private GroupListView lvContries;
    private TextView tvScroll;
    private LinearLayout llScroll;
    private CountryAdapter adapter;

    public CountryListView(Context context) {
        super(context);
        init(context);
    }

    public CountryListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CountryListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        SizeHelper.prepare(context);

        lvContries = new GroupListView(context);
        lvContries.setDividerHeight(SizeHelper.fromPxWidth(1));
        lvContries.setDivider(context.getResources().getDrawable(R.drawable.cl_divider));
        adapter = new CountryAdapter(lvContries);
        lvContries.setAdapter(adapter);
        LayoutParams lpContries = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        int dp9 = SizeHelper.fromPxWidth(12);
        lpContries.setMargins(dp9, 0, dp9, 0);
        addView(lvContries, lpContries);

        tvScroll = new TextView(context);
        tvScroll.setTextColor(context.getResources().getColor(R.color.white));
        tvScroll.setBackgroundResource(R.drawable.country_group_scroll_down);
        tvScroll.setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeHelper.fromPxWidth(80));
        tvScroll.setTypeface(Typeface.DEFAULT);
        tvScroll.setVisibility(GONE);
        tvScroll.setGravity(Gravity.CENTER);
        int dp80 = SizeHelper.fromPxWidth(120);
        LayoutParams lp = new LayoutParams(dp80, dp80);
        lp.addRule(CENTER_IN_PARENT);
        addView(tvScroll, lp);

        llScroll = new LinearLayout(context);
        llScroll.setBackgroundResource(R.drawable.country_group_scroll_up);
        llScroll.setOrientation(LinearLayout.VERTICAL);
        llScroll.setOnTouchListener(this);
        lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(ALIGN_PARENT_RIGHT);
        lp.addRule(CENTER_VERTICAL);
        lp.rightMargin = SizeHelper.fromPxWidth(7);
        addView(llScroll, lp);

        initScroll(context);
    }

    private void initScroll(Context context) {
        llScroll.removeAllViews();
        SizeHelper.prepare(context);

        int size = adapter.getGroupCount();
        int dp3 = SizeHelper.fromPxWidth(6);
        int dp2 = SizeHelper.fromPxWidth(4);
        for (int i = 0; i < size; i++) {
            TextView tv = new TextView(context);
            tv.setText(adapter.getGroupTitle(i));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, SizeHelper.fromPxWidth(18));
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(dp3, dp2, dp3, dp2);
            llScroll.addView(tv);
        }
    }

    /**
     * 设置列表右边的字母索引的滑动监听
     */
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                v.setBackgroundResource(R.drawable.country_group_scroll_down);
                float x = event.getX();
                float y = event.getY();
                ViewGroup vg = (ViewGroup) v;
                onScroll(vg, x, y);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                float x = event.getX();
                float y = event.getY();
                ViewGroup vg = (ViewGroup) v;
                onScroll(vg, x, y);
            }
            break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                v.setBackgroundResource(R.drawable.country_group_scroll_up);
                tvScroll.setVisibility(GONE);
            }
            break;
        }
        return true;
    }

    /**
     * 设置列表右边的字母索引的滑动时的显示
     */
    public void onScroll(ViewGroup llScroll, float x, float y) {
        for (int i = 0, count = llScroll.getChildCount(); i < count; i++) {
            TextView v = (TextView) llScroll.getChildAt(i);
            if (x >= v.getLeft() && x <= v.getRight()
                    && y >= v.getTop() && y <= v.getBottom()) {
                lvContries.setSelection(i);
                tvScroll.setVisibility(VISIBLE);
                tvScroll.setText(v.getText());
                break;
            }
        }
    }

    /**
     * 搜索接口
     *
     * @param token
     */
    public void onSearch(String token) {
        adapter.search(token);
        adapter.notifyDataSetChanged();
        if (adapter.getGroupCount() == 0) {
            llScroll.setVisibility(View.GONE);
        } else {
            llScroll.setVisibility(View.VISIBLE);
        }
        initScroll(getContext());
    }

    /**
     * 设置listview item 的点击事件监听
     *
     * @param listener
     */
    public void setOnItemClickListener(GroupListView.OnItemClickListener listener) {
        lvContries.setOnItemClickListener(listener);
    }

    /**
     * 获取国家对象<国家名，区号， ID>
     *
     * @param group
     * @param position
     * @return
     */
    public String[] getCountry(int group, int position) {
        return adapter.getItem(group, position);
    }

}
