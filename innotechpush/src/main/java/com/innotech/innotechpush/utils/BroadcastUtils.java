package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.Intent;

public class BroadcastUtils {
    public static final String ACTION_UPDATEUSERINFO = "com.inno.push.action.UPDATEUSERINFO";

    /**
     * 触发用户上传信息的广播
     *
     * @param context
     */
    public static void sendUpdateUserInfoBroadcast(Context context) {
        Intent sendBIntent = new Intent(BroadcastUtils.ACTION_UPDATEUSERINFO);
        context.sendBroadcast(sendBIntent);
    }
}
