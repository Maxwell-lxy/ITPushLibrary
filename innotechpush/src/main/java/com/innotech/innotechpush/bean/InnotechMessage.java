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
}
