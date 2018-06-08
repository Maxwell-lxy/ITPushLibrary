package com.innotech.innotechpush.receiver;

import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;
import com.meizu.cloud.pushsdk.MzPushMessageReceiver;
import com.meizu.cloud.pushsdk.handler.MzPushMessage;
import com.meizu.cloud.pushsdk.notification.PushNotificationBuilder;
import com.meizu.cloud.pushsdk.platform.message.PushSwitchStatus;
import com.meizu.cloud.pushsdk.platform.message.RegisterStatus;
import com.meizu.cloud.pushsdk.platform.message.SubAliasStatus;
import com.meizu.cloud.pushsdk.platform.message.SubTagsStatus;
import com.meizu.cloud.pushsdk.platform.message.UnRegisterStatus;

/**
 * 魅族推送的接收器
 */

public class MeizuPushMsgReceiver extends MzPushMessageReceiver {

    private static final String TAG = "Meizu Push Receiver";

    @Override
    @Deprecated
    public void onRegister(Context context, String pushid) {
        //调用 PushManager.register(context）方法后，会在此回调注册状态
        //应用在接受返回的 pushid
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onRegister pushid:"+pushid);
    }

    /**
     * 收到推送消息的回调,Flyme4.0 以上版本,或者云服务 5.0 以上版本 通过
     * 此方法接收 Push 消息
     *
     * @param context context
     * @param message 收到的推送消息
     */
    @Override
    public void onMessage(Context context, String message) {
        //接收服务器推送的透传消息
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onMessage message:"+message);
        if(InnotechPushManager.getPushReciver()!=null){
            InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context,getCreateMessge(message));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }

    }

    /**
     * 处理 flyme3.0 等以下平台的推送消息
     *
     * @param context
     * @param intent  flyme3.0 平台上默认是将透传的消息 json,按照 key-value 的
     *                组合设置到 intent 中,如果要获取相应的数据,可以调用
     *                intent.getExtra(key)方法获取
     */
    @Override
    public void onMessage(Context context, Intent intent) {
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onMessage intent:"+intent);
    }

    @Override
    @Deprecated
    public void onUnRegister(Context context, boolean b) {
        //调用 PushManager.unRegister(context）方法后，会在此回调反注册状态
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onUnRegister b:"+b);
    }

    //设置通知栏小图标
    @Override
    public void onUpdateNotificationBuilder(PushNotificationBuilder
                                                    pushNotificationBuilder) {
        //重要,详情参考应用小图标自定设置

//        pushNotificationBuilder.setmStatusbarIcon(R.drawable.mz_push_notification_small_icon);
    }

    @Override
    public void onPushStatus(Context context, PushSwitchStatus
            pushSwitchStatus) {
        //检查通知栏和透传消息开关状态回调
    }

    /**
     * 魅族推送注册后进入该回调
     *
     * @param context
     * @param registerStatus
     */
    @Override
    public void onRegisterStatus(Context context, RegisterStatus
            registerStatus) {
        //调用新版订阅 PushManager.register(context,appId,appKey)回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onRegisterStatus registerStatus:"+registerStatus);
        if(registerStatus.getCode().equals(RegisterStatus.SUCCESS_CODE)){
            UserInfoUtils.deviceToken.setDevice_token1(registerStatus.getPushId());
            UserInfoUtils.sendBroadcast(context);
        }
    }

    /**
     * 魅族推送注销后进入该回调
     *
     * @param context
     * @param unRegisterStatus
     */
    @Override
    public void onUnRegisterStatus(Context context, UnRegisterStatus
            unRegisterStatus) {
        //新版反订阅回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onUnRegisterStatus unRegisterStatus:"+unRegisterStatus);
    }

    @Override
    public void onSubTagsStatus(Context context, SubTagsStatus
            subTagsStatus) {
        //标签回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onSubTagsStatus subTagsStatus:"+subTagsStatus);
    }

    @Override
    public void onSubAliasStatus(Context context, SubAliasStatus
            subAliasStatus) {
        //别名回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onSubAliasStatus subAliasStatus:"+subAliasStatus);
    }

    @Override
    public void onNotificationArrived(Context context, MzPushMessage
            mzPushMessage) {
        //通知栏消息到达回调，flyme6 基于 android6.0 以上不再回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onNotificationArrived mzPushMessage:"+mzPushMessage);
        if(InnotechPushManager.getPushReciver()!=null){
            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context,getCreateMessge(mzPushMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationClicked(Context context, MzPushMessage
            mzPushMessage) {
        //通知栏消息点击回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onNotificationClicked mzPushMessage:"+mzPushMessage);
        if(InnotechPushManager.getPushReciver()!=null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, getCreateMessge(mzPushMessage));
        }else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationDeleted(Context context, MzPushMessage
            mzPushMessage) {
        //通知栏消息删除回调；flyme6 基于 android6.0 以上不再回调
        LogUtils.e(context, LogUtils.TAG_MEIZU+"MeizuPushMsgReceiver onNotificationDeleted mzPushMessage:"+mzPushMessage);
    }

    private InnotechMessage getCreateMessge(MzPushMessage mzPushMessage){
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(mzPushMessage.getTitle());
        mPushMessage.setData( mzPushMessage.getContent());
        return mPushMessage;
    }

    private InnotechMessage getCreateMessge(String message){
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setData(message);
        return mPushMessage;
    }

}
