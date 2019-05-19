package com.hezaro.wall.notification.player

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.ui.PlayerNotificationManager.ACTION_STOP
import com.google.android.exoplayer2.ui.PlayerNotificationManager.BitmapCallback
import com.google.android.exoplayer2.ui.PlayerNotificationManager.MediaDescriptionAdapter
import com.google.android.exoplayer2.ui.PlayerNotificationManager.NotificationListener
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.feature.main.MainActivity
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE_GET
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.services.MediaPlayerService
import com.hezaro.wall.utils.toBitmap
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target

class PlayerNotificationHelper(
    private val context: Context,
    private val service: MediaPlayerService,
    private val mediaSession: MediaSessionHelper
) : NotificationHelper, MediaDescriptionAdapter, NotificationListener {

    private val CHANNEL_NAME = R.string.exo_download_notification_channel_name
    private val CHANNEL_ID = "321"
    private val REQUEST_CODE = 991
    private val NOTIFICATION_ID = 114
    var episode: Episode? = null
    private lateinit var notificationManager: PlayerNotificationManager
    private val pendingIntent = PendingIntent
        .getActivity(
            context,
            REQUEST_CODE,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    private var receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent != null) {

                when (intent.action) {
                    ACTION_EPISODE -> {
                        episode = intent.getParcelableExtra(ACTION_EPISODE_GET)
                    }
                    ACTION_PLAYER -> {
                        val action = intent.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE)
                        when (action) {
                            MediaPlayerState.STATE_IDLE, MediaPlayerState.STATE_ENDED -> {
                                service.stopSelf()
                            }
                            MediaPlayerState.STATE_PLAYING -> {
                                onGoing(true); onShow(service.mediaPlayer)
                            }
                            MediaPlayerState.STATE_PAUSED -> {
                                onGoing(false)
                            }
                        }
                    }
                }
            }
        }
    }

    fun initNotificationHelper() {

        notificationManager = PlayerNotificationManager.createWithNotificationChannel(
            context, CHANNEL_ID, CHANNEL_NAME, NOTIFICATION_ID, this
        )
            .apply {
                setNotificationListener(this@PlayerNotificationHelper)
                setMediaSessionToken(mediaSession.sessionToken)
                setFastForwardIncrementMs(0)
                setRewindIncrementMs(0)
                setPriority(NotificationCompat.PRIORITY_DEFAULT)
                setStopAction(ACTION_STOP)
            }

        LocalBroadcastManager.getInstance(context)
            .registerReceiver(receiver, IntentFilter(ACTION_EPISODE).also { it.addAction(ACTION_PLAYER) })
    }

    override fun onNotificationStarted(notificationId: Int, notification: Notification) {
        service.startForeground(notificationId, notification)
    }

    override fun onNotificationCancelled(notificationId: Int) {
        service.stopSelf()
    }

    override fun getCurrentContentTitle(player: Player): String {
        return episode?.title ?: "Unknown"
    }

    override fun createCurrentContentIntent(player: Player): PendingIntent = pendingIntent

    override fun getCurrentContentText(player: Player): String? {
        return episode?.podcast?.title ?: "Unknown"
    }

    override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
        episode?.let {
            if (it.cover.isNotEmpty())
                Picasso.get()
                    .load(it.cover)
                    .into(object : Target {
                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                            callback.onBitmap(
                                BitmapFactory.decodeResource(
                                    context.resources,
                                    R.drawable.ic_placeholder
                                )
                            )
                        }

                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        }

                        override fun onBitmapLoaded(bitmap: Bitmap?, from: LoadedFrom?) {
                            callback.onBitmap(bitmap)
                        }
                    })
        }

        return AppCompatResources.getDrawable(context, R.drawable.placeholder)?.toBitmap()
    }

    override fun onShow(mediaPlayer: MediaPlayer) {
        notificationManager.setPlayer(mediaPlayer.player)
    }

    override fun onHide() {
        notificationManager.setPlayer(null)
    }

    override fun onGoing(onGoing: Boolean) {
        notificationManager.setOngoing(onGoing)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        mediaSession.onDestroy()
        notificationManager.setPlayer(null)
    }
}