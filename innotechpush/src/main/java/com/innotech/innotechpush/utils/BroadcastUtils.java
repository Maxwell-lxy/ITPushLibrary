package com.innotech.innotechpush.utils;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.sdk.SocketClientJobService;

public class BroadcastUtils {

    /**
     * 触发用户上传信息的广播
     *
     * @param context
     */
    public static void sendUpdateUserInfoBroadcast(final Context context) {
        InnotechPushMethod.updateUserInfo(context, new RequestCallback() {
            @Override
            public void onSuccess(String msg) {
                LogUtils.e(context, ">>>>>>>>>>>> UserInfo onSuccess msg:" + msg);

            }

            @Override
            public void onFail(String msg) {
                LogUtils.e(context, ">>>>>>>>>>> UserInfo onFail msg:" + msg);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context, SocketClientJobService.class));
                    builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
                    JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                    if (tm != null) {
                        tm.schedule(builder.build());
                    }
                }
            }
        });
    }

}
