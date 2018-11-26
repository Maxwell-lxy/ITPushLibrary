package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenSP {
    private static final String FILE_NAME = "it_push_token";
    public static final String KEY_MI_REGID = "mi_regid";

    /**
     * 保存数据的方法
     *
     * @param context：上下文
     * @param key：键
     * @param value：值
     */
    public static void putString(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.commit();
    }

    /**
     * 得到保存数据的方法
     *
     * @param context：上下文
     * @param key：键
     * @param defaultValue：默认值
     * @return 值
     */
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }
}
