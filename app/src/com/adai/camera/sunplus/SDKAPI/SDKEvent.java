/**
 * Added by zhangyanhu C01012,2014-7-11
 */
package com.adai.camera.sunplus.SDKAPI;

import android.os.Handler;
import android.util.Log;

import com.adai.camera.sunplus.data.GlobalInfo;
import com.icatch.wificam.customer.ICatchWificamListener;
import com.icatch.wificam.customer.type.ICatchEvent;
import com.icatch.wificam.customer.type.ICatchEventID;

/**
 * Added by zhangyanhu C01012,2014-7-11
 */
public class SDKEvent {
    private static final String TAG = "SDKEvent";
    private CameraAction cameraAction = CameraAction.getInstance();
    private Handler handler;
    private SdcardStateListener sdcardStateListener;
    private BatteryStateListener batteryStateListener;
    private CaptureStartListener captureStartListener;
    private CaptureDoneListener captureDoneListener;
    private VideoOffListener videoOffListener;
    private FileAddedListener fileAddedListener;
    private VideoOnListener videoOnListener;
    private ConnectionFailureListener connectionFailureListener;
    private TimeLapseStopListener timeLapseStopListener;
    private ServerStreamErrorListener serverStreamErrorListener;
    private VideoRecordingTimeStartListener videoRecordingTimeStartListener;
    private FileDownloadListener fileDownloadListener;
    private UpdateFWCompletedListener updateFWCompletedListener;
    private UpdateFWPoweroffListener updateFWPoweroffListener;
    private static NoSdcardListener noSdcardListener;

    // private Context context;
    public SDKEvent(Handler handler) {
        this.handler = handler;
        // this.context = context;
    }

