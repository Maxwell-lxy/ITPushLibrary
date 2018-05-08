package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.RequestCallback;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;

/**
 * app上传用户信息接口调用的接收器
 */

public class UserInfoReceiver extends BroadcastReceiver {

    public static final String ACTION_UPDATEUSERINFO = "com.inno.push.action.UPDATEUSERINFO";
    RequestCallback mCallBack   =  new RequestCallback() {
        @Override
        public void onSuccess(String msg) {
            Log.i("Innotech_Push",">>>>>>>>>>>> UserInfo onSuccess msg:"+msg);
        }

        @Override
        public void onFail(String msg) {
            Log.i("Innotech_Push",">>>>>>>>>>> UserInfo onFail msg:"+msg);
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e(context, "sdkname:" + InnotechPushManager.pushSDKName + " geTuiIsOk:" + UserInfoUtils.geTuiIsOk+" uMengIsOk:"+UserInfoUtils.uMengIsOk );
        if (InnotechPushManager.pushSDKName != InnotechPushManager.otherSDKName) {
            if(UserInfoUtils.canUupdateUserInfo(context)){
                InnotechPushMethod.updateUserInfo(context,mCallBack);
            }
        } else {
            if(UserInfoUtils.geTuiIsOk&&UserInfoUtils.uMengIsOk){
                if(UserInfoUtils.canUupdateUserInfo(context)) {
                    InnotechPushMethod.updateUserInfo(context, mCallBack);
                    UserInfoUtils.resetGeTuiAndUmeng();
                }
            }
        }
    }
}
