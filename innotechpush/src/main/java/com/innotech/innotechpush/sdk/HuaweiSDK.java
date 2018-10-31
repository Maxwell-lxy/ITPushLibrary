package com.innotech.innotechpush.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.DbUtils;
import com.innotech.innotechpush.utils.LogUtils;

import java.lang.reflect.Method;

public class HuaweiSDK {

    public HuaweiSDK(Application application) {
        LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + " HMSAgent.init");
        HMSAgent.init(application);
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

    /**
     * 判断EMUI版本>=4.1
     * 华为推送需要EMUI版本>=4.1
     */
    public static boolean isUpEMUI41() {
        int emuiApiLevel = 0;
        try {
            Class cls = Class.forName("android.os.SystemProperties");
            Method method = cls.getDeclaredMethod("get", new Class[]{String.class});
            emuiApiLevel = Integer.parseInt((String) method.invoke(cls, new Object[]{"ro.build.hw_emui_api_level"}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return emuiApiLevel >= 10;
    }

}
