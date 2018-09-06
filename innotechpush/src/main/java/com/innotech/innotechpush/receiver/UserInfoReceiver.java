package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.RequestCallback;

/**
 * app上传用户信息接口调用的接收器
 */

public class UserInfoReceiver extends BroadcastReceiver {

    RequestCallback mCallBack = new RequestCallback() {
        @Override
        public void onSuccess(String msg) {
            Log.i("Innotech_Push", ">>>>>>>>>>>> UserInfo onSuccess msg:" + msg);
            updateUI(msg);

        }

        @Override
        public void onFail(String msg) {
            Log.i("Innotech_Push", ">>>>>>>>>>> UserInfo onFail msg:" + msg);
            updateUI(msg);
        }
    };

    private void updateUI(String guid) {
        Message msg = new Message();
        msg.what = 2;
        Bundle b = new Bundle();// 存放数据
        b.putString("guid", guid);
        msg.setData(b);
        if (InnotechPushMethod.getMyHandler() != null) {
            InnotechPushMethod.getMyHandler().sendMessage(msg);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        InnotechPushMethod.updateUserInfo(context, mCallBack);
    }
}
