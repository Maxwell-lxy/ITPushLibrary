package com.innotech.itpushlibrary;

import com.inno.innosdk.pb.InnoMain;
import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.sdk.KeepApplication;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.patch.annotaion.Modify;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.Random;

/**
 * Created by admin on 2018/4/11.
 */

public class App extends KeepApplication {

    int s = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        if(PermissionUtils.isGrantSDCardReadPermission(this)){
            new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new RobustCallBackSample()).start();
        }
        int max = 10000;
        int min = 1000;
        Random random = new Random();
        s = random.nextInt(max) % (max - min + 1) + min;
        startInnoSdk("test", "union", "" + s);
        CrashReport.initCrashReport(getApplicationContext(), "450dd7d0c6", false);
    }

    private void startInnoSdk(String cid, String ch, String member_id) {
//        InnoMain.setValueMap("ch", ch);//这里填入你们的渠道号
//        InnoMain.setValueMap("member_id", member_id);//这里填入你们的用户id
//        InnoMain.startInno(this, cid, new InnoMain.CallBack() {//cid为注册的业务方id
//            @Override
//            public void getOpenid(String openid, int isnew, String remark) {
        InnotechPushManager.getInstance().initPushSDK(App.this);
        InnotechPushManager.pushIcon = R.mipmap.ic_launcher;
        InnotechPushManager.getInstance().setPushRevicer(new TestPushReciver());
//            }
//        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        InnotechPushManager.getInstance().terminate();
    }
}
