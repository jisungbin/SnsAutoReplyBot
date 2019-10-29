package com.sungbin.reply.bot.api;

import android.content.Context;
import android.content.SharedPreferences;


public class AppData{
    private static Context ctx = null;

    public static void init(Context context){
        ctx = context;
    }

    public static int getInt(String name, int _null){
        SharedPreferences sf = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        return sf.getInt(name, _null);
    }

    public static Boolean getBoolean(String name, boolean _null){
        SharedPreferences sf = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        return sf.getBoolean(name, _null);
    }

    public static String getString(String name, String _null){
        SharedPreferences sf = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        return sf.getString(name, _null);
    }

    public static void putString(String name, String data){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(name, data);
        editor.apply();
    }

    public static void putInt(String name, int data){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(name, data);
        editor.apply();
    }

    public static void putBoolean(String name, Boolean data){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(name, data);
        editor.apply();
    }

    public static void remove(String name){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(name);
        editor.apply();
    }

    public static void clear(){
        SharedPreferences sharedPreferences = ctx.getSharedPreferences("AppData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
