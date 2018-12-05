package com.adai.camera.novatek.filemanager;

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

import com.adai.camera.FileManagerConstant;
import com.adai.camera.novatek.adapter.NovatekFileManagerAdapter;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.view.WrapContentGridLayoutManager;
import com.example.ipcamera.domain.MinuteFile;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/8/8 10:07.
 */

public class NovatekFileManagerFragment extends BaseFragment implements NovatekFileManagerContract.View, View.OnClickListener {
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TYPE = "type";
    private static final String ARG_FILE_PATH = "file_path";
    public static final int REQUEST_FILE_DELETE = 1;
    private int mColumnCount;
    private int mType;
    private String mFilePath;
    private LinearLayout mRlBottomLayout;
    private ImageButton mBtnDelete, mBtnDownload;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NovatekFileManagerContract.Presenter mPresenter;
    private NovatekFileManagerAdapter mNovatekFileManagerAdapter;
    private boolean isEditMode = false;
    private TextView mTvEmpty;

    public static NovatekFileManagerFragment newInstance(int columnCount, int type, String filePath) {
        NovatekFileManagerFragment fragment = new NovatekFileManagerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mType = getArguments().getInt(ARG_TYPE);
            mFilePath = getArguments().getString(ARG_FILE_PATH);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        initFile();
    }

    public void initFile() {
        if (mPresenter != null) {
            mPresenter.initFile(mType, mFilePath);
        }
    }

    @Override
    protected void init() {
        super.init();
        mPresenter = new NovatekFileManagerPresenter();
        mPresenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout rootView = (LinearLayout) View.inflate(getContext(), R.layout.fragment_filemanager, null);
        mRlBottomLayout = (LinearLayout) rootView.findViewById(R.id.bottom_layout);
        mBtnDelete = (ImageButton) rootView.findViewById(R.id.btn_delete);
        mBtnDownload = (ImageButton) rootView.findViewById(R.id.btn_download);
        if (mType == FileManagerConstant.TYPE_REMOTE_NORMAL_VIDEO || mType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO) {
            mBtnDownload.setVisibility(View.VISIBLE);
            mBtnDownload.setOnClickListener(this);
        }
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl);
        mTvEmpty = (TextView) rootView.findViewById(R.id.tv_empty);
        mTvEmpty.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return event.getAction() == MotionEvent.ACTION_DOWN;
            }
        });
        mTvEmpty.setText(R.string.no_file);
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.main_color);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.initFile(mType, mFilePath);
            }
        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new WrapContentGridLayoutManager(mContext, mColumnCount));
        mBtnDelete.setOnClickListener(this);
        return rootView;
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
        }
    }

    @Override
    public void showLoading() {
        showpDialog();
    }

    @Override
    public void showLoading(String string) {
        switch (mType) {
            case FileManagerConstant.TYPE_LOCAL_PICTURE:
            case FileManagerConstant.TYPE_LOCAL_VIDEO:
                mSwipeRefreshLayout.setRefreshing(true);
                break;
            default:
                showpDialog(string);
                break;
        }
    }

    @Override
    public void showLoading(@StringRes int res) {
        showpDialog(res);
    }

    @Override
    public void showToast(String string) {
        showpDialog(string);
    }

    @Override
    public void showToast(@StringRes int res) {
        super.showToast(res);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtils.e("requestCode = " + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            mNovatekFileManagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void hideLoading() {
        hidepDialog();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public Context getAttachedContext() {
        return mContext;
    }

    @Override
    public void sortFileEnd(ArrayList<MinuteFile> minuteFiles) {
        if (mNovatekFileManagerAdapter == null) {
            mNovatekFileManagerAdapter = new NovatekFileManagerAdapter(mContext, minuteFiles, mType);
            mRecyclerView.setAdapter(mNovatekFileManagerAdapter);
            mNovatekFileManagerAdapter.setEventListener(new NovatekFileManagerAdapter.EventListener() {
                @Override
                public void modeChange(boolean isEditMode) {
                    NovatekFileManagerFragment.this.isEditMode = isEditMode;
                    mRlBottomLayout.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                }

                @Override
                public void hasFile(boolean hasFile) {
                    mBtnDelete.setEnabled(hasFile);
                    mBtnDownload.setEnabled(hasFile);
                    mRlBottomLayout.setEnabled(hasFile);
                }
            });
        } else {
            mNovatekFileManagerAdapter.setEditMode(false);
            mNovatekFileManagerAdapter.notifyDataSetChanged();
        }
        mTvEmpty.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public Activity getAttachedActivity() {
        return mContext;
    }

    @Override
    public void deleteResult(boolean success) {
        mRlBottomLayout.setVisibility(View.GONE);
        //退出编辑模式
        if (mNovatekFileManagerAdapter != null) {
            mNovatekFileManagerAdapter.setEditMode(false);
        }
    }

    @Override
    public void empty() {
        if (mNovatekFileManagerAdapter != null) {
            mNovatekFileManagerAdapter.setEditMode(false);
            mNovatekFileManagerAdapter.notifyDataSetChanged();
        }
        mTvEmpty.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        LogUtils.e("用户可见 = " + isVisibleToUser);
        if (!isVisibleToUser) {
            setEditMode(false);
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    @Override
    public void setEditMode(boolean isEditMode) {
        if (mRlBottomLayout != null) {
            mRlBottomLayout.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        }
        if (mNovatekFileManagerAdapter != null) {
            if (VoiceManager.selectedMinuteFile.size() > 0) {
                mNovatekFileManagerAdapter.setEditMode(isEditMode);
                mNovatekFileManagerAdapter.notifyDataSetChanged();
            } else {
                mNovatekFileManagerAdapter.setEditMode(isEditMode);
            }
        }
    }
}
