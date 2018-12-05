package com.adai.gkdnavi;

import android.os.Bundle;

import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkdnavi.fragment.square.AttentionListFragment;

public class AttentionListActivity extends BaseFragmentActivity implements AttentionListFragment.OnListFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attention_list);
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
        int type=getIntent().getIntExtra("type",0);
        int userid=getIntent().getIntExtra("userid",-1);
        setTitle(type==0?R.string.watchlist:R.string.fans_list);
        getSupportFragmentManager().beginTransaction().add(R.id.content, AttentionListFragment.newInstance(type,userid)).commit();
    }

    @Override
    public void onListFragmentInteraction(LikeUserBean item) {

    }
}
