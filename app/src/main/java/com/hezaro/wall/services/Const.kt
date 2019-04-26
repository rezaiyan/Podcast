package com.hezaro.wall.services

import android.support.v4.media.session.PlaybackStateCompat

const val DEFAULT_PLAYBACK_SPEED = 1.0f

const val MS_TO_REVERSE_ON_PAUSE = 0

const val SAVE_INSTANCE_EPISODES = "com.hezaro.wall.explore"
const val RC_SIGN_IN = 991
const val MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_FAST_FORWARD or
        PlaybackStateCompat.ACTION_REWIND or
        PlaybackStateCompat.ACTION_SEEK_TO or
        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS

const val ACTION_PLAY_QUEUE = "com.hezaro.wall.playQueue"

const val ACTION_PLAY_EPISODE = "com.hezaro.wall.playNew"

const val ACTION_PLAY_PLAYLIST = "com.hezaro.wall.concatPlaylist"

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

const val PARAM_EPISODE = "episode"

const val PARAM_SEEK_MS = "seekMs"

const val PARAM_PLAYBACK_SPEED = "playbackSpeed"