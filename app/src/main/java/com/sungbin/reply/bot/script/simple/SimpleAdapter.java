package com.sungbin.reply.bot.script.simple;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.shashank.sony.fancytoastlib.FancyToast;
import com.sungbin.reply.bot.R;
import com.sungbin.reply.bot.view.activty.MainActivity;
import com.sungbin.reply.bot.utils.Utils;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.NameViewHolder>{

    private ArrayList<SimpleItem> mItems;
    private Context ctx;
    private Activity act;
    private String roomTypeStr = "",  msgTypeStr = "";

    public class NameViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected ImageButton edit, delete;
        protected Switch onoff;

        public NameViewHolder(View view) {
            super(view);
            this.name = view.findViewById(R.id.name);
            this.edit = view.findViewById(R.id.edit);
            this.delete = view.findViewById(R.id.delete);
            this.onoff = view.findViewById(R.id.onoff);
        }
    }

    public SimpleAdapter(ArrayList<SimpleItem> mItems, Activity act){
        this.mItems = mItems;
        this.act = act;
    }

    @Override
    public NameViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        this.ctx = viewGroup.getContext();
        View view = LayoutInflater.from(ctx).inflate(R.layout.simple_cell, viewGroup, false);
        return new NameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SimpleAdapter.NameViewHolder viewholder, final int position) {
        final String name = mItems.get(position).getName();

        final int accent = Color.parseColor(Utils.readData(ctx, "accent", "#80D6FF"));
        final int primary = Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5"));
        final int primaryDark = Color.parseColor(Utils.readData(ctx, "primaryDark", "#0077c2"));

        viewholder.name.setText(name);
        viewholder.name.setSelected(true);
        viewholder.name.setTextColor(primaryDark);

        viewholder.onoff.setText("");
        viewholder.onoff.setTextOn("");
        viewholder.onoff.setTextOff("");
        viewholder.onoff.getTrackDrawable().setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        viewholder.onoff.getThumbDrawable().setColorFilter(primary, PorterDuff.Mode.SRC_IN);

        if(Utils.toBoolean(Utils.readData(ctx, "simple/"+name, "true"))){ //스크립트 활성화
            viewholder.onoff.setChecked(true);
            viewholder.name.setTextColor(primaryDark);
        }
        else{ //스크립트 비활성화
            viewholder.onoff.setChecked(false);
            viewholder.name.setTextColor(primary);
        }

        viewholder.onoff.setOnCheckedChangeListener((compoundButton, tf) -> {
            Utils.saveData(ctx, "simple/"+name, tf+"");
            if(tf){
                String item = Utils.readData(ctx, "ScriptOn", "");
                item = item + "\n" + name;
                Utils.saveData(ctx, "ScriptOn", item);
                viewholder.name.setTextColor(primaryDark);
            }
            else{
                String item = Utils.readData(ctx, "ScriptOn", "");
                item = item.replace("\n" + name, "");
                Utils.saveData(ctx, "ScriptOn", item);
                viewholder.name.setTextColor(primary);
            }
        });

        viewholder.delete.setOnClickListener(view -> FancyToast.makeText(ctx, ctx.getString(R.string.press_long_to_delete), FancyToast.LENGTH_SHORT, FancyToast.INFO, false).show());

        viewholder.delete.setOnLongClickListener(view -> {
            String path = "Simple/"+name+"/";
            String RoomType = path+"RoomType.data";
            String MsgType = path+"MsgType.data";
            String Room = path+"Room.data";
            String Sender = path+"Sender.data";
            String Msg = path+"Msg.data";
            String Reply = path+"Reply.data";

            Utils.delete(RoomType);
            Utils.delete(MsgType);
            Utils.delete(Room);
            Utils.delete(Sender);
            Utils.delete(Msg);
            Utils.delete(Reply);
            Utils.delete(path);

            FancyToast.makeText(ctx, ctx.getString(R.string.script_delete), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show();
            MainActivity.adpater.notifyDataSetChanged();
            return false;
        });

        viewholder.edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final int primary = Color.parseColor(Utils.readData(ctx, "primary", "#42A5F5"));
                final int accent = Color.parseColor(Utils.readData(ctx, "accent", "#80D6FF"));
                roomTypeStr = "";
                msgTypeStr = "";

                ColorStateList colorStateList = new ColorStateList(new int[][]{
                        new int[]{-android.R.attr.state_enabled},
                        new int[]{android.R.attr.state_enabled}},
                        new int[]{accent, primary});

                AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
                dialog.setTitle(R.string.simple_script_edit);

                int p = Utils.dip2px(ctx, 7);
                LinearLayout layout = new LinearLayout(ctx);
                layout.setPadding(p, p, p, p);
                layout.setOrientation(LinearLayout.VERTICAL);

                final RadioGroup roomType = new RadioGroup(ctx);
                roomType.setOrientation(RadioGroup.VERTICAL);

                RadioButton isGroupChat = new RadioButton(ctx);
                isGroupChat.setText(R.string.only_group_room);
                isGroupChat.setOnClickListener(view1 -> roomTypeStr = "true");
                if(Build.VERSION.SDK_INT>=21) isGroupChat.setButtonTintList(colorStateList);
                roomType.addView(isGroupChat);

                RadioButton isGroupChatNot = new RadioButton(ctx);
                isGroupChatNot.setText(R.string.only_ppl_room);
                isGroupChatNot.setOnClickListener(view12 -> roomTypeStr = "false");
                if(Build.VERSION.SDK_INT>=21) isGroupChatNot.setButtonTintList(colorStateList);
                roomType.addView(isGroupChatNot);

                RadioButton all = new RadioButton(ctx);
                all.setText(R.string.all_room);
                all.setOnClickListener(view13 -> roomTypeStr = "all");
                if(Build.VERSION.SDK_INT>=21) all.setButtonTintList(colorStateList);
                roomType.addView(all);
                layout.addView(roomType);

                final View v = new View(ctx);
                int dividerHeight = (int) ctx.getResources().getDisplayMetrics().density;
                v.setBackgroundColor(Color.GRAY);
                v.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dividerHeight));
                layout.addView(v);

                final RadioGroup msgType = new RadioGroup(ctx);
                msgType.setOrientation(RadioGroup.VERTICAL);

                RadioButton equals = new RadioButton(ctx);
                equals.setText(R.string.only_msg_equals);
                equals.setOnClickListener(view14 -> msgTypeStr = "equals");
                if(Build.VERSION.SDK_INT>=21) equals.setButtonTintList(colorStateList);
                msgType.addView(equals);

                RadioButton contains = new RadioButton(ctx);
                contains.setText(R.string.only_contains_msg);
                contains.setOnClickListener(view15 -> msgTypeStr = "contains");
                if(Build.VERSION.SDK_INT>=21) contains.setButtonTintList(colorStateList);
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

                String path = "simple/"+name+"/";
                String RoomType = Utils.read(path+"RoomType.data", "");
                String MsgType = Utils.read(path+"MsgType.data", "");
                String Room = Utils.read(path+"Room.data", "");
                String Sender = Utils.read(path+"Sender.data", "");
                String Msg = Utils.read(path+"Msg.data", "");
                String Reply = Utils.read(path+"Reply.data", "");

                switch(RoomType){
                    case "true":
                        isGroupChat.toggle();
                        roomTypeStr = "true";
                        break;
                    case "false":
                        isGroupChatNot.toggle();
                        roomTypeStr = "false";
                        break;
                    case "all":
                        all.toggle();
                        roomTypeStr = "all";
                        break;
                }

                switch(MsgType){
                    case "equals":
                        equals.toggle();
                        msgTypeStr = "equals";
                        break;
                    case "contains":
                        contains.toggle();
                        msgTypeStr = "contains";
                        break;
                }

                room.setText(Room);
                sender.setText(Sender);
                msg.setText(Msg);
                reply.setText(Reply);

                FrameLayout container = new FrameLayout(ctx);
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
                );

                params.leftMargin = ctx.getResources().getDimensionPixelSize(R.dimen.fab_margin);
                params.rightMargin = ctx.getResources().getDimensionPixelSize(R.dimen.fab_margin);

                layout.setLayoutParams(params);
                container.addView(layout);

                ScrollView scrollView = new ScrollView(ctx);
                scrollView.addView(container);

                dialog.setView(scrollView);
                dialog.setNegativeButton("취소", null);
                dialog.setPositiveButton("수정", (dialogInterface, i) -> {
                    if(StringUtils.isBlank(msgTypeStr)||StringUtils.isBlank(roomTypeStr)){
                        Utils.toast(act, ctx.getString(R.string.plz_check_radio_btn), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    }
                    else if(StringUtils.isBlank(reply.getText().toString())){
                        Utils.toast(act, ctx.getString(R.string.please_input_reply_msg), FancyToast.LENGTH_SHORT, FancyToast.WARNING);
                    }
                    else{
                        Utils.save("simple/"+name+"/RoomType.data", roomTypeStr);
                        Utils.save("simple/"+name+"/MsgType.data", msgTypeStr);
                        Utils.save("simple/"+name+"/Room.data", room.getText().toString());
                        Utils.save("simple/"+name+"/Sender.data", sender.getText().toString());
                        Utils.save("simple/"+name+"/Msg.data", msg.getText().toString());
                        Utils.save("simple/"+name+"/Reply.data", reply.getText().toString());
                        Utils.toast(act, ctx.getString(R.string.success_edit), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS);
                    }
                });

                dialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (null != mItems ? mItems.size() : 0);
    }

    public SimpleItem getItem(int position) {
        return mItems.get(position);
    }

    public void setCursorColor(EditText view, @ColorInt int color) {
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
