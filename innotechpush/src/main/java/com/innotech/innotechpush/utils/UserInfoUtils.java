package com.innotech.innotechpush.utils;

import android.content.Context;
import android.content.Intent;

import com.innotech.innotechpush.bean.Channel;
import com.innotech.innotechpush.bean.DeviceInfo;
import com.innotech.innotechpush.bean.DeviceToken;
import com.innotech.innotechpush.bean.UserInfo;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.receiver.UserInfoReceiver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/4/19.
 */

public class UserInfoUtils {

//    public static String objJson(Context context, UserInfo mUserInfo) throws JSONException {
//        LogUtils.e(context, mUserInfo.getApp_id() + " " + mUserInfo.getApp_key());
//        JSONObject userInfoObj = new JSONObject();
//        JSONObject deviceInfo = new JSONObject();
//        DeviceInfo mDeviceInfo = mUserInfo.getDevice_info();
//        deviceInfo.put("android_id", mDeviceInfo.getAndroid_id());
//        deviceInfo.put("sn", mDeviceInfo.getSn());
//        deviceInfo.put("imei", mDeviceInfo.getImei());
//        deviceInfo.put("os", mDeviceInfo.getOs());
//
//        JSONObject userInfo = new JSONObject();
//        userInfo.put("app_id", mUserInfo.getApp_id());
//        userInfo.put("app_key", mUserInfo.getApp_key());
//        userInfo.put("device_info", deviceInfo);
//        userInfo.put("device_token1", mUserInfo.getDevice_token1());
//        userInfo.put("device_token2", mUserInfo.getDevice_token2());
//        userInfo.put("channel", mUserInfo.getChannel());
//        userInfo.put("ip", mUserInfo.getIp());
//        userInfo.put("guid", mUserInfo.getGuid());
//        userInfo.put("open_notice", mUserInfo.isOpen_notice());
//        userInfo.put("idempotent", UserInfoUtils.UUID);
//        userInfo.put("version", PushConstant.INNOTECH_PUSH_VERSION);
//
//        userInfoObj.put("info", userInfo);
//        return userInfoObj.toString();
//    }

}