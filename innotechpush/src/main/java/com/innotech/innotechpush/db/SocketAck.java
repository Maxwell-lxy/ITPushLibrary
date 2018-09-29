package com.innotech.innotechpush.db;

import com.orm.SugarRecord;

public class SocketAck extends SugarRecord {

    String json;
    int cmd;

    public SocketAck() {
    }

    public SocketAck(String json, int cmd) {
        this.json = json;
        this.cmd = cmd;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }
}
