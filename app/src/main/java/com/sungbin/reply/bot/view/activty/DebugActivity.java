package com.sungbin.reply.bot.view.activty;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.adapter.DebugAdapter;
import com.sungbin.reply.bot.dto.DebugItem;
import com.sungbin.reply.bot.listener.KakaoTalkListener;
import com.sungbin.reply.bot.utils.Utils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import gun0912.tedkeyboardobserver.TedKeyboardObserver;

public class DebugActivity extends AppCompatActivity {

    private EditText sender, room, input;
    public static DebugAdapter adapter;
    public static ArrayList items = new ArrayList();
    public static RecyclerView list;
    private int topMargin;
    private boolean isOldView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isOldView = Utils.toBoolean(Utils.readData(getApplicationContext(), "useOldDebug", "false"));
        setContentView(isOldView ? R.layout.activity_debug : R.layout.activity_debug_new);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Intent i = getIntent();
        final String scriptName = i.getStringExtra("name"); //확장자 포함

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        adapter = new DebugAdapter(items, this);

        list = findViewById(R.id.debugView);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setAdapter(adapter);

        final Switch isGroupChat = (Switch) findViewById(R.id.isGroupChat);
        sender = (EditText) findViewById(R.id.senderName);
        room = (EditText) findViewById(R.id.roomName);
        input= (EditText) findViewById(R.id.input);
        final ImageButton send = (ImageButton) findViewById(R.id.send);

        new TedKeyboardObserver(this)
                .listen(isShow -> {
                    list.scrollToPosition(items.size()-1);
                });

        if(!isOldView){
            LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
            BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
            bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState){
                    if(newState==3){ //열림
                        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        params.bottomMargin = bottomSheet.getHeight();
                        params.topMargin = topMargin;

                        list.setLayoutParams(params);
                        list.scrollToPosition(items.size()-1);
                    }
                    else if(newState==4){ //닫힘
                        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        params.bottomMargin = 150;
                        params.topMargin = topMargin;

                        list.setLayoutParams(params);
                        list.scrollToPosition(items.size()-1);
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset){

                }
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
            setCursorColor(input, Color.parseColor(accent));
            isGroupChat.getTrackDrawable().setColorFilter(Color.parseColor(accent), PorterDuff.Mode.SRC_IN);
            isGroupChat.getThumbDrawable().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_IN);
            isGroupChat.setTextColor(Color.parseColor(primaryDark));
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

        send.setOnClickListener(v -> send.post(() -> {
            DebugItem item = new DebugItem(sender.getText().toString(),
                    input.getText().toString(), Gravity.RIGHT);
            items.add(item);
            adapter.notifyDataSetChanged();

            KakaoTalkListener.scriptName = scriptName;
            KakaoTalkListener.callDebugJsResponder(scriptName,
                    getString(R.string.string_debug_mode),
                    input.getText().toString(),
                    room.getText().toString(),
                    isGroupChat.isChecked());

            list.scrollToPosition(items.size()-1);
            input.setText("");
        }));
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if(!isOldView){
            AppBarLayout toolbar = this.findViewById(R.id.toolbar_layout);
            topMargin = toolbar.getHeight();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        menu.add(0, 1, 0, "Clear").setIcon(R.drawable.ic_delete_white_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == 1){
            items.clear();
            adapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }
}