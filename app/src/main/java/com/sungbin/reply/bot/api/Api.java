package com.sungbin.reply.bot.api;

import android.content.Context;
import android.os.StrictMode;
import android.text.Html;
import android.util.Log;

import com.sungbin.reply.bot.listener.KakaoTalkListener;
import com.sungbin.reply.bot.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Api{

    public static String result = null;
    public static String showAll = StringUtils.repeat("\u200b", 500);
    private static Context ctx = null;
    private static String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    public static void init(Context context){
        ctx = context;
    }

    public static String getHtmlFromJava(String adress){
        try {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
            URLConnection con = new URL(adress).openConnection();
            con.setConnectTimeout(5000);
            con.setUseCaches(false);
            InputStreamReader isr = new InputStreamReader(con.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            String str = br.readLine();
            String str2 = "";
            while (true) {
                str2 = br.readLine();
                if (str2 != null) {
                    str = str + "\n" + str2;
                } else {
                    br.close();
                    isr.close();
                    return str;
                }
            }
        } catch (Exception e) {
            return "HTML 파싱중 오류 발생!\n\n" + e.getMessage();
        }
    }

    public static String getHtmlFromJsoup(String adress){
        try{
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            Connection conn = Jsoup.connect(adress).userAgent(USER_AGENT).timeout(Utils.Number(Utils.readData(ctx, "HtmlParseTime", "5")) * 1000);
            Document doc = conn.get();
            return doc.toString();
        }
        catch(IOException e){
            return e.getMessage();
        }
    }

    public static boolean replyRoom(String room, String msg){
        if(KakaoTalkListener.actions.containsKey(room)) {
            KakaoTalkListener.reply(KakaoTalkListener.actions.get(room), msg);
            return true;
        } else return false;
    }

    public static boolean replyRoomShowAll(String room, String msg1, String msg2){
        if(KakaoTalkListener.actions.containsKey(room)) {
            KakaoTalkListener.reply(KakaoTalkListener.actions.get(room), msg1 + showAll + msg2);
            return true;
        } else return false;
    }

    public static String deleteHtml(String html){
        return Html.fromHtml(html).toString();
    }

    public static String getLanguageCode(String text){
        try {
            String id = Utils.readData(ctx, "naver-id", "null");
            String secret = Utils.readData(ctx, "naver-secret", "null");
            if(id=="null"||secret=="null") return null;
            String query = URLEncoder.encode(text, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/detectLangs";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", id);
            con.setRequestProperty("X-Naver-Client-Secret", secret);
            String postParams = "query=" + query;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            return response.toString().split("\":\"")[1].split("\"\\}")[0];
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private enum Language{
        한글("ko"), 한국어("ko"), 영어("en"), 중국어간체("zh-CN"),
        중국어번체("zh-TW"), 스페인어("es"), 프랑스어("fr"),
        베트남어("vi"), 태국어("th"), 인도네시아어("id");

        String value;
        private Language(String value){
            this.value = value;
        }
        public String getValue(){
            return value;
        }
    }

    public static String translate(String target, String text){
        try {
            String sourceLang = getLanguageCode(text);
            if(sourceLang==null){
                return "어플 설정에서 API 설정을 해 주세요.";
            }
            String targetCode = null;
            try{
                targetCode = Language.valueOf(target).getValue();
            }
            catch(Exception e){
                return "타겟 언어가 잘못됬습니다.\n지원하는 언어 : " + Arrays.toString(Language.values());
            }
            String id = Utils.readData(ctx, "naver-id", "null");
            String secret = Utils.readData(ctx, "naver-secret", "null");
            String query = URLEncoder.encode(text, "UTF-8");
            String apiURL = "https://openapi.naver.com/v1/papago/n2mt";
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("X-Naver-Client-Id", id);
            con.setRequestProperty("X-Naver-Client-Secret", secret);
            String postParams = "source="+sourceLang+"&target="+targetCode+"&text="+query;
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(postParams);
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) {
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            String result = response.toString();
            if(result.contains("translatedText")){
                return result.split("translatedText\":\"")[1].split("\"\\}\\}\\}")[0];
            }
            else {
                return "번역중 오류 발생!\n\n"+result.split("errorMessage\":\"")[1].split("\",\"errorCode")[0];
            }
        } catch (Exception e) {
            return "번역중 오류 발생!\n\n"+e.getMessage();
        }
    }

    public static String post(String adress, String postName, String postData){
        new Thread(new Runnable(){
            @Override
            public void run(){
                try{
                    HttpPost request = new HttpPost(adress);
                    ArrayList data = new ArrayList();
                    data.add(new BasicNameValuePair(postName, postData));
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(data, "UTF-8");
                    request.setEntity(entity);
                    DefaultHttpClient client = new DefaultHttpClient();
                    client.execute(request);
                    result = "true";
                }
                catch(Exception e){
                    result = e.getMessage();
                }
            }
        }).start();
        return result;
    }

}
