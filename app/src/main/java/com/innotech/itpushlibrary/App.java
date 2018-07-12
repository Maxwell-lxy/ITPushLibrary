package com.innotech.itpushlibrary;

import android.app.Application;

import com.innotech.innotechpush.InnotechPushManager;

/**
 * Created by admin on 2018/4/11.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        InnotechPushManager.getInstance().initPushSDK(this);
        InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
    }
}
