package com.sungbin.reply.bot.api;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;

import com.sungbin.reply.bot.notification.NotificationManager;
import com.sungbin.reply.bot.R;

public class Utils{

    private static Context ctx;
    private static Vibrator vibrator;

    public static void init(Context context){
        ctx = context;
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static void makeToast(String content){
        Toast.makeText(ctx, content, Toast.LENGTH_SHORT).show();
    }

    public static void makeNoti(String title, String content){
        NotificationManager.setGroupName("Utils.makeNoti");
        NotificationManager.createChannel(ctx, "User Made Notification", "User Made Notification from Utils API.");
        NotificationManager.showNormalNotification(ctx, 1, title, content);
    }

    public static void makeVibration(int time){
        vibrator.vibrate(time * 1000);
    }

    public static void copy(String content){
        ClipboardManager clipboardManager = (ClipboardManager) ctx.getSystemService(ctx.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", content);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(ctx, R.string.copy_success, Toast.LENGTH_SHORT).show();
    }

}