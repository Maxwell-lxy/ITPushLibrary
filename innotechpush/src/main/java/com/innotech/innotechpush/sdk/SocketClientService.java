package com.innotech.innotechpush.sdk;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.db.ClientLog;
import com.innotech.innotechpush.utils.AlarmManagerUtils;

import org.json.JSONArray;

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
        String guid = "";
        String imei = "";
        JSONArray array = new JSONArray();
        for (ClientLog log : logs) {
            array.put(log.getLogStr());
            guid = log.getGuid();
            imei = log.getImei();
        }
        InnotechPushMethod.clientlog(this.getApplicationContext(), array.toString(), guid, imei, new RequestCallback() {

            @Override
            public void onSuccess(String msg) {
                for (ClientLog log : logs) {
                    log.delete();
                }
            }

            @Override
            public void onFail(String msg) {

            }
        });
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
