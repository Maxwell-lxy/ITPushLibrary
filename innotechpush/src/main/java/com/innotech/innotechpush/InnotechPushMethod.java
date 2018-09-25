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
import com.innotech.innotechpush.db.ClientMsgNotify;
import com.innotech.innotechpush.sdk.SocketClientService;
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
import java.util.ArrayList;
import java.util.List;

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
            String guid = TokenUtils.getGuid(context);
            aliasObj.put("guid", guid);
            aliasObj.put("alias", alias);
            if (TextUtils.isEmpty(guid)) {
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
            object.put("imei", imei == null ? "" : imei);
            object.put("open_id", openId);
            object.put("app_id", appId);
            final JSONObject paramsObj = new JSONObject();
            paramsObj.put("notify_data", object);
            String params = paramsObj.toString();
            String sign = SignUtils.sign("POST", NetWorkUtils.PATH_CLIENT_MSG_NOTIFY, params);
            NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_CLIENT_MSG_NOTIFY, params, sign, new RequestCallback() {
                @Override
                public void onSuccess(String msg) {
                    LogUtils.e(context, "客户端消息回执成功");
                }

                @Override
                public void onFail(String msg) {
                    if (tryTime < 3) {
                        LogUtils.e(context, "客户端消息回执尝试再次请求");
                        clientMsgNotify(context, array, tryTime + 1);
                    } else {
                        LogUtils.e(context, "客户端消息回执失败");
                        //写入本地数据库
                        new ClientMsgNotify(paramsObj.toString()).save();
                    }
                }
            });
        } catch (JSONException e) {
            LogUtils.e(context, "客户端消息回执参数转换json出错！");
            new ClientLog(context, LogCode.LOG_EX_JSON, "客户端消息回执参数转换json出错").save();
        }
    }

    /**
     * 上报回执
     *
     * @param context
     */
    public synchronized static void uploadClientMsgNotify(final Context context) {
        try {
            final List<ClientMsgNotify> notifies = ClientMsgNotify.find(ClientMsgNotify.class, null, null, null, "ID", "10");
            JSONArray msgIdsArrayForType1 = new JSONArray();
            JSONArray msgIdsArrayForType2 = new JSONArray();
            JSONArray msgIdsArrayForType3 = new JSONArray();
            String guid = "";
            String imei = "";
            String openId = "";
            int appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
            if (notifies != null && notifies.size() > 0) {
                for (ClientMsgNotify notify : notifies) {
                    if (!TextUtils.isEmpty(notify.getNotifyData())) {
                        JSONObject notifyData = new JSONObject(notify.getNotifyData());
                        JSONObject notifyObj = notifyData.getJSONObject("notify_data");
                        guid = notifyObj.getString("guid");
                        imei = notifyObj.getString("imei");
                        openId = notifyObj.getString("open_id");
                        JSONArray idTypeArray = notifyObj.getJSONArray("id_types");
                        for (int i = 0; i < idTypeArray.length(); i++) {
                            JSONObject obj = idTypeArray.getJSONObject(i);
                            int type = obj.getInt("type");
                            JSONArray msgIdsArray = obj.getJSONArray("msg_ids");
                            switch (type) {
                                case 1:
                                    for (int j = 0; j < msgIdsArray.length(); j++) {
                                        msgIdsArrayForType1.put(msgIdsArray.getString(j));
                                    }
                                    break;
                                case 2:
                                    for (int j = 0; j < msgIdsArray.length(); j++) {
                                        msgIdsArrayForType2.put(msgIdsArray.getString(j));
                                    }
                                    break;
                                case 3:
                                    for (int j = 0; j < msgIdsArray.length(); j++) {
                                        msgIdsArrayForType3.put(msgIdsArray.getString(j));
                                    }
                                    break;
                            }
                        }
                    }
                }

                JSONObject idTypesObj1 = new JSONObject();
                idTypesObj1.put("msg_ids", msgIdsArrayForType1);
                idTypesObj1.put("type", 1);
                JSONObject idTypesObj2 = new JSONObject();
                idTypesObj2.put("msg_ids", msgIdsArrayForType2);
                idTypesObj2.put("type", 2);
                JSONObject idTypesObj3 = new JSONObject();
                idTypesObj3.put("msg_ids", msgIdsArrayForType3);
                idTypesObj3.put("type", 3);
                JSONArray idTypesArray = new JSONArray();
                idTypesArray.put(idTypesObj1);
                idTypesArray.put(idTypesObj2);
                idTypesArray.put(idTypesObj3);
                JSONObject object = new JSONObject();
                object.put("id_types", idTypesArray);
                object.put("guid", guid);
                object.put("try_time", 3);
                object.put("imei", imei);
                object.put("open_id", openId);
                object.put("app_id", appId);
                final JSONObject paramsObj = new JSONObject();
                paramsObj.put("notify_data", object);
                String params = paramsObj.toString();
                String sign = SignUtils.sign("POST", NetWorkUtils.PATH_CLIENT_MSG_NOTIFY, params);
                NetWorkUtils.sendPostRequest(context, NetWorkUtils.URL_CLIENT_MSG_NOTIFY, params, sign, new RequestCallback() {
                    @Override
                    public void onSuccess(String msg) {
                        LogUtils.e(context, "客户端消息回执成功");
                        for (ClientMsgNotify notify : notifies) {
                            notify.delete();
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        LogUtils.e(context, "客户端消息回执失败");
                    }
                });
            }
        } catch (JSONException e) {
            LogUtils.e(context, "客户端消息回执参数转换json出错！" + e.getMessage());
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
            e.printStackTrace();
        }
    }

    /**
     * 上报日志
     * 启动service和心跳时进行上报
     *
     * @param context
     */
    public synchronized static void uploadLogs(final Context context) {
        try {
            final List<ClientLog> logs = ClientLog.find(ClientLog.class, null, null, null, "ID", "20");
            String guid = "";
            String imei = "";
            JSONArray array = new JSONArray();
            LogUtils.e(context, "logs的长度" + logs.size());
            if (logs != null && logs.size() > 0) {
                for (ClientLog log : logs) {
                    array.put(log.getLogStr());
                    guid = log.getGuid();
                    imei = log.getImei();
                }
                InnotechPushMethod.clientlog(context, array.toString(), guid, imei, new RequestCallback() {

                    @Override
                    public void onSuccess(String msg) {
                        for (ClientLog log : logs) {
                            log.delete();
                        }
                    }

                    @Override
                    public void onFail(String msg) {
                        LogUtils.e(context, "日志上报失败");
                    }
                });
            }
        } catch (Exception e) {
            LogUtils.e(context, "日志上报报错");
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
