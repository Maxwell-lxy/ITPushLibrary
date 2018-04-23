package com.innotech.innotechpush.bean;

/**
 * Created by admin on 2018/4/19.
 */

public class UserInfo {
    private  String app_id;
    private  String app_key;
    private DeviceInfo info;
    private String  imei;
    private DeviceToken device_token;
    private String os;
    private String ip;
    private boolean open_notice;
    private boolean chang;

    public String getApp_id() {
        return app_id;
    }

    public void setApp_id(String app_id) {
        this.app_id = app_id;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public DeviceInfo getInfo() {
        return info;
    }

    public void setInfo(DeviceInfo info) {
        this.info = info;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public DeviceToken getDevice_token() {
        return device_token;
    }

    public void setDevice_token(DeviceToken device_token) {
        this.device_token = device_token;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isOpen_notice() {
        return open_notice;
    }

    public void setOpen_notice(boolean open_notice) {
        this.open_notice = open_notice;
    }

    public boolean isChang() {
        return chang;
    }

    public void setChang(boolean chang) {
        this.chang = chang;
    }

}
