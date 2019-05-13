package com.hezaro.wall.sdk.platform.player

import com.google.android.exoplayer2.Player
import com.hezaro.wall.data.model.Episode

abstract class MediaPlayer {

    abstract val player: Player

    abstract val isPlaying: Boolean

    abstract val isStreaming: Boolean

    abstract val currentPosition: Long

    abstract val duration: Long

    abstract fun getCurrentEpisode(): Episode?

    abstract fun clearPlaylist()

    abstract fun playTrack(e: Episode)

    abstract fun concatPlaylist(p: ArrayList<Episode>)

    abstract fun playPlaylist(p: ArrayList<Episode>, episode: Episode, readyToPlay: Boolean = true)

    abstract fun resumePlayback()

    abstract fun pausePlayback()

    abstract fun stopPlayback()

    abstract fun seekTo(position: Long)

    abstract fun selectTrack(episode: Episode)

    abstract operator fun next()

    abstract fun previous()

    abstract fun setPlaybackSpeed(speed: Float)

    abstract fun onDestroy()
}