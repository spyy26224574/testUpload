package com.adai.camera.novatek.settting.subsetting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
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

import com.adai.camera.novatek.data.NovatekRepository;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.example.ipcamera.domain.MovieRecord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NovatekSubSettingActivity extends BaseActivity {
    private ListView mListView;
    private SparseArray<String> mMenuItem;
    private RadioAdapter adapter;
    private ArrayList<Integer> mKeyList;
    private int mCurStateId;
    protected static final int START = 0;
    protected static final int SUCCESS = 1;
    protected static final int FAIL = 2;

    public static void actionStart(Context context, String title, int cmdId) {
        Intent intent = new Intent(context, NovatekSubSettingActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("cmdId", cmdId);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novatek_sub_setting);
        init();
        initView();
    }

    @Override
    protected void initView() {
        super.initView();
        mListView = (ListView) findViewById(R.id.listView);
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        final int cmdId = intent.getIntExtra("cmdId", 0);
        setTitle(title);
        mMenuItem = NovatekRepository.getInstance().getMenuItem(cmdId);
        mKeyList = new ArrayList<>();
        for (int i = 0; i < mMenuItem.size(); i++) {
            mKeyList.add(mMenuItem.keyAt(i));
        }
        Collections.sort(mKeyList);
        String curStateId = NovatekRepository.getInstance().getCurStateId(cmdId);
        if (TextUtils.isEmpty(curStateId)) {
            mCurStateId = 0;
        } else {
            mCurStateId = Integer.valueOf(curStateId);
        }
        adapter = new RadioAdapter(this, mMenuItem);
        mListView.setAdapter(adapter);
        int curPosition = mKeyList.indexOf(mCurStateId);
        mListView.performItemClick(mListView.getChildAt(curPosition), curPosition, mListView.getItemIdAtPosition(curPosition));

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
//                Toast.makeText(CameraSubSetActivity.this,
//                        "您选择的是：" + mSubMenuList.get(arg2), Toast.LENGTH_SHORT).show();
                mCurStateId = (int) arg1.getTag();
                changeStatus(cmdId, mCurStateId);
                nHandler.sendEmptyMessage(START);

            }
        });
    }

    private void changeStatus(int cmdId, int curStateId) {
        switch (cmdId) {
//            case NovatekWifiCommands.MOVIE_SET_RECORD_SIZE:
//                CameraUtils.sendImmediateCmd(Contacts.BASE_URL + cmdId + "&par=" + curStateId, new CameraUtils.ImmediateCmdListener() {
//                    @Override
//                    public boolean onResponse(String response) {
//                        boolean flag;
//                        try {
//                            InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                            DomParseUtils domParseUtils = new DomParseUtils();
//                            MovieRecord record = domParseUtils.getParserXml(is);
//                            flag = record != null && record.getStatus().equals("0");
//                        } catch (UnsupportedEncodingException e) {
//                            flag = false;
//                            e.printStackTrace();
//                        }
//                        if (flag) {
//                            nHandler.sendEmptyMessageDelayed(SUCCESS, 500);
//                        } else {
//                            nHandler.sendEmptyMessageDelayed(FAIL, 500);
//                        }
//                        return flag;
//                    }
//
//                    @Override
//                    public void onStreamRefreshed(boolean isRefreshed) {
//
//                    }
//
//                    @Override
//                    public void onErrorResponse(Exception e) {
//                        nHandler.sendEmptyMessageDelayed(FAIL, 500);
//                    }
//                });
//                break;
            default:
//                CameraUtils.sendCmd(Contacts.BASE_URL + cmdId + "&par=" + curStateId, new CameraUtils.CmdListener() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                            DomParseUtils domParseUtils = new DomParseUtils();
//                            MovieRecord record = domParseUtils.getParserXml(is);
//                            if (record != null && record.getStatus().equals("0")) {
//                                nHandler.sendEmptyMessage(SUCCESS);
//                            } else { //返回状态不为0
//                                nHandler.sendEmptyMessage(FAIL);
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            nHandler.sendEmptyMessage(FAIL);
//                        }
//                    }
//
//                    @Override
//                    public void onErrorResponse(Exception volleyError) {
//                        nHandler.sendEmptyMessage(FAIL);
//                    }
//                });
                if (!CameraUtils.isRecording || !CameraUtils.hasSDCard || CameraUtils.CURRENT_MODE == CameraUtils.MODE_PHOTO) {
                    CameraUtils.sendCmd(cmdId, String.valueOf(curStateId), new CameraUtils.CmdCallback() {
                        @Override
                        public void success(int commandId, String par, MovieRecord movieRecord) {
                            NovatekRepository.getInstance().setCurStateId(commandId, par);
                            nHandler.sendEmptyMessage(SUCCESS);
                        }

                        @Override
                        public void failed(int commandId, String par, String error) {
                            nHandler.sendEmptyMessage(FAIL);
                        }
                    });

                } else {
                    CameraUtils.sendAutoToggleRecordCmd(cmdId, String.valueOf(curStateId), new CameraUtils.CmdCallback() {
                        @Override
                        public void success(int commandId, String par, MovieRecord movieRecord) {
                            NovatekRepository.getInstance().setCurStateId(commandId, par);
                            nHandler.sendEmptyMessage(SUCCESS);
                        }

                        @Override
                        public void failed(int commandId, String par, String error) {
                            nHandler.sendEmptyMessage(FAIL);
                        }
                    });
                }
                break;

        }
    }

    @SuppressLint("HandlerLeak")
    private Handler nHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START: // 发送命令
                    showpDialog(R.string.set_status);
                    break;
                case SUCCESS:// 设置成功
                    hidepDialog();
                    showToast(R.string.set_success);

                    break;
                case FAIL://  设置失败
                    hidepDialog();
                    showToast(R.string.set_failure);
                    break;
                default:
                    break;
            }
        }
    };

    public class RadioAdapter extends BaseAdapter {

        private SparseArray<String> current_array;
        private List<Integer> keyList;
        private Context context;

        public RadioAdapter(Context context, SparseArray<String> authors) {
            super();
            this.context = context;
            this.current_array = authors;
            keyList = new ArrayList<>();
            for (int i = 0; i < authors.size(); i++) {
                keyList.add(authors.keyAt(i));
            }
            Collections.sort(keyList);
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
            Integer key = keyList.get(arg0);
            choiceListItemView.setName(current_array.get(key));
            choiceListItemView.setTag(key);
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
