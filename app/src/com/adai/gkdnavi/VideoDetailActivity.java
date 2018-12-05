package com.adai.gkdnavi;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.fragment.square.VideoDetailFragment;
import com.adai.gkdnavi.utils.VoiceManager;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.navi.BaiduMapNavigation;

import java.util.ArrayList;
import java.util.List;

public class VideoDetailActivity extends BaseFragmentActivity implements VideoDetailFragment.OnFragmentInteractionListener, View.OnClickListener {
    public static final String KEY_DELETE = "key_delete";
    private VideoDetailFragment detailFragment;
    //    private RecyclerView list;
    private ImageView btn_right;
    private int resourceid;
    private String fileType;
    private String isCollect = "N";

    public void setIsCollect(String isCollect) {
        this.isCollect = isCollect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
//        list=(RecyclerView)findViewById(R.id.recyclerview);
        btn_right = (ImageView) findViewById(R.id.right_img);
        btn_right.setImageResource(R.drawable.more_orange);
        btn_right.setVisibility(View.VISIBLE);
    }

    @Override
    protected void init() {
        super.init();
        setTitle(getString(R.string.title_detail));
        resourceid = getIntent().getIntExtra("resourceid", -1);
        fileType = getIntent().getStringExtra("fileType");
        if ("300".equals(fileType)) {
            SDKInitializer.initialize(getApplicationContext());
        }
        detailFragment = VideoDetailFragment.newInstance(resourceid, fileType);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, detailFragment).commit();
        btn_right.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("9522", "onDestroy");
        if ("300".equals(fileType)) {
            BaiduMapNavigation.finish(mContext);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("9522", "onRestart");
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.right_img:
                if (VoiceManager.isLogin) {
                    showSelectDialog();
                } else {
                    Intent loginIntent = new Intent(mContext,
                            LoginActivity.class);
                    startActivity(loginIntent);
                }
                break;
        }
    }

    private void showSelectDialog() {
        List<String> items = new ArrayList<>();
        if ("Y".equals(isCollect)) {
            items.add(getString(R.string.cancel_collect));
        } else {
            items.add(getString(R.string.collection));
        }
        items.add(getString(R.string.report));
        if (detailFragment.isCandelete()) {
            items.add(getString(R.string.action_delete));
        }
        new AlertDialog.Builder(mContext).setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        if ("Y".equals(isCollect)) {
                            deleteFavorite();
                        } else {
                            favorite();
                        }
                        break;
                    case 1:
                        gotoReport();
                        break;
                    case 2:
                        deleteResouce();
                        break;
                }
            }
        }).create().show();
    }

    private void deleteResouce() {
        RequestMethods_square.deleteResource(resourceid, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(getString(R.string.deleted_success));
                            Intent data = new Intent();
                            data.putExtra(KEY_DELETE, true);
                            setResult(RESULT_OK, data);
                            finish();
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
    }

    private void gotoReport() {
        Intent report = new Intent(mContext, ReportActivity.class);
        report.putExtra(ReportActivity.KEY_RESOURCE_ID, ReportActivity.class);
        startActivity(report);
    }

    /**
     * 添加收藏
     */
    private void favorite() {
        RequestMethods_square.addFavorite(resourceid, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(R.string.has_collected);
                            isCollect = "Y";
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
    }

    private void deleteFavorite() {
        RequestMethods_square.deleteFavorite(resourceid, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(R.string.has_canceled_collection);
                            isCollect = "N";
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
    }
}
