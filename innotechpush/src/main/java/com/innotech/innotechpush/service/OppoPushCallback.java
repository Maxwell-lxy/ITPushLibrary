package com.innotech.innotechpush.service;

import android.content.Context;
import android.util.Log;

import com.coloros.mcssdk.callback.PushAdapter;
import com.coloros.mcssdk.mode.SubscribeResult;
import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.utils.BroadcastUtils;

import java.util.List;

public class OppoPushCallback extends PushAdapter {
    private Context context;

    public OppoPushCallback(Context context) {
        this.context = context;
    }

    @Override
    public void onRegister(int code, String s) {
        if (code == 0) {
            Log.e("oppo", "注册成功，registerId:" + s);
            UserInfoModel.getInstance().setDevice_token1(s);
            BroadcastUtils.sendUpdateUserInfoBroadcast(context);
        } else {
            Log.e("oppo", "注册失败，code=" + code + ",msg=" + s);
        }
    }

    @Override
    public void onUnRegister(int code) {
        if (code == 0) {
//            LogUtils.e(application.getApplicationContext(), "注销成功，code=" + code);
        } else {
//            LogUtils.e(application.getApplicationContext(), "注销失败，code=" + code);
        }
    }

    @Override
    public void onGetAliases(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("获取别名成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("获取别名失败", "code=" + code);
        }
    }

    @Override
    public void onSetAliases(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("设置别名成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("设置别名失败", "code=" + code);
        }
    }

    @Override
    public void onUnsetAliases(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("取消别名成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("取消别名失败", "code=" + code);
        }
    }

    @Override
    public void onSetTags(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("设置标签成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("设置标签失败", "code=" + code);
        }
    }

    @Override
    public void onUnsetTags(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("取消标签成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("取消标签失败", "code=" + code);
        }
    }

    @Override
    public void onGetTags(int code, List<SubscribeResult> list) {
        if (code == 0) {
//                showResult("获取标签成功", "code=" + code + ",msg=" + Arrays.toString(list.toArray()));
        } else {
//                showResult("获取标签失败", "code=" + code);
        }
    }


    @Override
    public void onGetPushStatus(final int code, int status) {
        if (code == 0 && status == 0) {
//                showResult("Push状态正常", "code=" + code + ",status=" + status);
        } else {
//                showResult("Push状态错误", "code=" + code + ",status=" + status);
        }
    }

    @Override
    public void onGetNotificationStatus(final int code, final int status) {
        if (code == 0 && status == 0) {
//                showResult("通知状态正常", "code=" + code + ",status=" + status);
        } else {
//                showResult("通知状态错误", "code=" + code + ",status=" + status);
        }
    }

    @Override
    public void onSetPushTime(final int code, final String s) {
//            showResult("SetPushTime", "code=" + code + ",result:" + s);
    }
}
