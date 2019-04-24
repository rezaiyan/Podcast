package com.hezaro.wall.utils

import android.app.Activity
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_STOP
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import com.hezaro.wall.R
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.services.MediaPlayerService
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target

class PlayerNotificationHelper(
    private val context: Context,
    activity: Activity,
    private val service: MediaPlayerService
) : NotificationHelper, MediaDescriptionAdapter, NotificationListener {


    private val CHANNEL_NAME = R.string.exo_download_notification_channel_name
    private val CHANNEL_ID = "321"
    private val REQUEST_CODE = 991
    private val NOTIFICATION_ID = 114
    private val fastForwardIncrementMs = 30000L
    private val rewindIncrementMs = 10000L
    private lateinit var notificationManager: PlayerNotificationManager
    private val pendingIntent = PendingIntent
        .getActivity(
            context,
            REQUEST_CODE,
            Intent(context, activity.javaClass),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun initNotificationHelper(sessionToken: MediaSessionCompat.Token) {
        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context, CHANNEL_ID, CHANNEL_NAME, NOTIFICATION_ID, this
        ).apply {
            setStopAction(ACTION_STOP)
            setNotificationListener(this@PlayerNotificationHelper)
            setMediaSessionToken(sessionToken)
            setFastForwardIncrementMs(fastForwardIncrementMs)
            setRewindIncrementMs(rewindIncrementMs)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

    }

    override fun onNotificationStarted(notificationId: Int, notification: Notification) {
        service.startForeground(notificationId, notification)
    }

    override fun onNotificationCancelled(notificationId: Int) {
        service.stopSelf()
    }

    override fun getCurrentContentTitle(player: Player): String {
        return service.currentEpisode.value!!.title
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent = pendingIntent

    override fun getCurrentContentText(player: Player): String? {
        return service.currentEpisode.value!!.podcast!!.title
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        Picasso.get()
            .load(service.currentEpisode.value!!.cover)
            .into(object : Target {
                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    callback.onBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.ic_placeholder))
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                }

                override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
                    callback.onBitmap(bitmap)
                }
            })
        return null
    }

    override fun onShow(mediaPlayer: MediaPlayer) {
        notificationManager.setPlayer(mediaPlayer.player)
    }

    override fun onHide() {
        notificationManager.setPlayer(null)
    }

    override fun onDestroy() {
        notificationManager.setPlayer(null)
    }
}