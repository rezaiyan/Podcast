package com.hezaro.wall.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hezaro.wall.R
import com.hezaro.wall.utils.GeneralNotificationHelper
import com.hezaro.wall.utils.NotificationBody
import org.json.JSONObject
import timber.log.Timber

class WallFirebaseMessagingService : FirebaseMessagingService() {


    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let {
            it.notification?.let { notification ->
                GeneralNotificationHelper(baseContext).showMessage(
                    NotificationBody(
                        id = it.messageId ?: "1",
                        title = notification.title ?: getString(R.string.app_name),
                        message = notification.body ?: "",
                        bigMessage = getBigMessage(remoteMessage)
                    )
                )
            }
        }
    }

    private fun getBigMessage(remoteMessage: RemoteMessage): String {
        val data = remoteMessage.data
        var bigMessage = ""
        if (data.isNotEmpty()) {
            val json = JSONObject(data)
            bigMessage = json.getString("big_message")
        }
        return bigMessage
    }

    override fun onNewToken(p0: String?) {
        super.onNewToken(p0)
        Timber.i("onNewIntent= $p0")
    }
}