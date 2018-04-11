package com.innotech.innotechpush.utils;

import android.content.Context;
import android.util.Log;

/**
 * innotech push需使用该类提供的方法进行日志打印，
 * 方便动态的设置调试模式
 * 方便通过统一的tag进行查询
 */

public class LogUtils {
    public static  String TAG_HUAWEI = "[HuaWeiPush] ";
    public static  String TAG_GETUI = "[GeTuiPush] ";
    public static String TAG_XIAOMI = "[MiPush] ";
    public static String TAG_MEIZU = "[MeiZuPush] ";
    public static String TAG_UMENG = "[UmengPush] ";

    private static final String TAG = "Innotech_Push";

    private static Boolean isDebug(Context context){
        return Utils.getMetaDataBoolean(context,"INNOTECH_PUSH_DEBUG");
    }

    public static void i(Context context,String msg) {
        if (isDebug(context))
            Log.i(TAG, msg);
    }

    public static void d(Context context,String msg) {
        if (isDebug(context))
            Log.d(TAG, msg);
    }

    public static void e(Context context,String msg) {
        if (isDebug(context))
            Log.e(TAG, msg);
    }

    public static void v(Context context,String msg) {
        if (isDebug(context))
            Log.v(TAG, msg);
    }
}
