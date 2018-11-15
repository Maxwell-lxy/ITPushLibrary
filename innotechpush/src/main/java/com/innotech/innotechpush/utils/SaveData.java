package com.innotech.innotechpush.utils;

import android.content.Context;

import com.innotech.innotechpush.bean.BaseResponse;
import com.innotech.innotechpush.bean.Guid;
import com.innotech.innotechpush.callback.RequestCallback;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 处理接口返回值
 */

public class SaveData {

    public static void saveData(Context context, String json, String url, RequestCallback mCallBack) {
        //app上传用户信息
        if (url.equals(NetWorkUtils.URL_UPDATEUSERINFO)) {
            BaseResponse<Guid> response = new DataAnalysis<Guid>().analysisData(json, Guid.class.getName());
            if (response.getCode() == 0) {
                try {
                    TokenUtils.saveGuid(context, response.getData().getGuid());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                UserInfoSPUtils.putString(context, UserInfoSPUtils.KEY_GUID, response.getData().getGuid());
                if (mCallBack != null) {
                    mCallBack.onSuccess(response.getData().getGuid());
                }
                LogUtils.e(context, "app上传用户信息成功！");
            } else {
                if (mCallBack != null) {
                    mCallBack.onFail("app上传用户信息失败！");
                }
                LogUtils.e(context, "app上传用户信息失败！");
            }
        }
        //绑定用户别名
        else if (url.equals(NetWorkUtils.URL_ALIAS)) {
            BaseResponse response = new DataAnalysis().analysisData(json, null);
            if (response.getCode() == 0) {
                LogUtils.e(context, "绑定用户别名成功！");
                if (mCallBack != null) {
                    mCallBack.onSuccess("绑定用户别名成功！");
                }

            } else {
                LogUtils.e(context, "绑定用户别名失败！");
                if (mCallBack != null) {
                    mCallBack.onFail("绑定用户别名失败！");
                }
            }
        }
        //获取socket地址
        else if (url.equals(NetWorkUtils.URL_SOCKET_ADDR)) {
            BaseResponse<String> response = new DataAnalysis<String>().analysisData(json, String.class.getName());
            if (response.getCode() == 0) {
                if (mCallBack != null) {
                    mCallBack.onSuccess(response.getData());
                }
            } else {
                if (mCallBack != null) {
                    mCallBack.onFail("获取socket地址失败！");
                }
            }
        }
        //客户端日志接口、客户端消息回执接口
        else if (url.equals(NetWorkUtils.URL_LOG)) {
            BaseResponse response = new DataAnalysis().analysisData(json, null);
            if (response.getCode() == 0) {
                if (mCallBack != null) {
                    mCallBack.onSuccess("LogServer成功！");
                }
            } else {
                if (mCallBack != null) {
                    mCallBack.onFail("LogServer失败！");
                }
            }
        }
    }
}
