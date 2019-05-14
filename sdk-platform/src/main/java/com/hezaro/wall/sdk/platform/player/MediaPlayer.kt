package com.hezaro.wall.sdk.platform.player

import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.hezaro.wall.data.model.Episode

interface MediaPlayer {

    val player: Player

    val isPlaying: Boolean

    val isStreaming: Boolean

    val currentPosition: Long

    val duration: Long

    fun getCurrentEpisode(): Episode?

    fun clearPlaylist()

    fun playTrack(e: Episode)

    fun concatPlaylist(p: ArrayList<Episode>)

    fun playPlaylist(p: ArrayList<Episode>, episode: Episode, readyToPlay: Boolean = true)

    fun resumePlayback()

    fun pausePlayback()

    fun stopPlayback()

    fun seekTo(position: Long)

    fun selectTrack(episode: Episode)

    operator fun next()

    fun previous()

    fun setPlaybackSpeed(speed: Float)

    fun onDestroy()

    fun setInstanceListener(instanceListener: InstanceListener)
}

interface InstanceListener {
    fun onNewInstance(p: SimpleExoPlayer?, errorOccurred: Boolean)
}