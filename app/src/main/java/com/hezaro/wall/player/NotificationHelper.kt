package com.hezaro.wall.player

import com.hezaro.wall.sdk.platform.player.MediaPlayer

interface NotificationHelper{

    fun onShow(mediaPlayer: MediaPlayer)
    fun onHide()
    fun onDestroy()
    fun onGoing(onGoing: Boolean)
}