package com.adai.camera.sunplus.filemanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.StringRes;

import com.adai.camera.CameraFactory;
import com.adai.camera.FileManagerConstant;
import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.tool.SunplusMinuteFileDownloadManager;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.icatch.wificam.customer.type.ICatchFile;

import java.util.ArrayList;
import java.util.List;


public class SunplusFileActivity extends BaseActivity implements SunplusFileActivityContract.View {
    private ISunplusCamera mSunplusCamera;
    private SunplusFileFragment mSunplusFileFragment;
    private SunplusFileActivityContract.Presenter mPresenter;
    private ArrayList<ICatchFile> mICatchFiles = new ArrayList<>();
    private int mColumn;
    private int mType;

    public static void start(Context context, int column, int type) {
        Intent intent = new Intent(context, SunplusFileActivity.class);
        intent.putExtra("column", column);
        intent.putExtra("type", type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunplus_file);
        init();
        initView();
        initFile();
    }

    @Override
    protected void init() {
        super.init();
        Intent intent = getIntent();
        mColumn = intent.getIntExtra("column", 3);
        mType = intent.getIntExtra("type", FileManagerConstant.TYPE_REMOTE_VIDEO);
        mSunplusCamera = CameraFactory.getInstance().getSunplusCamera();
        mPresenter = new SunplusFileActivityPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        if (mType == FileManagerConstant.TYPE_REMOTE_VIDEO) {
            setTitle(R.string.video);
        } else {
            setTitle(R.string.photo);
        }
        if (mSunplusFileFragment == null) {
            mSunplusFileFragment = SunplusFileFragment.newInstance(mColumn, mType);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mSunplusFileFragment).commit();
    }

    private void initFile() {
        mPresenter.initFile(mType);
    }

    @Override
    protected void goBack() {
        boolean editMode = mSunplusFileFragment.isEditMode();
        if (editMode) {
            mSunplusFileFragment.setEditMode(false);
        } else {
            if (SunplusMinuteFileDownloadManager.isDownloading) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.notice)
                        .setMessage(R.string.downloading_exit)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setNegativeButton(R.string.cancel, null).show();
            } else {
                super.goBack();
            }
        }
    }

    @Override
    public void showLoading() {
        showpDialog();
    }

    @Override
    public void showLoading(@StringRes int id) {
        showpDialog(id);
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
    public Context getAttachedContext() {
        return this;
    }

    @Override
    public Activity getAttachedActivity() {
        return this;
    }

    @Override
    public void deleteResult(boolean success) {

    }

    @Override
    public void respGetFileList(List<ICatchFile> cameraFiles) {
        mICatchFiles.clear();
        mICatchFiles.addAll(cameraFiles);
        if (mICatchFiles.size() > 0) {
            mSunplusFileFragment.setData(mICatchFiles);
        } else {
            mSunplusFileFragment.empty();
        }
    }

    @Override
    public void showToast(@StringRes int id) {
        super.showToast(id);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }
}
