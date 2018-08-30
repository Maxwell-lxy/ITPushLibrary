package com.innotech.itpushlibrary;

import android.app.Application;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.utils.SPIcon;

/**
 * Created by admin on 2018/4/11.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        InnotechPushManager.getInstance().initPushSDK(this,"thisisopenid");
        InnotechPushManager.pushIcon = R.mipmap.ic_shortcut_account_box;
        InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
    }
}
