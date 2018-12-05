package com.adai.gkdnavi.fragment.square;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.params.GetIllegalDetailParam;
import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.bean.square.ReviewBean;
import com.adai.gkd.bean.square.ReviewPageBean;
import com.adai.gkd.bean.square.VideoDetailBean;
import com.adai.gkd.bean.square.VideoDetailPageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.VideoDetailActivity;
import com.adai.gkdnavi.adapter.IllegalReviewAdapter;
import com.adai.gkdnavi.adapter.ReviewAdapter;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.view.BottomRefreshRecycleView;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VideoDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link VideoDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class VideoDetailFragment extends BaseFragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String RESOURCEID = "resourceid";

    // TODO: Rename and change types of parameters
    private int resourceid;
    private String fileType;

    private OnFragmentInteractionListener mListener;
    private BottomRefreshRecycleView content_list;
    private View head_layout;
    private ReviewAdapter adapter;
    private List<ReviewBean> datas = new ArrayList<ReviewBean>();
    private ReviewRecyclerViewAdapter mAdapter;
    private IllegalReviewAdapter mIllegalReviewAdapter;
    private View reply_layout;
    private EditText et_write_comments, et_replycontent;
    private ImageView btn_like_bottom, btn_share_bottom;
    private TextView btn_send_reply;

    private int currentPage = 1;
    private int pagesize = 10;
    private SwipeRefreshLayout refreshlayout;
    private View bottom_layout;
    private boolean needRefresh;
    private boolean hasMore = true;

    public VideoDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param resourceid .
     * @return A new instance of fragment Video_Detail_Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static VideoDetailFragment newInstance(int resourceid, String fileType) {
        VideoDetailFragment fragment = new VideoDetailFragment();
        Bundle args = new Bundle();
        args.putInt(RESOURCEID, resourceid);
        args.putString("fileType", fileType);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            resourceid = getArguments().getInt(RESOURCEID);
            fileType = getArguments().getString("fileType");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment\
        View view = inflater.inflate(R.layout.fragment_video__detail_, null, false);
        reply_layout = view.findViewById(R.id.reply_layout);
        reply_layout.setOnClickListener(this);
        et_write_comments = (EditText) view.findViewById(R.id.et_write_comments);
        et_write_comments.setOnClickListener(this);
        et_replycontent = (EditText) view.findViewById(R.id.et_replycontent);
        btn_like_bottom = (ImageView) view.findViewById(R.id.btn_like_bottom);
        btn_like_bottom.setOnClickListener(this);
        btn_share_bottom = (ImageView) view.findViewById(R.id.btn_share_bottom);
        btn_share_bottom.setOnClickListener(this);
        if ("300".equals(fileType)) {
            btn_share_bottom.setVisibility(View.GONE);
        }
        btn_send_reply = (TextView) view.findViewById(R.id.btn_send_reply);
        btn_send_reply.setOnClickListener(this);
        content_list = (BottomRefreshRecycleView) view.findViewById(R.id.reply_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        content_list.setLayoutManager(layoutManager);
        head_layout = View.inflate(getActivity(), R.layout.video_detail_head, null);
//        ReviewRecyclerViewAdapter madapter = new ReviewRecyclerViewAdapter(getActivity(), datas);
//        content_list.setAdapter(madapter);
        LineDecoration decoration = new LineDecoration(2);
        content_list.addItemDecoration(decoration);
//        layoutManager.addView(head_layout);
//        adapter=new ReviewAdapter(getActivity(),datas);
//        content_list.addHeaderView(head_layout,null,true);
//        content_list.requestFocus();
//        content_list.setAdapter(adapter);
        refreshlayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshlayout);
        refreshlayout.setColorSchemeResources(R.color.main_color);
        content_list.setRefreshListener(new BottomRefreshRecycleView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!refreshlayout.isRefreshing() && hasMore) {
                    refreshlayout.setRefreshing(true);
                    currentPage++;
                    getReplyFromServer();
                }
            }
        });
        refreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage = 1;
                getDatafromServer();
            }
        });
        bottom_layout = view.findViewById(R.id.bottom_layout);
        getDatafromServer();
        return view;
    }

    /**
     * 获取是否允许删除
     *
     * @return
     */
    public boolean isCandelete() {
        if (detailData == null) return false;
        return "Y".equals(detailData.isDelete);
    }

    private void getReplyFromServer() {
        RequestMethods_square.getMoreReview(resourceid, currentPage, pagesize, new HttpUtil.Callback<ReviewPageBean>() {
            @Override
            public void onCallback(ReviewPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            if (result.data != null) {
                                if (result.data.items != null && result.data.items.size() > 0) {
                                    if (mAdapter != null) {
                                        mAdapter.addReview(result.data.items);
                                    }
                                    if (mIllegalReviewAdapter != null) {
                                        mIllegalReviewAdapter.addReview(result.data.items);
                                    }
                                } else {
                                    hasMore = false;
                                }
                            }
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
                refreshlayout.setRefreshing(false);
            }
        });
    }

    public void addSeeResource() {
        RequestMethods_square.addSeeResource(resourceid, null);
    }

    public void getDatafromServer() {
        if (!"300".equals(fileType)) {
            RequestMethods_square.getResourceDetail(fileType, resourceid, CurrentUserInfo.id, new HttpUtil.Callback<VideoDetailPageBean>() {
                @Override
                public void onCallback(VideoDetailPageBean result) {
                    if (result != null) {
                        switch (result.ret) {
                            case 0:
                                if (result.data != null) {
                                    loadData(result.data);
                                    addSeeResource();
                                } else {
                                    showToast(R.string.no_data);
                                }
                                break;
                            default:
                                showToast(result.message);
                                break;
                        }
                    }
                    refreshlayout.setRefreshing(false);
                }
            });
        } else {
            GetIllegalDetailParam getIllegalDetailParam = new GetIllegalDetailParam();
            getIllegalDetailParam.resourceId = resourceid;
            RequestMethods_square.getIllegalDetail(getIllegalDetailParam, new HttpUtil.Callback<VideoDetailPageBean>() {
                @Override
                public void onCallback(VideoDetailPageBean result) {
                    if (result != null) {
                        switch (result.ret) {
                            case 0:
                                if (result.data != null) {
                                    loadIllegalData(result.data);
                                    addSeeResource();
                                } else {
                                    showToast(R.string.no_data);
                                }
                                break;
                            default:
                                showToast(result.message);
                                break;
                        }
                    }
                    refreshlayout.setRefreshing(false);
                }
            });
        }
    }


    private VideoDetailBean detailData;

    private void loadData(VideoDetailBean data) {
        Log.e("9527", "data.videoUrl = " + data.videoUrl);
        detailData = data;
        mAdapter = new ReviewRecyclerViewAdapter(getActivity(), data);
        content_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                mAdapter.onScrollStateChanged();
            }
        });
        mAdapter.setOnReplyClick(new ReviewRecyclerViewAdapter.OnReplyClick() {
            @Override
            public void onReply(int toUserid, String nickname) {
                showReplyLayout(toUserid, nickname);
            }

            @Override
            public void onLike(int resourceid) {
                like();
            }

            @Override
            public void onAttention(View v, int userid) {
                if (v instanceof TextView) {
                    attentionView = (TextView) v;
                }
                attention(userid);
            }
        });
        content_list.setAdapter(mAdapter);
        if (getActivity() instanceof VideoDetailActivity) {
            ((VideoDetailActivity) getActivity()).setIsCollect(data.isCollect);
        }
        refreshLike();
