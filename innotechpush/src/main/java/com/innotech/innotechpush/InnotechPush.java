package com.innotech.innotechpush;

import android.content.Context;

import com.huawei.android.hms.agent.HMSAgent;
import com.innotech.innotechpush.config.PushConfig;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.Utils;
import com.meizu.cloud.pushsdk.PushManager;
import com.umeng.message.PushAgent;
import com.xiaomi.mipush.sdk.MiPushClient;

/**
 * 该类将在启动推送后提供一些功能操作
 */

public class InnotechPush {


    // TODO: 2018/4/8 设置userAccount

    // TODO: 2018/4/8

    /**
     * 设置别名
     *
     * @param context：Android平台上app的上下文，建议传入当前app的application context
     * @param alias：为指定用户设置别名
     */
    public static void setAlias(Context context, String alias) {
        //小米设备或者MIUI设备时
        if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
            MiPushClient.setAlias(context,alias,null);
        }
        //魅族设备时
        else if (Utils.isMeizuDevice()) {

        }
        //华为设备时
        else if (Utils.isHuaweiDevice()) {

        }
        //其他设备时
        else {


        }
    }

    // TODO: 2018/4/8 订阅topic

}
