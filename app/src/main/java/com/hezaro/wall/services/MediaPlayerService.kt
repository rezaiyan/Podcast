package com.hezaro.wall.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status
import com.hezaro.wall.notification.player.PlayerNotificationHelper
import com.hezaro.wall.sdk.platform.player.InstanceListener
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.player.MediaPlayerState.STATE_PLAYING
import com.hezaro.wall.sdk.platform.utils.ACTION_CLEAR_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_PAUSE
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_EPISODE_OF_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_PAUSE
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_QUEUE
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_SINGLE_EPISODE
import com.hezaro.wall.sdk.platform.utils.ACTION_PREPARE_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_RESUME_PLAYBACK
import com.hezaro.wall.sdk.platform.utils.ACTION_SEEK_BACKWARD
import com.hezaro.wall.sdk.platform.utils.ACTION_SEEK_FORWARD
import com.hezaro.wall.sdk.platform.utils.ACTION_SEEK_TO
import com.hezaro.wall.sdk.platform.utils.ACTION_SET_SPEED
import com.hezaro.wall.sdk.platform.utils.ACTION_SLEEP_TIMER
import com.hezaro.wall.sdk.platform.utils.ACTION_STOP_SERVICE
import com.hezaro.wall.sdk.platform.utils.DEFAULT_PLAYBACK_SPEED
import com.hezaro.wall.sdk.platform.utils.PARAM_EPISODE
import com.hezaro.wall.sdk.platform.utils.PARAM_PLAYBACK_SPEED
import com.hezaro.wall.sdk.platform.utils.PARAM_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.PARAM_SEEK_MS
import com.hezaro.wall.sdk.platform.utils.fastForwardIncrementMs
import com.hezaro.wall.sdk.platform.utils.rewindIncrementMs
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import timber.log.Timber

class MediaPlayerService : Service() {

    private val mediaPlayerState: Int
        get() = mediaPlayer.player.playbackState

    val currentEpisode: Episode?
        get() = mediaPlayer.getCurrentEpisode()

    private var mServiceBound = false

    private val notificationHelper: PlayerNotificationHelper by inject { parametersOf(this@MediaPlayerService) }
    val mediaPlayer: MediaPlayer by inject()

    val player = MutableLiveData<Player>()
    val liveError = MutableLiveData<Pair<Boolean, Player>>()

    inner class ServiceBinder : Binder() {

        val service: MediaPlayerService
            get() = this@MediaPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        liveError.value = Pair(false, mediaPlayer.player)
        player.value = mediaPlayer.player
        mediaPlayer.init()
        mediaPlayer.setInstanceListener(object : InstanceListener {
            override fun onNewInstance(
                p: SimpleExoPlayer?,
                errorOccurred: Boolean
            ) {
                player.value = mediaPlayer.player
                liveError.value = Pair(errorOccurred, mediaPlayer.player)
            }
        })
    }

    override fun onDestroy() {
        endPlayback()
        mediaPlayer.stopPlayback()
        player.value!!.stop()
        notificationHelper.onDestroy()
        mediaPlayer.onDestroy()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent != null && intent.action != null) {
            val action = intent.action

            when (action) {
                ACTION_PLAY_QUEUE -> player.value!!.next()
                ACTION_PLAY_EPISODE_OF_PLAYLIST -> {
                    intent.getParcelableExtra<Episode>(PARAM_EPISODE)?.let {
                        mediaPlayer.selectTrack(it)
                    }
                }
                ACTION_PLAY_SINGLE_EPISODE -> {
                    intent.getParcelableExtra<Episode>(PARAM_EPISODE)?.let {
                        mediaPlayer.playTrack(it)
                    }
                }
                ACTION_PREPARE_PLAYLIST -> addPlaylist(
                    intent.extras!!.getParcelableArrayList(
                        PARAM_PLAYLIST
                    )!!
                )
                ACTION_PLAY_PLAYLIST -> addPlaylist(
                    intent.extras!!.getParcelableArrayList(PARAM_PLAYLIST)!!,
                    intent.extras!!.getParcelable(PARAM_EPISODE)!!
                )
                ACTION_CLEAR_PLAYLIST -> clearPlaylist()
                ACTION_RESUME_PLAYBACK -> mediaPlayer.resumePlayback()
                ACTION_PLAY_PAUSE -> if (mediaPlayer.isPlaying) {
                    mediaPlayer.pausePlayback()
                } else {
                    mediaPlayer.resumePlayback()
                }
                ACTION_PAUSE -> mediaPlayer.pausePlayback()
                ACTION_SEEK_FORWARD -> seekTo(fastForwardIncrementMs)
                ACTION_SEEK_BACKWARD -> seekTo(-rewindIncrementMs)
                ACTION_SEEK_TO -> seekTo(intent.getIntExtra(PARAM_SEEK_MS, 30).toLong())
                ACTION_STOP_SERVICE -> {
                    notificationHelper.onHide()
                }
                ACTION_SLEEP_TIMER -> {
                    player.value!!.stop()
                    stopSelf()
                }
                ACTION_SET_SPEED ->
                    mediaPlayer.setPlaybackSpeed(
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
        notificationHelper.onGoing(false)
        if (mediaPlayerState == MediaPlayerState.STATE_IDLE) {
            stopSelf()
        }
        mServiceBound = false
        return super.onUnbind(intent)
    }

    override fun onBind(intent: Intent): IBinder? {
        notificationHelper.onGoing(true)
        Timber.d("Binded to service")
        mServiceBound = true
        return ServiceBinder()
    }

    private fun endPlayback() {
        currentEpisode?.playStatus = Status.PLAYED
    }

    private fun clearPlaylist() = mediaPlayer.clearPlaylist()

    private fun addPlaylist(playlist: ArrayList<Episode>, episode: Episode? = null) {
        if (episode == null)
            mediaPlayer.concatPlaylist(playlist)
        else mediaPlayer.playPlaylist(playlist, episode)
    }

    @Suppress("NAME_SHADOWING")
    private fun seekTo(msec: Long) {
        val seekTo = msec + player.value!!.currentPosition
        when (mediaPlayerState) {
            MediaPlayerState.STATE_PAUSED, STATE_PLAYING -> {
                var seekTo = seekTo

                if (seekTo < 0) {
                    seekTo = 0
                } else if (seekTo > mediaPlayer.duration) {
                    seekTo = mediaPlayer.duration
                }
                mediaPlayer.seekTo(seekTo)
            }
        }
    }
}