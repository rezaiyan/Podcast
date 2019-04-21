package com.hezaro.wall.sdk.platform.player

import com.google.android.exoplayer2.Player
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import java.lang.ref.WeakReference

abstract class MediaPlayer(wakeListener: WeakReference<MediaPlayerListener>) {

    var listenerReference: MediaPlayerListener = wakeListener.get()!!

    abstract val player: Player

    abstract val isPlaying: Boolean

    abstract val isStreaming: Boolean

    abstract val currentPosition: Long

    abstract val duration: Long

    abstract fun getCurrentEpisode(): Episode?

    abstract fun concatPlaylist(playlist: Playlist)

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