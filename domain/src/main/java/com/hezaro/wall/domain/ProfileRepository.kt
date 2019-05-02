package com.hezaro.wall.domain

import android.content.SharedPreferences
import com.hezaro.wall.data.model.UserInfo
import com.hezaro.wall.data.utils.BaseRepository
import com.hezaro.wall.sdk.base.Either
import com.hezaro.wall.sdk.base.exception.Failure
import com.hezaro.wall.sdk.base.extention.EMAIL
import com.hezaro.wall.sdk.base.extention.USER_NAME
import com.hezaro.wall.sdk.base.extention.get

interface ProfileRepository {

    fun userInfo(): Either<Failure, UserInfo>

    class ProfileRepositoryImpl(private val storage: SharedPreferences) :
        BaseRepository(),
        ProfileRepository {

        override fun userInfo(): Either<Failure, UserInfo> {
            with(storage) {
                val username = get(USER_NAME, "")
                val email = get(EMAIL, "")
                return if (email.isNotEmpty() && username.isNotEmpty())
                    Either.Right(UserInfo(username, email))
                else Either.Left(Failure.UserNotFound())
            }
        }
    }
}