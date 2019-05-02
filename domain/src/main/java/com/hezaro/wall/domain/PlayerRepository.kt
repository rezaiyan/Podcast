package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.extention.SPEED
import com.hezaro.wall.sdk.base.extention.get
import com.hezaro.wall.sdk.base.extention.put

interface PlayerRepository {

    fun setSpeed(speed: Float)
    fun getSpeed(): Float

    class PlayerRepositoryImpl(private val storage: SharedPreferences) :
        BaseRepository(),
        PlayerRepository {

        override fun getSpeed(): Float = storage.get(SPEED, 1.0F)
        override fun setSpeed(speed: Float) = storage.put(SPEED, speed)
    }
}