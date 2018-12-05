package com.adai.camera.novatek.filemanager.remote;

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
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.view.WrapContentGridLayoutManager;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;

import java.util.ArrayList;

/**
 * Created by huangxy on 2017/8/9 14:44.
 */

public class NovatekRemoteFileFragment extends BaseFragment implements NovatekRemoteFileContract.View, View.OnClickListener {
    private ArrayList<MinuteFile> mMinuteFiles;
    private int mColumnCount;
    private int mType;
    private ArrayList<FileDomain> mCameraFiles;
    private NovatekRemoteFilePresenter mPresenter;
    private LinearLayout mRlBottomLayout;
    private ImageButton mBtnDelete, mBtnDown;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NovatekFileManagerAdapter mNovatekFileManagerAdapter;
    private boolean isEditMode = false;
    private TextView mTvEmpty;

    public static NovatekRemoteFileFragment newInstance(int columnCount, int type) {
        NovatekRemoteFileFragment fragment = new NovatekRemoteFileFragment();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout rootView = (LinearLayout) View.inflate(getContext(), R.layout.fragment_filemanager, null);
        mRlBottomLayout = (LinearLayout) rootView.findViewById(R.id.bottom_layout);
        mBtnDelete = (ImageButton) rootView.findViewById(R.id.btn_delete);
        if (mType == FileManagerConstant.TYPE_REMOTE_URGENCY_VIDEO) {
            mBtnDelete.setVisibility(View.GONE);
        }
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
        mPresenter = new NovatekRemoteFilePresenter();
        mPresenter.attachView(this);
    }

    public void setData(ArrayList<FileDomain> fileDomains) {
        mCameraFiles = fileDomains;
        mPresenter.sortFile(mCameraFiles);
    }


    @Override
    public void sortFileEnd(ArrayList<MinuteFile> minuteFiles) {
        mMinuteFiles = minuteFiles;
        if (mNovatekFileManagerAdapter == null) {
            mNovatekFileManagerAdapter = new NovatekFileManagerAdapter(mContext, mMinuteFiles, mType);
            mRecyclerView.setAdapter(mNovatekFileManagerAdapter);
            mNovatekFileManagerAdapter.setEventListener(new NovatekFileManagerAdapter.EventListener() {
                @Override
                public void modeChange(boolean isEditMode) {
                    NovatekRemoteFileFragment.this.isEditMode = isEditMode;
                    mRlBottomLayout.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
                }

                @Override
                public void hasFile(boolean hasFile) {
                    mBtnDelete.setEnabled(hasFile);
                    mBtnDown.setEnabled(hasFile);
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
    public void showLoading(String string) {
        showpDialog(string);
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
    public void showToast(@StringRes int res) {
        super.showToast(res);
    }

    @Override
    public void hideLoading() {
        hidepDialog();
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
    public Context getAttachedContext() {
        return mContext;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            mNovatekFileManagerAdapter.notifyDataSetChanged();
        }
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            setEditMode(false);
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    @Override
    public void setEditMode(boolean editMode) {
        if (mRlBottomLayout != null) {
            mRlBottomLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        }
        if (mNovatekFileManagerAdapter != null) {
            if (VoiceManager.selectedMinuteFile.size() > 0) {
                mNovatekFileManagerAdapter.setEditMode(editMode);
                mNovatekFileManagerAdapter.notifyDataSetChanged();
            } else {
                mNovatekFileManagerAdapter.setEditMode(editMode);
            }
        }
    }
}
