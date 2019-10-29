package com.sungbin.reply.bot.view.sourcehub.view.activity

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sungbin.reply.bot.view.sourcehub.utils.FirebaseUtils
import com.sungbin.reply.bot.view.sourcehub.utils.Utils
import com.sungbin.reply.bot.view.sourcehub.view.fragment.BoardList
import com.sungbin.reply.bot.R
import kotlinx.android.synthetic.main.hub_activity_main.*

class MainActivity : AppCompatActivity() {

    private var fm: FragmentManager? = null
    private var fragmentTransaction: FragmentTransaction? = null
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_board -> {
                fragmentTransaction = fm!!.beginTransaction().apply {
                    replace(R.id.page, BoardList())
                    commit()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_rank -> {
                fragmentTransaction = fm!!.beginTransaction().apply {
                    replace(R.id.page, BoardList())
                    commit()
                }
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                fragmentTransaction = fm!!.beginTransaction().apply {
                    replace(R.id.page, BoardList())
                    commit()
                }
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.hub_activity_main)
        toolbar.title = ""
        setSupportActionBar(toolbar)
        navigation_view.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        fm = supportFragmentManager
        fragmentTransaction = fm!!.beginTransaction().apply {
            replace(R.id.page, BoardList())
            commit()
        }

        FirebaseUtils.subscribe("NewPostNoti", applicationContext)
        FirebaseUtils.subscribe("new_comment", applicationContext)

        val reference = FirebaseDatabase.getInstance().reference.child("User Nickname")
        reference.child(Utils.readData(applicationContext,
            "uid", "null")!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Utils.saveData(applicationContext,
                    "nickname", dataSnapshot.value.toString())
            }
            override fun onCancelled(databaseError: DatabaseError) {

            }
        })

    }
}
