package com.innotech.innotechpush.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

public class TokenUtils {

    /**
     * 保存token
     * 暂时只保存guid
     * @param context
     * @param guid
     * @throws JSONException
     */
    public static void saveGuid(Context context,String guid) throws JSONException {
        JSONObject object = new JSONObject();
        object.put("GUID", guid);
        FileUtils.writeFileData(context, object.toString(), FileUtils.FILE_TOKEN);
    }

    /**
     * 获取guid
     * @param context
     * @return
     * @throws JSONException
     */
    public static String getGuid(Context context) throws JSONException {
        String json = FileUtils.readFileData(context, FileUtils.FILE_TOKEN);
        JSONObject object = new JSONObject(json);
        String guid = object.getString("GUID");
        return guid;
    }
}
