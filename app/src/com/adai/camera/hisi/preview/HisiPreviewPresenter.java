package com.adai.camera.hisi.preview;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import com.adai.camera.CameraFactory;
import com.adai.camera.hisi.HisiCamera;
import com.adai.camera.hisi.net.MessageService;
import com.adai.camera.hisi.sdk.Command;
import com.adai.camera.hisi.sdk.Common;
import com.adai.camera.hisi.sdk.SyncMessageManager;
import com.adai.camera.hisi.sdk.SyncStateMessage;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.UIUtils;
import com.example.ipcamera.application.VLCApplication;

import static com.adai.camera.hisi.sdk.Common.*;

/**
 * @author huangxy
 * @date 2017/11/20 14:42.
 */

public class HisiPreviewPresenter extends HisiPreviewContract.Presenter {
    private boolean isPortrait = true;
    private Context mContext;
    private MessageReceiver messageReceiver;
    private int retryCount;
    private Handler mHandler = new Handler();

    @Override
    public void init() {
        mContext = mView.getAttachedContext();
    }

    @Override
    public void initOrientation() {
        Configuration configuration = VLCApplication.getAppContext().getResources().getConfiguration();
        isPortrait = configuration.orientation != Configuration.ORIENTATION_LANDSCAPE;
        mView.changeOrientation(isPortrait);
    }

    @Override
    public void connectSocket() {
        mView.initPlayView();
        mContext.startService(new Intent(mContext, MessageService.class));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int layoutDirection = newConfig.orientation;
        isPortrait = layoutDirection != Configuration.ORIENTATION_LANDSCAPE;
        mView.changeOrientation(isPortrait);
        mView.pictureVisible(isPhotoMode() || isPortrait);
    }

    @Override
    public void onStart() {
        if (messageReceiver == null) {
            messageReceiver = new MessageReceiver();
            mContext.registerReceiver(messageReceiver, new IntentFilter(MessageService.MESSAGE_ACTION));
        }
        UpdateStateTask updateStateTask = new UpdateStateTask();
        updateStateTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class UpdateStateTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
            if (null == dv.getLedState()) {
                //CGI不通，后面的不用做了
                return null;
            }
            //同步时间
            dv.setPhoneTime2Camera();

            //获取SD卡状态
            dv.getSdCardInfo();

//            //获取电量百分比
//            dv.getBatteryInfo();

            dv.loadRemotePreferences();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mView.eventEnable(true);
            updateUIByModeAndWorkState();
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onRestart() {
        GetDeviceAttrTask getDeviceAttrTask = new GetDeviceAttrTask();
        getDeviceAttrTask.execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class GetDeviceAttrTask extends AsyncTask<Void, String, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.showLoading();
        }

        @Override
        protected Integer doInBackground(Void... args) {
            return CameraFactory.getInstance().getHisiCamera().getDeviceAttr(new Common.DeviceAttr());
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (FAILURE == integer) {
                mView.hideLoading();
                mView.showToast(R.string.camera_disconnect);
                mView.exit();
            } else {
                mView.initPlayView();
            }
        }
    }

    @Override
    public void onStop() {
        if (messageReceiver != null) {
            mContext.unregisterReceiver(messageReceiver);
            messageReceiver = null;
        }
    }

    /**
     * 接收DV主动发过来的消息
     */
    class MessageReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String data = intent.getStringExtra("data");
            Message msg = SyncMessageManager.parseSyncMessage(data);
            if (msg == null) {
                return;
            }

