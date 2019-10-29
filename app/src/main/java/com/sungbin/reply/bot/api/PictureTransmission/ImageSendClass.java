package com.sungbin.reply.bot.api.PictureTransmission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

import com.sungbin.reply.bot.utils.Utils;

import java.io.File;

public class ImageSendClass{

    private static Context ctx;

    public ImageSendClass(Context ctx){
        this.ctx = ctx;
    }

    public static void getXY(){
        StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder().build());
        File dir = new File(Utils.sdcard);
        Uri imageUri = Uri.fromFile(dir);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("image/*");
        intent.setPackage("com.messenger.talk");
        intent.putExtra(Intent.EXTRA_STREAM, imageUri);
        ctx.startActivity(intent);
    }

    public static void sendImage(final int[] xy0, final int[] xy, final int[] xy2, final int[] xy3, final String imagePath) {
        try {
            StrictMode.setVmPolicy(new android.os.StrictMode.VmPolicy.Builder().build());
            File dir = new File(Utils.sdcard + "/" + imagePath);
            Uri imageUri = Uri.fromFile(dir);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setType("image/*");
            intent.setPackage("com.messenger.talk");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            ctx.startActivity(intent);
            Thread.sleep(2000);
            AccessibilityServiceManager manager = AccessibilityServiceManager.getInstance();
            manager.dispatch(xy0[0], xy0[1]);
            Thread.sleep(1000);
            manager.dispatch(xy[0], xy[1]);
            Thread.sleep(1000);
            manager.dispatch(xy2[0], xy2[1]);
            Thread.sleep(1000);
            manager.dispatch(xy3[0], xy3[1]);

            /*System.exit(0);

            Intent intent2 = new Intent(Intent.ACTION_MAIN); //태스크의 첫 액티비티로 시작
            intent2.addCategory(Intent.CATEGORY_HOME);   //홈화면 표시
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            ctx.startActivity(intent2);*/
        }
        catch(Exception e){
            Utils.error(ctx, e);
        }
    }

}
