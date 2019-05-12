package com.hezaro.wall.services

import android.content.Context
import android.content.Intent
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.sdk.platform.utils.ACTION_CLEAR_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_EPISODE
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAY_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_PREPARE_PLAYLIST
import com.hezaro.wall.sdk.platform.utils.ACTION_SEEK_BACKWARD
import com.hezaro.wall.sdk.platform.utils.ACTION_SEEK_FORWARD
import com.hezaro.wall.sdk.platform.utils.ACTION_SET_SPEED
import com.hezaro.wall.sdk.platform.utils.ACTION_STOP_SERVICE
import com.hezaro.wall.sdk.platform.utils.PARAM_EPISODE
import com.hezaro.wall.sdk.platform.utils.PARAM_PLAYBACK_SPEED
import com.hezaro.wall.sdk.platform.utils.PARAM_PLAYLIST

class MediaPlayerServiceHelper{

    companion object {

        fun preparePlaylist(context: Context, playlist: ArrayList<Episode>) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_PREPARE_PLAYLIST
            intent.putParcelableArrayListExtra(PARAM_PLAYLIST, playlist)
            context.startService(intent)
        }

        fun prepareAndPlayPlaylist(context: Context, playlist: ArrayList<Episode>, episode: Episode) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_PLAY_PLAYLIST
            intent.putExtra(PARAM_PLAYLIST, playlist)
            intent.putExtra(PARAM_EPISODE, episode)
            context.startService(intent)
        }

        fun clearPlaylist(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_CLEAR_PLAYLIST
            context.startService(intent)
        }

        fun playEpisode(context: Context, episode: Episode) {

            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_PLAY_EPISODE
            intent.putExtra(PARAM_EPISODE, episode)
            context.startService(intent)
        }

        fun changePlaybackSpeed(context: Context, speed: Float) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_SET_SPEED
            intent.putExtra(PARAM_PLAYBACK_SPEED, speed)
            context.startService(intent)
        }

        fun sendIntent(context: Context, action: String) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = action
            context.startService(intent)
        }

        fun seekForward(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_SEEK_FORWARD
            context.startService(intent)
        }

        fun seekBackward(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_SEEK_BACKWARD
            context.startService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_STOP_SERVICE
            context.startService(intent)
        }
    }
}