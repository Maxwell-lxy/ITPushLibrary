package com.innotech.innotechpush.sdk;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.utils.AlarmManagerUtils;
import com.innotech.innotechpush.utils.LogUtils;
import com.orm.SugarContext;

public class SocketClientService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.e(this, "socket服务开始启动");
        SocketManager.getInstance(this.getApplicationContext()).initSocket();
        //定时检查service存活，发送长连接心跳包
        AlarmManagerUtils.setHeartAlarm(this);
        execDbMethod();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * 执行与数据库有关的上报方法
     * 1、上传本地回执
     * 2、上传本地回执（华为点击）
     * 3、上报日志
     */
    private void execDbMethod() {
        try {
            SugarContext.getSugarContext();
            //没有报错，说明数据库已初始化，可以操作数据库相关方法
            //上传本地回执
            InnotechPushMethod.uploadClientMsgNotify(this.getApplicationContext());
            //上传本地回执（华为点击）
            InnotechPushMethod.uploadClientMsgNotifyHW(this.getApplicationContext());
            //上报日志
            InnotechPushMethod.uploadLogs(this.getApplicationContext());
        } catch (NullPointerException e) {
            LogUtils.e(this.getApplicationContext(), "execDbMethod e：" + e.getMessage());
            SugarContext.init(this.getApplicationContext());
            //上传本地回执
            InnotechPushMethod.uploadClientMsgNotify(this.getApplicationContext());
            //上传本地回执（华为点击）
            InnotechPushMethod.uploadClientMsgNotifyHW(this.getApplicationContext());
            //上报日志
            InnotechPushMethod.uploadLogs(this.getApplicationContext());
        }
    }

}
