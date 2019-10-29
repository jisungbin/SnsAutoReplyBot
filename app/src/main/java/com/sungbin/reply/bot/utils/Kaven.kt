package com.sungbin.reply.bot.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.StrictMode
import com.sungbin.reply.bot.utils.Utils.sdcard
import java.io.File
import org.jsoup.Jsoup
import com.google.firebase.storage.FirebaseStorage
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.reply.bot.dto.KavenItem
import com.sungbin.reply.bot.view.activty.DebugActivity.list


class Kaven : Application() {

    override fun onCreate() {
        super.onCreate()
        ctx = applicationContext
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitNetwork().build())
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var ctx: Context? = null
        private var result: String? = null

        @JvmStatic
        fun removeDir(mRootPath: String) {
            val file = File(mRootPath)
            val childFileList = file.listFiles()
            for (childFile in childFileList) {
                if (childFile.isDirectory) {
                    removeDir(childFile.absolutePath)
                } else {
                    childFile.delete()
                }
            }
            file.delete()
        }

        fun deleteAllDownload() {
            removeDir("$sdcard/New kakaotalk Bot 2/Kaven");
        }

        fun download(act: Activity, name: String, ctx: Context) {
            val storageRef = FirebaseStorage.getInstance().reference
            storageRef.child("kaven/source/$name").downloadUrl.addOnSuccessListener { uri ->
                val result = Jsoup.connect(uri.toString()).ignoreContentType(true).get().text()
                Utils.createFolder("Kaven")
                Utils.save("Kaven/$name", result)
                Utils.toast(act, "$name 다운로드 완료",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            }.addOnFailureListener { exception -> Utils.toast(act, "$name 다운로드 오류\n${exception.message}",
                    FancyToast.LENGTH_SHORT, FancyToast.ERROR) }
        }

        fun read(name: String): String{
            return Utils.read("Kaven/$name", "파일이 없습니다.")
        }

        fun delete(name: String){
            Utils.delete("Kaven/$name")
        }

        fun getAllDownload(): ArrayList<KavenItem> {
            val file = File("$sdcard/New kakaotalk Bot 2/Kaven")
            val list = file.listFiles()
            val lists = ArrayList<KavenItem>()
            if (list != null) {
                for (element in list) {
                    val name = element.toString().replace("$sdcard/New kakaotalk Bot 2/Kaven/", "")
                    val item = KavenItem(name)
                    lists.add(item)
                }
            }
            return lists;
        }
    }
}
