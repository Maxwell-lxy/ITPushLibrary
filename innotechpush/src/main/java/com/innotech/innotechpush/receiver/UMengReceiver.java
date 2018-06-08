package com.innotech.innotechpush.receiver;

import android.content.Context;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.SPUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;
import com.innotech.innotechpush.utils.Utils;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.UmengMessageHandler;
import com.umeng.message.entity.UMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 友盟推送的接收器
 */

public class UMengReceiver extends UmengMessageHandler implements IUmengRegisterCallback {
    private Context context;

    public UMengReceiver(Context context) {
        this.context = context;
    }

    @Override
    public void onSuccess(String deviceToken) {
        //注册成功会返回device token
        LogUtils.e(context, LogUtils.TAG_UMENG + " register Success deviceToken:" + deviceToken);
        UserInfoUtils.uMengIsOk = true;
        UserInfoUtils.deviceToken.setDevice_token2(deviceToken);
        UserInfoUtils.sendBroadcast(context);

    }

    @Override
    public void onFailure(String s, String s1) {
        LogUtils.e(context, LogUtils.TAG_UMENG + " register Failure s:" + s + " s1:" + s1);
        UserInfoUtils.uMengIsOk = true;
        UserInfoUtils.sendBroadcast(context);
    }

    /**
     * 通知的回调方法（通知送达时会回调）
     */
    @Override
    public void dealWithNotificationMessage(Context context, UMessage msg) {
        //调用super，会展示通知，不调用super，则不展示通知。
        super.dealWithNotificationMessage(context, msg);
        String text = msg.text;
        String custom = msg.custom;
        String title = msg.title;
        LogUtils.e(context, LogUtils.TAG_UMENG + "dealWithNotificationMessage: title" + title + " custom:" + custom + " text:" + text);
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context, getCreateMessge(msg));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    /**
     * 自定义消息的回调方法
     */
    @Override
    public void dealWithCustomMessage(final Context context, final UMessage msg) {
        try {
            LogUtils.i(context,"uMeng dealWithCustomMessage() msg.custom:"+msg.custom);
            JSONObject object = new JSONObject(msg.custom);
            String idempotent = object.getString("idempotent");
            if (!TextUtils.isEmpty(idempotent)) {
                //消息池去重验证
                if (SPUtils.isPass(context, idempotent)) {
                    //展示通知
                    Utils.showNotification(context, createMessageByJson(msg));
                    //消息存入消息池中
                    SPUtils.put(context, idempotent, System.currentTimeMillis());
                } else {
                    LogUtils.e(context, LogUtils.TAG_UMENG + " 该消息为重复消息，过滤掉，不做处理" + msg.custom);
                    //触发一次消息池的清理
                    SPUtils.clearPoor(context);
                }
            } else {
                LogUtils.e(context, LogUtils.TAG_UMENG + " 该消息中没有包含idempotent字段，不做处理" + msg.custom);
            }
        } catch (JSONException e) {
            LogUtils.e(context, LogUtils.TAG_UMENG + " dealWithCustomMessage方法中json转换失败");
        }
        LogUtils.e(context, LogUtils.TAG_UMENG + "dealWithCustomMessage:  msg" + msg.toString());
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context, createMessageByJson(msg));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    /**
     * 自定义通知栏样式的回调方法
     */
//    @Override
//    public Notification getNotification(Context context, UMessage msg) {
//        switch (msg.builder_id) {
//            case 1:
//                Notification.Builder builder = new Notification.Builder(context);
//                RemoteViews myNotificationView = new RemoteViews(context.getPackageName(),
//                        R.layout.notification_view);
//                myNotificationView.setTextViewText(R.id.notification_title, msg.title);
//                myNotificationView.setTextViewText(R.id.notification_text, msg.text);
//                myNotificationView.setImageViewBitmap(R.id.notification_large_icon, getLargeIcon(context, msg));
//                myNotificationView.setImageViewResource(R.id.notification_small_icon,
//                        getSmallIconId(context, msg));
//                builder.setContent(myNotificationView)
//                        .setSmallIcon(getSmallIconId(context, msg))
//                        .setTicker(msg.ticker)
//                        .setAutoCancel(true);
//
//                return builder.getNotification();
//            default:
//                //默认为0，若填写的builder_id并不存在，也使用默认。
//                return super.getNotification(context, msg);
//        }
//    }
    /**
     * {
     "action_content": "{"url":"http://www.baidu.com"}",  //action_type为2时读取url
     "action_type": 1,            // 1打开应用 2打开链接
     "content": "App全推测试内容3",  //通知内容
     "extra": "", //用户自定义json数据
     "idempotent": "XVlBzg1527500648", //唯一标识
     "unfold":"app全推展开内容", //通知展开显示文本
     "title": "App全推测试标题3" //通知标题
     }
     */
    private  InnotechMessage createMessageByJson(UMessage msg){
        InnotechMessage mPushMessage = new InnotechMessage();
        JSONObject object = null;
        try {
            object = new JSONObject(msg.custom);
            String title = object.getString("title");
            String content = object.getString("content");
            String extra = object.getString("extra");
            String unfold = object.getString("unfold");
            int action_type = object.getInt("action_type");
            if(action_type==2){
                String action_content = object.getString("action_content");
                JSONObject  con_object = new JSONObject(action_content);
                mPushMessage.setActionContent(con_object.getString("url"));
                mPushMessage.setActionContent(action_content);
            }
            mPushMessage.setTitle(title);
            mPushMessage.setContent(content);
            mPushMessage.setCustom(extra);
            mPushMessage.setNotiBigText(unfold);
            mPushMessage.setMessageId(msg.message_id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return mPushMessage;
    }
    private InnotechMessage getCreateMessge(UMessage uMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(uMessage.title);
        mPushMessage.setData(uMessage.custom);
        return mPushMessage;
    }
}
