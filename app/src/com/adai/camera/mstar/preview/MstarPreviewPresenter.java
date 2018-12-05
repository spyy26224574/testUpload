package com.adai.camera.mstar.preview;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.mstar.data.MstarDataSource;
import com.adai.camera.mstar.data.MstarRepository;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.LogUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.WifiUtil;
import com.example.ipcamera.application.VLCApplication;

import java.net.URL;

/**
 * Created by huangxy on 2017/10/11 22:16.
 */

public class MstarPreviewPresenter extends MstarPreviewContract.Presenter {
    private boolean isPortrait;
    private int mCameraId;

    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public void init() {
        mView.showLoading(R.string.in_the_buffer);
        MstarRepository.getInstance().initDataSource(new MstarDataSource.DataSourceSimpleCallBack() {
            @Override
            public void success() {
                MstarRepository.getInstance().getStatus(new MstarDataSource.DataSourceSimpleCallBack() {
                    @Override
                    public void success() {
                        mView.initPlayView();
                        mView.currentMode(MstarCamera.MODE_MOVIE);
                    }

                    @Override
                    public void error(String error) {
//                        mView.hideLoading();
//                        mView.showToast(R.string.access_camera_state_failed);
//                        mView.exit();
                        mView.initPlayView();
                        mView.currentMode(MstarCamera.MODE_MOVIE);
                    }
                });
            }

            @Override
            public void error(String error) {
//                mView.hideLoading();
//                mView.showToast(R.string.access_camera_state_failed);
//                mView.exit();
                mView.initPlayView();
                mView.currentMode(MstarCamera.MODE_MOVIE);
            }
        });
    }

