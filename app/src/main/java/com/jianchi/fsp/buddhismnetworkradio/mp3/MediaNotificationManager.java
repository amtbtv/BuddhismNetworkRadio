package com.jianchi.fsp.buddhismnetworkradio.mp3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.jianchi.fsp.buddhismnetworkradio.R;
import com.jianchi.fsp.buddhismnetworkradio.activity.StartActivity;

//import static com.jianchi.fsp.buddhismnetworkradio.mp3.Constant.StartWith_MP3_SERVICE;

public class MediaNotificationManager {
    Context context;
    NotificationManager notificationManager;
    PendingIntent contentIntent;

    /**
     * 通知ID
     */
    public static final int NOTI_CTRL_ID = 25478;
    public static final String NotificationChanneId = "BMP3PLAYERSERVICECHANNEL";

    public MediaNotificationManager(Context context){
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(context, StartActivity.class);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setAction(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        //intent.putExtra("StartWith", "StartWith_MP3_SERVICE");
        contentIntent = PendingIntent.getActivity(context, NOTI_CTRL_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void startNotification(String name, String msg) {

        Notification notification = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(NotificationChanneId, name, NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(mChannel);
            notification = new Notification.Builder(context)
                    .setChannelId(NotificationChanneId)
                    .setSmallIcon(R.mipmap.ic_launcher)//设置小图标
                    .setContentTitle(name)
                    .setContentText(msg)
                    .setContentIntent(contentIntent)
                    .build();
        } else {
            notification = new Notification.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)//设置小图标
                    .setContentTitle(name)
                    .setContentText(msg)
                    .setContentIntent(contentIntent)
                    .build();
        }
        notification.flags = notification.FLAG_NO_CLEAR;
        notificationManager.notify(NOTI_CTRL_ID, notification);
    }

    public void stopNotification() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTI_CTRL_ID);
    }
}
