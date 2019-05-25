package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.get

interface SearchRepository {

    fun search(query: String, page: Int = 1, offset: Int = 20): Either<Failure, ArrayList<Episode>>
    fun podcast(): Either<Failure, ArrayList<Podcast>>

    class SearchRepositoryImpl(
        private val storage: SharedPreferences,
        private val api: ApiService,
        private val database: EpisodeDao
    ) :
        BaseRepository(),
        SearchRepository {

        override fun podcast(): Either<Failure, ArrayList<Podcast>> = request(api.podcast()) { it.response }

        override fun search(query: String, page: Int, offset: Int) =
            request(api.search(query = query, page = page, offset = offset)) { remote ->
                val localList = database.getAllEpisodes(storage.get(EMAIL, ""))
                remote.response.forEach { e ->
                    if (localList.contains(e))
                        localList[localList.indexOf(e)].update(e)
                }
                remote.response
            }
    }
}
