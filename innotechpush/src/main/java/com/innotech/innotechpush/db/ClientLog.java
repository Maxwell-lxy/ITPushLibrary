package com.innotech.innotechpush.db;

import android.content.Context;

import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.utils.TokenUtils;
import com.innotech.innotechpush.utils.Utils;
import com.orm.SugarRecord;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientLog extends SugarRecord {

    String logStr;
    String guid;
    String imei;
    int appId;

    public ClientLog() {
    }

    public ClientLog(Context context, int code, String logStr) {
        try {
            JSONObject object = new JSONObject();
            object.put("code", code);
            object.put("message", logStr);
            this.logStr = object.toString();
            this.guid = TokenUtils.getGuid(context);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.imei = Utils.getIMEI(context);
        this.appId = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
    }

    public String getLogStr() {
        return logStr;
    }

    public void setLogStr(String logStr) {
        this.logStr = logStr;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

}
