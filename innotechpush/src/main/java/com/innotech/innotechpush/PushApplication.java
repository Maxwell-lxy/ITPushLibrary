package com.innotech.innotechpush;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;

import com.huawei.android.hms.agent.common.handler.ConnectHandler;
import com.huawei.android.hms.agent.push.handler.EnableReceiveNormalMsgHandler;
import com.huawei.android.hms.agent.push.handler.EnableReceiveNotifyMsgHandler;
import com.huawei.android.hms.agent.push.handler.GetPushStateHandler;
import com.huawei.android.hms.agent.push.handler.GetTokenHandler;
import com.huawei.android.hms.agent.push.handler.QueryAgreementHandler;
import com.huawei.hms.support.api.push.TokenResult;
import com.innotech.innotechpush.config.PushConfig;
import com.innotech.innotechpush.receiver.PushReciver;
import com.innotech.innotechpush.receiver.UMengReceiver;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.service.GTPushIntentService;
import com.innotech.innotechpush.service.GeTuiPushService;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.Utils;
import com.meizu.cloud.pushsdk.PushManager;
import com.huawei.android.hms.agent.HMSAgent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;

/**
 * 集成推送的app需要继承该application类
 */

public class PushApplication extends Application {

    public static PushReciver mPushReciver = null;

    @Override
    public void onCreate() {
        super.onCreate();
        //小米设备或者MIUI设备时，开启小米推送
        if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
            MiSDK miSDK = new MiSDK(this);
        }
        //魅族设备时，开启魅族推送
        else if (Utils.isMeizuDevice()) {
            String appId = Utils.getMetaDataString(this, "MEIZU_APP_ID").replace("innotech-", "");
            String appKey = Utils.getMetaDataString(this, "MEIZU_APP_KEY");
            LogUtils.e(getApplicationContext(), LogUtils.TAG_MEIZU+"Meizu  PushManager.register");
            PushManager.register(this, appId, appKey);
        }
        //华为设备时，开启华为推送
        else if (Utils.isHuaweiDevice()) {
            LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+" HMSAgent.init");
            HMSAgent.init(this);
        }
        //其他设备时，开启个推推送和友盟推送
        else {
            initGeTuiPush();
            initUMengPush();
        }

    }

    /**
     * 初始化并开启个推推送
     */
    public void initGeTuiPush() {
        LogUtils.e(getApplicationContext(), LogUtils.TAG_GETUI+"call initGeTuiPush()");
        com.igexin.sdk.PushManager.getInstance().initialize(getApplicationContext(), GeTuiPushService.class);
        // com.getui.demo.DemoIntentService 为第三⽅方⾃自定义的推送服务事件接收类
        com.igexin.sdk.PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), GTPushIntentService.class);
    }

    /**
     * 初始化并开启友盟推送
     */
    public void initUMengPush() {
        LogUtils.e(getApplicationContext(),LogUtils.TAG_UMENG+" call initUMengPush");
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, Utils.getMetaDataString(this, "UMENG_MESSAGE_SECRET"));
        PushAgent mPushAgent = PushAgent.getInstance(this);
        UMengReceiver uMengReceiver = new UMengReceiver(this);
        //注册推送服务，每次调用register方法都会回调该接口
        mPushAgent.register(uMengReceiver);
        mPushAgent.setMessageHandler(uMengReceiver);
    }

    public void setPushRevicer(PushReciver mPushReciver) {
        this.mPushReciver = mPushReciver;
    }

    public void huaWeiConnect(final Activity activity) {
        HMSAgent.connect(activity, new ConnectHandler() {
            @Override
            public void onConnect(int rst) {
                LogUtils.e(activity.getApplicationContext(), LogUtils.TAG_HUAWEI+"HMS connect end:" + rst);
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
                LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+"get token: end" + rtnCode);
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
                LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+"onResult:end code=" + rst);
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
                LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+"enableReceiveNormalMsg:end code=" + rst);
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

                LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+ "enableReceiveNotifyMsg:end code=" + rst);
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
                LogUtils.e(getApplicationContext(), LogUtils.TAG_HUAWEI+ "queryAgreement:end code=" + rst);
            }
        });
    }

}
