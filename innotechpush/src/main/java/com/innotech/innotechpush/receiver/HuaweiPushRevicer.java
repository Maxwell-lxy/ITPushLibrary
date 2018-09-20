package com.innotech.innotechpush.receiver;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.huawei.hms.support.api.push.PushReceiver;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.BroadcastUtils;
import com.innotech.innotechpush.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * 华为推送的接收器
 */

public class HuaweiPushRevicer extends PushReceiver {
    @Override
    public void onToken(Context context, String token, Bundle extras) {
        LogUtils.e(context, LogUtils.TAG_HUAWEI + "HuaweiPushRevicer onToken: end" + token);
        new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_HUAWEI + "HuaweiPushRevicer onToken: end" + token).save();
        UserInfoModel.getInstance().setDevice_token1(token);
        BroadcastUtils.sendUpdateUserInfoBroadcast(context);
    }

    @Override
    public boolean onPushMsg(Context context, byte[] msg, Bundle bundle) {
        try {
            //CP可以自己解析消息内容，然后做相应的处理
            String content = new String(msg, "UTF-8");
            LogUtils.e(context, LogUtils.TAG_HUAWEI + "收到PUSH透传消息,消息内容为:" + content);
            new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_HUAWEI + "收到PUSH透传消息,消息内容为:" + content).save();
            if (InnotechPushManager.getPushReciver() != null) {
                InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context, getCreateMessge(content));
            } else {
                InnotechPushManager.innotechPushReciverIsNull(context);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void onEvent(Context context, Event event, Bundle extras) {
        LogUtils.e(context, LogUtils.TAG_HUAWEI + "收到通知栏消息点击事件,onEvent");
        if (Event.NOTIFICATION_OPENED.equals(event) || Event.NOTIFICATION_CLICK_BTN.equals(event)) {
            int notifyId = extras.getInt(BOUND_KEY.pushNotifyId, 0);
            LogUtils.e(context, LogUtils.TAG_HUAWEI + "收到通知栏消息点击事件,notifyId:" + notifyId);
            LogUtils.e(context, LogUtils.TAG_HUAWEI + " extras:" + extras);
            new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_HUAWEI + "收到通知栏消息点击事件,notifyId:" + notifyId + " extras:" + extras).save();
            if (0 != notifyId) {
                NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                manager.cancel(notifyId);
            }

        }
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, getCreateMessge(extras));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
        super.onEvent(context, event, extras);
    }

    @Override
    public void onPushState(Context context, boolean pushState) {
        LogUtils.e(context, LogUtils.TAG_HUAWEI + "HuaweiPushRevicer onPushState:" + pushState);
        new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_HUAWEI + "HuaweiPushRevicer onPushState:" + pushState).save();
    }

    private InnotechMessage getCreateMessge(Bundle extras) {
        InnotechMessage mPushMessage = new InnotechMessage();
        String message = extras.getString(BOUND_KEY.pushMsgKey);
//        mPushMessage.setData(message);
        if (!TextUtils.isEmpty(message)) {
            try {
                JSONArray array = new JSONArray(message);
                JSONObject object = new JSONObject();
                for (int i = 0; i < array.length(); i++) {
                    JSONObject o = array.getJSONObject(i);
                    Iterator<String> it = o.keys();
                    while (it.hasNext()) {
                        String key = it.next();
                        object.put(key, o.get(key));
                    }
                }
                mPushMessage.setCustom(object.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mPushMessage;
    }

    private InnotechMessage getCreateMessge(String message) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setData(message);
        return mPushMessage;
    }
}
