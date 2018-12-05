package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkd.bean.square.LikePageBean;
import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.LoginActivity;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class AttentionListFragment extends BaseFragment {

    // TODO: Customize parameter argument names
    private static final String ARG_SOURCE_TYPE = "sourcetype";
    private static final String ARG_USER_ID = "userid";
    // TODO: Customize parameters
    private int mType = 0;
    private OnListFragmentInteractionListener mListener;
    private List<LikeUserBean> datas = new ArrayList<>();

    private int page = 1;
    private int pagesize = 20;
    private int userid = -1;
    private boolean hasMore = true;
    private AttenttionRecyclerViewAdapter adapter;
    private SwipeRefreshLayout refreshLayout;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AttentionListFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static AttentionListFragment newInstance(int type, int sourceid) {
        AttentionListFragment fragment = new AttentionListFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SOURCE_TYPE, type);
        args.putInt(ARG_USER_ID, sourceid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mType = getArguments().getInt(ARG_SOURCE_TYPE);
            userid = getArguments().getInt(ARG_USER_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment__like_list, container, false);

        refreshLayout = (SwipeRefreshLayout) view;
        refreshLayout.setColorSchemeResources(R.color.main_color);
        Context context = view.getContext();
        BottomRefreshRecycleView recyclerView = (BottomRefreshRecycleView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new LineDecoration(2));
        adapter = new AttenttionRecyclerViewAdapter(this, datas, mListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setRefreshListener(new BottomRefreshRecycleView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (hasMore) {
                    refreshLayout.setRefreshing(true);
                    page++;
                    getdataFromserver();
                }
            }
        });
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                page = 1;
                getdataFromserver();
            }
        });
        getdataFromserver();
        return view;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case PersonalPageActivity.REQUEST_CODE_FOUCS:
                if (data != null) {
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        String isFoucs = data.getStringExtra("isFoucs");
                        datas.get(position).isFocusOn = isFoucs;
                        adapter.notifyItemChanged(position);
                    }
                }
                break;
            case LoginActivity.REQ_CODE_LOGIN:
                if (VoiceManager.isLogin) {
                    adapter.notifyDataSetChanged();
                }
                break;
        }
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

    private void getdataFromserver() {
        RequestMethods_square.getAttention(userid, mType, page, pagesize, new HttpUtil.Callback<LikePageBean>() {
            @Override
            public void onCallback(LikePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (page == 1) {
                                datas.clear();
                            }
                            if (result.data != null && result.data.items != null) {
                                datas.addAll(result.data.items);
                            }
                            adapter.notifyDataSetChanged();
                            if (result.data != null && result.data._meta != null) {
                                if (result.data._meta.totalCount <= (page * pagesize)) {
                                    hasMore = false;
                                } else {
                                    hasMore = true;
                                }
                            }
                            break;
                        default:
                            if (page > 1) {
                                page--;
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
        void onListFragmentInteraction(LikeUserBean item);
    }
}
