package com.adai.gkdnavi;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.square.PersonalInfoBean;
import com.adai.gkd.bean.square.PersonalInfoPageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.fragment.square.ResourceGridFragment;
import com.adai.gkdnavi.fragment.square.TypeVideoRecyclerViewAdapter;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.filepicker.adapters.SectionsPagerAdapter;

import java.util.Locale;

public class PersonalPageActivity extends BaseFragmentActivity implements ResourceGridFragment.OnFragmentInteractionListener, View.OnClickListener {

    private static final int REQUST_CODE_LOGIN = 1;
    //    private ViewPager pager;
    public static final int REQUEST_CODE_FOUCS = 2;
    private static final int REQUEST_CODE_EDIT = 3;
    private SectionsPagerAdapter adapter;
    private ImageView user_logo;
    private TextView nick_name, sign, fans_count, foucs_count, collection_count;

    private int userid = 0;
    private View note_info_frame;
    private TextView login;
    private ImageView bg_head_img;
    private TextView concern;
    private ResourceGridFragment fragment;

    //    private TextView title_share,title_collection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_page);
        initView();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
//        pager=(ViewPager)findViewById(R.id.pager);
        user_logo = (ImageView) findViewById(R.id.user_logo);
        nick_name = (TextView) findViewById(R.id.nickname);
        fans_count = (TextView) findViewById(R.id.fans_count);
        foucs_count = (TextView) findViewById(R.id.foucs_count);
        note_info_frame = findViewById(R.id.note_info_frame);
        login = (TextView) findViewById(R.id.login);
//        title_share=(TextView)findViewById(R.id.title_share);
//        title_collection=(TextView)findViewById(R.id.title_collection);
        sign = (TextView) findViewById(R.id.sign);
        collection_count = (TextView) findViewById(R.id.collection_count);
        bg_head_img = (ImageView) findViewById(R.id.bg_head_img);
        concern = (TextView) findViewById(R.id.concern);
        concern.setOnClickListener(this);
        user_logo.setOnClickListener(this);
        findViewById(R.id.collection_layout).setOnClickListener(this);
        findViewById(R.id.foucs_layout).setOnClickListener(this);
        findViewById(R.id.fans_layout).setOnClickListener(this);
    }

    private void initEdit() {
        ImageView right_img = (ImageView) findViewById(R.id.right_img);
        right_img.setVisibility(View.VISIBLE);
        right_img.setImageResource(R.drawable.bg_edit_selector);
        right_img.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (VoiceManager.isLogin && CurrentUserInfo.id == userid) {
//            initEdit();
            concern.setVisibility(View.INVISIBLE);
        } else {
//            ImageView right_img = (ImageView) findViewById(R.id.right_img);
//            right_img.setVisibility(View.INVISIBLE);
            concern.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void init() {
        super.init();
        setTitle(R.string.homepage);
        userid = getIntent().getIntExtra("userid", 0);
//        adapter=new SectionsPagerAdapter(getSupportFragmentManager());
//        adapter.addFragment(ResourceGridFragment.newInstance(userid,0),getString(R.string.share));
//        adapter.addFragment(ResourceGridFragment.newInstance(userid,1),getString(R.string.collection));
//        pager.setAdapter(adapter);
//        title_share.setSelected(true);
//        title_share.setOnClickListener(this);
//        title_collection.setOnClickListener(this);
//        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
//            @Override
//            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//
//            }
//
//            @Override
//            public void onPageSelected(int position) {
//                title_collection.setSelected(false);
//                title_share.setSelected(false);
//                switch (position){
//                    case 0:
//                        title_share.setSelected(true);
//                        break;
//                    case 1:
//                        title_collection.setSelected(true);
//                        break;
//                }
//            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//
//            }
//        });
        fragment = ResourceGridFragment.newInstance(userid, 0);
        getSupportFragmentManager().beginTransaction().add(R.id.shared_content, fragment).commit();
        getDatafromServer();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getDatafromServer();
    }

    private void loadData(PersonalInfoBean data) {
        if (data == null) return;
//        ImageLoadHelper.getInstance().displayImage(data.portrait, user_logo,R.drawable.default_header_img);
//        ImageLoadHelper.getInstance().displayImage(data.portrait, bg_head_img);
        ImageLoaderUtil.getInstance().loadRoundImage(this, data.portrait, R.drawable.default_header_img, user_logo);
        ImageLoaderUtil.getInstance().loadImage(this, data.portrait, bg_head_img);
        nick_name.setText(data.nickname);
        sign.setText(data.signature);
        fans_count.setText(String.format("%d", data.fansCount));
        foucs_count.setText(String.format("%d", data.focusCount));
        collection_count.setText(String.format("%d", data.collectCount));
        if ("Y".equals(data.isFocus)) {
            concern.setSelected(true);
            concern.setText(getString(R.string.already_attention));
        } else {
            concern.setSelected(false);
            concern.setText(getString(R.string.add_attention));
        }
    }

    private void getDatafromServer() {
        RequestMethods_square.getPersonalInfo(userid, new HttpUtil.Callback<PersonalInfoPageBean>() {
            @Override
            public void onCallback(PersonalInfoPageBean result) {
                if (isFinishing()) return;
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            loadData((result.data));
                            break;
                        default:
                            showToast(result.message);
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                Intent login = new Intent(mContext, LoginActivity.class);
                startActivityForResult(login, REQUST_CODE_LOGIN);
                break;
            case R.id.user_logo:
            case R.id.right_img:
                if (VoiceManager.isLogin && CurrentUserInfo.id == userid) {
                    Intent edit = new Intent(mContext, EditPersonalforActivity.class);
                    startActivityForResult(edit, REQUEST_CODE_EDIT);
                }
                break;
            case R.id.collection_layout:
                Intent collection = new Intent(mContext, PersonalCollectionActivity.class);
                collection.putExtra("userid", userid);
                startActivity(collection);
                break;
            case R.id.foucs_layout:
                Intent foucs = new Intent(mContext, AttentionListActivity.class);
                foucs.putExtra("type", 0);
                foucs.putExtra("userid", userid);
                startActivity(foucs);
                break;
            case R.id.fans_layout:
                Intent fans = new Intent(mContext, AttentionListActivity.class);
                fans.putExtra("type", 1);
                fans.putExtra("userid", userid);
                startActivity(fans);
                break;
            case R.id.concern:
                attention(userid);
                break;
        }
    }

    @Override
    protected void goBack() {
        Intent data = getIntent();
        data.putExtra("isFoucs", concern.isSelected() ? "Y" : "N");
        setResult(RESULT_OK, data);
        super.goBack();
    }

    /**
     * 关注
     *
     * @param userid
     */
    public void attention(int userid) {
        if (checkLogin()) {
            if (concern.isSelected()) {
                RequestMethods_square.deleteAttention(userid, new HttpUtil.Callback<BasePageBean>() {
                    @Override
                    public void onCallback(BasePageBean result) {
                        if (result != null) {
                            switch (result.ret) {
                                case 0:
                                    try {
                                        Integer fansCount = Integer.valueOf(fans_count.getText().toString());
                                        fansCount--;
                                        fans_count.setText(String.format(Locale.ENGLISH, "%d", fansCount));
                                    } catch (Exception ignored) {
                                    }
                                    concern.setSelected(false);
                                    concern.setText(R.string.add_attention);
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
                                    try {
                                        Integer fansCount = Integer.valueOf(fans_count.getText().toString());
                                        fansCount++;
                                        fans_count.setText(String.format(Locale.ENGLISH, "%d", fansCount));
                                    } catch (Exception ignored) {
                                    }
                                    concern.setSelected(true);
                                    concern.setText(R.string.already_attention);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        switch (requestCode) {
            case REQUST_CODE_LOGIN:
                if (data.hasExtra("islogin") && data.getBooleanExtra("islogin", false))
                    getDatafromServer();
                break;
            case REQUEST_CODE_EDIT:
                getDatafromServer();
                fragment.onActivityResult(TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE, resultCode, data);
                break;
            default:
                fragment.onActivityResult(TypeVideoRecyclerViewAdapter.REQUEST_DELETE_CODE, resultCode, data);
                break;
        }
    }
}
