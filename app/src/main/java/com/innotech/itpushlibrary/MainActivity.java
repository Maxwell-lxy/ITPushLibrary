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

//        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        Intent clickIntent = new Intent(this, NotificationClickReceiver.class); //点击通知之后要发送的广播
//        int id = (int) ((Math.random() * 9 + 1) * 1000);
//        PendingIntent contentIntent = PendingIntent.getBroadcast(this, id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"channel_1");
//        mBuilder.setContentTitle("fdasfdsa")//设置通知栏标题
//                .setContentText("fdasfdsa")
//                .setAutoCancel(true)
//                .setTicker("fdasfdsa") //通知首次出现在通知栏，带上升动画效果的
//                .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
//                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//                .setDefaults(Notification.DEFAULT_VIBRATE)
//                .setSmallIcon(InnotechPushManager.pushIcon)
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources()
//                        , InnotechPushManager.pushIcon));
//        mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
//        notificationManager.notify(id, mBuilder.build());

//        if (Build.VERSION.SDK_INT >= 26) {
//            NotificationChannel channel = new NotificationChannel(NotificationUtils.notification_channel_id, NotificationUtils.notification_channel_name, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//            Notification.Builder mBuilder = new Notification.Builder(this, NotificationUtils.notification_channel_id);
//            mBuilder.setContentTitle("tesfs")//设置通知栏标题
//                    .setContentText("tesfds")
//                    .setAutoCancel(true)
//                    .setTicker("tesfs") //通知首次出现在通知栏，带上升动画效果的
//                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
//                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
//                    .setDefaults(Notification.DEFAULT_VIBRATE)
//                    .setSmallIcon(InnotechPushManager.pushIcon)
//                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources()
//                            , InnotechPushManager.pushIcon));
//            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
//            notificationManager.notify(id, mBuilder.build());
//        }
    }
}
