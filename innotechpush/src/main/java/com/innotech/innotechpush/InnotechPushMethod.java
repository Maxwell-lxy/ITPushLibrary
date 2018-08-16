package com.innotech.innotechpush;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;

import com.innotech.innotechpush.bean.UserInfo;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.data.DataAnalysis;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetWorkUtils;
import com.innotech.innotechpush.utils.SignUtils;
import com.innotech.innotechpush.utils.UserInfoSPUtils;
import com.innotech.innotechpush.utils.UserInfoUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 提供
 */

public class InnotechPushMethod {

    private static Handler myHandler;

    /**
     * app上传用户信息
     *
     * @param context：Android平台上app的上下文，建议传入当前app的application context
     */
    public static void updateUserInfo(Context context, final RequestCallback mCallBack) {
        Integer appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        String appKey = Utils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
        UserInfo userInfo = UserInfoUtils.getUserInfo(context, appId, appKey);
        if (userInfo != null) {
            UserInfoUtils.saveTokenToSP(context, userInfo.getDevice_token1(), userInfo.getDevice_token2());
            try {
                String json = UserInfoUtils.objJson(context, userInfo);
                String sign = SignUtils.sign("POST", NetWorkUtils.PATH_UPDATEUSERINFO, json);
                NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_UPDATEUSERINFO, json, sign, new RequestCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        InnotechPushManager.getInstance().initSocketPush();
                        mCallBack.onSuccess(msg);
                    }

                    @Override
                    public void onFail(String msg) {
                        mCallBack.onFail(msg);
                    }
                });
            } catch (JSONException e) {
                LogUtils.e(context, "app上传用户信息参数转换json出错！");
                if (mCallBack != null) {
                    mCallBack.onFail("app上传用户信息参数转换json出错！");
                }
            }
        }
    }

    private static boolean tokenIsChange(Context context) {
        boolean result = false;
        String oldToken = UserInfoSPUtils.getString(context, UserInfoSPUtils.KEY_TOKEN1, null);
        String newToken = UserInfoUtils.deviceToken.getDevice_token1();
        String oldToken2 = UserInfoSPUtils.getString(context, UserInfoSPUtils.KEY_TOKEN2, null);
        String newToken2 = UserInfoUtils.deviceToken.getDevice_token2();
        LogUtils.e(context, "oldToken:" + oldToken + " newToken:" + newToken);
        LogUtils.e(context, "oldToken2:" + oldToken2 + " newToken2:" + newToken2);
        try {
            if (newToken != null && !newToken.equals(oldToken)) {
                result = true;
            }
            if ((newToken2 != null && !newToken2.equals(oldToken2))) {
                result = true;
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return result;
    }

    /**
     * 设置别名
     *
     * @param context：Android平台上app的上下文，建议传入当前app的application context
     * @param alias：为指定用户设置别名
     * @param callback：接口回掉
     */
    public static void setAlias(Context context, String alias, RequestCallback callback) {
        JSONObject aliasObj = new JSONObject();
        try {
            Integer appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            aliasObj.put("app_id", appId);
            String guid = UserInfoSPUtils.getString(context, UserInfoSPUtils.KEY_GUID, "default");
            aliasObj.put("guid", guid);
            aliasObj.put("alias", alias);
            if (guid.equals("default")) {
                if (callback != null) {
                    callback.onFail("guid is default.Please reobtain the valid guid!");
                    return;
                }
            }
            if (alias == null || alias.isEmpty() || alias.length() == 0) {
                if (callback != null) {
                    callback.onFail("Alias can not be null or empty!");
                    return;
                }
            }
            String params = aliasObj.toString();
            String sign = SignUtils.sign("POST", NetWorkUtils.PATH_ALIAS, params);
            NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_ALIAS, params, sign, callback);
        } catch (JSONException e) {
            LogUtils.e(context, "设置别名参数转换json出错！");
            if (callback != null) {
                callback.onFail("设置别名参数转换json出错！");
            }
        }
    }

    public static void launcher(Activity activity) {
        InnotechPushManager.getInstance().setLauncherActivity(activity);
    }

    public static void setHandler(Handler handler) {
        myHandler = handler;
    }

    public static Handler getMyHandler() {
        return myHandler;
    }
}
