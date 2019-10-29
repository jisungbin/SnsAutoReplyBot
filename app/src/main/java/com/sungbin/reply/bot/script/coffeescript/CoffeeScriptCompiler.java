package com.sungbin.reply.bot.script.coffeescript;

import android.app.Activity;

import com.sungbin.reply.bot.utils.Utils;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class CoffeeScriptCompiler{
    private static Scriptable scope = null;
    private static android.content.Context ctx = null;

    public static void init(Activity activty){
        try{
            InputStream stream = activty.getAssets().open("coffee-script.js");
            Reader reader = new InputStreamReader(stream, "UTF-8");
            Context rhino = Context.enter();
            rhino.setOptimizationLevel(-1);
            scope = rhino.initSafeStandardObjects();
            rhino.evaluateReader(scope, reader, "coffee-script.js", 0, null);
            Context.exit();
            stream.close();
            ctx = activty.getApplicationContext();
        }
        catch(Exception e){
            Utils.error(activty, e, "Coffee Init");
        }
    }

    public static String checkCanCompile(String name, String code) {
        try {
            if(Utils.toBoolean(Utils.readData(ctx, "NotUseCoffeeScript", "false"))){
                return "커피스크립트 사용이 꺼져 있습니다.\n어플 설정에서 커피스크립트 사용에 체크해 주세요.";
            }
            Context rhino = Context.enter();
            rhino.setOptimizationLevel(-1);
            rhino.setLanguageVersion(Context.VERSION_1_8);
            Scriptable scope = rhino.newObject(CoffeeScriptCompiler.scope);
            scope.setParentScope(CoffeeScriptCompiler.scope);
            scope.put("code", scope, code);
            String result = (String) rhino.evaluateString(scope, String.format("coffeescript.compile(code, %s);", true), "CoffeeScriptCompiler", 0, null);
            Context.exit();
            return "true";
        }
        catch(Exception e) {
            return name+"을 실행하는데 오류가 발생했습니다.\n오류 내용 : "+e;
        }
    }

    public static String compile(String code) {
        Context rhino = Context.enter();
        rhino.setOptimizationLevel(-1);
        rhino.setLanguageVersion(Context.VERSION_1_8);
        Scriptable scope = rhino.newObject(CoffeeScriptCompiler.scope);
        scope.setParentScope(CoffeeScriptCompiler.scope);
        scope.put("code", scope, code);
        String result = (String) rhino.evaluateString(scope, String.format("coffeescript.compile(code, %s);", true), "CoffeeScriptCompiler", 0, null);
        Context.exit();
        return takeOut(result);
    }

    private static String takeOut(String src){
        src = src.trim();
        int start = src.indexOf("\n");
        int end = src.lastIndexOf("\n");
        return src.substring(start, end).trim();
    }

}
