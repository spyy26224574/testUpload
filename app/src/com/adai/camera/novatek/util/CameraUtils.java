package com.adai.camera.novatek.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.adai.camera.novatek.consant.NovatekWifiCommands;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.utils.LogUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MovieRecord;
import com.example.ipcamera.domain.MovieRecordValue;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;


/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/10/28 14:21
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class CameraUtils {
    public static final int MODE_PHOTO = 0;
    public static final int MODE_MOVIE = 1;
    public static final int MODE_PLAYBACK = 2;
    public static int CURRENT_MODE = MODE_MOVIE;
    private static final int VOLLEYTIMEOUT = 5000;
    public static boolean hasSDCard = true;
    public static boolean isRecording;
    public static boolean isCardFull = false;
    public static PRODUCT currentProduct = PRODUCT.DEFAULT;
    public static String SDCARD_MEMORY = "";
    public static String FREE_MEMORY="";

    public enum PRODUCT {
        //时隽
        SJ,
        //鼎创通
        DCT,
        //默认
        DEFAULT
    }

    public interface CmdCallback {
        void success(int commandId, String par, MovieRecord movieRecord);

        void failed(int commandId, String par, String error);
    }

    public interface CmdListener {
        void onResponse(String response);

        void onErrorResponse(Exception volleyError);
    }

    public interface cmdListenerIgnoreResponse {
        void onResponse();
    }

    public interface ImmediateCmdListener {
        boolean onResponse(String response);

        void onStreamRefreshed(boolean isRefreshed);

        void onErrorResponse(Exception e);
    }

    public interface RecordStatusListener {
        void success(boolean isRecording);

        void error(String error);
    }

    public interface RecordTimeCallback {
        void success(int time);

        void error(String error);
    }

    public interface ToggleStatusListener {
        void success();

        void error(String error);
    }

    public interface SDCardStatusListener {
        void success(int status);

        void error(String error);
    }

    public interface GetFileListListener {
        void success(List<FileDomain> fileDomains);

        void error(String error);
    }

    public static void sendCmd(String url, final CmdListener cmdListener) {
        LogUtils.e(url);
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (cmdListener != null) {
                    cmdListener.onResponse(s);
                    LogUtils.e(s);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (cmdListener != null) {
                    cmdListener.onErrorResponse(volleyError);
                }
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(6000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public static void sendImmediateCmd(final String url, final ImmediateCmdListener immediateCmdListener) {
        LogUtils.e(url);
        sendCmd(Contacts.URL_MOVIE_LIVE_VIEW_START + 0, new CmdListener() {
            @Override
            public void onResponse(String response) {
                sendCmd(url, new CmdListener() {
                    @Override
                    public void onResponse(final String response) {
                        if (immediateCmdListener != null) {
                            sendCmd(Contacts.URL_MOVIE_LIVE_VIEW_START + 1, new CmdListener() {
                                @Override
                                public void onResponse(String response_) {
                                    immediateCmdListener.onStreamRefreshed(true);
                                    boolean success = immediateCmdListener.onResponse(response);
                                }

                                @Override
                                public void onErrorResponse(Exception volleyError) {
                                    immediateCmdListener.onStreamRefreshed(false);
                                    boolean success = immediateCmdListener.onResponse(response);
                                }
                            });
                        }
                    }

                    @Override
                    public void onErrorResponse(Exception volleyError) {
                        if (immediateCmdListener != null) {
                            immediateCmdListener.onErrorResponse(volleyError);
                        }
                    }
                });
//                try {
//                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                    DomParseUtils domParseUtils = new DomParseUtils();
//                    MovieRecord record = domParseUtils.getParserXml(is);
//                    if (record != null && record.getStatus().equals("0")) {
//
//                    } else {
//                        if (immediateCmdListener != null) {
//                            immediateCmdListener.onErrorResponse(new Exception("设置失败"));
//                        }
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    if (immediateCmdListener != null) {
//                        immediateCmdListener.onErrorResponse(e);
//                    }
//                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (immediateCmdListener != null) {
                    immediateCmdListener.onErrorResponse(volleyError);
                }
            }
        });
    }

    public static void syncTime(final CmdListener cmdListener) {
        SimpleDateFormat day = new SimpleDateFormat("yyyy-MM-dd");
        String date = day.format(new java.util.Date(System.currentTimeMillis()));
        CameraUtils.sendCmd(Contacts.URL_SET_DATE + date, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String time = sdf.format(new java.util.Date(System.currentTimeMillis()));
                CameraUtils.sendCmd(Contacts.URL_SET_TIME + time, new CameraUtils.CmdListener() {
                    @Override
                    public void onResponse(String response) {
                        if (cmdListener != null) {
                            cmdListener.onResponse(response);
                        }
                    }

                    @Override
                    public void onErrorResponse(Exception volleyError) {
                        if (cmdListener != null) {
                            cmdListener.onErrorResponse(volleyError);
                        }
                    }
                });
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (cmdListener != null) {
                    cmdListener.onErrorResponse(volleyError);
                }
            }
        });
    }

    /**
     * 安全的恢复录制（没有处于录制状态才会开启录制）
     */
    public static void saveStartRecord() {
        StringRequest req = new StringRequest(Contacts.URL_MOVIE_RECORDING_TIME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Log.e("info", "result = " + result);
                            DomParseUtils dom = new DomParseUtils();
                            InputStream is = new ByteArrayInputStream(response
                                    .getBytes("utf-8"));
                            MovieRecordValue movieRecordValue = dom
                                    .getParserXmls(is);
                            if ("0".equals(movieRecordValue.getValue())) {
                                //没有在录制
                                StringRequest req = new StringRequest(Contacts.URL_MOVIE_RECORD + 1, null, null);
                                req.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                                VLCApplication.getInstance().addToRequestQueue(req);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    public static void startRecord(boolean isRecording, CmdListener cmdListener) {
        if (!isRecording) {
            sendCmd(Contacts.URL_MOVIE_RECORD + 1, cmdListener);
        } else {
            if (cmdListener != null) {
                cmdListener.onResponse("已经开启录制");
            }
        }
    }

    /**
     * @param record
     */
    public static void saveChangeRecordState(final boolean record, @NonNull final CmdListener cmdListener) {
        StringRequest req = new StringRequest(Contacts.URL_MOVIE_RECORDING_TIME,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            // Log.e("info", "result = " + result);
                            DomParseUtils dom = new DomParseUtils();
                            InputStream is = new ByteArrayInputStream(response
                                    .getBytes("utf-8"));
                            MovieRecordValue movieRecordValue = dom
                                    .getParserXmls(is);
                            if ("0".equals(movieRecordValue.getValue())) {
                                //没有在录制
                                if (record) {
                                    sendCmd(Contacts.URL_MOVIE_RECORD + 1, cmdListener);
                                } else {
                                    cmdListener.onResponse("");
                                }
                            } else {
                                //正在录制
                                if (!record) {
                                    sendCmd(Contacts.URL_MOVIE_RECORD + 0, cmdListener);
                                } else {
                                    cmdListener.onResponse("");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            cmdListener.onErrorResponse(e);
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                cmdListener.onErrorResponse(error);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(5000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    public interface ModeChangeListener {
        void success();

        void failure(Throwable throwable);
    }


    public static void changeMode(int mode, final ModeChangeListener modeChangeListener) {
        StringRequest req = new StringRequest(Contacts.URL_MODE_CHANGE + mode, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                LogUtils.e(response);
                DomParseUtils domParseUtils = new DomParseUtils();
                try {
                    ByteArrayInputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    MovieRecord record = domParseUtils.getParserXml(is);
                    if (record != null && record.getStatus().equals("0")) {
                        if (modeChangeListener != null) {
                            modeChangeListener.success();
                        }
                    } else {
                        if (modeChangeListener != null) {
                            modeChangeListener.failure(new Throwable("模式切换失败"));
                        }
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    modeChangeListener.failure(e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                modeChangeListener.failure(volleyError);
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(8000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    /**
     * 获取sd卡状态
     */
    public static void getSDCardStatus(final SDCardStatusListener sdCardStatusListener) {
        sendCmd(Contacts.URL_CHECK_SD, new CmdListener() {
            @Override
            public void onResponse(String response) {
                if (!TextUtils.isEmpty(response) && response.startsWith("<?xml")) {

                    try {
                        DomParseUtils dom = new DomParseUtils();
                        InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                        MovieRecordValue movieRecordValue = dom.getParserXmls(is);
                        int status = 1;
                        try {
                            status = Integer.valueOf(movieRecordValue.getValue());
                        } catch (NumberFormatException ignored) {
                        }
                        hasSDCard = status > 0;
                        isCardFull = (status == NovatekWifiCommands.CAMERA_CARD_STATUS + 5);
                        if (sdCardStatusListener != null) {
                            sdCardStatusListener.success(status);
                        }
//                        if ("1".equals(movieRecordValue.getValue())) {
//                            //有SD卡
//                            if (sdCardStatusListener != null) {
//                                sdCardStatusListener.success(true);
//                            }
//                        } else {
//                            //无SD卡
//                            if (sdCardStatusListener != null) {
//                                sdCardStatusListener.success(false);
//                            }
//                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        if (sdCardStatusListener != null) {
                            sdCardStatusListener.error(e.getMessage());
                        }
                    }
                } else {
                    if (sdCardStatusListener != null) {
                        sdCardStatusListener.error("获取sd卡状态失败");
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (sdCardStatusListener != null) {
                    sdCardStatusListener.error(volleyError.getMessage());
                }
            }
        });
    }

    /**
     * 获取文件列表
     */
    public static void getFileList(final GetFileListListener getFileListListener) {
        sendCmd(Contacts.URL_FILE_LIST, new CmdListener() {
            @Override
            public void onResponse(String response) {
                LogUtils.e("文件列表\n" + response);
                if (!TextUtils.isEmpty(response) && response.startsWith("<?xml")) {
                    InputStream is = null;
                    try {
                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
                        DomParseUtils dom = new DomParseUtils();
                        List<FileDomain> fileDomains = dom.parsePullXml(is);
                        if (getFileListListener != null) {
                            getFileListListener.success(fileDomains);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (getFileListListener != null) {
                            getFileListListener.error(e.getMessage());
                        }
                    }
                } else {
                    getFileListListener.error("获取文件列表失败");
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (getFileListListener != null) {
                    getFileListListener.error(volleyError.getMessage());
                }
            }
        });
    }

    public static void getRecordTime(final RecordTimeCallback recordTimeCallback) {
        sendCmd(Contacts.URL_MOVIE_RECORDING_TIME, new CmdListener() {
            @Override
            public void onResponse(String response) {
                try {
                    DomParseUtils dom = new DomParseUtils();
                    InputStream is = new ByteArrayInputStream(response
                            .getBytes("utf-8"));
                    MovieRecordValue movieRecordValue = dom
                            .getParserXmls(is);
                    if (recordTimeCallback != null) {
                        recordTimeCallback.success(Integer.parseInt(movieRecordValue.getValue()));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (recordTimeCallback != null) {
                        recordTimeCallback.error(e.getMessage());
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (recordTimeCallback != null) {
                    recordTimeCallback.error(volleyError.getMessage());
                }
            }
        });
    }

    /**
     * 获取录制状态
     */
    public static void getRecordStatus(final RecordStatusListener recordStatusListener) {
        String url = Contacts.URL_MOVIE_RECORDING_TIME;
        sendCmd(url, new CmdListener() {
            @Override
            public void onResponse(String response) {
                try {
                    DomParseUtils dom = new DomParseUtils();
                    InputStream is = new ByteArrayInputStream(response
                            .getBytes("utf-8"));
                    MovieRecordValue movieRecordValue = dom
                            .getParserXmls(is);
                    if ("0".equals(movieRecordValue.getValue())) {
                        if (recordStatusListener != null) {
                            recordStatusListener.success(false);
                        }
                    } else {
                        if (recordStatusListener != null) {
                            recordStatusListener.success(true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (recordStatusListener != null) {
                        recordStatusListener.error(e.getMessage());
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (recordStatusListener != null) {
                    recordStatusListener.error(volleyError.getMessage());
                }
            }
        });
    }

    /**
     * 切换录制状态
     */
    public static void toggleRecordStatus(boolean startRecord, final ToggleStatusListener toggleStatusListener) {
        sendCmd(Contacts.URL_MOVIE_RECORD + (startRecord ? "1" : "0"), new CmdListener() {
            @Override
            public void onResponse(String response) {
                InputStream is = null;
                try {
                    is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    DomParseUtils domParseUtils = new DomParseUtils();
                    MovieRecord record = domParseUtils.getParserXml(is);
                    if (record != null && "0".equals(record.getStatus())) {
                        if (toggleStatusListener != null) {
                            toggleStatusListener.success();
                        }
                    } else {
                        if (toggleStatusListener != null) {
                            toggleStatusListener.error("");
                        }
                    }
//                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    if (toggleStatusListener != null) {
                        toggleStatusListener.error(e.getMessage());
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (toggleStatusListener != null) {
                    toggleStatusListener.error(volleyError.getMessage());
                }
            }
        });
    }

    public static void getStreamUrl(CmdListener cmdListener) {
        String url = Contacts.BASE_URL + NovatekWifiCommands.GET_STREAM_URL;
        CameraUtils.sendCmd(url, cmdListener);
    }

    public static void sendCmdIgnoreResponse(int commandId, String par, final cmdListenerIgnoreResponse cmdListener) {
        sendCmd(commandId, par, new CmdCallback() {
            @Override
            public void success(int commandId, String par, MovieRecord movieRecord) {
                cmdListener.onResponse();
            }

            @Override
            public void failed(int commandId, String par, String error) {
                cmdListener.onResponse();
            }
        });
    }

    public static void sendCmd(final int commandId, final String par, final CmdCallback cmdCallback) {
//        if (commandId == NovatekWifiCommands.MOVIE_DATE_PRINT) {
//            CameraUtils.sendImmediateCmd(Contacts.BASE_URL + commandId + "&par=" + par, new CameraUtils.ImmediateCmdListener() {
//                @Override
//                public boolean onResponse(String response) {
//                    InputStream is;
//                    try {
//                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                        DomParseUtils domParseUtils = new DomParseUtils();
//                        MovieRecord record = domParseUtils.getParserXml(is);
//                        if (cmdCallback != null) {
//                            cmdCallback.success(commandId, par, record);
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                        if (cmdCallback != null) {
//                            cmdCallback.failed(commandId, e.getMessage());
//                        }
//                    }
//                    return true;
//                }
//
//                @Override
//                public void onStreamRefreshed(boolean isRefreshed) {
//
//                }
//
//                @Override
//                public void onErrorResponse(Exception e) {
//                    if (cmdCallback != null) {
//                        cmdCallback.failed(commandId, e.getMessage());
//                    }
//                }
//            });
//            return;
//        }
        String url = Contacts.BASE_URL + commandId + "&par=" + par;
        if (TextUtils.isEmpty(par)) {
            url = Contacts.BASE_URL + commandId;
        }
        CameraUtils.sendCmd(url, new CameraUtils.CmdListener() {
            @Override
            public void onResponse(String response) {
                InputStream is = null;
                try {
                    is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    DomParseUtils domParseUtils = new DomParseUtils();
                    MovieRecord record = domParseUtils.getParserXml(is);
//                    if (record == null || !"0".equals(record.getStatus())) {
//                        if (cmdCallback != null) {
//                            cmdCallback.failed(commandId, par, "设置失败");
//                        }
//                    } else {
                    if (cmdCallback != null) {
                        cmdCallback.success(commandId, par, record);
                    }
//                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    if (cmdCallback != null) {
                        cmdCallback.failed(commandId, par, e.getMessage());
                    }
                }
            }

            @Override
            public void onErrorResponse(Exception volleyError) {
                if (cmdCallback != null) {
                    cmdCallback.failed(commandId, par, volleyError.getMessage());
                }
            }
        });
    }

    public static void sendAutoToggleRecordCmd(final int commandId, final String par, final CmdCallback cmdCallback) {
        String url = Contacts.BASE_URL + commandId + "&par=" + par;
        if (TextUtils.isEmpty(par)) {
            url = Contacts.BASE_URL + commandId;
        }
        if (!hasSDCard) {
            CameraUtils.sendCmd(url, new CmdListener() {
                @Override
                public void onResponse(String response) {
                    InputStream is;
                    try {
                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
                        DomParseUtils domParseUtils = new DomParseUtils();
                        final MovieRecord record = domParseUtils.getParserXml(is);
                        if (cmdCallback != null) {
                            cmdCallback.success(commandId, par, record);
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                        if (cmdCallback != null) {
                            cmdCallback.failed(commandId, par, e.getMessage());
                        }
                    }
                }

                @Override
                public void onErrorResponse(Exception volleyError) {
                    if (cmdCallback != null) {
                        cmdCallback.failed(commandId, par, volleyError.getMessage());
                    }
                }
            });
        } else {
            CameraUtils.sendCmd(NovatekWifiCommands.MOVIE_RECORD, "0", new CameraUtils.CmdCallback() {//停止录制
                @Override
                public void success(int cmd, String parameter, MovieRecord movieRecord) {
                    String url = Contacts.BASE_URL + commandId + "&par=" + par;
                    if (TextUtils.isEmpty(par)) {
                        url = Contacts.BASE_URL + commandId;
                    }
                    if (movieRecord != null && "0".equals(movieRecord.getStatus())) {
                        CameraUtils.sendCmd(url, new CameraUtils.CmdListener() {
                            @Override
                            public void onResponse(String response) {
                                InputStream is;
                                try {
                                    is = new ByteArrayInputStream(response.getBytes("utf-8"));
                                    DomParseUtils domParseUtils = new DomParseUtils();
                                    final MovieRecord record = domParseUtils.getParserXml(is);
                                    CameraUtils.startRecord(false, new CameraUtils.CmdListener() {//开启录制
                                        @Override
                                        public void onResponse(String response) {
                                            if (cmdCallback != null) {
                                                cmdCallback.success(commandId, par, record);
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(Exception volleyError) {
                                            if (cmdCallback != null) {
                                                cmdCallback.failed(commandId, par, volleyError.getMessage());
                                            }
                                        }
                                    });
//                            }
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                    if (cmdCallback != null) {
                                        cmdCallback.failed(commandId, par, e.getMessage());
                                    }
                                }
                            }

                            @Override
                            public void onErrorResponse(Exception volleyError) {
                                if (cmdCallback != null) {
                                    cmdCallback.failed(commandId, par, volleyError.getMessage());
                                }
                            }
                        });
                    } else {
                        if (cmdCallback != null) {
                            cmdCallback.failed(commandId, par, "停止录制失败");
                        }
                    }

                }

                @Override
                public void failed(int cmd, String parameter, String error) {
                    if (cmdCallback != null) {
                        cmdCallback.failed(commandId, par, "停止录制失败：" + error);
                    }
                }
            });
        }

    }

    public static void changContactsVariable(int netmode) {
        if (netmode == 0) {
            Contacts.BASE_IP = Contacts.BASE_IP_WIFI;
            Contacts.BASE_HTTP_IP = Contacts.BASE_HTTP_IP_WIFI;
            Contacts.BASE_RTSP = Contacts.BASE_RTSP_WIFI;
            Contacts.BASE_PREVIEW_HTTP = Contacts.BASE_PREVIEW_HTTP_WIFI;
            Contacts.BASE_URL = Contacts.BASE_URL_WIFI;
            Contacts.URL_MOVIE_EV = Contacts.URL_MOVIE_EV_WIFI;
            Contacts.URL_CAPTURE_SIZE = Contacts.URL_CAPTURE_SIZE_WIFI;
            Contacts.URL_LIVE_VIEW_BITRATE = Contacts.URL_LIVE_VIEW_BITRATE_WIFI;
            Contacts.URL_MOVIE_RECORDING_TIME = Contacts.URL_MOVIE_RECORDING_TIME_WIFI;
            Contacts.URL_TAKE_PHOTO = Contacts.URL_TAKE_PHOTO_WIFI;
            Contacts.URL_PHOTOGRAPH = Contacts.URL_PHOTOGRAPH_WIFI;
            Contacts.URL_SYSTEM_RESET = Contacts.URL_SYSTEM_RESET_WIFI;
            Contacts.URL_QUERY_CURRENT_STATUS = Contacts.URL_QUERY_CURRENT_STATUS_WIFI;
            Contacts.URL_FILE_LIST = Contacts.URL_FILE_LIST_WIFI;
            Contacts.URL_HEARTBEAT = Contacts.URL_HEARTBEAT_WIFI;
            Contacts.URL_SAVE_MENU_INFORMATION = Contacts.URL_SAVE_MENU_INFORMATION_WIFI;
            Contacts.URL_RECONNECT_WIFI = Contacts.URL_RECONNECT_WIFI_WIFI;
            Contacts.URL_GET_DEVICE_ID = Contacts.URL_GET_DEVICE_ID_WIFI;
            Contacts.URL_CHECK_SD = Contacts.URL_CHECK_SD_WIFI;
            Contacts.URL_GET_CAMERA_VERSION = Contacts.URL_GET_CAMERA_VERSION_WIFI;
            Contacts.URL_GET_CAMERA_INFO = Contacts.URL_GET_CAMERA_INFO_WIFI;
            Contacts.URL_SET_SSID = Contacts.URL_SET_SSID_WIFI;
            Contacts.URL_SET_PASSPHRASE = Contacts.URL_SET_PASSPHRASE_WIFI;
            Contacts.URL_SET_DATE = Contacts.URL_SET_DATE_WIFI;
            Contacts.URL_SET_TIME = Contacts.URL_SET_TIME_WIFI;
            Contacts.URL_MOVIE_RECORD = Contacts.URL_MOVIE_RECORD_WIFI;
            Contacts.URL_MOVIE_RECORD_SIZE = Contacts.URL_MOVIE_RECORD_SIZE_WIFI;
            Contacts.URL_CYCLIC_RECORD = Contacts.URL_CYCLIC_RECORD_WIFI;
            Contacts.URL_MOVIE_HDR = Contacts.URL_MOVIE_HDR_WIFI;
            Contacts.URL_MOTION_DETECTION = Contacts.URL_MOTION_DETECTION_WIFI;
            Contacts.URL_MOVIE_AUDIO = Contacts.URL_MOVIE_AUDIO_WIFI;
            Contacts.URL_MOVIE_DATE_IN_PRINT = Contacts.URL_MOVIE_DATE_IN_PRINT_WIFI;
            Contacts.URL_MOVIE_LIVE_VIEW_SIZE = Contacts.URL_MOVIE_LIVE_VIEW_SIZE_WIFI;
            Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY_WIFI;
            Contacts.URL_SET_AUTO_RECORDING = Contacts.URL_SET_AUTO_RECORDING_WIFI;
            Contacts.URL_MODE_CHANGE = Contacts.URL_MODE_CHANGE_WIFI;
            Contacts.URL_TV_FORMAT = Contacts.URL_TV_FORMAT_WIFI;
            Contacts.URL_FORMAT = Contacts.URL_FORMAT_WIFI;
            Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE = Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE_WIFI;
            Contacts.URL_GET_THUMBNAIL_HEAD_RO = Contacts.URL_GET_THUMBNAIL_HEAD_RO_WIFI;
            Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO_WIFI;
            Contacts.URL_GET_THUMBNAIL_END = Contacts.URL_GET_THUMBNAIL_END_WIFI;
            Contacts.URL_NET_MODE = Contacts.URL_NET_MODE_WIFI;
            Contacts.URL_DELETE_ALL = Contacts.URL_DELETE_ALL_WIFI;
            Contacts.URL_DELETE_ONE_FILE = Contacts.URL_DELETE_ONE_FILE_WIFI;
            Contacts.URL_CONTINUE_SHOT = Contacts.URL_CONTINUE_SHOT_WIFI;
            Contacts.URL_SELEF_TIMER = Contacts.URL_SELEF_TIMER_WIFI;
            Contacts.URL_SETWB = Contacts.URL_SETWB_WIFI;
            Contacts.URL_PHOTO_COLOR = Contacts.URL_PHOTO_COLOR_WIFI;
            Contacts.URL_PHOTO_QUALITY = Contacts.URL_PHOTO_QUALITY_WIFI;
            Contacts.URL_SET_ISO = Contacts.URL_SET_ISO_WIFI;
            Contacts.URL_SHARPNESS = Contacts.URL_SHARPNESS_WIFI;
            Contacts.URL_MOVIE_LIVE_VIEW_START = Contacts.URL_MOVIE_LIVE_VIEW_START_WIFI;
            Contacts.URL_WIFIAPP_CMD_MJPEG_RTSP = Contacts.URL_WIFIAPP_CMD_MJPEG_RTSP_WIFI;
            Contacts.URL_QUERY_MENUITEM = Contacts.URL_QUERY_MENUITEM_WIFI;
            Contacts.URL_QUERY_MOVIE_SIZE = Contacts.URL_QUERY_MOVIE_SIZE_WIFI;
            Contacts.URL_SET_PIP_STYLE = Contacts.URL_SET_PIP_STYLE_WIFI;
        } else {
            Contacts.BASE_IP = Contacts.BASE_IP_AP;
            Contacts.BASE_HTTP_IP = Contacts.BASE_HTTP_IP_AP;
            Contacts.BASE_RTSP = Contacts.BASE_RTSP_AP;
            Contacts.BASE_PREVIEW_HTTP = Contacts.BASE_PREVIEW_HTTP_AP;
            Contacts.BASE_URL = Contacts.BASE_URL_AP;
            Contacts.URL_MOVIE_EV = Contacts.URL_MOVIE_EV_AP;
            Contacts.URL_CAPTURE_SIZE = Contacts.URL_CAPTURE_SIZE_AP;
            Contacts.URL_LIVE_VIEW_BITRATE = Contacts.URL_LIVE_VIEW_BITRATE_AP;
            Contacts.URL_MOVIE_RECORDING_TIME = Contacts.URL_MOVIE_RECORDING_TIME_AP;
            Contacts.URL_TAKE_PHOTO = Contacts.URL_TAKE_PHOTO_AP;
            Contacts.URL_PHOTOGRAPH = Contacts.URL_PHOTOGRAPH_AP;
            Contacts.URL_SYSTEM_RESET = Contacts.URL_SYSTEM_RESET_AP;
            Contacts.URL_QUERY_CURRENT_STATUS = Contacts.URL_QUERY_CURRENT_STATUS_AP;
            Contacts.URL_FILE_LIST = Contacts.URL_FILE_LIST_AP;
            Contacts.URL_HEARTBEAT = Contacts.URL_HEARTBEAT_AP;
            Contacts.URL_SAVE_MENU_INFORMATION = Contacts.URL_SAVE_MENU_INFORMATION_AP;
            Contacts.URL_RECONNECT_WIFI = Contacts.URL_RECONNECT_WIFI_AP;
            Contacts.URL_GET_DEVICE_ID = Contacts.URL_GET_DEVICE_ID_AP;
            Contacts.URL_CHECK_SD = Contacts.URL_CHECK_SD_AP;
            Contacts.URL_GET_CAMERA_VERSION = Contacts.URL_GET_CAMERA_VERSION_AP;
            Contacts.URL_GET_CAMERA_INFO = Contacts.URL_GET_CAMERA_INFO_AP;
            Contacts.URL_SET_SSID = Contacts.URL_SET_SSID_AP;
            Contacts.URL_SET_PASSPHRASE = Contacts.URL_SET_PASSPHRASE_AP;
            Contacts.URL_SET_DATE = Contacts.URL_SET_DATE_AP;
            Contacts.URL_SET_TIME = Contacts.URL_SET_TIME_AP;
            Contacts.URL_MOVIE_RECORD = Contacts.URL_MOVIE_RECORD_AP;
            Contacts.URL_MOVIE_RECORD_SIZE = Contacts.URL_MOVIE_RECORD_SIZE_AP;
            Contacts.URL_CYCLIC_RECORD = Contacts.URL_CYCLIC_RECORD_AP;
            Contacts.URL_MOVIE_HDR = Contacts.URL_MOVIE_HDR_AP;
            Contacts.URL_MOTION_DETECTION = Contacts.URL_MOTION_DETECTION_AP;
            Contacts.URL_MOVIE_AUDIO = Contacts.URL_MOVIE_AUDIO_AP;
            Contacts.URL_MOVIE_DATE_IN_PRINT = Contacts.URL_MOVIE_DATE_IN_PRINT_AP;
            Contacts.URL_MOVIE_LIVE_VIEW_SIZE = Contacts.URL_MOVIE_LIVE_VIEW_SIZE_AP;
            Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY = Contacts.URL_MOVIE_G_SENSOR_SENSITIVITY_AP;
            Contacts.URL_SET_AUTO_RECORDING = Contacts.URL_SET_AUTO_RECORDING_AP;
            Contacts.URL_MODE_CHANGE = Contacts.URL_MODE_CHANGE_AP;
            Contacts.URL_TV_FORMAT = Contacts.URL_TV_FORMAT_AP;
            Contacts.URL_FORMAT = Contacts.URL_FORMAT_AP;
            Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE = Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE_AP;
            Contacts.URL_GET_THUMBNAIL_HEAD_RO = Contacts.URL_GET_THUMBNAIL_HEAD_RO_AP;
            Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO_AP;
            Contacts.URL_GET_THUMBNAIL_END = Contacts.URL_GET_THUMBNAIL_END_AP;
            Contacts.URL_NET_MODE = Contacts.URL_NET_MODE_AP;
            Contacts.URL_DELETE_ALL = Contacts.URL_DELETE_ALL_AP;
            Contacts.URL_DELETE_ONE_FILE = Contacts.URL_DELETE_ONE_FILE_AP;
            Contacts.URL_CONTINUE_SHOT = Contacts.URL_CONTINUE_SHOT_AP;
            Contacts.URL_SELEF_TIMER = Contacts.URL_SELEF_TIMER_AP;
            Contacts.URL_SETWB = Contacts.URL_SETWB_AP;
            Contacts.URL_PHOTO_COLOR = Contacts.URL_PHOTO_COLOR_AP;
            Contacts.URL_PHOTO_QUALITY = Contacts.URL_PHOTO_QUALITY_AP;
            Contacts.URL_SET_ISO = Contacts.URL_SET_ISO_AP;
            Contacts.URL_SHARPNESS = Contacts.URL_SHARPNESS_AP;
            Contacts.URL_MOVIE_LIVE_VIEW_START = Contacts.URL_MOVIE_LIVE_VIEW_START_AP;
            Contacts.URL_WIFIAPP_CMD_MJPEG_RTSP = Contacts.URL_WIFIAPP_CMD_MJPEG_RTSP_AP;
            Contacts.URL_QUERY_MENUITEM = Contacts.URL_QUERY_MENUITEM_AP;
            Contacts.URL_QUERY_MOVIE_SIZE = Contacts.URL_QUERY_MOVIE_SIZE_AP;
            Contacts.URL_SET_PIP_STYLE = Contacts.URL_SET_PIP_STYLE_AP;
        }
    }
}
