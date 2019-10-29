package com.sungbin.reply.bot.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.sungbin.reply.bot.script.CoffeeScript.CoffeeCode
import com.sungbin.reply.bot.script.javascript.JsCode
import com.sungbin.reply.bot.script.lua.LuaCode
import com.sungbin.reply.bot.script.simple.SimpleCode

class MainAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> JsCode()
            1 -> CoffeeCode()
            2 -> LuaCode()
            3 -> SimpleCode()
            else -> JsCode()
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }
}
