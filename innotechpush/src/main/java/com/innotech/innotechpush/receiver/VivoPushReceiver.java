package com.innotech.innotechpush.receiver;

import android.content.Context;

import com.innotech.innotechpush.utils.LogUtils;
import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

public class VivoPushReceiver extends OpenClientPushMessageReceiver {
    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage upsNotificationMessage) {

    }

    @Override
    public void onReceiveRegId(Context context, String regId) {
        LogUtils.e(context, "vivo regId：" + regId);
    }
}
