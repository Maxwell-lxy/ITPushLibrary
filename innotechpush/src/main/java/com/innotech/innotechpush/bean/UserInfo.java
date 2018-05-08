package com.innotech.innotechpush.bean;

/**
 * app上传用户信息的参数类
 */

public class UserInfo {
    private Integer app_id;
    private String app_key;
    private DeviceInfo device_info;
    private String device_token1;
    private String device_token2;
    private String channel;
    private String ip;
    private boolean open_notice;

    public Integer getApp_id() {
        return app_id;
    }

    public void setApp_id(Integer app_id) {
        this.app_id = app_id;
    }

    public DeviceInfo getDevice_info() {
        return device_info;
    }

    public void setDevice_info(DeviceInfo device_info) {
        this.device_info = device_info;
    }

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

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
