package com.sungbin.reply.bot.view.activty;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.Fade;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyCloseAd;
import com.fsn.cauly.CaulyCloseAdListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.adapter.MainAdapter;
import com.sungbin.reply.bot.BuildConfig;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.script.coffeescript.CoffeeScriptCompiler;
import com.sungbin.reply.bot.notification.NotificationManager;
import com.sungbin.reply.bot.utils.Utils;
import com.sungbin.reply.bot.view.sourcehub.utils.DialogUtils;
import com.sungbin.reply.bot.view.sourcehub.view.activity.LoginActivity;
import com.sungbin.reply.bot.widget.BubbleTab;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, CaulyCloseAdListener{

    public static MainAdapter adpater;
    public static ViewPager viewPager;
    public static BottomNavigationView mainNavi;
    public static BubbleTab bubbleTab;
    private static Switch onoff;
    private static String lang, roomTypeStr, msgTypeStr;
    private static boolean error = false;
    private static Activity act;
    private static AlertDialog alert;
    private static int langType;
    private int color = 0;
    private CaulyCloseAd mCloseAd;
    private FirebaseRemoteConfig remoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        boolean useOldMain = Utils.toBoolean(Utils.readData(getApplicationContext(), "UseOldHome", "false"));
        setContentView(!useOldMain?R.layout.activity_main_new:R.layout.activity_main);

        act = MainActivity.this;

        if(Utils.readData(getApplicationContext(), "permission", "false").equals("false")){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){ //안드로이드 버전 6.0 이상일때
                finish();
                Intent i = new Intent(MainActivity.this, PermissionAgree.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            else Utils.saveData(getApplicationContext(), "permission", "true");
        }

        CaulyAdInfo closeAdInfo = new CaulyAdInfoBuilder(getString(R.string.ad_id)).build();
        mCloseAd = new CaulyCloseAd();
        mCloseAd.setButtonText("확인", "닫기");
        mCloseAd.setDescriptionText(getString(R.string.thank_you_watch_ad));
        mCloseAd.setAdInfo(closeAdInfo);
        mCloseAd.setCloseAdListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(!Utils.isInstall(getApplicationContext(), "com.google.android.wearable.app")){
            Utils.toast(this, "안드로이드 웨어 설치가 필요합니다.", FancyToast.LENGTH_SHORT, FancyToast.WARNING);
            Intent marketLaunch = new Intent(Intent.ACTION_VIEW);
            marketLaunch.setData(Uri.parse("market://search?q=com.google.android.wearable.app"));
            startActivity(marketLaunch);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if(useOldMain) bubbleTab = findViewById(R.id.bubbleTab);
        else mainNavi = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.viewPager);
        onoff = findViewById(R.id.onoff);

        adpater = new MainAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adpater);
        if(!useOldMain){
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener(){
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels){
                }

                @Override
                public void onPageSelected(int position){
                    langType = position;
                    switch(position){
                        case 0:
                            mainNavi.getMenu().findItem(R.id.action_js).setChecked(true);
                            break;
                        case 1:
                            mainNavi.getMenu().findItem(R.id.action_coffee).setChecked(true);
                            break;
                        case 2:
                            mainNavi.getMenu().findItem(R.id.action_lua).setChecked(true);
                            break;
                        case 3:
                            mainNavi.getMenu().findItem(R.id.action_simple).setChecked(true);
                            break;
                    }
                }

                @Override
                public void onPageScrollStateChanged(int state){

                }
            });
        }

        if(useOldMain){
            bubbleTab.setupWithViewPager(viewPager);
        }
        else{
            mainNavi.setItemIconTintList(null);
            mainNavi.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item){
                    switch(item.getItemId()){
                        case R.id.action_js:
                            viewPager.setCurrentItem(0);
                            break;
                        case R.id.action_coffee:
                            viewPager.setCurrentItem(1);
                            break;
                        case R.id.action_lua:
                            viewPager.setCurrentItem(2);
                            break;
                        default:
                            viewPager.setCurrentItem(3);
                    }
                    return true;
                }
            });
        }

        NotificationManager.setGroupName(getString(R.string.short_name));
        NotificationManager.createChannel(getApplicationContext(), getString(R.string.name_notification), getString(R.string.info_notification));

        onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean tf){
                Utils.saveData(getApplicationContext(), "OnOff", tf+"");
                if(tf){
                    String list = Utils.readData(getApplicationContext(), "ScriptOn", "");
                    if(!StringUtils.isBlank(list.replace("\n", ""))){
                        NotificationManager.showInboxStyleNotification(getApplicationContext(), 1, getString(R.string.short_name), getString(R.string.bot_is_running), list.split("\n"));
                    }
                    else NotificationManager.showNormalNotification(getApplicationContext(), 1, getString(R.string.short_name), getString(R.string.run_script_non));
                }
                else{
                    NotificationManager.deleteNotification(getApplicationContext(), 1);
                }
            }
        });

        onoff.setChecked(Utils.toBoolean(Utils.readData(getApplicationContext(), "OnOff", "true")));

        if(Utils.toBoolean(Utils.readData(getApplicationContext(), "OnOff", "true"))){
            String list = Utils.readData(getApplicationContext(), "ScriptOn", "");
            if(!StringUtils.isBlank(list.replace("\n", ""))){
                NotificationManager.showInboxStyleNotification(getApplicationContext(), 1, getString(R.string.short_name), getString(R.string.bot_is_running), list.split("\n"));
            }
            else NotificationManager.showNormalNotification(getApplicationContext(), 1, getString(R.string.short_name), getString(R.string.run_script_non));
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Fade fade = new Fade();
            fade.setDuration(2000);
            getWindow().setEnterTransition(fade);
        }

        if(!Utils.toBoolean(Utils.readData(getApplicationContext(), "NotUseCoffeeScript", "false")))
            CoffeeScriptCompiler.init(this);

        Utils.createFolder("javascript");
        Utils.createFolder("LuaScript");
        Utils.createFolder("coffeescript");
        Utils.createFolder("simple");
        Utils.createFolder("Temporary");

        if(Utils.toBoolean(Utils.readData(getApplicationContext(), "permission", "false"))){
            remoteConfig = FirebaseRemoteConfig.getInstance();
            FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings
                    .Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build();
            remoteConfig.setConfigSettings(configSettings);
            remoteConfig.fetch(60).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    remoteConfig.activateFetched();
                }
                else{
                    Utils.toast(this, "서버에서 데이터를 불러오는 중에 오류가 발생했습니다.");
                }
                displayMessage();
            });
        }

        final String primary = Utils.readData(getApplicationContext(), "primary", "#42A5F5");
        final String primaryDark = Utils.readData(getApplicationContext(), "primaryDark", "#0077C2");
        final String accent = Utils.readData(getApplicationContext(), "accent", "#80D6FF");

        boolean themeChange = Utils.toBoolean(Utils.readData(getApplicationContext(), "theme change", "false"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(primaryDark));
            window.setNavigationBarColor(Color.parseColor(accent));
            toolbar.setBackgroundColor(Color.parseColor(primary));
            onoff.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            onoff.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            onoff.setTextColor(Color.parseColor(primaryDark));
            if(themeChange){
                FancyToast.makeText(this, getString(R.string.succes_theme), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                Utils.saveData(getApplicationContext(), "theme change", "false");
            }
        }
        else{
            if(themeChange){
                Utils.saveData(getApplicationContext(), "theme change", "false");
                FancyToast.makeText(this, getString(R.string.cant_set_theme), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show();
            }
        }

        if(Utils.toBoolean(Utils.readData(getApplicationContext(), "ShowToastEasterEgg", "false"))){
            Handler delayHandler = new Handler();
            delayHandler.postDelayed(new Runnable(){
                @Override
                public void run(){
                    final Toast toast = FancyToast.makeText(getApplicationContext(), "도훈아 고마워", FancyToast.LENGTH_LONG, FancyToast.INFO, false);
                    toast.show();
                    Handler delayHandler = new Handler();
                    delayHandler.postDelayed(new Runnable(){
                        @Override
                        public void run(){
                            toast.cancel();
                            final Toast toast = FancyToast.makeText(getApplicationContext(), "흠아 고마워", FancyToast.LENGTH_LONG, FancyToast.INFO, false);
                            toast.show();
                            Handler delayHandler = new Handler();
                            delayHandler.postDelayed(new Runnable(){
                                @Override
                                public void run(){
                                    toast.cancel();
                                    final Toast toast = FancyToast.makeText(getApplicationContext(), "승환이 귀여워", FancyToast.LENGTH_LONG, FancyToast.INFO, false);
                                    toast.show();

                                    Handler delayHandler = new Handler();
                                    delayHandler.postDelayed(new Runnable(){
                                        @Override
                                        public void run(){
                                            toast.cancel();
                                        }
                                    }, 200);
                                }
                            }, 200);
                        }
                    }, 200);
                }
            }, 200);
        }
    }

    /*----- 플로팅 광고 ----*/
    @Override
    protected void onResume() {
        super.onResume();
        if(mCloseAd != null) mCloseAd.resume(this); // 필수 호출
    }

    @Override
    public void onFailedToReceiveCloseAd(CaulyCloseAd ad, int errCode,String errMsg) {
        // 광고 로드 실패
    }

    @Override
    public void onLeaveCloseAd(CaulyCloseAd ad) {
        // CloseAd의 광고를 클릭하여 앱을 벗어났을 경우 호출되는 함수이다.
    }

    @Override
    public void onReceiveCloseAd(CaulyCloseAd ad, boolean isChargable) {
        // CloseAd의 request()를 호출했을 때, 광고의 여부를 알려주는 함수이다.
    }

    @Override
    public void onLeftClicked(CaulyCloseAd ad) {
        //왼쪽 버튼을 클릭 하였을 때, 원하는 작업을 수행하면 된다.
    }

    @Override
    public void onRightClicked(CaulyCloseAd ad) {
        //오른쪽 버튼을 클릭 하였을 때, 원하는 작업을 수행하면 된다.
        //Default로는 오른쪽 버튼이 종료로 설정되어있다.
    }

    @Override
    public void onShowedCloseAd(CaulyCloseAd ad, boolean isChargable) {
        // 광고가 닫혔을때 실행된다.
    }
    /*----- 플로팅 광고 끝 ----*/

    @Override
    public void onBackPressed(){
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }
        else{
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent view in AndroidManifest.xml.
        int id = item.getItemId();

        if(id==R.id.action_licenses){
            new EasyLicensesDialogCompat(this).setTitle(R.string.opensource_dialog_title).setPositiveButton("닫기", null).show();
        }
        if(id==R.id.action_soucre){
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(i);
        }
        if(id==R.id.action_settings){
            Intent i = new Intent(MainActivity.this, SettingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        if(id==R.id.action_kaven){
            Intent i = new Intent(MainActivity.this, KavenActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id==R.id.rate){
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("market://details?id=com.sungbin.reply.bot"));
            startActivity(i);
        }
        if(id==R.id.share){
            Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("text/plain");
            String text = getString(R.string.share_app);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            Intent chooser = Intent.createChooser(intent, getString(R.string.share_app_title));
            startActivity(chooser);
        }
        if(id==R.id.email){
            showSendEmailDialog();
        }
        if(id==R.id.showAd){
            Utils.toast(this, getString(R.string.thank_you_watch_ad), FancyToast.LENGTH_LONG, FancyToast.SUCCESS);
            mCloseAd.show(this);
        }
        if(id==R.id.showCafe){
            Utils.showWebTab("https://cafe.naver.com/nameyee", this);
        }
        if(id==R.id.showApi){
            Utils.showWebTab("https://github.com/sungbin5304/NewAutoReplyBot-Helper", this);
        }
        if(id==R.id.info){
            //FancyToast.makeText(this, "프로필 사진을 불러오는 중입니다...", FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
            showAbtMe();
        }
        if(id==R.id.theme){
            //setThemeDialog();
            Utils.toast(this,
                    "임시 지원 중단...\n테마 기능 리메이크중...",
                    FancyToast.LENGTH_SHORT, FancyToast.WARNING);
        }
        if(id==R.id.exit){
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSendEmailDialog(){
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle(R.string.send_email_to_dev);

            int p = Utils.dip2px(this, 7);

            final LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(p, p, p, p);
            layout.setFocusableInTouchMode(true);

            final TextInputLayout textLayout = new TextInputLayout(this);
            textLayout.setCounterEnabled(true);

            final TextInputEditText inputTitle = new TextInputEditText(this);
            inputTitle.setHint(R.string.input_email_title);

            textLayout.addView(inputTitle);
            layout.addView(textLayout);

            final TextInputLayout textLayout2 = new TextInputLayout(this);
            textLayout2.setCounterEnabled(true);

            final TextInputEditText inputText = new TextInputEditText(this);
            inputText.setHint(R.string.input_email_content);

            textLayout2.addView(inputText);
            layout.addView(textLayout2);

            dialog.setPositiveButton("전송", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    if(!inputTitle.getText().toString().isEmpty()&&!inputText.getText().toString().isEmpty()){
                        Intent email = new Intent(Intent.ACTION_SEND);
                        email.setType("plain/text");
                        String[] address = {getString(R.string.email_naver), getString(R.string.email_google)};
                        email.putExtra(Intent.EXTRA_EMAIL, address);
                        email.putExtra(Intent.EXTRA_SUBJECT, inputTitle.getText().toString());
                        email.putExtra(Intent.EXTRA_TEXT, inputText.getText().toString());
                        startActivity(email);
                    }
                    else FancyToast.makeText(getApplicationContext(), getString(R.string.plz_input_all), FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                }
            });

            dialog.setNegativeButton("취소", null);


            ScrollView scroll = new ScrollView(this);
            scroll.addView(layout);

            dialog.setView(scroll);
            dialog.show();
        }
        catch(Exception e){
            Utils.toast(this, e.toString());
        }
    }

    private String getAppVersionName(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        }
        catch(PackageManager.NameNotFoundException e){
            return "null";
        }
    }

    private void displayMessage() {
        String newVersionName = remoteConfig.getString("version_name");
        String nowVersionName = getAppVersionName();

        String noticeCode = remoteConfig.getString("notice_code");
        String notice = remoteConfig.getString("notice_msg").replace("\\n", "\n");
        boolean wasShowNotice = Utils.toBoolean(Utils.readData(getApplicationContext(), noticeCode, "false"));

        if(!newVersionName.equals(nowVersionName)){
            Utils.toast(this, getResources().getString(R.string.need_update), FancyToast.LENGTH_LONG, FancyToast.INFO);
            showNewVersionDialog();
        }

        if(!wasShowNotice){
            showNotice(notice);
            Utils.saveData(getApplicationContext(), noticeCode, "true");
        }
    }

    private void showNotice(String content){
        final Context ctx = MainActivity.this;

        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
        dialog.setTitle("공지사항");
        dialog.setMessage(content);
        dialog.setPositiveButton("닫기", null);
        dialog.setCancelable(false);
        dialog.show();
    }

    public void showNewVersionDialog(){
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("market://details?id=com.sungbin.reply.bot"));
        startActivity(i);
    }

    public void showAbtMe(){
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Translucent);
            /*AboutView view = AboutBuilder.with(this)
                    .setPhoto(R.drawable.android)
                    .setCover(R.drawable.sungbin)
                    .setName(R.string.my_name)
                    .setSubTitle(R.string.my_activity)
                    .setBrief(R.string.my_age)
                    .setAppIcon(R.drawable.icon)
                    .setAppName(R.string.app_name)
                    .addGooglePlayStoreLink(R.string.my_store_id)
                    .addGitHubLink(R.string.my_github_id)
                    .addFacebookLink(R.string.my_facebook_id)
                    .addEmailLink(R.string.email_google)
                    .addYoutubeChannelLink(R.string.my_youtube_id)
                    .addFiveStarsAction()
                    .setVersionNameAsAppSubTitle()
                    .addShareAction(R.string.nav_header_title)
                    .setWrapScrollView(true)
                    .setLinksAnimated(true)
                    .setShowAsCard(true)
                    .build();*/
            dialog.setTitle("준비중...");
            dialog.setMessage("개발중...");
            dialog.show();
        }
        catch(Exception e){
            Utils.toast(this, e.toString());
        }
    }

    public static void addScript(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(act);
        dialog.setTitle(R.string.add_script);

        final TextInputLayout textLayout = new TextInputLayout(act);
        textLayout.setCounterEnabled(true);

        final TextInputEditText inputTitle = new TextInputEditText(act);
        inputTitle.setHint(R.string.input_email_title);

        textLayout.addView(inputTitle);

        dialog.setView(DialogUtils.makeMarginLayout(act.getResources(), act, textLayout));
        dialog.setPositiveButton("추가", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                Intent intent = new Intent(act, ScriptEdit.class);
                switch(langType){
                    case 0:
                        intent.putExtra("code", "null");
                        intent.putExtra("language", "Js");
                        intent.putExtra("name", inputTitle.getText().toString());
                        act.startActivity(intent);
                        break;
                    case 1:
                        intent.putExtra("code", "null");
                        intent.putExtra("language", "Coffee");
                        intent.putExtra("name", inputTitle.getText().toString());
                        act.startActivity(intent);
                        break;
                    case 2:
                        intent.putExtra("code", "null");
                        intent.putExtra("language", "lua");
                        intent.putExtra("name", inputTitle.getText().toString());
                        act.startActivity(intent);
                        break;
                    case 3:{
                        final int primary = Color.parseColor(Utils.readData(act, "primary", "#42A5F5"));
                        final int accent = Color.parseColor(Utils.readData(act, "accent", "#80D6FF"));
                        final Context ctx = act;
                        roomTypeStr = "";
                        msgTypeStr = "";

                        ColorStateList colorStateList = new ColorStateList(new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{android.R.attr.state_enabled}}, new int[]{accent, primary});

                        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                        builder.setTitle(R.string.add_simple_script);

                        int p = Utils.dip2px(act, 7);
                        LinearLayout layout = new LinearLayout(ctx);
                        layout.setPadding(p, p, p, p);
                        layout.setOrientation(LinearLayout.VERTICAL);

                        final RadioGroup roomType = new RadioGroup(ctx);
                        roomType.setOrientation(RadioGroup.VERTICAL);

                        RadioButton isGroupChat = new RadioButton(ctx);
                        isGroupChat.setText(R.string.only_group_room);
                        isGroupChat.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                roomTypeStr = "true";
                            }
                        });
                        if(Build.VERSION.SDK_INT >= 21) isGroupChat.setButtonTintList(colorStateList);
                        roomType.addView(isGroupChat);

                        RadioButton isGroupChatNot = new RadioButton(ctx);
                        isGroupChatNot.setText(R.string.only_ppl_room);
                        isGroupChatNot.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                roomTypeStr = "false";
                            }
                        });
                        if(Build.VERSION.SDK_INT >= 21) isGroupChatNot.setButtonTintList(colorStateList);
                        roomType.addView(isGroupChatNot);

                        RadioButton all = new RadioButton(ctx);
                        all.setText(R.string.all_room);
                        all.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                roomTypeStr = "all";
                            }
                        });
                        if(Build.VERSION.SDK_INT >= 21) all.setButtonTintList(colorStateList);
                        roomType.addView(all);
                        layout.addView(roomType);

                        final View view = new View(ctx);
                        int dividerHeight = (int) act.getResources().getDisplayMetrics().density;
                        view.setBackgroundColor(Color.GRAY);
                        view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));
                        layout.addView(view);

                        final RadioGroup msgType = new RadioGroup(ctx);
                        msgType.setOrientation(RadioGroup.VERTICAL);

                        RadioButton equals = new RadioButton(ctx);
                        equals.setText(R.string.only_msg_equals);
                        equals.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                msgTypeStr = "equals";
                            }
                        });
                        if(Build.VERSION.SDK_INT >= 21) equals.setButtonTintList(colorStateList);
                        msgType.addView(equals);

                        RadioButton contains = new RadioButton(ctx);
                        contains.setText(R.string.only_contains_msg);
                        contains.setOnClickListener(new View.OnClickListener(){
                            @Override
                            public void onClick(View view){
                                msgTypeStr = "contains";
                            }
                        });
                        if(Build.VERSION.SDK_INT >= 21) contains.setButtonTintList(colorStateList);
                        msgType.addView(contains);
                        layout.addView(msgType);

                        final EditText sender = new EditText(ctx);
                        sender.setHint(R.string.input_sender_name);
                        setCursorColor(sender, primary);
                        layout.addView(sender);

                        final EditText room = new EditText(ctx);
                        room.setHint(R.string.input_room_name);
                        setCursorColor(room, primary);
                        layout.addView(room);

                        final EditText msg = new EditText(ctx);
                        msg.setHint(R.string.input_msg);
                        setCursorColor(msg, primary);
                        layout.addView(msg);

                        final EditText reply = new EditText(ctx);
                        reply.setHint(R.string.input_reply_msg);
                        setCursorColor(reply, primary);
                        layout.addView(reply);

                        builder.setView(DialogUtils.makeMarginLayout(act.getResources(), act, layout));
                        builder.setNegativeButton("취소", null);
                        builder.setPositiveButton("추가", (dialogInterface1, i1) -> {
                            if(StringUtils.isBlank(msgTypeStr)||StringUtils.isBlank(roomTypeStr)){
                                Utils.toast(act, ctx.getString(R.string.plz_check_radio_btn), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                            }
                            else if(StringUtils.isBlank(reply.getText().toString())){
                                Utils.toast(act, ctx.getString(R.string.please_input_reply_msg), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                            }
                            else{
                                String name1 = inputTitle.getText().toString();
                                Utils.createFolder("simple/"+name1);
                                Utils.save("simple/"+name1+"/RoomType.data", roomTypeStr);
                                Utils.save("simple/"+name1+"/MsgType.data", msgTypeStr);
                                Utils.save("simple/"+name1+"/Room.data", room.getText().toString());
                                Utils.save("simple/"+name1+"/Sender.data", sender.getText().toString());
                                Utils.save("simple/"+name1+"/Msg.data", msg.getText().toString());
                                Utils.save("simple/"+name1+"/Reply.data", reply.getText().toString());
                                Utils.toast(act, act.getString(R.string.sucess_add), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                                MainActivity.adpater.notifyDataSetChanged();
                            }
                        });
                        builder.show();
                    }
                    break;
                }
            }
        });
        dialog.show();
    }

    public static void addNewScript(){
        try{
            if(!Utils.toBoolean(Utils.readData(act, "UseOldHome", "false"))){
                addScript();
                return;
            }
            
            lang = "";
            error = false;
            final int primary = Color.parseColor(Utils.readData(act, "primary", "#42A5F5"));
            final int accent = Color.parseColor(Utils.readData(act, "accent", "#80D6FF"));

            ColorStateList colorStateList = new ColorStateList(new int[][]{
                    new int[]{-android.R.attr.state_enabled},
                    new int[]{android.R.attr.state_enabled}},
                    new int[]{accent, primary});

            AlertDialog.Builder dialog = new AlertDialog.Builder(act);
            dialog.setTitle(R.string.add_script);

            int p = Utils.dip2px(act, 7);

            LinearLayout layout = new LinearLayout(act);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(p, p, p, p);

            TextView language = new TextView(act);
            language.setText(R.string.set_script_leng);
            language.setTextColor(Color.BLACK);
            layout.addView(language);

            RadioGroup group = new RadioGroup(act);
            group.setOrientation(RadioGroup.VERTICAL);

            RadioButton js = new RadioButton(act);
            js.setText(R.string.js_name);
            js.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    lang = "Js";
                }
            });
            if(Build.VERSION.SDK_INT>=21) js.setButtonTintList(colorStateList);
            group.addView(js);

            RadioButton coffee = new RadioButton(act);
            coffee.setText(R.string.coffee_name);
            coffee.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    lang = "Coffee";
                }
            });
            if(Build.VERSION.SDK_INT>=21) coffee.setButtonTintList(colorStateList);
            group.addView(coffee);

            RadioButton lua = new RadioButton(act);
            lua.setText(R.string.lua_name);
            lua.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    lang = "lua";
                }
            });
            if(Build.VERSION.SDK_INT>=21) lua.setButtonTintList(colorStateList);
            group.addView(lua);

            RadioButton simple = new RadioButton(act);
            simple.setText(R.string.simple_name);
            simple.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    lang = "simple";
                }
            });
            if(Build.VERSION.SDK_INT>=21) simple.setButtonTintList(colorStateList);
            group.addView(simple);

            layout.addView(group);

            final TextInputLayout name = new TextInputLayout(act);
            name.setFocusableInTouchMode(true);
            name.setCounterEnabled(true);
            name.setErrorEnabled(true);
            name.setCounterMaxLength(20);
            name.setPadding(0, Utils.dip2px(act, 2), 0, 0);

            final TextInputEditText inputnName = new TextInputEditText(act);
            inputnName.setHint(R.string.add_script_name);
            inputnName.addTextChangedListener(new TextWatcher(){
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after){

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count){

                }

                @Override
                public void afterTextChanged(Editable s){
                    if(s.length()>20){
                        name.setError(act.getString(R.string.script_name_max));
                        error = true;
                    }
                    else{
                        name.setError(null);
                        error = false;
                    }
                }
            });
            name.addView(inputnName);
            layout.addView(name);

            dialog.setNegativeButton("취소", null);
            dialog.setPositiveButton("추가", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    if(lang.isEmpty()){
                        Utils.toast(act, act.getString(R.string.script_leng_type),
                                FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                        return;
                    }
                    else{
                        if(!inputnName.getText().toString().isEmpty()){
                            if(!inputnName.getText().toString().contains(".")){
                                if(!error){
                                    if(!lang.equals("simple")){
                                        Intent intent = new Intent(act, ScriptEdit.class);
                                        intent.putExtra("code", "null");
                                        intent.putExtra("language", lang);
                                        intent.putExtra("name", inputnName.getText().toString());
                                        act.startActivity(intent);
                                    }
                                    else{
                                        final int primary = Color.parseColor(Utils.readData(act, "primary", "#42A5F5"));
                                        final int accent = Color.parseColor(Utils.readData(act, "accent", "#80D6FF"));
                                        final Context ctx = act;
                                        roomTypeStr = "";
                                        msgTypeStr = "";

                                        ColorStateList colorStateList = new ColorStateList(
                                                new int[][]{new int[]{-android.R.attr.state_enabled},
                                                        new int[]{android.R.attr.state_enabled}},
                                                new int[]{accent, primary});

                                        AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                                        dialog.setTitle(R.string.add_simple_script);

                                        int p = Utils.dip2px(act, 7);
                                        LinearLayout layout = new LinearLayout(ctx);
                                        layout.setPadding(p, p, p, p);
                                        layout.setOrientation(LinearLayout.VERTICAL);

                                        final RadioGroup roomType = new RadioGroup(ctx);
                                        roomType.setOrientation(RadioGroup.VERTICAL);

                                        RadioButton isGroupChat = new RadioButton(ctx);
                                        isGroupChat.setText(R.string.only_group_room);
                                        isGroupChat.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                roomTypeStr = "true";
                                            }
                                        });
                                        if(Build.VERSION.SDK_INT >= 21) isGroupChat.setButtonTintList(colorStateList);
                                        roomType.addView(isGroupChat);

                                        RadioButton isGroupChatNot = new RadioButton(ctx);
                                        isGroupChatNot.setText(R.string.only_ppl_room);
                                        isGroupChatNot.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                roomTypeStr = "false";
                                            }
                                        });
                                        if(Build.VERSION.SDK_INT >= 21) isGroupChatNot.setButtonTintList(colorStateList);
                                        roomType.addView(isGroupChatNot);

                                        RadioButton all = new RadioButton(ctx);
                                        all.setText(R.string.all_room);
                                        all.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                roomTypeStr = "all";
                                            }
                                        });
                                        if(Build.VERSION.SDK_INT >= 21) all.setButtonTintList(colorStateList);
                                        roomType.addView(all);
                                        layout.addView(roomType);

                                        final View view = new View(ctx);
                                        int dividerHeight = (int) act.getResources().getDisplayMetrics().density;
                                        view.setBackgroundColor(Color.GRAY);
                                        view.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));
                                        layout.addView(view);

                                        final RadioGroup msgType = new RadioGroup(ctx);
                                        msgType.setOrientation(RadioGroup.VERTICAL);

                                        RadioButton equals = new RadioButton(ctx);
                                        equals.setText(R.string.only_msg_equals);
                                        equals.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                msgTypeStr = "equals";
                                            }
                                        });
                                        if(Build.VERSION.SDK_INT >= 21) equals.setButtonTintList(colorStateList);
                                        msgType.addView(equals);

                                        RadioButton contains = new RadioButton(ctx);
                                        contains.setText(R.string.only_contains_msg);
                                        contains.setOnClickListener(new View.OnClickListener(){
                                            @Override
                                            public void onClick(View view){
                                                msgTypeStr = "contains";
                                            }
                                        });
                                        if(Build.VERSION.SDK_INT >= 21) contains.setButtonTintList(colorStateList);
                                        msgType.addView(contains);
                                        layout.addView(msgType);

                                        final EditText sender = new EditText(ctx);
                                        sender.setHint(R.string.input_sender_name);
                                        setCursorColor(sender, primary);
                                        layout.addView(sender);

                                        final EditText room = new EditText(ctx);
                                        room.setHint(R.string.input_room_name);
                                        setCursorColor(room, primary);
                                        layout.addView(room);

                                        final EditText msg = new EditText(ctx);
                                        msg.setHint(R.string.input_msg);
                                        setCursorColor(msg, primary);
                                        layout.addView(msg);

                                        final EditText reply = new EditText(ctx);
                                        reply.setHint(R.string.input_reply_msg);
                                        setCursorColor(reply, primary);
                                        layout.addView(reply);

                                        FrameLayout container = new FrameLayout(ctx);
                                        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                                        );

                                        params.leftMargin = act.getResources().getDimensionPixelSize(R.dimen.fab_margin);
                                        params.rightMargin = act.getResources().getDimensionPixelSize(R.dimen.fab_margin);

                                        layout.setLayoutParams(params);
                                        container.addView(layout);

                                        ScrollView scrollView = new ScrollView(ctx);
                                        scrollView.addView(container);

                                        dialog.setView(scrollView);
                                        dialog.setNegativeButton("취소", null);
                                        dialog.setPositiveButton("추가", (dialogInterface1, i1) -> {
                                            if(StringUtils.isBlank(msgTypeStr)||StringUtils.isBlank(roomTypeStr)){
                                                Utils.toast(act, ctx.getString(R.string.plz_check_radio_btn), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                                            }
                                            else if(StringUtils.isBlank(reply.getText().toString())){
                                                Utils.toast(act, ctx.getString(R.string.please_input_reply_msg), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                                            }
                                            else{
                                                String name1 = inputnName.getText().toString();
                                                Utils.createFolder("simple/"+name1);
                                                Utils.save("simple/"+name1+"/RoomType.data", roomTypeStr);
                                                Utils.save("simple/"+name1+"/MsgType.data", msgTypeStr);
                                                Utils.save("simple/"+name1+"/Room.data", room.getText().toString());
                                                Utils.save("simple/"+name1+"/Sender.data", sender.getText().toString());
                                                Utils.save("simple/"+name1+"/Msg.data", msg.getText().toString());
                                                Utils.save("simple/"+name1+"/Reply.data", reply.getText().toString());
                                                Utils.toast(act, act.getString(R.string.sucess_add), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                                                MainActivity.adpater.notifyDataSetChanged();
                                            }
                                        });

                                        alert = dialog.create();
                                        dialog.show();
                                    }
                                }
                                else Utils.toast(act, act.getString(R.string.script_name_can_length), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                            }
                            else Utils.toast(act, act.getString(R.string.script_name_is_warng), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                        }
                        else Utils.toast(act, act.getString(R.string.plz_input_script_name), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    }
                }
            });

            dialog.setView(DialogUtils.makeMarginLayout(act.getResources(), act, layout));
            alert = dialog.create();

            dialog.show();
        }
        catch(Exception e){
            Utils.error(act, e);
        }
    }

    public void setThemeDialog(){
        try{
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("테마 설정");

            int p = Utils.dip2px(getApplicationContext(), 7);

            GridLayout grid = new GridLayout(this);
            grid.setPadding(p, p, p, p);
            grid.setColumnCount(4);
            grid.setRowCount(5);
            grid.setOrientation(GridLayout.HORIZONTAL);

            String[] colorName = "RED\n,PINK\n,PURPLE\n,DEEP\nPURPLE,INDIGO\n,BLUE\n,LIGHT\nBLUE,CYAN\n,TEAL\n,GREEN\n,LIGHT\nGREEN,LIME\n,YELLOW\n,AMBER\n,ORANGE\n,DEEP\nORANGE,BROWN\n,GREY\n,LIGHT\nGREY,DEFAULT\n".split(",");
            String[] colorCode = "e84e40,f48fb1,ce93d8,9575cd,9fa8da,738ffe,81d4fa,4dd0e1,4db6ac,42bd41,9ccc65,cddc39,ffeb3b,ffc107,ffa726,ff7043,795548,9e9e9e,cfd8dc,42A5F5".split(",");

            final Button[] buttons = new Button[20];

            for(color = 0; color<20; color++){
                buttons[color] = new Button(this);
                buttons[color].setId(color);
                buttons[color].setText(colorName[color]);
                buttons[color].setTextColor(Color.WHITE);
                buttons[color].setBackgroundColor(Color.parseColor("#"+colorCode[color]));
                buttons[color].setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        setThemeColor(view.getId());
                    }
                });
                grid.addView(buttons[color]);
            }

            HorizontalScrollView scroll = new HorizontalScrollView(this);
            scroll.addView(grid);
            scroll.setPadding(p, p, p, p);
            scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

            dialog.setView(scroll);
            dialog.show();
        }
        catch(Exception e){
            Utils.error(MainActivity.this, e);
        }
    }

    public void setThemeColor(int tag){
        switch(tag){
            case 0:
                setColor("e84e3c", "af1113", "ff8168");
                break;
            case 1:
                setColor("f48fb1", "bf5f82", "ffc1e3");
                break;
            case 2:
                setColor("ce93d8", "9c64a6", "ffc4ff");
                break;
            case 3:
                setColor("9575cd", "65499c", "c7a4ff");
                break;
            case 4:
                setColor("9fa8da", "6f79a8", "d1d9ff");
                break;
            case 5:
                setColor("738ffe", "3862ca", "a9bfff");
                break;
            case 6:
                setColor("81d4fa", "4ba3c7", "b6ffff");
                break;
            case 7:
                setColor("4dd0e1", "009faf", "88ffff");
                break;
            case 8:
                setColor("4db6ac", "00867d", "82e9de");
                break;
            case 9:
                setColor("42bd41", "008c09", "7af071");
                break;
            case 10:
                setColor("9ccc65", "6b9b37", "cfff95");
                break;
            case 11:
                setColor("cddc39", "99aa00", "ffff6e");
                break;
            case 12:
                setColor("ffeb3b", "c8b900", "ffff72");
                break;
            case 13:
                setColor("ffc107", "c79100", "fff350");
                break;
            case 14:
                setColor("ffa726", "c77800", "ffd95b");
                break;
            case 15:
                setColor("ff7043", "c63f17", "ffa270");
                break;
            case 16:
                setColor("775447", "4a2b20", "a78172");
                break;
            case 17:
                setColor("9e9e9e", "707070", "cfcfcf");
                break;
            case 18:
                setColor("ced7db", "9da6a9", "ffffff");
                break;
            case 19:
                setColor("42A5F5", "0077C2", "80D6FF");
                break;
        }
    }

    public void setColor(String primary, String primaryDark, String accent){
        Utils.saveData(getApplicationContext(), "primary", "#"+primary);
        Utils.saveData(getApplicationContext(), "primaryDark", "#"+primaryDark);
        Utils.saveData(getApplicationContext(), "accent", "#"+accent);
        Utils.saveData(getApplicationContext(), "theme change", "true");
        Utils.restart(getApplicationContext());
    }

    public static void setCursorColor(EditText view, @ColorInt int color) {
        try {
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);

            Drawable drawable = ContextCompat.getDrawable(view.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
            view.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

            view.setHighlightColor(color);
        }
        catch(Exception e) {
            return;
        }
    }

}