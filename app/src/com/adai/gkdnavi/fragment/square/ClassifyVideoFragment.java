package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkd.bean.square.ClassifyVideoBean;
import com.adai.gkd.bean.square.ClassifyVideoPageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.adapter.ClassifyVideoAdapter;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.square.CacheManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类视频浏览fragment
 *
 * @author admin
 */
public class ClassifyVideoFragment extends BaseFragment {
    private boolean isFirstLoad = true;
    private View mainview;
    private RecyclerView videolist;
    private SwipeRefreshLayout refreshlayout;
    private List<ClassifyVideoBean> datas = new ArrayList<>();
    private ClassifyVideoAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mainview = inflater.inflate(R.layout.fragment_classify_video, null);
        videolist = (RecyclerView) mainview.findViewById(R.id.videolist);
        refreshlayout = (SwipeRefreshLayout) mainview.findViewById(R.id.swipe_container);
        refreshlayout.setColorSchemeResources(R.color.main_color);
        ClassifyVideoPageBean cache = CacheManager.getInstance().getCache(ClassifyVideoFragment.class.getSimpleName(), ClassifyVideoPageBean.class);
        if (cache != null) {
            if (cache.data != null && cache.data.size() > 0) {
                datas.clear();
                datas.addAll(cache.data);
            }
        }
        adapter = new ClassifyVideoAdapter(this, datas);
        videolist.setLayoutManager(new LinearLayoutManager(getActivity()));
        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.line_space_height);
        LineDecoration decoration = new LineDecoration(size);
        videolist.addItemDecoration(decoration);
        videolist.setAdapter(adapter);
        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDataFromServer();
            }
        });
        refreshlayout.setRefreshing(true);
//		initdata();
        return mainview;
    }

    public void firstLoad() {
        if (isFirstLoad) {
            isFirstLoad = false;
            getDataFromServer();

        }
    }

    private void getDataFromServer() {
        RequestMethods_square.getClassifyVideo(new HttpUtil.Callback<ClassifyVideoPageBean>() {
            @Override
            public void onCallback(ClassifyVideoPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (result.data != null && result.data.size() > 0) {
                                datas.clear();
                                datas.addAll(result.data);
                                adapter.notifyDataSetChanged();
                                CacheManager.getInstance().setCache(ClassifyVideoFragment.class.getSimpleName(), result);
                            }
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                } else {

                }
                refreshlayout.setRefreshing(false);
            }
        });
        if (!refreshlayout.isRefreshing()) {
            refreshlayout.setRefreshing(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(_TAG_, "onActivityResult: ");
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE:
                if (data != null && data.getBooleanExtra(VideoDetailActivity.KEY_DELETE, false)) {
                    getDataFromServer();
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(_TAG_, "onDestroy: ");
    }
    //	private List<ClassifyVideoBean> getDatas(){
//		for (int i = 0; i < 5; i++) {
//			ClassifyVideoBean cBean=new ClassifyVideoBean();
//			cBean.typeId=i;
//			cBean.typeName="类型名称"+i;
//			cBean.typeDescribe="这是类型"+i+"的简短描述";
//			cBean.squareColle=new ArrayList<VideoGridBean>();
//			for (int j = 0; j < 4; j++) {
//				VideoGridBean vBean=new VideoGridBean();
//				vBean.browseCount=100;
//				vBean.des="这是类型"+i+"第"+j+"个实例描述";
//				vBean.coverPicture=null;
//				vBean.fileType="100";
//				vBean.uploadDate="2016-07-08 14:55:22";
//				vBean.resourceId=j;
//				cBean.squareColle.add(vBean);
//			}
//			datas.add(cBean);
//		}
//		return null;
//	}
}
