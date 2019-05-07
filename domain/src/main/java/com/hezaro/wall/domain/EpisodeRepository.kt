package com.hezaro.wall.domain

import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.utils.BaseRepository

interface EpisodeRepository {

    fun save(episode: Episode)
    fun delete(episode: Episode)

    class EpisodeRepositoryImpl(private val database: EpisodeDao) :
        BaseRepository(),
        EpisodeRepository {

        override fun save(episode: Episode) {
            episode.isDownloaded = 1
            episode.creationDate = System.currentTimeMillis()
            database.saveEpisode(episode)
        }

        override fun delete(episode: Episode) = database.deleteIfIsNotLastPlayed(episode)
    }
}