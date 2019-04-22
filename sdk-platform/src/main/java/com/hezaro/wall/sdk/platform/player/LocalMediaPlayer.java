package com.hezaro.wall.sdk.platform.player;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.hezaro.wall.data.model.Episode;
import com.hezaro.wall.data.model.Playlist;
import java.io.File;
import java.lang.ref.WeakReference;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

public class LocalMediaPlayer extends MediaPlayer implements Player.EventListener {

    private static final String TAG = LocalMediaPlayer.class.getSimpleName();

    private static final int MAX_CACHE_SIZE = 250_000_000;

    private static final String PODCAST_CACHE_DIR = "podcast-cache";

    private final Context context;

    private SimpleExoPlayer exoPlayer;

    private Episode episode;

    private boolean isStreaming;

    private int mediaPlayerState;

    private Playlist playlist;

    private SimpleCache cache;

    public LocalMediaPlayer(WeakReference<MediaPlayerListener> mediaPlayerListener, Context context) {
        super(mediaPlayerListener);
        this.context = context;
        mediaPlayerListener.get().setPlayer(this);
        if (exoPlayer == null) {
            TrackSelector trackSelector = new DefaultTrackSelector();
            DefaultLoadControl loadControl = new DefaultLoadControl();
            RenderersFactory renderersFactory = new DefaultRenderersFactory(this.context);
            exoPlayer = ExoPlayerFactory
                    .newSimpleInstance(this.context, renderersFactory, trackSelector, loadControl);
            exoPlayer.addListener(this);
            mediaPlayerState = MediaPlayerState.STATE_IDLE;
        }
    }

    @Override
    public Player getPlayer() {
        return exoPlayer;
    }

    @Override
    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public long getDuration() {
        return exoPlayer.getDuration();
    }

    @Override
    public boolean isStreaming() {
        return isStreaming;
    }

    @NotNull
    @Override
    public Episode getCurrentEpisode() {
        return episode;
    }

    @Override
    public void concatPlaylist(@NotNull Playlist playlist) {
        this.playlist = playlist;
        final ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        for (Episode episode : playlist.getItems()) {
            concatenatingMediaSource.addMediaSource(buildMediaSource(episode));
        }
        exoPlayer.prepare(concatenatingMediaSource, true, false);
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void onTracksChanged(final TrackGroupArray trackGroups, final TrackSelectionArray trackSelections) {
        episode = playlist.getItem(exoPlayer.getCurrentWindowIndex());
        getListenerReference().notifyEpisode(episode);
    }

    @Override
    public void selectTrack(final Episode episode) {
        this.episode = episode;
        if (playlist != null) {
            int currentIndex = playlist.getIndex(episode);
            if (currentIndex >= 0 && exoPlayer.getCurrentWindowIndex() != currentIndex) {
                exoPlayer.seekTo(currentIndex, 0);
                exoPlayer.setPlayWhenReady(true);
            }
        }

    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public void next() {
        exoPlayer.next();
    }

    @Override
    public void previous() {
        exoPlayer.previous();
    }

    @Override
    public void resumePlayback() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pausePlayback() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stopPlayback() {
        exoPlayer.stop();
        isStreaming = false;
    }

    @Override
    public void seekTo(long position) {
        exoPlayer.seekTo(position);
    }

    @Override
    public void onDestroy() {
        Timber.tag(TAG).d("Tearing down");
        exoPlayer.release();
        exoPlayer.removeListener(this);
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        String playbackStateStr;

        switch (playbackState) {
            case Player.STATE_BUFFERING:
                mediaPlayerState = MediaPlayerState.STATE_CONNECTING;
                playbackStateStr = "Buffering";
                break;
            case Player.STATE_ENDED:
                mediaPlayerState = MediaPlayerState.STATE_ENDED;
                playbackStateStr = "Ended";
                break;
            case Player.STATE_IDLE:
                mediaPlayerState = MediaPlayerState.STATE_IDLE;
                playbackStateStr = "Idle";
                break;
            case Player.STATE_READY:
                mediaPlayerState = playWhenReady ? MediaPlayerState.STATE_PLAYING :
                        MediaPlayerState.STATE_PAUSED;
                playbackStateStr = "Ready";
                break;
            default:
                mediaPlayerState = MediaPlayerState.STATE_IDLE;
                playbackStateStr = "Unknown";
                break;
        }
        getListenerReference().onStateChanged(mediaPlayerState);
        Timber.tag(TAG).d(String.format("ExoPlayer state changed: %s, Play When Ready: %s",
                playbackStateStr,
                String.valueOf(playWhenReady)));
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Toast.makeText(context, error.getCause().getMessage(), Toast.LENGTH_SHORT).show();
        Timber.w(error, "Player error encountered");
        stopPlayback();

    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void setPlaybackSpeed(float speed) {
        PlaybackParameters playbackParams = new PlaybackParameters(speed);
        exoPlayer.setPlaybackParameters(playbackParams);
    }

    private MediaSource buildMediaSource(Episode mEpisode) {
        final Uri uri = Uri.parse(mEpisode.getRemoteMediaUrl());
        final CacheDataSourceFactory dataSourceFactory = getCacheDataSource(
                new File(context.getCacheDir(), PODCAST_CACHE_DIR));
        isStreaming = true;

        if (uri != null) {
            Timber.tag(TAG).d("Playing from URI %s", uri);
            return new ExtractorMediaSource.Factory(dataSourceFactory).setTag(mEpisode.getDescription())
                    .createMediaSource(uri);
        }
        throw new IllegalStateException("Unable to build media source");
    }


    private CacheDataSourceFactory getCacheDataSource(File cacheDir) {
        if (cache == null) {
            cache = new SimpleCache(cacheDir, new LeastRecentlyUsedCacheEvictor(MAX_CACHE_SIZE));
        }

        DataSource.Factory upstream = new DefaultHttpDataSourceFactory("wall.userAgent", null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, true);
        return new CacheDataSourceFactory(cache, upstream,
                CacheDataSource.FLAG_IGNORE_CACHE_FOR_UNSET_LENGTH_REQUESTS,
                CacheDataSource.DEFAULT_MAX_CACHE_FILE_SIZE);
    }

}
