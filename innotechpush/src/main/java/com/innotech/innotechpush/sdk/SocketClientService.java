package com.innotech.innotechpush.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.AlarmManagerUtils;
import com.innotech.innotechpush.utils.LogUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SocketClientService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e(this, "socket服务开始启动");
        SocketManager.getInstance(this.getApplicationContext()).initSocket();
        //上传本地回执
        InnotechPushMethod.uploadClientMsgNotify(this.getApplicationContext());
        //上传本地回执（华为点击）
        InnotechPushMethod.uploadClientMsgNotifyHW(this.getApplicationContext());
        //上报日志
        InnotechPushMethod.uploadLogs(this.getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //定时检查service存活，发送长连接心跳包
        AlarmManagerUtils.setHeartAlarm(this);
        return START_STICKY;
    }

}
