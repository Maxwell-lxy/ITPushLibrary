package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by admin on 2018/4/19.
 */

public class UserInfoSPUtils {
    public static final String FILE_NAME = "innotech_push_userinfo";
    public static final String KEY_OPEN_NOTICE = "open_notice";
    /**
     * 保存数据的方法
     *
     * @param context
     * @param key
     * @param value
     */
    public static void put(Context context, String key, int value) {
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
    public static int get(Context context, String key, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME,
                Context.MODE_PRIVATE);
        return  sp.getInt(key,defaultValue);
    }
}
