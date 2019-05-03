package com.hezaro.wall.domain

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface ExploreRepository {

    fun explore(page: Int = 1, offset: Int = 20): Either<Failure, MutableList<Episode>>

    class ExploreRepositoryImpl(private val api: ApiService) :
        BaseRepository(),
        ExploreRepository {

        override fun explore(page: Int, offset: Int) =
            request(api.explore(page = page, offset = offset)) { it.response }
    }
}