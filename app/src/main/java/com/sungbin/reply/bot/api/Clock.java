package com.sungbin.reply.bot.api;

import com.sungbin.reply.bot.utils.Utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Clock{
    public static String normal(boolean isFull){
        SimpleDateFormat sdf = new SimpleDateFormat(isFull ? "kk:mm" : "hh:mm");
        return sdf.format( new Date(System.currentTimeMillis()));
    }

    private static String replaceLast(String string, String toReplace, String replacement) {
        int pos = string.lastIndexOf(toReplace);
        if(pos > -1) {
            return string.substring(0, pos) + replacement + string.substring(pos + toReplace.length(), string.length());
        }
        else {
            return string;
        }
    }

    public static String getSource(){
        try{
            URL url = new URL("https://firebasestorage.googleapis.com/v0/b/new-auto-reply-bot.appspot.com/o/analog_clock.js?alt=media&token=609f0bf0-ecb0-44b4-81e3-e0a923268367");
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String str = br.readLine();
            String line = "";
            while((line = br.readLine()) != null){
                str += "\n"+line;
            }
            br.close();
            return str;
        }
        catch(Exception e){
            return e.toString();
        }
    }

    public static String analog(){
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        try{
            Scriptable scope = rhino.initSafeStandardObjects();
            Object result = rhino.evaluateString(scope, getSource(), "javascript", 1, null);
            return result.toString();
        }
        catch(Exception e){
            return e.toString();
        }
    }

    public static String digital(boolean isFull){
        String[][] digtalNumber = {{"███","█░█","█░█","█░█","███"},{"░░█","░░█","░░█","░░█","░░█"},{"███","░░█","███","█░░","███"},{"███","░░█","███","░░█","███"},{"█░█","█░█","███","░░█","░░█"},{"███","█░░","███","░░█","███"},{"███","█░░","███","█░█","███"},{"███","█░█","░░█","░░█","░░█"},{"███","█░█","███","█░█","███"},{"███","█░█","███","░░█","███"}};
        String[] dot = {"░","█","░","█","░"};
        String data = "";
        String number = normal(isFull);

        for(int i=0;i<5;i++){
            String str = "";

            for(int n=0;n<number.length();n++){
                String cash = number.charAt(n) + "";
                if(!cash.equals(":")){ //숫자
                    String num = digtalNumber[Utils.Number(cash)][i];
                    str += num + " ";
                }
                else{ //:
                    str += dot[i] + " ";
                }
            }

            str = replaceLast(str, " ","");
            data += str + "::";
        }
        return data.replace("::", "\n");
    }
}
