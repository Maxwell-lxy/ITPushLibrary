package com.innotech.innotechpush.utils;

import android.content.Context;

import com.innotech.innotechpush.bean.BaseResponse;
import com.innotech.innotechpush.bean.Guid;
import com.innotech.innotechpush.data.DataAnalysis;

/**
 * 处理接口返回值
 */

public class SaveData {

    public static void saveData(Context context, String json, String url) {
        //app上传用户信息
        if (url.equals(NetWorkUtils.URL_UPDATEUSERINFO)) {
            BaseResponse<Guid> response = new DataAnalysis<Guid>().analysisData(json, Guid.class.getName());
            if (response.getCode() == 0) {
                UserInfoSPUtils.putString(context, UserInfoSPUtils.KEY_GUID, response.getData().getGuid());
                LogUtils.e(context, "app上传用户信息成功！");
            } else {
                LogUtils.e(context, "app上传用户信息失败！");
            }
        }
        //绑定用户别名
        else if (url.equals(NetWorkUtils.URL_ALIAS)) {
            BaseResponse response = new DataAnalysis().analysisData(json, null);
            if (response.getCode() == 0) {
                LogUtils.e(context, "绑定用户别名成功！");
            } else {
                LogUtils.e(context, "绑定用户别名失败！");
            }
        }
    }
}
