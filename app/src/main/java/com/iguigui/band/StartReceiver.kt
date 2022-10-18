package com.iguigui.band

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orhanobut.logger.Logger

class StartReceiver : BroadcastReceiver() {

    private val ACTION_BOOT = "android.intent.action.BOOT_COMPLETED"
    private val ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED"
    private val ACTION_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED"
    private val ACTION_MEDIA_EJECT = "android.intent.action.MEDIA_EJECT"
    private val ACTION_MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED"

    override fun onReceive(context: Context, intent: Intent) {

        context.startService(Intent(context, LogService::class.java))

        Logger.i("StartReceiver ${intent.action}")
        // 判断是否是系统开启启动的消息，如果是，则启动APP
        if (ACTION_BOOT == intent.action
            || ACTION_MEDIA_MOUNTED == intent.action
            || ACTION_MEDIA_UNMOUNTED == intent.action
            || ACTION_MEDIA_EJECT == intent.action
            || ACTION_MEDIA_REMOVED == intent.action
        ) {
            val intentMainActivity = Intent(context, MainActivity::class.java)
            intentMainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intentMainActivity)
            Logger.i("StartReceiver")

            context.startService(Intent(context, ReceiverService::class.java))
        }
    }


}