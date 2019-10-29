package com.sungbin.reply.bot.utils;

/**
 * Created by SungBin on 2018-10-21.
 */

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.browser.customtabs.CustomTabsIntent;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.view.activty.MainActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;


public class Utils extends Application{

    public static final String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static Context ctx;
    private static Context ctx2;

    @Override
    public void onCreate(){
        super.onCreate();
        ctx = getApplicationContext();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
    }

    public static void createFolder(String name){
        new File(sdcard+"/New kakaotalk Bot 2/"+name+"/").mkdirs();
    }

    public static String read(String name, String _null){
        try{
            File file = new File(sdcard+"/New kakaotalk Bot 2/"+name);
            if(!file.exists()) return _null;
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            String str = br.readLine();
            String line = "";
            while((line = br.readLine())!=null){
                str += "\n"+line;
            }
            fis.close();
            isr.close();
            br.close();
            return str+"";
        }
        catch(Exception e){ }
        return _null;
    }

    public static void save(String name, String str){
        try{
            File file = new File(sdcard+"/New kakaotalk Bot 2/"+name);
            FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(str.getBytes());
            fos.close();
        }
        catch(Exception e){
        }
    }

    public static void delete(String name){
        File file = new File(sdcard+"/New kakaotalk Bot 2/"+name);
        file.delete();
    }

    public static void setContext(Context ctx3){
        ctx2 = ctx3;
    }

    public static Context getContext(){
        return ctx2;
    }

    public static String getHtml(String link){
        try{
            URLConnection con = new URL(link).openConnection();
            if(con!=null){
                con.setConnectTimeout(5000);
                con.setUseCaches(false);
                InputStreamReader isr = new InputStreamReader(con.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String str = br.readLine();
                String str2 = "";
                while(true){
                    str2 = br.readLine();
                    if(str2!=null){
                        str = str+"\n"+str2;
                    }
                    else{
                        br.close();
                        isr.close();
                        return str;
                    }
                }
            }
        }
        catch(Exception ignored){ }
        return null;
    }

    public static void toast(Activity ctx2, String msg){
        ctx2.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                Toast.makeText(ctx2, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void toast(Context ctx2, String msg){
        if (Looper.myLooper() == null) Looper.prepare();
        Toast.makeText(ctx2, msg, Toast.LENGTH_SHORT).show();
        if (Looper.myLooper() == null) Looper.loop();
    }

    public static void Ltoast(Context ctx2, String msg){
        if (Looper.myLooper() == null) Looper.prepare();
        Toast.makeText(ctx2, msg, Toast.LENGTH_LONG).show();
        if (Looper.myLooper() == null) Looper.loop();
    }

    public static int Number(String num){
        return Integer.parseInt(num);
    }

    public static boolean toBoolean(String tf){
        return tf.equals("true");
    }

    public static int dip2px(Context ctx, int dips){
        return (int) Math.ceil(dips*ctx.getResources().getDisplayMetrics().density);
    }

    public static String readData(Context ctx, String name, String _null){
        if(toBoolean(read("AppData/FixScriptCantOn", "false"))){
            return read("AppData/"+name, _null);
        }
        else{
            SharedPreferences pref = ctx.getSharedPreferences("pref", MODE_PRIVATE);
            return pref.getString(name, _null);
        }
    }

    public static void saveData(Context ctx, String name, String value){
        if(toBoolean(read("AppData/FixScriptCantOn", "false"))){
            save("AppData/"+name, value);
        }
        else{
            SharedPreferences pref = ctx.getSharedPreferences("pref", MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();

            editor.putString(name, value);
            editor.commit();
        }
    }

    public static void showWebTab(String link, Activity activity){
        try {
            PackageManager pm = activity.getPackageManager();
            PackageInfo pi = pm.getPackageInfo("com.android.chrome", PackageManager.GET_META_DATA);
            ApplicationInfo appInfo = pi.applicationInfo;

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
            builder.setToolbarColor(activity.getColor(R.color.colorAccent));
            CustomTabsIntent customTabsIntent = builder.build();
            customTabsIntent.intent.setPackage("com.android.chrome");
            customTabsIntent.launchUrl(activity, Uri.parse(link));
        }
        catch (PackageManager.NameNotFoundException e){
            try{
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(link);
                intent.setData(uri);
                activity.startActivity(intent);
            }
            catch(Exception error){
                Utils.toast(activity, "호출 실패!\n\n"+error.getMessage());
            }
        }
    }

    public static void clearData(Context ctx){
        SharedPreferences pref = ctx.getSharedPreferences("pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static void copy(Context ctx, String text){
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(ctx, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show();
    }

    public static void copy(Context ctx, String text, boolean showToast){
        ClipboardManager clipboard = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", text);
        clipboard.setPrimaryClip(clip);
        if(showToast) Toast.makeText(ctx, "클립보드에 복사되었습니다.", Toast.LENGTH_LONG).show();
    }

    public static void error(Context ctx, Exception e, String name){
        String data = "Error: "+e+"\nLineNumber: "+e.getStackTrace()[0].getLineNumber()+"\nAt: "+name;
        Utils.toast(ctx, data);
        Utils.copy(ctx, data);
        Log.e("Error", data);
    }

    public static void error(Context ctx, Exception e){
        String data = "Error: "+e+"\nLineNumber: "+e.getStackTrace()[0].getLineNumber();
        Utils.toast(ctx, data);
        Utils.copy(ctx, data);
        Log.e("Error", data);
    }

    public static void restart(Context context){
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis()+100, mPendingIntent);
        System.exit(0);
    }

    public static void toast(Context ctx, String txt, int length, int type){
        if (Looper.myLooper() == null) Looper.prepare();
        FancyToast.makeText(ctx, txt, length, type, false).show();
        if (Looper.myLooper() == null) Looper.loop();
    }

    public static void toast(Activity ctx, String txt, int length, int type){
        ctx.runOnUiThread(new Runnable(){
            @Override
            public void run(){
                FancyToast.makeText(ctx, txt, length, type, false).show();
            }
        });
    }

    public static boolean isInstall(Context ctx, String pakageName){
        Intent intent = ctx.getPackageManager().getLaunchIntentForPackage(pakageName);
        return intent!=null;
    }
}