package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.THEME
import com.hezaro.wall.sdk.base.extention.get

interface SplashRepository {

    fun version(): Either<Failure, Version>
    fun isNight(): Boolean

    class SplashRepositoryImpl(
        private val storage: SharedPreferences,
        private val api: ApiService
    ) :
        BaseRepository(),
        SplashRepository {

        override fun isNight() = storage.get(THEME, true)
        override fun version(): Either<Failure, Version> = request(api.version()) { it.response }
    }
}