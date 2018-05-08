package com.innotech.innotechpush;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.huawei.android.hms.agent.HMSAgent;
import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.EnableReceiveNormalMsgHandler;
import com.huawei.android.hms.agent.push.handler.EnableReceiveNotifyMsgHandler;
import com.huawei.android.hms.agent.push.handler.GetPushStateHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.huawei.android.hms.agent.push.handler.QueryAgreementHandler;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.receiver.PushReciver;
import com.innotech.innotechpush.receiver.UMengReceiver;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.service.GTPushIntentService;
import com.innotech.innotechpush.service.GeTuiPushService;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.Utils;
import com.meizu.cloud.pushsdk.PushManager;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.PushAgent;

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
    public static String huaweiSDKName = "huawei";
    public static String meizuSDKName = "meizu";
    public static String otherSDKName = "union ";

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
            huaWeiConnect(activity);
        }
    }

    /**
     * 初始化推送SDK
     *
     * @param application
     */
    public void initPushSDK(Application application) {
        this.application = application;
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
        else if (Utils.isHuaweiDevice()) {
            pushSDKName = huaweiSDKName;
            LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + " HMSAgent.init");
            HMSAgent.init(application);
        }
        //其他设备时，开启个推推送和友盟推送
        else {
            pushSDKName = otherSDKName;
            initGeTuiPush();
            initUMengPush();
        }
    }


    /**
     * 初始化并开启个推推送
     */
    private void initGeTuiPush() {
        LogUtils.e(application.getApplicationContext(), LogUtils.TAG_GETUI + "call initGeTuiPush()");
        com.igexin.sdk.PushManager.getInstance().initialize(application.getApplicationContext(), GeTuiPushService.class);
        // com.getui.demo.DemoIntentService 为第三⽅方⾃自定义的推送服务事件接收类
        com.igexin.sdk.PushManager.getInstance().registerPushIntentService(application.getApplicationContext(), GTPushIntentService.class);
    }

    /**
     * 初始化并开启友盟推送
     */
    private void initUMengPush() {
        LogUtils.e(application.getApplicationContext(), LogUtils.TAG_UMENG + " call initUMengPush");
        String umAppKey = Utils.getMetaDataString(application, PushConstant.UMENG_APP_KEY);
        String umMsgSec = Utils.getMetaDataString(application, PushConstant.UMENG_MESSAGE_SECRET);
        LogUtils.e(application.getApplicationContext(), "友盟key：" + umAppKey + "，友盟消息密钥：" + umMsgSec);
        UMConfigure.init(application, UMConfigure.DEVICE_TYPE_PHONE, umMsgSec);
        PushAgent mPushAgent = PushAgent.getInstance(application);
        UMengReceiver uMengReceiver = new UMengReceiver(application);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(uMengReceiver);
        mPushAgent.setMessageHandler(uMengReceiver);
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

    private void huaWeiConnect(final Activity activity) {
        HMSAgent.connect(activity, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                LogUtils.e(activity.getApplicationContext(), LogUtils.TAG_HUAWEI + "HMS connect end:" + rst);
                getToken();
                getPushStatus();
                setReceiveNormalMsg(true);
                setReceiveNotifyMsg(true);
            }
        });
        //  getToken();
    }

    /**
     * 获取token
     */
    public void getToken() {
        HMSAgent.Push.getToken(new GetTokenHandler() {
            @Override
            public void onResult(int rtnCode) {
                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "get token: end" + rtnCode);
            }
        });
    }

    /**
     * 获取push状态 | Get Push State
     */
    public void getPushStatus() {
        HMSAgent.Push.getPushState(new GetPushStateHandler() {
            @Override
            public void onResult(int rst) {
                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "onResult:end code=" + rst);
            }
        });
    }

    /**
     * 设置是否接收普通透传消息 | Set whether to receive normal pass messages
     *
     * @param enable 是否开启 | enabled or not
     */
    public void setReceiveNormalMsg(boolean enable) {
        HMSAgent.Push.enableReceiveNormalMsg(enable, new EnableReceiveNormalMsgHandler() {
            @Override
            public void onResult(int rst) {
                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "enableReceiveNormalMsg:end code=" + rst);
            }
        });
    }

    /**
     * 设置接收通知消息 | Set up receive notification messages
     *
     * @param enable 是否开启 | enabled or not
     */
    public void setReceiveNotifyMsg(boolean enable) {
        HMSAgent.Push.enableReceiveNotifyMsg(enable, new EnableReceiveNotifyMsgHandler() {
            @Override
            public void onResult(int rst) {

                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "enableReceiveNotifyMsg:end code=" + rst);
            }
        });
    }

    /**
     * 显示push协议 | Show Push protocol
     */
    public void showAgreement() {
        HMSAgent.Push.queryAgreement(new QueryAgreementHandler() {
            @Override
            public void onResult(int rst) {
                LogUtils.e(application.getApplicationContext(), LogUtils.TAG_HUAWEI + "queryAgreement:end code=" + rst);
            }
        });
    }

}
