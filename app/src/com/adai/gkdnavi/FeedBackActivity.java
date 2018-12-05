package com.adai.gkdnavi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.adapter.SharePhotoRecyclerAdapter;
import com.adai.gkdnavi.utils.StringUtils;
import com.adai.gkdnavi.utils.UISwitchButton;
import com.example.ipcamera.application.VLCApplication;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.preview.PhotoPreviewActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FeedBackActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    //    private static final int REQUEST_LOGIN_CODE = 1;
    private RecyclerView photo_grid;
    //    private SharePhotoGridAdapter adapter;
    private SharePhotoRecyclerAdapter adapter;
    private ArrayList<String> shareImages = new ArrayList<>();
    private UISwitchButton mSubmitLog, mNeedService;
    private LinearLayout mLlContactMethod;
    private EditText mEtMoblie, mEtEmail;
    private EditText mShareText;
    private Spinner type_spinner;
    private TextView log_notice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed_back);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.feedback);
        photo_grid = (RecyclerView) findViewById(R.id.photo_grid);
        type_spinner = (Spinner) findViewById(R.id.type_spinner);
        adapter = new SharePhotoRecyclerAdapter(this, 3);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setSmoothScrollbarEnabled(true);
        gridLayoutManager.setAutoMeasureEnabled(true);
        photo_grid.setLayoutManager(gridLayoutManager);
        photo_grid.setHasFixedSize(true);
        photo_grid.setNestedScrollingEnabled(false);
        photo_grid.setAdapter(adapter);
        findViewById(R.id.submit).setOnClickListener(this);
        mEtMoblie = (EditText) findViewById(R.id.et_mobile);
        mEtEmail = (EditText) findViewById(R.id.et_email);
        mShareText = (EditText) findViewById(R.id.share_text);
        mLlContactMethod = (LinearLayout) findViewById(R.id.ll_contact_method);
        mSubmitLog = (UISwitchButton) findViewById(R.id.submit_log);
        mNeedService = (UISwitchButton) findViewById(R.id.sb_need_contact);
        mSubmitLog.setOnCheckedChangeListener(this);
        mNeedService.setOnCheckedChangeListener(this);
        log_notice = (TextView) findViewById(R.id.log_notice);
    }

    @Override
    protected void init() {
        super.init();
        mSubmitLog.setChecked(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
//            case REQUEST_LOGIN_CODE:
//                if (data.getBooleanExtra("islogin", false)) {
//                    // TODO: 如果登录了就上传反馈信息,需要判断是否上传日志和联系方式
//                    checkFeedback();
//                }
//                break;
            case ShareActivity.REQUESE_SELECT_PHOTO_CODE:
                if (data != null) {
                    ArrayList<String> select = data.getStringArrayListExtra(PhotoPreviewActivity.KEY_SELECT_LIST);
                    adapter.addPhotos(select);
                }
                break;
            case ShareActivity.REQUEST_PICKPHOTO_CODE:
                if (data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(PhotoPickerActivity.KEY_RESULT);
                    adapter.addPhotos(result);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submit:
//                if (VoiceManager.isLogin) {
//                    // TODO: 登录了就上传反馈信息,需要判断是否上传日志和联系方式
                checkFeedback();
//                } else {
//                    startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN_CODE);
//                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.submit_log:
                // TODO: 需要获取最近两天的崩溃日志并上传
                log_notice.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
            case R.id.sb_need_contact:
                mLlContactMethod.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                break;
        }
    }

    private void checkFeedback() {
        String sharetext = mShareText.getText().toString();
        if (TextUtils.isEmpty(sharetext)) {
            showToast(R.string.hint_enter_feedback);
            return;
        }
        String email = mEtEmail.getText().toString();
        String phonenum = mEtMoblie.getText().toString();
        if (mNeedService.isChecked()) {
            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(phonenum)) {
                showToast(getString(R.string.enter_mobile_or_email));
                return;
            }
            if (TextUtils.isEmpty(phonenum)) {
                if (!StringUtils.checkEmail(email)) {
                    Toast.makeText(this, getResources().getString(R.string.emailerror), Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                if (TextUtils.isEmpty(email)) {
                    if (phonenum.length() <= 6) {
                        showToast(R.string.phonenum_error);
                        return;
                    }
                } else {
                    if (!StringUtils.checkEmail(email)) {
                        Toast.makeText(this, getResources().getString(R.string.emailerror), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }
        List<String> images = adapter.getPhotos();
        List<File> logs = mSubmitLog.isChecked() ? getErrorLog() : null;
        List<File> imagefiles = new ArrayList<>();
        if (images != null && images.size() > 0) {
            for (String image : images) {
                imagefiles.add(new File(image));
            }
        }
        showpDialog();
        RequestMethods_square.feedBack(type_spinner.getSelectedItemPosition(), sharetext, mSubmitLog.isChecked() ? 'Y' : 'N', logs, mNeedService.isChecked() ? 'Y' : 'N', phonenum, email, imagefiles, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            showToast(getString(R.string.commit_succeeded));
                            finish();
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
                hidepDialog();
            }
        });
    }

    private List<File> getErrorLog() {
        File file = new File(VLCApplication.LOG_PATH);
        List<File> errorlist = new ArrayList<>();
        String[] logs = file.list();
        if (logs != null && logs.length > 0) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            for (String log : logs) {
                if (log.startsWith("error")) {
                    if (log.length() >= 28) {
                        String datestr = log.substring(5, 24);
                        try {
                            Date date = format.parse(datestr);
                            long sum = System.currentTimeMillis() - date.getTime();
                            if (sum < 2 * 24 * 60 * 60 * 1000) {
                                errorlist.add(new File(VLCApplication.LOG_PATH + "/" + log));
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return errorlist;
    }
}
