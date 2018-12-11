package com.innotech.innotechpush.sdk;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.TokenSP;
import com.innotech.innotechpush.utils.Utils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * Created by admin on 2018/4/3.
 */

public class MiSDK {


    public MiSDK(final Context context) {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        String appId = Utils.getMetaDataString(context, "MI_APP_ID").replace("innotech-", "");
        String appKey = Utils.getMetaDataString(context, "MI_APP_KEY").replace("innotech-", "");
        LogUtils.e(context, LogUtils.TAG_XIAOMI + "MiPushClient.registerPush appId:" + appId + " appKey:" + appKey);
        MiPushClient.registerPush(context, appId, appKey);
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(LogUtils.TAG_XIAOMI, content, t);
                DbUtils.addClientLog(context, LogCode.LOG_INIT, LogUtils.TAG_XIAOMI + content);
            }

            @Override
            public void log(String content) {
                Log.d(LogUtils.TAG_XIAOMI, content);
                DbUtils.addClientLog(context, LogCode.LOG_INIT, LogUtils.TAG_XIAOMI + content);
            }
        };
        Logger.setLogger(context, newLogger);
        //开启一个10s的定时器，10s小米注册没有成功则注册union渠道
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                String regId = TokenSP.getString(context, TokenSP.KEY_MI_REGID, "");
                if (TextUtils.isEmpty(regId)) {
                    InnotechPushManager.getInstance().initGeTuiPush();
                    LogUtils.e(context, "MiPushClient.registerPush fail init GeTui push");
                    DbUtils.addClientLog(context, LogCode.LOG_INIT, "MiPushClient.registerPush fail init GeTui push");
                }
            }
        }, 10000);
    }

}