    @Override
    public void initOrientation() {
        Configuration configuration = VLCApplication.getAppContext().getResources().getConfiguration();
        isPortrait = configuration.orientation != Configuration.ORIENTATION_LANDSCAPE;
        mView.changeOrientation(isPortrait);
        mView.showPip(isPortrait ? 0 : 1);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int layoutDirection = newConfig.orientation;
        isPortrait = layoutDirection != Configuration.ORIENTATION_LANDSCAPE;
        mView.showPip(isPortrait ? 0 : 1);
        mView.changeOrientation(isPortrait);
//        mView.pictureVisible(MstarCamera.CUR_MODE == MstarCamera.MODE_PHOTO || isPortrait);
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onRestart() {
        mView.showLoading(R.string.please_wait);
        mView.initPlayView();
//        mView.startPreview();
//        CameraCommand.asynSendRequest(CameraCommand.commandQueryAV1Url(), mGetStreamUrlListener);
    }

    @Override
    public void onStop() {

    }


    @Override
    public void onBufferChanged(float buffer) {

    }

    private int retryCount = 0;
    private int restoreCount = 0;

    @Override
    public void onLoadComplete() {
        retryCount = 0;
        restoreCount = 0;
//        if (MstarCamera.IS_RECORDING) {
//            mView.showRecordState(true);
//            mView.hideLoading();
//        } else {
//            CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
//                @Override
//                public void onResponse(String response) {
//                    mView.hideLoading();
//                    if (response != null && response.contains("OK")) {
//                        if (MstarCamera.IS_RECORDING) {
//                            MstarCamera.IS_RECORDING = false;
//                            mView.showRecordState(false);
//                        } else {
//                            MstarCamera.IS_RECORDING = true;
//                            mView.showRecordState(true);
//                        }
//                    } else {
//                        if (response != null && response.contains("718")) {
//                            mView.showToast(R.string.label_sd_error);
//                        } else {
//                            mView.showToast(R.string.start_record_failed);
//                        }
//                    }
//                }
//
//                @Override
//                public void onErrorResponse(String message) {
//                    mView.hideLoading();
//                    mView.showToast(R.string.start_record_failed);
//                }
//            });
//        }
//        CameraCommand.asynSendRequest(CameraCommand.commandQueryAV1Url(), mGetStreamUrlListener);
        CameraCommand.asynSendRequest(CameraCommand.commandCameraTimeSettingsUrl(), new CameraCommand.RequestListener() {//同步时间
            @Override
            public void onResponse(String response) {
                CameraCommand.asynSendRequest(CameraCommand.commandCameraGetcamidUrl(), mGetCamIdListener);
            }

            @Override
            public void onErrorResponse(String message) {
                CameraCommand.asynSendRequest(CameraCommand.commandCameraGetcamidUrl(), mGetCamIdListener);
            }
        });
    }

    @Override
    public void onError() {
        ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.Check_connection));
        mView.exit();
    }

    @Override
    public void onPlayError() {
        LogUtils.e("" + retryCount);
        if (retryCount != 3) {
            retryCount++;
            delayStartPreView(0);
        } else {
            mView.hideLoading();
            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.Abnormal_play));
            mView.exit();
        }
    }

    @Override
    public void onEnd() {
        if (restoreCount != 3) {
            restoreCount++;
            delayStartPreView(500);
        } else {
            mView.hideLoading();
            ToastUtil.showShortToast(VLCApplication.getAppContext(), VLCApplication.getAppContext().getString(R.string.Abnormal_play));
            mView.exit();
        }
        mView.startPreview();
    }

    private void delayStartPreView(int delayMillis) {
        nHandler.removeCallbacks(delayStartPreViewTask);
        nHandler.postDelayed(delayStartPreViewTask, delayMillis);
    }

    private Runnable delayStartPreViewTask = new Runnable() {
        @Override
        public void run() {
            mView.startPreview();
        }
    };

    @Override
    public void changeMode(int mode) {
        MstarCamera.CUR_MODE = mode;
        mView.currentMode(mode);
    }

    @Override
    public void recordShot() {
        if (MstarCamera.CUR_MODE == MstarCamera.MODE_PHOTO) {
            takePhoto();
        } else {
            record();
        }
    }

    private void record() {
        if (MstarCamera.IS_RECORDING) {
            mView.showLoading(R.string.msg_center_stop_recording);
        } else {
            mView.showLoading(R.string.Opening_record);
        }
        CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
            @Override
            public void onResponse(String response) {
                mView.hideLoading();
                if (response != null && response.contains("OK")) {
                    if (MstarCamera.IS_RECORDING) {
                        MstarCamera.IS_RECORDING = false;
                        mView.showRecordState(false);
                    } else {
                        MstarCamera.IS_RECORDING = true;
                        mView.showRecordState(true);
                    }
                } else {
                    if (response != null && response.contains("718")) {
                        mView.showToast(R.string.label_sd_error);
                    } else {
                        if (MstarCamera.IS_RECORDING) {
                            mView.showToast(R.string.stop_recording_failed);
                        } else {
                            mView.showToast(R.string.start_record_failed);
                        }
                    }
                }
            }

            @Override
            public void onErrorResponse(String message) {
                mView.hideLoading();
                mView.showToast(R.string.start_record_failed);
            }
        });
    }

    @Override
    public void takePhoto() {
        mView.showLoading(R.string.please_wait);
        CameraCommand.asynSendRequest(CameraCommand.commandCameraSnapshotUrl(), mTakePhotoListener);
    }

    @Override
    public void onStartPlay() {
        LogUtils.e("retryCount = " + retryCount);
    }

    @Override
    public void onResume() {
//        mView.showLoading(R.string.please_wait);
//        delayInit(2000);
//        delayStartPreView(500);
    }

    @Override
    public void switchPip() {
        new CameraIdSwitch().execute();
    }

    @SuppressLint("StaticFieldLeak")
    private class CameraIdSwitch extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mView.showLoading(R.string.please_wait);
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = null;
            if (mCameraId == 1) {
                url = CameraCommand.commandCameraSwitchtoFrontUrl();
            } else if (mCameraId == 0) {
                url = CameraCommand.commandCameraSwitchtoRearUrl();
            }

            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            //Log.i(TAG, "CameraIdSwitch:"+result) ;
            mView.hideLoading();
            if (result == null) {
                return;
            }
            String err = result.substring(0, 3);
            if (!err.equals("709")) {
                int drawableRes = R.drawable.dualcam_front;
                if (mCameraId == 1) {
                    mCameraId = 0;
                } else if (mCameraId == 0) {
                    mCameraId = 1;
                    drawableRes = R.drawable.dualcam_behind;
                }
                mView.respChangePip(drawableRes);
                mView.showToast(R.string.set_success);
            } else {
                mView.showToast(R.string.set_failure);
            }
            super.onPostExecute(result);
        }
    }

    /**
     * 获取视频播放地址
     */
    private CameraCommand.RequestListener mGetStreamUrlListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {
            if (!TextUtils.isEmpty(response)) {
                try {
                    String[] lines_temp = response.split("Camera.Preview.RTSP.av=");
                    String[] lines = lines_temp[1].split(System.getProperty("line.separator"));
                    int av = Integer.valueOf(lines[0]);
                    switch (av) {
                        case 1:    // liveRTSP/av1 for RTSP MJPEG+AAC
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_MJPEG_AAC_URL;
                            break;
                        case 2: // liveRTSP/v1 for RTSP H.264
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_URL;
                            break;
                        case 3: // liveRTSP/av2 for RTSP H.264+AAC
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_AAC_URL;
                            break;
                        case 4: // liveRTSP/av4 for RTSP H.264+PCM
                            MstarCamera.URL_STREAM = "rtsp://" + MstarCamera.CAM_IP + MstarCamera.DEFAULT_RTSP_H264_PCM_URL;
                            break;
                        default:
                            break;
                    }
                    CameraCommand.asynSendRequest(CameraCommand.commandCameraTimeSettingsUrl(), new CameraCommand.RequestListener() {//同步时间
                        @Override
                        public void onResponse(String response) {
                            CameraCommand.asynSendRequest(CameraCommand.commandCameraGetcamidUrl(), mGetCamIdListener);
                        }

                        @Override
                        public void onErrorResponse(String message) {
                            CameraCommand.asynSendRequest(CameraCommand.commandCameraGetcamidUrl(), mGetCamIdListener);
                        }
                    });
                } catch (Exception ignore) {
                    mView.exit();
                    mView.hideLoading();
                    mView.showToast(R.string.please_connect_camera);
                }
            } else {
                mView.hideLoading();
                mView.showToast(R.string.please_connect_camera);
                mView.exit();
            }
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.please_connect_camera);
            mView.exit();
        }
    };

    /**
     * 获取cameraID
     */
    private CameraCommand.RequestListener mGetCamIdListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String response) {
            int drawableRes = R.drawable.dualcam_front;
            String err = response.substring(0, 3);
            if (err.equals("703")) {
                mCameraId = 0;
            } else if (response.contains("OK")) {
                String[] lines_temp = response.split("Camera.Preview.Source.1.Camid=");
                if ((lines_temp[1] != null) && (lines_temp[0] != null)) {
                    switch (lines_temp[1]) {
                        case "front\n":
                            mCameraId = 0;
                            drawableRes = R.drawable.dualcam_front;
                            break;
                        case "rear\n":
                            mCameraId = 1;
                            drawableRes = R.drawable.dualcam_behind;
                            break;
                        default:
                            mCameraId = 0;
                            drawableRes = R.drawable.dualcam_front;
                            break;
                    }
                    CameraCommand.asynSendRequest(CameraCommand.commandCameraStautsUrl(), mGetRecordStatusListener);
                } else {
                    mCameraId = 0;
                    mView.hideLoading();
                    mView.showToast(R.string.please_connect_camera);
                    mView.exit();
                }
            }
            mView.respChangePip(drawableRes);
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.please_connect_camera);
            mView.exit();
        }
    };

    /**
     * 获取录制状态
     */
    private CameraCommand.RequestListener mGetRecordStatusListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String result) {
            if (result != null && result.contains("OK")) {
                String[] lines;
                // Check Video Status - Recording or Standby (defined in FW)
                String[] lines_temp = result.split("Camera.Preview.MJPEG.status.record=");
                lines = lines_temp[1].split(System.getProperty("line.separator"));
                MstarCamera.IS_RECORDING = "Recording".equals(lines[0]);
                mView.showRecordState(MstarCamera.IS_RECORDING);
                // Check Camera Mode - Videomode or NotVideomode (defined in FW)
                lines_temp = result.split("Camera.Preview.MJPEG.status.mode=");
                lines = lines_temp[1].split(System.getProperty("line.separator"));
                MstarCamera.SENSOR_MODE = lines[0];
//                if (MstarCamera.SENSOR_MODE.equals("Videomode")) {
////                    mUIMode = "VIDEO";
//                    mView.currentMode(MstarCamera.MODE_MOVIE);
//                } else {
//                    mView.currentMode(MstarCamera.MODE_PHOTO);
//                }
//                if (MstarCamera.SENSOR_MODE.equals("Idlemode")) {
//                    CameraCommand.asynSendRequest(CameraCommand.commandExitPlayback(), new CameraCommand.RequestListener() {
//                        @Override
//                        public void onResponse(String response) {
//                            MstarCamera.SENSOR_MODE = "Videomode";
//                            mView.initPlayView();
//                        }
//
//                        @Override
//                        public void onErrorResponse(String message) {
//                            if (!MstarCamera.IsCameraInPreviewMode()) {
//                                mView.hideLoading();
//                                mView.showToast(R.string.Abnormal_play);
//                                mView.exit();
//                            }
//                        }
//                    });
//                } else {
//                    mView.initPlayView();
//                }
                if (MstarCamera.IS_RECORDING) {
                    mView.showRecordState(true);
                    mView.hideLoading();
                } else {
                    CameraCommand.asynSendRequest(CameraCommand.commandCameraRecordUrl(), new CameraCommand.RequestListener() {
                        @Override
                        public void onResponse(String response) {
                            mView.hideLoading();
                            if (response != null && response.contains("OK")) {
                                if (MstarCamera.IS_RECORDING) {
                                    MstarCamera.IS_RECORDING = false;
                                    mView.showRecordState(false);
                                } else {
                                    MstarCamera.IS_RECORDING = true;
                                    mView.showRecordState(true);
                                }
                            } else {
                                if (response != null && response.contains("718")) {
                                    mView.showToast(R.string.label_sd_error);
                                } else {
                                    mView.showToast(R.string.start_record_failed);
                                }
                            }
                        }

                        @Override
                        public void onErrorResponse(String message) {
                            mView.hideLoading();
                            mView.showToast(R.string.start_record_failed);
                        }
                    });
                }
            } else {
                mView.hideLoading();
                mView.showToast(R.string.access_camera_state_failed);
            }
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.access_camera_state_failed);
        }
    };

    /**
     * 切换录制状态
     */
    private CameraCommand.RequestListener mChangeRecordStatusListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String result) {
            if (result != null && result.contains("OK")) {
                if (MstarCamera.IS_RECORDING) {
                    MstarCamera.IS_RECORDING = false;
                    mView.showRecordState(false);
                } else {
                    MstarCamera.IS_RECORDING = true;
                    mView.showRecordState(true);
                }
            } else {
                if (result != null && result.contains("718")) {
                    mView.showToast(R.string.label_sd_error);
                } else {
                    mView.showToast(R.string.set_failure);
                }
            }
            mView.hideLoading();
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.switch_failed);
        }
    };

    /**
     * 拍照
     */
    private CameraCommand.RequestListener mTakePhotoListener = new CameraCommand.RequestListener() {
        @Override
        public void onResponse(String result) {
            if (result != null && result.contains("OK")) {
                mView.showToast(R.string.takephoto_sucess);
            } else {
                if (result != null && result.contains("718")) {
                    mView.showToast(R.string.label_sd_error);
                } else {
                    mView.showToast(R.string.takephoto_failed);
                }
            }
            mView.hideLoading();
        }

        @Override
        public void onErrorResponse(String message) {
            mView.hideLoading();
            mView.showToast(R.string.takephoto_failed);
        }
    };

    @Override
    public void detachView() {
        super.detachView();
        WifiUtil.getInstance().checkAvailableNetwork(VLCApplication.getAppContext());
    }
}
