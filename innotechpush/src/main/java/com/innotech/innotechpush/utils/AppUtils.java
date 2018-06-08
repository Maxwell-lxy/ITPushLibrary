package com.innotech.innotechpush.utils;

import android.app.ActivityManager;
import android.app.KeyguardManager;
import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import static android.content.Context.KEYGUARD_SERVICE;

/**
 * Created by admin on 2018/5/29.
 */

public class AppUtils {
    /**
     * 判断应用是否是在后台
     */
    public static boolean appIsBackground(Context context) {
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(KEYGUARD_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            LogUtils.i(context,"appProcess.processName:"+appProcess.processName+" context.getPackageName():"+context.getPackageName()+" importance:"+appProcess.importance);
            if (TextUtils.equals(appProcess.processName, context.getPackageName())) {
                return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
            }
        }
        return false;
    }
}
