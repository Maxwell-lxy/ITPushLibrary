package com.innotech.innotechpush.service;

import android.content.Context;
import android.text.TextUtils;

import com.igexin.sdk.GTIntentService;
import com.igexin.sdk.message.GTCmdMessage;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.BroadcastUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NotificationUtils;
import com.innotech.innotechpush.utils.SPUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 个推的回调方法
 */

public class PushIntentService extends GTIntentService {

    public PushIntentService() {

    }

    @Override
    public void onReceiveServicePid(Context context, int i) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onReceiveServicePid -> " + "ServicePid = " + i);
    }

    @Override
    public void onReceiveClientId(Context context, String clientid) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onReceiveClientId -> " + "clientid = " + clientid);
        UserInfoModel.getInstance().setDevice_token1(clientid);
        BroadcastUtils.sendUpdateUserInfoBroadcast(context);
    }

    @Override
    public void onReceiveMessageData(Context context, GTTransmitMessage gtTransmitMessage) {
        try {
            byte[] payload = gtTransmitMessage.getPayload();
            String data = new String(payload);

            LogUtils.e(context, "GeTui onReceiveMessageData() data:" + data + "  gtTransmitMessage.getPayloadId():" + gtTransmitMessage.getPayloadId());
            JSONObject object = new JSONObject(data);
            String idempotent = object.getString("idempotent");
            InnotechPushManager.getIdempotentLock().lock();
            try {
                if (!TextUtils.isEmpty(idempotent)) {
                    //消息池去重验证
                    if (SPUtils.isPass(context, idempotent)) {
                        //展示通知
                        NotificationUtils.sendNotificationByStyle(context, createMessageByJson(gtTransmitMessage));
                        //消息存入消息池中
                        SPUtils.put(context, idempotent, System.currentTimeMillis());
                        if (InnotechPushManager.getPushReciver() != null) {
                            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context, createMessageByJson(gtTransmitMessage));
                        } else {
                            InnotechPushManager.innotechPushReciverIsNull(context);
                        }
                    } else {
                        LogUtils.e(context, LogUtils.TAG_GETUI + " 该消息为重复消息，过滤掉，不做处理" + data);
                        //触发一次消息池的清理
                        SPUtils.clearPoor(context);
                    }
                } else {
                    LogUtils.e(context, LogUtils.TAG_GETUI + " 该消息中没有包含idempotent字段，不做处理" + data);
                }
            } finally {
                InnotechPushManager.getIdempotentLock().unlock();
            }
        } catch (JSONException e) {
            LogUtils.e(context, LogUtils.TAG_GETUI + " dealWithCustomMessage方法中json转换失败");
            DbUtils.addClientLog(context, LogCode.LOG_EX_JSON, LogUtils.TAG_GETUI + " dealWithCustomMessage方法中json转换失败" + e.getMessage());
        }
    }

    @Override
    public void onReceiveOnlineState(Context context, boolean b) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onReceiveOnlineState() -> " + "b = " + b);
    }

    @Override
    public void onReceiveCommandResult(Context context, GTCmdMessage gtCmdMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onReceiveCommandResult() -> ");
    }

    @Override
    public void onNotificationMessageArrived(Context context, GTNotificationMessage gtNotificationMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onNotificationMessageArrived() -> ");
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context, getCreateMessge(gtNotificationMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, GTNotificationMessage gtNotificationMessage) {
        LogUtils.e(context, LogUtils.TAG_GETUI + "onNotificationMessageClicked() -> ");
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, getCreateMessge(gtNotificationMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    private InnotechMessage getCreateMessge(GTNotificationMessage gtNotificationMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(gtNotificationMessage.getTitle());
        mPushMessage.setContent(gtNotificationMessage.getContent());
        return mPushMessage;
    }

    private InnotechMessage getCreateMessge(GTTransmitMessage gtTransmitMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        byte[] payload = gtTransmitMessage.getPayload();
        String data = new String(payload);
        mPushMessage.setData(data);
        return mPushMessage;
    }

    private InnotechMessage createMessageByJson(GTTransmitMessage msg) {
        byte[] payload = msg.getPayload();
        String data = new String(payload);
        InnotechMessage mPushMessage = new InnotechMessage();
        JSONObject object = null;
        try {
            object = new JSONObject(data);
            int style = object.getInt("style");
            String taskId = object.getString("task_id");
            String title = object.getString("title");
            String content = object.getString("content");
            String extra = object.getString("extra");
            String unfold = object.getString("unfold");
            int action_type = object.getInt("action_type");
            if (action_type == 2) {
                String action_content = object.getString("action_content");
                JSONObject con_object = new JSONObject(action_content);
                mPushMessage.setActionContent(con_object.getString("url"));
            }
            mPushMessage.setStyle(style);
            mPushMessage.setTitle(title);
            mPushMessage.setContent(content);
            mPushMessage.setCustom(extra);
            mPushMessage.setUnfold(unfold);
            mPushMessage.setMessageId(taskId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mPushMessage;
    }
}
