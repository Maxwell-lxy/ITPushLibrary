package com.innotech.innotechpush.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.util.Log;

import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.Utils;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.List;

/**
 * Created by admin on 2018/4/3.
 */

public class MiSDK {

    public MiSDK(Application application) {
        // 注册push服务，注册成功后会向DemoMessageReceiver发送广播
        // 可以从DemoMessageReceiver的onCommandResult方法中MiPushCommandMessage对象参数中获取注册信息
        LogUtils.e(application.getApplicationContext(), LogUtils.TAG_XIAOMI + "call MiSDK()");
        if (shouldInit(application)) {

            String appId = Utils.getMetaDataString(application, "MI_APP_ID").replace("innotech-", "");
            String appKey = Utils.getMetaDataString(application, "MI_APP_KEY").replace("innotech-", "");
            MiPushClient.registerPush(application, appId, appKey);
            LogUtils.e(application.getApplicationContext(), LogUtils.TAG_XIAOMI + "MiPushClient.registerPush appId:" + appId + " appKey:" + appKey);
        }
        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(LogUtils.TAG_XIAOMI, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(LogUtils.TAG_XIAOMI, content);
            }
        };
        Logger.setLogger(application, newLogger);

    }

    private boolean shouldInit(Application application) {
        ActivityManager am = ((ActivityManager) application.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = application.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}