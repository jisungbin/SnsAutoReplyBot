package com.sungbin.reply.bot.view.activty;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.utils.Utils;

import org.apache.commons.lang3.StringUtils;

public class LogActivity extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent i = getIntent();
        final String name = i.getStringExtra("name");

        String data = Utils.readData(getApplicationContext(), "Log/"+name, "");
        data = data.replaceFirst("\n","").replace("\n", "<br>");
        if(StringUtils.isBlank(data)) data = getString(R.string.log_empty);

        final TextView showLog = findViewById(R.id.showLog);
        showLog.setText(Html.fromHtml(data));

        FloatingActionButton delete = (FloatingActionButton) findViewById(R.id.delete);
        delete.setOnClickListener(view -> {
            Utils.saveData(getApplicationContext(), "Log/"+name, "");
            showLog.setText(getString(R.string.log_empty));
        });

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
            delete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(accent)));
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

    }

}
