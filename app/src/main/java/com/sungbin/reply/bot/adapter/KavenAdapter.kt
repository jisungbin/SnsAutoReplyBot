package com.sungbin.reply.bot.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import androidx.annotation.NonNull
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.reply.bot.R
import com.sungbin.reply.bot.dto.KavenItem
import com.sungbin.reply.bot.utils.Kaven
import com.sungbin.reply.bot.utils.Utils

class KavenAdapter(private val list: ArrayList<KavenItem>?, private val act: Activity) :
        androidx.recyclerview.widget.RecyclerView.Adapter<KavenAdapter.KavenViewHolder>() {

    private var ctx: Context? = null

    inner class KavenViewHolder(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): KavenViewHolder {
        ctx = viewGroup.context
        val view = LayoutInflater.from(ctx).inflate(R.layout.kaven_list_view, viewGroup, false)
        return KavenViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull viewholder: KavenViewHolder, position: Int) {
        val name = list!![position].name
        viewholder.title.text = name
        viewholder.title.setOnClickListener {
            val dialog = AlertDialog.Builder(ctx)
            dialog.setTitle(name)
            dialog.setMessage(Kaven.read(name!!))
            dialog.setPositiveButton("닫기", null)
            dialog.setNegativeButton("삭제") { _, _ ->
                Kaven.delete(name)
                Utils.toast(act, "삭제되었습니다.",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            }
            dialog.show()
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

    fun getItem(position: Int): KavenItem {
        return list!![position]
    }

}
