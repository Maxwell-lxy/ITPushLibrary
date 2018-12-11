package com.innotech.innotechpush.utils;

import android.content.Context;
import android.text.TextUtils;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.db.DbUtils;

public class BroadcastUtils {

    /**
     * 触发用户上传信息的广播
     *
     * @param context：上下文
     */
    public static void sendUpdateUserInfoBroadcast(final Context context) {
        //本地能获取到guid值，且APPID、device_token、channel、open_notice、version，无变化时，且在一天之内，不调用接口
        String guid = TokenUtils.getGuid(context);
        int app_id_old = UpdateUserInfoSP.getInt(context, UpdateUserInfoSP.KEY_APPID, 0);
        int app_id_new = UserInfoModel.getInstance().getApp_id();
        String device_token_old = UpdateUserInfoSP.getString(context, UpdateUserInfoSP.KEY_DEVICE_TOKEN, "");
        String device_token_new = UserInfoModel.getInstance().getDevice_token1() == null ? "" : UserInfoModel.getInstance().getDevice_token1();
        String channel_old = UpdateUserInfoSP.getString(context, UpdateUserInfoSP.KEY_CHANNEL, "");
        String channel_new = UserInfoModel.getInstance().getChannel();
        String open_notice_old = UpdateUserInfoSP.getString(context, UpdateUserInfoSP.KEY_OPEN_NOTICE, "");
        String open_notice_new = UserInfoModel.getInstance().isOpen_notice() + "";
        String version_old = UpdateUserInfoSP.getString(context, UpdateUserInfoSP.KEY_VERSION, "");
        String version_new = UserInfoModel.getInstance().getVersion();
        long currentTime = System.currentTimeMillis();
        String time_old = DateUtils.getDateToString(UpdateUserInfoSP.getLong(context, UpdateUserInfoSP.KEY_TIME, currentTime), "yyyy-MM-dd");
        String time_new = DateUtils.getDateToString(currentTime, "yyyy-MM-dd");

        if (!TextUtils.isEmpty(guid)
                && app_id_old == app_id_new
                && device_token_old.equals(device_token_new)
                && channel_old.equals(channel_new)
                && open_notice_old.equals(open_notice_new)
                && version_old.equals(version_new)
                && time_old.equals(time_new)) {
            if (InnotechPushManager.getPushReciver() != null) {
                InnotechPushManager.getPushReciver().onReceiveGuid(context, guid);
            } else {
                InnotechPushManager.innotechPushReciverIsNull(context);
            }
            InnotechPushManager.getInstance().initSocketPush();
            LogUtils.e(context, "use the local guid, not call updateUserInfo method");
            DbUtils.addClientLog(context, LogCode.LOG_DATA_API, "use the local guid, not call updateUserInfo method:"
                    + "app_id_old:" + app_id_old + "app_id_new:" + app_id_new
                    + "device_token_old:" + device_token_old + "device_token_new:" + device_token_new
                    + "channel_old:" + channel_old + "channel_new:" + channel_new
                    + "open_notice_old:" + open_notice_old + "open_notice_new:" + open_notice_new
                    + "version_old:" + version_old + "version_new:" + version_new
                    + "time_old:" + time_old + "time_new:" + time_new);
            return;
        }

        InnotechPushMethod.updateUserInfo(context, new RequestCallback() {
            @Override
            public void onSuccess(String msg) {
                LogUtils.e(context, "updateUserInfo onSuccess msg B:" + msg);
                DbUtils.addClientLog(context, LogCode.LOG_DATA_API, "UserInfo onSuccess msg:" + msg);
                //请求成功，将请求的APPID、device_token、channel、open_notice、version、日期存到本地。
                UpdateUserInfoSP.putInt(context, UpdateUserInfoSP.KEY_APPID, UserInfoModel.getInstance().getApp_id());
                UpdateUserInfoSP.putString(context, UpdateUserInfoSP.KEY_DEVICE_TOKEN, UserInfoModel.getInstance().getDevice_token1());
                UpdateUserInfoSP.putString(context, UpdateUserInfoSP.KEY_CHANNEL, UserInfoModel.getInstance().getChannel());
                UpdateUserInfoSP.putString(context, UpdateUserInfoSP.KEY_OPEN_NOTICE, UserInfoModel.getInstance().isOpen_notice() + "");
                UpdateUserInfoSP.putString(context, UpdateUserInfoSP.KEY_VERSION, UserInfoModel.getInstance().getVersion());
                UpdateUserInfoSP.putLong(context, UpdateUserInfoSP.KEY_TIME, System.currentTimeMillis());
                LogUtils.e(context, "updateUserInfo onSuccess msg E:" + msg);
            }

            @Override
            public void onFail(String msg) {
                LogUtils.e(context, "updateUserInfo onFail msg B:" + msg);
                DbUtils.addClientLog(context, LogCode.LOG_DATA_API, "UserInfo onFail msg:" + msg);
                LogUtils.e(context, "updateUserInfo onFail msg E:" + msg);
//                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
//                    JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, SocketClientJobService.class));
//                    builder.setMinimumLatency(5000);
//                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
//                    JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
//                    if (tm != null) {
//                        tm.schedule(builder.build());
//                    }
//                }
            }
        });
    }

}
