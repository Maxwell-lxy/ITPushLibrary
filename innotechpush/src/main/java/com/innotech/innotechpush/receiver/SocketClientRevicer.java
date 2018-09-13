package com.innotech.innotechpush.receiver;

import android.content.Context;
import android.text.TextUtils;

import com.coloros.mcssdk.PushManager;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NotificationUtils;
import com.innotech.innotechpush.utils.SPUtils;
import com.innotech.innotechpush.utils.Utils;
import com.innotech.socket_library.PushMessageReceiver;
import com.innotech.socket_library.sdk.PushMessage;

import org.json.JSONException;
import org.json.JSONObject;

public class SocketClientRevicer extends PushMessageReceiver {

    @Override
    public void onReceivePassThroughMessage(Context context, PushMessage pushMessage) {
        super.onReceivePassThroughMessage(context, pushMessage);
        if (!(Utils.isXiaomiDevice() || Utils.isMIUI())
                && !Utils.isMeizuDevice()
//                && !Utils.isHuaweiDevice()
                && !PushManager.isSupportPush(context)) {
            try {
                if (!TextUtils.isEmpty(pushMessage.getTransmission())) {
                    JSONObject object = new JSONObject(pushMessage.getTransmission());
                    String idempotent = object.getString("idempotent");
                    InnotechPushManager.getIdempotentLock().lock();
                    try {
                        if (!TextUtils.isEmpty(idempotent)) {
                            //消息池去重验证
                            if (SPUtils.isPass(context, idempotent)) {
                                //展示通知
                                NotificationUtils.sendNotificationByStyle(context, createMessageByJson(pushMessage));
                                //消息存入消息池中
                                SPUtils.put(context, idempotent, System.currentTimeMillis());
                                if (InnotechPushManager.getPushReciver() != null) {
                                    InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context, getInnotechMessage(pushMessage));
                                } else {
                                    InnotechPushManager.innotechPushReciverIsNull(context);
                                }
                            } else {
                                LogUtils.e(context, LogUtils.TAG_INNOTECH + " 该消息为重复消息，过滤掉，不做处理" + pushMessage.getTransmission());
                                //触发一次消息池的清理
                                SPUtils.clearPoor(context);
                            }
                        } else {
                            LogUtils.e(context, LogUtils.TAG_INNOTECH + " 该消息中没有包含idempotent字段，不做处理" + pushMessage.getTransmission());
                        }
                    } finally {
                        InnotechPushManager.getIdempotentLock().unlock();
                    }
                }
            } catch (JSONException e) {
                LogUtils.e(context, LogUtils.TAG_INNOTECH + " dealWithCustomMessage方法中json转换失败");
            }
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, PushMessage pushMessage) {
        super.onNotificationMessageClicked(context, pushMessage);
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, getInnotechMessage(pushMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, PushMessage pushMessage) {
        super.onNotificationMessageArrived(context, pushMessage);
        if (!(Utils.isXiaomiDevice() || Utils.isMIUI())
                && !Utils.isMeizuDevice()
//                && !Utils.isHuaweiDevice()
                && !PushManager.isSupportPush(context)) {
            if (InnotechPushManager.getPushReciver() != null) {
                InnotechPushManager.getPushReciver().onNotificationMessageArrived(context, getInnotechMessage(pushMessage));
            } else {
                InnotechPushManager.innotechPushReciverIsNull(context);
            }
        }
    }

    private InnotechMessage getInnotechMessage(PushMessage pushMessage) {
        InnotechMessage message = new InnotechMessage();
        if (pushMessage != null) {
            message.setMessageId(pushMessage.getMsg_id() + "");
            message.setTitle(pushMessage.getTitle());
            message.setContent(pushMessage.getContent());
            if (pushMessage.getPass_through() != 1) {
                message.setPushType(0);
            } else {
                message.setPushType(1);
            }
            if (!TextUtils.isEmpty(pushMessage.getTransmission())) {
                message.setCustom(pushMessage.getTransmission());
            }
        }
        return message;
    }

    private InnotechMessage createMessageByJson(PushMessage pushMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(pushMessage.getTitle());
        mPushMessage.setContent(pushMessage.getContent());
        mPushMessage.setCustom(pushMessage.getTransmission());
        mPushMessage.setMessageId(pushMessage.getMsg_id());
        mPushMessage.setStyle(pushMessage.getStyle());
        mPushMessage.setUnfold(pushMessage.getUnfold());
        return mPushMessage;
    }
}
