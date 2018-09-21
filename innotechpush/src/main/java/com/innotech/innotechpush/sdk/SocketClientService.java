package com.innotech.innotechpush.sdk;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.AlarmManagerUtils;
import com.innotech.innotechpush.utils.LogUtils;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class SocketClientService extends Service {

    private final static String TAG = "socket-library";
    private final static int SERVICE_ID = 1003;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "socket服务开始启动");
        SocketManager.getInstance(this.getApplicationContext()).initSocket();
        //上报日志
        final List<ClientLog> logs = ClientLog.listAll(ClientLog.class);
        final List<ClientLog> tempLogs = new ArrayList<>();
        final List<ClientLog> successLogs = new ArrayList<>();
        String guid = "";
        String imei = "";
        JSONArray array = new JSONArray();
        LogUtils.e(this, "logs的长度" + logs.size());
        for (int i = 0; i < logs.size(); i++) {
            if (i == 0) {
                tempLogs.add(logs.get(i));
            } else if (i % 30 == 0 || i == logs.size() - 1) {//30条上报一次
                array = new JSONArray();
                if (i == logs.size() - 1) {
                    tempLogs.add(logs.get(i));
                }
                for (ClientLog log : tempLogs) {
                    array.put(log.getLogStr());
                    guid = log.getGuid();
                    imei = log.getImei();
                }
                LogUtils.e(this, "tempLogs的长度" + tempLogs.size());
                successLogs.addAll(tempLogs);
                tempLogs.clear();
                InnotechPushMethod.clientlog(this.getApplicationContext(), array.toString(), guid, imei, new RequestCallback() {

                    @Override
                    public void onSuccess(String msg) {
                        LogUtils.e(SocketClientService.this, "successLogs的长度" + successLogs.size());
                        if (successLogs.size() > 0) {
                            for (ClientLog log : successLogs) {
                                log.delete();
                            }
                            successLogs.clear();
                        }
                        LogUtils.e(SocketClientService.this, "successLogs的长度" + successLogs.size());
                    }

                    @Override
                    public void onFail(String msg) {

                    }
                });
            } else {
                tempLogs.add(logs.get(i));
            }
        }
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

    /**
     * 给 API >= 18 的平台上用的灰色保活手段
     */
    public static class GrayInnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            startForeground(SERVICE_ID, new Notification());
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }

}
