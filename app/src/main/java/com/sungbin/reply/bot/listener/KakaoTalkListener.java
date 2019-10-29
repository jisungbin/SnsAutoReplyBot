package com.sungbin.reply.bot.listener;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Base64;
import android.view.Gravity;

import com.faendir.rhino_android.RhinoAndroidHelper;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.api.Black;
import com.sungbin.reply.bot.api.File;
import com.sungbin.reply.bot.api.school.SchoolException;
import com.sungbin.reply.bot.script.coffeescript.CoffeeScriptCompiler;
import com.sungbin.reply.bot.api.PictureTransmission.ImageSendClass;
import com.sungbin.reply.bot.dto.DebugItem;
import com.sungbin.reply.bot.utils.PicturePathManager;
import com.sungbin.reply.bot.utils.PrimitiveWrapFactory;
import com.sungbin.reply.bot.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.annotations.JSStaticFunction;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.sungbin.reply.bot.view.activty.DebugActivity.adapter;
import static com.sungbin.reply.bot.view.activty.DebugActivity.items;
import static com.sungbin.reply.bot.view.activty.DebugActivity.list;
import static com.sungbin.reply.bot.api.school.School.getAreaData;
import static org.luaj.vm2.LuaValue.NIL;

public class KakaoTalkListener extends NotificationListenerService{

    public static HashMap<String, Notification.Action> actions = new HashMap<>();
    public static HashMap<String, Function> jsScripts = new HashMap<>();
    public static HashMap<String, LuaValue> luaScripts = new HashMap<>();
    public static HashMap<String, ScriptableObject> jsScope = new HashMap<>();
    public static String scriptName = "", preCode = null, newCode = null, result = null;
    private static android.content.Context ctx;
    private static int i = 0;

    @Override
    public void onCreate(){
        super.onCreate();
        ctx = getApplicationContext();

        com.sungbin.reply.bot.api.Utils.init(getApplicationContext());
        com.sungbin.reply.bot.api.Device.init(getApplicationContext());
        com.sungbin.reply.bot.api.Api.init(getApplicationContext());
        com.sungbin.reply.bot.api.AppData.init(getApplicationContext());
        com.sungbin.reply.bot.api.Black.init(getApplicationContext());

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        FancyToast.makeText(ctx, ctx.getString(R.string.all_ready_to_run), FancyToast.LENGTH_LONG, FancyToast.SUCCESS, false).show();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        FancyToast.makeText(ctx, ctx.getString(R.string.problem_run), FancyToast.LENGTH_LONG, FancyToast.ERROR, false).show();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        super.onNotificationPosted(sbn);
        if(!com.sungbin.reply.bot.utils.Utils.toBoolean(com.sungbin.reply.bot.utils.Utils.readData(getApplicationContext(), "OnOff", "true"))) return;
        String packageList = com.sungbin.reply.bot.utils.Utils.readData(getApplicationContext(), "PackageList", "com.messenger.talk");
        if(StringUtils.isBlank(packageList)) packageList = "com.kakao.talk";
        if(Arrays.asList(packageList.split("\n")).contains(sbn.getPackageName())){
            Notification.WearableExtender wExt = new Notification.WearableExtender(sbn.getNotification());
            for(Notification.Action act : wExt.getActions()){
                if(act.getRemoteInputs()!=null&&act.getRemoteInputs().length>0){
                    if(act.title.toString().toLowerCase().contains("reply")||act.title.toString().toLowerCase().contains("답장")){
                        Bundle data = sbn.getNotification().extras;
                        String room = "", sender = "", msg = "";
                        boolean isGroupChat = data.get("android.text") instanceof SpannableString;
                        if(Build.VERSION.SDK_INT>23){
                            room = data.getString("android.summaryText");
                            sender = data.get("android.title").toString();
                            if(room==null){
                                isGroupChat = false;
                                room = sender;
                            }
                            else isGroupChat = true;
                            msg = data.get("android.text").toString();
                        }
                        else{
                            room = data.getString("android.title");
                            if(isGroupChat){
                                String html = Html.toHtml((Spanned) data.get("android.text"));
                                sender = Html.fromHtml(html.split("<b>")[1].split("</b>")[0]).toString();
                                msg = Html.fromHtml(html.split("</b>")[1].split("</p>")[0].substring(1)).toString();
                            }
                            else{
                                sender = room;
                                msg = data.get("android.text").toString();
                            }
                        }
                        if(!actions.containsKey(room)) actions.put(room, act);
                        String blackRoom = com.sungbin.reply.bot.utils.Utils.readData(getApplicationContext(), "RoomBlackList", "");
                        String blackSender = com.sungbin.reply.bot.utils.Utils.readData(getApplicationContext(), "SenderBlackList", "");
                        if(blackRoom.contains(room) || blackSender.contains(sender)) return;
                        else chatHook(ctx, sender, msg, room, isGroupChat, act, sbn.getNotification().largeIcon, sbn.getPackageName());
                    }
                }
            }
        }
    }

