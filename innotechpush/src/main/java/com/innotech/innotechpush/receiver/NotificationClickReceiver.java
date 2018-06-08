package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.AppUtils;
import com.innotech.innotechpush.utils.LogUtils;

/**
 * 个推和友盟通过透传的方式展示的消息，点击后的回调
 */

public class NotificationClickReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechMessage myMsg = (InnotechMessage) intent.getSerializableExtra("InnotechMessage");
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, myMsg);
            LogUtils.i(context,"AppUtils.appIsBackground(context):"+AppUtils.appIsBackground(context)+" myMsg.getActionContent():"+myMsg.getActionContent());
            if(null!=myMsg.getActionContent()&&myMsg.getActionContent().length()>0){
                Uri uri = Uri.parse(myMsg.getActionContent());
                Intent intentUrl = new Intent(Intent.ACTION_VIEW, uri);
                context.startActivity(intentUrl);
            }else{
                if(AppUtils.appIsBackground(context)){
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
    }
}
