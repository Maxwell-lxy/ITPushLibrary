package com.innotech.innotechpush.bean;

/**
 * Created by admin on 2018/4/20.
 */

public class DeviceToken {
    private String device_token1;//推送token，个推：cid，华为：device_token，小米：regId，魅族：pushId
    private String device_token2;//友盟：device_token

    public String getDevice_token1() {
        return device_token1;
    }

    public void setDevice_token1(String device_token1) {
        this.device_token1 = device_token1;
    }

    public String getDevice_token2() {
        return device_token2;
    }

    public void setDevice_token2(String device_token2) {
        this.device_token2 = device_token2;
    }
}
