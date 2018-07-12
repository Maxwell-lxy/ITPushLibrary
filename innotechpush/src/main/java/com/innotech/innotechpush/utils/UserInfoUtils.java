package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.bean.Channel;
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
    public static boolean geTuiIsOk = false;
    public static boolean uMengIsOk = false;

    public static UserInfo getUserInfo(Context context, Integer appId, String appKey) {
        UserInfo userInfo = new UserInfo();
        userInfo.setApp_id(appId);
        userInfo.setApp_key(appKey);
        userInfo.setDevice_token1(deviceToken.getDevice_token1());
        userInfo.setDevice_token2(deviceToken.getDevice_token2());
        userInfo.setIp(Utils.getIPAddress(context));
        String guid = UserInfoSPUtils.getString(context, UserInfoSPUtils.KEY_GUID, null);
        userInfo.setGuid(guid);
        userInfo.setOpen_notice(Utils.isNotificationEnabled(context));
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setImei(Utils.getIMEI(context));
        deviceInfo.setOs(Utils.getOS());
        deviceInfo.setAndroid_id(Utils.getAndroidId(context));
        deviceInfo.setSn(Utils.getSerialNumber());
        userInfo.setDevice_info(deviceInfo);
        //小米设备或MIUI系统
        if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
            userInfo.setChannel(Channel.MI);
        }
        //魅族设备
        else if (Utils.isMeizuDevice()) {
            userInfo.setChannel(Channel.MZ);
        }
        //华为设备
        else if (Utils.isHuaweiDevice()) {
            userInfo.setChannel(Channel.HW);
        }
        //其他设备
        else {
            userInfo.setChannel(Channel.UNION);
        }
        return userInfo;
    }

    public static String objJson(Context context, UserInfo mUserInfo) throws JSONException {
        LogUtils.e(context, mUserInfo.getApp_id() + " " + mUserInfo.getApp_key());
        JSONObject userInfoObj = new JSONObject();
        JSONObject deviceInfo = new JSONObject();
        DeviceInfo mDeviceInfo = mUserInfo.getDevice_info();
        deviceInfo.put("android_id", mDeviceInfo.getAndroid_id());
        deviceInfo.put("sn", mDeviceInfo.getSn());
        deviceInfo.put("imei", mDeviceInfo.getImei());
        deviceInfo.put("os", mDeviceInfo.getOs());

        JSONObject userInfo = new JSONObject();
        userInfo.put("app_id", mUserInfo.getApp_id());
        userInfo.put("app_key", mUserInfo.getApp_key());
        userInfo.put("device_info", deviceInfo);
        userInfo.put("device_token1", mUserInfo.getDevice_token1());
        userInfo.put("device_token2", mUserInfo.getDevice_token2());
        userInfo.put("channel", mUserInfo.getChannel());
        userInfo.put("ip", mUserInfo.getIp());
        userInfo.put("guid", mUserInfo.getGuid());
        userInfo.put("open_notice", mUserInfo.isOpen_notice());

        userInfoObj.put("info", userInfo);
        return userInfoObj.toString();
    }


    public static void sendBroadcast(Context context) {
        Intent sendBIntent = new Intent(UserInfoReceiver.ACTION_UPDATEUSERINFO);
        context.sendBroadcast(sendBIntent);
    }

    public static void saveTokenToSP(Context context, String token1, String token2) {
        if (token1 != null) {
            UserInfoSPUtils.putString(context, UserInfoSPUtils.KEY_TOKEN1, token1);
        }
        if (token2 != null) {
            UserInfoSPUtils.putString(context, UserInfoSPUtils.KEY_TOKEN2, token2);
        }

    }

    public static boolean canUupdateUserInfo(Context context) {
//        boolean result = false;
//        long curTime =  System.currentTimeMillis();
//        long lastTime = UserInfoSPUtils.getLong(context,UserInfoSPUtils.KEY_UPDATEUSERINFO_TIME,curTime);
//        long diffTime = curTime-lastTime;
//        LogUtils.d(context,"canUupdateUserInfo() curTime:"+curTime+" lastTime:"+lastTime+" diffTime:"+diffTime);
//        long standardDiffTime = 1000*60*60*24;
//        if(diffTime>=standardDiffTime||diffTime==0){
//            result = true;
//            UserInfoSPUtils.putLong(context,UserInfoSPUtils.KEY_UPDATEUSERINFO_TIME,curTime);
//        }
//        LogUtils.d(context,"canUupdateUserInfo() result:"+result);
        return true;
    }

    public static void resetGeTuiAndUmeng() {
        geTuiIsOk = false;
        uMengIsOk = false;
    }
}