package com.innotech.innotechpush.receiver;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.huawei.hms.support.api.push.PushReceiver;
import com.innotech.innotechpush.PushApplication;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.LogUtils;
import com.xiaomi.mipush.sdk.MiPushMessage;

/**
 * Created by admin on 2018/4/4.
 */

public class HuaweiPushRevicer extends PushReceiver{
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        LogUtils.e(context,LogUtils.TAG_HUAWEI+"HuaweiPushRevicer onToken: end" + token);
    }
    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            //CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            LogUtils.e(context,LogUtils.TAG_HUAWEI+"收到PUSH透传消息,消息内容为:" + content);
            PushApplication.mPushReciver.onReceivePassThroughMessage(context,getCreateMessge(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void onEvent(Context context, Event event, Bundle extras) {
        LogUtils.e(context,LogUtils.TAG_HUAWEI+"收到通知栏消息点击事件,onEvent" );
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
            LogUtils.e(context,LogUtils.TAG_HUAWEI+"收到通知栏消息点击事件,notifyId:" + notifyId);
            if (0 != notifyId) {
                    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(notifyId);
            }

        }

        PushApplication.mPushReciver.onNotificationMessageClicked(context,getCreateMessge(extras));

        super.onEvent(context, event, extras);
    }
    @Override
    public void onPushState(Context context, boolean pushState) {
        LogUtils.e(context,LogUtils.TAG_HUAWEI+"HuaweiPushRevicer onPushState:" + pushState);
    }

    private InnotechMessage getCreateMessge(Bundle extras){
        InnotechMessage mPushMessage = new InnotechMessage();
        String message = extras.getString(BOUND_KEY.pushMsgKey);
        mPushMessage.setData(message);
        return mPushMessage;
    }

    private InnotechMessage getCreateMessge(String  message){
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setData(message);
        return mPushMessage;
    }
}
