package com.hezaro.wall.sdk.platform.player

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Playlist
import com.hezaro.wall.sdk.platform.player.download.PlayerDownloadHelper
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE
import com.hezaro.wall.sdk.platform.utils.ACTION_EPISODE_GET
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER
import com.hezaro.wall.sdk.platform.utils.ACTION_PLAYER_STATUS
import timber.log.Timber

class LocalMediaPlayer(private val context: Context) : MediaPlayer(), Player.EventListener {

    private var exoPlayer: SimpleExoPlayer? = null

    private var episode: Episode? = null

    override var isStreaming: Boolean = false
        private set

    private var mediaPlayerState: Int = 0

    private var playlist: Playlist? = null

    private val concatenatingMediaSource = ConcatenatingMediaSource()

    override val player: Player
        get() = exoPlayer!!

    override val currentPosition: Long
        get() = exoPlayer!!.currentPosition

    override val duration: Long
        get() = exoPlayer!!.duration

    override val isPlaying: Boolean
        get() = exoPlayer!!.playWhenReady

    private val dataSourceFactory by lazy {
        PlayerDownloadHelper(context)
            .buildDataSourceFactory()
    }

    init {
        if (exoPlayer == null) {
            val trackSelector = DefaultTrackSelector()
            val loadControl = DefaultLoadControl()
            val factory = DefaultRenderersFactory(this.context)
            exoPlayer = ExoPlayerFactory
                .newSimpleInstance(this.context, factory, trackSelector, loadControl)
            exoPlayer!!.addListener(this)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
            exoPlayer!!.setAudioAttributes(audioAttributes, true)
            mediaPlayerState = MediaPlayerState.STATE_IDLE
        }
    }

    override fun clearPlaylist() {
        playlist?.getItems()?.clear()
        concatenatingMediaSource.clear()
        playlist = null
    }

    override fun getCurrentEpisode(): Episode? {
        return episode
    }

    override fun playPlaylist(p: ArrayList<Episode>, episode: Episode, readyToPlay: Boolean) {
        this.playlist?.getItems()?.clear()
        concatenatingMediaSource.clear()
        this.playlist = Playlist(p)
        for (i in 0 until this.playlist!!.getItems().size)
            concatenatingMediaSource.addMediaSource(buildMediaSource(playlist!!.getItem(i)))

        Timber.tag("MediaPlayer").i("concatenatingMediaSource.size= ${concatenatingMediaSource.size}")
        exoPlayer!!.prepare(concatenatingMediaSource)

        this.episode = episode
        broadcastEpisode(episode)
        val currentIndex = playlist!!.getIndex(episode)
        exoPlayer!!.seekTo(currentIndex, episode.state)
        exoPlayer!!.playWhenReady = readyToPlay
        Timber.tag("MediaPlayer").i("Selected track index = $currentIndex")
    }

    override fun concatPlaylist(p: ArrayList<Episode>) {
        if (this.playlist == null) {
            this.playlist = Playlist(p)
            for (i in 0 until this.playlist!!.getItems().size) {
                concatenatingMediaSource.addMediaSource(buildMediaSource(playlist!!.getItem(i)))
            }
        } else {
            for (i in 0 until playlist!!.getItems().size) {
                val it = playlist!!.getItem(i)
                if (this.playlist!!.getIndex(it) == -1) {
                    this.playlist!!.addItem(it)
                    concatenatingMediaSource.addMediaSource(buildMediaSource(it))
                }
            }
        }
        Timber.tag("MediaPlayer").i("concatenatingMediaSource.size= ${concatenatingMediaSource.size}")
        val beReset = exoPlayer!!.currentWindowIndex > 0 || isPlaying
        if (!beReset) {
            exoPlayer!!.prepare(concatenatingMediaSource)
        }
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        if (trackGroups!!.isEmpty) {
            episode = playlist!!.getItem(exoPlayer!!.currentWindowIndex)
            broadcastEpisode(episode!!)
        }
    }

    private var errorOccurred = false
    override fun selectTrack(episode: Episode) {
        this.episode = episode
        val currentIndex = playlist?.getIndex(episode) ?: -1
        if (playlist != null && currentIndex > 0) {
            if (exoPlayer!!.currentTimeline.windowCount > 0 &&
                exoPlayer!!.currentTimeline.windowCount >= currentIndex && exoPlayer!!.currentWindowIndex != currentIndex || currentIndex == 0
            ) {
                Timber.tag("MediaPlayer").i("Selected track index = $currentIndex")
                exoPlayer!!.seekTo(currentIndex, if (episode.state >= 0) episode.state else 0)
                exoPlayer!!.playWhenReady = true
            }
        } else playPlaylist(arrayListOf(episode), episode, false)
    }

    override fun next() {
        exoPlayer!!.next()
    }

    override fun previous() {
        exoPlayer!!.previous()
    }

    override fun resumePlayback() {
        exoPlayer!!.playWhenReady = true
    }

    override fun pausePlayback() {
        exoPlayer!!.playWhenReady = false
    }

    override fun stopPlayback() {
        exoPlayer!!.stop()
        isStreaming = false
    }

    override fun seekTo(position: Long) {
        exoPlayer!!.seekTo(position)
    }

    override fun onDestroy() {
        Timber.tag(TAG).d("Tearing down")
        exoPlayer!!.release()
        exoPlayer!!.removeListener(this)
    }

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        val playbackStateStr: String

        when (playbackState) {
            Player.STATE_BUFFERING -> {
                mediaPlayerState = MediaPlayerState.STATE_CONNECTING
                playbackStateStr = "Buffering"
            }
            Player.STATE_ENDED -> {
                mediaPlayerState = MediaPlayerState.STATE_ENDED
                playbackStateStr = "Ended"
            }
            Player.STATE_IDLE -> {
                mediaPlayerState = MediaPlayerState.STATE_IDLE
                playbackStateStr = "Idle"
            }
            Player.STATE_READY -> {
                mediaPlayerState = if (playWhenReady)
                    MediaPlayerState.STATE_PLAYING
                else
                    MediaPlayerState.STATE_PAUSED
                playbackStateStr = "Ready"
            }
            else -> {
                mediaPlayerState = MediaPlayerState.STATE_IDLE
                playbackStateStr = "Unknown"
            }
        }
        broadcastStatus(mediaPlayerState)
        Timber.tag(TAG).d(
            String.format(
                "ExoPlayer state changed: %s, Play When Ready: %s",
                playbackStateStr,
                playWhenReady.toString()
            )
        )
    }

    private fun broadcastEpisode(episode: Episode) {
        val intent = Intent(ACTION_EPISODE)
        intent.putExtra(ACTION_EPISODE_GET, episode)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    private fun broadcastStatus(status: Int) {
        val intent = Intent(ACTION_PLAYER)
        intent.putExtra(ACTION_PLAYER_STATUS, status)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        errorOccurred = true
        Timber.w(error, "Player error encountered -> $error")
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun setPlaybackSpeed(speed: Float) {
        val playbackParams = PlaybackParameters(speed)
        exoPlayer!!.playbackParameters = playbackParams
    }

    private fun buildMediaSource(mEpisode: Episode): MediaSource {
        val uri = Uri.parse(mEpisode.source)
        isStreaming = true

        if (uri != null) {
            return ExtractorMediaSource.Factory(dataSourceFactory).setTag(mEpisode.description)
                .createMediaSource(uri)
        }
        throw IllegalStateException("Unable to build media source")
    }

    companion object {

        private val TAG = LocalMediaPlayer::class.java.simpleName
    }
}
