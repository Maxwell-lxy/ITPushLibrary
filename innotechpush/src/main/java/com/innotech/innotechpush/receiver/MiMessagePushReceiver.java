package com.innotech.innotechpush.receiver;

import android.content.Context;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.R;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.sdk.MiSDK;
import com.innotech.innotechpush.utils.BroadcastUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient;
import com.xiaomi.mipush.sdk.MiPushCommandMessage;
import com.xiaomi.mipush.sdk.MiPushMessage;
import com.xiaomi.mipush.sdk.PushMessageReceiver;

import java.util.List;

public class MiMessagePushReceiver extends PushMessageReceiver {
    private String mRegId;
    private String mTopic;
    private String mAlias;
    private String mAccount;
    private String mStartTime;
    private String mEndTime;

    @Override
    public void onReceivePassThroughMessage(Context context, MiPushMessage miPushMessage) {
        // super.onReceivePassThroughMessage(context, miPushMessage);
        showMessageInfoforTest(context, "onReceivePassThroughMessage", miPushMessage);
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onReceivePassThroughMessage(context, getCreateMessge(miPushMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationMessageClicked(Context context, MiPushMessage miPushMessage) {
        // super.onNotificationMessageClicked(context, miPushMessage);
        showMessageInfoforTest(context, "onNotificationMessageClicked", miPushMessage);
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageClicked(context, getCreateMessge(miPushMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onNotificationMessageArrived(Context context, MiPushMessage miPushMessage) {
        //  super.onNotificationMessageArrived(context, miPushMessage);
        showMessageInfoforTest(context, "onNotificationMessageArrived", miPushMessage);
        if (InnotechPushManager.getPushReciver() != null) {
            InnotechPushManager.getPushReciver().onNotificationMessageArrived(context, getCreateMessge(miPushMessage));
        } else {
            InnotechPushManager.innotechPushReciverIsNull(context);
        }
    }

    @Override
    public void onReceiveMessage(Context context, MiPushMessage miPushMessage) {
        //  super.onReceiveMessage(context, miPushMessage);
        showMessageInfoforTest(context, "onReceiveMessage", miPushMessage);
    }

    @Override
    public void onReceiveRegisterResult(Context context, MiPushCommandMessage miPushCommandMessage) {
        // super.onReceiveRegisterResult(context, miPushCommandMessage);
        String command = miPushCommandMessage.getCommand();
        List<String> arguments = miPushCommandMessage.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (miPushCommandMessage.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
            } else {
                log = context.getString(R.string.register_fail);
                try {
                    Thread.sleep(5500);
                    new MiSDK(context);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            log = miPushCommandMessage.getReason();
        }

        LogUtils.e(context, LogUtils.TAG_XIAOMI + "metodName:onReceiveRegisterResult" + " log:" + log);
        new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_XIAOMI + "metodName:onReceiveRegisterResult" + " log:" + log).save();
    }

    @Override
    public void onCommandResult(Context context, MiPushCommandMessage message) {
        //  super.onCommandResult(context, miPushCommandMessage);
        String command = message.getCommand();
        List<String> arguments = message.getCommandArguments();
        String cmdArg1 = ((arguments != null && arguments.size() > 0) ? arguments.get(0) : null);
        String cmdArg2 = ((arguments != null && arguments.size() > 1) ? arguments.get(1) : null);
        String log;
        if (MiPushClient.COMMAND_REGISTER.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mRegId = cmdArg1;
                log = context.getString(R.string.register_success);
                UserInfoModel.getInstance().setDevice_token1(mRegId);
                BroadcastUtils.sendUpdateUserInfoBroadcast(context);
            } else {
                log = context.getString(R.string.register_fail);
            }
        } else if (MiPushClient.COMMAND_SET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.set_alias_success, mAlias);
            } else {
                log = context.getString(R.string.set_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ALIAS.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAlias = cmdArg1;
                log = context.getString(R.string.unset_alias_success, mAlias);
            } else {
                log = context.getString(R.string.unset_alias_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.set_account_success, mAccount);
            } else {
                log = context.getString(R.string.set_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSET_ACCOUNT.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mAccount = cmdArg1;
                log = context.getString(R.string.unset_account_success, mAccount);
            } else {
                log = context.getString(R.string.unset_account_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.subscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.subscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_UNSUBSCRIBE_TOPIC.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mTopic = cmdArg1;
                log = context.getString(R.string.unsubscribe_topic_success, mTopic);
            } else {
                log = context.getString(R.string.unsubscribe_topic_fail, message.getReason());
            }
        } else if (MiPushClient.COMMAND_SET_ACCEPT_TIME.equals(command)) {
            if (message.getResultCode() == ErrorCode.SUCCESS) {
                mStartTime = cmdArg1;
                mEndTime = cmdArg2;
                log = context.getString(R.string.set_accept_time_success, mStartTime, mEndTime);
            } else {
                log = context.getString(R.string.set_accept_time_fail, message.getReason());
            }
        } else {
            log = message.getReason();
        }
        LogUtils.e(context, LogUtils.TAG_XIAOMI + "metodName:onCommandResult" + " log:" + log + " mRegId:" + mRegId);
        new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_XIAOMI + "metodName:onCommandResult" + " log:" + log + " mRegId:" + mRegId).save();
    }


    private void showMessageInfoforTest(Context context, String metodName, MiPushMessage miPushMessage) {
        String contentStr = miPushMessage.getContent();
        String titleStr = miPushMessage.getTitle();
        String descriptionStr = miPushMessage.getDescription();
        if (!TextUtils.isEmpty(miPushMessage.getTopic())) {
            mTopic = miPushMessage.getTopic();
        } else if (!TextUtils.isEmpty(miPushMessage.getAlias())) {
            mAlias = miPushMessage.getAlias();
        }
        String dataInfo = "==jar== contentStr:" + contentStr + " titleStr:" + titleStr + " descriptionStr:" + descriptionStr;
        LogUtils.e(context, LogUtils.TAG_XIAOMI + "metodName:" + metodName + " mTopic:" + mTopic + " mAlias:" + mAlias + dataInfo);
        new ClientLog(context, LogCode.LOG_DATA_NOTIFY, LogUtils.TAG_XIAOMI + "metodName:" + metodName + " mTopic:" + mTopic + " mAlias:" + mAlias + dataInfo).save();
    }

    private InnotechMessage getCreateMessge(MiPushMessage miPushMessage) {
        InnotechMessage mPushMessage = new InnotechMessage();
        mPushMessage.setTitle(miPushMessage.getTitle());
        if (!TextUtils.isEmpty(miPushMessage.getDescription())) {
            mPushMessage.setContent(miPushMessage.getDescription());
        }
        if (!TextUtils.isEmpty(miPushMessage.getContent())) {
            mPushMessage.setCustom(miPushMessage.getContent());
        }
        return mPushMessage;
    }
}
