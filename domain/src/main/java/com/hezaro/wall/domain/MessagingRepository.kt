package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.base.BaseRepository
import com.hezaro.wall.data.model.Meta
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.NOTIFICATION_TOKEN
import com.hezaro.wall.sdk.base.extention.put

interface MessagingRepository {

    fun sendToken(token: String): Either<Failure, Meta>

    class MessagingRepositoryImpl(private val api: ApiService, private val storage: SharedPreferences) :
        BaseRepository(),
        MessagingRepository {

        override fun sendToken(token: String) = request(api.sendApi(token)) {
            storage.put(NOTIFICATION_TOKEN, token)
            it.meta
        }
    }
}