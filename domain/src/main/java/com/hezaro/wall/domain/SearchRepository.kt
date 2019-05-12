package com.hezaro.wall.domain

import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Podcast
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface SearchRepository {

    fun search(query: String, page: Int = 1, offset: Int = 20): Either<Failure, ArrayList<Episode>>
    fun podcast(): Either<Failure, ArrayList<Podcast>>

    class SearchRepositoryImpl(private val api: ApiService, private val database: EpisodeDao) :
        BaseRepository(),
        SearchRepository {

        override fun podcast(): Either<Failure, ArrayList<Podcast>> = request(api.podcast()) { it.response }

        override fun search(query: String, page: Int, offset: Int) =
            request(api.search(query = query, page = page, offset = offset)) { remote ->
                val localList = database.getAllEpisodes()
                remote.response.forEach { e ->
                    if (localList.contains(e))
                        localList[localList.indexOf(e)].update(e)
                }
                remote.response
            }
    }
}
