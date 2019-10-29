package com.sungbin.reply.bot.view.activty;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;

import com.faendir.rhino_android.RhinoAndroidHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.TextViewCompat;

import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.highlighter.JsHighlighter;
import com.sungbin.reply.bot.highlighter.LuaHighlighter;
import com.sungbin.reply.bot.listener.KakaoTalkListener;
import com.sungbin.reply.bot.utils.PrimitiveWrapFactory;
import com.sungbin.reply.bot.utils.Utils;
import com.sungbin.reply.bot.widget.LineNumberEditText;
import com.sungbin.reply.bot.widget.TextViewUndoRedo;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.ScriptableObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimerTask;
import java.util.Timer;

public class ScriptEdit extends AppCompatActivity{

    private LineNumberEditText input;
    private TextViewUndoRedo undoRedo;
    private String name, lang, code;
    private boolean isSaved = false;
    private ActionMode actionMode;
    private String res = null;
    private ArrayList<String> suggestList = new ArrayList<>();

    private class AutoSaveTimer extends TimerTask{
        @Override
        public void run() {
            final String newCode = input.getText().toString();
            if(!name.contains(".")){
                switch(lang){
                    case "Js":
                        Utils.save("javascript/"+name+".js", newCode);
                        break;
                    case "Coffee":
                        Utils.save("coffeescript/"+name+".coffee", newCode);
                        break;
                    case "lua":
                        Utils.save("LuaScript/"+name+".lua", newCode);
                        break;
                }
            }
            else{
                switch(lang){
                    case "Js":
                        Utils.save("javascript/"+name, newCode);
                        break;
                    case "Coffee":
                        Utils.save("coffeescript/"+name, newCode);
                        break;
                    case "lua":
                        Utils.save("LuaScript/"+name, newCode);
                        break;
                }
            }

            Looper.prepare();
            Utils.toast(ScriptEdit.this, "스크립트가 자동저장 되었습니다.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
            Looper.loop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_script_add);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        lang = intent.getStringExtra("language");
        code = intent.getStringExtra("code");

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(name);
        setSupportActionBar(toolbar);

        final TextView autoInput = findViewById(R.id.append_auto);
        autoInput.setVisibility(View.GONE);
        TextViewCompat.setAutoSizeTextTypeWithDefaults(autoInput,
                TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM);

        input = findViewById(R.id.editText);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            Thread thr = new Thread();
            @Override
            public void afterTextChanged(final Editable s){
                String[] data = {"String", "File", "java", "io", "Array", "int", "function", "return", "var", "let", "const", "if", "else", "switch", "for", "while", "do", "break", "continue", "case", "in", "with", "true", "false", "new", "null", "undefined", "typeof", "delete", "try", "catch", "finally", "prototype", "this", "super", "default", "prototype"};
                List<String> list = new ArrayList<String>(Arrays.asList(data));
                try{
                    suggestList.clear();

                    Layout layout = input.getLayout();
                    int selectionStart = Selection.getSelectionStart(input.getText());
                    final String now = s.toString().split("\n")[layout.getLineForOffset(selectionStart)].trim()
                            .split(" ")[s.toString().split("\n")[layout.getLineForOffset(selectionStart)]
                            .trim().split(" ").length - 1];

                    final String all = s.toString();
                    for(int i=0;i<all.split("\n").length;i++){
                        String[] variable = {"var", "const", "let", "function"};
                        String cash = all.split("\n")[i];
                        for(int n=0;n<variable.length;n++){
                            String keyword = variable[n];
                            if(cash.contains(keyword)){
                                String str = cash.split(keyword+" ")[1].split(
                                        keyword.equals("function")?"\\{":"=")[0];
                                //변수 키워드 가져오기
                                if(str.contains(" ")) str = str.split(" ")[0];

                                //문자열 안에 있는지 검사
                                if(cash.split(str)[0].contains("\"")){ //변수키워드 왼쪽에 " 검사
                                    if(checkCharCount(cash.split(str)[0], "\"") == 2){ //"" ~~ -> 문자열 X
                                        suggestList.add(str); //추천리스트에 변수키워드 추가
                                    }
                                }
                                else if(cash.split(str)[1].contains("\"")){ //변수키워드 오른쪽에 " 검사
                                    if(checkCharCount(cash.split(str)[1], "\"") == 2){ //"" ~~ -> 문자열 X
                                        suggestList.add(str); //추천리스트에 변수키워드 추가
                                    }
                                }
                                else suggestList.add(str); //"가 없을땐 바로 추천리스트에 변수키워드 추가
                            }
                        }
                    }

                    for(int i=0;i<list.size();i++){
                        if(list.get(i).startsWith(now) && !list.get(i).equals(now)){
                            suggestList.add(list.get(i));
                        }
                    }

                    if(!suggestList.isEmpty()){
                        if(suggestList.size() == 1){
                            autoInput.setVisibility(View.VISIBLE);
                            autoInput.setText(suggestList.get(0));
                            autoInput.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v){
                                    autoInput.setVisibility(View.GONE);
                                    try{
                                        insert(suggestList.get(0).replace(now, ""));
                                    }
                                    catch(Exception ignored){}
                                }
                            });
                        }
                        else{
                            if(suggestList.size() != list.size() &&
                                    !StringUtils.isBlank(s.toString().split("\n")[layout.getLineForOffset(selectionStart)])){
                                autoInput.setVisibility(View.VISIBLE);
                                autoInput.setText("자동완성");
                                autoInput.setOnClickListener(new View.OnClickListener(){
                                    @Override
                                    public void onClick(View v){
                                        PopupMenu p = new PopupMenu(getApplicationContext(), v);
                                        for(int i=0;i<suggestList.size();i++){
                                            p.getMenu().add(0, i, 0, suggestList.get(i));
                                        }
                                        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener(){
                                            @Override
                                            public boolean onMenuItemClick(MenuItem item){
                                                autoInput.setVisibility(View.GONE);
                                                insert(suggestList.get(item.getItemId()).replace(now, ""));
                                                return false;
                                            }
                                        });
                                        p.show();
                                    }
                                });
                            }
                        }
                    }
                    else {
                        autoInput.setVisibility(View.GONE);
                    }
                }
                catch(Exception ignored){}

                if(thr != null && thr.isAlive()) thr.interrupt();
                thr = new Thread(new Runnable() {
                    @Override
                    public void run(){
                        try{
                            res = preCompile(input.getText().toString());
                            if(res == null){
                                runOnUiThread(() -> {
                                    //오류 X
                                    try{
                                        for(ClickableSpan e : s.getSpans(0, s.length(), ClickableSpan.class)){
                                            s.removeSpan(e);
                                        }
                                    }
                                    catch(Exception e){
                                        e.printStackTrace();
                                    }

                                    if(!Utils.toBoolean(Utils.readData(getApplicationContext(), "NotHighLight", "false"))){
                                        switch(lang){
                                            case "Js":
                                            case "Coffee":
                                                JsHighlighter highlighter = new JsHighlighter(getApplicationContext());
                                                highlighter.apply(s);
                                                break;
                                            case "lua":
                                                LuaHighlighter highlighter2 = new LuaHighlighter(getApplicationContext());
                                                highlighter2.apply(s);
                                                break;
                                        }
                                    }
                                });
                            }
                            else{
                                //오류 O
                                if(Utils.toBoolean(Utils.readData(getApplicationContext(), "NotErrorHightLight", "false"))) return;
                                int line = Integer.parseInt(res.split("#")[1].split("\\)")[0]);
                                String[] str = s.toString().split("\n");
                                int indexStart = 0;
                                int indexEnd = 0;
                                int i = 0;
                                for(i=0;i<line;i++){
                                    indexStart += str[i].length()+1;
                                }

                                indexEnd = indexStart+str[i].length();
                                final int fS = indexStart;
                                final int fE = indexEnd;
                                runOnUiThread(() -> {
                                    ClickableSpan clickableSpan = new ClickableSpan() {
                                        @Override
                                        public void onClick(@NotNull View widget) {

                                        }

                                        @Override
                                        public void updateDrawState(TextPaint textPaint) {
                                            textPaint.setColor(Color.RED); // 해당 텍스트 색상 변경
                                            textPaint.setUnderlineText(true); // 해당 텍스트 언더라인
                                            textPaint.setFakeBoldText(true); // 해당 텍스트 두껍게 처리
                                        }
                                    };
                                    s.setSpan(clickableSpan, fS, fE, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                });
                            }
                        }
                        catch(Throwable e){
                            e.printStackTrace();
                        }
                    }
                });
                thr.start();
            }
        });
        input.setTextSize(Utils.Number(Utils.readData(getApplicationContext(), "SourceSize", "17")));

        undoRedo = new TextViewUndoRedo(input);

        if(!code.equals("null")) input.setText(code);
        else{
            switch(lang){
                case "Js":
                    input.setText("function response(room, msg, sender, isGroupChat, replier, ImageDB, package) {\n	  /*\n	  *String room - 방 이름 리턴\n	  *String msg - 받은 메시지 리턴\n	  *String sender - 발송자 이름 리턴\n	  *boolean isGroupChat - 단체채팅방(오픈채팅방) 인지 리턴\n	  *Notification.Action replier - 노티 액션 리턴\n	  *ImageDB - 이미지 관련 작업 가능\n	  *Package - 앱의 패키지명 리턴*/\n	  \n	  if(msg.trim().equals(\"안녕\")){\n	    replier.reply(\"나도 안녕~!\");\n	  }\n}");
                    break;
                case "Coffee":
                    input.setText("response = (room, msg, sender, isGroupChat, replier, ImageDB, package) -> \n  replier.reply \"하세요~!\" if msg is \"안녕\"");
                    break;
                case "lua":
                    input.setText("function response(room, msg, sender)\nif msg == \"루아\" then\n  Bot.sendChat(room, \"루아!\")\nend\nend");
                    break;
            }
        }

        final FloatingActionButton save = (FloatingActionButton) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final String newCode = input.getText().toString();
                if(StringUtils.isBlank(newCode)){
                    FancyToast.makeText(getApplicationContext(), getString(R.string.plz_input_code), FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                }
                else{
                    boolean notCheckScript = Utils.toBoolean(Utils.readData(getApplicationContext(), "NotUseScriptCheck", "false")); //괄호 개수 체크 안함
                    if(!notCheckScript){
                        int open = checkCharCount(newCode, "{");
                        int close = checkCharCount(newCode, "}");
                        if(open!=close){
                            FancyToast.makeText(getApplicationContext(),
                                    getString(R.string.not_match_code),
                                    FancyToast.LENGTH_SHORT, FancyToast.WARNING, false).show();
                            Utils.toast(ScriptEdit.this, getString(R.string.press_save_long),
                                    FancyToast.LENGTH_LONG, FancyToast.INFO);
                            return;
                        }
                    }
                    if(!name.contains(".")){
                        switch(lang){
                            case "Js":
                                Utils.save("javascript/"+name+".js", newCode);
                                break;
                            case "Coffee":
                                Utils.save("coffeescript/"+name+".coffee", newCode);
                                break;
                            case "lua":
                                Utils.save("LuaScript/"+name+".lua", newCode);
                                break;
                        }
                    }
                    else{
                        switch(lang){
                            case "Js":
                                Utils.save("javascript/"+name, newCode);
                                break;
                            case "Coffee":
                                Utils.save("coffeescript/"+name, newCode);
                                break;
                            case "lua":
                                Utils.save("LuaScript/"+name, newCode);
                                break;
                        }
                    }
                    isSaved = true;
                    FancyToast.makeText(getApplicationContext(), getString(R.string.saved_script), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
                }
            }
        });
        save.setOnLongClickListener(v -> {
            final String newCode = input.getText().toString();
            if(!name.contains(".")){
                switch(lang){
                    case "Js":
                        Utils.save("javascript/"+name+".js", newCode);
                        break;
                    case "Coffee":
                        Utils.save("coffeescript/"+name+".coffee", newCode);
                        break;
                    case "lua":
                        Utils.save("LuaScript/"+name+".lua", newCode);
                        break;
                }
            }
            else{
                switch(lang){
                    case "Js":
                        Utils.save("javascript/"+name, newCode);
                        break;
                    case "Coffee":
                        Utils.save("coffeescript/"+name, newCode);
                        break;
                    case "lua":
                        Utils.save("LuaScript/"+name, newCode);
                        break;
                }
            }
            FancyToast.makeText(getApplicationContext(), getString(R.string.saved_script), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            MainActivity.adpater.notifyDataSetChanged();
            return false;
        });

        if(Utils.toBoolean(Utils.readData(getApplicationContext(), "AutoSave", "false"))){
            Timer timer = new Timer();
            timer.schedule(new AutoSaveTimer(), 300000, 300000);
        }

        ((TextView) findViewById(R.id.action_left_slash)).setText("\\");

        findViewById(R.id.action_indent).setOnClickListener(v -> insert("\t\t\t\t"));
        findViewById(R.id.action_undo).setOnClickListener(v -> undoRedo.undo());
        findViewById(R.id.action_redo).setOnClickListener(v -> undoRedo.redo());
        findViewById(R.id.action_right_big).setOnClickListener(v -> insert("{"));
        findViewById(R.id.action_left_big).setOnClickListener(v -> insert("}"));
        findViewById(R.id.action_right_small).setOnClickListener(v -> insert("("));
        findViewById(R.id.action_left_small).setOnClickListener(v -> insert(")"));
        findViewById(R.id.action_right_slash).setOnClickListener(v -> insert("/"));
        findViewById(R.id.action_left_slash).setOnClickListener(v -> insert("\\"));
        findViewById(R.id.action_big_quote).setOnClickListener(v -> insert("\""));
        findViewById(R.id.action_small_quote).setOnClickListener(v -> insert("'"));
        findViewById(R.id.action_dot).setOnClickListener(v -> insert("."));
        findViewById(R.id.action_end).setOnClickListener(v -> insert(";"));
        findViewById(R.id.action_plus).setOnClickListener(v -> insert("+"));
        findViewById(R.id.action_minus).setOnClickListener(v -> insert("-"));

        final NestedScrollView scrollView = findViewById(R.id.scriptEdit_scrollView);
        scrollView.post(() -> scrollView.scrollTo(0, Utils.Number(Utils.readData(getApplicationContext(),
                name+"ScrollY", "0"))));

        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener(){
            @Override
            public void onScrollChange(NestedScrollView nestedScrollView, int x, int y, int oldx, int oldy){
                Utils.saveData(getApplicationContext(),
                        name+"ScrollY", scrollView.getScrollX() + "");

                if (y > oldy) {
                    //Down
                    save.hide();
                }
                if (y < oldy) {
                    //Up
                    save.show();
                }
            }
        });

        final String primary = Utils.readData(getApplicationContext(), "primary", "#42A5F5");
        final String primaryDark = Utils.readData(getApplicationContext(), "primaryDark", "#0077C2");
        final String accent = Utils.readData(getApplicationContext(), "accent", "#80D6FF");

        setCursorColor(input, Color.parseColor(primary));

        boolean themeChange = Utils.toBoolean(Utils.readData(getApplicationContext(), "theme change", "false"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(primaryDark));
            window.setNavigationBarColor(Color.parseColor(accent));
            toolbar.setBackgroundColor(Color.parseColor(primary));
            input.getBackground().mutate().setColorFilter(Color.parseColor(primary), PorterDuff.Mode.SRC_ATOP);
            save.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(accent)));
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

    public String preCompile(String source){
        try {
            Context parseContext = new RhinoAndroidHelper().enterContext();
            parseContext.setWrapFactory(new PrimitiveWrapFactory());
            parseContext.setLanguageVersion(Context.VERSION_ES6);
            parseContext.setOptimizationLevel(-1);
            ScriptableObject scope = (ScriptableObject) parseContext.initStandardObjects(new ImporterTopLevel(parseContext));
            Script script_real = parseContext.compileString(source, name, 0, null);
            ScriptableObject.defineClass(scope, KakaoTalkListener.Log.class);
            ScriptableObject.defineClass(scope, KakaoTalkListener.Api.class);
            ScriptableObject.defineClass(scope, KakaoTalkListener.Clock.class);
            ScriptableObject.defineClass(scope, KakaoTalkListener.Device.class);
            ScriptableObject.defineClass(scope, KakaoTalkListener.File.class);
            ScriptableObject.defineClass(scope, KakaoTalkListener.Utils.class);
            script_real.exec(parseContext, scope);

            Function func = (Function) scope.get("response", scope);
            Context.exit();
            return null;
        }
        catch(Throwable e){
            return e.getMessage();
        }
    }

    @Override
    public void onBackPressed(){
        if(!code.equals(input.getText().toString()) && !code.equals("null") && !isSaved){
            Snackbar snackbar = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.data_changed, Snackbar.LENGTH_LONG);
            View snackView = snackbar.getView();
            snackView.setBackgroundColor(Color.parseColor(Utils.readData(getApplicationContext(), "primary", "#42A5F5")));
            snackbar.setAction("바로 닫기", view -> {
                MainActivity.adpater.notifyDataSetChanged();
                super.onBackPressed();
            }).setActionTextColor(Color.WHITE);
            snackbar.show();
        }
        else {
            MainActivity.adpater.notifyDataSetChanged();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.script_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case R.id.find:
                replaceText();
                break;
            case R.id.error:
                if(res == null) Utils.toast(ScriptEdit.this, getResources().getString(R.string.error_is_gone),
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                else Utils.toast(ScriptEdit.this, res,
                        FancyToast.LENGTH_LONG, FancyToast.ERROR);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public int checkCharCount(String str, String find){
        return StringUtils.countMatches(str, find);
    }

    public void replaceText(){
        final android.content.Context ctx = ScriptEdit.this;

        final int primary = Color.parseColor(Utils.readData(getApplicationContext(), "primary", "#42A5F5"));
        final int accent = Color.parseColor(Utils.readData(getApplicationContext(), "accent", "#80D6FF"));

        final AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
        dialog.setTitle("텍스트 찾기/바꾸기");

        int p = Utils.dip2px(getApplicationContext(), 7);
        LinearLayout layout = new LinearLayout(ctx);
        layout.setPadding(p, p, p, p);
        layout.setFocusableInTouchMode(true);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextInputLayout find = new TextInputLayout(this);
        find.setCounterEnabled(true);
        find.setPadding(0, Utils.dip2px(getApplicationContext(), 2), 0, 0);

        final TextInputEditText inputFind = new TextInputEditText(this);
        inputFind.setHint(getString(R.string.input_search_string));
        find.addView(inputFind);

        final TextInputLayout replace = new TextInputLayout(this);
        replace.setCounterEnabled(true);
        replace.setPadding(0, Utils.dip2px(getApplicationContext(), 2), 0, 0);

        final TextInputEditText inputReplace = new TextInputEditText(this);
        inputReplace.setHint(getString(R.string.input_replace_string));
        replace.addView(inputReplace);

        final Switch isAll = new Switch(ctx);
        isAll.setText("모두 바꾸기");
        isAll.setTextOff("");
        isAll.setTextOn("");
        isAll.getTrackDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        isAll.getThumbDrawable().setColorFilter(primary, PorterDuff.Mode.SRC_IN);

        final Switch isRegex = new Switch(ctx);
        isRegex.setText("정규식 검색");
        isRegex.getTrackDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        isRegex.getThumbDrawable().setColorFilter(primary, PorterDuff.Mode.SRC_IN);
        isRegex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if(isChecked) {
                    Utils.toast(ScriptEdit.this,
                            "정규식 사용을 체크하실 경우 모두 바꾸기로 진행됩니다.",
                            FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    isAll.setChecked(true);
                    isAll.setClickable(false);
                } else {
                    isAll.setChecked(false);
                    isAll.setClickable(true);
                }
            }
        });
        isRegex.setTextOff("");
        isRegex.setTextOn("");

        layout.addView(find);
        layout.addView(replace);
        layout.addView(isRegex);
        layout.addView(isAll);

        FrameLayout container = new FrameLayout(ctx);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );

        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        params.rightMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);

        layout.setLayoutParams(params);
        container.addView(layout);

        dialog.setNeutralButton("취소", null);
        dialog.setNegativeButton("검색", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                Utils.toast(ScriptEdit.this,
                        checkCharCount(input.getText().toString(), inputFind.getText().toString()) + "개가 있습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
            }
        });
        dialog.setPositiveButton("바꾸기", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which){
                if(isRegex.isChecked()){ //정규식
                    input.setText(input.getText().toString()
                            .replaceAll(inputFind.getText().toString(),
                                    inputReplace.getText().toString()));
                } else { //일반
                    if(isAll.isChecked()){ //다 바꾸기
                        input.setText(input.getText().toString()
                                .replace(inputFind.getText().toString(),
                                        inputReplace.getText().toString()));
                    } else { //한개만 바꾸기
                        input.setText(input.getText().toString()
                                .replaceFirst(inputFind.getText().toString(),
                                        inputReplace.getText().toString()));
                    }
                }
            }
        });
        dialog.setView(container);
        dialog.show();
    }

    public void insert(String tag){
        input.getText().insert(input.getSelectionStart(), tag);
    }

    public void setCursorColor(LineNumberEditText view, @ColorInt int color) {
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