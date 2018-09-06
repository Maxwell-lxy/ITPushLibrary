package com.innotech.innotechpush.sdk;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

public class KeepLiveActivity extends Activity {
    private static volatile boolean started;
    private BroadcastReceiver receiver;
    private boolean created;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    if (intent != null) {
                        String action = intent.getAction();
                        if (action != null && action.equals("android.intent.action.USER_PRESENT")) {
                            KeepLiveActivity.this.finish();
                        }
                    }
                }
            };
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.USER_PRESENT");
            try {
                registerReceiver(receiver, intentFilter);
                created = true;
            } catch (Throwable ignored) {
            }
        }
    }

    protected void onResume() {
        super.onResume();
        if (created) {
            PowerManager powerManager;
            try {
                powerManager = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
            } catch (Exception e) {
                powerManager = null;
            }
            if (powerManager != null) {
                if(powerManager.isScreenOn() || (Build.VERSION.SDK_INT >= 20 && powerManager.isInteractive())){
                    finish();
                }
            }
            return;
        }
        finish();
    }

    protected void onDestroy() {
        started = false;
        created = false;
        if (receiver != null) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception e) {
            }
        }
        super.onDestroy();
    }

    public static void startDaemonActivity(Context context)  {
        if (context != null && !started) {
            if (Build.BRAND.equals("OPPO")) {
                AudioKeeper.getInstance().playOnce();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
            try {
                context.startActivity(getIntent(context));
            } catch (Throwable th) {
                Log.e("ray",th+"");
            }
        }
    }

    private static Intent getIntent(Context context) {
        Intent intent = new Intent(context, KeepLiveActivity.class);
        intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        return intent;
    }
}
