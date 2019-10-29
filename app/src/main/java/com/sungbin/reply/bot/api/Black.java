package com.sungbin.reply.bot.api;

import android.annotation.SuppressLint;
import android.content.Context;

import com.sungbin.reply.bot.utils.Utils;

public class Black{
    @SuppressLint("StaticFieldLeak")
    private static Context ctx;

    public static void init(Context context){
        ctx = context;
    }

    public static void addSender(String sender){
        String preSenderList = Utils.readData(ctx, "SenderBlackList", "");
        String newSenderList = preSenderList + "\n" + preSenderList;
        Utils.saveData(ctx, "SenderBlackList", newSenderList);
    }

    public static void removeSender(String sender){
        String preSenderList = Utils.readData(ctx, "SenderBlackList", "");
        String newSenderList = preSenderList.replace("\n" + preSenderList, "");
        Utils.saveData(ctx, "SenderBlackList", newSenderList);
    }

    public static String readSender(){
        return Utils.readData(ctx, "SenderBlackList", "");
    }

    public static void addRoom(String sender){
        String preSenderList = Utils.readData(ctx, "RoomBlackList", "");
        String newSenderList = preSenderList + "\n" + preSenderList;
        Utils.saveData(ctx, "RoomBlackList", newSenderList);
    }

    public static void removeRoom(String sender){
        String preSenderList = Utils.readData(ctx, "RoomBlackList", "");
        String newSenderList = preSenderList.replace("\n" + preSenderList, "");
        Utils.saveData(ctx, "RoomBlackList", newSenderList);
    }

    public static String readRoom(){
        return Utils.readData(ctx, "RoomBlackList", "");
    }
}
