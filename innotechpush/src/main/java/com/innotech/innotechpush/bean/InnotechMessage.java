package com.innotech.innotechpush.bean;

import java.io.Serializable;

/**
 * 推送回调提供对外的信息类
 */

public class InnotechMessage implements Serializable {
    private String messageId;
    private String title;
    private String content;
    //消息类型 0 通知栏消息 1 透传消息
    private int pushType;
    private String data;
    private String custom;
    private String unfold;
    private String actionContent;
    //0、默认展示样式 1、纯图展示
    private int style;

    public InnotechMessage() {
    }
    
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
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

    public int getPushType() {
        return pushType;
    }

    public void setPushType(int pushType) {
        this.pushType = pushType;
    }

    public String getCustom() {
        return custom;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public String getActionContent() {
        return actionContent;
    }

    public void setActionContent(String actionContent) {
        this.actionContent = actionContent;
    }

    public String getUnfold() {
        return unfold;
    }

    public void setUnfold(String unfold) {
        this.unfold = unfold;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }
}
