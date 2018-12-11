package com.innotech.innotechpush;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.config.BroadcastConstant;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.receiver.PushReciver;
import com.innotech.innotechpush.receiver.SocketClientRevicer;
import com.innotech.innotechpush.sdk.HuaweiSDK;
import com.innotech.innotechpush.sdk.MeizuSDK;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.sdk.PushReceiver;
import com.innotech.innotechpush.sdk.SocketClientService;
import com.innotech.innotechpush.service.OppoPushCallback;
import com.innotech.innotechpush.service.PushIntentService;
import com.innotech.innotechpush.service.PushService;
import com.innotech.innotechpush.utils.CommonUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.TokenUtils;
import com.innotech.innotechpush.utils.Utils;
import com.orm.SugarContext;
import com.vivo.push.IPushActionListener;
import com.vivo.push.PushClient;

import org.json.JSONException;

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
    private Context appContext;
    private static PushReciver mPushReciver;
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
     * 初始化推送SDK
     *
     * @param application：业务方的application
     */
    public void initPushSDK(final Application application) {
        this.application = application;
        this.appContext = application.getApplicationContext();
        String processName = getProcessName(application, android.os.Process.myPid());
        LogUtils.e(application, "当前进程名字：" + processName);
        //动态注册广播
        if (CommonUtils.isMainProcess(appContext)) {
            registerMainReceiver(appContext);
        } else if (CommonUtils.isPushProcess(appContext)) {
            registerPushReceiver(appContext);
        }

        SugarContext.init(application);
        UserInfoModel.getInstance().init(appContext);
        LogUtils.e(appContext, "是否支持oppo推送：" + com.coloros.mcssdk.PushManager.isSupportPush(appContext));
        if (CommonUtils.isMainProcess(appContext)) {
            if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
                new MiSDK(appContext);
            } else if (Utils.isMeizuDevice()) {//魅族设备时，开启魅族推送
                new MeizuSDK(appContext);
            } else if (Utils.isHuaweiDevice() && PushConstant.hasHuawei && HuaweiSDK.isUpEMUI41()) {//华为设备时，开启华为推送
                new HuaweiSDK(application);
            } else if (Utils.isOPPO() && PushConstant.hasOppo && com.coloros.mcssdk.PushManager.isSupportPush(appContext)) {//oppo设备时，开启oppo推送
                String appKey = Utils.getMetaDataString(application, "OPPO_APP_KEY");
                String appSecret = Utils.getMetaDataString(application, "OPPO_APP_SECRET");
                com.coloros.mcssdk.PushManager.getInstance().register(appContext, appKey, appSecret, new OppoPushCallback(application));
            } else { //其他设备时，开启个推推送和socket长连接
                initGeTuiPush();
                LogUtils.e(appContext, "是否支持vivo推送：" + PushClient.getInstance(appContext).isSupport());
                if (Utils.isVivo() && PushConstant.hasVivo && PushClient.getInstance(appContext).isSupport()) {
                    PushClient.getInstance(appContext).initialize();
                    PushClient.getInstance(appContext).turnOnPush(new IPushActionListener() {
                        @Override
                        public void onStateChanged(int state) {
                            LogUtils.e(appContext, "vivo state:" + state);
                        }
                    });
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                        String guid = TokenUtils.getGuid(appContext);
                        if (CommonUtils.isCanRunService(appContext, SocketClientService.class.getName()) && !TextUtils.isEmpty(guid)) {
                            appContext.startService(new Intent(appContext, SocketClientService.class));
                        }
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                });
            }
        }
    }

    public void initSocketPush() {
        appContext.startService(new Intent(appContext, SocketClientService.class));
    }

    /**
     * 初始化并开启个推推送
     */
    public void initGeTuiPush() {
        LogUtils.e(appContext, LogUtils.TAG_GETUI + "call initGeTuiPush()");
        com.igexin.sdk.PushManager.getInstance().initialize(appContext, PushService.class);
        // com.getui.demo.DemoIntentService 为第三⽅方⾃自定义的推送服务事件接收类
        com.igexin.sdk.PushManager.getInstance().registerPushIntentService(appContext, PushIntentService.class);
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

    /**
     * 获得幂等锁
     *
     * @return 锁
     */
    public static Lock getIdempotentLock() {
        if (idempotentLock == null) {
            idempotentLock = new ReentrantLock();
        }
        return idempotentLock;
    }

    /**
     * 获取进程名
     */
    private static String getProcessName(Context cxt, int pid) {
        ActivityManager am = (ActivityManager) cxt.getSystemService(Context.ACTIVITY_SERVICE);
        if (am == null) {
            return null;
        }
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

    //
    public void terminate() {
        SugarContext.terminate();
    }

    //动态注册广播
    private void registerPushReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BroadcastConstant.RECEIVE_MESSAGE);
        filter.addAction(BroadcastConstant.MESSAGE_CLICK);
        filter.addAction(BroadcastConstant.ACTION_FRESH_PUSH + context.getPackageName());
        context.registerReceiver(new PushReceiver(), filter);
    }

    //动态注册广播
    private void registerMainReceiver(Context context) {
        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(BroadcastConstant.RECEIVE_MESSAGE);
        filter1.addAction(BroadcastConstant.MESSAGE_CLICK);
        filter1.addAction(BroadcastConstant.ERROR);
        context.registerReceiver(new SocketClientRevicer(), filter1);
    }
}
