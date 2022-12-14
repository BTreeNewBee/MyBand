package com.iguigui.band

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import com.orhanobut.logger.Logger
import com.rabbitmq.client.*
import kotlinx.coroutines.coroutineScope
import java.util.*
import kotlin.concurrent.thread

class ReceiverService : Service() {

    private val fellAsleepReceiver by lazy { FellAsleepReceiver() }
    private val wokeUpReceiver by lazy { WokeUpReceiver() }
    private val startNonWearReceiver by lazy { StartNonWearReceiver() }

    override fun onBind(intent: android.content.Intent): android.os.IBinder? {
        Logger.d("onBind ${intent.action}")
        return null
    }

    override fun onCreate() {
        Logger.d("ReceiverService onCreate")
        super.onCreate()
        connectionRMQ()

        //爷醒了
        registerReceiver(
            fellAsleepReceiver,
            IntentFilter("nodomain.freeyourgadget.gadgetbridge.FellAsleep")
        )

        //爷睡了
        registerReceiver(
            wokeUpReceiver,
            IntentFilter("nodomain.freeyourgadget.gadgetbridge.WokeUp")
        )

        //爷不带手环了
        registerReceiver(
            startNonWearReceiver,
            IntentFilter("nodomain.freeyourgadget.gadgetbridge.StartNonWear")
        )

        //爷玩手机了
        registerReceiver(
            startNonWearReceiver,
//            IntentFilter("nodomain.freeyourgadget.gadgetbridge.StartNonWear")
            IntentFilter("android.intent.action.SCREEN_ON")
        )

        //爷不玩了
        registerReceiver(
            startNonWearReceiver,
//            IntentFilter("nodomain.freeyourgadget.gadgetbridge.StartNonWear")
            IntentFilter("android.intent.action.SCREEN_OFF")
        )
    }

    override fun onDestroy() {
        Logger.d("ReceiverService onDestroy")
        super.onDestroy()
        unregisterReceiver(fellAsleepReceiver)
        unregisterReceiver(wokeUpReceiver)
        unregisterReceiver(startNonWearReceiver)
    }


    fun connectionRMQ() {
        val props = Properties()
        props.load(assets.open("config.properties"))
        RMQProperties = props
        setUpConnectionFactory()
    }


}


class FellAsleepReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show()
        Logger.i("FellAsleepReceiver context $context intent $intent")
        thread {
            channel.basicPublish(
                "amq.direct",
                "clientInfo",
                basicProperties,
                "FellAsleep".toByteArray()
            )
        }
    }
}

class WokeUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show()
        Logger.i("WokeUpReceiver context $context intent $intent")
        thread {
            channel.basicPublish(
                "amq.direct",
                "clientInfo",
                basicProperties,
                "WokeUp".toByteArray()
            )
        }
    }
}

class StartNonWearReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Intent Detected.", Toast.LENGTH_LONG).show()
        Logger.i("StartNonWearReceiver context $context intent $intent")
        thread {
            channel.basicPublish(
                "amq.direct",
                "clientInfo",
                basicProperties,
                "StartNonWearReceiver =  ${intent?.action}".toByteArray()
            )
        }
    }
}