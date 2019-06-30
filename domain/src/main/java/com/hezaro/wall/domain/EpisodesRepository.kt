package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Status.Companion.SortBy
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.get

interface EpisodesRepository {

    fun episodes(page: Int = 1, offset: Int = 20, sortBy: @SortBy String): Either<Failure, ArrayList<Episode>>

    class EpisodesRepositoryImpl(
        private val storage: SharedPreferences,
        private val api: ApiService,
        private val database: EpisodeDao
    ) :
        BaseRepository(),
        EpisodesRepository {

        override fun episodes(page: Int, offset: Int, sortBy: @SortBy String) =

            request(api.episodes(sort = sortBy, page = page, offset = offset)) { remote ->
                val localList = database.getAllEpisodes(storage.get(EMAIL, ""))
                remote.response.forEach { e ->
                    if (localList.contains(e))
                        localList[localList.indexOf(e)].update(e)
                }
                remote.response
            }
    }
}