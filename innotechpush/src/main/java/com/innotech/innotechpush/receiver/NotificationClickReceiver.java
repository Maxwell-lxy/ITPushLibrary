package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.PushApplication;
import com.innotech.innotechpush.bean.InnotechMessage;

/**
 * 个推和友盟通过透传的方式展示的消息，点击后的回调
 */

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PushApplication.mPushReciver.onNotificationMessageClicked(context,new InnotechMessage());
    }
}
