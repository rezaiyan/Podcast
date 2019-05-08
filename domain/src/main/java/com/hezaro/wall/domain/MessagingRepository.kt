package com.hezaro.wall.domain

import com.hezaro.wall.data.model.Meta
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure

interface MessagingRepository {

    fun sendToken(token: String): Either<Failure, Meta>

    class ProfileRepositoryImpl(private val api: ApiService) :
        BaseRepository(),
        MessagingRepository {

        override fun sendToken(token: String) = request(api.sendApi(token)) { it.meta }
    }
}