package com.hezaro.wall.domain

import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.model.Explore
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

/**
 * @author ali (alirezaiyann@gmail.com)
 * @since 6/25/19 10:47 AM.
 */

interface ExploreRepository {

    fun explore(): Either<Failure, Explore>

    class ExploreRepositoryImpl(private val api: ApiService) :
        BaseRepository(),
        ExploreRepository {

        override fun explore() =
            request(api.explore()) { it.response }

    }
}