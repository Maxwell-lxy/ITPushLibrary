package com.innotech.innotechpush.sdk;

import android.content.Context;

import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.Utils;
import com.meizu.cloud.pushsdk.PushManager;

public class MeizuSDK {

    public MeizuSDK(Context context) {
        LogUtils.e(context, LogUtils.TAG_MEIZU + "初始化魅族推送");

        String appId = Utils.getMetaDataString(context, "MEIZU_APP_ID").replace("innotech-", "");
        String appKey = Utils.getMetaDataString(context, "MEIZU_APP_KEY");
        PushManager.register(context, appId, appKey);
    }
}
