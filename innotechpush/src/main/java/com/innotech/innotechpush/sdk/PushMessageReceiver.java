package com.innotech.innotechpush.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.innotech.innotechpush.config.BroadcastConstant;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.utils.CommonUtils;

public abstract class PushMessageReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        PushMessage message = (PushMessage) intent.getSerializableExtra("PushMessage");
        Log.e("allen","PushMessageReceiver的onReceive方法"+message.getAppId()+":"+appId);
        if(message.getAppId() == appId) {
            if (intent.getAction().equals(BroadcastConstant.RECEIVE_MESSAGE)) {
                if (message.getPass_through() != 1) {
                    onNotificationMessageArrived(context, message);
                } else {
                    onReceivePassThroughMessage(context, message);
                }
            } else if (intent.getAction().equals(BroadcastConstant.MESSAGE_CLICK)) {
                onNotificationMessageClicked(context, message);
            } else if (intent.getAction().equals(BroadcastConstant.ERROR)) {

            }
        }
    }

    /**
     * 用来接收服务器向客户端发送的透传消息
     *
     * @param context
     * @param pushMessage
     */
    public void onReceivePassThroughMessage(Context context, PushMessage pushMessage) {
    }

    /**
     * 用来接收服务器向客户端发送的通知消息，
     * 这个回调方法会在用户手动点击通知后触发。
     *
     * @param context
     * @param pushMessage
     */
    public void onNotificationMessageClicked(Context context, PushMessage pushMessage) {
    }

    /**
     * 用来接收服务器向客户端发送的通知消息，
     * 这个回调方法是在通知消息到达客户端时触发。
     *
     * @param context
     * @param pushMessage
     */
    public void onNotificationMessageArrived(Context context, PushMessage pushMessage) {
    }
}
