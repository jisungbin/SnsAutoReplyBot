package com.sungbin.reply.bot.script.javascript;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.view.activty.DebugActivity;
import com.sungbin.reply.bot.view.activty.MainActivity;
import com.sungbin.reply.bot.listener.KakaoTalkListener;
import com.sungbin.reply.bot.view.activty.LogActivity;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.view.activty.ScriptEdit;
import com.sungbin.reply.bot.utils.Utils;

import java.io.File;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;


public class JsAdapter extends RecyclerView.Adapter<JsAdapter.NameViewHolder> {

    private ArrayList<JsItem> nameList;
    private Context ctx;
    private Activity act;
    private String result;

    public class NameViewHolder extends RecyclerView.ViewHolder {
        protected CardView cardView;
        protected TextView name, lastTime;
        protected ImageButton edit, delete, log, debug, reload;
        protected Switch onoff;

        public NameViewHolder(View view) {
            super(view);
            this.cardView = view.findViewById(R.id.cardView);
            this.name = view.findViewById(R.id.name);
            this.edit = view.findViewById(R.id.edit);
            this.delete = view.findViewById(R.id.delete);
            this.log = view.findViewById(R.id.log);
            this.debug = view.findViewById(R.id.debug);
            this.reload = view.findViewById(R.id.reload);
            this.lastTime = view.findViewById(R.id.lastTime);
            this.onoff = view.findViewById(R.id.onoff);
        }
    }

    public JsAdapter(ArrayList<JsItem> list, Activity act) {
        this.nameList = list;
        this.act = act;
    }

