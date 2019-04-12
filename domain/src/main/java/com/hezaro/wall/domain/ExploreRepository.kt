package com.hezaro.wall.domain

import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface ExploreRepository {

    fun explore(): Either<Failure, Nothing>

    class ExploreRepositoryImpl(private val api: ApiService) :
        BaseRepository(),
        ExploreRepository {
        override fun explore(): Either<Failure, Nothing> {
            return request(api.explore()) { it }
        }
    }
}