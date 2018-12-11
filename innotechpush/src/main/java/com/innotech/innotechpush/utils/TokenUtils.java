package com.innotech.innotechpush.utils;

import android.content.Context;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenUtils {

    /**
     * 保存token
     * 暂时只保存guid
     *
     * @param context：上下文
     * @param guid：guid
     * @throws JSONException：异常
     */
    public static void saveGuid(Context context, String guid) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("GUID", guid);
        FileUtils.writeFileData(context, object.toString(), FileUtils.FILE_TOKEN);
    }

    /**
     * 获取guid
     *
     * @param context:上下文
     * @return guid
     */
    public static String getGuid(Context context) {
        String json = FileUtils.readFileData(context, FileUtils.FILE_TOKEN);
        if (!TextUtils.isEmpty(json)) {
            try {
                JSONObject object = new JSONObject(json);
                return object.optString("GUID");
            } catch (JSONException e) {
                return "";
            }
        }
        return "";
    }
}
