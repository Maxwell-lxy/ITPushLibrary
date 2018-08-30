package com.innotech.innotechpush.bean;

/**
 * Created by admin on 2018/4/19.
 */

public class DeviceInfo {
    //设备信息
    private String android_id;
    //Serial Number
    private String sn;
    //imei号
    private String imei;
    //用户品牌
    private String os;
    //操作系统版本
    private String os_version;
    //用户设备名称
    private String os_device;

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getImei() {
        return imei;
    }


    public void setImei(String imei) {
        this.imei = imei;
    }


    public String getAndroid_id() {
        return android_id;
    }

    public void setAndroid_id(String android_id) {
        this.android_id = android_id;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getOs_device() {
        return os_device;
    }

    public void setOs_device(String os_device) {
        this.os_device = os_device;
    }
}
