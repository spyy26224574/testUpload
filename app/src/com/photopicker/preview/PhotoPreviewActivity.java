package com.photopicker.preview;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.ShareUtils;
import com.example.ipcamera.application.VLCApplication;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.utils.OtherUtils;

import java.util.ArrayList;

public class PhotoPreviewActivity extends FragmentActivity implements View.OnClickListener {

    public static final String KEY_MODE = "key_mode";
    public static final String KEY_POSTION = "key_postion";
    public static final String KEY_TOTAL_LIST = "total_list";
    public static final String KEY_SELECT_LIST = "select_list";
    public static final int MODE_NOMAL = 0;
    public static final int MODE_SELECT = 1;
    public static final int MODE_NETWORK = 2;
    public static final int MODE_LOCAL = 3;
    ArrayList<String> selectedlist = new ArrayList<>();
    ArrayList<String> allList = new ArrayList<>();
    ImageViewPagerAdapter adapter;
    HackyViewPager pager;
    private int currentMode = 0;
    private int postion = 0;

    //    private View frame;
    private View left_back;
    private TextView title;
    //    private ImageView btn_right;
    private Button mCommitBtn;
    private View main_view;
    private int mMaxNum = VLCApplication.MAX_PHOTO_NUM;
    private CheckBox checkBox;
    private RelativeLayout mRlBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏
        setContentView(R.layout.activity_photo_preview);
        initview();
        init();
    }

//    public void toggleFrame(){
//        if(frame.getVisibility()==View.VISIBLE){
//            frame.setVisibility(View.GONE);
//        }else{
//            frame.setVisibility(View.VISIBLE);
//        }
//    }

    private void initview() {
        pager = (HackyViewPager) findViewById(R.id.pager);
//        frame=findViewById(R.id.frame);
        left_back = findViewById(R.id.back);
        title = (TextView) findViewById(R.id.title);
//        btn_right=(ImageView)findViewById(R.id.right_img);
        main_view = findViewById(R.id.main_view);
        checkBox = (CheckBox) findViewById(R.id.checkbox);
        mCommitBtn = (Button) findViewById(R.id.commit);
        mRlBottom = (RelativeLayout) findViewById(R.id.rl_bottom);
    }

    private void init() {
        currentMode = getIntent().getIntExtra(KEY_MODE, 0);
        postion = getIntent().getIntExtra(KEY_POSTION, 0);
        allList = getIntent().getStringArrayListExtra(KEY_TOTAL_LIST);
        mMaxNum = getIntent().getIntExtra(PhotoPickerActivity.EXTRA_MAX_MUN, mMaxNum);
        adapter = new ImageViewPagerAdapter(getSupportFragmentManager(), allList);
        pager.setAdapter(adapter);

        pager.setOnClickListener(this);
        left_back.setOnClickListener(this);
//        btn_right.setOnClickListener(this);
        main_view.setOnClickListener(this);
//        if(currentMode==MODE_LOCAL){
//            btn_right.setVisibility(View.VISIBLE);
//        }else{
//            btn_right.setVisibility(View.GONE);
//        }
        if (currentMode == MODE_SELECT) {
            checkBox.setVisibility(View.VISIBLE);
            selectedlist = getIntent().getStringArrayListExtra(KEY_SELECT_LIST);
            checkBox.setOnClickListener(this);
            checkBox.setChecked(selectedlist.contains(allList.get(postion)));
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    if (currentMode == MODE_SELECT) {
                        if (mMaxNum == selectedlist.size()) {
                            boolean contains = selectedlist.contains(allList.get(position));
                            mRlBottom.setVisibility(contains ? View.VISIBLE : View.GONE);
                        } else {
                            mRlBottom.setVisibility(View.VISIBLE);
                        }
                        checkBox.setChecked(selectedlist.contains(allList.get(position)));
                        title.setText(String.format("%d/%d", pager.getCurrentItem() + 1, allList.size()));
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            resetCommit();
            mCommitBtn.setOnClickListener(this);
        } else {
            checkBox.setVisibility(View.GONE);
        }
        pager.setCurrentItem(postion);
    }

    private void resetCommit() {
        if (mCommitBtn == null) return;
        mCommitBtn.setText(OtherUtils.formatResourceString(getApplicationContext(),
                R.string.commit_num, selectedlist.size(), mMaxNum));
        mCommitBtn.setEnabled(selectedlist.size() > 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void onBack() {
        finish();
    }

    private void returnData() {
        if (currentMode == MODE_SELECT) {
            Intent data = new Intent();
            data.putStringArrayListExtra(KEY_SELECT_LIST, selectedlist);
            setResult(RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBack();
                break;
            case R.id.main_view:

                break;
            case R.id.pager:

                break;
            case R.id.right_img:
                if (currentMode == MODE_LOCAL) {
//                    Intent share=new Intent(Intent.ACTION_SEND);
//                    share.setType("image/*");
//                    share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(allList.get(pager.getCurrentItem()))));
//                    share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(share);

//                    ArrayList<Uri> photos = new ArrayList<>();
//                    photos.add(Uri.fromFile(new File(allList.get(pager.getCurrentItem()))));
                    ArrayList<String> paths = new ArrayList<>();
                    paths.add(allList.get(pager.getCurrentItem()));
                    new ShareUtils().sharePhoto(this, paths);
                }
                break;
            case R.id.checkbox:
                int item = pager.getCurrentItem();
                if (!checkBox.isChecked()) {
                    selectedlist.remove(allList.get(item));
                } else {
                    if (!selectedlist.contains(allList.get(item)))
                        selectedlist.add(allList.get(item));
                }
                resetCommit();
//                checkBox.setChecked(!checkBox.isChecked());
                break;
            case R.id.commit:
                returnData();
                break;
        }
    }
}
