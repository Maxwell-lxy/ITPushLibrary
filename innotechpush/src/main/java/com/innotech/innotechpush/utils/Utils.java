package com.innotech.innotechpush.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.innotech.innotechpush.R;
import com.innotech.innotechpush.receiver.NotificationClickReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * 工具类
 */

public class Utils {

    private static final String TAG = "Innotech Push Utils";
    private static final String XIAOMI = "Xiaomi";
    private static final String MIUI_VERSION_NAME = "ro.miui.ui.version.name";
    private static final String MEIZU = "Meizu";
    private static final String HUAWEI = "Huawei";

    /**
     * 判断是否小米设备
     *
     * @return
     */
    public static boolean isXiaomiDevice() {
        if (XIAOMI.equals(Build.MANUFACTURER)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否MIUI系统
     *
     * @return
     */
    public static boolean isMIUI() {
        String prop = getSystemProperty(MIUI_VERSION_NAME);
        if (prop != null && !"".equals(prop.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否魅族设备
     *
     * @return
     */
    public static boolean isMeizuDevice() {
        if (MEIZU.equals(Build.BRAND)) {
            return true;
        }
        return false;
    }

    /**
     * 判断是否华为设备
     *
     * @return
     */
    public static boolean isHuaweiDevice() {
        if (HUAWEI.equals(Build.BRAND) || HUAWEI.toUpperCase().equals(Build.MANUFACTURER)) {
            return true;
        }
        return false;
    }

    /**
     * 返回系统属性
     *
     * @param propName 要检索的属性
     * @return 属性，如果未找到，则返回NULL
     */
    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            Log.e(TAG, "Unable to read sysprop " + propName, ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    /**
     * 获取meta-data的值（字符串）
     *
     * @param context
     * @param name
     * @return
     */
    public static String getMetaDataString(Context context, String name) {
        String value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getString(name);
        } catch (Exception e) {
            LogUtils.e(context,"Couldn't find config value: " + name);
        }

        return value;
    }

    /**
     * 获取meta-data的值（布尔）
     *
     * @param context
     * @param name
     * @return
     */
    public static Boolean getMetaDataBoolean(Context context, String name) {
        Boolean value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getBoolean(name);
        } catch (Exception e) {
            LogUtils.e(context,"Couldn't find config value: " + name);
        }

        return value;
    }

    /**
     * 获取meta-data的值（长整型）
     *
     * @param context
     * @param name
     * @return
     */
    public static Long getMetaDataLong(Context context, String name) {
        Long value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getLong(name);
        } catch (Exception e) {
            LogUtils.e(context,"Couldn't find config value: " + name);
        }

        return value;
    }

    public static Integer getMetaDataInteger(Context context, String name) {
        Integer value = null;

        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA);
            value = ai.metaData.getInt(name);
        } catch (Exception e) {
            LogUtils.e(context,"Couldn't find config value: " + name);
        }

        return value;
    }

    /**
     * 显示通知
     * @param context
     * @param title
     * @param text
     */
    public static void showNotification(Context context,String title,String text){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setContentTitle(title)//设置通知栏标题
                .setContentText(text)
                .setAutoCancel(true)
                .setTicker("新消息") //通知首次出现在通知栏，带上升动画效果的
                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.push);//设置通知小ICON
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        int id = (int) (System.currentTimeMillis() / 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
        notificationManager.notify(id, mBuilder.build());
    }

}
