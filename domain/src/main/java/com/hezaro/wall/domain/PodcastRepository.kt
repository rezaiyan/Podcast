package com.hezaro.wall.domain

import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface PodcastRepository {

    fun getEpisodes(podcastId: Long): Either<Failure, ArrayList<Episode>>

    class PodcastRepositoryImpl(
        private val api: ApiService
    ) :
        BaseRepository(),
        PodcastRepository {

        override fun getEpisodes(podcastId: Long): Either<Failure, ArrayList<Episode>> =
            request(api.episodesOfPodcast(podcastId)) { it.response }
    }
}