package com.innotech.innotechpush;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.innotech.innotechpush.bean.UserInfoModel;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.config.LogCode;
import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.LogUtils;
import com.innotech.innotechpush.utils.NetWorkUtils;
import com.innotech.innotechpush.utils.SignUtils;
import com.innotech.innotechpush.utils.TokenUtils;
import com.innotech.innotechpush.utils.UserInfoSPUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONArray;
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
            new ClientLog(context, LogCode.LOG_EX_JSON, "app上传用户信息参数转换json出错！").save();
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
            new ClientLog(context, LogCode.LOG_EX_JSON, "设置别名参数转换json出错！").save();
            if (callback != null) {
                callback.onFail("设置别名参数转换json出错！");
            }
        }
    }

    /**
     * 客户端消息回执接口
     */
    public static void clientMsgNotify(final Context context, final JSONArray array, final int tryTime) {
        try {
            String guid = TokenUtils.getGuid(context);
            String imei = Utils.getIMEI(context);
            String openId = getTK(context);
            int appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            JSONObject object = new JSONObject();
            object.put("id_types", array);
            object.put("guid", guid);
            object.put("try_time", tryTime);
            object.put("imei", imei);
            object.put("open_id", openId);
            object.put("app_id", appId);
            JSONObject paramsObj = new JSONObject();
            paramsObj.put("notify_data", object);
            String params = paramsObj.toString();
            String sign = SignUtils.sign("POST", NetWorkUtils.PATH_CLIENT_MSG_NOTIFY, params);
            NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_CLIENT_MSG_NOTIFY, params, sign, new RequestCallback() {
                @Override
                public void onSuccess(String msg) {
                    LogUtils.e(context, "客户端消息回执成功");
                    ClientLog log = new ClientLog(context, LogCode.LOG_DATA_API, "客户端消息回执成功");
                    log.save();
                }

                @Override
                public void onFail(String msg) {
                    if (tryTime < 3) {
                        LogUtils.e(context, "客户端消息回执尝试再次请求");
                        ClientLog log = new ClientLog(context, LogCode.LOG_DATA_API, "客户端消息回执尝试再次请求");
                        log.save();
                        clientMsgNotify(context, array, tryTime + 1);
                    } else {
                        LogUtils.e(context, "客户端消息回执失败");
                        //todo 写入本地数据库

                        ClientLog log = new ClientLog(context, LogCode.LOG_DATA_API, "客户端消息回执失败");
                        log.save();
                    }
                }
            });
        } catch (JSONException e) {
            LogUtils.e(context, "客户端消息回执参数转换json出错！");
            new ClientLog(context, LogCode.LOG_EX_JSON, "客户端消息回执参数转换json出错").save();
        }
    }

    /**
     * 客户端API - 客户端日志接口
     */
    public synchronized static void clientlog(final Context context, String log, String guid, String imei, final RequestCallback callback) {
        try {
            int appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            JSONObject paramsObj = new JSONObject();
            paramsObj.put("log_str", log);
            paramsObj.put("guid", guid);
            paramsObj.put("imei", imei);
            paramsObj.put("app_id", appId);
            String params = paramsObj.toString();
            String sign = SignUtils.sign("POST", NetWorkUtils.PATH_CLIENT_LOG, params);
            NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_CLIENT_LOG, params, sign, new RequestCallback() {
                @Override
                public void onSuccess(String msg) {
                    LogUtils.e(context, "客户端日志成功");
                    callback.onSuccess(msg);
                }

                @Override
                public void onFail(String msg) {
                    LogUtils.e(context, "客户端日志失败");
                    callback.onFail(msg);
                }
            });
        } catch (JSONException e) {
            LogUtils.e(context, "客户端日志失败");
            new ClientLog(context, LogCode.LOG_EX_JSON, "客户端日志失败").save();
            e.printStackTrace();
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
            new ClientLog(context, LogCode.LOG_DATA_COMMON, "反作弊的TK值：" + tk).save();
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
