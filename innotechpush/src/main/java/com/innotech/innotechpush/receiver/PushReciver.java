package com.innotech.innotechpush.receiver;

import android.content.Context;

import com.innotech.innotechpush.bean.InnotechMessage;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;

/**
 * 对消息进行处理
 * 对外提供抽象方法
 */

public class PushReciver {

    public PushReciver() {

    }
    public void onReceivePassThroughMessage(Context var1, InnotechMessage mPushMessage) {
    }

    public void onNotificationMessageClicked(Context var1,InnotechMessage mPushMessage) {
    }

    public void onNotificationMessageArrived(Context var1, InnotechMessage mPushMessage) {

    }

    @Deprecated
    public void onReceiveMessage(Context var1, InnotechMessage mPushMessage) {

    }

}
