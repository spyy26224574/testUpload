package com.adai.gkdnavi.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adai.camera.CameraConstant;
import com.adai.gkd.bean.request.CameraVersionBean;
import com.adai.gkd.bean.square.PersonalInfoBean;
import com.adai.gkd.bean.square.PersonalInfoPageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.AttentionListActivity;
import com.adai.gkdnavi.EditPersonalforActivity;
import com.adai.gkdnavi.EditVideoActivity;
import com.adai.gkdnavi.FeedBackActivity;
import com.adai.gkdnavi.GuanyuActivity;
import com.adai.gkdnavi.LoginActivity;
import com.adai.gkdnavi.MySharedActivity;
import com.adai.gkdnavi.PersonalCollectionActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.UserAgreementActivity;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/10/26 17:26
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class MineFragment extends BaseFragment implements View.OnClickListener {
    private ImageView headimg;
    private TextView nickname;
    private TextView sign;
    private LinearLayout logedlayout;
    private TextView loginregister;
    private ImageView loginregisterimage;
    private LinearLayout loginlayout;
    private LinearLayout ll_version_info;
    private LinearLayout ll_feedback;
    private LinearLayout ll_software_protocol;
    private TextView fans_count, foucs_count, collection_count;
    private LinearLayout ll_collection;

    private TextView localbanbennumber, shexiangtounumber;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_main_self, container, false);
        //guanyu = (LinearLayout) rootView.findViewById(R.id.guanyu);
        loginlayout = (LinearLayout) rootView.findViewById(R.id.login_layout);
        loginregister = (TextView) rootView.findViewById(R.id.login_register);
        loginregisterimage = (ImageView) rootView.findViewById(R.id.login_register_image);
        logedlayout = (LinearLayout) rootView.findViewById(R.id.loged_layout);
        sign = (TextView) rootView.findViewById(R.id.sign);
        nickname = (TextView) rootView.findViewById(R.id.nickname);
        headimg = (ImageView) rootView.findViewById(R.id.head_img);
        ll_version_info = (LinearLayout) rootView.findViewById(R.id.ll_version_info);
        ll_feedback = (LinearLayout) rootView.findViewById(R.id.ll_feedback);
        ll_software_protocol = (LinearLayout) rootView.findViewById(R.id.ll_software_protocol);
        fans_count = (TextView) rootView.findViewById(R.id.fans_count);
        foucs_count = (TextView) rootView.findViewById(R.id.foucs_count);
        collection_count = (TextView) rootView.findViewById(R.id.collection_count);
        ll_collection = (LinearLayout) rootView.findViewById(R.id.ll_collection);
        rootView.findViewById(R.id.collection_layout).setOnClickListener(this);
        rootView.findViewById(R.id.foucs_layout).setOnClickListener(this);
        rootView.findViewById(R.id.fans_layout).setOnClickListener(this);
        rootView.findViewById(R.id.my_share).setOnClickListener(this);
        rootView.findViewById(R.id.ll_myreport).setOnClickListener(this);

        shexiangtounumber = (TextView) rootView.findViewById(R.id.shexiangtounumber);
        localbanbennumber = (TextView) rootView.findViewById(R.id.localbanbennumber);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    protected void init() {
        super.init();
        //guanyu.setOnClickListener(this);
        loginregister.setOnClickListener(this);
        loginregisterimage.setOnClickListener(this);
        logedlayout.setOnClickListener(this);
        ll_version_info.setOnClickListener(this);
        ll_feedback.setOnClickListener(this);
        ll_software_protocol.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (VoiceManager.isLogin) {
            logedlayout.setVisibility(View.VISIBLE);
            loginlayout.setVisibility(View.GONE);
            nickname.setText(CurrentUserInfo.nickname);
            if (CurrentUserInfo.signature == null) {
                sign.setVisibility(View.GONE);
            } else {
                sign.setVisibility(View.VISIBLE);
                sign.setText(CurrentUserInfo.signature);
            }
            Log.e("9527", "id = " + CurrentUserInfo.id);
            Log.e("9527", "username = " + CurrentUserInfo.username);
            Log.e("9527", "nickname = " + CurrentUserInfo.nickname);
            Log.e("9527", "signature = " + CurrentUserInfo.signature);
            Log.e("9527", "portrait = " + CurrentUserInfo.portrait);

            if (CurrentUserInfo.portrait != null) {
//                ImageLoadHelper.getInstance().displayImage(CurrentUserInfo.portrait, headimg,R.drawable.default_header_img);
                ImageLoaderUtil.getInstance().loadImage(getContext(), CurrentUserInfo.portrait, R.drawable.default_header_img, headimg);
            } else {
//                ImageLoadHelper.getInstance().displayImage("",headimg,R.drawable.default_header_img);
                ImageLoaderUtil.getInstance().loadImage(getContext(), "", R.drawable.default_header_img, headimg);
            }
            ll_collection.setVisibility(View.VISIBLE);
            getDatafromServer();
        } else {
            ll_collection.setVisibility(View.GONE);
            logedlayout.setVisibility(View.GONE);
            loginlayout.setVisibility(View.VISIBLE);
        }

        String cameraVersion = SpUtils.getString(getActivity(), CameraConstant.CAMERA_FIRMWARE_VERSION, "");
        shexiangtounumber.setText(cameraVersion);

        SharedPreferences gspOTACont = getActivity().getSharedPreferences("gspOta", Context.MODE_PRIVATE);
        String strLocalVer = gspOTACont.getString("gspLocalVerNo", "");//版本号
        localbanbennumber.setText(strLocalVer);


    }

    private void getDatafromServer() {
        RequestMethods_square.getPersonalInfo(CurrentUserInfo.id, new HttpUtil.Callback<PersonalInfoPageBean>() {
            @Override
            public void onCallback(PersonalInfoPageBean result) {
                if (result != null) {
                    switch (result.ret) {
                        case 0:
                            loadData((result.data));
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void loadData(PersonalInfoBean data) {
        if (data == null) return;
        fans_count.setText(String.format("%d", data.fansCount));
        foucs_count.setText(String.format("%d", data.focusCount));
        collection_count.setText(String.format("%d", data.collectCount));
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.collection_layout:
                if (VoiceManager.isLogin) {
                    Intent collection = new Intent(mContext, PersonalCollectionActivity.class);
                    collection.putExtra("userid", CurrentUserInfo.id);
                    startActivity(collection);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.foucs_layout:
                if (VoiceManager.isLogin) {
                    Intent foucs = new Intent(mContext, AttentionListActivity.class);
                    foucs.putExtra("type", 0);
                    foucs.putExtra("userid", CurrentUserInfo.id);
                    startActivity(foucs);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.fans_layout:
                if (VoiceManager.isLogin) {
                    Intent fans = new Intent(mContext, AttentionListActivity.class);
                    fans.putExtra("type", 1);
                    fans.putExtra("userid", CurrentUserInfo.id);
                    startActivity(fans);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.my_share:
                if (VoiceManager.isLogin) {
                    Intent myShare = new Intent(mContext, MySharedActivity.class);
                    myShare.putExtra("userid", CurrentUserInfo.id);
                    startActivity(myShare);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.ll_myreport:
                if (VoiceManager.isLogin) {
                    Intent myreport = new Intent(getActivity(), MySharedActivity.class);
                    myreport.putExtra("userid", CurrentUserInfo.id);
                    myreport.putExtra("title", getString(R.string.myreport));
                    myreport.putExtra("type", 2);
                    startActivity(myreport);
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.login_register:
            case R.id.login_register_image:
                Intent login = new Intent(mContext, LoginActivity.class);
                startActivity(login);
                break;
            case R.id.loged_layout:
                Intent edit = new Intent(mContext, EditPersonalforActivity.class);
                startActivity(edit);
//                logout();
                break;
            case R.id.ll_version_info:
                Intent intent_version_info = new Intent(mContext,
                        GuanyuActivity.class);
                startActivity(intent_version_info);
                break;
            case R.id.ll_feedback:
                Intent intent_feedback = new Intent(mContext,
                        FeedBackActivity.class);
                startActivity(intent_feedback);
                break;
            case R.id.ll_software_protocol:
                Intent intent_software_protocol = new Intent(mContext,
                        UserAgreementActivity.class);
                startActivity(intent_software_protocol);
                break;
            default:
                break;
        }
    }
}
