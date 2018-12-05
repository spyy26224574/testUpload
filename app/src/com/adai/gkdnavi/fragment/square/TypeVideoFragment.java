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

import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkd.bean.square.TypeVideoListPageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TypeVideoFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private List<TypeVideoBean> datas = new ArrayList<TypeVideoBean>();
    private TypeVideoRecyclerViewAdapter adapter;
    private int currentPage = 1;
    private int pagesize = 10;
    private int typeid;
    private boolean hasMore = true;

    private SwipeRefreshLayout refreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TypeVideoFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TypeVideoFragment newInstance(int columnCount, int typeid) {
        TypeVideoFragment fragment = new TypeVideoFragment();
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
        View view = inflater.inflate(R.layout.fragment_typevideo_list, container, false);

        refreshLayout = (SwipeRefreshLayout) view;
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
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        adapter = new TypeVideoRecyclerViewAdapter(this, datas, mListener);
        recyclerView.addItemDecoration(new LineDecoration(getActivity().getResources().getDimensionPixelSize(R.dimen.line_space_height)));
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter.getLastPlay() != null) {
                    if (!adapter.getLastPlay().mView.isShown()) {
                        adapter.getLastPlay().video_view.stopPlayback();
                        adapter.getLastPlay().video_view.release(true);
                        adapter.getLastPlay().video_view.stopBackgroundPlay();
                        adapter.getLastPlay().btn_status.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        recyclerView.setAdapter(adapter);
//        }
        getDataFromServer();
        return view;
    }

    public void getDataFromServer() {
        if (!refreshLayout.isRefreshing())
            refreshLayout.setRefreshing(true);
        typeid = getArguments().getInt("typeid");
        RequestMethods_square.getTypeVideo(typeid, currentPage, pagesize, new HttpUtil.Callback<TypeVideoListPageBean>() {
            @Override
            public void onCallback(TypeVideoListPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (currentPage == 1) {
                                datas.clear();
                            }
                            if (result.data != null && result.data.items != null) {
                                datas.addAll(result.data.items);
                                adapter.notifyDataSetChanged();
                                if (result.data._meta.totalCount > pagesize * currentPage) {
                                    hasMore = true;
                                } else {
                                    hasMore = false;
                                }
                            } else {
                                showToast(R.string.no_data);
                            }
                            break;
                        default:
                            if (currentPage > 1) {
                                currentPage--;
                            }
                            showToast(result.message);
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
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e(_TAG_, "onActivityResult: TypeVideoFragment");
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE:
                if (data != null && data.getBooleanExtra(VideoDetailActivity.KEY_DELETE, false)) {
                    currentPage = 1;
                    getDataFromServer();
                }
                break;
        }
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(TypeVideoBean item);
    }

    @Override
    public void onDestroy() {
        adapter.onDestroy();
        super.onDestroy();
    }
}
