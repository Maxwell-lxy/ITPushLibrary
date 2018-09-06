package com.innotech.innotechpush.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.innotech.innotechpush.R;
import com.innotech.innotechpush.bean.InnotechMessage;
import com.innotech.innotechpush.receiver.NotificationClickReceiver;

import java.util.LinkedList;

public class NotificationUtils {

    public static void sendCustomNotification(Context context, InnotechMessage msg) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        RemoteViews remoteViews;
        if (isDarkNotificationTheme(context)) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.dark_notification);
        } else {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.dark_notification);
        }
//        builder.setCustomBigContentView(remoteViews);
//        builder.setContent(remoteViews);
        Notification noti = builder.build();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
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
