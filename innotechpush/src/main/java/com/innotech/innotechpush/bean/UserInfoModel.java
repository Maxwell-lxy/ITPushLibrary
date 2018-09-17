package com.innotech.innotechpush.bean;

import android.content.Context;
import android.os.Build;

import com.innotech.innotechpush.config.PushConstant;
import com.innotech.innotechpush.utils.UserInfoSPUtils;
import com.innotech.innotechpush.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public class UserInfoModel {

    private static UserInfoModel userInfoModel;
    //应用id
    private int app_id;
    //appkey
    private String app_key;
    //设备信息
    private DeviceInfo device_info;
    //推送token，个推：cid，华为：device_token，小米：regId，魅族：pushId
    private String device_token1;
    //注册的渠道：'mi','huawei','meizu','union' union 包括友盟和个推, 'ios'
    private String channel;
    //客户端ip，app端不用传，gateway传过来
    private String ip;
    //用户通知开起状态 true 开启，false 关闭
    private boolean open_notice;
    //guid不为空会进行数据更新
    private String guid;
    //idempotent进行幂等性判断
    private String idempotent;
    //客户端安装的sdk版本号
    private String version;
    //反作弊惟一id
    private String open_id;

    public static UserInfoModel getInstance() {
        if (userInfoModel == null) {
            userInfoModel = new UserInfoModel();
        }
        return userInfoModel;
    }

    public void init(Context context) {
        this.app_id = Utils.getMetaDataInteger(context, PushConstant.INNOTECH_APP_ID);
        this.app_key = Utils.getMetaDataString(context, PushConstant.INNOTECH_APP_KEY);
        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setImei(Utils.getIMEI(context));
        deviceInfo.setOs(Build.BRAND);
        deviceInfo.setAndroid_id(Utils.getAndroidId(context));
        deviceInfo.setSn(Utils.getSerialNumber());
        deviceInfo.setOs_version(Build.VERSION.RELEASE);
        deviceInfo.setOs_device(Build.MODEL);
        this.device_info = deviceInfo;
        //小米设备或MIUI系统
        if (Utils.isXiaomiDevice() || Utils.isMIUI()) {
            this.channel = Channel.MI;
        }
        //魅族设备
        else if (Utils.isMeizuDevice()) {
            this.channel = Channel.MZ;
        }
        //华为设备
//        else if (Utils.isHuaweiDevice()) {
//            this.channel = Channel.HW;
//        }
        //oppo设备
        else if (com.coloros.mcssdk.PushManager.isSupportPush(context) && Utils.isOPPO()) {
            this.channel = Channel.OPPO;
        }
        //其他设备
        else {
            this.channel = Channel.UNION;
        }
        this.ip = Utils.getIPAddress(context);
        this.open_notice = Utils.isNotificationEnabled(context);
        this.guid = UserInfoSPUtils.getString(context, UserInfoSPUtils.KEY_GUID, null);
        this.idempotent = UUID.randomUUID().toString();
        this.version = PushConstant.INNOTECH_PUSH_VERSION;
    }

    public String toJson() throws JSONException {
        JSONObject devJson = new JSONObject();
        if (this.device_info != null) {
            devJson.put("android_id", this.device_info.getAndroid_id());
            devJson.put("sn", this.device_info.getSn());
            devJson.put("imei", this.device_info.getImei());
            devJson.put("os", this.device_info.getOs());
            devJson.put("os_version", this.device_info.getOs_version());
            devJson.put("os_device", this.device_info.getOs_device());
        }
        JSONObject userJson = new JSONObject();
        userJson.put("app_id", this.app_id);
        userJson.put("app_key", this.app_key);
        userJson.put("device_info", devJson);
        userJson.put("device_token1", this.device_token1);
        userJson.put("channel", this.channel);
        userJson.put("ip", this.ip);
        userJson.put("open_notice", this.open_notice);
        userJson.put("guid", this.guid);
        userJson.put("idempotent", this.idempotent);
        userJson.put("version", this.version);
        userJson.put("open_id", this.open_id);
        JSONObject infoJson = new JSONObject();
        infoJson.put("info", userJson);
        return infoJson.toString();
    }

    public int getApp_id() {
        return app_id;
    }

    public void setApp_id(int app_id) {
        this.app_id = app_id;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public String getIdempotent() {
        return idempotent;
    }

    public void setIdempotent(String idempotent) {
        this.idempotent = idempotent;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getOpen_id() {
        return open_id;
    }

    public void setOpen_id(String open_id) {
        this.open_id = open_id;
    }
}
