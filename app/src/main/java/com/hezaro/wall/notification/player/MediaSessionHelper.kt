package com.hezaro.wall.notification.player

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.Token
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.Builder
import androidx.core.graphics.drawable.toBitmap
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.hezaro.wall.R
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.receivers.RemoteControlReceiver
import com.hezaro.wall.sdk.platform.ext.loadWith
import com.hezaro.wall.sdk.platform.player.MediaPlayer
import com.hezaro.wall.sdk.platform.player.MediaPlayerState
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER_STATUS
import com.hezaro.wall.sdk.platform.utils.MEDIA_SESSION_ACTIONS
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom
import com.squareup.picasso.Target
import timber.log.Timber

private const val REQ_CODE_MEDIA_BUTTON_RECEIVER = 151

//TODO The MediaSessionController & MediaSessionConnector have to be add
class MediaSessionHelper(private val context: Context, private val mediaPlayer: MediaPlayer) {

    private val updateSessionReceiver = UpdateMediaSessionReceiver()
    private val TAG = MediaSessionHelper::class.java.simpleName
    private var mediaSession: MediaSessionCompat =
        MediaSessionCompat(context, TAG)
    val sessionToken: Token = mediaSession.sessionToken
    private var playbackState: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
        .build()
        set(value) {
            mediaSession.setPlaybackState(value)
        }

    init {
        IntentFilter(ACTION_PLAYER).let {
            LocalBroadcastManager.getInstance(context).registerReceiver(updateSessionReceiver, it)
        }

        val sessionIntent = context.packageManager?.getLaunchIntentForPackage(context.packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(context, 0, sessionIntent, 0)
        mediaSession.apply {
            setCallback(MediaSessionCallback())
            setMediaButtonReceiver(getMediaButtonReceiverIntent(context))
            setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        playbackState = PlaybackStateCompat.Builder()
            .setState(PlaybackStateCompat.STATE_NONE, 0, 1.0f)
            .build()
    }

    fun onDestroy() {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(updateSessionReceiver)
        mediaSession.run {
            isActive = false
            release()
        }
    }

    inner class UpdateMediaSessionReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                updatePlaybackState(it.getIntExtra(ACTION_PLAYER_STATUS, MediaPlayerState.STATE_IDLE))
            }
        }
    }

    private fun updatePlaybackState(stateVal: Int) {
        // update current position
        val currentPosition = mediaPlayer.currentPosition
        val playerState: Int
        var actions = MEDIA_SESSION_ACTIONS

        when (stateVal) {
            MediaPlayerState.STATE_CONNECTING -> {
                playerState = PlaybackStateCompat.STATE_CONNECTING
                actions = actions or PlaybackStateCompat.ACTION_PAUSE
            }
            MediaPlayerState.STATE_IDLE -> {
                playerState = PlaybackStateCompat.STATE_NONE
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
            MediaPlayerState.STATE_ENDED -> {
                playerState = PlaybackStateCompat.STATE_STOPPED
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
            MediaPlayerState.STATE_PAUSED -> {
                playerState = PlaybackStateCompat.STATE_PAUSED
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
            MediaPlayerState.STATE_PLAYING -> {
                playerState = PlaybackStateCompat.STATE_PLAYING
                actions = actions or PlaybackStateCompat.ACTION_PAUSE
            }
            else -> {
                playerState = PlaybackStateCompat.STATE_NONE
                actions = actions or PlaybackStateCompat.ACTION_PLAY
            }
        }

        mediaPlayer.getCurrentEpisode()?.let {
            Picasso.get()
                .loadWith(it.cover, R.drawable.ic_placeholder)
                .error(R.drawable.ic_placeholder)
                .into(object : Target {
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable) {
                        Timber.e(e)
                        updateMediaSession(it, errorDrawable.toBitmap(), actions, playerState, currentPosition)
                    }

                    override fun onBitmapLoaded(bitmap: Bitmap, from: LoadedFrom?) {
                        updateMediaSession(it, bitmap, actions, playerState, currentPosition)
                    }
                })
        }
    }

    private fun updateMediaSession(
        episode: Episode,
        bitmap: Bitmap,
        actions: Long,
        playerState: Int,
        currentPosition: Long
    ) {

        playbackState = Builder()
            .setState(playerState, currentPosition, 1.0f)
            .setActions(actions)
            .build()

        episode.apply {
            mediaSession.setMetadata(
                MediaMetadataCompat.Builder()
//                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, podcast.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_AUTHOR, podcast.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, description)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
                    .build()
            )
        }
    }

    private fun getMediaButtonReceiverIntent(context: Context): PendingIntent {
        val mediaButtonReceiver = ComponentName(context, RemoteControlReceiver::class.java)
        return PendingIntent.getBroadcast(
            context, REQ_CODE_MEDIA_BUTTON_RECEIVER,
            Intent().setComponent(mediaButtonReceiver), PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlayFromSearch(query: String?, extras: Bundle?) {
            Timber.i("query%s", query)
        }

        override fun onSeekTo(pos: Long) {
            mediaPlayer.seekTo(pos)
        }

        override fun onPlay() {
            mediaPlayer.resumePlayback()
        }

        override fun onPause() {
            mediaPlayer.pausePlayback()
        }

        override fun onFastForward() {
            mediaPlayer.next()
        }

        override fun onRewind() {
            mediaPlayer.previous()
        }

        override fun onSkipToNext() {
            onFastForward()
        }

        override fun onSkipToPrevious() {
            onRewind()
        }
    }
}