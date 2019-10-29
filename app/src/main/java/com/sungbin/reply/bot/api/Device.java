package com.sungbin.reply.bot.api;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
public class Device{

    private static Context ctx;

    public static void init(Context context){
        ctx = context;
    }

    public static String getPhoneModel(){
        return Build.MODEL;
    }

    public static int getAndroidSDKVersion(){
        return Build.VERSION.SDK_INT;
    }

    public static String getAndroidVersion(){
        return Build.VERSION.RELEASE;
    }

    public static int getBattey(){
        Intent intentBattery = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = intentBattery.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intentBattery.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;

        return (int)(batteryPct * 100);
    }

    public static boolean getIsCharging(){
        Intent intentBattery = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int status = intentBattery.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

        boolean isCharging = false;
        if(status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL){
            isCharging = true;
        }

        return isCharging;
    }

}