            switch (msg.what) {
                case SyncMessageManager.MSG_SYNC_STATE:
                    processMsgSyncState((SyncStateMessage) msg.obj);
                    break;

                case SyncMessageManager.MSG_SYNC_SETTING:
                    //预留消息类型
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 处理状态同步消息
     *
     * @param stateMessage
     */
    private void processMsgSyncState(SyncStateMessage stateMessage) {
        int mode = stateMessage.mode;
        int event = stateMessage.event;
        int time = stateMessage.pasttime;
        LogUtils.e(String.format("SyncMsg: {mode:%d, state(cmd):%d, event:%d, time:%d}", mode, stateMessage.state, event, time));
        //将远端的STATE转换为本地STATE表示方式
        int state;
        switch (stateMessage.state) {
            case SyncMessageManager.REMOTE_STATE_START_LOOPRECORD:
                state = WORK_STATE_VIDEO_LOOP;
                break;
            case SyncMessageManager.REMOTE_STATE_START_RECORD:
                state = WORK_STATE_RECORD;
                break;
            case SyncMessageManager.REMOTE_STATE_START_TIMELAPSE:
                state = WORK_STATE_TIMELAPSE;
                break;
            case SyncMessageManager.REMOTE_STATE_START_TIMER:
                state = WORK_STATE_TIMER;
                break;
            case SyncMessageManager.REMOTE_STATE_START_RECORD_TIMELAPSE:
                state = WORK_STATE_VIDEO_TIMELAPSE;
                break;
            default:
                state = WORK_STATE_IDLE;
                break;
        }

        //由于各板端的实现差异及BUG等情况，板端发过来的可能是普通录像，需要校正
        if (WORK_MODE_VIDEO_TIMELAPSE == stateMessage.mode) {
            if (WORK_STATE_RECORD == state) {
                state = WORK_STATE_VIDEO_TIMELAPSE;
            }
        }
        if (WORK_STATE_VIDEO_LOOP == stateMessage.mode) {
            if (WORK_STATE_RECORD == state) {
                state = WORK_STATE_VIDEO_LOOP;
            }
        }
        CameraFactory.getInstance().getHisiCamera().workState = state;
        if (time >= 0) {
            switch (state) {
                case WORK_STATE_TIMER:
                    mView.startSecondTimer(time);
                    CameraFactory.getInstance().getHisiCamera().mode = WORK_MODE_PHOTO_TIMER;
                    break;

                case WORK_STATE_TIMELAPSE:
//                    mView.startSecondTimer(time);
                    CameraFactory.getInstance().getHisiCamera().mode = WORK_MODE_MULTI_TIMELAPSE;
                    break;

                case WORK_STATE_VIDEO_TIMELAPSE:
//                    mView.startSecondTimer(time);
                    CameraFactory.getInstance().getHisiCamera().mode = WORK_MODE_VIDEO_TIMELAPSE;
                    break;

                case WORK_STATE_VIDEO_LOOP:
                    mView.startSecondTimer(time);
                    CameraFactory.getInstance().getHisiCamera().mode = WORK_MODE_VIDEO_LOOP;
                    break;

                case WORK_STATE_RECORD:
                    mView.startSecondTimer(time);
                    CameraFactory.getInstance().getHisiCamera().mode = WORK_MODE_VIDEO_NORMAL;
                    break;

                case WORK_STATE_IDLE:
                default:
                    mView.stopSecondTimer();
                    break;
            }
            if (mode >= 0) {
                CameraFactory.getInstance().getHisiCamera().mode = mode;
            }
//            updateCommandBar();
            switch (CameraFactory.getInstance().getHisiCamera().mode) {
                case WORK_MODE_VIDEO_NORMAL:
                case WORK_MODE_VIDEO_LOOP:
                case WORK_MODE_VIDEO_TIMELAPSE:
                case WORK_MODE_VIDEO_PHOTO:
                case WORK_MODE_VIDEO_SLOW:
                    if (CameraFactory.getInstance().getHisiCamera().workState == WORK_STATE_RECORD || CameraFactory.getInstance().getHisiCamera().workState == WORK_STATE_VIDEO_TIMELAPSE
                            || CameraFactory.getInstance().getHisiCamera().workState == WORK_STATE_VIDEO_LOOP) {
                        mView.showRecordState(true);
                    } else {
                        mView.showRecordState(false);
                    }
                    break;
                default:
                    break;
            }
        }

        if (event > EVENT_NORMAL && event < EVENT_INVALID && event != EVENT_SDCARD_MOUNTED) {
            ToastUtil.showShortToast(VLCApplication.getAppContext(), UIUtils.getString(aEventStringRes[event]));
        }
        switch (event) {
            case EVENT_SDCARD_ERROR:
            case EVENT_SDCARD_NOT_EXIST:
                CameraFactory.getInstance().getHisiCamera().sdCardInfo = null;
//                updateInfoBar();
                break;

            case EVENT_SDCARD_MOUNTED:
                //不立即更新SD卡信息，待定期获取后自动刷新
                break;

            case EVENT_AC_ON:
                if (CameraFactory.getInstance().getHisiCamera().batteryInfo != null) {
                    CameraFactory.getInstance().getHisiCamera().batteryInfo.bAC = true;
                    CameraFactory.getInstance().getHisiCamera().batteryInfo.bCharging = true;
//                    updateInfoBar();
                }
                break;

            case EVENT_AC_OFF:
                if (CameraFactory.getInstance().getHisiCamera().batteryInfo != null) {
                    CameraFactory.getInstance().getHisiCamera().batteryInfo.bAC = false;
                    CameraFactory.getInstance().getHisiCamera().batteryInfo.bCharging = false;
//                    updateInfoBar();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onBufferChanged(float buffer) {
        if (buffer >= 100) {
            mView.hideLoading();
        } else {
            mView.showLoading(R.string.in_the_buffer);
        }
    }

    private boolean allowAutoStarRecord = true;

    @Override
    public void onLoadComplete() {
        retryCount = 0;
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    mView.hideLoading();
                } else {
                    if (allowAutoStarRecord) {
                        //开启录制
                        recordShot();
                    }
                }
                break;
            default:
                mView.hideLoading();
                break;
        }
        allowAutoStarRecord = true;
    }

    @Override
    public void onError() {
        mView.showToast(R.string.Check_connection);
        mView.exit();
    }

    @Override
    public void onPlayError() {
        LogUtils.e("" + retryCount);
        if (retryCount == 3) {
            mView.hideLoading();
            mView.showToast(R.string.Abnormal_play);
            mView.exit();
        } else {
            retryCount++;
            delayStartPreView(300);
        }
    }

    @Override
    public void onEnd() {
        delayStartPreView(500);
    }

    public void delayStartPreView(int delayMillis) {
        mHandler.removeCallbacks(delayStartPreViewTask);
        mHandler.postDelayed(delayStartPreViewTask, delayMillis);
    }

    private Runnable delayStartPreViewTask = new Runnable() {
        @Override
        public void run() {
            mView.initPlayView();
        }
    };

    @Override
    public void changeMode(int mode) {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        if (!dv.supportWorkMode() || dv.mode == mode) {
            return;
        }
        ChangeModeTask changeModeTask = new ChangeModeTask();
        changeModeTask.execute(mode);

    }

    @Override
    public void takePhoto() {
        if (CameraFactory.getInstance().getHisiCamera().sdCardInfo != null) {
            int state = CameraFactory.getInstance().getHisiCamera().sdCardInfo.sdState;
            if (state == SD_STATE_ERROR || state == SD_STATE_NONE) {
                mView.showToast(R.string.error_no_sd);
                return;
            } else if (state == SD_STATE_FULL && CameraFactory.getInstance().getHisiCamera().workState == WORK_STATE_IDLE) {
                mView.showToast(R.string.error_sd_full);
                return;
            }
        }
        pressCommandButton();
    }

    @Override
    public void onStartPlay() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void toggleMode() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        if (!dv.supportWorkMode()) {
            return;
        }
        int mode;
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    mView.showToast(R.string.wifi_stoprecordingbef);
                    return;
                } else {
                    mode = WORK_MODE_PHOTO_SINGLE;
                }
                break;
            default:
                mode = WORK_MODE_VIDEO_NORMAL;
                break;
        }
        ChangeModeTask changeModeTask = new ChangeModeTask();
        changeModeTask.execute(mode);
    }

    @SuppressLint("StaticFieldLeak")
    private class ChangeModeTask extends AsyncTask<Integer, Void, Integer> {
        private int mode;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.showLoading();
        }

        @Override
        protected Integer doInBackground(Integer... integers) {
            HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
            if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                    || dv.workState == WORK_STATE_VIDEO_LOOP) {
                final Result result = CameraFactory.getInstance().getHisiCamera().executeCommand(Command.ACTION_RECORD_STOP);
                if (FAILURE == result.returnCode) {
                    return FAILURE;
                } else {
                    UIUtils.post(new Runnable() {
                        @Override
                        public void run() {
                            processMsgCommand(result);
                        }
                    });
                }
            }
            mode = integers[0];
