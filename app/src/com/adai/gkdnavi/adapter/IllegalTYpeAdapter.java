package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.gkd.bean.IllegalTypeBean;
import com.adai.gkdnavi.R;

import java.util.List;

/**
 * Created by huangxy on 2017/1/18.
 */

public class IllegalTYpeAdapter extends BaseAdapter {
    private List<IllegalTypeBean.DataBean.ItemsBean> mItemsBeens;
    private Context mContext;
    private boolean isShowPrice;

    public IllegalTYpeAdapter(Context context, List<IllegalTypeBean.DataBean.ItemsBean> itemsBeens, boolean isShowPrice) {
        mContext = context;
        this.isShowPrice = isShowPrice;
        mItemsBeens = itemsBeens;
    }

    @Override
    public int getCount() {
        return mItemsBeens == null ? 0 : mItemsBeens.size();
    }

    @Override
    public Object getItem(int position) {
        return mItemsBeens.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ChoiceListItemView choiceListItemView;
        if (convertView == null) {
            choiceListItemView = new ChoiceListItemView(mContext, null);
        } else {
            choiceListItemView = (ChoiceListItemView) convertView;
        }
        choiceListItemView.mLlPrice.setVisibility(isShowPrice ? View.VISIBLE : View.GONE);
        choiceListItemView.mTvIllegalType.setText(mItemsBeens.get(position).violation_name);
        choiceListItemView.mTvReportPrice.setText(mItemsBeens.get(position).price_time);
        return choiceListItemView;
    }


    private class ChoiceListItemView extends LinearLayout implements Checkable {

        private ImageView mIvSelected;
        private TextView mTvIllegalType;
        private LinearLayout mLlPrice;
        private TextView mTvReportPrice;

        public ChoiceListItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.item_illegal_type, this, true);
            mIvSelected = (ImageView) v.findViewById(R.id.iv_selected);
            mTvIllegalType = (TextView) v.findViewById(R.id.tv_illegal_type);
            mLlPrice = (LinearLayout) v.findViewById(R.id.ll_price);
            mTvReportPrice = (TextView) v.findViewById(R.id.tv_report_price);
        }


        @Override
        public void setChecked(boolean checked) {
            mIvSelected.setVisibility(checked ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public boolean isChecked() {
            return mIvSelected.getVisibility() == View.VISIBLE;
        }

        @Override
        public void toggle() {

        }
    }

}