    public void addEventListener() {
        addEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED);
        addEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);
        addEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED);
        addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START);
        addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE);
        addEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF);
        addEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED);
        addEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON);
        addEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED);
        addEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP);
        addEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR);
        addEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD);
        addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED);
        addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF);
    }

    public void delEventListener() {
        delEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED);
        delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL);
        delEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED);
        delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START);
        delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE);
        delEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF);
        delEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED);
        delEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON);
        delEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED);
        delEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP);
        delEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR);
        delEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD);
        delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED);
        delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF);
    }

    public void addEventListener(int iCatchEventID) {
        // switch(iCatchEventID){
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_FULL) {
            sdcardStateListener = new SdcardStateListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL, sdcardStateListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED) {
            batteryStateListener = new BatteryStateListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED, batteryStateListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_START) {
            captureStartListener = new CaptureStartListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START, captureStartListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE) {
            captureDoneListener = new CaptureDoneListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE, captureDoneListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_VIDEO_OFF) {
            videoOffListener = new VideoOffListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF, videoOffListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_ADDED) {
            fileAddedListener = new FileAddedListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED, fileAddedListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_VIDEO_ON) {
            videoOnListener = new VideoOnListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON, videoOnListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED) {
            connectionFailureListener = new ConnectionFailureListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_TIMELAPSE_STOP) {
            timeLapseStopListener = new TimeLapseStopListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP, timeLapseStopListener);
        }

        if (iCatchEventID == ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR) {
            serverStreamErrorListener = new ServerStreamErrorListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR, serverStreamErrorListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_DOWNLOAD) {
            fileDownloadListener = new FileDownloadListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD, fileDownloadListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED) {
            updateFWCompletedListener = new UpdateFWCompletedListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED, updateFWCompletedListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF) {
            updateFWPoweroffListener = new UpdateFWPoweroffListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF, updateFWPoweroffListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_REMOVED) {
            noSdcardListener = new NoSdcardListener();
            cameraAction.addEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener);
        }
    }

    public static void addScanEventListener(ICatchWificamListener listener) {
        Log.e(TAG, "addScanEventListener: [Normal] -- SDKEvent: Start addScanEventListener");
        CameraAction.addScanEventListener(listener);
        Log.e(TAG, "[Normal] -- SDKEvent: End addScanEventListener");
    }

    public static void delScanEventListener(ICatchWificamListener listener) {
        Log.e(TAG, "[Normal] -- SDKEvent: Start delScanEventListener");
        CameraAction.delScanEventListener(listener);
        Log.e(TAG, "[Normal] -- SDKEvent: End delScanEventListener");
    }

    // JIRA ICOM-1577 Begin:Add by b.jiang C01063 2015-07-17
    public static void addStaticEventListener(int eventID) {
        noSdcardListener = new NoSdcardListener();
        CameraAction.addStaicEventListener(eventID, noSdcardListener);
    }

    public static void delStaticEventListener(int ichEventSdcardRemoved) {
        if (noSdcardListener != null) {
            CameraAction.delStaicEventListener(ichEventSdcardRemoved, noSdcardListener);
        }

    }

    // JIRA ICOM-1577 End:Add by b.jiang C01063 2015-07-17
    public void addCustomizeEvent(int eventID) {
        switch (eventID) {
            case 0x5001:
                videoRecordingTimeStartListener = new VideoRecordingTimeStartListener();
                cameraAction.addCustomEventListener(eventID, videoRecordingTimeStartListener);
                break;
        }

    }

    public void delCustomizeEventListener(int eventID) {
        switch (eventID) {
            case 0x5001:
                if (videoRecordingTimeStartListener != null) {
                    cameraAction.delCustomEventListener(eventID, videoRecordingTimeStartListener);
                }
                break;
        }

    }

    public void delEventListener(int iCatchEventID) {
        // switch(iCatchEventID){
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_FULL && sdcardStateListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_FULL, sdcardStateListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED && batteryStateListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_BATTERY_LEVEL_CHANGED, batteryStateListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE && captureDoneListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_COMPLETE, captureDoneListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CAPTURE_START && captureStartListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_CAPTURE_START, captureStartListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_VIDEO_OFF && videoOffListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_VIDEO_OFF, videoOffListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_ADDED && fileAddedListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FILE_ADDED, fileAddedListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_VIDEO_ON && videoOnListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_VIDEO_ON, videoOnListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED && connectionFailureListener != null) {
            Log.e(TAG, "1111connectionFailureListener != null");
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_CONNECTION_DISCONNECTED, connectionFailureListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_TIMELAPSE_STOP && timeLapseStopListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_TIMELAPSE_STOP, timeLapseStopListener);
        }

        if (iCatchEventID == ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR && serverStreamErrorListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SERVER_STREAM_ERROR, serverStreamErrorListener);
        }

        if (iCatchEventID == ICatchEventID.ICH_EVENT_FILE_DOWNLOAD && fileDownloadListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FILE_DOWNLOAD, fileDownloadListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED && updateFWCompletedListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_COMPLETED, updateFWCompletedListener);
        }

        if (iCatchEventID == ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF && updateFWPoweroffListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_FW_UPDATE_POWEROFF, updateFWPoweroffListener);
        }
        if (iCatchEventID == ICatchEventID.ICH_EVENT_SDCARD_REMOVED && noSdcardListener != null) {
            cameraAction.delEventListener(ICatchEventID.ICH_EVENT_SDCARD_REMOVED, noSdcardListener);
        }

    }

    public class SdcardStateListener implements ICatchWificamListener {

        @Override
        public void eventNotify(ICatchEvent arg0) {

            handler.obtainMessage(GlobalInfo.EVENT_SD_CARD_FULL).sendToTarget();
            Log.e(TAG, "[Normal] -- Main: event: EVENT_SD_CARD_FULL");
        }
    }

    /**
     * Added by zhangyanhu C01012,2014-3-7
     */
    public class BatteryStateListener implements ICatchWificamListener {

        @Override
        public void eventNotify(ICatchEvent arg0) {

            handler.obtainMessage(GlobalInfo.EVENT_BATTERY_ELETRIC_CHANGED).sendToTarget();
        }
    }

    public class CaptureDoneListener implements ICatchWificamListener {

        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:capture done");
            handler.obtainMessage(GlobalInfo.EVENT_CAPTURE_COMPLETED).sendToTarget();
        }
    }

    public class CaptureStartListener implements ICatchWificamListener {

        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:capture start");
            handler.obtainMessage(GlobalInfo.EVENT_CAPTURE_START).sendToTarget();
        }
    }

    /**
     * Added by zhangyanhu C01012,2014-3-10
     */
    public class VideoOffListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:videooff");
            handler.obtainMessage(GlobalInfo.EVENT_VIDEO_OFF).sendToTarget();
        }
    }

    /**
     * Added by zhangyanhu C01012,2014-3-10
     */
    public class VideoOnListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:videoON");
            handler.obtainMessage(GlobalInfo.EVENT_VIDEO_ON).sendToTarget();
        }
    }

    /**
     * Added by zhangyanhu C01012,2014-4-1
     */
    public class FileAddedListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:FileAddedListener");
            handler.obtainMessage(GlobalInfo.EVENT_FILE_ADDED).sendToTarget();
        }
    }

    public class ConnectionFailureListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:ConnectionFailureListener");
            handler.obtainMessage(GlobalInfo.EVENT_CONNECTION_FAILURE).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class TimeLapseStopListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:TimeLapseStopListener");
            handler.obtainMessage(GlobalInfo.EVENT_TIME_LAPSE_STOP).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class ServerStreamErrorListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:ServerStreamErrorListener");
            handler.obtainMessage(GlobalInfo.EVENT_SERVER_STREAM_ERROR).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class FileDownloadListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive event:FileDownloadListener");
            Log.e(TAG, "1111receive event:FileDownloadListener");
            handler.obtainMessage(GlobalInfo.EVENT_FILE_DOWNLOAD, arg0.getFileValue1()).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class VideoRecordingTimeStartListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive VideoRecordingTimeStartListener");
            handler.obtainMessage(GlobalInfo.EVENT_VIDEO_RECORDING_TIME).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class UpdateFWCompletedListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive UpdateFWCompletedListener");
            handler.obtainMessage(GlobalInfo.EVENT_FW_UPDATE_COMPLETED).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    public class UpdateFWPoweroffListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive UpdateFWPoweroffListener");
            handler.obtainMessage(GlobalInfo.EVENT_FW_UPDATE_POWEROFF).sendToTarget();
            // sendOkMsg(EVENT_FILE_ADDED);
        }
    }

    // JIRA ICOM-1577 Begin:Add by b.jiang C01063 2015-07-17
    public static class NoSdcardListener implements ICatchWificamListener {
        @Override
        public void eventNotify(ICatchEvent arg0) {

            Log.e(TAG, "[Normal] -- SDKEvent: --------------receive NoSdcardListener");
            GlobalInfo.isSdCardExist = false;
            Log.e(TAG, "[Normal] -- SDKEvent: receive NoSdcardListener GlobalInfo.isSdCard = " + GlobalInfo.isSdCardExist);
        }
    }

    // JIRA ICOM-1577 End:Add by b.jiang C01063 2015-07-17

}
