package com.innotech.innotechpush.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.innotech.innotechpush.InnotechPushManager;
import com.innotech.innotechpush.R;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.receiver.NotificationClickReceiver;
import com.innotech.innotechpush.sdk.ImageLoadCallback;
import com.innotech.innotechpush.sdk.ImageLoadTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;

public class NotificationUtils {

    public static final String notification_channel_id = "channel_innotech";
    public static final String notification_channel_name = "系统通知";

    /**
     * 根据通知信息显示不同样式的通知
     * unfold: 为空禁用
     * type:1 文本 content内容为文本
     * type:2 大图 content内容为图片url
     *
     * @param context
     * @param message：通知信息
     */
    public static void sendNotificationByStyle(final Context context, final InnotechMessage message) throws JSONException {
        if (message.getStyle() != 2) {//默认展示
            if (!TextUtils.isEmpty(message.getUnfold())) {//有长文字或者大图显示
                JSONObject unfoldJson = new JSONObject(message.getUnfold());
                int type = unfoldJson.getInt("type");
                String content = unfoldJson.getString("content");
                if (type == 1) {//有长文字
                    showNotification(context, message, content);
                } else if (type == 2) {//有大图显示
                    new ImageLoadTask(new ImageLoadCallback() {
                        @Override
                        public void onResult(Bitmap bitmap) {
                            showNotification(context, message, bitmap);
                        }
                    }).execute(content);
                } else {
                    showNotification(context, message);
                }
            } else {//普通通知
                showNotification(context, message);
            }
        } else {//纯图
            new ImageLoadTask(new ImageLoadCallback() {
                @Override
                public void onResult(Bitmap bitmap) {
                    showOnlyImageNotification(context, message, bitmap);
                }
            }).execute(message.getContent());
        }
    }

    /**
     * 普通通知
     *
     * @param context
     * @param msg
     */
    public static void showNotification(Context context, InnotechMessage msg) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NotificationUtils.notification_channel_id, NotificationUtils.notification_channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder mBuilder = new Notification.Builder(context, NotificationUtils.notification_channel_id);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        }
    }

    /**
     * 长文本通知
     *
     * @param context
     * @param msg
     * @param bigText
     */
    public static void showNotification(Context context, InnotechMessage msg, String bigText) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NotificationUtils.notification_channel_id, NotificationUtils.notification_channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder mBuilder = new Notification.Builder(context, NotificationUtils.notification_channel_id);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon))
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setStyle(new Notification.BigTextStyle()
                            .setBigContentTitle(msg.getTitle())
                            .setSummaryText(msg.getContent())
                            .bigText(bigText));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon))
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .setBigContentTitle(msg.getTitle())
                            .setSummaryText(msg.getContent())
                            .bigText(bigText));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        }
    }

    /**
     * 含大图通知
     *
     * @param context
     * @param msg
     * @param bitmap
     */
    public static void showNotification(Context context, InnotechMessage msg, Bitmap bitmap) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NotificationUtils.notification_channel_id, NotificationUtils.notification_channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder mBuilder = new Notification.Builder(context, NotificationUtils.notification_channel_id);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon))
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setStyle(new Notification.BigPictureStyle()
                            .setBigContentTitle(msg.getTitle())
                            .setSummaryText(msg.getContent())
                            .bigPicture(bitmap));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setTicker(msg.getTitle()) //通知首次出现在通知栏，带上升动画效果的
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources()
                            , InnotechPushManager.pushIcon))
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .setBigContentTitle(msg.getTitle())
                            .setSummaryText(msg.getContent())
                            .bigPicture(bitmap));
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        }
    }

    /**
     * 纯图通知
     *
     * @param context
     * @param msg
     * @param bitmap
     */
    public static void showOnlyImageNotification(Context context, InnotechMessage msg, Bitmap bitmap) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.inno_notification);
        remoteViews.setImageViewResource(R.id.iv_img, R.mipmap.ic_launcher);
        remoteViews.setImageViewBitmap(R.id.iv_img, bitmap);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(NotificationUtils.notification_channel_id, NotificationUtils.notification_channel_name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
            Notification.Builder mBuilder = new Notification.Builder(context, NotificationUtils.notification_channel_id);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setContent(remoteViews);
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        } else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(msg.getTitle())//设置通知栏标题
                    .setContentText(msg.getContent())
                    .setAutoCancel(true)
                    .setWhen(System.currentTimeMillis())//通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
                    .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setSmallIcon(InnotechPushManager.pushIcon)
                    .setContent(remoteViews);
            mBuilder.setContentIntent(contentIntent); //设置通知栏点击意图
            notificationManager.notify(id, mBuilder.build());
        }
    }

    public static void sendCustomNotification(Context context, InnotechMessage msg) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        RemoteViews remoteViews;
        if (isDarkNotificationTheme(context)) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.inno_notification);
        } else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.inno_notification);
        }
//        builder.setCustomBigContentView(remoteViews);
//        builder.setContent(remoteViews);
        Notification noti = builder.build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            noti.bigContentView = remoteViews;
        }
        Intent clickIntent = new Intent(context, NotificationClickReceiver.class); //点击通知之后要发送的广播
        clickIntent.putExtra("InnotechMessage", msg);
        int id = (int) ((Math.random() * 9 + 1) * 1000);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context.getApplicationContext(), id, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent); //设置通知栏点击意图
        notificationManager.notify(id, noti);
    }

    public static boolean isDarkNotificationTheme(Context context) {
        return !isSimilarColor(Color.BLACK, getNotificationColor(context));
    }

    /**
     * 获取通知栏颜色
     *
     * @param context
     * @return
     */
    public static int getNotificationColor(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        Notification notification = builder.build();
        int layoutId = notification.contentView.getLayoutId();
        ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null, false);
        if (viewGroup.findViewById(android.R.id.title) != null) {
            return ((TextView) viewGroup.findViewById(android.R.id.title)).getCurrentTextColor();
        }
        return findColor(viewGroup);
    }

    private static int findColor(ViewGroup viewGroupSource) {
        int color = Color.TRANSPARENT;
        LinkedList<ViewGroup> viewGroups = new LinkedList<>();
        viewGroups.add(viewGroupSource);
        while (viewGroups.size() > 0) {
            ViewGroup viewGroup1 = viewGroups.getFirst();
            for (int i = 0; i < viewGroup1.getChildCount(); i++) {
                if (viewGroup1.getChildAt(i) instanceof ViewGroup) {
                    viewGroups.add((ViewGroup) viewGroup1.getChildAt(i));
                } else if (viewGroup1.getChildAt(i) instanceof TextView) {
                    if (((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor() != -1) {
                        color = ((TextView) viewGroup1.getChildAt(i)).getCurrentTextColor();
                    }
                }
            }
            viewGroups.remove(viewGroup1);
        }
        return color;
    }

    private static boolean isSimilarColor(int baseColor, int color) {
        int simpleBaseColor = baseColor | 0xff000000;
        int simpleColor = color | 0xff000000;
        int baseRed = Color.red(simpleBaseColor) - Color.red(simpleColor);
        int baseGreen = Color.green(simpleBaseColor) - Color.green(simpleColor);
        int baseBlue = Color.blue(simpleBaseColor) - Color.blue(simpleColor);
        double value = Math.sqrt(baseRed * baseRed + baseGreen * baseGreen + baseBlue * baseBlue);
        if (value < 180.0) {
            return true;
        }
        return false;
    }

}
