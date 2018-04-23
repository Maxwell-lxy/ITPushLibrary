package com.innotech.innotechpush.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;

/**
 * Created by admin on 2018/4/20.
 */

public class UserInfoReceiver extends BroadcastReceiver{

    public static final String ACTION_UPDATEUSERINFO = "com.inno.push.action.UPDATEUSERINFO";
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.e(context,"sdkname:"+InnotechPushManager.pushSDKName+" geTuiAndUmengIsOk:"+UserInfoUtils.geTuiAndUmengIsOk);
        if(InnotechPushManager.pushSDKName!=null){
            InnotechPushManager.updateUserInfo(context);
        }else{
            if(UserInfoUtils.geTuiAndUmengIsOk==-1){
                UserInfoUtils.geTuiAndUmengIsOk = 0;
            }else if(UserInfoUtils.geTuiAndUmengIsOk==0){
                InnotechPushManager.updateUserInfo(context);
                UserInfoUtils.geTuiAndUmengIsOk = -1;
            }
        }
    }
}
