package com.sungbin.reply.bot.script.lua;

public class LuaItem{
    private String name;

    public LuaItem(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
}
