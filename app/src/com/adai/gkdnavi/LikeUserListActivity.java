package com.adai.gkdnavi;

import android.os.Bundle;

import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkdnavi.fragment.square.LikeuserListFragment;

public class LikeUserListActivity extends BaseFragmentActivity implements LikeuserListFragment.OnListFragmentInteractionListener {

    LikeuserListFragment fragment;
    private int resourceid=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like_user_list);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void init() {
        super.init();
        setTitle(R.string.praise_list);
        resourceid=getIntent().getIntExtra("resourceid",-1);
        fragment=LikeuserListFragment.newInstance(1,resourceid);
        getSupportFragmentManager().beginTransaction().replace(R.id.content,fragment).commit();
    }

    @Override
    public void onListFragmentInteraction(LikeUserBean item) {

    }
}
