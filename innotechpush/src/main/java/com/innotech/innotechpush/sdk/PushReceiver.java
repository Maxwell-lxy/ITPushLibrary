package com.innotech.innotechpush.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.config.BroadcastConstant;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.AlarmManagerUtils;
import com.innotech.innotechpush.utils.CommonUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetUtil;
import com.innotech.innotechpush.utils.Utils;

import java.util.ArrayList;

public class PushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.e(context, "PushReceiver onReceive：" + action);
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_USER_PRESENT.equals(action)
                || Intent.ACTION_MEDIA_MOUNTED.equals(action)
                || Intent.ACTION_POWER_CONNECTED.equals(action)
                || Intent.ACTION_POWER_DISCONNECTED.equals(action)) {
            if (!CommonUtils.isServiceRunning(context, SocketClientService.class.getName())) {
                context.startService(new Intent(context, SocketClientService.class));
            }
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            int netWorkState = NetUtil.getNetWorkState(context);
            LogUtils.e(context, "netWorkState：" + netWorkState);
            if (netWorkState != -1) {
                if (!CommonUtils.isServiceRunning(context, SocketClientService.class.getName())) {
                    context.startService(new Intent(context, SocketClientService.class));
                } else {
                    SocketManager.getInstance(context).reConnect();
                }
            }
        } else if (action.equals(BroadcastConstant.ACTION_FRESH_PUSH + context.getPackageName())) {
            LogUtils.e(context, BroadcastConstant.ACTION_FRESH_PUSH + context.getPackageName());
            if (!CommonUtils.isServiceRunning(context, SocketClientService.class.getName())) {
                context.startService(new Intent(context, SocketClientService.class));
            }
            //发送心跳包
            SocketManager.getInstance(context).sendHeartData();
            //上传本地回执
            InnotechPushMethod.uploadClientMsgNotify(context);
            //上报日志
            InnotechPushMethod.uploadLogs(context);
            //定时检查service存活，发送长连接心跳包
            AlarmManagerUtils.setHeartAlarm(context);
        } else if (BroadcastConstant.MESSAGE_CLICK.equals(action)) {
            //通知被点击之后需要给服务器发送回执
            LogUtils.e(context, "通知被点击");
            PushMessage message = (PushMessage) intent.getSerializableExtra("PushMessage");
            if (message != null) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(message.getMsg_id());
                if (message.isOffLineMsg()) {
                    SocketManager.getInstance(context).ackCmd(list, 1003);
                } else {
                    SocketManager.getInstance(context).ackCmd(list, 3);
                }
                DbUtils.addClientLog(context, LogCode.LOG_DATA_NOTIFY, "通知被点击：" + message.getMsg_id());
            }
        } else if (BroadcastConstant.RECEIVE_MESSAGE.equals(action)) {
            //目前的逻辑
            //收到消息（无论通知消息还是透传消息）后，判断通知权限是否开启，若开启，则给服务器回执消息展示，若未开启，则不回执
            Integer appId = CommonUtils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            PushMessage message = (PushMessage) intent.getSerializableExtra("PushMessage");
            if (CommonUtils.isNotificationEnabled(context) && message.getAppId() == appId) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(message.getMsg_id());
                if (message.isOffLineMsg()) {
                    SocketManager.getInstance(context).ackCmd(list, 1002);
                } else if (CommonUtils.isXiaomiDevice()
                        || CommonUtils.isMIUI()
                        || CommonUtils.isMeizuDevice()
                        || (Utils.isHuaweiDevice() && PushConstant.hasHuawei && HuaweiSDK.isUpEMUI41())
//                        || (Utils.isOPPO() && PushConstant.hasOppo && com.coloros.mcssdk.PushManager.isSupportPush(context))
                        ) {
                    SocketManager.getInstance(context).ackCmd(list, 102);
                } else {
                    SocketManager.getInstance(context).ackCmd(list, 2);
                }
                DbUtils.addClientLog(context, LogCode.LOG_DATA_NOTIFY, "显示通知：" + message.getMsg_id());
            }
        }
    }


}
