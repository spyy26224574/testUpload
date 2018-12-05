package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkd.bean.square.TypeVideoListPageBean;
import com.adai.gkd.bean.square.TypeVideoPageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;
import com.ijk.media.widget.media.IjkVideoView;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ResourceGridFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ResourceGridFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ResourceGridFragment extends BaseFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USERID = "userid";
    private static final String ARG_TYPE = "type";

    // TODO: Rename and change types of parameters
    private int userid;
    /**
     * 类型,0为分享,1为收藏,2为举报
     */
    private int type;

    private int page = 1;
    private int pagesize = 10;
    private boolean hasMore = true;
    private List<TypeVideoBean> mdatas = new ArrayList<>();

    private OnFragmentInteractionListener mListener;

    private BottomRefreshRecycleView gridView;
    private SwipeRefreshLayout refreshLayout;
    private TypeVideoRecyclerViewAdapter adapter;

    public ResourceGridFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param userid Parameter 1.
     * @param type   Parameter 2.
     * @return A new instance of fragment ResourceGridFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ResourceGridFragment newInstance(int userid, int type) {
        ResourceGridFragment fragment = new ResourceGridFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_USERID, userid);
        args.putInt(ARG_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userid = getArguments().getInt(ARG_USERID);
            type = getArguments().getInt(ARG_TYPE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resource_grid, container, false);
        gridView = (BottomRefreshRecycleView) view.findViewById(R.id.gridView);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);
        refreshLayout.setColorSchemeResources(R.color.main_color);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });
        gridView.setLayoutManager(new LinearLayoutManager(getActivity()));
        gridView.addItemDecoration(new LineDecoration(getActivity().getResources().getDimensionPixelSize(R.dimen.line_space_height)));
        adapter = new TypeVideoRecyclerViewAdapter(this, mdatas, null);
        gridView.setAdapter(adapter);
        gridView.setRefreshListener(new BottomRefreshRecycleView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!refreshLayout.isRefreshing() && hasMore) {
                    page++;
                    getDatafromServer();
                }
            }
        });
        gridView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
//        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent detail=new Intent(getActivity(), VideoDetailActivity.class);
//                detail.putExtra("resourceid",mdatas.get(position).resourceId);
//                detail.putExtra("fileType",mdatas.get(position).fileType);
//                getActivity().startActivity(detail);
//            }
//        });
        getDatafromServer();
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    HttpUtil.Callback<TypeVideoListPageBean> callback = new HttpUtil.Callback<TypeVideoListPageBean>() {
        @Override
        public void onCallback(TypeVideoListPageBean result) {
            if (result != null) {
                switch (result.ret) {
                    case 0:
                        loadData(result.data);
                        break;
                    default:
//                        showToast(result.message);
                        if (page > 1)
                            page--;
                        break;
                }
            }
            refreshLayout.setRefreshing(false);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        if (adapter != null) {
            adapter.onPause();
        }
    }

    private void loadData(TypeVideoPageBean data) {
        if (data != null || data.items != null && data.items.size() > 0) {
            if (page == 1) mdatas.clear();
            mdatas.addAll(data.items);
            adapter.notifyDataSetChanged();
            if (data._meta.totalCount > page * pagesize) {
                hasMore = true;
            } else {
                hasMore = false;
            }
        } else {
            if (page > 1) page--;
        }
    }

    private void getDatafromServer() {
        refreshLayout.setRefreshing(true);
        switch (type) {
            case 0:
                RequestMethods_square.getShare(userid, page, pagesize, callback);
                break;
            case 1:
                RequestMethods_square.getCollect(userid, page, pagesize, callback);
                break;
            case 2:
                RequestMethods_square.getReport(userid, page, pagesize, callback);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // updateSize player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE:
                if (data != null && data.getBooleanExtra(VideoDetailActivity.KEY_DELETE, false)) {
                    page = 1;
                    getDatafromServer();
                }
                break;
            default:
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
