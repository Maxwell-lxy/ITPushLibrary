package com.innotech.innotechpush.service;

import android.content.Context;
import android.text.TextUtils;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.PushApplication;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.SPUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 个推的回调方法
 */

public class GTPushIntentService extends GTIntentService {

    public GTPushIntentService() {

    }
    @Override
    public void onReceiveServicePid(Context context, int i) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onReceiveServicePid -> " + "ServicePid = " + i);
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onReceiveClientId -> " + "clientid = " + clientid);
        UserInfoUtils.deviceToken.setGetui(clientid);
        UserInfoUtils.sendBroadcast(context);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        try {
            byte[] payload = gtTransmitMessage.getPayload();
            String data = new String(payload);
            JSONObject object = new JSONObject(data);
            String title = object.getString("title");
            String content = object.getString("content");
            String idempotent = object.getString("idempotent");
            if(!TextUtils.isEmpty(idempotent)){
                //消息池去重验证
                if(SPUtils.isPass(context,idempotent)){
                    //展示通知
                    Utils.showNotification(context,title,content);
                    //消息存入消息池中
                    SPUtils.put(context,idempotent,System.currentTimeMillis());
                }else{
                    LogUtils.e(context,LogUtils.TAG_GETUI+" 该消息为重复消息，过滤掉，不做处理"+data);
                    //触发一次消息池的清理
                    SPUtils.clearPoor(context);
                }
            }else{
                LogUtils.e(context,LogUtils.TAG_GETUI+" 该消息中没有包含idempotent字段，不做处理"+data);
            }
        } catch (JSONException e) {
            LogUtils.e(context,LogUtils.TAG_GETUI+" dealWithCustomMessage方法中json转换失败");
        }
        if(InnotechPushManager.getPushReciver()!=null){
            InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context,getCreateMessge(gtTransmitMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onReceiveOnlineState() -> " + "b = " +b);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onReceiveCommandResult() -> ");
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onNotificationMessageArrived() -> ");
        if(InnotechPushManager.getPushReciver()!=null){
            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context,getCreateMessge(gtNotificationMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI+"onNotificationMessageClicked() -> ");
        if(InnotechPushManager.getPushReciver()!=null){
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context,getCreateMessge(gtNotificationMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    private InnotechMessage getCreateMessge(GTNotificationMessage gtNotificationMessage){
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(gtNotificationMessage.getTitle());
        mPushMessage.setContent(gtNotificationMessage.getContent());
        return mPushMessage;
    }
    private InnotechMessage getCreateMessge(GTTransmitMessage gtTransmitMessage){
        InnotechMessage mPushMessage = new InnotechMessage();
        byte[] payload =  gtTransmitMessage.getPayload();
        String data = new String(payload);
        mPushMessage.setData(data);
        return mPushMessage;
    }
}
