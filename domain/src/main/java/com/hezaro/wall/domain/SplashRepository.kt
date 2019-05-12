package com.hezaro.wall.domain

import com.hezaro.wall.data.model.Version
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface SplashRepository {

    fun version(): Either<Failure, Version>

    class SplashRepositoryImpl(
        private val api: ApiService
    ) :
        BaseRepository(),
        SplashRepository {

        override fun version(): Either<Failure, Version> = request(api.version()) { it.response }
    }
}