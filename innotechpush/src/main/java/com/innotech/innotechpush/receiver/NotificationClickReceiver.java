package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.AppUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.socket_library.constants.BroadcastConstant;
import com.innotech.socket_library.sdk.PushMessage;

/**
 * 个推和友盟通过透传的方式展示的消息，点击后的回调
 */

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        InnotechMessage myMsg = (InnotechMessage) intent.getSerializableExtra("InnotechMessage");
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, myMsg);
            LogUtils.i(context, "AppUtils.appIsBackground(context):" + AppUtils.appIsBackground(context) + " myMsg.getActionContent():" + myMsg.getActionContent());
            if (null != myMsg.getActionContent() && myMsg.getActionContent().length() > 0) {
                Uri uri = Uri.parse(myMsg.getActionContent());
                Intent intentUrl = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intentUrl);
            } else {
                if (AppUtils.appIsBackground(context)) {
                    //打开应用
                    Intent launchIntent = context.getPackageManager().
                            getLaunchIntentForPackage(context.getPackageName());
                    launchIntent.setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                    context.startActivity(launchIntent);
                }
            }

        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }

        //通知被点击后发送一个回执的广播
        PushMessage message = new PushMessage();
        message.setMsg_id(myMsg.getMessageId());
        message.setTitle(myMsg.getTitle());
        message.setContent(myMsg.getContent());
        if(!TextUtils.isEmpty(myMsg.getCustom())){
            message.setTransmission(myMsg.getCustom());
        }
        message.setPass_through(myMsg.getPushType());
        Intent intent1 = new Intent();
        intent1.setAction(BroadcastConstant.MESSAGE_CLICK);
        intent1.putExtra("PushMessage", message);
        context.sendBroadcast(intent1);
    }
}