//            if (mode == WORK_MODE_VIDEO_NORMAL) {
//                if (FAILURE == CameraFactory.getInstance().getHisiCamera().setWorkMode(mode)) {
//                    return FAILURE;
//                } else {
//                    final Result result = CameraFactory.getInstance().getHisiCamera().executeCommand(Command.ACTION_RECORD_START);
//                    if (FAILURE != result.returnCode) {
//                        UIUtils.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                processMsgCommand(result);
//                            }
//                        });
//                    }
//                    return SUCCESS;
//                }
//            }
            return CameraFactory.getInstance().getHisiCamera().setWorkMode(integers[0]);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mView.hideLoading();
            if (FAILURE == integer) {
                mView.showToast(R.string.set_failure);
            } else {
                CameraFactory.getInstance().getHisiCamera().mode = mode;
                delayStartPreView(0);
                updateUIByModeAndWorkState();
            }
        }
    }

    private boolean isRecording() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        if (!dv.supportWorkMode()) {
            return false;
        }
        boolean isRecording;
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
//                    showToast(R.string.wifi_stoprecordingbef);
                    isRecording = true;
                } else {
                    isRecording = false;
                }
                break;
            default:
                isRecording = false;
                break;
        }
        return isRecording;
    }

    /**
     * 切换DV模式和工作状态,显示或隐藏相关组件
     */
    private void updateUIByModeAndWorkState() {
        UIUtils.post(new Runnable() {
            @Override
            public void run() {
                updateInfoBar();
                updateCommandBar();
            }
        });
    }

    /**
     * 更新上方信息栏
     */
    private void updateInfoBar() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        if (dv.sdCardInfo != null) {
            if (dv.sdCardInfo.sdState == SD_STATE_NONE || dv.sdCardInfo.sdState == SD_STATE_ERROR) {
                mView.showToast(R.string.wifi_sdcard);
            } else if (dv.sdCardInfo.sdState == SD_STATE_FULL) {
                mView.showToast(R.string.wifi_camera_storage);
            }
        }
        mView.updateInfoBar();
    }

    /**
     * 更新底部命令栏
     */
    private void updateCommandBar() {
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        switch (CameraFactory.getInstance().getHisiCamera().mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    mView.showRecordState(true);
                    mView.modeChange(false);
                } else {
//                    executeCommandAndSendResult(Command.ACTION_RECORD_START);
                    mView.showRecordState(false);
                    mView.modeChange(false);
                }
                break;
            case WORK_MODE_PHOTO_SINGLE:
                mView.modeChange(true);
                break;
            case WORK_MODE_MULTI_BURST:
                mView.modeChange(true);
                break;

            case WORK_MODE_MULTI_TIMELAPSE:
                if (dv.workState == WORK_STATE_TIMELAPSE) {
                    mView.modeChange(true);
                } else {
                    mView.modeChange(true);
                }
                break;

            case WORK_MODE_PHOTO_TIMER:
                if (dv.workState == WORK_STATE_TIMER) {
                    mView.modeChange(true);
                } else {
                    mView.modeChange(true);
                }
                break;

            case WORK_MODE_MULTI_CONTINUOUS:
                mView.modeChange(true);
                break;
            default:
                break;
        }
    }

    private boolean isPhotoMode() {
        int mode = CameraFactory.getInstance().getHisiCamera().mode;
        switch (mode) {
            case WORK_MODE_PHOTO_SINGLE:
            case WORK_MODE_MULTI_BURST:
            case WORK_MODE_MULTI_TIMELAPSE:
            case WORK_MODE_PHOTO_TIMER:
            case WORK_MODE_MULTI_CONTINUOUS:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void recordShot() {
        mView.showLoading();
        if (CameraFactory.getInstance().getHisiCamera().sdCardInfo != null) {
            int state = CameraFactory.getInstance().getHisiCamera().sdCardInfo.sdState;
            if (state == SD_STATE_ERROR || state == SD_STATE_NONE) {
//                mView.showToast(R.string.wifi_camera_storage);
                mView.showToast(R.string.wifi_sdcard);
                mView.hideLoading();
                return;
            } else if (state == SD_STATE_FULL && CameraFactory.getInstance().getHisiCamera().workState == WORK_STATE_IDLE) {
//                mView.showToast(R.string.wifi_sdcard);
                mView.showToast(R.string.wifi_camera_storage);
                mView.hideLoading();
                return;
            }
        }
        pressCommandButton();
    }

    @Override
    public void setResolution(int mode, String value) {
        new SetResolutionTask(mode, value).execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class SetResolutionTask extends AsyncTask<Void, Void, Integer> {

        private final int mMode;
        private final String mValue;

        public SetResolutionTask(int mode, String value) {
            mMode = mode;
            mValue = value;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.stopPreview();
            mView.showLoading();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            if (isRecording()) {
                final Result result = CameraFactory.getInstance().getHisiCamera().executeCommand(Command.ACTION_RECORD_STOP);
                UIUtils.post(new Runnable() {
                    @Override
                    public void run() {
                        processMsgCommand(result);
                    }
                });
                if (result.returnCode == FAILURE) {
                    return FAILURE;
                }
            } else {
                allowAutoStarRecord = false;
            }
            switch (mMode) {
                case WORK_MODE_VIDEO_NORMAL:
                    int setVideoResolution = CameraFactory.getInstance().getHisiCamera().setParameter(mMode, CONFIG_VIDEO_VIDEO_RESOLUTION, mValue);
                    if (FAILURE != setVideoResolution) {
                        CameraFactory.getInstance().getHisiCamera().modeConfig.videoNormalResolution = mValue;
                    }
                    return setVideoResolution;
                case WORK_MODE_VIDEO_LOOP:
                    int setVideoLoopResolution = CameraFactory.getInstance().getHisiCamera().setParameter(mMode, CONFIG_VIDEO_LOOP_TYPE, mValue);
                    if (FAILURE != setVideoLoopResolution) {
                        CameraFactory.getInstance().getHisiCamera().modeConfig.videoLoopResolution = mValue;
                    }
                    return setVideoLoopResolution;
                case WORK_MODE_PHOTO_SINGLE:
                    int setPhotoResolution = CameraFactory.getInstance().getHisiCamera().setParameter(mMode, CONFIG_PHOTO_RESOLUTION, mValue);
                    if (FAILURE != setPhotoResolution) {
                        CameraFactory.getInstance().getHisiCamera().modeConfig.photoSingleResolution = mValue;
                    }
                    return setPhotoResolution;
                default:
                    break;
            }
            return FAILURE;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            mView.initPlayView();
            mView.hideLoading();
            if (FAILURE != integer) {
                mView.showToast(R.string.set_success);
                mView.updateInfoBar();
            } else {
                mView.showToast(R.string.set_failure);
            }
        }
    }

    /**
     * 按下中间的录像（停止录像，拍照）按钮，在新线程中判断发什么命令，执行命令，发回命令结果
     */
    private void pressCommandButton() {
        int cmd = -1;
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        //根据当前模式和是否正在录像，是否正在间隔拍，确定要发送的命令
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
                if (dv.deviceAttr.type.equals(Common.SENSOR_34220)) {
                    if (dv.workState == WORK_STATE_RECORD) {
                        cmd = Command.ACTION_RECORD_STOP;
                    } else {
                        cmd = Command.ACTION_RECORD_START;
                    }
                } else {
                    if (dv.workState == WORK_STATE_RECORD) {
                        cmd = Command.ACTION_VIDEO_COMMON_STOP;
                    } else {
                        cmd = Command.ACTION_VIDEO_COMMON_START;
                    }
                }
                break;
            case WORK_MODE_VIDEO_LOOP:
                if (dv.workState == WORK_STATE_VIDEO_LOOP) {
                    cmd = Command.ACTION_VIDEO_LOOP_STOP;
                } else {
                    cmd = Command.ACTION_VIDEO_LOOP_START;
                }
                break;

            case WORK_MODE_VIDEO_TIMELAPSE:
                if (dv.workState == WORK_STATE_VIDEO_TIMELAPSE) {
                    cmd = Command.ACTION_VIDEO_TIMELAPSE_STOP;
                } else {
                    cmd = Command.ACTION_VIDEO_TIMELAPSE_START;
                }
                break;

            case WORK_MODE_VIDEO_PHOTO:
                if (dv.workState == WORK_STATE_RECORD) {
                    cmd = Command.ACTION_VIDEO_SNAP_STOP;
                } else {
                    cmd = Command.ACTION_VIDEO_SNAP_START;
                }
                break;

            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD) {
                    cmd = Command.ACTION_VIDEO_SLOW_STOP;
                } else {
                    cmd = Command.ACTION_VIDEO_SLOW_START;
                }
                break;

            case WORK_MODE_PHOTO_SINGLE:
                cmd = Command.ACTION_PHOTO;
                break;

            case WORK_MODE_MULTI_BURST:
                cmd = Command.ACTION_BURST;
                break;

            case WORK_MODE_MULTI_TIMELAPSE:
                if (dv.workState == WORK_STATE_TIMELAPSE) {
                    cmd = Command.ACTION_TIMELAPSE_STOP;
                } else {
                    cmd = Command.ACTION_TIMELAPSE_START;
                }
                break;

            case WORK_MODE_PHOTO_TIMER:
                if (dv.workState == WORK_STATE_TIMER) {
                    cmd = Command.ACTION_TIMER_STOP;
                } else {
                    cmd = Command.ACTION_TIMER_START;
                }

            default:
                break;
        }

        executeCommandAndSendResult(cmd);
    }

    private void executeCommandAndSendResult(int cmd) {
        ExecuteCommandTask executeCommandTask = new ExecuteCommandTask();
        executeCommandTask.execute(cmd);
    }

    @SuppressLint("StaticFieldLeak")
    private class ExecuteCommandTask extends AsyncTask<Integer, Void, Common.Result> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.eventEnable(false);
        }

        @Override
        protected Common.Result doInBackground(Integer... integers) {
            return CameraFactory.getInstance().getHisiCamera().executeCommand(integers[0]);
        }

        @Override
        protected void onPostExecute(Common.Result result) {
            super.onPostExecute(result);
            processMsgCommand(result);
            mView.hideLoading();
            UIUtils.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mView.eventEnable(true);
                }
            }, 1000);
        }
    }

    /**
     * 处理命令执行结果的消息
     */
    private void processMsgCommand(Common.Result result) {
        if (result.returnCode == FAILURE) {
            mView.showToast(commandError2String(result.errorCode));
            updateUIByModeAndWorkState();
            return;
        }
        HisiCamera dv = CameraFactory.getInstance().getHisiCamera();
        switch (result.cmd) {
            case Command.ACTION_VIDEO_SNAP_STOP:
            case Command.ACTION_VIDEO_SLOW_STOP:
            case Command.ACTION_VIDEO_COMMON_STOP:
            case Command.ACTION_VIDEO_LOOP_STOP:
            case Command.ACTION_RECORD_STOP:
                mView.stopSecondTimer();
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_PHOTO:
                mView.showToast(R.string.takephoto_sucess);
                break;

            case Command.ACTION_VIDEO_SNAP_START:
            case Command.ACTION_VIDEO_SLOW_START:
            case Command.ACTION_VIDEO_COMMON_START:
            case Command.ACTION_VIDEO_LOOP_START:
            case Command.ACTION_RECORD_START:
                dv.workState = WORK_STATE_RECORD;
                mView.startSecondTimer(0);
                break;

            case Command.ACTION_TIMELAPSE_START:
                dv.workState = WORK_STATE_TIMELAPSE;
                break;

            case Command.ACTION_TIMER_START:
                dv.workState = WORK_STATE_TIMER;
                break;

            case Command.ACTION_TIMELAPSE_STOP:
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_BURST:
                break;

            case Command.ACTION_TIMER_STOP:
                dv.workState = WORK_STATE_IDLE;
                break;

            case Command.ACTION_VIDEO_TIMELAPSE_START:
                dv.workState = WORK_STATE_VIDEO_TIMELAPSE;
                break;

            case Command.ACTION_VIDEO_TIMELAPSE_STOP:
                dv.workState = WORK_STATE_IDLE;
                break;
            default:
                break;
        }
        switch (dv.mode) {
            case WORK_MODE_VIDEO_NORMAL:
            case WORK_MODE_VIDEO_LOOP:
            case WORK_MODE_VIDEO_TIMELAPSE:
            case WORK_MODE_VIDEO_PHOTO:
            case WORK_MODE_VIDEO_SLOW:
                if (dv.workState == WORK_STATE_RECORD || dv.workState == WORK_STATE_VIDEO_TIMELAPSE
                        || dv.workState == WORK_STATE_VIDEO_LOOP) {
                    mView.showRecordState(true);
                } else {
                    mView.showRecordState(false);
                }
            default:
                break;
        }
    }

    private String commandError2String(int errorCode) {
        int errorRes;
        switch (errorCode) {
            case ERR_NO_SD:
                errorRes = R.string.wifi_sdcard;
                break;

            case ERR_SD_FULL:
                errorRes = R.string.wifi_camera_storage;
                break;

            case ERR_SD_ERROR:
                errorRes = R.string.error_sd_error;
                break;

            case ERR_RECORD_NO_SPACE:
                errorRes = R.string.wifi_camera_storage;
                break;

            case ERR_LOOP_NO_SPACE:
                errorRes = R.string.wifi_camera_storage;
                break;

            case ERR_SANPSHOT_NO_SPACE:
                errorRes = R.string.wifi_camera_storage;
                break;

            case ERR_GET_CHANNEL_STATE_FAIL:
                errorRes = R.string.error_get_channel_state_fail;
                break;

            case ERR_CHANNEL_BUSY:
                errorRes = R.string.error_channel_busy;
                break;

            case ERR_START_CHANNEL_FAIL:
                errorRes = R.string.error_start_channel_fail;
                break;

            case ERR_STOP_CHANNEL_FAIL:
                errorRes = R.string.error_stop_channel_fail;
                break;

            case ERR_SNAPSHOT_PRARM_ERROR:
                errorRes = R.string.error_snapshot_param_error;
                break;

            default:
                errorRes = R.string.set_failure;
                break;
        }

        return UIUtils.getString(errorRes);
    }

    @Override
    public void detachView() {
        super.detachView();
        mContext.stopService(new Intent(mContext, MessageService.class));

    }
}
