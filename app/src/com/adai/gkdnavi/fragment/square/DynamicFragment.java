package com.adai.gkdnavi.fragment.square;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.bean.square.RecommanPageBean;
import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkd.bean.square.TypeVideoListPageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.adapter.HeaderAndFooterWrapper;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.square.CacheManager;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;
import com.adai.gkdnavi.widget.LoadingLayout;
import com.ijk.media.widget.media.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * interface.
 */
public class DynamicFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private TypeVideoFragment.OnListFragmentInteractionListener mListener;
    private List<TypeVideoBean> datas = new ArrayList<TypeVideoBean>();
    private DynamicRecyclerViewAdapter adapter;
    private int currentPage = 1;
    private int pagesize = 10;
    private int typeid;

    private SwipeRefreshLayout refreshLayout;

    private boolean hasMore=true;

    private HeaderAndFooterWrapper<RecyclerView.ViewHolder> headerAndFooteradapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public DynamicFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static DynamicFragment newInstance(int columnCount, int typeid) {
        DynamicFragment fragment = new DynamicFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt("typeid", typeid);
        fragment.setArguments(args);
        return fragment;
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
        mLoadingLayout=new LoadingLayout(getContext());
        View view = inflater.inflate(R.layout.fragment_typevideo_list, container, false);
        mLoadingLayout.addView(view);
        mLoadingLayout.setContentView(view);
        mLoadingLayout.setRetryListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRetry();
            }
        });
        refreshLayout = (SwipeRefreshLayout) view;
        refreshLayout.setColorSchemeResources(R.color.main_color);
        refreshLayout.setId(R.id.dymicfragment);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                getDataFromServer();
            }
        });
        // Set the adapter
//        if (view instanceof RecyclerView) {
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
                if (hasMore&&!refreshLayout.isRefreshing()) {
                    refreshLayout.setRefreshing(true);
                    currentPage++;
                    getDataFromServer();
                }
            }
        });

        TypeVideoListPageBean serverCache = CacheManager.getInstance().getCache(DynamicFragment.class.getSimpleName() + "Server", TypeVideoListPageBean.class);
        if (serverCache != null) {
            if (serverCache.data != null && serverCache.data.items != null) {
                datas.addAll(serverCache.data.items);
            }
        }
        adapter = new DynamicRecyclerViewAdapter(getActivity(), datas, mListener);

        RecommanPageBean recommendCache = CacheManager.getInstance().getCache(DynamicFragment.class.getSimpleName() + "Recommend", RecommanPageBean.class);
        if (recommendCache != null) {
            if(recommendCache.data != null && recommendCache.data.size() > 0){
                adapter.addRecommand(recommendCache.data);
            }
        }
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
                            adapter.getLastPlay().btn_status.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });
//        headerAndFooteradapter=new HeaderAndFooterWrapper<>(adapter);
//        TextView textView=new TextView(getContext());
//        textView.setText("tttttttttttttttttttttttttt");
//        headerAndFooteradapter.addHeaderView(textView);
//        initHeader();
//        headerAndFooteradapter.addHeaderView(headerView);
        recyclerView.setAdapter(adapter);
//        }

        return mLoadingLayout;
    }

    @Override
    protected void lazyLoadData() {
        getRecommcandData();
        getDataFromServer();
    }

    private View headerView;
    private RecyclerView recommandlist;
    private MyLikeUserRecyclerViewAdapter recommandAdapter;
    private List<LikeUserBean> recommandDatas=new ArrayList<>();
    private void initHeader(){
        headerView=LayoutInflater.from(getContext()).inflate(R.layout.dynamic_header,null);
        recommandlist= (RecyclerView) headerView.findViewById(R.id.recommandlist);
        recommandAdapter=new MyLikeUserRecyclerViewAdapter(getActivity(),recommandDatas,null);
        recommandlist.setAdapter(recommandAdapter);
        getRecommcandData();
    }

    private void getRecommcandData(){
        RequestMethods_square.getReconmendPerson(new HttpUtil.Callback<RecommanPageBean>() {
            @Override
            public void onCallback(RecommanPageBean result) {
                if(result!=null){
                    switch (result.ret){
                        case 0:
                            if(result.data != null && result.data.size() > 0){
                                CacheManager.getInstance().setCache(DynamicFragment.class.getSimpleName() + "Recommend", result);
                                adapter.addRecommand(result.data);
                            }
                            break;
                        default:

                            break;
                    }
                }
            }
        });
    }
    public void getDataFromServer() {
        if(!refreshLayout.isRefreshing()){
            refreshLayout.setRefreshing(true);
        }
        RequestMethods_square.getDynamicVideo(currentPage, pagesize, new HttpUtil.Callback<TypeVideoListPageBean>() {
            @Override
            public void onCallback(TypeVideoListPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (currentPage == 1) {
                                datas.clear();
                                CacheManager.getInstance().setCache(DynamicFragment.class.getSimpleName() + "Server", result);
                            }
                            if (result.data != null && result.data.items != null&&result.data.items.size()>0) {
                                datas.addAll(result.data.items);
                                adapter.notifyDataSetChanged();
                                if(result.data._meta.totalCount>pagesize*currentPage){
                                    hasMore=true;
                                }else{
                                    hasMore=false;
                                }
                                showContent();
                            } else {
                                showEmptyContent();
//                                showContent();
//                                showToast(getString(R.string.no_data));
                            }
                            break;
                        default:
                            if(!TextUtils.isEmpty(result.message))
                            showToast(result.message);
                            if(currentPage>1){
                                currentPage--;
                            }
                            showError();
                            break;
                    }
                }
                refreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onRetry() {
        super.onRetry();
        getDataFromServer();
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
            if (!isVisibleToUser)adapter.onPause();
        }
    }
    public DynamicRecyclerViewAdapter getAdapter() {
        return adapter;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.onDestroy();
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

}
