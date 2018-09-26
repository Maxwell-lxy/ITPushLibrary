package com.innotech.itpushlibrary;

import com.inno.innosdk.pb.InnoMain;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.sdk.KeepApplication;

import java.util.Random;

/**
 * Created by admin on 2018/4/11.
 */

public class App extends KeepApplication {

    int s = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        s = random.nextInt(max) % (max - min + 1) + min;
//        startInnoSdk("test", "union", "" + s);
        new Thread(new Runnable() {
            @Override
            public void run() {
                InnotechPushManager.getInstance().initPushSDK(App.this);
                InnotechPushManager.pushIcon = R.mipmap.ic_launcher;
                InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
            }
        }).start();
    }

    private void startInnoSdk(String cid, String ch, String member_id) {
        InnoMain.setValueMap("ch", ch);//这里填入你们的渠道号
        InnoMain.setValueMap("member_id", member_id);//这里填入你们的用户id
        InnoMain.startInno(this, cid, new InnoMain.CallBack() {//cid为注册的业务方id
            @Override
            public void getOpenid(String openid, int isnew, String remark) {
                InnotechPushManager.getInstance().initPushSDK(App.this);
                InnotechPushManager.pushIcon = R.mipmap.ic_launcher;
                InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        InnotechPushManager.getInstance().terminate();
    }
}
