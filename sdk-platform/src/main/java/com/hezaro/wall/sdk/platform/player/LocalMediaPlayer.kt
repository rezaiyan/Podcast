package com.hezaro.wall.sdk.platform.player

import android.annotation.TargetApi
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
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
import timber.log.Timber
import java.lang.ref.WeakReference

class LocalMediaPlayer(mediaPlayerListener: WeakReference<MediaPlayerListener>, private val context: Context) :
    MediaPlayer(mediaPlayerListener), Player.EventListener {

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
        mediaPlayerListener.get()!!.setPlayer(this)
        if (exoPlayer == null) {
            val trackSelector = DefaultTrackSelector()
            val loadControl = DefaultLoadControl()
            val renderersFactory = DefaultRenderersFactory(this.context)
            exoPlayer = ExoPlayerFactory
                .newSimpleInstance(this.context, renderersFactory, trackSelector, loadControl)
            exoPlayer!!.addListener(this)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA)
                .setContentType(C.CONTENT_TYPE_MOVIE)
                .build()
            exoPlayer!!.setAudioAttributes(audioAttributes, true)
            mediaPlayerState = MediaPlayerState.STATE_IDLE
        }
    }

    override fun getCurrentEpisode(): Episode? {
        return episode
    }

    override fun concatPlaylist(playlist: Playlist) {
        if (this.playlist == null) {
            this.playlist = playlist
            for (i in 0 until this.playlist!!.getItems().size) {
                concatenatingMediaSource.addMediaSource(buildMediaSource(playlist.getItem(i)))
            }
        } else {
            for (i in 0 until playlist.getItems().size) {
                val it = playlist.getItem(i)
                if (this.playlist!!.getIndex(it) == -1) {
                    this.playlist!!.getItems().add(it)
                    concatenatingMediaSource.addMediaSource(buildMediaSource(it))
                }
            }
        }

        val beReset = exoPlayer!!.currentWindowIndex > 0 || isPlaying
        if (!beReset) {
            exoPlayer!!.prepare(concatenatingMediaSource)
        }
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
        if (trackGroups!!.isEmpty) {
            episode = playlist!!.getItem(exoPlayer!!.currentWindowIndex)
            listenerReference.notifyEpisode(episode!!)
        }
    }

    override fun selectTrack(episode: Episode) {
        this.episode = episode
        if (playlist != null) {
            val currentIndex = playlist!!.getIndex(episode)

            if (currentIndex > 0 && exoPlayer!!.currentTimeline.windowCount > 0 &&
                exoPlayer!!.currentTimeline.windowCount >= currentIndex && exoPlayer!!.currentWindowIndex != currentIndex || currentIndex == 0
            ) {
                exoPlayer!!.seekTo(currentIndex, 0)
                exoPlayer!!.playWhenReady = true
            }
        }
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
        listenerReference.onStateChanged(mediaPlayerState)
        Timber.tag(TAG).d(
            String.format(
                "ExoPlayer state changed: %s, Play When Ready: %s",
                playbackStateStr,
                playWhenReady.toString()
            )
        )
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        Toast.makeText(context, error!!.cause!!.message, Toast.LENGTH_SHORT).show()
        Timber.w(error, "Player error encountered")
        stopPlayback()
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun setPlaybackSpeed(speed: Float) {
        val playbackParams = PlaybackParameters(speed)
        exoPlayer!!.playbackParameters = playbackParams
    }

    private fun buildMediaSource(mEpisode: Episode): MediaSource {
        val uri = Uri.parse(mEpisode.remoteMediaUrl)
        isStreaming = true

        if (uri != null) {
            Timber.tag(TAG).d("Playing from URI %s", uri)
            return ExtractorMediaSource.Factory(dataSourceFactory).setTag(mEpisode.description)
                .createMediaSource(uri)
        }
        throw IllegalStateException("Unable to build media source")
    }

/*
    private fun getCacheDataSource(cacheDir: File): CacheDataSourceFactory {
        if (cache == null) {
            cache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE.toLong()))
        }

        val upstream = DefaultHttpDataSourceFactory(
            "wall.userAgent", null,
            DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
            DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true
        )
        return CacheDataSourceFactory(
            cache, upstream,
            CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS,
            CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE
        )
    }*/


    companion object {

        private val TAG = LocalMediaPlayer::class.java.simpleName

    }
}
