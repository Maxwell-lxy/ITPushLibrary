package com.innotech.innotechpush.db;

import com.orm.SugarRecord;

public class ClientMsgNotifyHW extends SugarRecord {
    String notifyData;

    public ClientMsgNotifyHW() {
    }


    public ClientMsgNotifyHW(String notifyData) {
        this.notifyData = notifyData;
    }

    public String getNotifyData() {
        return notifyData;
    }

    public void setNotifyData(String notifyData) {
        this.notifyData = notifyData;
    }
}
