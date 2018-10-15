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

    /**
     * 注册成功，返回GUID
     * @param context
     * @param guid
     */
    public void onReceiveGuid(Context context, String guid) {
    }

    /**
     * 透传信息处理回调方法
     *
     * @param var1
     * @param mPushMessage
     */
    public void onReceivePassThroughMessage(Context var1, InnotechMessage mPushMessage) {
    }

    /**
     * 点击通知栏消息回调方法
     *
     * @param var1
     * @param mPushMessage
     */
    public void onNotificationMessageClicked(Context var1, InnotechMessage mPushMessage) {
    }

    /**
     * 通知栏详细到达时回调
     *
     * @param var1
     * @param mPushMessage
     */
    public void onNotificationMessageArrived(Context var1, InnotechMessage mPushMessage) {

    }

}
