package com.sungbin.reply.bot.view.activty

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity;
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import com.shashank.sony.fancytoastlib.FancyToast
import com.sungbin.reply.bot.R
import com.sungbin.reply.bot.adapter.KavenAdapter
import com.sungbin.reply.bot.utils.Kaven
import com.sungbin.reply.bot.utils.Utils

import kotlinx.android.synthetic.main.activity_kaven.*
import kotlinx.android.synthetic.main.content_kaven.*

class KavenActivity : AppCompatActivity() {

    private var adapter: KavenAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kaven)
        setSupportActionBar(toolbar)

        Utils.toast(this,
                getString(R.string.swipe_can_reload),
                FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)

        swipe.setOnRefreshListener {
            adapter = KavenAdapter(Kaven.getAllDownload(), this)
            list.adapter = adapter
            swipe.isRefreshing = false
        }

        fab.setOnClickListener {
            val dialog = AlertDialog.Builder(KavenActivity@this)
            dialog.setTitle(getString(R.string.add_kaven_download))

            val input = EditText(KavenActivity@this)
            input.hint = getString(R.string.input_download_kaven_name)

            val container = FrameLayout(KavenActivity@this)
            val params = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            params.leftMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)
            params.rightMargin = resources.getDimensionPixelSize(R.dimen.fab_margin)

            input.layoutParams = params
            container.addView(input)

            dialog.setView(container)
            dialog.setPositiveButton("다운로드") { _, _ ->
                var name = input.text.toString();
                if(!name.contains(".js")) name = "$name.js"
                Kaven.download(this, name, KavenActivity@this)
                Utils.toast(this, "다운로드중...",
                        FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
                adapter = KavenAdapter(Kaven.getAllDownload(), this)
                list.adapter = adapter
            }
            dialog.setNegativeButton("취소", null)

            dialog.show()
        }

        val primary = Utils.readData(applicationContext, "primary", "#42A5F5")
        val primaryDark = Utils.readData(applicationContext, "primaryDark", "#0077C2")
        val accent = Utils.readData(applicationContext, "accent", "#80D6FF")

        val themeChange = Utils.toBoolean(Utils.readData(applicationContext, "theme change", "false"))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val window = this.window
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = Color.parseColor(primaryDark)
            window.navigationBarColor = Color.parseColor(accent)
            swipe.setColorSchemeColors(Color.parseColor(accent),
                    Color.parseColor(primary),
                    Color.parseColor(primaryDark),
                    Color.parseColor(primary))
            toolbar.setBackgroundColor(Color.parseColor(primary))
            fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(accent))
            if (themeChange) {
                FancyToast.makeText(this, getString(R.string.succes_theme), FancyToast.LENGTH_SHORT, FancyToast.SUCCESS, false).show()
                Utils.saveData(applicationContext, "theme change", "false")
            }
        } else {
            if (themeChange) {
                Utils.saveData(applicationContext, "theme change", "false")
                FancyToast.makeText(this, getString(R.string.cant_set_theme), FancyToast.LENGTH_SHORT, FancyToast.ERROR, false).show()
            }
        }

        adapter = KavenAdapter(Kaven.getAllDownload(), this)
        list.adapter = adapter
        list.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 1, 0, "모두 삭제").setIcon(R.drawable.ic_delete_white_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        if(id == 1){
            Kaven.deleteAllDownload()
            Utils.toast(this, "다운로드한 Kaven이 모두 삭제 되었습니다.",
                    FancyToast.LENGTH_SHORT, FancyToast.SUCCESS)
            adapter = KavenAdapter(Kaven.getAllDownload(), this)
            list.adapter = adapter
        }
        return super.onOptionsItemSelected(item)
    }

}
