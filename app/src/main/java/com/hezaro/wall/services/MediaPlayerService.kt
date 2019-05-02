package com.hezaro.wall.services

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Binder
import android.os.IBinder
import android.support.v4.media.session.PlaybackStateCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.Player
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.data.model.Status
import com.hezaro.wall.player.MediaPlayerListenerImpl
import com.hezaro.wall.player.MediaSessionHelper
import com.hezaro.wall.player.PlayerNotificationHelper
import com.hezaro.wall.receivers.HeadsetReceiver
import com.hezaro.wall.sdk.platform.player.LocalMediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.player.MediaPlayerState.STATE_PLAYING
import com.hezaro.wall.utils.ACTION_PAUSE
import com.hezaro.wall.utils.ACTION_PLAY_EPISODE
import com.hezaro.wall.utils.ACTION_PLAY_PAUSE
import com.hezaro.wall.utils.ACTION_PLAY_PLAYLIST
import com.hezaro.wall.utils.ACTION_PLAY_QUEUE
import com.hezaro.wall.utils.ACTION_RESUME_PLAYBACK
import com.hezaro.wall.utils.ACTION_SEEK_BACKWARD
import com.hezaro.wall.utils.ACTION_SEEK_FORWARD
import com.hezaro.wall.utils.ACTION_SEEK_TO
import com.hezaro.wall.utils.ACTION_SET_SPEED
import com.hezaro.wall.utils.ACTION_SLEEP_TIMER
import com.hezaro.wall.utils.ACTION_STOP_SERVICE
import com.hezaro.wall.utils.DEFAULT_PLAYBACK_SPEED
import com.hezaro.wall.utils.MEDIA_SESSION_ACTIONS
import com.hezaro.wall.utils.PARAM_EPISODE
import com.hezaro.wall.utils.PARAM_PLAYBACK_SPEED
import com.hezaro.wall.utils.PARAM_PLAYLIST
import com.hezaro.wall.utils.PARAM_SEEK_MS
import com.hezaro.wall.utils.fastForwardIncrementMs
import com.hezaro.wall.utils.rewindIncrementMs
import timber.log.Timber
import java.lang.ref.WeakReference

class MediaPlayerService : Service() {

    var mediaPlayer: MediaPlayer? = null

    private lateinit var mediaSessionHelper: MediaSessionHelper

    private var mediaPlayerState: Int = MediaPlayerState.STATE_IDLE

    var currentEpisode: Episode? = null

    private var headsetReceiverIsRegistered: Boolean = false

    private var mServiceBound = false

    private val headsetReceiver: HeadsetReceiver by lazy { HeadsetReceiver { mediaPlayer!!.pausePlayback() } }

    private lateinit var notificationHelper: PlayerNotificationHelper

    private val context by lazy<Context> { this }

    private val mBinder = ServiceBinder()

    val player: Player
        get() = mediaPlayer!!.player

    inner class ServiceBinder : Binder() {

        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    fun serviceConnected(activity: Activity) {

        notificationHelper = PlayerNotificationHelper(context, activity, this)

        val mediaPlayerListener = MediaPlayerListenerImpl(
            context, notificationHelper, currentEpisode,
            { this.mediaPlayerState = it },
            { this.currentEpisode = it })

        mediaPlayer = LocalMediaPlayer(WeakReference(mediaPlayerListener), this)

        mediaSessionHelper = MediaSessionHelper(context, mediaPlayer!!)
        notificationHelper.initNotificationHelper(mediaSessionHelper.sessionToken)
        headsetReceiver.let {
            if (!headsetReceiverIsRegistered) {
                val iff = IntentFilter(Intent.ACTION_HEADSET_PLUG)
                iff.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                LocalBroadcastManager.getInstance(this).registerReceiver(it, iff)
                headsetReceiverIsRegistered = true
            }

        }
    }

    override fun onDestroy() {
        if (mediaPlayer == null) {
            return
        }
        endPlayback(true)
        mediaPlayerState = MediaPlayerState.STATE_IDLE
        mediaSessionHelper.playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, mediaPlayer!!.currentPosition, 1.0f)
            .setActions(MEDIA_SESSION_ACTIONS or PlaybackStateCompat.ACTION_PLAY)
            .build()
        headrestUnPlugged()
        notificationHelper.onDestroy()
        mediaSessionHelper.onDestroy()
        mediaPlayer!!.onDestroy()
        mediaPlayer = null
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action != null) {
            val action = intent.action

            when (action) {
                ACTION_PLAY_QUEUE -> player.next()
                ACTION_PLAY_EPISODE -> {
                    intent.getParcelableExtra<Episode>(PARAM_EPISODE)?.let {
                        currentEpisode = it
                        mediaPlayer?.selectTrack(it)
                    }
                }
                ACTION_PLAY_PLAYLIST -> addPlaylist(intent.extras!!.getParcelable(PARAM_PLAYLIST))
                ACTION_RESUME_PLAYBACK -> mediaPlayer!!.resumePlayback()
                ACTION_PLAY_PAUSE -> if (mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pausePlayback()
                } else {
                    mediaPlayer!!.resumePlayback()
                }
                ACTION_PAUSE -> mediaPlayer!!.pausePlayback()
                ACTION_SEEK_FORWARD -> seekTo(fastForwardIncrementMs)
                ACTION_SEEK_BACKWARD -> seekTo(-rewindIncrementMs)
                ACTION_SEEK_TO -> seekTo(intent.getIntExtra(PARAM_SEEK_MS, 30).toLong())
                ACTION_STOP_SERVICE -> {
                    endPlayback(true)
                    if (!mServiceBound) {
                        stopSelf()
                    }
                }
                ACTION_SLEEP_TIMER -> {
                    player.stop()
                    stopSelf()
                }
                ACTION_SET_SPEED ->
                    mediaPlayer?.setPlaybackSpeed(
                        intent.getFloatExtra(
                            PARAM_PLAYBACK_SPEED,
                            DEFAULT_PLAYBACK_SPEED
                        )
                    )

            }
        }
        return Service.START_NOT_STICKY
    }

    override fun onUnbind(intent: Intent): Boolean {
        Timber.d("Unbinded from service")

        if (mediaPlayerState == MediaPlayerState.STATE_IDLE) {
            stopSelf()
        }
        mServiceBound = false
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        Timber.d("Binded to service")
        mServiceBound = true
        return mBinder
    }

    private fun headrestUnPlugged() {
        if (headsetReceiverIsRegistered) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(headsetReceiver)
            unregisterReceiver(headsetReceiver)
            headsetReceiverIsRegistered = false
        }
    }

    private fun endPlayback(cancelNotification: Boolean) {
        currentEpisode!!.status = Status.PLAYED
        if (cancelNotification) {
            notificationHelper.onHide()
        }
    }

    private fun addPlaylist(playlist: Playlist?) {
        if (playlist != null) mediaPlayer!!.concatPlaylist(playlist)
        else Timber.w("Player is playing, episode cannot be null")
    }

    @Suppress("NAME_SHADOWING")
    private fun seekTo(msec: Long) {
        val seekTo = msec + player.currentPosition
        when (mediaPlayerState) {
            MediaPlayerState.STATE_PAUSED, STATE_PLAYING -> {
                var seekTo = seekTo

                if (seekTo < 0) {
                    seekTo = 0
                } else if (seekTo > mediaPlayer!!.duration) {
                    seekTo = mediaPlayer!!.duration
                }
                mediaPlayer!!.seekTo(seekTo)
            }
        }
    }
}