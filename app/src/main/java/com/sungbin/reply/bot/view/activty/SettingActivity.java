package com.sungbin.reply.bot.view.activty;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.Constants;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.fsn.cauly.CaulyAdInfo;
import com.fsn.cauly.CaulyAdInfoBuilder;
import com.fsn.cauly.CaulyAdView;
import com.fsn.cauly.CaulyAdViewListener;
import com.fsn.cauly.CaulyInterstitialAd;
import com.fsn.cauly.CaulyInterstitialAdListener;
import com.rarepebble.colorpicker.ColorObserver;
import com.rarepebble.colorpicker.ColorPickerView;
import com.rarepebble.colorpicker.ObservableColor;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.utils.AppStorage;
import com.sungbin.reply.bot.utils.Utils;

import java.lang.reflect.Field;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingActivity extends AppCompatActivity implements BillingProcessor.IBillingHandler, CaulyAdViewListener, CaulyInterstitialAdListener{

    private AlertDialog alert = null;
    private CaulyAdView javaAdView = null;
    private Switch fixScriptCantOn, useOldMain, useOldDebug, notCheck, notCoffee, notToast, autoSave, notHighLight, notErrorHighLight, errorBotOff;
    private SeekBar sourceSizePrograss, htmlParseTimePrograss;
    private EditText clientId, clientSecret, sourceSizeEditText, htmlParseTimeEditText, packageList, roomBlackList, senderBlackList;
    private Button getKeyWay, removeAd, showAd, setAppTheme, setHightLightTheme, seleteApp, notiPermission, dataClear, showTuTo;
    private TextView preview;
    private boolean showInterstitial = false;
    private LinearLayout rootView;

    private AppStorage storage;
    private BillingProcessor bp;
    private SweetAlertDialog pDialog = null;
    private PackageManager pm = null;
    private IAAdapter mAdapter = null;
    private ArrayList checked = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Activity activity = this;
        storage = new AppStorage(getApplicationContext());

        bp = new BillingProcessor(this, getString(R.string.google_buy_api_key), this);
        bp.initialize();

        mAdapter = new IAAdapter(this);

        fixScriptCantOn = findViewById(R.id.fixScriptCantOn);
        useOldMain = findViewById(R.id.useOldMain);
        useOldDebug = findViewById(R.id.useOldDebug);
        notCheck = findViewById(R.id.notCheck);
        notCoffee = findViewById(R.id.notCoffee);
        notToast = findViewById(R.id.notToast);
        autoSave = findViewById(R.id.autoSave);
        notHighLight = findViewById(R.id.notHighLight);
        notErrorHighLight = findViewById(R.id.notErrorHighLight);
        errorBotOff = findViewById(R.id.errorBotOff);

        sourceSizePrograss = findViewById(R.id.sourceSizePrograss);
        htmlParseTimePrograss = findViewById(R.id.htmlParseTimePrograss);
        clientId = findViewById(R.id.clientId);
        clientSecret = findViewById(R.id.clientSecret);

        sourceSizeEditText = findViewById(R.id.sourceSizeEditText);
        htmlParseTimeEditText = findViewById(R.id.htmlParseTimeEditText);
        packageList = findViewById(R.id.packageList);
        roomBlackList = findViewById(R.id.roomBlackList);
        senderBlackList = findViewById(R.id.senderBlackList);

        removeAd = findViewById(R.id.removeAd);
        showAd = findViewById(R.id.showAd);
        setAppTheme = findViewById(R.id.appTheme);
        setHightLightTheme = findViewById(R.id.sourceTheme);
        seleteApp = findViewById(R.id.seleteApp);
        notiPermission = findViewById(R.id.notiRead);
        dataClear = findViewById(R.id.dataClear);
        showTuTo = findViewById(R.id.showTuTo);
        getKeyWay = findViewById(R.id.getKeyWay);

        preview = findViewById(R.id.previewSize);

        CaulyAdInfo adInfo = new CaulyAdInfoBuilder(getString(R.string.ad_id)).
                effect("TopSlide").
                bannerHeight("Fixed_50").
                build();

        javaAdView = new CaulyAdView(this);
        javaAdView.setAdInfo(adInfo);
        javaAdView.setAdViewListener(this);

        rootView = findViewById(R.id.adLayout);
        rootView.addView(javaAdView);

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
            packageList.getBackground().mutate().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_ATOP);
            roomBlackList.getBackground().mutate().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_ATOP);
            senderBlackList.getBackground().mutate().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_ATOP);
            notCheck.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notCheck.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            notCoffee.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notCoffee.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            notCheck.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notToast.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notToast.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            autoSave.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            autoSave.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            notHighLight.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notHighLight.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            notErrorHighLight.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            notErrorHighLight.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            errorBotOff.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            errorBotOff.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            sourceSizePrograss.getProgressDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            sourceSizePrograss.getThumb().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            htmlParseTimePrograss.getProgressDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            htmlParseTimePrograss.getThumb().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
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

        seleteApp.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                new AppTask().execute();
            }
        });

        dataClear.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view){
                Utils.clearData(getApplicationContext());
                FancyToast.makeText(getApplicationContext(), getString(R.string.success_data_clear), FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show();
                return true;
            }
        });

        dataClear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.toast(SettingActivity.this,
                        getString(R.string.push_btn_long),
                        FancyToast.LENGTH_SHORT, FancyToast.WARNING);
            }
        });

        showTuTo.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.saveData(getApplicationContext(), "isFirstStart", "true");

                SharedPreferences pref = getApplicationContext().getSharedPreferences("material_showcaseview_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();

                editor.putInt("status_버튼 설명", 0);
                editor.commit();
                Utils.toast(SettingActivity.this, "어플 재시작시 튜토리얼이 표시됩니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
            }
        });

        notiPermission.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });

        setAppTheme.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final Context ctx = SettingActivity.this;

                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                dialog.setTitle("유형 선택");

                int p = Utils.dip2px(getApplicationContext(), 7);
                LinearLayout layout = new LinearLayout(ctx);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setWeightSum(3);
                layout.setPadding(p, p, p, p);

                final Button primary = new Button(ctx);
                primary.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                primary.setText("Primary");
                primary.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("primary", primary);
                    }
                });
                layout.addView(primary);

                final Button primaryDark = new Button(ctx);
                primaryDark.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                primaryDark.setText("PrimaryDark");
                primaryDark.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("primaryDark", primaryDark);
                    }
                });
                layout.addView(primaryDark);

                final Button accent = new Button(ctx);
                accent.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                accent.setText("Accent");
                accent.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("accent", accent);
                    }
                });
                layout.addView(accent);

                dialog.setNegativeButton("취소", null);
                dialog.setNeutralButton("기본값으로", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){

                    }
                });
                dialog.setPositiveButton("적용", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        Utils.restart(ctx);
                    }
                });
                dialog.setView(layout);
                dialog.show();
            }
        });

        setHightLightTheme.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                final Context ctx = SettingActivity.this;

                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                dialog.setTitle("유형 선택");

                int p = Utils.dip2px(getApplicationContext(), 7);
                LinearLayout layout = new LinearLayout(ctx);
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setWeightSum(4);
                layout.setPadding(p, p, p, p);

                final Button primary = new Button(ctx);
                primary.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                primary.setText("주석");
                primary.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("green", primary);
                    }
                });
                layout.addView(primary);

                final Button primaryDark = new Button(ctx);
                primaryDark.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                primaryDark.setText("메소드");
                primaryDark.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("blue", primaryDark);
                    }
                });
                layout.addView(primaryDark);

                final Button accent = new Button(ctx);
                accent.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                accent.setText("숫자");
                accent.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("red", accent);
                    }
                });
                layout.addView(accent);

                final Button grown = new Button(ctx);
                grown.setLayoutParams(new android.widget.LinearLayout.LayoutParams(-1, -2, 1));
                grown.setText("문자열");
                grown.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View view){
                        themeSet("brown", grown);
                    }
                });
                layout.addView(grown);

                dialog.setNegativeButton("취소", null);
                dialog.setNeutralButton("기본값으로", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){

                    }
                });
                dialog.setPositiveButton("적용", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i){
                        Utils.restart(ctx);
                    }
                });
                dialog.setView(layout);
                dialog.show();
            }
        });

        fixScriptCantOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                if(b) Utils.toast(SettingActivity.this,
                        getResources().getString(R.string.apply_app_restart),
                        FancyToast.LENGTH_SHORT, FancyToast.INFO);
                Utils.createFolder("AppData");
                Utils.save("AppData/FixScriptCantOn", b+"");
            }
        });
        useOldMain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                if(b) Utils.toast(SettingActivity.this,
                        getResources().getString(R.string.apply_app_restart),
                        FancyToast.LENGTH_SHORT, FancyToast.INFO);
                Utils.saveData(getApplicationContext(), "UseOldHome", b+"");
            }
        });
        useOldDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "useOldDebug", b+"");
            }
        });
        notCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "NotUseScriptCheck", b+"");
            }
        });
        notCoffee.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "NotUseCoffeeScript", b+"");
            }
        });
        notToast.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "ShowToastEasterEgg", b+"");
            }
        });
        autoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "AutoSave", b+"");
            }
        });
        notHighLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "NotHighLight", b+"");
            }
        });
        notErrorHighLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "NotErrorHightLight", b+"");
            }
        });
        errorBotOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b){
                Utils.saveData(getApplicationContext(), "ErrorBotOff", b+"");
            }
        });

        sourceSizePrograss.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b){
                sourceSizeEditText.setText(i+"");
                Utils.saveData(getApplicationContext(), "SourceSize", i+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });
        htmlParseTimePrograss.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b){
                htmlParseTimeEditText.setText(i+"");
                Utils.saveData(getApplicationContext(), "HtmlParseTime", i+"");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });

        sourceSizeEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){
                try{
                    sourceSizeEditText.setSelection(sourceSizeEditText.getText().length());
                    int i = Integer.parseInt(editable.toString());
                    if(i<1||i>50){
                        Utils.toast(SettingActivity.this, getString(R.string.plz_one_to_fivefive), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    }
                    else{
                        sourceSizePrograss.setProgress(i);
                        preview.setTextSize(i);
                        Utils.saveData(getApplicationContext(), "SourceSize", i+"");
                    }
                }
                catch(Exception e){
                    Utils.toast(SettingActivity.this, getString(R.string.plz_one_to_fivefive), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                }
            }
        });
        htmlParseTimeEditText.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void afterTextChanged(Editable editable){
                try{
                    htmlParseTimeEditText.setSelection(htmlParseTimeEditText.getText().length());
                    int i = Integer.parseInt(editable.toString());
                    if(i<1||i>10){
                        Utils.toast(SettingActivity.this, getString(R.string.plz_one_to_ten), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    }
                    else{
                        htmlParseTimePrograss.setProgress(i);
                        Utils.saveData(getApplicationContext(), "HtmlParseTime", i+"");
                    }
                }
                catch(Exception e){
                    Utils.toast(SettingActivity.this, getString(R.string.plz_one_to_fivefive), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                }
            }
        });

        removeAd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                bp.consumePurchase("remove_ad");
                bp.purchase(SettingActivity.this, "remove_ad");
               /* if (storage.purchasedRemoveAds()) {
                    Utils.toast(getApplicationContext(),
                            getString(R.string.already_buy),
                            FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                } else {
                    bp.purchase(SettingActivity.this, "remove_ad");
                }*/
            }
        });

        showAd.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Utils.toast(SettingActivity.this, getString(R.string.thank_you), FancyToast.LENGTH_LONG, FancyToast.SUCCESS);
                CaulyAdInfo adInfo = new CaulyAdInfoBuilder(getString(R.string.ad_id)).build();
                // 전면 광고 생성
                CaulyInterstitialAd interstial = new CaulyInterstitialAd();
                interstial.setAdInfo(adInfo);
                interstial.setInterstialAdListener(SettingActivity.this);
                interstial.requestInterstitialAd(SettingActivity.this);
                showInterstitial = true;
            }
        });

        boolean fixScriptCantOnBool, useOldMainBool, useOldDebugBool, notScriptCheck, notCoffeeBool, toastBool, autoSaveBool, notHightLightBool, notErrorHightLightBool, errorBotOffBool;
        int sourceSizeInt, htmlParseTimeInt;
        String ClientId, ClientSecret, PackageListString, RoomBlackListString, SenderBlackListString;

        fixScriptCantOnBool = Utils.toBoolean(Utils.read("AppData/FixScriptCantOn", "false"));
        useOldMainBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "UseOldHome", "false"));
        useOldDebugBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "useOldView", "false"));
        notScriptCheck = Utils.toBoolean(Utils.readData(getApplicationContext(), "NotUseScriptCheck", "false"));
        notCoffeeBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "NotUseCoffeeScript", "false"));
        toastBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "ShowToastEasterEgg", "false"));
        autoSaveBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "AutoSave", "false"));
        notHightLightBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "NotHighLight", "false"));
        notErrorHightLightBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "NotErrorHightLight", "false"));
        errorBotOffBool = Utils.toBoolean(Utils.readData(getApplicationContext(), "ErrorBotOff", "false"));

        sourceSizeInt = Utils.Number(Utils.readData(getApplicationContext(), "SourceSize", "17"));
        htmlParseTimeInt = Utils.Number(Utils.readData(getApplicationContext(), "HtmlParseTime", "5"));

        PackageListString = Utils.readData(getApplicationContext(), "PackageList", "");
        RoomBlackListString = Utils.readData(getApplicationContext(), "RoomBlackList", "");
        SenderBlackListString = Utils.readData(getApplicationContext(), "SenderBlackList", "");
        ClientId = Utils.readData(getApplicationContext(), "naver-id", "");
        ClientSecret = Utils.readData(getApplicationContext(), "naver-secret", "");

        fixScriptCantOn.setChecked(fixScriptCantOnBool);
        useOldMain.setChecked(useOldMainBool);
        useOldDebug.setChecked(useOldDebugBool);
        notCheck.setChecked(notScriptCheck);
        notCoffee.setChecked(notCoffeeBool);
        notToast.setChecked(toastBool);
        autoSave.setChecked(autoSaveBool);
        notHighLight.setChecked(notHightLightBool);
        notErrorHighLight.setChecked(notErrorHightLightBool);
        errorBotOff.setChecked(errorBotOffBool);

        getKeyWay.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Utils.showWebTab("https://blog.naver.com/sungbin_dev/221647611157", activity);
            }
        });

        sourceSizePrograss.setProgress(sourceSizeInt);
        htmlParseTimePrograss.setProgress(htmlParseTimeInt);

        sourceSizeEditText.setText(sourceSizeInt+"");
        htmlParseTimeEditText.setText(htmlParseTimeInt+"");

        packageList.setText(PackageListString.replace("null", ""));
        roomBlackList.setText(RoomBlackListString.replace("null", ""));
        senderBlackList.setText(SenderBlackListString.replace("null", ""));
        clientSecret.setText(ClientSecret.replace("null", ""));
        clientId.setText(ClientId.replace("null", ""));

        preview.setTextSize(sourceSizeInt);

        setCursorColor(roomBlackList, Color.parseColor(primary));
        setCursorColor(sourceSizeEditText, Color.parseColor(primary));
        setCursorColor(senderBlackList, Color.parseColor(primary));
        setCursorColor(packageList, Color.parseColor(primary));
        setCursorColor(htmlParseTimeEditText, Color.parseColor(primary));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, 1, 0, this.getString(R.string.save_string)).setIcon(R.drawable.ic_save_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == 1){
            String packageListData = packageList.getText().toString();
            String roomBlackListData = roomBlackList.getText().toString();
            String senderBlackListData = senderBlackList.getText().toString();
            String clientIdString = clientId.getText().toString();
            String clientSecretString = clientSecret.getText().toString();
            Utils.saveData(getApplicationContext(), "RoomBlackList", roomBlackListData);
            Utils.saveData(getApplicationContext(), "SenderBlackList", senderBlackListData);
            Utils.saveData(getApplicationContext(), "PackageList", packageListData);
            Utils.saveData(getApplicationContext(), "naver-id", clientIdString);
            Utils.saveData(getApplicationContext(), "naver-secret", clientSecretString);
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView(), R.string.saved, Snackbar.LENGTH_LONG);
            View snackView = snackbar.getView();
            snackView.setBackgroundColor(Color.parseColor(Utils.readData(getApplicationContext(), "primary", "#42A5F5")));
            snackbar.show();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroy() {
        if (bp != null) {
            bp.release();
        }
        super.onDestroy();
    }

    /*----- 인앱 결제 -----*/
    @Override
    public void onPointerCaptureChanged(boolean hasCapture){

    }

    @Override
    public void onProductPurchased(@NonNull String productId, @Nullable TransactionDetails details) {
        Utils.toast(SettingActivity.this, getResources().getString(R.string.thank_buy),
                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
        storage.setPurchasedRemoveAds(bp.isPurchased(productId));
        rootView.removeAllViews();
    }

    @Override
    public void onPurchaseHistoryRestored() {
        // * 구매 정보가 복원되었을때 호출
        // bp.loadOwnedPurchasesFromGoogle() 하면 호출 가능
    }

    @Override
    public void onBillingError(int errorCode, @Nullable Throwable error) {
        if (errorCode != Constants.BILLING_RESPONSE_RESULT_USER_CANCELED) {
            Utils.toast(SettingActivity.this,
                    "구매중 오류가 발생했습니다.\n\nError Code: "+errorCode,
                    FancyToast.LENGTH_SHORT, FancyToast.ERROR);
        }
    }

    @Override
    public void onBillingInitialized() {
        // storage에 구매여부 저장
        storage.setPurchasedRemoveAds(bp.isPurchased("remove_ad"));
    }

    /*----- 전면 광고 -----*/
    @Override
    public void onReceiveInterstitialAd(CaulyInterstitialAd ad, boolean isChargeableAd) {
        // 전면 광고 호출 성공
        if(showInterstitial){
            ad.show();
            showInterstitial = false;
        }
        else ad.cancel();
    }
    @Override
    public void onFailedToReceiveInterstitialAd(CaulyInterstitialAd ad, int errorCode, String errorMsg) {
        // 전면 광고 수신 실패할 경우 호출됨.
    }
    @Override
    public void onClosedInterstitialAd(CaulyInterstitialAd ad) {
        // 전면 광고가 닫기 버튼으로 닫힌 경우 호출됨.
    }
    @Override
    public void onLeaveInterstitialAd(CaulyInterstitialAd arg0){
        // 전면 광고가 뒤로가기 버튼으로 닫힌 경우 호출됨
    }
    /*----- 전면 광고 끝 -----*/

    /*----- 배너 광고 -----*/
    @Override
    public void onReceiveAd(CaulyAdView adView, boolean isChargeableAd) {
        // 광고 수신 성공 & 노출된 경우 호출됨.
        // 수신된 광고가 무료 광고인 경우 isChargeableAd 값이 false 임.
    }
    @Override
    public void onFailedToReceiveAd(CaulyAdView adView, int errorCode, String errorMsg) {
        // 배너 광고 수신 실패할 경우 호출됨.
    }
    @Override
    public void onShowLandingScreen(CaulyAdView adView){
        // 광고 배너를 클릭하여 랜딩 페이지가 열린 경우 호출됨.
    }
    @Override
    public void onCloseLandingScreen(CaulyAdView adView) {
        // 광고 배너를 클릭하여 랜딩 페이지가 닫힌 경우 호출됨.
    }
    /*----- 배너 광고 끝 -----*/

    @Override
    public void onBackPressed(){
        String oldPackageListString = Utils.readData(getApplicationContext(), "PackageList", "");
        String oldRoomBlackListString = Utils.readData(getApplicationContext(), "RoomBlackList", "");
        String oldSenderBlackListString = Utils.readData(getApplicationContext(), "SenderBlackList", "");

        String newPackageListString = packageList.getText().toString();
        String newRoomBlackListString = roomBlackList.getText().toString();
        String newSenderBlackListString =  senderBlackList.getText().toString();

        if(!newPackageListString.equals(oldPackageListString)
                || !newRoomBlackListString.equals(oldRoomBlackListString)
                || !newSenderBlackListString.equals(oldSenderBlackListString)){
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.data_changed, Snackbar.LENGTH_SHORT);
            View snackView = snackbar.getView();
            snackView.setBackgroundColor(Color.parseColor(Utils.readData(getApplicationContext(), "primary", "#42A5F5")));
            snackbar.setAction("바로 닫기", new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    finish();
                }
            }).setActionTextColor(Color.WHITE);
            snackbar.show();
        }
        else super.onBackPressed();
    }

    public void themeSet(final String type, final Button btn){
        alert = null;

        AlertDialog.Builder dialog = new AlertDialog.Builder(SettingActivity.this, R.style.Theme_AppCompat_Translucent);

        int p = Utils.dip2px(getApplicationContext(), 7);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setBackgroundColor(Color.parseColor("#ffffff"));
        layout.setPadding(p, p, p, p);
        layout.setOrientation(LinearLayout.VERTICAL);

        final ColorPickerView picker = new ColorPickerView(SettingActivity.this);
        picker.showAlpha(true);
        picker.showHex(true);
        picker.showPreview(true);
        picker.setColor(Color.parseColor("#00000000"));
        picker.setOriginalColor(Color.parseColor("#00000000"));
        picker.addColorObserver(new ColorObserver(){
            @Override
            public void updateColor(ObservableColor observableColor){
                picker.setOriginalColor(observableColor.getColor());
            }
        });
        layout.addView(picker);

        Button save = new Button(SettingActivity.this);
        save.setText("선택 완료");
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                int color = picker.getColor();
                String hexColor = String.format("#%08X", (0xFFFFFFFF & color));
                Utils.saveData(getApplicationContext(), type, hexColor);
                Utils.saveData(getApplicationContext(), "theme change", "true");
                Utils.toast(SettingActivity.this, getString(R.string.color_select), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                btn.setTextColor(color);
                alert.cancel();
            }
        });
        layout.addView(save);

        dialog.setView(layout);

        alert = dialog.create();
        alert.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        alert.show();
    }

    public void setCursorColor(EditText view, @ColorInt int color) {
        try {
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(view);

            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(view);;

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

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mName;
        public TextView mPacakge;
        public CheckBox checkBox;;
    }

    private class IAAdapter extends BaseAdapter{
        private Context mContext = null;

        private List<ApplicationInfo> mAppList = null;
        private ArrayList<AppInfo> mListData = new ArrayList<AppInfo>();

        public IAAdapter(Context mContext) {
            super();
            this.mContext = mContext;
        }

        @Override
        public int getCount() {
            return mListData.size();
        }

        @Override
        public Object getItem(int arg) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(String.valueOf(position));
        }

        @Override
        public int getViewTypeCount(){
            return super.getViewTypeCount();
        }

        @Override
        public int getItemViewType(int position){
            return super.getItemViewType(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if(convertView == null) {
                holder = new ViewHolder();

                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.app_list_layout, null);

                holder.mIcon =  convertView.findViewById(R.id.app_icon);
                holder.mName = convertView.findViewById(R.id.app_name);
                holder.checkBox = convertView.findViewById(R.id.checkbox);
                holder.mPacakge = convertView.findViewById(R.id.app_package);

                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final AppInfo data = mListData.get(position);

            if(data.mIcon != null) {
                holder.mIcon.setImageDrawable(data.mIcon);
            }

            String primary = Utils.readData(getApplicationContext(), "primary", "#42A5F5");
            String primaryDark = Utils.readData(getApplicationContext(), "primaryDark", "#0077C2");

            ColorStateList colorStateList = new ColorStateList(
                    new int[][] {
                            new int[] { -android.R.attr.state_checked }, // unchecked
                            new int[] {  android.R.attr.state_checked }  // checked
                    },
                    new int[] {
                            Color.parseColor(primary),
                            Color.parseColor(primaryDark)
                    }
            );

            holder.checkBox.setButtonTintList(colorStateList);
            holder.checkBox.setChecked(data.getSelected());
            holder.checkBox.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(data.getSelected()){ //체크 해제
                       data.setSelected(false);
                        String string = data.mAppPackge;
                        if(checked.contains(string)){
                            checked.remove(string);
                        }
                    }
                    else { //체크
                        data.setSelected(true);
                        checked.add(data.mAppPackge);
                    }
                }
            });
            holder.mName.setText(data.mAppNaem);
            holder.mPacakge.setText(data.mAppPackge);

            return convertView;
        }

        public void rebuild() {
            if(mAppList == null) {
                pm = SettingActivity.this.getPackageManager();
                mAppList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_DISABLED_COMPONENTS);
            }

            AppInfo.AppFilter filter = null;

            if(filter != null) {
                filter.init();
            }

            mListData.clear();

            AppInfo addInfo = null;
            ApplicationInfo info = null;
            for(ApplicationInfo app : mAppList) {
                info = app;

                if(filter == null || filter.filterApp(info)) {
                    addInfo = new AppInfo();
                    addInfo.mIcon = app.loadIcon(pm);
                    addInfo.mAppNaem = app.loadLabel(pm).toString();
                    addInfo.mAppPackge = app.packageName;
                    mListData.add(addInfo);
                }
            }

            Collections.sort(mListData, AppInfo.ALPHA_COMPARATOR);
        }
    }

    public void showAppList(final EditText edittext){
        try{
            final Context ctx = SettingActivity.this;
            final ArrayList packageList = new ArrayList();
            AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
            dialog.setTitle("앱 선택");

            ListView list = new ListView(ctx);
            list.setAdapter(mAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> av, View view, int position, long id){
                    String packageStr = ((TextView) view.findViewById(R.id.app_package)).getText().toString();
                    edittext.setText(edittext.getText() + "\n" + packageStr);
                    alert.cancel();
                }
            });

            dialog.setNeutralButton("기본값", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i){
                    edittext.setText("com.messenger.talk");
                }
            });
            dialog.setPositiveButton("추가", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which){
                    String packageStr = edittext.getText().toString();
                    for(int i=0;i<checked.size();i++){
                        packageStr += "\n" + checked.get(i);
                    }
                    edittext.setText(packageStr);
                    alert.cancel();
                }
            });
            dialog.setView(list);

            alert = dialog.create();
            alert.show();
        }
        catch(Exception e){
            Utils.error(SettingActivity.this, e);
        }
    }

    private class AppTask extends AsyncTask<Void, Void, Void>{
        @Override
        protected void onPreExecute() {
            pDialog = new SweetAlertDialog(SettingActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor(Utils.readData(getApplicationContext(), "primary", "#42A5F5")));
            pDialog.setTitleText("\n\n\n앱 목록 불러오는중...");
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            mAdapter.rebuild();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mAdapter.notifyDataSetChanged();
            pDialog.cancel();
            checked.clear();
            showAppList(packageList);
        }
    };

    static class AppInfo {
        interface AppFilter {
            void init();
            boolean filterApp(ApplicationInfo info);
        }

        Drawable mIcon = null;
        String mAppNaem = null;
        String mAppPackge = null;
        private boolean isSelected = false;

        boolean getSelected() {
            return isSelected;
        }

        void setSelected(boolean selected) {
            isSelected = selected;
        }

        static final AppFilter THIRD_PARTY_FILTER = new AppFilter() {
            public void init() {
            }

            @Override
            public boolean filterApp(ApplicationInfo info) {
                if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    return true;
                } else return (info.flags&ApplicationInfo.FLAG_SYSTEM)==0;
            }
        };


        static final Comparator<AppInfo> ALPHA_COMPARATOR = new Comparator<AppInfo>() {
            private final Collator sCollator = Collator.getInstance();
            @Override
            public int compare(AppInfo object1, AppInfo object2) {
                return sCollator.compare(object1.mAppNaem, object2.mAppNaem);
            }
        };
    }

}
