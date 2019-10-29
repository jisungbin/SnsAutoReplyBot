package com.sungbin.reply.bot.view.sourcehub.utils

import android.content.Context
import android.content.res.Resources
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import com.sungbin.reply.bot.R

object DialogUtils {
    @JvmStatic
    fun makeMarginLayout(res: Resources, ctx: Context, layout: LinearLayout): ScrollView {
        val container = ScrollView(ctx)
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.leftMargin = res.getDimensionPixelSize(R.dimen.margin_default)
        params.rightMargin = res.getDimensionPixelSize(R.dimen.margin_default)
        params.topMargin = res.getDimensionPixelSize(R.dimen.margin_default)

        layout.layoutParams = params
        container.addView(layout)

        val view = ScrollView(ctx)
        view.isFocusableInTouchMode = true
        view.addView(container)

        return view
    }
}