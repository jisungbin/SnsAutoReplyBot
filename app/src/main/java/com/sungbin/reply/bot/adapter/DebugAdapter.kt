package com.sungbin.reply.bot.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.text.SpannableString
import androidx.annotation.NonNull
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import com.sungbin.reply.bot.R
import com.sungbin.reply.bot.dto.DebugItem
import com.sungbin.reply.bot.utils.ReadMoreUtils
import com.sungbin.reply.bot.utils.Utils

class DebugAdapter(private val list: ArrayList<DebugItem>?,
                          private val act: Activity) :
        androidx.recyclerview.widget.RecyclerView.Adapter<DebugAdapter.DebugViewHolder>() {

    inner class DebugViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        var sender_R: TextView = view.findViewById(R.id.sender_view_R)
        var msg_R: TextView = view.findViewById(R.id.content_view_R)
        var view_R: androidx.cardview.widget.CardView = view.findViewById(R.id.debug_card_view_R)
        var content_R: RelativeLayout = view.findViewById(R.id.right_content_view)

        var sender_L: TextView = view.findViewById(R.id.sender_view)
        var msg_L: TextView = view.findViewById(R.id.content_view)
        var view_L: androidx.cardview.widget.CardView = view.findViewById(R.id.debug_card_view)
        var content_L: RelativeLayout = view.findViewById(R.id.left_content_view)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): DebugViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.debug_room_list_view, viewGroup, false)
        return DebugViewHolder(view)
    }

    @SuppressLint("RtlHardcoded")
    override fun onBindViewHolder(@NonNull viewholder: DebugViewHolder, position: Int) {
        val sender = list!![position].name
        val content = list[position].msg
        val gravity = list[position].gravity

        if(gravity == Gravity.RIGHT) {
            viewholder.sender_R.text = sender
            viewholder.msg_R.text = content
            viewholder.view_R.setOnLongClickListener {
                Utils.copy(act, content)
                return@setOnLongClickListener false
            }

            ReadMoreUtils.setReadMoreLength(viewholder.msg_R, content!!, 500)
        }
        else{
            viewholder.content_R.visibility = View.GONE
            viewholder.content_L.visibility = View.VISIBLE

            viewholder.sender_L.text = sender
            viewholder.msg_L.text = content
            viewholder.view_L.setOnLongClickListener {
                Utils.copy(act, content)
                return@setOnLongClickListener false
            }

            ReadMoreUtils.setReadMoreLength(viewholder.msg_L, content!!, 500)
        }
    }

    override fun getItemCount(): Int {
        return list?.size ?: 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun getItem(position: Int): DebugItem{
        return list!![position]
    }

}
