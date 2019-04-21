package com.hezaro.wall.services

import android.content.Context
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayerListener
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.player.MediaPlayerState.STATE_PLAYING
import com.hezaro.wall.utils.ACTION_EPISODE
import com.hezaro.wall.utils.ACTION_EPISODE_GET
import com.hezaro.wall.utils.ACTION_PLAYER
import com.hezaro.wall.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.utils.NotificationHelper
import timber.log.Timber

class MediaPlayerListenerImpl(
    private val context: Context,
    private val notificationHelper: NotificationHelper,
    private val currentEpisode: MutableLiveData<Episode>,
    private var onStateChanged: Int
) : MediaPlayerListener {

    lateinit var mediaPlayer: MediaPlayer

    override fun setPlayer(mediaPlayer: MediaPlayer) {
        this.mediaPlayer = mediaPlayer
    }

    override fun onStateChanged(state: Int) {
        broadcastStatus(state)
        onStateChanged = state
        when (state) {
            MediaPlayerState.STATE_CONNECTING -> {
            }
            MediaPlayerState.STATE_ENDED -> {
                currentEpisode.value!!.status = Status.PLAYED
                mediaPlayer.next()
            }
            MediaPlayerState.STATE_IDLE -> updateEpisode(state)
            STATE_PLAYING -> {
                if (::mediaPlayer.isInitialized) {

                    notificationHelper.onShow(mediaPlayer)
                    updateEpisode(state)
                }
            }
            MediaPlayerState.STATE_PAUSED -> updateEpisode(state)
        }
    }

    private fun updateEpisode(state: Int) {
        Timber.d("Updating episode, state: %d", state)
        if (::mediaPlayer.isInitialized || currentEpisode.value!!.id == -1) {
            return
        }

        when (state) {
            STATE_PLAYING ->
                currentEpisode.value?.let {
                    it.status = Status.IN_PROGRESS
                }
            MediaPlayerState.STATE_PAUSED, MediaPlayerState.STATE_IDLE -> {
                currentEpisode.value?.let {
                    it.status = Status.PLAYED
                }
            }
            else -> throw IllegalArgumentException(
                "Incorrect state for showing addPlaylist pause notification"
            )
        }
    }

    override fun notifyEpisode(episode: Episode) {
        currentEpisode.value = episode
        val intent = Intent(ACTION_EPISODE)
        intent.putExtra(ACTION_EPISODE_GET, episode)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun broadcastStatus(status: Int) {
        val intent = Intent(ACTION_PLAYER)
        intent.putExtra(ACTION_PLAYER_STATUS, status)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
