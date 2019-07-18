package com.hezaro.wall.sdk.platform.utils

import android.support.v4.media.session.PlaybackStateCompat

val fastForwardIncrementMs = 30000L
val rewindIncrementMs = 10000L

const val DEFAULT_PLAYBACK_SPEED = 1.0f
const val MS_TO_REVERSE_ON_PAUSE = 0

const val ERROR_LOGIN_CODE = 422

const val SAVE_INSTANCE_EPISODES = "com.hezaro.wall.episodes"
const val SAVE_INSTANCE_PAGE = "com.hezaro.wall.episodes.page"
const val SAVE_INSTANCE_SORT = "com.hezaro.wall.episodes.sort"
const val RC_SIGN_IN = 991
const val MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_FAST_FORWARD or
        PlaybackStateCompat.ACTION_REWIND or
        PlaybackStateCompat.ACTION_SEEK_TO or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

const val ACTION_PLAY_QUEUE = "com.hezaro.wall.playQueue"

const val ACTION_PLAY_EPISODE_OF_PLAYLIST = "com.hezaro.wall.playNew"

const val ACTION_PLAY_SINGLE_EPISODE = "com.hezaro.wall.play.episodes"

const val ACTION_PREPARE_PLAYLIST = "com.hezaro.wall.prepare_playlist"

const val ACTION_PLAY_PLAYLIST = "com.hezaro.wall.play_playlist"

const val ACTION_CLEAR_PLAYLIST = "com.hezaro.wall.clearPlaylist"

const val ACTION_RESUME_PLAYBACK = "com.hezaro.wall.addPlaylist"

const val ACTION_PAUSE = "com.hezaro.wall.pause"

const val ACTION_PLAY_PAUSE = "com.hezaro.wall.play_pause"

const val ACTION_SEEK_FORWARD = "com.hezaro.wall.seekForward"

const val ACTION_SEEK_BACKWARD = "com.hezaro.wall.seekBackward"

const val ACTION_SEEK_TO = "com.hezaro.wall.seekTo"

const val ACTION_STOP_SERVICE = "com.hezaro.wall.stopService"

const val ACTION_SLEEP_TIMER = "com.hezaro.wall.sleepTimer"

const val ACTION_SET_SPEED = "com.hezaro.wall.setPlaybackSpeed"

const val PARAM_PLAYLIST = "playlist"

const val PARAM_EPISODE = "openEpisodeInfo"

const val PARAM_PODCAST_ID = "episodeList"

const val PARAM_PODCAST = "openPodcastInfo"

const val PARAM_SEEK_MS = "seekMs"

const val PARAM_PLAYBACK_SPEED = "playbackSpeed"

const val ACTION_PLAYER = "com.hezaro.wall.player.action"

const val ACTION_PLAYER_STATUS = "com.hezaro.wall.player.action.playStatus"

const val ACTION_EPISODE = "com.hezaro.wall.openEpisodeInfo.action"

const val ACTION_EPISODE_GET = "com.hezaro.wall.openEpisodeInfo.action.updateDownloadStatus"