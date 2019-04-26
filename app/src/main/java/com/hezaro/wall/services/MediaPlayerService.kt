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
import androidx.lifecycle.MutableLiveData
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
import timber.log.Timber
import java.lang.ref.WeakReference

class MediaPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var mediaSessionHelper: MediaSessionHelper

    private var mediaPlayerState: Int = MediaPlayerState.STATE_IDLE

    var currentEpisode: MutableLiveData<Episode> = MutableLiveData()

    private var headsetIsPlugged: Boolean = false

    private var mServiceBound = false

    private var headsetReceiver: HeadsetReceiver? = null

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
            context, notificationHelper, currentEpisode, mediaPlayerState
        )
        mediaPlayer = LocalMediaPlayer(WeakReference(mediaPlayerListener), this)


        mediaSessionHelper = MediaSessionHelper(context, mediaPlayer!!)
        notificationHelper.initNotificationHelper(mediaSessionHelper.sessionToken)
        headsetReceiver = HeadsetReceiver(mediaPlayer!!)
        headrestPlugged()
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
                        mediaPlayer?.let { media -> media.selectTrack(it) }
                        currentEpisode.postValue(it)
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
                ACTION_SEEK_FORWARD -> seekTo(30000)
                ACTION_SEEK_BACKWARD -> seekTo(-10000)
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
                ACTION_SET_SPEED -> mediaPlayer!!.setPlaybackSpeed(
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

    private fun headrestPlugged() {
        if (!headsetIsPlugged) {
            registerReceiver(headsetReceiver, IntentFilter(Intent.ACTION_HEADSET_PLUG))
            registerReceiver(
                headsetReceiver, IntentFilter(
                    AudioManager.ACTION_AUDIO_BECOMING_NOISY
                )
            )
            headsetIsPlugged = true
        }
    }

    private fun headrestUnPlugged() {
        if (headsetIsPlugged) {
            unregisterReceiver(headsetReceiver)
            headsetIsPlugged = false
        }
    }

    private fun makePlaylist(playlist: Playlist) {
        currentEpisode.postValue(playlist.getFirst())
        mediaPlayer!!.concatPlaylist(playlist)
    }

    private fun endPlayback(cancelNotification: Boolean) {
        currentEpisode.value?.let { it.status = Status.PLAYED }
        headrestUnPlugged()
        if (cancelNotification) {
            notificationHelper.onHide()
        }
    }

    private fun addPlaylist(playlist: Playlist?) {
        currentEpisode.postValue(playlist?.getFirst())
        when (mediaPlayerState) {
            MediaPlayerState.STATE_CONNECTING, MediaPlayerState.STATE_PLAYING, MediaPlayerState.STATE_PAUSED ->

                when {
                    playlist != null -> {
                        endPlayback(false)
                        makePlaylist(playlist)
                    }
                    mediaPlayerState == MediaPlayerState.STATE_PAUSED -> mediaPlayer?.resumePlayback()
                    else -> Timber.w("Player is playing, episode cannot be null")
                }
            MediaPlayerState.STATE_ENDED, MediaPlayerState.STATE_IDLE ->
                // stopped or uninitialized, so we need to start from scratch
                if (playlist != null) {
                    makePlaylist(playlist)
                } else {
                    Timber.w("Player is stopped/uninitialized, episode cannot be null")
                }
            else -> Timber.w(
                "Trying to addPlaylist an episode, but player is in a $mediaPlayerState"
            )
        }
    }

    @Suppress("NAME_SHADOWING")
    private fun seekTo(seekTo: Long) {
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