package com.adai.gkdnavi;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.adai.gkdnavi.fragment.square.ResourceGridFragment;

public class MySharedActivity extends BaseActivity implements ResourceGridFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_shared);
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
        String title = getIntent().getStringExtra("title");
        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.my_share);
        }
        setTitle(title);
        int type = getIntent().getIntExtra("type", 0);
        int userid = getIntent().getIntExtra("userid", 0);
        ResourceGridFragment fragment = ResourceGridFragment.newInstance(userid, type);
        getSupportFragmentManager().beginTransaction().add(R.id.shared_content, fragment).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
