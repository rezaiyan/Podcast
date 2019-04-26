package com.hezaro.wall.player

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.hezaro.wall.services.MediaPlayerServiceHelper.Companion.sendIntent
import com.hezaro.wall.utils.ACTION_PAUSE
import com.hezaro.wall.utils.ACTION_RESUME_PLAYBACK
import com.hezaro.wall.utils.ACTION_SEEK_BACKWARD
import com.hezaro.wall.utils.ACTION_SEEK_FORWARD

class RemoteControlReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if (Intent.ACTION_MEDIA_BUTTON == intent.action) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)

            when (event.keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY -> sendIntent(context, ACTION_RESUME_PLAYBACK)
                KeyEvent.KEYCODE_MEDIA_PAUSE -> sendIntent(context, ACTION_PAUSE)
                KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> sendIntent(context, ACTION_SEEK_FORWARD)
                KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> sendIntent(context, ACTION_SEEK_BACKWARD)
            }
        }
    }
}
