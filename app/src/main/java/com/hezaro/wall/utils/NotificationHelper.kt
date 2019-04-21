package com.hezaro.wall.utils

import com.hezaro.wall.sdk.platform.player.MediaPlayer

interface NotificationHelper{

    fun onShow(mediaPlayer: MediaPlayer)
    fun onHide()
    fun onDestroy()
}