package com.innotech.innotechpush.data;

import android.util.Log;
import com.innotech.innotechpush.bean.NetWorkUserInfoResponse;
import com.innotech.innotechpush.bean.UserData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/4/20.
 */

public class DataAnalysis {
    public NetWorkUserInfoResponse analysisUserInfoData(String response){
       // NetWorkUserInfoResponse  userInfoResponse=new Gson().fromJson(response, NetWorkUserInfoResponse.class);
        NetWorkUserInfoResponse  userInfoResponse = new NetWorkUserInfoResponse();
        try {
            JSONObject  jsonObject = new JSONObject(response);
            int code = jsonObject.getInt("code");
            String msg = jsonObject.getString("msg");
            String data = jsonObject.getString("data");

            JSONObject jsonObjectData = new JSONObject(response);
            String guid =   jsonObjectData.getString("guid");
            UserData mUserData = new UserData();
            mUserData.setGuid(guid);
            userInfoResponse.setCode(code);
            userInfoResponse.setMsg(msg);
            userInfoResponse.setData(mUserData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("push_net","sendPostRequest() guid:"+userInfoResponse.getData().getGuid()+" msg:"+userInfoResponse.getMsg());
        return userInfoResponse;
    }
}
