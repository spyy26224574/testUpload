package com.adai.camera.hisi.sdk;

import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Created by cWX212165 on 2016/4/12.
 */
public class SyncMessageManager {
    private static final String TAG = "SyncMessageManager";

    public static final int MSG_SYNC_STATE   = 0;    //工作模式，状态消息
    public static final int MSG_SYNC_SETTING = 1;    //设置变动消息，预留

    //DV端同步消息state对应的数字
    public static final int REMOTE_STATE_START_LOOPRECORD = 0;
    public static final int REMOTE_STATE_START_RECORD = 1;
    public static final int REMOTE_STATE_START_TIMELAPSE = 2;
    public static final int REMOTE_STATE_START_TIMER = 3;
    public static final int REMOTE_STATE_STOP_RECORD = 4;
    public static final int REMOTE_STATE_STOP_TIMELAPSE = 5;
    public static final int REMOTE_STATE_STOP_TIMER = 6;
    public static final int REMOTE_STATE_START_RECORD_TIMELAPSE = 8; //启动缩时录像

    /**
     * 根据JSON字符串解析为消息对象
     * @param data
     * @return
     */
    public static Message parseSyncMessage(String data){

        Message msg = null;

        try {
            JSONObject object = (JSONObject)new JSONTokener(data).nextValue();
            if( object.equals(JSONObject.NULL) ){
                return null;
            }

            msg = new Message();
            try {
                msg.what = object.getInt("type");
            }
            catch(JSONException e){
                msg.what = MSG_SYNC_STATE; //默认消息类型，历史版本的状态同步消息无此字段
            }

            switch (msg.what){
                case MSG_SYNC_STATE:
                    SyncStateMessage syncStateMessage = new SyncStateMessage();

                    try {
                        syncStateMessage.mode = object.getInt("mode");
                    }
                    catch(JSONException e){
                        syncStateMessage.mode = -1; //历史版本的消息中无工作模式
                    }

                    syncStateMessage.state = object.getInt("state");
                    syncStateMessage.event = object.getInt("event");
                    syncStateMessage.pasttime = object.getInt("pasttime");

                    msg.obj = syncStateMessage;

                    break;

                case MSG_SYNC_SETTING:
                    //预留类型
                    break;

                default:
                    return null;
            }

        } catch (Exception e) {
            Log.i(TAG, "parseSyncMessage failed: " + data);
            e.printStackTrace();
            return null;
        }
        return msg;
    }
}
