package com.hezaro.wall.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import timber.log.Timber

class HeadsetReceiver(private val mediaPlayer: MediaPlayer) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (isInitialStickyBroadcast) {
            return
        }

        val action = intent.action
        Timber.d("Action: %s", action)

        // physical headphone events
        if (action != null) {
            when (action) {

                Intent.ACTION_HEADSET_PLUG -> {
                    val state = intent.getIntExtra("state", -1)

                    if (state == 0) {
                        mediaPlayer.pausePlayback()
                        Timber.d("Headset unplugged")
                    }
                }
                AudioManager.ACTION_AUDIO_BECOMING_NOISY -> mediaPlayer.pausePlayback()
            }
        }
    }
}