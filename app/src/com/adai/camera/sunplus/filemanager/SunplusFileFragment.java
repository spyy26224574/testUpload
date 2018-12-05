package com.adai.camera.sunplus.filemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.camera.sunplus.bean.SunplusMinuteFile;
import com.adai.camera.sunplus.data.GlobalInfo;
import com.adai.camera.sunplus.filemanager.adapter.SunplusFileAdapter;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.view.WrapContentGridLayoutManager;
import com.icatch.wificam.customer.type.ICatchFile;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/9/19 9:41.
 */

public class SunplusFileFragment extends BaseFragment implements View.OnClickListener, SunplusFileFragmentContract.View {
    private ArrayList<SunplusMinuteFile> mMinuteFiles;
    private int mColumnCount;
    private int mType;
    public static final int REQUEST_FILE_DELETE = 1;
    private ArrayList<ICatchFile> mCameraFiles;
    private SunplusFileFragmentContract.Presenter mPresenter;
    private SunplusFileAdapter mSunplusFileAdapter;
    private LinearLayout mLlBottomLayout;
    private ImageButton mBtnDelete, mBtnDown;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isEditMode = false;
    private TextView mTvEmpty;

    public static SunplusFileFragment newInstance(int columnCount, int type) {
        SunplusFileFragment fragment = new SunplusFileFragment();
        Bundle args = new Bundle();
        args.putInt("columnCount", columnCount);
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            mColumnCount = arguments.getInt("columnCount");
            mType = arguments.getInt("type");
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout rootView = (LinearLayout) View.inflate(mContext, R.layout.fragment_filemanager, null);
        mLlBottomLayout = (LinearLayout) rootView.findViewById(R.id.bottom_layout);
        mBtnDelete = (ImageButton) rootView.findViewById(R.id.btn_delete);
        mBtnDown = (ImageButton) rootView.findViewById(R.id.btn_download);
        mBtnDown.setVisibility(View.VISIBLE);
        mBtnDown.setOnClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl);
        mSwipeRefreshLayout.setEnabled(false);
        mTvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
        mTvEmpty.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });
        mTvEmpty.setText(R.string.no_file);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.main_color);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new WrapContentGridLayoutManager(mContext, mColumnCount));
        mBtnDelete.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new SunplusFileFragmentPresent();
        mPresenter.attachView(this);
    }

    @Override
    public void sortFileEnd(ArrayList<SunplusMinuteFile> minuteFiles) {
        mMinuteFiles = minuteFiles;
        if (mSunplusFileAdapter == null) {
            mSunplusFileAdapter = new SunplusFileAdapter(mContext, mMinuteFiles);
            mRecyclerView.setAdapter(mSunplusFileAdapter);
            mSunplusFileAdapter.setEventListener(new SunplusFileAdapter.EventListener() {
                @Override
                public void modeChange(boolean isEditMode) {
                    SunplusFileFragment.this.isEditMode = isEditMode;
                    mLlBottomLayout.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                }

                @Override
                public void hasFile(boolean hasFile) {
                    mBtnDelete.setEnabled(hasFile);
                    mBtnDown.setEnabled(hasFile);
                    mLlBottomLayout.setEnabled(hasFile);
                }
            });
        } else {
            mSunplusFileAdapter.setEditMode(false);
            mSunplusFileAdapter.notifyDataSetChanged();
        }
        mTvEmpty.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading(@StringRes int res) {
        showpDialog(res);
    }

    @Override
    public void showToast(String string) {
        super.showToast(string);
    }

    @Override
    public void showLoading(String string) {
        showpDialog(string);
    }

    @Override
    public void hideLoading() {
        hidepDialog();
    }

    @Override
    public void showToast(@StringRes int res) {
        super.showToast(res);
    }

    @Override
    public Context getAttachedContext() {
        return mContext;
    }

    @Override
    public void setEditMode(boolean editMode) {
        if (mLlBottomLayout != null) {
            mLlBottomLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        }
        if (mSunplusFileAdapter != null) {
            if (GlobalInfo.mSelectedMinuteFile.size() > 0) {
                mSunplusFileAdapter.setEditMode(editMode);
                mSunplusFileAdapter.notifyDataSetChanged();
            } else {
                mSunplusFileAdapter.setEditMode(editMode);
            }
        }
    }

    @Override
    public void empty() {
        if (mSunplusFileAdapter != null) {
            mSunplusFileAdapter.setEditMode(false);
            mSunplusFileAdapter.notifyDataSetChanged();
        }
        mTvEmpty.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            mSunplusFileAdapter.notifyDataSetChanged();
        }
    }

    public void setData(ArrayList<ICatchFile> fileDomains) {
        mCameraFiles = fileDomains;
        mPresenter.sortFile(mCameraFiles);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_download:
                mPresenter.download();
                break;
            case R.id.btn_delete:
                mPresenter.deleteFile();
                break;
            default:
                break;
        }
    }
}
