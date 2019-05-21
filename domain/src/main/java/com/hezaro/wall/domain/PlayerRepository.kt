package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.Meta
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.SPEED
import com.hezaro.wall.sdk.base.extention.get
import com.hezaro.wall.sdk.base.extention.put

interface PlayerRepository {

    fun sendLastPosition(episodeId: Long, lastPosition: Long): Either<Failure, Meta>
    fun setSpeed(speed: Float)
    fun getSpeed(): Float
    fun savePlayedEpisode(episode: Episode)
    fun likeAction(like: Boolean, id: Long)
    fun userIsLogin(): Boolean
    fun updateEpisode(it: Episode)
    fun save(episode: Episode)
    fun delete(episode: Episode)

    class PlayerRepositoryImpl(
        private val storage: SharedPreferences,
        private val api: ApiService,
        private val database: EpisodeDao
    ) :
        BaseRepository(),
        PlayerRepository {

        override fun save(episode: Episode) {
            episode.creationDate = System.currentTimeMillis()
            database.saveEpisode(episode)
        }

        override fun delete(episode: Episode) = database.removeDownloaded(episode)


        override fun updateEpisode(it: Episode) =
            database.updateDownloadStatus(
                it.id,
                it.isBookmarked,
                it.likes,
                it.downloadStatus,
                it.isLastPlay,
                it.state
            )


        override fun userIsLogin() = storage.get(EMAIL, "").isNotEmpty()

        override fun likeAction(like: Boolean, id: Long) {
            if (like)
                request(api.like(id)) {}
            else
                request(api.disLike(id)) {}
        }

        override fun getSpeed(): Float = storage.get(SPEED, 1.0F)
        override fun setSpeed(speed: Float) = storage.put(SPEED, speed)
        override fun sendLastPosition(episodeId: Long, lastPosition: Long) =
            if (storage.get(EMAIL, "").isNotEmpty())
                request(api.sendLastPosition(episodeId, lastPosition)) { it.meta }
            else Either.Left(Failure.UserNotFound())

        override fun savePlayedEpisode(episode: Episode) {
            database.updateLastEpisode(episode)
        }
    }
}