package com.innotech.itpushlibrary;

import android.app.Application;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.utils.SPIcon;

import java.util.Random;

/**
 * Created by admin on 2018/4/11.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        InnotechPushManager.getInstance().initPushSDK(this, "" + s);
        InnotechPushManager.pushIcon = R.mipmap.ic_shortcut_account_box;
        InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
    }
}
