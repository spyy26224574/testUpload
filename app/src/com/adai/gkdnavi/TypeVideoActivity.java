package com.adai.gkdnavi;

import android.os.Bundle;

import com.adai.gkd.bean.square.TypeVideoBean;
import com.adai.gkdnavi.fragment.square.TypeVideoFragment;

public class TypeVideoActivity extends BaseFragmentActivity implements TypeVideoFragment.OnListFragmentInteractionListener {

    TypeVideoFragment typeVideoFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type_video);
        initView();
        init();
        int typeid=getIntent().getIntExtra("typeid",-1);
        String typename=getIntent().getStringExtra("typename");
        setTitle(typename);
        typeVideoFragment=TypeVideoFragment.newInstance(1,typeid);
        getSupportFragmentManager().beginTransaction().replace(R.id.content,typeVideoFragment).commit();

    }

    @Override
    protected void initView() {
        super.initView();
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void onListFragmentInteraction(TypeVideoBean item) {

    }
}
