package com.innotech.innotechpush.sdk;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.os.Build;

public class KeepApplication extends Application {

    public boolean keepLive0;//音频播放
    public boolean keepLive1;//熄屏自启
    public boolean combine; //组合策略,熄屏时不播放音频

    void initKeepAlive() {
        keepLive0 = true && !Build.MANUFACTURER.equals("huawei");
        keepLive1 = true;
        combine = false;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        initKeepAlive();
        String currentProcName = "";
        KeepLiveService.start(base);
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
                if (processInfo.pid == pid) {
                    currentProcName = processInfo.processName;
                    break;
                }
            }
            if (currentProcName.equals(getPackageName())) {
                ActivityHelper.registerActivityCallback(this);
            }
        }
    }
}
