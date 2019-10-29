package com.sungbin.reply.bot.api;

import android.app.Application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import static com.sungbin.reply.bot.utils.Utils.save;
import static com.sungbin.reply.bot.utils.Utils.sdcard;

public class File extends Application{

    public static String getSdcardPath(){
        return sdcard;
    }

    public static void createFolder(String path){
        String name = path;
        if(!name.contains(sdcard) && !path.toLowerCase().contains("sdcard"))
            name = sdcard + "/" + name;
        new java.io.File(name).mkdirs();
    }

    public static String read(String path, String _null){
        try{
            String name = path;
            if(!name.contains(sdcard) && !path.toLowerCase().contains("sdcard")) name = sdcard + "/" + name;
            java.io.File file = new java.io.File(name);
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
        catch(Exception e){
        }
        return _null;
    }

    public static void append(String path, String str){
        try{
            String name = path;
            if(!name.contains(sdcard) && !path.toLowerCase().contains("sdcard")) name = sdcard + "/" + name;
            String preContent = read(name, "");
            String newContent = preContent + str;
            write(name, newContent);
        }
        catch(Exception e){
        }
    }

    public static void write(String path, String str){
        try{
            String name = path;
            if(!name.contains(sdcard) && !path.toLowerCase().contains("sdcard")) name = sdcard + "/" + name;
            java.io.File file = new java.io.File(name);
            FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(str.getBytes());
            fos.close();
        }
        catch(Exception e){
        }
    }

    public static void remove(String name){
        if(!name.contains(sdcard) && !name.toLowerCase().contains("sdcard")) name = sdcard + "/" + name;
        java.io.File file = new java.io.File(name);
        file.delete();
    }

}
