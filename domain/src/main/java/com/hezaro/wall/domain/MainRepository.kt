package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.local.EpisodeDao
import com.hezaro.wall.data.model.Episode
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.model.Version
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.JWT
import com.hezaro.wall.sdk.base.extention.USER_NAME
import com.hezaro.wall.sdk.base.extention.put

interface MainRepository {

    fun login(idToken: String): Either<Failure, UserInfo>
    fun version(): Either<Failure, Version>
    fun retrieveLatestPlayedEpisode(): Episode?

    class MainRepositoryImpl(
        private val api: ApiService,
        private val storage: SharedPreferences,
        private val database: EpisodeDao
    ) :
        BaseRepository(),
        MainRepository {

        override fun retrieveLatestPlayedEpisode(): Episode? = database.getLastPlayedEpisode()

        override fun version(): Either<Failure, Version> = request(api.version()) { it.response }

        override fun login(idToken: String) =
            request(api.login(idToken)) {
                it.response.also { i ->

                    storage.apply {
                        put(USER_NAME, i.username)
                        put(EMAIL, i.email)
                        put(JWT, i.jwt)
                    }

                }

            }
    }
}