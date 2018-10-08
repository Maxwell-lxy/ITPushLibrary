package com.innotech.innotechpush.sdk;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.utils.CommonUtils;
import com.innotech.innotechpush.utils.NotificationUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/5/17.
 */

public class PushMessageManager {
    private Context mContext;
    private static PushMessage newMessage;
    private static PushMessageManager mPushMessageManager;

    private PushMessageManager(Context context) {
        this.mContext = context;
    }

    public static PushMessageManager getInstance(Context context) {
        if (mPushMessageManager == null) {
            mPushMessageManager = new PushMessageManager(context);
        }
        return mPushMessageManager;
    }

    public PushMessage getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(PushMessage message) {
        newMessage = message;
        if (message.getPass_through() != 1) {
            try {
                NotificationUtils.sendNotificationByStyle(mContext, createMessageByJson(message));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent();
        intent.setAction("com.innotech.push.RECEIVE_MESSAGE");
        Integer appId = CommonUtils.getMetaDataInteger(mContext, PushConstant.INNOTECH_APP_ID);
        message.setAppId(appId);
        intent.putExtra("PushMessage", message);
        mContext.sendBroadcast(intent);
    }

    private InnotechMessage createMessageByJson(PushMessage pushMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(pushMessage.getTitle());
        mPushMessage.setContent(pushMessage.getContent());
        if(!TextUtils.isEmpty(pushMessage.getTransmission())){
            try {
                JSONObject tranObj = new JSONObject(pushMessage.getTransmission());
                String extra = tranObj.getString("extra");
                mPushMessage.setCustom(extra);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        mPushMessage.setMessageId(pushMessage.getMsg_id());
        mPushMessage.setStyle(pushMessage.getStyle());
        mPushMessage.setUnfold(pushMessage.getUnfold());
        return mPushMessage;
    }
}
