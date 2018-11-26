package com.innotech.innotechpush.db;

import android.content.Context;
import android.util.Log;

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

    /**
     * db添加回执记录
     *
     * @param context
     * @param params
     */
    public static void addClientMsgNotifyHW(Context context, String params) {
        try {
            ClientMsgNotifyHW notify = new ClientMsgNotifyHW(params);
            notify.save();
        } catch (Exception e) {
            LogUtils.e(context, "db添加回执记录（华为点击）异常");
        }
    }

    /**
     * db添加长连接回执
     *
     * @param context
     * @param json
     * @param cmd
     */
    public static void addSocketAck(Context context, String json, int cmd) {
        try {
            SocketAck socketAck = new SocketAck(json, cmd);
            socketAck.save();
        } catch (Exception e) {
            LogUtils.e(context, "db添加长连接回执异常");
        }
    }
}
