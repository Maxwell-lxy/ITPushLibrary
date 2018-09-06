package com.innotech.innotechpush.sdk;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ActivityHelper {
    private static List<Activity> ResumeActivities = Collections.synchronizedList(new LinkedList<Activity>());

    private static boolean hasResumeActivity() {
        if ((ResumeActivities == null || ResumeActivities.size() <= 0)) {
            return false;
        }
        return true;
    }

    private static void post(Runnable runnable) {
        if (runnable != null) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                runnable.run();
            } else {
                new Handler(Looper.getMainLooper()).post(runnable);
            }
        }
    }

    public static void registerActivityCallback(final KeepApplication application) {
        post(new Runnable() {
                 @Override
                 public void run() {
                     if (Build.VERSION.SDK_INT >= 14) {
                         application.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                             @Override
                             public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                             }

                             @Override
                             public void onActivityStarted(Activity activity) {
                                 if (ResumeActivities != null) {
                                     ResumeActivities.add(activity);
                                 }
                                 if (ResumeActivities != null && ResumeActivities.size() == 1) {
                                     application.getApplicationContext().sendBroadcast(new Intent("com.inno.foreground"));
                                 }
                             }

                             @Override
                             public void onActivityResumed(Activity activity) {

                             }

                             @Override
                             public void onActivityPaused(Activity activity) {

                             }

                             @Override
                             public void onActivityStopped(Activity activity) {
                                 if (ResumeActivities != null) {
                                     ResumeActivities.remove(activity);
                                 }
                                 if (!hasResumeActivity()) {
                                     application.getApplicationContext().sendBroadcast(new Intent("com.inno.background"));
                                 }
                             }

                             @Override
                             public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                             }

                             @Override
                             public void onActivityDestroyed(Activity activity) {

                             }
                         });
                     }
                 }
             }
        );
    }
}
