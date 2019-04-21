package com.hezaro.wall.sdk.platform.player

import com.hezaro.wall.data.model.Episode

interface MediaPlayerListener {

    fun setPlayer(mediaPlayer: MediaPlayer)

    fun onStateChanged(state: Int)

    fun notifyEpisode(episode: Episode)
}