    @Override
    public NameViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.ctx = viewGroup.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.bubbletab_cell, viewGroup, false);
        return new NameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NameViewHolder viewholder, final int position) {
        final String name = nameList.get(position).getName(); //확장자 O

        final int accent = Color.parseColor(Utils.readData(ctx, "accent", "#80D6FF"));
        final int primary = Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5"));
        final int primaryDark = Color.parseColor(Utils.readData(ctx, "primaryDark", "#0077c2"));

        final String lastTime = Utils.readData(ctx, name+".time", "알수없음");

        viewholder.name.setText(name.replace(".js",""));
        viewholder.name.setSelected(true);
        viewholder.name.setTextColor(primaryDark);

        viewholder.lastTime.setSelected(true);
        viewholder.lastTime.setTextColor(primary);
        viewholder.lastTime.setText(viewholder.lastTime.getText().toString().replace("@time", lastTime));

        viewholder.onoff.setText("");
        viewholder.onoff.setTextOn("");
        viewholder.onoff.setTextOff("");
        viewholder.onoff.getTrackDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        viewholder.onoff.getThumbDrawable().setColorFilter(primary, PorterDuff.Mode.SRC_IN);

        if(Utils.toBoolean(Utils.readData(ctx, name, "false"))){ //활성화
            viewholder.onoff.setChecked(true);
            viewholder.name.setTextColor(primaryDark);
        }
        else{ //비활성화
            viewholder.onoff.setChecked(false);
            viewholder.name.setTextColor(primary);
        }

        viewholder.onoff.setOnCheckedChangeListener((compoundButton, tf) -> {
            if(tf){ //켜진거
                if(KakaoTalkListener.jsScripts.containsKey(name)){
                    String item = Utils.readData(ctx, "ScriptOn", "");
                    item = item + "\n" + name;
                    Utils.saveData(ctx, "ScriptOn", item);
                    viewholder.name.setTextColor(primaryDark);
                    Utils.saveData(ctx, name, "true");
                }
                else{
                    viewholder.onoff.setChecked(false);
                    viewholder.name.setTextColor(primary);
                    Utils.toast(act, ctx.getString(R.string.need_reload), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    Utils.saveData(ctx, name, "false");
                }
            }
            else{ //꺼진거
                String item = Utils.readData(ctx, "ScriptOn", "");
                item = item.replace("\n" + name, "");
                Utils.saveData(ctx, "ScriptOn", item);
                viewholder.name.setTextColor(primary);
                Utils.saveData(ctx, name, "false");
            }
        });

        viewholder.reload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                result = null;
                final SweetAlertDialog pDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5")));
                pDialog.setTitleText(ctx.getString(R.string.script_reloading));
                pDialog.setCancelable(false);
                final Thread thr = new Thread(() -> viewholder.reload.post(() -> {
                    pDialog.show();
                    KakaoTalkListener.jsScripts.remove(name);
                    KakaoTalkListener.jsScope.remove(name);
                    result = KakaoTalkListener.initializeJavaScript(name);
                }));
                thr.start();

                Thread thr2 = new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            thr.join();
                            viewholder.reload.post(() -> {
                                pDialog.cancel();
                                if(!result.equals("true")){
                                    viewholder.onoff.setChecked(false);
                                    viewholder.onoff.setChecked(false);
                                    viewholder.name.setTextColor(Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5")));
                                    Utils.toast(act, result, FancyToast.LENGTH_SHORT, FancyToast.ERROR);
                                    return;
                                }

                                boolean result2 = KakaoTalkListener.callJsResponder(name, "", "", "", true, null, null, "DebugActivity");
                                if(result2) Utils.toast(act, ctx.getString(R.string.success_reload), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                                else{
                                    KakaoTalkListener.jsScripts.remove(name);
                                    KakaoTalkListener.jsScope.remove(name);
                                    viewholder.onoff.setChecked(false);
                                    viewholder.name.setTextColor(Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5")));
                                }
                            });
                        }
                        catch(InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                });
                thr2.start();
            }
        });

        viewholder.delete.setOnClickListener(view -> Utils.toast(act, ctx.getString(R.string.press_long_to_delete), FancyToast.LENGTH_SHORT, FancyToast.INFO));

        viewholder.delete.setOnLongClickListener(view -> {
            Utils.delete("JavaScript/"+name);
            Utils.toast(act, ctx.getString(R.string.script_delete), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
            MainActivity.adpater.notifyDataSetChanged();
            return false;
        });

        viewholder.debug.setOnClickListener(view -> {
            DebugActivity.items.clear();
            Intent intent = new Intent(ctx, DebugActivity.class);
            intent.putExtra("name", name);
            ctx.startActivity(intent);
        });

        viewholder.edit.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, ScriptEdit.class);
            intent.putExtra("name", name);
            intent.putExtra("language", "Js");
            intent.putExtra("code", Utils.read("javascript/"+name, "null"));
            ctx.startActivity(intent);
            Utils.toast(act, ctx.getString(R.string.press_long_edit_script_name),
                    FancyToast.LENGTH_SHORT, FancyToast.INFO);
        });

        viewholder.edit.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v){
                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                dialog.setTitle(ctx.getString(R.string.edit_script_name));

                EditText input = new EditText(ctx);
                input.setText(name.replace(".js", ""));
                input.setHint(ctx.getString(R.string.edit_script_name));

                FrameLayout container = new FrameLayout(ctx);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );

                params.topMargin = ctx.getResources().getDimensionPixelSize(R.dimen.fab_margin);
                params.leftMargin = ctx.getResources().getDimensionPixelSize(R.dimen.fab_margin);
                params.rightMargin = ctx.getResources().getDimensionPixelSize(R.dimen.fab_margin);

                input.setLayoutParams(params);
                container.addView(input);

                dialog.setView(container);
                dialog.setNegativeButton("취소", null);
                dialog.setPositiveButton("확인", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        File filePre = new File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/javascript/", name);
                        File fileNow = new File(com.sungbin.reply.bot.utils.Utils.sdcard+"/New kakaotalk Bot 2/javascript/",
                                input.getText().toString() + ".js");
                        filePre.renameTo(fileNow);
                        MainActivity.adpater.notifyDataSetChanged();
                        Utils.toast(act, "스크립트의 이름이 변경되었습니다.",
                                 FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                    }
                });
                dialog.show();
                return false;
            }
        });

        viewholder.log.setOnClickListener(view -> {
            Intent i = new Intent(ctx, LogActivity.class);
            i.putExtra("name", name);
            ctx.startActivity(i);
        });

        String SHOWCASE_ID = ctx.getString(R.string.btn_description);

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(act, SHOWCASE_ID);

        sequence.addSequenceItem(viewholder.delete, ctx.getString(R.string.btn_delete), ctx.getString(R.string.ok_string));

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.log)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.btn_log)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.debug)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.btn_debug)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.edit)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.btn_edit)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.reload)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.btn_reload)
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.onoff)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.switch_onoff)
                        .withRectangleShape()
                        .build()
        );

        sequence.addSequenceItem(
                new MaterialShowcaseView.Builder(act)
                        .setTarget(viewholder.cardView)
                        .setDismissText(ctx.getString(R.string.ok_string))
                        .setContentText(R.string.delete_preview_box)
                        .withRectangleShape()
                        .build()
        );

        if(Utils.readData(act, "isFirstStart", "true").equals("true")) sequence.start();

        Utils.saveData(act, "isFirstStart", "false");
    }

    @Override
    public int getItemCount() {
        return (null != nameList ? nameList.size() : 0);
    }

    public JsItem getItem(int position) {
        return nameList.get(position);
    }

}
