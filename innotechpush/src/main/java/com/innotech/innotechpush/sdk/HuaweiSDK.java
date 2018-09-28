package com.innotech.innotechpush.sdk;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Process;

import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.LogUtils;

import java.util.List;

public class HuaweiSDK {

    public HuaweiSDK(Application application) {
        if (shouldInit(application.getApplicationContext())) {
            LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + " HMSAgent.init");
            HMSAgent.init(application);
        }
    }

    /**
     * for HuaWei push SDK
     * 要在activity中调用才能与华为建立连接
     *
     * @param activity
     */
    public static void huaWeiConnect(final Activity activity) {
        HMSAgent.connect(activity, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                if (rst == 0) {
                    LogUtils.e(activity.getApplicationContext(), LogUtils.TAG_HUAWEI + "HMS connect end:" + rst);
                    DbUtils.addClientLog(activity.getApplicationContext(), LogCode.LOG_INIT, LogUtils.TAG_HUAWEI + "HMS connect end:" + rst);
                    HMSAgent.Push.getToken(new GetTokenHandler() {
                        @Override
                        public void onResult(int rtnCode) {
                            LogUtils.e(activity.getApplicationContext(), LogUtils.TAG_HUAWEI + "get token: end" + rtnCode);
                        }
                    });
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            huaWeiConnect(activity);
                        }
                    }, 1000);
                }
            }
        });
    }

    private boolean shouldInit(Context context) {
        ActivityManager am = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = context.getPackageName();
        int myPid = Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }
}
