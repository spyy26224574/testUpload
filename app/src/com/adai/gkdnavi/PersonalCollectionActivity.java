package com.adai.gkdnavi;

import android.net.Uri;
import android.os.Bundle;

import com.adai.gkdnavi.fragment.square.ResourceGridFragment;

public class PersonalCollectionActivity extends BaseFragmentActivity implements ResourceGridFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_collection);
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
        setTitle(R.string.collection_list);
        int userid=getIntent().getIntExtra("userid",-1);
        getSupportFragmentManager().beginTransaction().add(R.id.content, ResourceGridFragment.newInstance(userid,1)).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
