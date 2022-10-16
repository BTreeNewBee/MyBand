package com.iguigui.band

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<Button>(R.id.ConnectBand).setOnClickListener {

        }
        val myReceiver = MyReceiver() //实例化一下广播接收器
        registerReceiver(myReceiver, IntentFilter("com.iguigui.band")) //注册广播接收器

    }




}

class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show()

    }
}