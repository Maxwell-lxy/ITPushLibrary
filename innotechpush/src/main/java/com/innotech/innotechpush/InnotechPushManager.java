package com.innotech.innotechpush;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.receiver.PushReciver;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.sdk.SocketClientService;
import com.innotech.innotechpush.service.OppoPushCallback;
import com.innotech.innotechpush.service.PushIntentService;
import com.innotech.innotechpush.service.PushService;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.SPUtils;
import com.innotech.innotechpush.utils.Utils;
import com.meizu.cloud.pushsdk.PushManager;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * SDK核心类，SDK初始化用到
 * 单例类
 */

public class InnotechPushManager {
    private static InnotechPushManager mInnotechPushManager = null;
    private Application application;
    private static PushReciver mPushReciver;
    public static String pushSDKName = null;
    public static String miSDKName = "mi";
    //    public static String huaweiSDKName = "huawei";
    public static String meizuSDKName = "meizu";
    public static String oppoSDKName = "oppo";
    public static String otherSDKName = "union ";
    /**
     * 个推和集团长连接做幂等时需要加锁，防止两个回调相隔时间较近或同时到达。
     */
    private static Lock idempotentLock;
    /**
     * 通知栏图标
     */
    public static int pushIcon = R.mipmap.ic_launcher;

    public InnotechPushManager() {

    }

    public static InnotechPushManager getInstance() {
        if (mInnotechPushManager == null) {
            mInnotechPushManager = new InnotechPushManager();
        }
        return mInnotechPushManager;
    }

    /**
     * for HuaWei push SDK
     * 要在activity中调用才能与华为建立连接
     *
     * @param activity
     */
    public void setLauncherActivity(Activity activity) {
        if (Utils.isHuaweiDevice()) {
//            huaWeiConnect(activity);
        }
    }

    /**
     * 初始化推送SDK
     *
     * @param application
     */
    public void initPushSDK(Application application) {
        this.application = application;
        UserInfoModel.getInstance().init(application.getApplicationContext());
        String processName = getProcessName(application, android.os.Process.myPid());
        LogUtils.e(application, "当前进程名字：" + processName);
        if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
            pushSDKName = miSDKName;
            new MiSDK(application);
        }
        //魅族设备时，开启魅族推送
        else if (Utils.isMeizuDevice()) {
            pushSDKName = meizuSDKName;
            String appId = Utils.getMetaDataString(application, "MEIZU_APP_ID").replace("innotech-", "");
            String appKey = Utils.getMetaDataString(application, "MEIZU_APP_KEY");
            LogUtils.e(application.getApplicationContext(), LogUtils.TAG_MEIZU + "Meizu  PushManager.register");
            PushManager.register(application, appId, appKey);
        }
        //华为设备时，开启华为推送
//        else if (Utils.isHuaweiDevice()) {
////            pushSDKName = huaweiSDKName;
////            LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + " HMSAgent.init");
////            HMSAgent.init(application);
////        }
        //oppo设备时，开启oppo推送
//        else if (com.coloros.mcssdk.PushManager.isSupportPush(application.getApplicationContext()) && Utils.isOPPO()) {
//            pushSDKName = oppoSDKName;
//            String appKey = Utils.getMetaDataString(application, "OPPO_APP_KEY");
//            String appSecret = Utils.getMetaDataString(application, "OPPO_APP_SECRET");
//            com.coloros.mcssdk.PushManager.getInstance().register(application.getApplicationContext(), appKey, appSecret, new OppoPushCallback(application));
//        }
        //其他设备时，开启个推推送和socket长连接
        else {
            pushSDKName = otherSDKName;
            initGeTuiPush();
        }
    }

    public void initSocketPush() {
        application.getApplicationContext().startService(new Intent(application.getApplicationContext(), SocketClientService.class));
    }

    /**
     * 初始化并开启个推推送
     */
    private void initGeTuiPush() {
        LogUtils.e(application.getApplicationContext(), LogUtils.TAG_GETUI + "call initGeTuiPush()");
        com.igexin.sdk.PushManager.getInstance().initialize(application.getApplicationContext(), PushService.class);
        // com.getui.demo.DemoIntentService 为第三⽅方⾃自定义的推送服务事件接收类
        com.igexin.sdk.PushManager.getInstance().registerPushIntentService(application.getApplicationContext(), PushIntentService.class);
    }

    public void setPushRevicer(PushReciver mPushReciver) {
        this.mPushReciver = mPushReciver;
    }

    public static PushReciver getPushReciver() {
        return mPushReciver;
    }

    public static void innotechPushReciverIsNull(Context context) {
        LogUtils.e(context, "InnotechPushReciver is null!");
    }

//    private void huaWeiConnect(final Activity activity) {
//        HMSAgent.connect(activity, new ConnectHandler() {
//            @Override
//            public void onConnect(int rst) {
//                LogUtils.e(activity.getApplicationContext(), LogUtils.TAG_HUAWEI + "HMS connect end:" + rst);
//                getToken();
//            }
//        });
//    }

    /**
     * 获取token
     */
//    public void getToken() {
//        HMSAgent.Push.getToken(new GetTokenHandler() {
//            @Override
//            public void onResult(int rtnCode) {
//                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "get token: end" + rtnCode);
//            }
//        });
//    }

    /**
     * 获得幂等锁
     *
     * @return
     */
    public static Lock getIdempotentLock() {
        if (idempotentLock == null) {
            idempotentLock = new ReentrantLock();
        }
        return idempotentLock;
    }

    /**
     * 是否
     *
     * @param cxt
     * @param pid
     * @return
     */
    public static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            if (procInfo.pid == pid) {
                return procInfo.processName;
            }
        }
        return null;
    }

}
