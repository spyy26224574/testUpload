package com.adai.camera.sunplus.setting;

import android.content.Context;
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
import com.adai.camera.product.ISunplusCamera;
import com.adai.camera.sunplus.SDKAPI.CameraProperties;
import com.adai.gkdnavi.BaseActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * @author huangxy
 */
public class SunplusSubSettingActivity extends BaseActivity {
    private ListView mListView;
    public static final String CURRENT_TYPE = "current_type";
    public static final String TEXT_TITLE = "text_title";
    //视频设置子选项当前选项
    static final public int VIDEO_RESOLUTION = 0;
    static final public int TIME_LAPSE_VIDEO = 1;
    static final public int SLOW_PHOTOGRAPHY = 2;
    static final public int LOOP_RECORDING = 3;
    static final public int LIGHT_SOURCE_FREQUENCY = 4;
    //图片设置子选项当前选项
    static final public int PHOTO_RESOLUTION = 5;
    static final public int TIME_TAKING_PICTURES = 6;
    static final public int CONTINUOUS_SHOOTING = 7;
    static final public int IMAGE_QUALITY = 8;
    //设置子选项当前选项
    static final public int EXPOSURE_COMPENSATION = 9;
    static final public int WHITE_BALANCE = 10;
    static final public int COLOR = 11;
    static final public int IMAGING_FIELD = 12;
    static final public int ISO = 13;
    static final public int SHARPNESS = 14;
    //系统设置子选项当前选项
    static final public int SCREEN_PROTECTOR = 15;
    static final public int TIMED_SHUTDOWN = 16;
    static final public int TV_FORMAT = 17;
    static final public int SET_TIME = 18;
    static final public int CAMERA_LANGUAGE = 19;
    private int curType = 0;
    private ISunplusCamera mSunplusCamera;
    private CameraProperties mCameraProperties;
    private RadioAdapter mRadioAdapter;
    private List<String> mSubMenuList = new ArrayList<>();
    private int curIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sunplus_sub_setting);
        initData();
        initView();
        initEvent();
    }


    private void initData() {
        curType = getIntent().getIntExtra(CURRENT_TYPE, VIDEO_RESOLUTION);
        mSunplusCamera = CameraFactory.getInstance().getSunplusCamera();
        mCameraProperties = CameraProperties.getInstance();
        switch (curType) {
            case VIDEO_RESOLUTION:
                List<String> videoSizes = mSunplusCamera.getVideoSize().getValueList();
                String currentVideoSize = mSunplusCamera.getVideoSize().getCurrentValue();
                for (int i = 0; i < videoSizes.size(); i++) {
                    if (currentVideoSize.equals(videoSizes.get(i))) {
                        curIndex = i;
                    }
                    mSubMenuList.add(videoSizes.get(i));
                }
                break;
            case TIME_LAPSE_VIDEO:
                break;
            case SLOW_PHOTOGRAPHY:
                break;
            case LOOP_RECORDING:
                break;
            case LIGHT_SOURCE_FREQUENCY:
                String[] electricityFrequencies = mSunplusCamera.getElectricityFrequency().getValueList();
                String currentElectricityFrequency = mSunplusCamera.getElectricityFrequency().getCurrentUiStringInSetting();
                for (int i = 0; i < electricityFrequencies.length; i++) {
                    if (currentElectricityFrequency.equals(electricityFrequencies[i])) {
                        curIndex = i;
                    }
                    mSubMenuList.add(electricityFrequencies[i]);
                }
                break;
            case PHOTO_RESOLUTION:
                String[] imageSizes = mSunplusCamera.getImageSize().getValueArrayString();
                String currentImageSize = mSunplusCamera.getImageSize().getCurrentUiStringInSetting();
                for (int i = 0; i < imageSizes.length; i++) {
                    if (currentImageSize.equals(imageSizes[i])) {
                        curIndex = i;
                    }
                    mSubMenuList.add(imageSizes[i]);
                }
                break;
            case TIME_TAKING_PICTURES:
                String currentCaptureDelay = mSunplusCamera.getCaptureDelay().getCurrentUiStringInPreview();
                String[] captureDelays = mSunplusCamera.getCaptureDelay().getValueList();
                for (int i = 0; i < captureDelays.length; i++) {
                    if (currentCaptureDelay.equals(captureDelays[i])) {
                        curIndex = i;
                    }
                    mSubMenuList.add(captureDelays[i]);
                }
                break;
            case CONTINUOUS_SHOOTING:
                String[] bursts = mSunplusCamera.getBurst().getValueList();
                String burst = mSunplusCamera.getBurst().getCurrentUiStringInSetting();
                for (int i = 0; i < bursts.length; i++) {
                    if (burst.equals(bursts[i])) {
                        curIndex = i;
                    }
                    mSubMenuList.add(bursts[i]);
                }
                break;
            case IMAGE_QUALITY:
                break;
            case EXPOSURE_COMPENSATION:

                break;
            case WHITE_BALANCE:
                String[] whiteBalances = mSunplusCamera.getWhiteBalance().getValueList();
                String whiteBalance = mSunplusCamera.getWhiteBalance().getCurrentUiStringInSetting();
                for (int i = 0; i < whiteBalances.length; i++) {
                    if (whiteBalance.equals(whiteBalances[i])) {
                        curIndex = i;
                    }
                    mSubMenuList.add(whiteBalances[i]);
                }
                break;
            case COLOR:
                break;
            case IMAGING_FIELD:
                break;
            case ISO:
                break;
            case SHARPNESS:
                break;
            case SCREEN_PROTECTOR:
                break;
            case TIMED_SHUTDOWN:
                break;
            case TV_FORMAT:
                break;
            case SET_TIME:
                break;
            case CAMERA_LANGUAGE:
                break;
            default:
                break;
        }
        mRadioAdapter = new RadioAdapter(this, mSubMenuList);
    }

    @Override
    protected void initView() {
        super.initView();
        String title = getIntent().getStringExtra(TEXT_TITLE);
        setTitle(title == null ? "" : title);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(mRadioAdapter);
        mListView.performItemClick(mListView.getChildAt(curIndex), curIndex, 0);
    }

    private void initEvent() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                changeStatus(position);
            }
        });
    }

    private void changeStatus(int position) {
        boolean ret = false;
        switch (curType) {
            case VIDEO_RESOLUTION:
                List<String> videoSizes = mSunplusCamera.getVideoSize().getValueList();
                ret = mCameraProperties.setVideoSize(videoSizes.get(position));
                mCameraProperties.getRecordingRemainTime();//没有这句开启录制后会导致机器死机
                break;
            case TIME_LAPSE_VIDEO:
                break;
            case SLOW_PHOTOGRAPHY:
                break;
            case LOOP_RECORDING:
                break;
            case LIGHT_SOURCE_FREQUENCY:
                ret = mSunplusCamera.getElectricityFrequency().setValueByPosition(position);
                break;
            case PHOTO_RESOLUTION:
                ret = mSunplusCamera.getImageSize().setValueByPosition(position);
                break;
            case TIME_TAKING_PICTURES:
                ret = mSunplusCamera.getCaptureDelay().setValueByPosition(position);
                break;
            case CONTINUOUS_SHOOTING:
                ret = mSunplusCamera.getBurst().setValueByPosition(position);
                break;
            case IMAGE_QUALITY:
                break;
            case EXPOSURE_COMPENSATION:

                break;
            case WHITE_BALANCE:
                ret = mSunplusCamera.getWhiteBalance().setValueByPosition(position);
                break;
            case COLOR:
                break;
            case IMAGING_FIELD:
                break;
            case ISO:
                break;
            case SHARPNESS:
                break;
            case SCREEN_PROTECTOR:
                break;
            case TIMED_SHUTDOWN:
                break;
            case TV_FORMAT:
                break;
            case SET_TIME:
                break;
            case CAMERA_LANGUAGE:
                break;
            default:
                break;
        }
        ToastUtil.showShortToast(this, ret ? getString(R.string.set_success) : getString(R.string.set_failure));
    }

    public class RadioAdapter extends BaseAdapter {

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
