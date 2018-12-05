package com.adai.gkdnavi.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ArrayRes;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.adai.gkdnavi.R;

import java.util.ArrayList;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/8 14:13
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class SingleChoiceDialog extends Dialog implements View.OnClickListener {
    private TextView mTitle;
    private TextView mConfirm;
    private TextView mCancel;
    private ListView mList;
    private ArrayList<ItemBean> mData;
    private int mWhichSelected;

    public interface OnItemClickListener {
        void onItemClick(String content, int which);
    }

    private OnItemClickListener mOnItemClickListener;

    private void assignViews() {
        mTitle = (TextView) findViewById(R.id.title);
        mConfirm = (TextView) findViewById(R.id.confirm);
        mCancel = (TextView) findViewById(R.id.cancel);
        mList = (ListView) findViewById(R.id.list);
    }

    public SingleChoiceDialog(Context context) {
        this(context, 0);
    }

    public SingleChoiceDialog setTitle(String title) {
        mTitle.setText(title);
        return this;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            int height = mList.getHeight();
            if (height > width - 112 * density) {
                ViewGroup.LayoutParams layoutParams = mList.getLayoutParams();
                layoutParams.height = (int) (width - 112 * density);
                mList.setLayoutParams(layoutParams);
            }
        }
    }

    public SingleChoiceDialog setSingleChoiceItems(ArrayList<String> array, int checkedItem, @NonNull OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        mData = new ArrayList<>();
        for (String s : array) {
            ItemBean itemBean = new ItemBean();
            itemBean.content = s;
            mData.add(itemBean);
        }
        mWhichSelected = checkedItem;
        mData.get(mWhichSelected).selected = true;
        mList.setAdapter(mSingleChoiceAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWhichSelected = position;
                for (ItemBean itemBean : mData) {
                    itemBean.selected = false;
                }
                mData.get(position).selected = true;
                mSingleChoiceAdapter.notifyDataSetChanged();
            }
        });
        return this;
    }
    public SingleChoiceDialog setSingleChoiceItems(@ArrayRes int array, int checkedItem, @NonNull OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
        String[] stringArray = getContext().getResources().getStringArray(array);
        mData = new ArrayList<>();
        for (String s : stringArray) {
            ItemBean itemBean = new ItemBean();
            itemBean.content = s;
            mData.add(itemBean);
        }
        mWhichSelected = checkedItem;
        mData.get(mWhichSelected).selected = true;
        mList.setAdapter(mSingleChoiceAdapter);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mWhichSelected = position;
                for (ItemBean itemBean : mData) {
                    itemBean.selected = false;
                }
                mData.get(position).selected = true;
                mSingleChoiceAdapter.notifyDataSetChanged();
            }
        });
        return this;
    }
    private int width;
    private float density;

    private void initWidth() {
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        DisplayMetrics outMetrics = new DisplayMetrics();
        window.getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        density = outMetrics.density;
        int windowWidth = outMetrics.widthPixels;
        int windowHeight = outMetrics.heightPixels;
        width = windowWidth > windowHeight ? windowHeight : windowWidth;
        attributes.width = (int) (width - 32 * density);
        window.setAttributes(attributes);
    }

    public SingleChoiceDialog(Context context, int themeResId) {
        super(context, themeResId);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_list);
        initWidth();
        assignViews();
        initEvent();
    }

    private void initEvent() {
        mConfirm.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                mOnItemClickListener.onItemClick(mData.get(mWhichSelected).content, mWhichSelected);
                dismiss();
                break;
            case R.id.cancel:
                dismiss();
                break;
        }
    }

    private BaseAdapter mSingleChoiceAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(getContext(), R.layout.dialog_item_singlechoice, null);
                holder.mTextView = (TextView) convertView.findViewById(R.id.textView);
                holder.mRadioButton = (RadioButton) convertView.findViewById(R.id.radioButton);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.mTextView.setText(mData.get(position).content);
            holder.mRadioButton.setChecked(mData.get(position).selected);
            return convertView;
        }

        class ViewHolder {
            TextView mTextView;
            RadioButton mRadioButton;
        }
    };
}
