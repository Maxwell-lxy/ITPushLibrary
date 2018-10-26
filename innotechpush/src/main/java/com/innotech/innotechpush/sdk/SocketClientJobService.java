package com.innotech.innotechpush.sdk;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SocketClientJobService extends JobService {
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("allen", "Service onStartJob");
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("allen", "Service onStopJob");
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("allen", "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("allen", "Service destroyed");
    }
}
