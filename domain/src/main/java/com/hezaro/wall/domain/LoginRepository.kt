package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.remote.ApiService
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.JWT
import com.hezaro.wall.sdk.base.extention.USER_NAME
import com.hezaro.wall.sdk.base.extention.put

interface LoginRepository {

    fun login(idToken: String): Either<Failure, UserInfo>

    class LoginRepositoryImpl(private val api: ApiService, private val storage: SharedPreferences) :
        BaseRepository(),
        LoginRepository {

        override fun login(idToken: String): Either<Failure, UserInfo> {
            return request(api.login(idToken)) {
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
}