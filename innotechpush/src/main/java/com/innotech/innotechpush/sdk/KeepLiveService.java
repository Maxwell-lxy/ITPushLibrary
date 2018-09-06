package com.innotech.innotechpush.sdk;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;

public class KeepLiveService extends Service {
    private BroadcastReceiver receiver;
    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    private int mInterval = 1000; // 1 s by default, can be changed later
    private Handler mHandler;
    private KeepApplication application;

    public static void start(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, KeepLiveService.class);
        intent.setPackage(context.getPackageName());
        try {
            context.startService(intent);
        } catch (RuntimeException e) {
        }
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            if (powerManager != null) {
                if (powerManager.isScreenOn() || (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive())) {
                    acquireLock();
                } else {
                    mHandler.postDelayed(mStatusChecker, mInterval);
                }
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        application = (KeepApplication) getApplication();
        if (powerManager == null) {
            try {
                powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            } catch (Exception e) {
                powerManager = null;
            }
        }
        if (application.keepLive0 && (isScreenOn() || !application.combine)) {
            AudioKeeper.getInstance().start();
        }
        start();
        if (application.keepLive1 && powerManager != null && !powerManager.isScreenOn() && !(Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive())) {
            KeepLiveActivity.startDaemonActivity(KeepLiveService.this);
        }
    }

    private boolean isScreenOn() {
        if (powerManager != null) {
            if (powerManager.isScreenOn() || (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive())) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onDestroy() {
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
            }
        }
        releaseLock();
        if (application.keepLive0 && (isScreenOn() || !application.combine)) {
            AudioKeeper.getInstance().stop();
        }
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }

    private void releaseLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void acquireLock() {
        if (powerManager != null && wakeLock == null) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "power:lock");
        }
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    private void start() {
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && intent.getAction() != null) {
                        if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
                            if (application.keepLive1) {
                                KeepLiveActivity.startDaemonActivity(KeepLiveService.this);
                                if (Build.BRAND.equals("OPPO")) {
                                    releaseLock();
                                    startRepeatingTask();
                                }
                            }
                            if (application.keepLive0 && application.combine) {
                                AudioKeeper.getInstance().stop();
                            }
                        } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
                            if (application.keepLive1 && Build.BRAND.equals("OPPO")) {
                                stopRepeatingTask();
                                if (powerManager != null) {
                                    acquireLock();
                                    //Log.e("ray","SCREEN_ON received");
                                    //KeepLiveActivity.startDaemonActivity(KeepLiveService.this);
                                    //KeepLiveService.this.startActivity(new Intent(KeepLiveService.this,MainActivity.class));
                                }
                            }
                            if (application.keepLive0 && application.combine) {
                                AudioKeeper.getInstance().start();
                            }
                        } else if (intent.getAction().equals("com.inno.foreground")) {
                            if (application.keepLive0) {
                                AudioKeeper.getInstance().stop();
                            }
                            if (application.keepLive1 && Build.BRAND.equals("OPPO")) {
                                releaseLock();
                            }
                        } else if (intent.getAction().equals("com.inno.background")) {
                            if (application.keepLive0 && (!application.combine || !application.keepLive1 || isScreenOn())) {
                                AudioKeeper.getInstance().start();
                            }
                            if (!application.keepLive0 && application.keepLive1 && Build.BRAND.equals("OPPO")) {
                                acquireLock();
                            }
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            if (application.keepLive1 || (application.keepLive0 && application.combine)) {
                intentFilter.addAction("android.intent.action.SCREEN_OFF");
            }
            if (application.keepLive0 || (application.keepLive1 && Build.BRAND.equals("OPPO"))) {
                intentFilter.addAction("com.inno.foreground");
                intentFilter.addAction("com.inno.background");
            }
            if ((application.keepLive1 && Build.BRAND.equals("OPPO") || (application.keepLive0 && application.combine))) {
                intentFilter.addAction("android.intent.action.SCREEN_ON");
            }
            try {
                registerReceiver(receiver, intentFilter);
            } catch (Throwable th) {
                Log.e("ray", th + "");
            }
        }
    }
}
