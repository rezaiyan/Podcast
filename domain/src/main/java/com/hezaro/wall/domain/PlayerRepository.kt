package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.model.Meta
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.SPEED
import com.hezaro.wall.sdk.base.extention.get
import com.hezaro.wall.sdk.base.extention.put

interface PlayerRepository {

    fun sendLastPosition(episodeId: Int, lastPosition: Long): Either<Failure, Meta>
    fun setSpeed(speed: Float)
    fun getSpeed(): Float

    class PlayerRepositoryImpl(private val storage: SharedPreferences, private val api: ApiService) :
        BaseRepository(),
        PlayerRepository {

        override fun getSpeed(): Float = storage.get(SPEED, 1.0F)
        override fun setSpeed(speed: Float) = storage.put(SPEED, speed)
        override fun sendLastPosition(episodeId: Int, lastPosition: Long) =
            request(api.sendLastPosition(episodeId, lastPosition)) { it.meta }
    }
}