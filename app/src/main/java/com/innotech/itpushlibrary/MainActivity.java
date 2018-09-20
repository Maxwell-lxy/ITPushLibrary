package com.innotech.itpushlibrary;

import android.app.Activity;
import android.os.Bundle;

import com.innotech.innotechpush.InnotechPushMethod;

public class MainActivity extends Activity {
    private static final int REQUEST_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        InnotechPushMethod.launcher(this);

//        PackageManager pkgManager = getPackageManager();
//
//        // 读写 sd card 权限非常重要, android6.0默认禁止的, 建议初始化之前就弹窗让用户赋予该权限
//        boolean sdCardWritePermission =
//                pkgManager.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
//
//        // read phone state用于获取 imei 设备信息
//        boolean phoneSatePermission =
//                pkgManager.checkPermission(Manifest.permission.READ_PHONE_STATE, getPackageName()) == PackageManager.PERMISSION_GRANTED;
//
//        if (Build.VERSION.SDK_INT >= 23 && !sdCardWritePermission || !phoneSatePermission) {
//            requestPermission();
//        } else {
//            com.igexin.sdk.PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
//        }
//
//        // 注册 intentService 后 PushDemoReceiver 无效, sdk 会使用 DemoIntentService 传递数据,
//        // AndroidManifest 对应保留一个即可(如果注册 DemoIntentService, 可以去掉 PushDemoReceiver, 如果注册了
//        // IntentService, 必须在 AndroidManifest 中声明)
//        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), DemoIntentService.class);
//
//    }
//
//    private void requestPermission() {
//        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
//                REQUEST_PERMISSION);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if (requestCode == REQUEST_PERMISSION) {
//            if ((grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
//                PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
//            } else {
//                PushManager.getInstance().initialize(this.getApplicationContext(), userPushService);
//            }
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
    }
}
