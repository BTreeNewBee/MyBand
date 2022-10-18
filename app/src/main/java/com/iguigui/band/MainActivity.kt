package com.iguigui.band

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.*
import android.util.Log.*
import android.widget.Button
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.iguigui.band.permission.PermissionsManager
import com.iguigui.band.permission.PermissionsResultAction
import com.orhanobut.logger.*
import com.rabbitmq.client.*
import java.io.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermissions()
        startService(Intent(this, LogService::class.java))
        Logger.i("MainActivity onCreate")
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        startService(Intent(this, ReceiverService::class.java))
        findViewById<Button>(R.id.ConnectBand).setOnClickListener {

        }
    }

    override fun onStop() {
        super.onStop()
        Logger.i("onStop")
    }


    override fun onDestroy() {
        Logger.i("onDestroy")
        super.onDestroy()
    }


    private fun requestPermissions() {
        if (!//写入权限
            //电话拨打权限
            PermissionsManager.getInstance().hasAllPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            )
        )
        ;
        PermissionsManager.getInstance().requestPermissionsIfNecessaryForResult(this,
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), object : PermissionsResultAction() {

                override fun onGranted() {
                }

                override fun onDenied(permission: String) {
                }
            })
    }

    override fun onRequestPermissionsResult(
        permsRequestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(permsRequestCode, permissions, grantResults)
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults)
    }

}

