package com.adai.camera.mstar.setting.subsetting;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.data.MstarRepository;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MstarSubSettingActivity extends BaseActivity {
    private ListView mListView;
    private List<String> mMenuItem;
    private RadioAdapter adapter;
    private ArrayList<Integer> mKeyList;
    private int mMenuid;
    private CameraCommand.RequestListener mRequestListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {
            if (CameraCommand.checkResponse(response)) {
                showToast(R.string.set_success);
            } else {
                showToast(R.string.set_failure);
            }
            hidepDialog();
        }

        @Override
        public void onErrorResponse(String message) {
            showToast(R.string.set_failure);
            hidepDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mstar_sub_setting);
        init();
        initView();
    }

    public static void actionStart(Context context, String title, int cmdId) {
        Intent intent = new Intent(context, MstarSubSettingActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("menuid", cmdId);
        context.startActivity(intent);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void initView() {
        super.initView();
        mListView = (ListView) findViewById(R.id.listView);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        mMenuid = intent.getIntExtra("menuid", 0);
        setTitle(title);
        switch (mMenuid) {
            case MstarRepository.MENU_ID.menuEV:
                mMenuItem = Arrays.asList(MstarRepository.EV_val);
                break;
            case MstarRepository.MENU_ID.menuMTD:
                mMenuItem = Arrays.asList(MstarRepository.MTD_val);
                break;
            default:
                MstarRepository.Menu menu = MstarRepository.getInstance().GetAutoMenu(mMenuid);
                if (menu != null) {
                    mMenuItem = menu.GetMenuItemIdList();
                }
                break;
        }
        if (mMenuItem != null && mMenuItem.size() > 0) {
            mListView.setAdapter(new RadioAdapter(this, mMenuItem));
        } else {
            return;
        }

        int curPosition = 0;

        switch (mMenuid) {
            case MstarRepository.MENU_ID.menuEV:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getEVRet());
                break;
            case MstarRepository.MENU_ID.menuMTD:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getMTDRet());
                break;
            case MstarRepository.MENU_ID.menuIMAGE_RES:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getImageresRet());
                break;
            case MstarRepository.MENU_ID.menuVIDEO_RES:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getVideoresRet());
                break;
            case MstarRepository.MENU_ID.menuWHITE_BALANCE:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getAWBRet());
                break;
            case MstarRepository.MENU_ID.menuGST:
                curPosition = MstarRepository.getInstance().AutoMenuCheck(mMenuid, MstarRepository.getInstance().getGsensorRet());
                break;
            default:
                break;
        }
        if (curPosition >= 0) {
            mListView.performItemClick(mListView.getChildAt(curPosition), curPosition, mListView.getItemIdAtPosition(curPosition));
        }

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                showpDialog();
                switch (mMenuid) {
                    case MstarRepository.MENU_ID.menuMTD:
                        CameraCommand.asynSendRequest(CameraCommand.commandSetmotiondetectionUrl(arg2, null), mRequestListener);
                        break;
                    case MstarRepository.MENU_ID.menuEV:
                        CameraCommand.asynSendRequest(CameraCommand.commandSetEVUrl(arg2), mRequestListener);
                        break;
                    default:
                        CameraCommand.asynSendRequest(CameraCommand.commandSetUrl(mMenuid, mMenuItem.get(arg2)), mRequestListener);
                        break;
                }
            }
        });
    }

    private class RadioAdapter extends BaseAdapter {

        private List<String> current_array;
        private Context context;

        public RadioAdapter(Context context, List<String> authors) {
            super();
            this.context = context;
            this.current_array = authors;
        }

        @Override
        public int getCount() {
            return current_array.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {

            ChoiceListItemView choiceListItemView = new ChoiceListItemView(context, null);
            choiceListItemView.setName(current_array.get(arg0));
//            if (key == current_value) {
//                choiceListItemView.setChecked(true);
//            }
            return choiceListItemView;
        }
    }

    public class ChoiceListItemView extends LinearLayout implements Checkable {

        private TextView tv_sub;
        private CheckBox cb_sub;

        public ChoiceListItemView(Context context, AttributeSet attrs) {
            super(context, attrs);

            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.dv_sub_item, this, true);
            tv_sub = (TextView) v.findViewById(R.id.tv_sub_item);
            cb_sub = (CheckBox) v.findViewById(R.id.cb_sub_item);
        }

        public void setName(String text) {
            tv_sub.setText(text);
        }

        @Override
        public boolean isChecked() {
            return cb_sub.isChecked();
        }

        @Override
        public void setChecked(boolean checked) {
            cb_sub.setChecked(checked);
            //根据是否选中来选择不同的背景图片
            if (checked) {
                cb_sub.setBackgroundResource(R.drawable.bg_sub_checked);
            } else {
                cb_sub.setBackgroundResource(R.drawable.bg_sub_nomal);
            }
        }

        @Override
        public void toggle() {
            cb_sub.toggle();
        }

    }
}
