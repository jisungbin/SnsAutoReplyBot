package com.sungbin.reply.bot.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.annotation.StringDef;
import androidx.core.app.NotificationManagerCompat;

import com.sungbin.reply.bot.view.activty.MainActivity;
import com.sungbin.reply.bot.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class NotificationManager {

    /**
     * Created by SungBin on 2018. 01. 07.
     */

    private static String GROUP_NAME = "undefined";

    public static void setGroupName(String name){
        GROUP_NAME = name;
    }

    public static void createChannel(Context context, String name, String description) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannelGroup group1 = new NotificationChannelGroup(GROUP_NAME, GROUP_NAME);
            getManager(context).createNotificationChannelGroup(group1);

            NotificationChannel channelMessage = new NotificationChannel(Channel.NAME, name, android.app.NotificationManager.IMPORTANCE_DEFAULT);
            channelMessage.setDescription(description);
            channelMessage.setGroup(GROUP_NAME);
            channelMessage.setLightColor(Color.parseColor("#42a5f5"));
            channelMessage.enableVibration(true);
            channelMessage.setVibrationPattern(new long[]{0, 0});
            getManager(context).createNotificationChannel(channelMessage);
        }
    }

    private static android.app.NotificationManager getManager(Context context) {
        return (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void showNormalNotification(Context context, int id, String title, String content) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            Notification.Builder builder = new Notification.Builder(context, Channel.NAME)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(getSmallIcon())
                    .setAutoCancel(true)
                    .setOngoing(true);

            Intent intent = new Intent(context, NotificationButtonClick.class);
            intent.putExtra("BotOff","BotOff");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(getSmallIcon(), context.getString(R.string.bot_off_title), pi);

            Intent i = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            getManager(context).notify(id, builder.build());
        }
        else{
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(getSmallIcon())
                    .setAutoCancel(true)
                    .setOngoing(true);

            Intent intent = new Intent(context, NotificationButtonClick.class);
            intent.putExtra("BotOff","BotOff");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(getSmallIcon(), context.getString(R.string.bot_off_title), pi);

            Intent i = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            getManager(context).notify(id, builder.build());
        }
    }

    public static void showInboxStyleNotification(Context context, int id, String title, String content, String[] boxText) {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            Notification.Builder builder = new Notification.Builder(context, Channel.NAME)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(getSmallIcon())
                    .setAutoCancel(true)
                    .setOngoing(true);
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBigContentTitle(title);
            inboxStyle.setSummaryText(content);

            for(String str : boxText) {
                inboxStyle.addLine(str);
            }

            Intent intent = new Intent(context, NotificationButtonClick.class);
            intent.putExtra("BotOff","BotOff");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(getSmallIcon(), context.getString(R.string.bot_off_title), pi);
            builder.setStyle(inboxStyle);

            Intent i = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            getManager(context).notify(id, builder.build());
        }
        else{
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setSmallIcon(getSmallIcon())
                    .setAutoCancel(true)
                    .setOngoing(true);
            Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
            inboxStyle.setBigContentTitle(title);
            inboxStyle.setSummaryText(content);

            for (String str : boxText) {
                inboxStyle.addLine(str);
            }

            Intent intent = new Intent(context, NotificationButtonClick.class);
            intent.putExtra("BotOff","BotOff");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(getSmallIcon(), context.getString(R.string.bot_off_title), pi);
            builder.setStyle(inboxStyle);

            Intent i = new Intent(context, MainActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(contentIntent);

            getManager(context).notify(id, builder.build());
        }
    }

    public static void deleteNotification(Context context, int id){
        NotificationManagerCompat.from(context).cancel(id);
    }

    private static int getSmallIcon() {
        return R.drawable.icon;
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            Channel.NAME
    })
    public @interface Channel {
        String NAME = "CHANNEL";
    }

}