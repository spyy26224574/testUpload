package com.adai.gkdnavi;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.adai.camera.FileManagerConstant;
import com.adai.camera.novatek.filemanager.NovatekFileManagerFragment;

public class FileGridActivity extends BaseActivity {
    private NovatekFileManagerFragment mNovatekFileManagerFragment;

    public static void actionStart(Context context, int columnCount, int type, String filePath) {
        Intent intent = new Intent(context, FileGridActivity.class);
        intent.putExtra("columnCount", columnCount);
        intent.putExtra("type", type);
        intent.putExtra("filePath", filePath);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_grid);
        initView();
        Intent intent = getIntent();
        String filePath = intent.getStringExtra("filePath");
        int type = intent.getIntExtra("type", FileManagerConstant.TYPE_LOCAL_PICTURE);
        int columnCount = intent.getIntExtra("columnCount", 3);
        mNovatekFileManagerFragment = NovatekFileManagerFragment.newInstance(columnCount, type, filePath);
        getSupportFragmentManager().beginTransaction().replace(R.id.content, mNovatekFileManagerFragment).commit();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.photo);
    }
}
