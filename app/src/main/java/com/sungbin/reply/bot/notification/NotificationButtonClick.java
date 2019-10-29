package com.sungbin.reply.bot.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.utils.Utils;

public class NotificationButtonClick extends BroadcastReceiver{
    @Override
    public void onReceive(Context ctx, Intent intent) {
        String data = intent.getStringExtra("BotOff");
        if(data.equals("BotOff")){
            Utils.saveData(ctx, "OnOff", "false");
            FancyToast.makeText(ctx, ctx.getString(R.string.bot_off), FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
            NotificationManager.deleteNotification(ctx, 1);
        }
    }
}