    public void chatHook(android.content.Context act, String sender, String msg, String room, boolean isGroupChat, Notification.Action session, Bitmap profileImage, String packageName){
        try{
            String coffeePath = com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/coffeescript";
            java.io.File[] coffeeList = new java.io.File(coffeePath).listFiles();

            String luaPath = com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/LuaScript";
            java.io.File[] luaList = new java.io.File(luaPath).listFiles();

            String jsPath = com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/javascript";
            java.io.File[] jsList = new java.io.File(jsPath).listFiles();

            String simplePath = com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/simple";
            java.io.File[] simpleList = new java.io.File(simplePath).listFiles();

            if(jsList!=null){
                for(int i = 0; i<jsList.length; i++){ //자바 스크립트
                    scriptName = jsList[i].toString().replace(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/javascript/", ""); //확장자 O
                    boolean onoff = com.sungbin.reply.bot.utils.Utils.toBoolean(com.sungbin.reply.bot.utils.Utils.readData(ctx, scriptName, "true"));
                    if(!onoff) continue;
                    if(!jsScripts.containsKey(scriptName)) continue;
                    else{
                        callJsResponder(scriptName, sender, msg, room, isGroupChat, session, profileImage, packageName);
                    }
                }
            }

            if(coffeeList!=null){
                for(int i = 0; i<coffeeList.length; i++){ //커피 스크립트
                    scriptName = coffeeList[i].toString().replace(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/coffeescript/", "");
                    boolean onoff = com.sungbin.reply.bot.utils.Utils.toBoolean(com.sungbin.reply.bot.utils.Utils.readData(ctx, scriptName, "true"));
                    if(!onoff) continue;
                    if(!jsScripts.containsKey(scriptName)) continue;
                    else{
                        callJsResponder(scriptName, sender, msg, room, isGroupChat, session, profileImage, packageName);
                    }
                }
            }

            if(luaList!=null){
                for(int i = 0; i<luaList.length; i++){ //루아 스크립트
                    scriptName = luaList[i].toString().replace(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/LuaScript/", "");
                    boolean onoff = com.sungbin.reply.bot.utils.Utils.toBoolean(com.sungbin.reply.bot.utils.Utils.readData(ctx, "simple/"+scriptName, "true"));
                    if(!onoff) continue;
                    if(!luaScripts.containsKey(scriptName)) continue;
                    else{
                        callLuaResponder(scriptName, sender, msg, room);
                    }
                }
            }

            if(simpleList != null){
                for(int i = 0; i<simpleList.length; i++){
                    String name = simpleList[i].toString().replace(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/simple/", "").split("/")[0];
                    boolean onoff = com.sungbin.reply.bot.utils.Utils.toBoolean(com.sungbin.reply.bot.utils.Utils.readData(ctx, scriptName, "true"));
                    if(!onoff) continue;
                    String path = "simple/"+name+"/";
                    String RoomType = com.sungbin.reply.bot.utils.Utils.read(path+"RoomType.data", "X");
                    String MsgType = com.sungbin.reply.bot.utils.Utils.read(path+"MsgType.data", "X");
                    String Room = com.sungbin.reply.bot.utils.Utils.read(path+"Room.data", "X");
                    String Sender = com.sungbin.reply.bot.utils.Utils.read(path+"Sender.data", "X");
                    String Msg = com.sungbin.reply.bot.utils.Utils.read(path+"Msg.data", "X");
                    String Reply = com.sungbin.reply.bot.utils.Utils.read(path+"Reply.data", "X");
                    callSimpleResponder(RoomType, MsgType, Room, Sender, Msg, Reply, sender, msg, room, isGroupChat);
                }
            }

        }
        catch(Exception e){
            com.sungbin.reply.bot.utils.Utils.error(act, e, "Kaven");
        }
    }

    public static String initializeJavaScript(String name){
        try{
            java.io.File script = new java.io.File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/javascript/"+name);
            if(!script.exists()) return ctx.getString(R.string.script_file_is_non);

            if(com.sungbin.reply.bot.utils.Utils.read("javascript/"+name, null).contains("Kaven.add")){
                return kavanInit(name);
            }

            Context parseContext = new RhinoAndroidHelper().enterContext();
            parseContext.setWrapFactory(new PrimitiveWrapFactory());
            parseContext.setLanguageVersion(Context.VERSION_ES6);
            parseContext.setOptimizationLevel(-1);
            ScriptableObject scope = (ScriptableObject) parseContext.initStandardObjects(new ImporterTopLevel(parseContext));
            Script script_real = parseContext.compileReader(new FileReader(script), name, 0, null);
            ScriptableObject.defineClass(scope, Log.class);
            ScriptableObject.defineClass(scope, Api.class);
            ScriptableObject.defineClass(scope, Clock.class);
            ScriptableObject.defineClass(scope, Device.class);
            ScriptableObject.defineClass(scope, School.class);
            ScriptableObject.defineClass(scope, AppData.class);
            ScriptableObject.defineClass(scope, File.class);
            ScriptableObject.defineClass(scope, DataBase.class);
            ScriptableObject.defineClass(scope, FileStream.class);
            ScriptableObject.defineClass(scope, Bridge.class);
            ScriptableObject.defineClass(scope, Utils.class);
            ScriptableObject.defineClass(scope, Image.class);
            ScriptableObject.defineClass(scope, Black.class);
            jsScope.put(name, scope);
            script_real.exec(parseContext, scope);
            Function responder = (Function) scope.get("response", scope);
            jsScripts.put(name, responder);
            Context.exit();
            return "true";
        }
        catch(Exception e){
            if(e.toString().contains("java.lang.String android.content.Context.getPackageName()' on a null object reference")) return "리로드 오류";
            if(e.toString().contains("org.mozilla.javascript.UniqueTag cannot be cast to org.mozilla.javascript.Function")) return "리로드 오류";
            return e.getMessage();
        }
    }

    public static String initializeCoffeeScript(String name){
        try{
            java.io.File script = new java.io.File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/coffeescript/"+name);
            if(!script.exists()) return ctx.getString(R.string.script_file_is_non);
            String coffee = com.sungbin.reply.bot.utils.Utils.read("coffeescript/"+name, "");
            String check = CoffeeScriptCompiler.checkCanCompile(name, coffee);
            if(check.equals("true")){
                String js = CoffeeScriptCompiler.compile(coffee);
                com.sungbin.reply.bot.utils.Utils.save("Temporary/CoffeeToJavaScript.js", js);
                script = new java.io.File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/Temporary/CoffeeToJavaScript.js");
                Context parseContext = new RhinoAndroidHelper().enterContext();
                parseContext.setLanguageVersion(Context.VERSION_ES6);
                Script script_real = parseContext.compileReader(new FileReader(script), script.getName(), 0, null);
                ScriptableObject scope = parseContext.initStandardObjects();
                ScriptableObject.defineClass(scope, Log.class);
                ScriptableObject.defineClass(scope, Api.class);
                ScriptableObject.defineClass(scope, Clock.class);
                ScriptableObject.defineClass(scope, Device.class);
                ScriptableObject.defineClass(scope, File.class);
                ScriptableObject.defineClass(scope, DataBase.class);
                ScriptableObject.defineClass(scope, FileStream.class);
                ScriptableObject.defineClass(scope, Bridge.class);
                ScriptableObject.defineClass(scope, School.class);
                ScriptableObject.defineClass(scope, AppData.class);
                ScriptableObject.defineClass(scope, Utils.class);
                ScriptableObject.defineClass(scope, Image.class);
                ScriptableObject.defineClass(scope, Black.class);
                jsScope.put(name, scope);
                script_real.exec(parseContext, scope);
                Function responder = (Function) scope.get("response", scope);
                jsScripts.put(name, responder);
                Context.exit();
                com.sungbin.reply.bot.utils.Utils.delete("Temporary/CoffeeToJavaScript.js");
                return "true";
            }
            else{
                return check;
            }
        }
        catch(Exception e){
            if(e.toString().contains("java.lang.String android.content.Context.getPackageName()' on a null object reference")) return "컴파일 에러!\n다시 시도해 주세요.\n"+e;
            if(e.toString().contains("org.mozilla.javascript.UniqueTag cannot be cast to org.mozilla.javascript.Function")) return "컴파일 에러!\n다시 시도해 주세요.\n"+e;
            return "initializeCoffeeScript 에서 오류가 발생하였습니다.\n오류 내용 : "+e;
        }
    }

    public static String initializeLuaScript(String name){
        try{
            String code = com.sungbin.reply.bot.utils.Utils.read("LuaScript/"+name, "");

            Globals globals = JsePlatform.standardGlobals();

            LuaValue bot = CoerceJavaToLua.coerce(new Bot());
            globals.set("Bot", bot);

            LuaTable table = new LuaTable();
            table.set("sendChat", new Bot.SendChat());
            table.set("__index", table);
            bot.setmetatable(table);

            LuaValue chunk = globals.load(code);
            chunk.call();

            luaScripts.put(name, globals.get("response"));

            return "true";
        }
        catch(Exception e){
            return name+"을 실행하는데 오류가 발생했습니다.\n오류 내용 : "+e;
        }
    }

    public static boolean callJsResponder(String name, String sender, String msg, String room, boolean isGroupChat, Notification.Action session, Bitmap profileImage, String packageName){
        Context parseContext = new RhinoAndroidHelper().enterContext();
        parseContext.setLanguageVersion(Context.VERSION_ES6);
        Function responder = jsScripts.get(name);
        ScriptableObject execScope = jsScope.get(name);
        try{
            if(responder==null || execScope==null) {
                Context.exit();
                com.sungbin.reply.bot.utils.Utils.toast(ctx,
                        ctx.getString(R.string.cant_read_script));
                return false;
            }
            else{
                try{
                    responder.call(parseContext, execScope, execScope, new Object[]{room, msg, sender, isGroupChat, new Replier(session, name), new ImageDB(profileImage), packageName});
                    Context.exit();
                    return true;
                }
                catch(Exception e){
                    com.sungbin.reply.bot.utils.Utils.toast(ctx,
                            "스크립트 실행중에 심각한 오류가 발생했습니다.\n\n"+e,
                            FancyToast.LENGTH_SHORT, FancyToast.ERROR);
                    return false;
                }
            }
        }
        catch(Exception e){
            com.sungbin.reply.bot.utils.Utils.toast(ctx,
                    name+" 리로드중에 오류가 발생했습니다.\n오류 내용 : "+e,
                    FancyToast.LENGTH_SHORT, FancyToast.ERROR);
            return false;
        }
    }

    public static void callDebugJsResponder(String name, String sender, String msg, String room, boolean isGroupChat){
        Context parseContext = new RhinoAndroidHelper().enterContext();
        parseContext.setLanguageVersion(Context.VERSION_ES6);
        Function responder = jsScripts.get(name);
        ScriptableObject execScope = jsScope.get(name);
        try{
            if(responder==null || execScope==null) {
                Context.exit();
                com.sungbin.reply.bot.utils.Utils.toast(ctx,
                        ctx.getString(R.string.cant_read_script));
            }
            else{
                Resources r = ctx.getResources();
                BitmapDrawable bd = (BitmapDrawable) r.getDrawable(R.drawable.icon);
                Bitmap bitmap = bd.getBitmap();
                responder.call(parseContext, execScope, execScope, new Object[]{room, msg, sender, isGroupChat, new DebugReplier(sender), new ImageDB(bitmap), "DebugActivity"});
                Context.exit();
            }
        }
        catch(Exception e){
            com.sungbin.reply.bot.utils.Utils.toast(ctx,
                    name+"을 실행하는데 오류가 발생했습니다.\n오류 내용 : "+e);
        }
    }

    public static String callLuaResponder(String name, String sender, String msg, String room){
        try{
            LuaValue func = luaScripts.get(name);
            if(func == null) return ctx.getString(R.string.cant_read_script);
            if(room == null) func.call(NIL, LuaValue.valueOf(msg), LuaValue.valueOf(sender));
            else func.call(LuaValue.valueOf(room), LuaValue.valueOf(msg), LuaValue.valueOf(sender));
            return "true";
        }
        catch(Exception e){
            return name+"을 실행하는데 오류가 발생했습니다.\n오류 내용 : "+e;
        }
    }

    public static void callSimpleResponder(String RoomType, String MsgType, String Room, String Sender, String Msg, String Reply, String sender, String msg, String room, boolean isGroupChat){
        Notification.Action act = actions.get(room);
        if(act == null) FancyToast.makeText(ctx, room+"방의 세션을 불러올 수 없습니다.", FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
        if(Room.equals("null")) Room = room;
        if(Sender.equals("null")) Sender = sender;
        if(Msg.equals("")) Msg = msg;

        if(RoomType.equals("true")){ //단체 채팅
            if(isGroupChat){
                if(MsgType.equals("equals")){
                    if(msg.equals(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
                }
                else{ //contains
                    if(msg.contains(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
                }
            }
        }
        else if(RoomType.equals("false")){ //개인 채팅
            if(!isGroupChat){
                if(MsgType.equals("equals")){
                    if(msg.equals(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
                }
                else{ //contains
                    if(msg.contains(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
                }
            }
        }
        else{ //모두
            if(MsgType.equals("equals")){
                if(msg.equals(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
            }
            else{ //contains
                if(msg.contains(Msg)&&Sender.equals(sender)&&Room.equals(room)) reply(act, Reply);
            }
        }
    }

    public static void reply(Notification.Action session, String value){
        Intent sendIntent = new Intent();
        Bundle msg = new Bundle();
        for(RemoteInput inputable : session.getRemoteInputs())
            msg.putCharSequence(inputable.getResultKey(), value);
        RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);
        try{
            session.actionIntent.send(ctx, 0, sendIntent);
        }
        catch(Exception e){
            com.sungbin.reply.bot.utils.Utils.error(ctx, e, "reply");
        }
    }

    public static class Replier{
        private static Notification.Action session = null;
        private static String name = null;

        public Replier(Notification.Action session, String name){
            super();
            this.session = session;
            this.name = name;
        }

        public static void reply(String room, String value){
            try{
                Notification.Action session2 = actions.get(room);
                if(session2==null) {
                    com.sungbin.reply.bot.utils.Utils.toast(ctx,
                            "메세지를 전송할 방의 세션을 가져오지 못했습니다.");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 hh:mm");
                String time = sdf.format(new Date(System.currentTimeMillis()));
                Intent sendIntent = new Intent();
                Bundle msg = new Bundle();
                for(RemoteInput inputable : session2.getRemoteInputs())
                    msg.putCharSequence(inputable.getResultKey(), value);
                RemoteInput.addResultsToIntent(session2.getRemoteInputs(), sendIntent, msg);
                session2.actionIntent.send(ctx, 0, sendIntent);
                com.sungbin.reply.bot.utils.Utils.saveData(ctx, name+".time",
                        time);
            }
            catch(Exception e){
                com.sungbin.reply.bot.utils.Utils.error(ctx, e, "Replier");
            }
        }

        public static void reply(String value){
            try{
                if(session==null) {
                    com.sungbin.reply.bot.utils.Utils.toast(ctx,
                            "메세지를 전송할 방의 세션을 가져오지 못했습니다.");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 hh:mm");
                String time = sdf.format(new Date(System.currentTimeMillis()));
                Intent sendIntent = new Intent();
                Bundle msg = new Bundle();
                for(RemoteInput inputable : session.getRemoteInputs())
                    msg.putCharSequence(inputable.getResultKey(), value);
                RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);
                session.actionIntent.send(ctx, 0, sendIntent);
                com.sungbin.reply.bot.utils.Utils.saveData(ctx, name+".time",
                        time);
            }
            catch(Exception e){
                com.sungbin.reply.bot.utils.Utils.error(ctx, e, "Replier");
            }
        }

        public static void replyShowAll(String room, String value1, String value2){
            try{
                Notification.Action session2 = actions.get(room);
                if(session2==null) {
                    com.sungbin.reply.bot.utils.Utils.toast(ctx,
                            "메세지를 전송할 방의 세션을 가져오지 못했습니다.");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 hh:mm");
                String time = sdf.format(new Date(System.currentTimeMillis()));
                Intent sendIntent = new Intent();
                Bundle msg = new Bundle();
                for(RemoteInput inputable : session2.getRemoteInputs())
                    msg.putCharSequence(inputable.getResultKey(),
                            value1 + com.sungbin.reply.bot.api.Api.showAll + value2);
                RemoteInput.addResultsToIntent(session2.getRemoteInputs(), sendIntent, msg);
                session2.actionIntent.send(ctx, 0, sendIntent);
                com.sungbin.reply.bot.utils.Utils.saveData(ctx, name+".time",
                        time);
            }
            catch(Exception e){
                com.sungbin.reply.bot.utils.Utils.error(ctx, e, "Replier");
            }
        }

        public static void replyShowAll(String value1, String value2){
            try{
                if(session==null) {
                    com.sungbin.reply.bot.utils.Utils.toast(ctx,
                            "메세지를 전송할 방의 세션을 가져오지 못했습니다.");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MM월 dd일 hh:mm");
                String time = sdf.format(new Date(System.currentTimeMillis()));
                Intent sendIntent = new Intent();
                Bundle msg = new Bundle();
                for(RemoteInput inputable : session.getRemoteInputs())
                    msg.putCharSequence(inputable.getResultKey(),
                            value1 + com.sungbin.reply.bot.api.Api.showAll + value2);
                RemoteInput.addResultsToIntent(session.getRemoteInputs(), sendIntent, msg);
                session.actionIntent.send(ctx, 0, sendIntent);
                com.sungbin.reply.bot.utils.Utils.saveData(ctx, name+".time",
                        time);
            }
            catch(Exception e){
                com.sungbin.reply.bot.utils.Utils.error(ctx, e, "Replier");
            }
        }
    }

    public static class ImageDB{
        private static Bitmap profileImage = null;

        public ImageDB(Bitmap profileImage){
            super();
            this.profileImage = profileImage;
        }

        public static String getProfileImage(){
            if(profileImage == null) return "프로필 이미지가 없습니다.";
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            profileImage.compress(Bitmap.CompressFormat.PNG, 100, baos);
            byte[] bImage = baos.toByteArray();
            String base64 = Base64.encodeToString(bImage, 0);
            return base64;
        }

        public static String getPicture(){
            return PicturePathManager.getLastPicture();
        }
    }

    public static class DebugReplier{
        private String sender = null;

        private DebugReplier(String sender){
            super();
            this.sender = sender;
        }

        public void reply(String value){
            Handler mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    DebugItem item = new DebugItem(sender, value, Gravity.LEFT);
                    items.add(item);
                    adapter.notifyDataSetChanged();
                    list.scrollToPosition(items.size()-1);
                }
            }, 0);
        }
    }

    private static class Bot{
        static class SendChat extends TwoArgFunction{
            @Override
            public LuaValue call(LuaValue room, LuaValue _msg){
                Notification.Action act = actions.get(room);
                if(act == null) FancyToast.makeText(ctx,room+"방의 세션을 불러올 수 없습니다.",FancyToast.LENGTH_LONG,FancyToast.WARNING,false).show();
                else reply(act, _msg.tojstring());
                return _msg;
            }

        }
    }

    private static String kavanInit(String scriptName){
        try{
            result = null;
            preCode = com.sungbin.reply.bot.utils.Utils.read("javascript/"+scriptName, null);
            newCode = com.sungbin.reply.bot.utils.Utils.read("javascript/"+scriptName, null);
            for(i=1; i<preCode.split("Kaven.add\\(").length; i++){
                String name = preCode.split("Kaven.add\\(\"")[i].split("\"")[0];
                if(!name.contains(".js")) name = name + ".js";
                String cut = "Kaven.add(\""+name+"\")";
                String kaven = com.sungbin.reply.bot.utils.Utils.
                        read("Kaven/"+name, "null");
                if(kaven == "null") {
                    return name + "을 Kaven에서 먼저 다운로드 해 주세요.";
                } else {
                    newCode = newCode.replace(cut, kaven);
                }
            }

            com.sungbin.reply.bot.utils.Utils.save("Temporary/KavenToJavaScript.js", newCode);
            java.io.File script = new java.io.File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/Temporary/KavenToJavaScript.js");
            Context parseContext = new RhinoAndroidHelper().enterContext();
            parseContext.setWrapFactory(new PrimitiveWrapFactory());
            parseContext.setLanguageVersion(Context.VERSION_ES6);
            parseContext.setOptimizationLevel(-1);
            ScriptableObject scope = (ScriptableObject) parseContext.initStandardObjects(new ImporterTopLevel(parseContext));
            Script script_real = parseContext.compileReader(new FileReader(script), scriptName, 0, null);
            ScriptableObject.defineClass(scope, Log.class);
            ScriptableObject.defineClass(scope, Api.class);
            ScriptableObject.defineClass(scope, Clock.class);
            ScriptableObject.defineClass(scope, Device.class);
            ScriptableObject.defineClass(scope, School.class);
            ScriptableObject.defineClass(scope, AppData.class);
            ScriptableObject.defineClass(scope, File.class);
            ScriptableObject.defineClass(scope, DataBase.class);
            ScriptableObject.defineClass(scope, FileStream.class);
            ScriptableObject.defineClass(scope, Bridge.class);
            ScriptableObject.defineClass(scope, Utils.class);
            ScriptableObject.defineClass(scope, Image.class);
            ScriptableObject.defineClass(scope, Black.class);
            jsScope.put(scriptName, scope);
            script_real.exec(parseContext, scope);
            Function responder = (Function) scope.get("response", scope);
            jsScripts.put(scriptName, responder);
            Context.exit();
            com.sungbin.reply.bot.utils.Utils.delete("Temporary/KavenToJavaScript.js");
            return "true";
        }
        catch(Exception e){
            if(e.toString().contains("java.lang.String android.content.Context.getPackageName()' on a null object reference")) return "리로드 오류";
            if(e.toString().contains("org.mozilla.javascript.UniqueTag cannot be cast to org.mozilla.javascript.Function")) return "리로드 오류";
            return e.getMessage();
        }
    }

    public static class Log extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Log";
        }

        @JSStaticFunction
        public static void d(String log){
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            String getTime = sdf.format(date);

            String pre = com.sungbin.reply.bot.utils.Utils.readData(ctx, "Log/"+scriptName, "");
            String new_ = pre + "\n<font color=green>["+getTime+"] "+log+"</font>";

            com.sungbin.reply.bot.utils.Utils.saveData(ctx, "Log/"+scriptName, new_);
        }

        @JSStaticFunction
        public static void e(String log){
            long now = System.currentTimeMillis();
            Date date = new Date(now);
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
            String getTime = sdf.format(date);

            String pre = com.sungbin.reply.bot.utils.Utils.readData(ctx, "Log/"+scriptName, "");
            String new_ = pre + "\n<font color=red>["+getTime+"] "+log+"</font>";

            com.sungbin.reply.bot.utils.Utils.saveData(ctx, "Log/"+scriptName, new_);
        }

    }

    public static class AppData extends ScriptableObject{
        @Override
        public String getClassName(){
            return "AppData";
        }

        @JSStaticFunction
        public static void putInt(String name, int value){
            com.sungbin.reply.bot.api.AppData.putInt(name, value);
        }

        @JSStaticFunction
        public static void putString(String name, String value){
            com.sungbin.reply.bot.api.AppData.putString(name, value);
        }

        @JSStaticFunction
        public static void putBoolean(String name, boolean value){
            com.sungbin.reply.bot.api.AppData.putBoolean(name, value);
        }

        @JSStaticFunction
        public static int getInt(String name, int value){
            return com.sungbin.reply.bot.api.AppData.getInt(name, value);
        }

        @JSStaticFunction
        public static String getString(String name, String value){
            return com.sungbin.reply.bot.api.AppData.getString(name, value);
        }

        @JSStaticFunction
        public static Boolean getBoolean(String name, boolean value){
            return com.sungbin.reply.bot.api.AppData.getBoolean(name, value);
        }

        @JSStaticFunction
        public static void clear(){
            com.sungbin.reply.bot.api.AppData.clear();
        }

        @JSStaticFunction
        public static void remove(String name){
            com.sungbin.reply.bot.api.AppData.remove(name);
        }
    }

    public static class Api extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Api";
        }

        @JSStaticFunction
        public static android.content.Context getContext(){
            return ctx;
        }

        @JSStaticFunction
        public static String getHtml(String link){
            return com.sungbin.reply.bot.api.Api.getHtmlFromJava(link);
        }

        @JSStaticFunction
        public static String post(String adress, String name, String data){
            return com.sungbin.reply.bot.api.Api.post(adress, name, data);
        }

        @JSStaticFunction
        public static Boolean replyRoom(String room, String mesesage){
            return com.sungbin.reply.bot.api.Api.replyRoom(room, mesesage);
        }

        @JSStaticFunction
        public static Boolean replyRoomShowAll(String room, String msg1, String msg2){
            return com.sungbin.reply.bot.api.Api.replyRoomShowAll(room, msg1, msg2);
        }

        @JSStaticFunction
        public static String deleteHtml(String html){
            return com.sungbin.reply.bot.api.Api.deleteHtml(html);
        }

        @JSStaticFunction
        public static String translate(String target, String text){
            return com.sungbin.reply.bot.api.Api.translate(target, text);
        }
    }

    public static class Clock extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Clock";
        }

        @JSStaticFunction
        public static String normal(boolean isFull){
            return com.sungbin.reply.bot.api.Clock.normal(isFull);
        }

        @JSStaticFunction
        public static String digital(boolean isFull){
            return com.sungbin.reply.bot.api.Clock.digital(isFull);
        }

        @JSStaticFunction
        public static String analog(){
            return com.sungbin.reply.bot.api.Clock.analog();
        }
    }

    public static class Device extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Device";
        }

        @JSStaticFunction
        public static String getPhoneModel(){
            return com.sungbin.reply.bot.api.Device.getPhoneModel();
        }

        @JSStaticFunction
        public static int getAndroidSDKVersion(){
            return com.sungbin.reply.bot.api.Device.getAndroidSDKVersion();
        }

        @JSStaticFunction
        public static String getAndroidVersion(){
            return com.sungbin.reply.bot.api.Device.getAndroidVersion();
        }

        @JSStaticFunction
        public static int getBattey(){
            return com.sungbin.reply.bot.api.Device.getBattey();
        }

        @JSStaticFunction
        public static boolean getIsCharging(){
            return com.sungbin.reply.bot.api.Device.getIsCharging();
        }
    }

    public static class School extends ScriptableObject{
        @Override
        public String getClassName(){
            return "School";
        }

        @JSStaticFunction
        public static String[] getMeal(String area, String name, int year, int month){
            try {
                String[] areaList = "서울::인천::부산::광주::대전::대구::세종::울산::경기::강원::충북::충남::경북::경남::전북::전남::제주".split("::");
                if(Arrays.toString(areaList).contains(area)){
                    com.sungbin.reply.bot.api.school.School school = com.sungbin.reply.bot.api.school.School.find(getAreaData(area), name);
                    List list = school.getMonthlyMenu(year, month);
                    String strings = "";
                    for(int i=0;i<list.size();i++){
                        strings += "★" + list.get(i);
                    }
                    strings = strings.replaceFirst("★", "");
                    return strings.split("★");
                }
                else{
                    return ("false★없는 지역입니다.\n지역 리스트 : " + Arrays.toString(areaList)).split("★");
                }
            } catch (SchoolException e) {
                return ("false★" + e.getMessage()).split("★"); //배열로 만들려고
            }
        }

        @JSStaticFunction
        public static String[] getPlan(String area, String name, int year, int month){
            try {
                String[] areaList = "서울::인천::부산::광주::대전::대구::세종::울산::경기::강원::충북::충남::경북::경남::전북::전남::제주".split("::");
                if(Arrays.toString(areaList).contains(area)){
                    com.sungbin.reply.bot.api.school.School school = com.sungbin.reply.bot.api.school.School.find(getAreaData(area), name);
                    List list = school.getMonthlySchedule(year, month);
                    String strings = "";
                    for(int i = 0; i<list.size(); i++){
                        String content = list.get(i).toString().replace("\n", "");
                        if(content.isEmpty()) content = "일정이 등록되지 않았습니다.";
                        strings += "★"+content;
                    }
                    strings = strings.replaceFirst("★", "");
                    return strings.split("★");
                }
                else{
                    return ("false★없는 지역입니다.\n지역 리스트 : " + Arrays.toString(areaList)).split("★");
                }
            } catch (SchoolException e) {
                return ("false★" + e.getMessage()).split("★"); //배열로 만들려고
            }
        }

    }

    public static class Bridge extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Bridge";
        }

        @JSStaticFunction
        public static String getVariableValue(String name, String value){
            if(jsScope.containsKey(name)){
                try{
                    ScriptableObject execScope = jsScope.get(name);
                    String result = ((Object) execScope.get(value, execScope)).toString();
                    if(result.matches("org.mozilla.javascript.UniqueTag@[a-z0-9]{7}: NOT_FOUND")){
                        return "해당 스크립트에서 " + value + "라는 변수를 찾을 수 없습니다.";
                    }
                    else return result;
                }
                catch(Exception e){
                    return "해당 스크립트에서 " + value + "라는 변수를 찾을 수 없습니다.";
                }
            }
            else return "해당 스크립트의 스코프에 접근할 수 없습니다.\n해당 스크립트 리로드를 해 주세요.";
        }
    }

    public static class FileStream extends ScriptableObject{
        @Override
        public String getClassName(){
            return "FileStream";
        }

        @JSStaticFunction
        public static String read(String path){
            return com.sungbin.reply.bot.api.File.read(path, null);
        }

        @JSStaticFunction
        public static void write(String path, String content){
            com.sungbin.reply.bot.api.File.write(path, content);
        }

        @JSStaticFunction
        public static void append(String path, String content){
            com.sungbin.reply.bot.api.File.append(path, content);
        }

        @JSStaticFunction
        public static void remove(String path){
            com.sungbin.reply.bot.api.File.remove(path);
        }
    }

    public static class DataBase extends ScriptableObject{
        @Override
        public String getClassName(){
            return "DataBase";
        }

        @JSStaticFunction
        public static String getDataBase(String path){
            return com.sungbin.reply.bot.utils.Utils.read("DataBase/"+path, null);
        }

        @JSStaticFunction
        public static void setDataBase(String path, String content){
            com.sungbin.reply.bot.utils.Utils.createFolder("DataBase");
            com.sungbin.reply.bot.utils.Utils.save("DataBase/"+path, content);
        }

        @JSStaticFunction
        public static void appendDataBase(String path, String content){
            String preContent = DataBase.getDataBase(path);
            com.sungbin.reply.bot.utils.Utils.save("DataBase/"+path, preContent+content);
        }

        @JSStaticFunction
        public static void removeDataBase(String path){
            com.sungbin.reply.bot.utils.Utils.delete("DataBase/"+path);
        }
    }

    public static class Black extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Black";
        }

        @JSStaticFunction
        public static String getSender(){
            return com.sungbin.reply.bot.api.Black.readSender();
        }

        @JSStaticFunction
        public static String getRoom(){
            return com.sungbin.reply.bot.api.Black.readRoom();
        }

        @JSStaticFunction
        public static void addRoom(String room){
            com.sungbin.reply.bot.api.Black.addRoom(room);
        }

        @JSStaticFunction
        public static void addSender(String sender){
            com.sungbin.reply.bot.api.Black.addSender(sender);
        }

        @JSStaticFunction
        public static void removeRoom(String room){
            com.sungbin.reply.bot.api.Black.removeRoom(room);
        }

        @JSStaticFunction
        public static void removeSender(String sender){
            com.sungbin.reply.bot.api.Black.addSender(sender);
        }
    }

    public static class File extends ScriptableObject{
        @Override
        public String getClassName(){
            return "File";
        }

        @JSStaticFunction
        public static String getSdcardPath(){
            return com.sungbin.reply.bot.api.File.getSdcardPath();
        }

        @JSStaticFunction
        public static void createFolder(String path){
            com.sungbin.reply.bot.api.File.createFolder(path);
        }

        @JSStaticFunction
        public static String read(String name, String _null){
            return com.sungbin.reply.bot.api.File.read(name, _null);
        }

        @JSStaticFunction
        public static void write(String name, String content){
            com.sungbin.reply.bot.api.File.write(name, content);
        }

        @JSStaticFunction
        public static void append(String name, String content){
            com.sungbin.reply.bot.api.File.append(name, content);
        }

        @JSStaticFunction
        public static void remove(String name){
            com.sungbin.reply.bot.api.File.remove(name);
        }
    }

    public static class Utils extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Utils";
        }

        @JSStaticFunction
        public static void makeToast(String str){
            com.sungbin.reply.bot.api.Utils.makeToast(str);
        }

        @JSStaticFunction
        public static void makeNoti(String title, String content){
            com.sungbin.reply.bot.api.Utils.makeNoti(title, content);
        }

        @JSStaticFunction
        public static String getWebText(String link){
            return com.sungbin.reply.bot.api.Api.getHtmlFromJsoup(link);
        }

        @JSStaticFunction
        public static void makeVibration(int time){
            com.sungbin.reply.bot.api.Utils.makeVibration(time);
        }

        @JSStaticFunction
        public static void copy(String content){
            com.sungbin.reply.bot.api.Utils.copy(content);
        }
    }

    public static class Image extends ScriptableObject{
        @Override
        public String getClassName(){
            return "Image";
        }

        @JSStaticFunction
        public static void getXY(){
            ImageSendClass imageSendClass = new ImageSendClass(ctx);
            imageSendClass.getXY();
        }
    }

}