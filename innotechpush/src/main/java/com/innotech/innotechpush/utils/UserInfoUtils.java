package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.Intent;
import com.innotech.innotechpush.bean.DeviceInfo;
import com.innotech.innotechpush.bean.DeviceToken;
import com.innotech.innotechpush.bean.UserInfo;
import com.innotech.innotechpush.receiver.UserInfoReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/4/19.
 */

public class UserInfoUtils {

    public static DeviceToken deviceToken = new DeviceToken();
    public static int geTuiAndUmengIsOk = -1;

    public static UserInfo getUserInfo(Context context,String appId, String appKey){
        UserInfo userInfo = new UserInfo();
        userInfo.setApp_id(appId);
        userInfo.setApp_key(appKey);
        userInfo.setDevice_token(deviceToken);
        userInfo.setImei(Utils.getIMEI(context));
        userInfo.setIp(Utils.getIPAddress(context));
        userInfo.setOpen_notice(Utils.isNotificationEnabled(context));
        userInfo.setChang(Utils.getChange(context));
        userInfo.setOs(Utils.getOS());
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setAndroid_id(Utils.getAndroidId(context));
        deviceInfo.setSn(Utils.getSerialNumber());
        userInfo.setInfo(deviceInfo);
        return userInfo;
    }

    public static String objJson(Context context,UserInfo mUserInfo) throws JSONException {
        LogUtils.e(context,mUserInfo.getApp_id()+" "+mUserInfo.getApp_key());
        JSONObject deviceToken = new JSONObject();

        DeviceToken mDeviceToken =  mUserInfo.getDevice_token();
        deviceToken.put("getui",mDeviceToken.getGetui());
        deviceToken.put("umeng",mDeviceToken.getUmeng());
        deviceToken.put("huawei",mDeviceToken.getHuawei());
        deviceToken.put("mi",mDeviceToken.getMi());
        deviceToken.put("meizu",mDeviceToken.getMeizu());

        JSONObject deviceInfo = new JSONObject();
        DeviceInfo mDeviceInfo = mUserInfo.getInfo();
        deviceInfo.put("android_id",mDeviceInfo.getAndroid_id());
        deviceInfo.put("sn",mDeviceInfo.getSn());

        JSONObject userInfo = new JSONObject();
        userInfo.put("app_id",mUserInfo.getApp_id());
        userInfo.put("app_key",mUserInfo.getApp_key());
        userInfo.put("info",deviceInfo);
        userInfo.put("imei",mUserInfo.getImei());
        userInfo.put("device_token",deviceToken);
        userInfo.put("os",mUserInfo.getOs());
        userInfo.put("ip",mUserInfo.getIp());
        userInfo.put("open_notice",mUserInfo.isOpen_notice());
        userInfo.put("chang",mUserInfo.isChang());
        return userInfo.toString();
    }


    public static void sendBroadcast(Context context){
        Intent sendBIntent = new Intent(UserInfoReceiver.ACTION_UPDATEUSERINFO);
        context.sendBroadcast(sendBIntent);
    }
}