//        if ("Y".equals(data.isReview)) {
//            bottom_layout.setVisibility(View.VISIBLE);
//        }
    }

    private void loadIllegalData(VideoDetailBean data) {
        detailData = data;
        mIllegalReviewAdapter = new IllegalReviewAdapter(getContext(), data);
        mIllegalReviewAdapter.setOnReplyClick(new IllegalReviewAdapter.OnReplyClick() {
            @Override
            public void onReply(int toUserid, String nickname) {
                showReplyLayout(toUserid, nickname);
            }

            @Override
            public void onLike(int resourceid) {
                like();
            }

            @Override
            public void onAttention(View v, int userid) {
                if (v instanceof TextView) {
                    attentionView = (TextView) v;
                }
                attention(userid);
            }
        });
        content_list.setAdapter(mIllegalReviewAdapter);
        refreshLike();
//        if ("Y".equals(data.isReview)) {
//            bottom_layout.setVisibility(View.VISIBLE);
//        }
    }

    public int getUserId() {
        if (detailData != null) {
            return detailData.userId;
        }
        return -1;
    }

    private TextView attentionView;

    /**
     * 关注
     *
     * @param userid
     */
    public void attention(int userid) {
        if (checkLogin()) {
            needRefresh = false;
            if (detailData != null) {
                if ("Y".equals(detailData.isFocus)) {
                    RequestMethods_square.deleteAttention(userid, new HttpUtil.Callback<BasePageBean>() {
                        @Override
                        public void onCallback(BasePageBean result) {
                            if (result != null) {
                                switch (result.ret) {
                                    case 0:
                                        attentionView.setText(getString(R.string.add_attention));
                                        detailData.isFocus = "N";
                                        attentionView.setSelected(false);
//                                        mAdapter.notifyItemChanged(0);
                                        break;
                                    default:
                                        showToast(result.message);
                                        break;
                                }
                            }
                        }
                    });
                } else {
                    RequestMethods_square.addAttention(userid, new HttpUtil.Callback<BasePageBean>() {
                        @Override
                        public void onCallback(BasePageBean result) {
                            if (result != null) {
                                switch (result.ret) {
                                    case 0:
                                        attentionView.setText(getString(R.string.already_attention));
                                        detailData.isFocus = "Y";
                                        attentionView.setSelected(true);
//                                        mAdapter.notifyItemChanged(0);
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
        } else {
            needRefresh = true;
        }
    }

    boolean isOnlike = false;

    private void like() {
        if (!checkLogin()) {
            needRefresh = true;
            return;
        }
        needRefresh = false;
        if (isOnlike) return;
        isOnlike = true;
        if (detailData != null) {
            if ("Y".equals(detailData.isLike)) {
                RequestMethods_square.deleteLike(resourceid, new HttpUtil.Callback<BasePageBean>() {
                    @Override
                    public void onCallback(BasePageBean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    detailData.isLike = "N";
                                    detailData.likeCount--;
                                    if (detailData.likeList != null && detailData.likeList.size() > 0) {
                                        for (LikeUserBean user : detailData.likeList) {
                                            if (user.userId == CurrentUserInfo.id) {
                                                detailData.likeList.remove(user);
                                                break;
                                            }
                                        }
                                    }
                                    refreshLike();
                                    break;
                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        isOnlike = false;
                    }
                });
            } else {
                RequestMethods_square.addLike(resourceid, new HttpUtil.Callback<BasePageBean>() {
                    @Override
                    public void onCallback(BasePageBean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    detailData.isLike = "Y";
                                    detailData.likeCount++;
                                    if (detailData.likeList == null) {
                                        detailData.likeList = new ArrayList<LikeUserBean>();
                                    }
                                    if (detailData.likeList.size() < 6) {
                                        LikeUserBean user = new LikeUserBean();
                                        user.userId = CurrentUserInfo.id;
                                        user.nickname = CurrentUserInfo.nickname;
                                        user.portrait = CurrentUserInfo.portrait;
                                        detailData.likeList.add(user);
                                    }
                                    refreshLike();
                                    break;
                                default:
                                    showToast(result.message);
                                    break;
                            }
                        }
                        isOnlike = false;
                    }
                });
            }
        }
    }

    private void refreshLike() {
        if (mAdapter != null) {
            mAdapter.notifyItemChanged(1);
        }
        if (mIllegalReviewAdapter != null) {
            mIllegalReviewAdapter.notifyItemChanged(1);
        }
        if (detailData != null)
            btn_like_bottom.setSelected("Y".equals(detailData.isLike));
    }

    private void reply() {
        if (!checkLogin()) {
            needRefresh = true;
            return;
        }
        needRefresh = false;
        String replytext = et_replycontent.getText().toString();
        if (TextUtils.isEmpty(replytext)) {
            showToast(R.string.please_input_reply);
            return;
        }
        RequestMethods_square.addReview(resourceid, lastuserid, replytext, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            getDatafromServer();
                            clearReplyInfo();
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
        hideReplyLayout();
    }

    private void loadLikeGrid(List<LikeUserBean> likelist) {

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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

    private int lastuserid;

    private void showReplyLayout(int userid, String nickname) {
        reply_layout.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(nickname))
            et_replycontent.setHint(getContext().getString(R.string.reply) + nickname);
        et_replycontent.requestFocus();
        InputMethodManager inputManager =
                (InputMethodManager) et_replycontent.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(et_replycontent, 0);
        lastuserid = userid;
    }

    private void hideReplyLayout() {
        reply_layout.setVisibility(View.GONE);
    }

    private void clearReplyInfo() {
        et_replycontent.setHint("");
        et_replycontent.setText("");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null)
            mAdapter.onActivityStop();
        if (mIllegalReviewAdapter != null) {
            mIllegalReviewAdapter.onActivityDestroy();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null)
            mAdapter.onActivityResume();
//        if (mIllegalReviewAdapter != null) {
//            mIllegalReviewAdapter.onActivityDestroy();
//        }
        if (needRefresh) {
            refreshlayout.setRefreshing(true);
            getDatafromServer();
        }
    }


    @Override
    public void onDestroy() {
//        if (mAdapter != null)
//            mAdapter.onActivityDestroy();
//        if (mIllegalReviewAdapter != null) {
//            mIllegalReviewAdapter.onActivityDestroy();
//        }
        super.onDestroy();
        if (mAdapter != null)
            mAdapter.onActivityDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mAdapter != null) {
            mAdapter.onActivityPause();
        }
        if (mIllegalReviewAdapter != null) {
            mIllegalReviewAdapter.onActivityPause();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.et_write_comments://写评论
                if (detailData != null && "Y".equals(detailData.isReview)) {
                    showReplyLayout(-1, null);
                } else {
                    showToast(R.string.not_support_comment);
                }
                break;
            case R.id.reply_layout:
                hideReplyLayout();
                break;
            case R.id.btn_like_bottom:
                like();
                break;
            case R.id.btn_send_reply:
                reply();
                break;
            case R.id.btn_share_bottom:
                // TODO:  分享url
                if (detailData != null && !TextUtils.isEmpty(detailData.shareAddress)) {
                    new ShareUtils().showShareDialog(getActivity(), detailData.shareAddress, detailData.des, detailData.des, detailData.coverPicture);
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
