package com.hezaro.wall.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hezaro.wall.R
import com.hezaro.wall.feature.core.main.MainActivity

class GeneralNotificationHelper(private val context: Context) {


    val notificationID = 1002
    val requestCode = 519
    val CHANNEL_ID = "1002"
    val CHANNEL_NAME = "common_messages"
    var manager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as (NotificationManager)

    init {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            manager.createNotificationChannel(notificationChannel)
        }

    }
    fun showMessage(notificationBody: NotificationBody) {

        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
        notificationBuilder.setContentTitle(notificationBody.title)
        notificationBuilder.setContentText(notificationBody.message)
        manager.notify(notificationID, notificationBuilder.build())
    }
}