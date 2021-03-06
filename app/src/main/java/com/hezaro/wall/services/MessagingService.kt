package com.hezaro.wall.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hezaro.wall.R.string
import com.hezaro.wall.notification.GeneralNotificationHelper
import com.hezaro.wall.notification.MessagingViewModel
import com.hezaro.wall.notification.NotificationBody
import org.json.JSONObject
import org.koin.android.ext.android.inject
import timber.log.Timber

class MessagingService : FirebaseMessagingService() {

    private val vm: MessagingViewModel by inject()

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)
        remoteMessage?.let {
            it.notification?.let { notification ->
                GeneralNotificationHelper(baseContext).showMessage(
                    NotificationBody(
                        id = it.messageId ?: "1",
                        title = notification.title ?: getString(string.app_name),
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
        Timber.tag("MessagingService").i("Token: $p0")
        p0?.let { vm.sendToken(it) }
    }
}