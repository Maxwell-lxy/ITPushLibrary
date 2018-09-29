package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;

public class BroadcastUtils {

    /**
     * 触发用户上传信息的广播
     *
     * @param context
     */
    public static void sendUpdateUserInfoBroadcast(Context context) {
        InnotechPushMethod.updateUserInfo(context, new RequestCallback() {
            @Override
            public void onSuccess(String msg) {
                Log.i("Innotech_Push", ">>>>>>>>>>>> UserInfo onSuccess msg:" + msg);

            }

            @Override
            public void onFail(String msg) {
                Log.i("Innotech_Push", ">>>>>>>>>>> UserInfo onFail msg:" + msg);
            }
        });
    }

}
