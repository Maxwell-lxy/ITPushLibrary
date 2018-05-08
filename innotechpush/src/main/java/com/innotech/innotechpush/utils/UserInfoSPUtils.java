package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by admin on 2018/4/19.
 */

public class UserInfoSPUtils {
    public static final String FILE_NAME = "innotech_push_userinfo";
    public static final String KEY_OPEN_NOTICE = "open_notice";
    public static final String KEY_GUID = "guid";
    public static final String KEY_ACCESSID = "accessid";
    public static final String KEY_TOKEN1= "device_token1";
    public static final String KEY_TOKEN2 = "device_token2";

    /**
     * 保存数据的方法
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putInt(Context context, String key, int value) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    /**
     * 得到保存数据的方法
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return  sp.getInt(key,defaultValue);
    }


    /**
     * 保存数据的方法
     *
     * @param context
     * @param key
     * @param value
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,value);
        editor.commit();
    }

    /**
     * 得到保存数据的方法
     *
     * @param context
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return  sp.getString(key,defaultValue);
    }
}
