package com.innotech.innotechpush.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.innotech.innotechpush.config.BroadcastConstant;

public class AlarmManagerUtils {
    /**
     * 心跳时间间隔
     * 暂时以固定的值（4分），后期优化为动态计算的
     */
    public static final int HEART_INTERVAL = 40 * 60 * 100;

    public static void setHeartAlarm(Context context) {
        AlarmManager manger = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(BroadcastConstant.ACTION_FRESH_PUSH + context.getPackageName());//广播接收
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);//意图为开启广播
        long currentTime = System.currentTimeMillis() + HEART_INTERVAL;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manger.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, currentTime, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manger.setExact(AlarmManager.RTC_WAKEUP, currentTime, pendingIntent);
        } else {
            manger.set(AlarmManager.RTC_WAKEUP, currentTime, pendingIntent);
        }
    }
}
