package com.innotech.innotechpush.db;

import com.orm.SugarRecord;

public class ClientMsgNotify extends SugarRecord {
    String notifyData;

    public ClientMsgNotify() {
    }


    public ClientMsgNotify(String notifyData) {
        this.notifyData = notifyData;
    }

    public String getNotifyData() {
        return notifyData;
    }

    public void setNotifyData(String notifyData) {
        this.notifyData = notifyData;
    }
}
