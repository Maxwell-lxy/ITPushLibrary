package com.innotech.innotechpush.db;

import android.content.Context;

import com.innotech.innotechpush.utils.LogUtils;

public class DbUtils {

    /**
     * db添加日志记录
     *
     * @param context
     * @param code
     * @param logStr
     */
    public static void addClientLog(Context context, int code, String logStr) {
        try {
            ClientLog log = new ClientLog(context, code, logStr);
            log.save();
        } catch (Exception e) {
            LogUtils.e(context, "db添加日志记录异常");
        }
    }

    /**
     * db添加回执记录
     *
     * @param context
     * @param params
     */
    public static void addClientMsgNotify(Context context, String params) {
        try {
            ClientMsgNotify notify = new ClientMsgNotify(params);
            notify.save();
        } catch (Exception e) {
            LogUtils.e(context, "db添加回执记录异常");
        }
    }
}
