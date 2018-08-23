package com.innotech.innotechpush.utils;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.R;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.receiver.NotificationClickReceiver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;


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
        if (MEIZU.toLowerCase().equals(Build.BRAND.toLowerCase())) {
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
            LogUtils.e(context, "Couldn't find config value: " + name);
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
            LogUtils.e(context, "Couldn't find config value: " + name);
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
            LogUtils.e(context, "Couldn't find config value: " + name);
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
            LogUtils.e(context, "Couldn't find config value: " + name);
        }

        return value;
    }

    /**
     * 显示通知
     *
     * @param context
     * @param msg
     */
    public static void showNotification(Context context, InnotechMessage msg) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if (msg.getNotiBigText() != null && msg.getNotiBigText().length() > 0) {
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker("新消息") //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle(msg.getTitle())
                            .bigText(msg.getNotiBigText()));
        } else {
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker("新消息") //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon);
        }

        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
        notificationManager.notify(id, mBuilder.build());
    }

    /**
     * 获取手机IMEI号
     * <p>
     * 需要动态权限: android.permission.READ_PHONE_STATE
     */
    public static String getIMEI(Context context) {
        String imei = null;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        try {
            imei = telephonyManager.getDeviceId();
        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
        return imei;
    }

    /**
     * 获取Android ID
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    /**
     * 获取 SerialNumber
     *
     * @return
     */
    public static String getSerialNumber() {
        String serialNumber = android.os.Build.SERIAL;
        return serialNumber;
    }

    /**
     * 要注意的是，areNotificationsEnabled方法的有效性官方只最低支持到API 19，低于19的仍可调用此方法不过只会返回true，即默认为用户已经开启了通知。
     * 查了各种资料，目前暂时没有办法获取19以下的系统是否开启了某个App的通知显示权限。
     *
     * @param context
     * @return
     */
    public static boolean isNotificationEnabled(Context context) {
        boolean result = true;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            result = manager.areNotificationsEnabled();
        } else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            result = isNotificationEnable(context);
        }
        return result;
    }

    /*
     * 判断通知权限是否打开
     */
    private static boolean isNotificationEnable(Context context) {
        AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        ApplicationInfo appInfo = context.getApplicationInfo();

        String pkg = context.getApplicationContext().getPackageName();
        int uid = appInfo.uid;

        Class appOpsClass = null; /* Context.APP_OPS_MANAGER */

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                appOpsClass = Class.forName(AppOpsManager.class.getName());
            }
            Method checkOpNoThrowMethod = appOpsClass.getMethod("checkOpNoThrow", Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField("OP_POST_NOTIFICATION");
            int value = (int) opPostNotificationValue.get(Integer.class);
            return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String getOS() {
        return Build.BRAND;
    }

    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    /**
     * 判断用户是否修改过通知权限
     *
     * @param context
     * @return
     */
    public static boolean getChange(Context context) {
        boolean result = false;
        int openNopticeLastValue = UserInfoSPUtils.getInt(context, UserInfoSPUtils.KEY_OPEN_NOTICE, -1);
        int openNopticeCurValue = isNotificationEnabled(context) ? 0 : 1;
        if (openNopticeLastValue == -1) {
            result = true;
        } else {
            result = openNopticeLastValue != openNopticeCurValue ? true : false;
        }
        UserInfoSPUtils.putInt(context, UserInfoSPUtils.KEY_OPEN_NOTICE, openNopticeCurValue);
        LogUtils.e(context, "openNopticeLastValue=" + openNopticeLastValue + " openNopticeCurValue=" + openNopticeCurValue + " result:" + result);
        return result;
    }

}
