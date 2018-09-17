package com.innotech.innotechpush;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetWorkUtils;
import com.innotech.innotechpush.utils.SignUtils;
import com.innotech.innotechpush.utils.UserInfoSPUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        try {
            //channel为空时说明部分数据被系统回收掉了，需要重新初始化一下
            if (TextUtils.isEmpty(UserInfoModel.getInstance().getChannel())) {
                UserInfoModel.getInstance().init(context);
            }
            if (TextUtils.isEmpty(UserInfoModel.getInstance().getOpen_id())) {
                UserInfoModel.getInstance().setOpen_id(getTK(context));
                //如果由于openid延迟获取不到，则延迟1s再获取一次。
                if (TextUtils.isEmpty(UserInfoModel.getInstance().getOpen_id())) {
                    try {
                        Thread.sleep(1000);
                        UserInfoModel.getInstance().setOpen_id(getTK(context));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            String json = UserInfoModel.getInstance().toJson();
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

    private static String getTK(Context context) {
        String tk = "";
        try {
            Class clazz = Class.forName("com.inno.innosdk.pb.InnoMain");
            Method checkInfo = clazz.getMethod("checkInfo", Context.class);
            Object object = checkInfo.invoke(clazz.newInstance(), context);
            tk = (String) object;
            LogUtils.e(context, "反作弊的TK值：" + tk);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return tk;
    }

}
