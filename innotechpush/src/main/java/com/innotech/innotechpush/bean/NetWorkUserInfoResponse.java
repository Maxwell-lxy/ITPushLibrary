package com.innotech.innotechpush.bean;

/**
 * Created by admin on 2018/4/20.
 */

public class NetWorkUserInfoResponse {
    private  int code;
    private String msg;
    private UserData data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public UserData getData() {
        return data;
    }

    public void setData(UserData data) {
        this.data = data;
    }
}
