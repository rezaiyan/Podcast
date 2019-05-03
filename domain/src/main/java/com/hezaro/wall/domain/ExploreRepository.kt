package com.hezaro.wall.domain

import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface ExploreRepository {

    fun explore(page: Int = 1, offset: Int = 20, sortBy: @SortBy String): Either<Failure, MutableList<Episode>>

    class ExploreRepositoryImpl(private val api: ApiService) :
        BaseRepository(),
        ExploreRepository {

        override fun explore(page: Int, offset: Int, sortBy: @SortBy String) =
            request(api.explore(sort = sortBy, page = page, offset = offset)) { it.response }
    }
}