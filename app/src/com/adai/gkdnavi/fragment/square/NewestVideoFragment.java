package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.camera.CameraConstant;
import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkd.bean.square.TypeVideoListPageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.square.CacheManager;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;
import com.ijk.media.widget.media.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class NewestVideoFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String TAG = NewestVideoFragment.class.getSimpleName();
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private TypeVideoFragment.OnListFragmentInteractionListener mListener;
    private List<TypeVideoBean> datas = new ArrayList<TypeVideoBean>();
    private TypeVideoRecyclerViewAdapter adapter;
    private int currentPage = 1;
    private int pagesize = 10;
    private int typeid;
    private boolean hasMore = true;

    private SwipeRefreshLayout refreshLayout;
    private String mNews;
    private boolean mIsUserVisible;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NewestVideoFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static NewestVideoFragment newInstance(int columnCount, int typeid) {
        NewestVideoFragment fragment = new NewestVideoFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt("typeid", typeid);
        fragment.setArguments(args);
        return fragment;
    }

    public TypeVideoRecyclerViewAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_typevideo_list, container, false);

        // Set the adapter
//        if (view instanceof RecyclerView) {
        refreshLayout = (SwipeRefreshLayout) view;
        refreshLayout.setColorSchemeResources(R.color.main_color);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                getDataFromServer();
            }
        });
        Context context = view.getContext();
        BottomRefreshRecycleView recyclerView = (BottomRefreshRecycleView) view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        recyclerView.setRefreshListener(new BottomRefreshRecycleView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (hasMore && !refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(true);
                    currentPage++;
                    getDataFromServer();
                }
            }
        });
        TypeVideoListPageBean serverCache = CacheManager.getInstance().getCache(NewestVideoFragment.class.getSimpleName() + "Server", TypeVideoListPageBean.class);
        if (serverCache != null) {
            if (serverCache.data != null && serverCache.data.items != null) {
                datas.addAll(serverCache.data.items);
            }
        }
        adapter = new TypeVideoRecyclerViewAdapter(this, datas, mListener);
        recyclerView.addItemDecoration(new LineDecoration(getActivity().getResources().getDimensionPixelSize(R.dimen.line_space_height)));
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter.getLastPlay() != null) {
                    if (!adapter.getLastPlay().mView.isShown()) {
                        if (adapter.getLastPlay().video_view.getCurrentState() == IjkVideoView.STATE_PREPARING) {
                            return;
                        }
                        if (adapter.getLastPlay().video_view.isPlaying()) {
                            adapter.getLastPlay().video_view.stopPlayback();
                            adapter.getLastPlay().video_view.release(true);
                            adapter.getLastPlay().video_view.stopBackgroundPlay();
                            adapter.getLastPlay().video_logo.setVisibility(View.VISIBLE);
                            adapter.getLastPlay().video_progress.setVisibility(View.GONE);
                            adapter.getLastPlay().btn_status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
//        }
        return view;
    }

    public void reFactoryData() {
        if (mIsUserVisible) {
            currentPage = 1;
            getDataFromServer();
        }
    }

    @Override
    protected void lazyLoadData() {
        getDataFromServer();
    }

    @Override
    protected void onUserVisible(boolean isUserVisible) {
        mIsUserVisible = isUserVisible;
        Log.e(_TAG_, "onUserVisible: " + mIsUserVisible);
        if (!isUserVisible) {
            return;
        }
        String news = SpUtils.getString(mContext, CameraConstant.CAMERA_FACTORY, "");
        if (!news.equals(mNews)) {
            mNews = news;
            reFactoryData();
        }
    }

    public void getDataFromServer() {
        if (!refreshLayout.isRefreshing()) {
            refreshLayout.setRefreshing(true);
        }
        RequestMethods_square.getNewstVideo(currentPage, pagesize, new HttpUtil.Callback<TypeVideoListPageBean>() {
            @Override
            public void onCallback(TypeVideoListPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (currentPage == 1) {
                                datas.clear();
                                CacheManager.getInstance().setCache(NewestVideoFragment.class.getSimpleName() + "Server", result);
                            }
                            if (result.data != null && result.data.items != null) {
                                datas.addAll(result.data.items);
                                adapter.notifyDataSetChanged();
                                if (result.data._meta.totalCount > currentPage * pagesize) {
                                    hasMore = true;
                                } else {
                                    hasMore = false;
                                }
                            } else {
                                showToast(R.string.no_data);
                            }
                            break;
                        default:
                            showToast(result.message);
                            if (currentPage > 1) {
                                currentPage--;
                            }
                            break;
                    }
                }
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(_TAG_, "onActivityResult: newsvideo");
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE:
                if (data != null && data.getBooleanExtra(VideoDetailActivity.KEY_DELETE, false)) {
                    currentPage = 1;
                    getDataFromServer();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        adapter.onPause();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (adapter != null) {
            if (!isVisibleToUser) {
                adapter.onPause();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.onDestroy();
    }
}
