package com.innotech.innotechpush.sdk;

import java.io.Serializable;

public class PushMessage implements Serializable {
    private static final long serialVersionUID = -781818219968760565L;
    //appid
    private int appId;
    //消息id
    private String msg_id;
    //要推送的消息标题
    private String title;
    //要推送的消息内容
    private String content;
    //要透传的消息，消息内容，根据业务自行约定
    private String transmission;
    //展开式通知内容
    private String unfold;
    private String guid;
    //1透传
    private int pass_through;
    //0、默认展示样式 1、纯图展示
    private int style;

    public String getMsg_id() {
        return msg_id;
    }

    public void setMsg_id(String msg_id) {
        this.msg_id = msg_id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTransmission() {
        return transmission;
    }

    public void setTransmission(String transmission) {
        this.transmission = transmission;
    }

    public String getUnfold() {
        return unfold;
    }

    public void setUnfold(String unfold) {
        this.unfold = unfold;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public int getPass_through() {
        return pass_through;
    }

    public void setPass_through(int pass_through) {
        this.pass_through = pass_through;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }
}
