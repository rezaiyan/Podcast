package com.hezaro.wall.services

import android.content.Context
import android.content.Intent
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist

class MediaPlayerServiceHelper{

    companion object {

        fun playPlaylist(context: Context, playlist: Playlist) {
            val intent = Intent(context, MediaPlayerService::class.java)
            intent.action = ACTION_PLAY_PLAYLIST
            intent.putExtra(PARAM_PLAYLIST, playlist)
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
    }
}