package com.sungbin.reply.bot.view.sourcehub.utils

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import com.sungbin.reply.bot.R
import com.sungbin.reply.bot.view.sourcehub.notification.NotificationManager
import java.lang.Exception

object FirebaseUtils {
    fun subscribe(topic: String, ctx: Context){
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
        }
        catch (e: Exception) {
            Utils.error(ctx, e, ctx.getString(R.string.sub_topic))
        }
    }

    fun unSubscribe(topic: String, ctx: Context){
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
        }
        catch (e: Exception){
            Utils.error(ctx, e, ctx.getString(R.string.unsub_topic))
        }
    }

    fun showNoti(ctx: Context, title:String, content:String, topic: String){
        NotificationManager.sendNotiToFcm(ctx, title, content, topic)
    }
}