package com.sungbin.reply.bot.script.javascript

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import androidx.recyclerview.widget.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shashank.sony.fancytoastlib.FancyToast

import com.sungbin.reply.bot.R
import com.sungbin.reply.bot.view.activty.MainActivity
import com.sungbin.reply.bot.listener.SwipeController
import com.sungbin.reply.bot.listener.SwipeControllerActions
import com.sungbin.reply.bot.utils.Utils

import java.io.File
import java.util.ArrayList

class JsCode : androidx.fragment.app.Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var scroll: NestedScrollView
    private lateinit var fab: FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bubbletab_page, container, false)
    }

    @SuppressLint("RestrictedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val jsItem = ArrayList<JsItem>()
        val isFirstStart = Utils.readData(context!!, "isFirstStart", "true")
        if (isFirstStart == "true") {
            val name = JsItem("스크립트 미리보기")
            jsItem.add(name)
        } else {
            val path = Utils.sdcard + "/New kakaotalk Bot 2/JavaScript"
            val list = File(path).listFiles()

            for (i in list.indices) {
                val name = JsItem(list[i].toString().replace(Utils.sdcard + "/New kakaotalk Bot 2/JavaScript/", ""))
                jsItem.add(name)
            }
        }

        val accent = Utils.readData(context, "accent", "#80D6FF")
        fab = view.findViewById(R.id.add)
        fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(accent))
        scroll = view.findViewById(R.id.nest_scroll_view)
        recyclerView = view.findViewById(R.id.list) as RecyclerView
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        scroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, y, _, oldy ->
            if (y > oldy) {
                //Down
                fab.hide()
            }
            if (y < oldy) {
                //Up
                fab.show()
            }
        })

        fab.setOnClickListener {
            MainActivity.addScript()
        }

        val useOldMain = Utils.toBoolean(Utils.readData(context, "UseOldHome", "false"))
        if(useOldMain) fab.visibility = View.GONE

        val swipeController = SwipeController(object : SwipeControllerActions() {
            override fun onRightClicked(position: Int) {
                super.onLeftClicked(position)
                val name = jsItem[position].name
                Utils.delete("JavaScript/$name")
                FancyToast.makeText(context, context!!.getString(R.string.script_delete), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show()
                MainActivity.adpater.notifyDataSetChanged()
            }
            override fun onLeftClicked(position: Int) {
                super.onRightClicked(position)
                val name = jsItem[position].name
                Utils.delete("JavaScript/$name")
                FancyToast.makeText(context, context!!.getString(R.string.script_delete), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show()
                MainActivity.adpater.notifyDataSetChanged()
            }
        })

        val itemTouchHelper = ItemTouchHelper(swipeController)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                swipeController.onDraw(c)
            }
        })

        val jsAdapter = JsAdapter(jsItem, activity)
        jsAdapter.notifyDataSetChanged()
        recyclerView.adapter = jsAdapter
    }


}
