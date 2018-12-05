package com.adai.camera.hisi.setting;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
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

import com.adai.camera.CameraFactory;
import com.adai.camera.hisi.HisiCamera;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.CONFIG_PHOTO_SCENE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_LOOP_TYPE;
import static com.adai.camera.hisi.sdk.Common.CONFIG_VIDEO_VIDEO_RESOLUTION;
import static com.adai.camera.hisi.sdk.Common.FAILURE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_PHOTO_SINGLE;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_LOOP;
import static com.adai.camera.hisi.sdk.Common.WORK_MODE_VIDEO_NORMAL;

public class HisiSubSettingActivity extends BaseActivity {
    private ListView mListView;
    private RadioAdapter mRadioAdapter;
    private List<String> mSubMenuList = new ArrayList<>();
    private int mWorkmode;
    private int mConfigID;
    private int curIndex = -1;

    public static final int TYPE_WORK_MODE_TYPE = 0;
    public static final int TYPE_SCREEN_SLEEP = 1;
    private int mType;
    private HisiCamera mHisiCamera;
    private int[] mScreenSleepValues;

    public static void startAction(Context context, int type, String title, int workmode, int configID) {
        Intent intent = new Intent(context, HisiSubSettingActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("type", type);
        intent.putExtra("workmode", workmode);
        intent.putExtra("configID", configID);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hisi_sub_setting);
        init();
        initView();
        initEvent();
    }

    @Override
    protected void init() {
        super.init();
        mHisiCamera = CameraFactory.getInstance().getHisiCamera();
        Intent intent = getIntent();
        mType = intent.getIntExtra("type", 0);
        mWorkmode = intent.getIntExtra("workmode", -1);
        mConfigID = intent.getIntExtra("configID", -1);
        switch (mType) {
            case TYPE_WORK_MODE_TYPE:
                String entries = "";
                String value = "";
                switch (mWorkmode) {
                    case WORK_MODE_VIDEO_NORMAL:
                        switch (mConfigID) {
                            case CONFIG_VIDEO_VIDEO_RESOLUTION:
                                entries = mHisiCamera.modeConfig.videoNormalResolutionValues;
                                value = mHisiCamera.modeConfig.videoNormalResolution;
                                break;
                            default:
                                break;
                        }
                        break;
                    case WORK_MODE_PHOTO_SINGLE:
                        switch (mConfigID) {
                            case CONFIG_PHOTO_RESOLUTION:
                                entries = mHisiCamera.modeConfig.photoSingleResolutionValues;
                                value = mHisiCamera.modeConfig.photoSingleResolution;
                                break;
                            default:
                                break;
                        }
                        break;
                    case WORK_MODE_VIDEO_LOOP:
                        switch (mConfigID) {
                            case CONFIG_VIDEO_LOOP_TYPE:
                                entries = mHisiCamera.modeConfig.videoLoopTypeValues;
                                value = mHisiCamera.modeConfig.videoLoopType;
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        break;
                }
                String[] array = entries.split(",");
                mSubMenuList.addAll(Arrays.asList(array));
                curIndex = mSubMenuList.indexOf(value);
                break;
            case TYPE_SCREEN_SLEEP:
                String[] screenSleepEntries = getResources().getStringArray(R.array.screen_auto_sleep_entries);
                mScreenSleepValues = getResources().getIntArray(R.array.screen_auto_sleep_values);
                mSubMenuList.addAll(Arrays.asList(screenSleepEntries));
                for (int i = 0; i < mScreenSleepValues.length; i++) {
                    if (mScreenSleepValues[i] == mHisiCamera.prefer.screenAutoSleep) {
                        curIndex = i;
                        break;
                    }
                }
                break;
            default:
                break;
        }

    }

    @Override
    protected void initView() {
        super.initView();
        String title = getIntent().getStringExtra("title");
        setTitle(title);
        mListView = (ListView) findViewById(R.id.listView);
        mRadioAdapter = new RadioAdapter(this, mSubMenuList);
        mListView.setAdapter(mRadioAdapter);
        mListView.performItemClick(mListView.getChildAt(curIndex), curIndex, 0);

    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExecuteCmdTask executeCmdTask = new ExecuteCmdTask();
                executeCmdTask.execute(position);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ExecuteCmdTask extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showpDialog();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            switch (mType) {
                case TYPE_WORK_MODE_TYPE:
                    String value = mSubMenuList.get(integers[0]);
                    int setParameterRet = mHisiCamera.setParameter(mWorkmode, mConfigID, value);
                    if (FAILURE != setParameterRet) {
                        switch (mWorkmode) {
                            case WORK_MODE_VIDEO_NORMAL:
                                switch (mConfigID) {
                                    case CONFIG_VIDEO_VIDEO_RESOLUTION:
                                        mHisiCamera.modeConfig.videoNormalResolution = value;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case WORK_MODE_VIDEO_LOOP:
                                switch (mConfigID) {
                                    case CONFIG_VIDEO_VIDEO_RESOLUTION:
                                        mHisiCamera.modeConfig.videoLoopResolution = value;
                                        break;
                                    case CONFIG_VIDEO_LOOP_TYPE:
                                        mHisiCamera.modeConfig.videoLoopType = value;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case WORK_MODE_PHOTO_SINGLE:
                                switch (mConfigID) {
                                    case CONFIG_PHOTO_RESOLUTION:
                                        mHisiCamera.modeConfig.photoSingleResolution = value;
                                        break;
                                    case CONFIG_PHOTO_SCENE:
                                        mHisiCamera.modeConfig.photoSingleScene = value;
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    return setParameterRet;
                case TYPE_SCREEN_SLEEP:
                    int setScreenAutoSleepRet = mHisiCamera.setScreenAutoSleep(mScreenSleepValues[integers[0]]);
                    if (FAILURE != setScreenAutoSleepRet) {
                        mHisiCamera.prefer.screenAutoSleep = mScreenSleepValues[integers[0]];
                    }
                    return setScreenAutoSleepRet;
                default:
                    return FAILURE;
            }
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            hidepDialog();
            if (FAILURE == integer) {
                showToast(R.string.set_failure);
            } else {
                showToast(R.string.set_success);
            }
        }
    }

    private class RadioAdapter extends BaseAdapter {

        private List<String> current_array;
        private Context context;

        RadioAdapter(Context context, List<String> authors) {
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
            return arg0;
        }

        @Override
        public View getView(int arg0, View arg1, ViewGroup arg2) {
            ChoiceListItemView choiceListItemView = new ChoiceListItemView(context, null);
            choiceListItemView.setName(current_array.get(arg0));
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
