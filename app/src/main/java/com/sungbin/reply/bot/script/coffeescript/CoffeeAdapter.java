package com.sungbin.reply.bot.script.coffeescript;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;


public class CoffeeAdapter extends RecyclerView.Adapter<CoffeeAdapter.NameViewHolder> {

    private ArrayList<CoffeeItem> nameList;
    private Context ctx;
    private Activity act;
    private String reloadName;
    private NameViewHolder reloadViewHolder;

    public class NameViewHolder extends RecyclerView.ViewHolder {
        protected TextView name, lastTime;
        protected ImageButton edit, delete, log, debug, reload;
        protected Switch onoff;

        public NameViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.name);
            this.edit =  view.findViewById(R.id.edit);
            this.delete = view.findViewById(R.id.delete);
            this.log = view.findViewById(R.id.log);
            this.debug = view.findViewById(R.id.debug);
            this.reload = view.findViewById(R.id.reload);
            this.lastTime = view.findViewById(R.id.lastTime);
            this.onoff = view.findViewById(R.id.onoff);
        }
    }

    public CoffeeAdapter(ArrayList<CoffeeItem> list, Activity act) {
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
        final String name = nameList.get(position).getName();

        final int accent = Color.parseColor(Utils.readData(ctx, "accent", "#80D6FF"));
        final int primary = Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5"));
        final int primaryDark = Color.parseColor(Utils.readData(ctx, "primaryDark", "#0077c2"));

        final String lastTime = Utils.readData(ctx, name+".time", "알수없음");

        viewholder.name.setText(name.replace(".coffee",""));
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
            if(tf){
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
            else{
                String item = Utils.readData(ctx, "ScriptOn", "");
                item = item.replace("\n" + name, "");
                Utils.saveData(ctx, "ScriptOn", item);
                viewholder.name.setTextColor(primary);
                Utils.saveData(ctx, name, "false");
            }
        });

        viewholder.reload.setOnClickListener(view -> {
            reloadViewHolder = viewholder;
            reloadName = name;
            new ReloadTask().execute();
        });

        viewholder.delete.setOnClickListener(view -> Utils.toast(act, ctx.getString(R.string.press_long_to_delete), FancyToast.LENGTH_SHORT, FancyToast.INFO));

        viewholder.delete.setOnLongClickListener(view -> {
            Utils.delete("CoffeeScript/"+name);
            Utils.toast(act, act.getString(R.string.script_delete), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
            MainActivity.adpater.notifyDataSetChanged();
            return false;
        });

        viewholder.debug.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, DebugActivity.class);
            intent.putExtra("name", name);
            DebugActivity.items.clear();
            ctx.startActivity(intent);
        });

        viewholder.edit.setOnClickListener(view -> {
            Intent intent = new Intent(ctx, ScriptEdit.class);
            intent.putExtra("name", name);
            intent.putExtra("lenguage", "Coffee");
            intent.putExtra("code", Utils.read("coffeescript/"+name, "null"));
            ctx.startActivity(intent);
        });
        viewholder.log.setOnClickListener(view -> {
            String data = Utils.readData(ctx, "Log/"+name, "");

            Intent i = new Intent(ctx, LogActivity.class);
            i.putExtra("data", data);
            i.putExtra("name", "Log/"+name);
            ctx.startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return (null != nameList ? nameList.size() : 0);
    }

    public CoffeeItem getItem(int position) {
        return nameList.get(position);
    }

    private class ReloadTask extends AsyncTask<Void, Void, Void>{

        private String result = null;
        private SweetAlertDialog pDialog;

        @Override
        protected void onPreExecute() {
            try{
                if(pDialog!=null) pDialog.cancel();
                pDialog = new SweetAlertDialog(ctx, SweetAlertDialog.PROGRESS_TYPE);
                pDialog.getProgressHelper().setBarColor(Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5")));
                pDialog.setTitleText(ctx.getString(R.string.script_reloading));
                pDialog.setCancelable(false);
                pDialog.show();
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                KakaoTalkListener.jsScripts.remove(reloadName);
                result = KakaoTalkListener.initializeCoffeeScript(reloadName);
                return null;
            }
            catch(Exception e){
                return null;
            }
        }

        @Override
        protected void onPostExecute(Void v) {
            try{
                pDialog.cancel();
                if(result.equals("true")) Utils.toast(act, ctx.getString(R.string.success_reload), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                else{
                    Utils.toast(act, result, FancyToast.LENGTH_SHORT, FancyToast.ERROR);
                    KakaoTalkListener.jsScripts.remove(reloadName);
                    reloadViewHolder.onoff.setChecked(false);
                    reloadViewHolder.name.setTextColor(Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5")));
                }
            }
            catch(Exception ignored){
            }
        }
    };

}
